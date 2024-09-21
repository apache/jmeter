/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jmeter.util;

import java.awt.Component;
import java.awt.HeadlessException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jorphan.gui.JFactory;
import org.apache.jorphan.gui.JMeterUIDefaults;
import org.apache.jorphan.reflect.ClassFinder;
import org.apache.jorphan.reflect.ServiceLoadExceptionHandler;
import org.apache.jorphan.test.UnitTestManager;
import org.apache.jorphan.util.JMeterError;
import org.apache.jorphan.util.JOrphanUtils;
import org.apache.oro.text.MalformedCachePatternException;
import org.apache.oro.text.PatternCacheLRU;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.apiguardian.api.API;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.security.AnyTypePermission;
import com.thoughtworks.xstream.security.NoTypePermission;

/**
 * This class contains the static utility methods used by JMeter.
 *
 */
public class JMeterUtils implements UnitTestManager {
    private static final Logger log = LoggerFactory.getLogger(JMeterUtils.class);

    private static final String JMETER_VARS_PREFIX = "__jm__";
    public static final String THREAD_GROUP_DISTRIBUTED_PREFIX_PROPERTY_NAME = "__jm.D_TG";

    // Note: cannot use a static variable here, because that would be processed before the JMeter properties
    // have been defined (Bug 52783)
    private static class LazyPatternCacheHolder {
        private LazyPatternCacheHolder() {
            super();
        }
        public static final PatternCacheLRU INSTANCE = new PatternCacheLRU(
                getPropDefault("oro.patterncache.size",1000), // $NON-NLS-1$
                new Perl5Compiler());
    }

    private static final class LazyJavaPatternCacheHolder {
        private LazyJavaPatternCacheHolder() {
            super();
        }
        public static final LoadingCache<Pair<String, Integer>, java.util.regex.Pattern> INSTANCE =
                Caffeine
                        .newBuilder()
                        .maximumSize(getPropDefault("jmeter.regex.patterncache.size", 1000))
                        .build(key -> {
                            //noinspection MagicConstant
                            return java.util.regex.Pattern.compile(key.getLeft(), key.getRight().intValue());
                        });
    }

    public static final String RES_KEY_PFX = "[res_key="; // $NON-NLS-1$

    private static final String EXPERT_MODE_PROPERTY = "jmeter.expertMode"; // $NON-NLS-1$

    private static final String ENGLISH_LANGUAGE = Locale.ENGLISH.getLanguage();

    private static volatile Properties appProperties;

    private static final CopyOnWriteArrayList<LocaleChangeListener> localeChangeListeners =
            new CopyOnWriteArrayList<>();

    private static volatile Locale locale;

    private static volatile ResourceBundle resources;

    // What host am I running on?
    private static String localHostIP = null;
    private static String localHostName = null;
    private static String localHostFullName = null;

    // TODO needs to be synch? Probably not changed after threads have started
    private static String jmDir; // JMeter Home directory (excludes trailing separator)
    private static String jmBin; // JMeter bin directory (excludes trailing separator)

    private static volatile boolean ignoreResources = false; // Special flag for use in debugging resources

    private static final ThreadLocal<Perl5Matcher> localMatcher = ThreadLocal.withInitial(Perl5Matcher::new);

    /**
     * Gets Perl5Matcher for this thread.
     * @return the {@link Perl5Matcher} for this thread
     */
    public static Perl5Matcher getMatcher() {
        return localMatcher.get();
    }

    /**
     * This method is used by the init method to load the property file that may
     * even reside in the user space, or in the classpath under
     * org.apache.jmeter.jmeter.properties.
     *
     * The method also initialises logging and sets up the default Locale
     *
     * TODO - perhaps remove?
     * [still used
     *
     * @param file
     *            the file to load
     * @return the Properties from the file
     * @see #getJMeterProperties()
     * @see #loadJMeterProperties(String)
     * @see #initLocale()
     */
    public static Properties getProperties(String file) {
        loadJMeterProperties(file);
        initLocale();
        return appProperties;
    }

    /**
     * Initialise JMeter logging
     * @deprecated does not do anything anymore
     */
    @Deprecated
    public static void initLogging() {
        // NOOP
    }

    /**
     * Initialise the JMeter Locale
     */
    public static void initLocale() {
        String loc = appProperties.getProperty("language"); // $NON-NLS-1$
        if (loc != null) {
            String []parts = JOrphanUtils.split(loc,"_");// $NON-NLS-1$
            if (parts.length==2) {
                setLocale(new Locale(parts[0], parts[1]));
            } else {
                setLocale(new Locale(loc, "")); // $NON-NLS-1$
            }
        } else {
            setLocale(Locale.getDefault());
        }
    }


    /**
     * Load the JMeter properties file; if not found, then
     * default to "org/apache/jmeter/jmeter.properties" from the classpath
     *
     * <p>
     * c.f. loadProperties
     *
     * @param file Name of the file from which the JMeter properties should be loaded
     */
    public static void loadJMeterProperties(String file) {
        Properties p = new Properties(System.getProperties());
        try (InputStream is = new FileInputStream(new File(file))) {
            p.load(is);
        } catch (IOException e) {
            try (InputStream is = ClassLoader.getSystemResourceAsStream(
                        "org/apache/jmeter/jmeter.properties")) { // $NON-NLS-1$
                if (is == null) {
                    throw new RuntimeException("Could not read JMeter properties file:" + file);
                }
                p.load(is);
            } catch (IOException ex) {
                throw new RuntimeException("Could not read JMeter properties file:" + file);
            }
        }
        appProperties = p;
    }

    /**
     * This method loads a property file that may reside in the user space, or
     * in the classpath
     *
     * @param file
     *            the file to load
     * @return the Properties from the file, may be null (e.g. file not found)
     */
    public static Properties loadProperties(String file) {
        return loadProperties(file, null);
    }

    /**
     * This method loads a property file that may reside in the user space, or
     * in the classpath
     *
     * @param file
     *            the file to load
     * @param defaultProps a set of default properties
     * @return the Properties from the file; if it could not be processed, the defaultProps are returned.
     */
    public static Properties loadProperties(String file, Properties defaultProps) {
        Properties p = new Properties(defaultProps);
        try (InputStream is = new FileInputStream(new File(file))) {
            p.load(is);
        } catch (IOException e) {
            final URL resource = JMeterUtils.class.getClassLoader().getResource(file);
            if (resource == null) {
                log.warn("Cannot find {}", file);
                return defaultProps;
            }
            try (InputStream is = resource.openStream()) {
                if (is == null) {
                    log.warn("Cannot open {}", file);
                    return defaultProps;
                }
                p.load(is);
            } catch (IOException ex) {
                log.warn("Error reading {} {}", file, ex.toString());
                return defaultProps;
            }
        }
        return p;
    }

    public static java.util.regex.Pattern compilePattern(String expression) {
        return compilePattern(expression, 0);
    }

    public static java.util.regex.Pattern compilePattern(String expression, int flags) {
        return LazyJavaPatternCacheHolder.INSTANCE.get(Pair.of(expression, Integer.valueOf(flags)));
    }

    public static PatternCacheLRU getPatternCache() {
        return LazyPatternCacheHolder.INSTANCE;
    }

    /**
     * Get a compiled expression from the pattern cache (READ_ONLY).
     *
     * @param expression regular expression to be looked up
     * @return compiled pattern
     *
     * @throws MalformedCachePatternException (Runtime)
     * This should be caught for expressions that may vary (e.g. user input)
     *
     */
    public static Pattern getPattern(String expression) throws MalformedCachePatternException {
        return getPattern(expression, Perl5Compiler.READ_ONLY_MASK);
    }
    /**
     * Get a compiled expression from the pattern cache.
     *
     * @param expression RE
     * @param options e.g. {@link Perl5Compiler#READ_ONLY_MASK READ_ONLY_MASK}
     * @return compiled pattern
     *
     * @throws MalformedCachePatternException (Runtime)
     * This should be caught for expressions that may vary (e.g. user input)
     *
     */
    public static Pattern getPattern(String expression, int options) throws MalformedCachePatternException {
        return LazyPatternCacheHolder.INSTANCE.getPattern(expression, options);
    }

    @Override
    public void initializeProperties(String file) {
        System.out.println("Initializing Properties: " + file); // NOSONAR intentional
        getProperties(file);
    }

    /**
     * Loads services implementing a given interface and scans JMeter search path for the implementations.
     * This is a transition replacement for {@link ClassFinder}, and JMeter would migrate to {@link ServiceLoader}-only
     * lookup in the future.
     * <p>Note: it is not always safe to cache the result as {@code search_paths} property might change over time</p>
     *
     * @param service interface that services should extend.
     * @param serviceLoader ServiceLoader to fetch services.
     * @param classLoader classLoader to use when searching for classes on the search path.
     * @param exceptionHandler exception handler to use for services that fail to load.
     * @return collection of services that load successfully
     * @param <S> type of service (class or interface)
     */
    @API(status = API.Status.DEPRECATED, since = "5.6")
    public static <S> Collection<S> loadServicesAndScanJars(
            @SuppressWarnings("BoundedWildcard") Class<S> service,
            ServiceLoader<S> serviceLoader,
            ClassLoader classLoader,
            ServiceLoadExceptionHandler<? super S> exceptionHandler
    ) {
        Collection<S> services = ClassFinder.loadServices(service, serviceLoader, exceptionHandler);

        List<String> classesFromJars;
        try (ClassFinder.Closeable ignored = ClassFinder.skipJarsWithJmeterSkipClassScanningAttribute()) {
            classesFromJars = findClassesThatExtend(service);
        } catch (IOException e) {
            log.warn("Unable to lookup {} with ClassFinder.findClassesThatExtend. " +
                    "Will use only results from ServiceLoader ({} items found)", service, services.size(), e);
            return services;
        }

        if (classesFromJars.isEmpty()) {
            return services;
        }

        Set<String> loadedClasses = new HashSet<>((int) (services.size() / 0.75f) + 1);
        for (S s : services) {
            loadedClasses.add(s.getClass().getName());
        }

        List<S> result = new ArrayList<>(services.size() + classesFromJars.size());
        result.addAll(services);
        for (String className : classesFromJars) {
            // Ignore classes that we loaded previously (e.g. with a ServiceLoader)
            if (!loadedClasses.add(className)) {
                continue;
            }
            try {
                Class<? extends S> klass = Class.forName(className, false, classLoader)
                        .asSubclass(service);
                result.add(klass.getDeclaredConstructor().newInstance());
            } catch (Throwable e) {
                if (e instanceof InvocationTargetException) {
                    //noinspection AssignmentToCatchBlockParameter
                    e = e.getCause();
                }
                exceptionHandler.handle(service, className, e);
            }
        }
        return result;
    }

    /**
     * Convenience method for
     * {@link ClassFinder#findClassesThatExtend(String[], Class[], boolean)}
     * with the option to include inner classes in the search set to false
     * and the path list is derived from JMeterUtils.getSearchPaths().
     *
     * @param superClass - single class to search for
     * @return List of Strings containing discovered class names.
     * @throws IOException when the used {@link ClassFinder} throws one while searching for the class
     * @deprecated use {@link #loadServicesAndScanJars(Class, ServiceLoader, ClassLoader, ServiceLoadExceptionHandler)} instead
     */
    @API(status = API.Status.DEPRECATED, since = "5.6")
    @Deprecated
    public static List<String> findClassesThatExtend(Class<?> superClass)
        throws IOException {
        return ClassFinder.findClassesThatExtend(getSearchPaths(), new Class[]{superClass}, false);
    }

    /**
     * Generate a list of paths to search.
     * The output array always starts with
     * JMETER_HOME/lib/ext
     * and is followed by any paths obtained from the "search_paths" JMeter property.
     *
     * @return array of path strings
     */
    public static String[] getSearchPaths() {
        String p = JMeterUtils.getPropDefault("search_paths", null); // $NON-NLS-1$
        String[] result = new String[1];

        if (p != null) {
            String[] paths = p.split(";"); // $NON-NLS-1$
            result = new String[paths.length + 1];
            System.arraycopy(paths, 0, result, 1, paths.length);
        }
        result[0] = getJMeterHome() + "/lib/ext"; // $NON-NLS-1$
        return result;
    }

    /**
     * Provide random numbers
     *
     * @param r -
     *            the upper bound (exclusive)
     * @return a random <code>int</code>
     */
    public static int getRandomInt(int r) {
        return ThreadLocalRandom.current().nextInt(r);
    }

    /**
     * Changes the current locale: re-reads resource strings and notifies
     * listeners.
     *
     * @param loc -
     *            new locale
     */
    public static void setLocale(Locale loc) {
        log.info("Setting Locale to {}", loc);
        /*
         * See bug 29920. getBundle() defaults to the property file for the
         * default Locale before it defaults to the base property file, so we
         * need to change the default Locale to ensure the base property file is
         * found.
         */
        Locale def = null;
        boolean isDefault = false; // Are we the default language?
        if (loc.getLanguage().equals(ENGLISH_LANGUAGE)) {
            isDefault = true;
            def = Locale.getDefault();
            // Don't change locale from en_GB to en
            if (!def.getLanguage().equals(ENGLISH_LANGUAGE)) {
                Locale.setDefault(Locale.ENGLISH);
            } else {
                def = null; // no need to reset Locale
            }
        }
        if ("ignoreResources".equals(loc.toString())){ // $NON-NLS-1$
            log.warn("Resource bundles will be ignored");
            ignoreResources = true;
            // Keep existing settings
        } else {
            ignoreResources = false;
            ResourceBundle resBund = ResourceBundle.getBundle("org.apache.jmeter.resources.messages", loc); // $NON-NLS-1$
            resources = resBund;
            locale = loc;
            final Locale resBundLocale = resBund.getLocale();
            if (!isDefault && !resBundLocale.equals(loc)) {
                // Check if we at least found the correct language:
                if (resBundLocale.getLanguage().equals(loc.getLanguage())) {
                    log.info("Could not find resources for '{}', using '{}'", loc, resBundLocale);
                } else {
                    log.error("Could not find resources for '{}'", loc);
                }
            }
        }
        notifyLocaleChangeListeners();
        /*
         * Reset Locale if necessary so other locales are properly handled
         */
        if (def != null) {
            Locale.setDefault(def);
        }
    }

    /**
     * Gets the current locale.
     *
     * @return current locale
     */
    public static Locale getLocale() {
        return locale;
    }

    public static void addLocaleChangeListener(LocaleChangeListener listener) {
        localeChangeListeners.add(listener);
    }

    public static void removeLocaleChangeListener(LocaleChangeListener listener) {
        localeChangeListeners.remove(listener);
    }

    /**
     * Notify all listeners interested in locale changes.
     *
     */
    private static void notifyLocaleChangeListeners() {
        LocaleChangeEvent event = new LocaleChangeEvent(JMeterUtils.class, locale);
        for (LocaleChangeListener listener : localeChangeListeners) {
            listener.localeChanged(event);
        }
    }

    /**
     * Gets the resource string for this key.
     *
     * If the resource is not found, a warning is logged
     *
     * @param key
     *            the key in the resource file
     * @return the resource string if the key is found; otherwise, return
     *         "[res_key="+key+"]"
     */
    public static String getResString(String key) {
        return getResStringDefault(key, RES_KEY_PFX + key + "]"); // $NON-NLS-1$
    }

    /**
     * Gets the resource string for this key in Locale.
     *
     * If the resource is not found, a warning is logged
     *
     * @param key
     *            the key in the resource file
     * @param forcedLocale Force a particular locale
     * @return the resource string if the key is found; otherwise, return
     *         "[res_key="+key+"]"
     * @since 2.7
     */
    public static String getResString(String key, Locale forcedLocale) {
        return getResStringDefault(key, RES_KEY_PFX + key + "]", // $NON-NLS-1$
                forcedLocale);
    }

    /**
     * Gets the resource string for this key.
     *
     * If the resource is not found, a warning is logged
     *
     * @param key
     *            the key in the resource file
     * @param defaultValue -
     *            the default value
     *
     * @return the resource string if the key is found; otherwise, return the
     *         default
     * @deprecated Only intended for use in development; use
     *             getResString(String) normally
     */
    @Deprecated
    public static String getResString(String key, String defaultValue) {
        return getResStringDefault(key, defaultValue);
    }

    /**
     * Helper method to do the actual work of fetching resources; allows
     * getResString(S,S) to be deprecated without affecting getResString(S);
     */
    private static String getResStringDefault(String key, String defaultValue) {
        return getResStringDefault(key, defaultValue, null);
    }

    /**
     * Helper method to do the actual work of fetching resources; allows
     * getResString(S,S) to be deprecated without affecting getResString(S);
     */
    private static String getResStringDefault(String key, String defaultValue, Locale forcedLocale) {
        if (key == null) {
            return null;
        }
        // Resource keys cannot contain spaces, and are forced to lower case
        String resKey = key.replace(' ', '_'); // $NON-NLS-1$ // $NON-NLS-2$
        resKey = resKey.toLowerCase(java.util.Locale.ENGLISH);
        String resString = null;
        try {
            ResourceBundle bundle = resources;
            if (forcedLocale != null || bundle == null) {
                bundle = getBundle(forcedLocale);
            }

            if (bundle.containsKey(resKey)) {
                resString = bundle.getString(resKey);
            } else {
                if(defaultValue == null) {
                    log.warn("ERROR! Resource string not found: [{}]", resKey);
                } else {
                    log.debug("Resource string not found: [{}], using default value {}", resKey, defaultValue);
                }
                resString = defaultValue;
            }
            if (ignoreResources) { // Special mode for debugging resource handling
                return "["+key+"]";
            }
        } catch (MissingResourceException mre) { // NOSONAR We handle correctly exception
            if (ignoreResources) { // Special mode for debugging resource handling
                return "[?"+key+"?]";
            }
            if(defaultValue == null) {
                log.warn("ERROR! Resource string not found: [{}]", resKey);
            } else {
                log.debug("Resource string not found: [{}], using default value {}", resKey, defaultValue);
            }
            resString = defaultValue;
        }
        return resString;
    }

    /**
     * Try to get a {@link ResourceBundle} for the given {@code forcedLocale}.
     * If none is found try to fallback to the bundle for the set {@link Locale}
     *
     * @param forcedLocale the {@link Locale} which should be used first
     * @return the resolved {@link ResourceBundle} or {@code null}, if none could be found
     */
    private static ResourceBundle getBundle(Locale forcedLocale) {
        for (Locale locale: Arrays.asList(forcedLocale, getLocale())) {
            if(locale != null) {
                ResourceBundle bundle = ResourceBundle.getBundle("org.apache.jmeter.resources.messages", locale); // $NON-NLS-1$
                if (bundle == null) {
                    log.warn("Could not resolve ResourceBundle for Locale [{}]", locale);
                } else {
                    return bundle;
                }
            }
        }
        return new DummyResourceBundle();
    }

    /**
     * Simple {@link ResourceBundle}, that handles questions for every key, by giving the key back as an answer.
     */
    private static class DummyResourceBundle extends ResourceBundle {

        @Override
        protected Object handleGetObject(String key) {
            return "[" + key + "]";
        }

        @Override
        public Enumeration<String> getKeys() {
            return Collections.emptyEnumeration();
        }
    };

    /**
     * To get I18N label from properties file
     *
     * @param key
     *            in messages.properties
     * @return I18N label without (if exists) last colon ':' and spaces
     */
    public static String getParsedLabel(String key) {
        String value = JMeterUtils.getResString(key);
        if(value != null) {
            return value.replaceFirst("(?m)\\s*?:\\s*$", ""); // $NON-NLS-1$ $NON-NLS-2$
        } else {
            return null;
        }
    }

    /**
     * Get the locale name as a resource.
     * Does not log an error if the resource does not exist.
     * This is needed to support additional locales, as they won't be in existing messages files.
     *
     * @param locale name
     * @return the locale display name as defined in the current Locale or the original string if not present
     */
    public static String getLocaleString(String locale){
        // All keys in messages.properties are lowercase (historical reasons?)
        String resKey = locale.toLowerCase(java.util.Locale.ENGLISH);
        if (resources.containsKey(resKey)) {
            return resources.getString(resKey);
        }
        return locale;
    }
    /**
     * This gets the currently defined appProperties. It can only be called
     * after the {@link #getProperties(String)} or {@link #loadJMeterProperties(String)}
     * method has been called.
     *
     * @return The JMeterProperties value,
     *         may be null if {@link #loadJMeterProperties(String)} has not been called
     * @see #getProperties(String)
     * @see #loadJMeterProperties(String)
     */
    public static Properties getJMeterProperties() {
        return appProperties;
    }

    /**
     * This looks for the requested image in the classpath under
     * org.apache.jmeter.images.&lt;name&gt;
     *
     * @param name
     *            Description of Parameter
     * @return The Image value
     */
    public static ImageIcon getImage(String name) {
        try {
            URL url = JMeterUtils.class.getClassLoader().getResource(
                    "org/apache/jmeter/images/" + name.trim());
            if(url != null) {
                return new ImageIcon(url); // $NON-NLS-1$
            } else {
                log.warn("no icon for {}", name);
                return null;
            }
        } catch (NoClassDefFoundError | InternalError e) {// Can be returned by headless hosts
            log.info("no icon for {} {}", name, e.getMessage());
            return null;
        }
    }

    /**
     * This looks for the requested image in the classpath under
     * org.apache.jmeter.images.<em>&lt;name&gt;</em>, and also sets the description
     * of the image, which is useful if the icon is going to be placed
     * on the clipboard.
     *
     * @param name
     *            the name of the image
     * @param description
     *            the description of the image
     * @return The Image value
     */
    public static ImageIcon getImage(String name, String description) {
        ImageIcon icon = getImage(name);
        if(icon != null) {
            icon.setDescription(description);
        }
        return icon;
    }

    public static String getResourceFileAsText(String name) {
        try {
            String lineEnd = System.getProperty("line.separator"); // $NON-NLS-1$
            InputStream is = JMeterUtils.class.getClassLoader().getResourceAsStream(name);
            if (is != null) {
                try (Reader in = new InputStreamReader(is, StandardCharsets.UTF_8);
                     BufferedReader fileReader = new BufferedReader(in)) {
                    return fileReader.lines()
                            .collect(Collectors.joining(lineEnd, "", lineEnd));
                }
            } else {
                return ""; // $NON-NLS-1$
            }
        } catch (IOException e) {
            return ""; // $NON-NLS-1$
        }
    }

    /**
     * Get a int value with default if not present.
     *
     * @param propName
     *            the name of the property.
     * @param defaultVal
     *            the default value.
     * @return The PropDefault value
     */
    public static int getPropDefault(String propName, int defaultVal) {
        try {
            return Integer.parseInt(appProperties.getProperty(propName, Integer.toString(defaultVal)).trim());
        } catch (Exception e) {
            log.warn("Exception '{}' occurred when fetching int property:'{}', defaulting to: {}", e.getMessage() , propName, defaultVal);
        }
        return defaultVal;
    }

    /**
     * Get a boolean value with default if not present.
     *
     * @param propName
     *            the name of the property.
     * @param defaultVal
     *            the default value.
     * @return The PropDefault value
     */
    public static boolean getPropDefault(String propName, boolean defaultVal) {
        try {
            String strVal = appProperties.getProperty(propName, Boolean.toString(defaultVal)).trim();
            if ("true".equalsIgnoreCase(strVal) || "t".equalsIgnoreCase(strVal)) { // $NON-NLS-1$  // $NON-NLS-2$
                return true;
            } else if ("false".equalsIgnoreCase(strVal) || "f".equalsIgnoreCase(strVal)) { // $NON-NLS-1$  // $NON-NLS-2$
                return false;
            } else {
                return Integer.parseInt(strVal) == 1;
            }
        } catch (Exception e) {
            log.warn("Exception '{}' occurred when fetching boolean property:'{}', defaulting to: {}", e.getMessage(), propName, defaultVal);
        }
        return defaultVal;
    }

    /**
     * Get an array of String if present and not empty, defaultValue if not present.
     *
     * @param propName
     *            the name of the property.
     * @param defaultVal
     *            the default value.
     * @return The PropDefault value
     */
    public static String[] getArrayPropDefault(String propName, String[] defaultVal) {
        try {
            String strVal = appProperties.getProperty(propName);
            if (StringUtils.isNotBlank(strVal)) {
                return strVal.trim().split("\\s+");
            }
        } catch (Exception e) {
            log.warn("Exception '{}' occurred when fetching Array property:'{}', defaulting to: {}",
                    e.getMessage(), propName, defaultVal != null ? Arrays.toString(defaultVal) : null);
        }
        return defaultVal;
    }

    /**
     * Get a long value with default if not present.
     *
     * @param propName
     *            the name of the property.
     * @param defaultVal
     *            the default value.
     * @return The PropDefault value
     */
    public static long getPropDefault(String propName, long defaultVal) {
        try {
            return Long.parseLong(appProperties.getProperty(propName, Long.toString(defaultVal)).trim());
        } catch (Exception e) {
            log.warn("Exception '{}' occurred when fetching long property:'{}', defaulting to: {}", e.getMessage(), propName, defaultVal);
        }
        return defaultVal;
    }

    /**
     * Get a float value with default if not present.
     *
     * @param propName
     *            the name of the property.
     * @param defaultVal
     *            the default value.
     * @return The PropDefault value
     */
    public static float getPropDefault(String propName, float defaultVal) {
        try {
            return Float.parseFloat(appProperties.getProperty(propName, Float.toString(defaultVal)).trim());
        } catch (Exception e) {
            log.warn("Exception '{}' occurred when fetching float property:'{}', defaulting to: {}", e.getMessage(), propName, defaultVal);
        }
        return defaultVal;
    }

    /**
     * Get a double value with default if not present.
     *
     * @param propName
     *            the name of the property.
     * @param defaultVal
     *            the default value.
     * @return The PropDefault value
     */
    public static double getPropDefault(String propName, double defaultVal) {
        try {
            return Float.parseFloat(appProperties.getProperty(propName, Double.toString(defaultVal)).trim());
        } catch (Exception e) {
            log.warn("Exception '{}' occurred when fetching double property:'{}', defaulting to: {}", e.getMessage(), propName, defaultVal);
        }
        return defaultVal;
    }

    /**
     * Get a String value with default if not present.
     *
     * @param propName
     *            the name of the property.
     * @param defaultVal
     *            the default value.
     * @return The PropDefault value applying a trim on it
     */
    public static String getPropDefault(String propName, String defaultVal) {
        try {
            String value = appProperties.getProperty(propName, defaultVal);
            if(value != null) {
                return value.trim();
            }
        } catch (Exception e) {
            log.warn("Exception '{}' occurred when fetching String property:'{}', defaulting to: {}", e.getMessage(), propName, defaultVal);
        }
        return defaultVal;
    }

    /**
     * Get the value of a JMeter property.
     *
     * @param propName
     *            the name of the property.
     * @return the value of the JMeter property, or {@code null} if not defined
     */
    public static String getProperty(String propName) {
        try {
            return appProperties.getProperty(propName);
        } catch (Exception e) {
            log.warn("Exception '{}' occurred when fetching String property:'{}'", e.getMessage(), propName);
        }
        return null;
    }

    /**
     * Set a String value
     *
     * @param propName
     *            the name of the property.
     * @param propValue
     *            the value of the property
     * @return the previous value of the property
     */
    public static Object setProperty(String propName, String propValue) {
        return appProperties.setProperty(propName, propValue);
    }

    /**
     * Report an error through a dialog box.
     * Title defaults to "error_title" resource string
     * @param errorMsg - the error message.
     */
    public static void reportErrorToUser(String errorMsg) {
        reportErrorToUser(errorMsg, JMeterUtils.getResString("error_title"), null); // $NON-NLS-1$
    }

    /**
     * Report an error through a dialog box in GUI mode
     * or in logs and stdout in Non GUI mode
     *
     * @param errorMsg - the error message.
     * @param titleMsg - title string
     */
    public static void reportErrorToUser(String errorMsg, String titleMsg) {
        reportErrorToUser(errorMsg, titleMsg, null);
    }

    /**
     * Report an error through a dialog box.
     * Title defaults to "error_title" resource string
     * @param errorMsg - the error message.
     * @param exception {@link Exception}
     */
    public static void reportErrorToUser(String errorMsg, Exception exception) {
        reportErrorToUser(errorMsg, JMeterUtils.getResString("error_title"), exception);
    }

    /**
     * Report an error through a dialog box in GUI mode
     * or in logs and stdout in Non GUI mode
     *
     * @param errorMsg - the error message.
     * @param titleMsg - title string
     * @param exception Exception
     */
    public static void reportErrorToUser(String errorMsg, String titleMsg, Exception exception) {
        if (errorMsg == null) {
            errorMsg = "Unknown error - see log file";
            log.warn("Unknown error", new Throwable("errorMsg == null"));
        }
        if (exception != null) {
            log.error(errorMsg, exception);
        } else {
            log.error(errorMsg);
        }
        GuiPackage instance = GuiPackage.getInstance();
        if (instance == null) {
            System.out.println(errorMsg); // NOSONAR intentional
            return; // Done
        }
        String errorMessage = errorMsg;
        SwingUtilities.invokeLater(() -> {
            try {
                JOptionPane.showMessageDialog(instance.getMainFrame(),
                        formatMessage(errorMessage),
                        titleMsg,
                        JOptionPane.ERROR_MESSAGE);
            } catch (HeadlessException e) {
                log.warn("reportErrorToUser(\"{}\") caused", errorMessage, e);
            }
        });
    }

    /**
     * Report an information through a dialog box in GUI mode
     *
     * @param msg - the information message.
     * @param titleMsg - title string
     */
    public static void reportInfoToUser(String msg, String titleMsg) {
        GuiPackage instance = GuiPackage.getInstance();
        if (instance == null) {
            log.info(msg);
            System.out.println(msg); // NOSONAR intentional
            return; // Done
        }
        SwingUtilities.invokeLater(() -> {
            try {
                JOptionPane.showMessageDialog(instance.getMainFrame(),
                        formatMessage(msg),
                        titleMsg,
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (HeadlessException e) {
                log.warn("reportInfoToUser(\"{}\") caused", msg, e);
            }
        });
    }

    private static JScrollPane formatMessage(String errorMsg) {
        JTextArea ta = new JTextArea(10, 50);
        ta.setText(errorMsg);
        ta.setWrapStyleWord(true);
        ta.setLineWrap(true);
        ta.setCaretPosition(0);
        ta.setEditable(false);
        return new JScrollPane(ta);
    }

    /**
     * Creates {@link JLabel} that is associated with a given {@link Component} instance.
     * @param component component for the label
     * @param resourceId resource ID to be used for retrieving label text
     * @return JLabel instance
     */
    public static JLabel labelFor(Component component, String resourceId) {
        JLabel label = new JLabel(getResString(resourceId));
        label.setName(resourceId);
        label.setLabelFor(component);
        return label;
    }

    /**
     * Creates {@link JLabel} that is associated with a given {@link Component} instance.
     * @param component component for the label
     * @param labelValue label text
     * @param name JLabel name
     * @return JLabel instance
     */
    public static JLabel labelFor(Component component, String labelValue, String name) {
        JLabel label = new JLabel(labelValue);
        label.setName(name);
        label.setLabelFor(component);
        return label;
    }

    /**
     * Takes an array of strings and a tokenizer character, and returns a string
     * of all the strings concatenated with the tokenizer string in between each
     * one.
     *
     * @param splittee
     *            Array of Objects to be concatenated.
     * @param splitChar
     *            Object to unsplit the strings with.
     * @return Array of all the tokens.
     * @deprecated use {@link JOrphanUtils#unsplit(Object[], Object)}
     */
    @Deprecated
    public static String unsplit(Object[] splittee, Object splitChar) {
        return JOrphanUtils.unsplit(splittee, splitChar);
    }

    // End Method

    /**
     * Takes an array of strings and a tokenizer character, and returns a string
     * of all the strings concatenated with the tokenizer string in between each
     * one.
     *
     * @param splittee
     *            Array of Objects to be concatenated.
     * @param splitChar
     *            Object to unsplit the strings with.
     * @param def
     *            Default value to replace null values in array.
     * @return Array of all the tokens.
     */
    //TODO - move to JOrphanUtils?
    public static String unsplit(Object[] splittee, Object splitChar, String def) {
        StringBuilder retVal = new StringBuilder();
        int count = -1;
        while (++count < splittee.length) {
            if (splittee[count] != null) {
                retVal.append(splittee[count]);
            } else {
                retVal.append(def);
            }
            if (count + 1 < splittee.length) {
                retVal.append(splitChar);
            }
        }
        return retVal.toString();
    }

    /**
     * @return true if test is running
     */
    public static boolean isTestRunning() {
        return JMeterContextService.getTestStartTime()>0;
    }

    /**
     * Get the JMeter home directory - does not include the trailing separator.
     *
     * @return the home directory
     */
    public static String getJMeterHome() {
        return jmDir;
    }

    /**
     * Get the JMeter bin directory - does not include the trailing separator.
     *
     * @return the bin directory
     */
    public static String getJMeterBinDir() {
        return jmBin;
    }

    public static void setJMeterHome(String home) {
        jmDir = home;
        jmBin = jmDir + File.separator + "bin"; // $NON-NLS-1$
    }

    /**
     * Gets the JMeter Version.
     *
     * @return the JMeter version string
     */
    public static String getJMeterVersion() {
        return JMeterVersion.getVERSION();
    }

    /**
     * Gets the JMeter copyright.
     *
     * @return the JMeter copyright string
     */
    public static String getJMeterCopyright() {
        return JMeterVersion.getCopyRight();
    }

    /**
     * Determine whether we are in 'expert' mode. Certain features may be hidden
     * from user's view unless in expert mode.
     *
     * @return true if we're in expert mode
     */
    public static boolean isExpertMode() {
        return JMeterUtils.getPropDefault(EXPERT_MODE_PROPERTY, false);
    }

    /**
     * Find a file in the current directory or in the JMeter bin directory.
     *
     * @param fileName the name of the file to find
     * @return File object
     */
    public static File findFile(String fileName){
        File f =new File(fileName);
        if (!f.exists()){
            f=new File(getJMeterBinDir(),fileName);
        }
        return f;
    }

    /**
     * Returns the cached result from calling
     * InetAddress.getLocalHost().getHostAddress()
     *
     * @return String representation of local IP address
     */
    public static synchronized String getLocalHostIP(){
        if (localHostIP == null) {
            getLocalHostDetails();
        }
        return localHostIP;
    }

    /**
     * Returns the cached result from calling
     * InetAddress.getLocalHost().getHostName()
     *
     * @return local host name
     */
    public static synchronized String getLocalHostName(){
        if (localHostName == null) {
            getLocalHostDetails();
        }
        return localHostName;
    }

    /**
     * Returns the cached result from calling
     * InetAddress.getLocalHost().getCanonicalHostName()
     *
     * @return local host name in canonical form
     */
    public static synchronized String getLocalHostFullName(){
        if (localHostFullName == null) {
            getLocalHostDetails();
        }
        return localHostFullName;
    }

    private static void getLocalHostDetails(){
        InetAddress localHost=null;
        try {
            localHost = InetAddress.getLocalHost();
        } catch (UnknownHostException e1) {
            log.error("Unable to get local host IP address.", e1);
            return; // TODO - perhaps this should be a fatal error?
        }
        localHostIP=localHost.getHostAddress();
        localHostName=localHost.getHostName();
        localHostFullName=localHost.getCanonicalHostName();
    }

    /**
     * Split line into name/value pairs and remove colon ':'
     *
     * @param headers
     *            multi-line string headers
     * @return a map name/value for each header
     */
    @SuppressWarnings("NonApiType")
    public static LinkedHashMap<String, String> parseHeaders(String headers) {
        LinkedHashMap<String, String> linkedHeaders = new LinkedHashMap<>();
        String[] list = headers.split("\n"); // $NON-NLS-1$
        for (String header : list) {
            int colon = header.indexOf(':'); // $NON-NLS-1$
            if (colon <= 0) {
                linkedHeaders.put(header, ""); // Empty value // $NON-NLS-1$
            } else {
                linkedHeaders.put(header.substring(0, colon).trim(), header
                        .substring(colon + 1).trim());
            }
        }
        return linkedHeaders;
    }

    /**
     * Run the runnable in AWT Thread if current thread is not AWT thread
     * otherwise runs call {@link SwingUtilities#invokeAndWait(Runnable)}
     * @param runnable {@link Runnable}
     */
    public static void runSafe(Runnable runnable) {
        runSafe(true, runnable);
    }

    /**
     * Run the runnable in AWT Thread if current thread is not AWT thread
     * otherwise runs call {@link SwingUtilities#invokeAndWait(Runnable)}
     * @param synchronous flag, whether we will wait for the AWT Thread to finish its job.
     * @param runnable {@link Runnable}
     */
    public static void runSafe(boolean synchronous, Runnable runnable) {
        if(SwingUtilities.isEventDispatchThread()) {
            runnable.run();//NOSONAR
        } else {
            if (synchronous) {
                try {
                    SwingUtilities.invokeAndWait(runnable);
                } catch (InterruptedException e) {
                    log.warn("Interrupted in thread {}",
                            Thread.currentThread().getName(), e);
                    Thread.currentThread().interrupt();
                } catch (InvocationTargetException e) {
                    throw new Error(e);
                }
            } else {
                SwingUtilities.invokeLater(runnable);
            }
        }
    }

    /**
     * Help GC by triggering GC and finalization
     */
    public static void helpGC() {
        System.gc(); // NOSONAR Intentional
        System.runFinalization();
    }

    /**
     * Hack to make matcher clean the two internal buffers it keeps in memory which size is equivalent to
     * the unzipped page size
     * @param matcher {@link Perl5Matcher}
     * @param pattern Pattern
     */
    public static void clearMatcherMemory(Perl5Matcher matcher, Pattern pattern) {
        try {
            if (pattern != null) {
                matcher.matches("", pattern); // $NON-NLS-1$
            }
        } catch (Exception e) {
            // NOOP
        }
    }

    /**
     * Provide info, whether we run in HiDPI mode
     * @return {@code true} if we run in HiDPI mode, {@code false} otherwise
     */
    @API(since = "5.3", status = API.Status.DEPRECATED)
    public static boolean getHiDPIMode() {
        return JMeterUtils.getPropDefault("jmeter.hidpi.mode", false);  // $NON-NLS-1$
    }

    /**
     * Provide info about the HiDPI scale factor
     * @return the factor by which we should scale elements for HiDPI mode
     */
    @API(since = "5.3", status = API.Status.DEPRECATED)
    public static double getHiDPIScaleFactor() {
        return Double.parseDouble(JMeterUtils.getPropDefault("jmeter.hidpi.scale.factor", "1.0"));  // $NON-NLS-1$  $NON-NLS-2$
    }

    /**
     * Apply HiDPI mode management to {@link JTable}
     * @param table the {@link JTable} which should be adapted for HiDPI mode
     */
    @API(since = "5.3", status = API.Status.DEPRECATED)
    public static void applyHiDPI(JTable table) {
        JFactory.singleLineRowHeight(table);
    }

    /**
     * Return delimiterValue handling the TAB case
     * @param delimiterValue Delimited value
     * @return String delimited modified to handle correctly tab
     * @throws JMeterError if delimiterValue has a length different from 1
     */
    public static String getDelimiter(String delimiterValue) {
        if ("\\t".equals(delimiterValue)) {// Make it easier to enter a tab (can use \<tab> but that is awkward)
            delimiterValue="\t";
        }

        if (delimiterValue.length() != 1){
            throw new JMeterError("Delimiter '"+delimiterValue+"' must be of length 1.");
        }
        return delimiterValue;
    }

    /**
     * Apply HiDPI scale factor on font if HiDPI mode is enabled
     */
    @API(since = "5.3", status = API.Status.DEPRECATED)
    public static void applyHiDPIOnFonts() {
        if (!getHiDPIMode()) {
            return;
        }
        applyScaleOnFonts((float) getHiDPIScaleFactor());
    }

    /**
     * Apply HiDPI scale factor on fonts
     * @param scale float scale to apply
     */
    @API(since = "5.3", status = API.Status.DEPRECATED)
    public static void applyScaleOnFonts(final float scale) {
        JMeterUIDefaults defaults = JMeterUIDefaults.INSTANCE;
        defaults.setScale(defaults.getScale() * scale);
    }

    /**
     * Refresh UI after LAF change or resizing
     */
    public static void refreshUI() {
        GuiPackage.getInstance().updateUIForHiddenComponents();
        JFactory.refreshUI();
    }

    /**
     * Setup default security policy
     * @param xstream {@link XStream}
     */
    public static void setupXStreamSecurityPolicy(XStream xstream) {
        // This will lift the insecure warning
        xstream.addPermission(NoTypePermission.NONE);
        // We reapply very permissive policy
        // See https://groups.google.com/forum/#!topic/xstream-user/wiKfdJPL8aY
        // TODO : How much are we concerned by CVE-2013-7285
        xstream.addPermission(AnyTypePermission.ANY);
    }

    /**
     * @param elementName String elementName
     * @return variable name for index following JMeter convention
     */
    public static String formatJMeterExportedVariableName(String elementName) {
        return JMETER_VARS_PREFIX + elementName;
    }

    /**
     * @return {@link XStream} XStream instance following JMeter security policy
     */
    public static final XStream createXStream() {
        XStream xstream = new XStream();
        JMeterUtils.setupXStreamSecurityPolicy(xstream);
        return xstream;
    }
}
