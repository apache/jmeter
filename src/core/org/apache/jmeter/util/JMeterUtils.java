/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.apache.jmeter.util;

import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;

import org.apache.commons.io.IOUtils;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.reflect.ClassFinder;
import org.apache.jorphan.test.UnitTestManager;
import org.apache.jorphan.util.JOrphanUtils;
import org.apache.log.Logger;
import org.apache.oro.text.PatternCacheLRU;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.xml.sax.XMLReader;

/**
 * This class contains the static utility methods used by JMeter.
 *
 */
public class JMeterUtils implements UnitTestManager {
    private static final Logger log = LoggingManager.getLoggerForClass();

    private static final PatternCacheLRU patternCache = new PatternCacheLRU(
            getPropDefault("oro.patterncache.size",1000), // $NON-NLS-1$
            new Perl5Compiler());

    private static final String EXPERT_MODE_PROPERTY = "jmeter.expertMode"; // $NON-NLS-1$
    
    private static final String ENGLISH_LANGUAGE = Locale.ENGLISH.getLanguage();

    private static volatile Properties appProperties;

    private static final Vector<LocaleChangeListener> localeChangeListeners = new Vector<LocaleChangeListener>();

    private static volatile Locale locale;

    private static volatile ResourceBundle resources;

    // What host am I running on?

    //@GuardedBy("this")
    private static String localHostIP = null;
    //@GuardedBy("this")
    private static String localHostName = null;
    //@GuardedBy("this")
    private static String localHostFullName = null;

    private static volatile boolean ignoreResorces = false; // Special flag for use in debugging resources

    private static final ThreadLocal<Perl5Matcher> localMatcher = new ThreadLocal<Perl5Matcher>() {
        @Override
        protected Perl5Matcher initialValue() {
            return new Perl5Matcher();
        }
    };

    // Provide Random numbers to whomever wants one
    private static final Random rand = new Random();

    /**
     * Gets Perl5Matcher for this thread.
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
     */
    public static Properties getProperties(String file) {
        loadJMeterProperties(file);
        initLogging();
        initLocale();
        return appProperties;
    }

    /**
     * Initialise JMeter logging
     */
    public static void initLogging() {
        LoggingManager.initializeLogging(appProperties);
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
     * c.f. loadProperties
     *
     */
    public static void loadJMeterProperties(String file) {
        Properties p = new Properties(System.getProperties());
        InputStream is = null;
        try {
            File f = new File(file);
            is = new FileInputStream(f);
            p.load(is);
        } catch (IOException e) {
            try {
                is =
                    ClassLoader.getSystemResourceAsStream("org/apache/jmeter/jmeter.properties"); // $NON-NLS-1$
                if (is == null) {
                    throw new RuntimeException("Could not read JMeter properties file");
                }
                p.load(is);
            } catch (IOException ex) {
                // JMeter.fail("Could not read internal resource. " +
                // "Archive is broken.");
            }
        } finally {
            JOrphanUtils.closeQuietly(is);
        }
        appProperties = p;
    }

    /**
     * This method loads a property file that may reside in the user space, or
     * in the classpath
     *
     * @param file
     *            the file to load
     * @return the Properties from the file
     */
    public static Properties loadProperties(String file) {
        Properties p = new Properties();
        InputStream is = null;
        try {
            File f = new File(file);
            is = new FileInputStream(f);
            p.load(is);
        } catch (IOException e) {
            try {
                final URL resource = JMeterUtils.class.getClassLoader().getResource(file);
                if (resource == null) {
                    log.warn("Cannot find " + file);
                    return null;
                }
                is = resource.openStream();
                if (is == null) {
                    log.warn("Cannot open " + file);
                    return null;
                }
                p.load(is);
            } catch (IOException ex) {
                log.warn("Error reading " + file + " " + ex.toString());
                return null;
            }
        } finally {
            JOrphanUtils.closeQuietly(is);
        }
        return p;
    }

    public static PatternCacheLRU getPatternCache() {
        return patternCache;
    }

    /**
     * Get a compiled expression from the pattern cache (READ_ONLY).
     *
     * @param expression
     * @return compiled pattern
     *
     * @throws  org.apache.oro.text.regex.MalformedPatternException (Runtime)
     * This should be caught for expressions that may vary (e.g. user input)
     *
     */
    public static Pattern getPattern(String expression){
        return getPattern(expression, Perl5Compiler.READ_ONLY_MASK);
    }

    /**
     * Get a compiled expression from the pattern cache.
     *
     * @param expression RE
     * @param options e.g. READ_ONLY_MASK
     * @return compiled pattern
     *
     * @throws  org.apache.oro.text.regex.MalformedPatternException (Runtime)
     * This should be caught for expressions that may vary (e.g. user input)
     *
     */
    public static Pattern getPattern(String expression, int options){
        return patternCache.getPattern(expression, options);
    }

    public void initializeProperties(String file) {
        System.out.println("Initializing Properties: " + file);
        getProperties(file);
    }

    /**
     * Convenience method for
     * {@link ClassFinder#findClassesThatExtend(String[], Class[], boolean)}
     * with the option to include inner classes in the search set to false
     * and the path list is derived from JMeterUtils.getSearchPaths().
     *
     * @param superClass - single class to search for
     * @return List of Strings containing discovered class names.
     */
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
            for (int i = 1; i < result.length; i++) {
                result[i] = paths[i - 1];
            }
        }
        result[0] = getJMeterHome() + "/lib/ext"; // $NON-NLS-1$
        return result;
    }

    /**
     * Provide random numbers
     *
     * @param r -
     *            the upper bound (exclusive)
     */
    public static int getRandomInt(int r) {
        return rand.nextInt(r);
    }

    /**
     * Changes the current locale: re-reads resource strings and notifies
     * listeners.
     *
     * @param loc -
     *            new locale
     */
    public static void setLocale(Locale loc) {
        log.info("Setting Locale to " + loc.toString());
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
        if (loc.toString().equals("ignoreResources")){ // $NON-NLS-1$
            log.warn("Resource bundles will be ignored");
            ignoreResorces = true;
            // Keep existing settings
        } else {
            ignoreResorces = false;
            ResourceBundle resBund = ResourceBundle.getBundle("org.apache.jmeter.resources.messages", loc); // $NON-NLS-1$
            resources = resBund;
            locale = loc;
            final Locale resBundLocale = resBund.getLocale();
            if (isDefault || resBundLocale.equals(loc)) {// language change worked
            // Check if we at least found the correct language:
            } else if (resBundLocale.getLanguage().equals(loc.getLanguage())) {
                log.info("Could not find resources for '"+loc.toString()+"', using '"+resBundLocale.toString()+"'");
            } else {
                log.error("Could not find resources for '"+loc.toString()+"'");
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
        @SuppressWarnings("unchecked") // clone will produce correct type
        Iterator<LocaleChangeListener> iterator = ((Vector<LocaleChangeListener>) localeChangeListeners.clone()).iterator();

        while (iterator.hasNext()) {
            LocaleChangeListener listener = iterator.next();
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

    public static final String RES_KEY_PFX = "[res_key="; // $NON-NLS-1$

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

    /*
     * Helper method to do the actual work of fetching resources; allows
     * getResString(S,S) to be deprecated without affecting getResString(S);
     */
    private static String getResStringDefault(String key, String defaultValue) {
        if (key == null) {
            return null;
        }
        // Resource keys cannot contain spaces, and are forced to lower case
        String resKey = key.replace(' ', '_'); // $NON-NLS-1$ // $NON-NLS-2$
        resKey = resKey.toLowerCase(java.util.Locale.ENGLISH);
        String resString = null;
        try {
            resString = resources.getString(resKey);
            if (ignoreResorces ){ // Special mode for debugging resource handling
                return "["+key+"]";
            }
        } catch (MissingResourceException mre) {
            if (ignoreResorces ){ // Special mode for debugging resource handling
                return "[?"+key+"?]";
            }
            log.warn("ERROR! Resource string not found: [" + resKey + "]", mre);
            resString = defaultValue;
        }
        return resString;
    }

    /**
     * To get I18N label from properties file
     * 
     * @param key
     *            in messages.properties
     * @return I18N label without (if exists) last colon ':' and spaces
     */
    public static String getParsedLabel(String key) {
        String value = JMeterUtils.getResString(key);
        return value.replaceFirst("(?m)\\s*?:\\s*$", ""); // $NON-NLS-1$ $NON-NLS-2$
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
        try {
            return resources.getString(resKey);
        } catch (MissingResourceException e) {
        }
        return locale;
    }
    /**
     * This gets the currently defined appProperties. It can only be called
     * after the {@link #getProperties(String)} method is called.
     *
     * @return The JMeterProperties value
     */
    public static Properties getJMeterProperties() {
        return appProperties;
    }

    /**
     * This looks for the requested image in the classpath under
     * org.apache.jmeter.images. <name>
     *
     * @param name
     *            Description of Parameter
     * @return The Image value
     */
    public static ImageIcon getImage(String name) {
        try {
            return new ImageIcon(JMeterUtils.class.getClassLoader().getResource(
                    "org/apache/jmeter/images/" + name.trim())); // $NON-NLS-1$
        } catch (NullPointerException e) {
            log.warn("no icon for " + name);
            return null;
        } catch (NoClassDefFoundError e) {// Can be returned by headless hosts
            log.info("no icon for " + name + " " + e.getMessage());
            return null;
        } catch (InternalError e) {// Can be returned by headless hosts
            log.info("no icon for " + name + " " + e.getMessage());
            return null;
        }
    }

    /**
     * This looks for the requested image in the classpath under
     * org.apache.jmeter.images. <name>, and also sets the description
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
        BufferedReader fileReader = null;
        try {
            String lineEnd = System.getProperty("line.separator"); // $NON-NLS-1$
            fileReader = new BufferedReader(new InputStreamReader(JMeterUtils.class.getClassLoader()
                    .getResourceAsStream(name)));
            StringBuilder text = new StringBuilder();
            String line = "NOTNULL"; // $NON-NLS-1$
            while (line != null) {
                line = fileReader.readLine();
                if (line != null) {
                    text.append(line);
                    text.append(lineEnd);
                }
            }
            // Done by finally block: fileReader.close();
            return text.toString();
        } catch (NullPointerException e) // Cannot find file
        {
            return ""; // $NON-NLS-1$
        } catch (IOException e) {
            return ""; // $NON-NLS-1$
        } finally {
            IOUtils.closeQuietly(fileReader);
        }
    }

    /**
     * Creates the vector of Timers plugins.
     *
     * @param properties
     *            Description of Parameter
     * @return The Timers value
     */
    public static Vector<Object> getTimers(Properties properties) {
        return instantiate(getVector(properties, "timer."), // $NON-NLS-1$
                "org.apache.jmeter.timers.Timer"); // $NON-NLS-1$
    }

    /**
     * Creates the vector of visualizer plugins.
     *
     * @param properties
     *            Description of Parameter
     * @return The Visualizers value
     */
    public static Vector<Object> getVisualizers(Properties properties) {
        return instantiate(getVector(properties, "visualizer."), // $NON-NLS-1$
                "org.apache.jmeter.visualizers.Visualizer"); // $NON-NLS-1$
    }

    /**
     * Creates a vector of SampleController plugins.
     *
     * @param properties
     *            The properties with information about the samplers
     * @return The Controllers value
     */
    // TODO - does not appear to be called directly
    public static Vector<Object> getControllers(Properties properties) {
        String name = "controller."; // $NON-NLS-1$
        Vector<Object> v = new Vector<Object>();
        Enumeration<?> names = properties.keys();
        while (names.hasMoreElements()) {
            String prop = (String) names.nextElement();
            if (prop.startsWith(name)) {
                Object o = instantiate(properties.getProperty(prop),
                        "org.apache.jmeter.control.SamplerController"); // $NON-NLS-1$
                v.addElement(o);
            }
        }
        return v;
    }

    /**
     * Create a string of class names for a particular SamplerController
     *
     * @param properties
     *            The properties with info about the samples.
     * @param name
     *            The name of the sampler controller.
     * @return The TestSamples value
     */
    public static String[] getTestSamples(Properties properties, String name) {
        return getVector(properties, name + ".testsample").toArray(new String[0]); // $NON-NLS-1$
    }

    /**
     * Create an instance of an org.xml.sax.Parser based on the default props.
     *
     * @return The XMLParser value
     */
    public static XMLReader getXMLParser() {
        XMLReader reader = null;
        try {
            reader = (XMLReader) instantiate(getPropDefault("xml.parser", // $NON-NLS-1$
                    "org.apache.xerces.parsers.SAXParser"), // $NON-NLS-1$
                    "org.xml.sax.XMLReader"); // $NON-NLS-1$
            // reader = xmlFactory.newSAXParser().getXMLReader();
        } catch (Exception e) {
            reader = (XMLReader) instantiate(getPropDefault("xml.parser", // $NON-NLS-1$
                    "org.apache.xerces.parsers.SAXParser"), // $NON-NLS-1$
                    "org.xml.sax.XMLReader"); // $NON-NLS-1$
        }
        return reader;
    }

    /**
     * Creates the vector of alias strings.
     *
     * @param properties
     * @return The Alias value
     */
    public static Hashtable<String, String> getAlias(Properties properties) {
        return getHashtable(properties, "alias."); // $NON-NLS-1$
    }

    /**
     * Creates a vector of strings for all the properties that start with a
     * common prefix.
     *
     * @param properties
     *            Description of Parameter
     * @param name
     *            Description of Parameter
     * @return The Vector value
     */
    public static Vector<String> getVector(Properties properties, String name) {
        Vector<String> v = new Vector<String>();
        Enumeration<?> names = properties.keys();
        while (names.hasMoreElements()) {
            String prop = (String) names.nextElement();
            if (prop.startsWith(name)) {
                v.addElement(properties.getProperty(prop));
            }
        }
        return v;
    }

    /**
     * Creates a table of strings for all the properties that start with a
     * common prefix.
     *
     * @param properties input to search
     * @param prefix to match against properties
     * @return a Hashtable where the keys are the original keys with the prefix removed
     */
    public static Hashtable<String, String> getHashtable(Properties properties, String prefix) {
        Hashtable<String, String> t = new Hashtable<String, String>();
        Enumeration<?> names = properties.keys();
        final int length = prefix.length();
        while (names.hasMoreElements()) {
            String prop = (String) names.nextElement();
            if (prop.startsWith(prefix)) {
                t.put(prop.substring(length), properties.getProperty(prop));
            }
        }
        return t;
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
        int ans;
        try {
            ans = (Integer.valueOf(appProperties.getProperty(propName, Integer.toString(defaultVal)).trim()))
                    .intValue();
        } catch (Exception e) {
            ans = defaultVal;
        }
        return ans;
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
        boolean ans;
        try {
            String strVal = appProperties.getProperty(propName, Boolean.toString(defaultVal)).trim();
            if (strVal.equalsIgnoreCase("true") || strVal.equalsIgnoreCase("t")) { // $NON-NLS-1$  // $NON-NLS-2$
                ans = true;
            } else if (strVal.equalsIgnoreCase("false") || strVal.equalsIgnoreCase("f")) { // $NON-NLS-1$  // $NON-NLS-2$
                ans = false;
            } else {
                ans = ((Integer.valueOf(strVal)).intValue() == 1);
            }
        } catch (Exception e) {
            ans = defaultVal;
        }
        return ans;
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
        long ans;
        try {
            ans = (Long.valueOf(appProperties.getProperty(propName, Long.toString(defaultVal)).trim())).longValue();
        } catch (Exception e) {
            ans = defaultVal;
        }
        return ans;
    }

    /**
     * Get a String value with default if not present.
     *
     * @param propName
     *            the name of the property.
     * @param defaultVal
     *            the default value.
     * @return The PropDefault value
     */
    public static String getPropDefault(String propName, String defaultVal) {
        String ans;
        try {
            ans = appProperties.getProperty(propName, defaultVal).trim();
        } catch (Exception e) {
            ans = defaultVal;
        }
        return ans;
    }

    /**
     * Get the value of a JMeter property.
     *
     * @param propName
     *            the name of the property.
     * @return the value of the JMeter property, or null if not defined
     */
    public static String getProperty(String propName) {
        String ans = null;
        try {
            ans = appProperties.getProperty(propName);
        } catch (Exception e) {
            ans = null;
        }
        return ans;
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
     * Sets the selection of the JComboBox to the Object 'name' from the list in
     * namVec.
     * NOTUSED?
     */
    public static void selJComboBoxItem(Properties properties, JComboBox combo, Vector<?> namVec, String name) {
        int idx = namVec.indexOf(name);
        combo.setSelectedIndex(idx);
        // Redisplay.
        combo.updateUI();
        return;
    }

    /**
     * Instatiate an object and guarantee its class.
     *
     * @param className
     *            The name of the class to instantiate.
     * @param impls
     *            The name of the class it subclases.
     * @return Description of the Returned Value
     */
    public static Object instantiate(String className, String impls) {
        if (className != null) {
            className = className.trim();
        }

        if (impls != null) {
            impls = impls.trim();
        }

        try {
            Class<?> c = Class.forName(impls);
            try {
                Class<?> o = Class.forName(className);
                Object res = o.newInstance();
                if (c.isInstance(res)) {
                    return res;
                }
                throw new IllegalArgumentException(className + " is not an instance of " + impls);
            } catch (ClassNotFoundException e) {
                log.error("Error loading class " + className + ": class is not found");
            } catch (IllegalAccessException e) {
                log.error("Error loading class " + className + ": does not have access");
            } catch (InstantiationException e) {
                log.error("Error loading class " + className + ": could not instantiate");
            } catch (NoClassDefFoundError e) {
                log.error("Error loading class " + className + ": couldn't find class " + e.getMessage());
            }
        } catch (ClassNotFoundException e) {
            log.error("Error loading class " + impls + ": was not found.");
        }
        return null;
    }

    /**
     * Instantiate a vector of classes
     *
     * @param v
     *            Description of Parameter
     * @param className
     *            Description of Parameter
     * @return Description of the Returned Value
     */
    public static Vector<Object> instantiate(Vector<String> v, String className) {
        Vector<Object> i = new Vector<Object>();
        try {
            Class<?> c = Class.forName(className);
            Enumeration<String> elements = v.elements();
            while (elements.hasMoreElements()) {
                String name = elements.nextElement();
                try {
                    Object o = Class.forName(name).newInstance();
                    if (c.isInstance(o)) {
                        i.addElement(o);
                    }
                } catch (ClassNotFoundException e) {
                    log.error("Error loading class " + name + ": class is not found");
                } catch (IllegalAccessException e) {
                    log.error("Error loading class " + name + ": does not have access");
                } catch (InstantiationException e) {
                    log.error("Error loading class " + name + ": could not instantiate");
                } catch (NoClassDefFoundError e) {
                    log.error("Error loading class " + name + ": couldn't find class " + e.getMessage());
                }
            }
        } catch (ClassNotFoundException e) {
            log.error("Error loading class " + className + ": class is not found");
        }
        return i;
    }

    /**
     * Tokenize a string into a vector of tokens
     *
     * @param string
     *            Description of Parameter
     * @param separator
     *            Description of Parameter
     * @return Description of the Returned Value
     */
    //TODO move to JOrphanUtils ?
    public static Vector<String> tokenize(String string, String separator) {
        Vector<String> v = new Vector<String>();
        StringTokenizer s = new StringTokenizer(string, separator);
        while (s.hasMoreTokens()) {
            v.addElement(s.nextToken());
        }
        return v;
    }

    /**
     * Create a button with the netscape style
     *
     * @param name
     *            Description of Parameter
     * @param listener
     *            Description of Parameter
     * @return Description of the Returned Value
     */
    public static JButton createButton(String name, ActionListener listener) {
        JButton button = new JButton(getImage(name + ".on.gif")); // $NON-NLS-1$
        button.setDisabledIcon(getImage(name + ".off.gif")); // $NON-NLS-1$
        button.setRolloverIcon(getImage(name + ".over.gif")); // $NON-NLS-1$
        button.setPressedIcon(getImage(name + ".down.gif")); // $NON-NLS-1$
        button.setActionCommand(name);
        button.addActionListener(listener);
        button.setRolloverEnabled(true);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(false);
        button.setPreferredSize(new Dimension(24, 24));
        return button;
    }

    /**
     * Create a button with the netscape style
     *
     * @param name
     *            Description of Parameter
     * @param listener
     *            Description of Parameter
     * @return Description of the Returned Value
     */
    public static JButton createSimpleButton(String name, ActionListener listener) {
        JButton button = new JButton(getImage(name + ".gif")); // $NON-NLS-1$
        button.setActionCommand(name);
        button.addActionListener(listener);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(false);
        button.setPreferredSize(new Dimension(25, 25));
        return button;
    }


    /**
     * Report an error through a dialog box.
     * Title defaults to "error_title" resource string
     * @param errorMsg - the error message.
     */
    public static void reportErrorToUser(String errorMsg) {
        reportErrorToUser(errorMsg, JMeterUtils.getResString("error_title")); // $NON-NLS-1$
    }

    /**
     * Report an error through a dialog box.
     *
     * @param errorMsg - the error message.
     * @param titleMsg - title string
     */
    public static void reportErrorToUser(String errorMsg, String titleMsg) {
        if (errorMsg == null) {
            errorMsg = "Unknown error - see log file";
            log.warn("Unknown error", new Throwable("errorMsg == null"));
        }
        GuiPackage instance = GuiPackage.getInstance();
        if (instance == null) {
            System.out.println(errorMsg);
            return; // Done
        }
        try {
            JOptionPane.showMessageDialog(instance.getMainFrame(),
                    errorMsg,
                    titleMsg,
                    JOptionPane.ERROR_MESSAGE);
        } catch (HeadlessException e) {
                log.warn("reportErrorToUser(\"" + errorMsg + "\") caused", e);
        }
    }

    /**
     * Finds a string in an array of strings and returns the
     *
     * @param array
     *            Array of strings.
     * @param value
     *            String to compare to array values.
     * @return Index of value in array, or -1 if not in array.
     */
    //TODO - move to JOrphanUtils?
    public static int findInArray(String[] array, String value) {
        int count = -1;
        int index = -1;
        if (array != null && value != null) {
            while (++count < array.length) {
                if (array[count] != null && array[count].equals(value)) {
                    index = count;
                    break;
                }
            }
        }
        return index;
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
     */
    //TODO - move to JOrphanUtils?
    public static String unsplit(Object[] splittee, Object splitChar) {
        StringBuilder retVal = new StringBuilder();
        int count = -1;
        while (++count < splittee.length) {
            if (splittee[count] != null) {
                retVal.append(splittee[count]);
            }
            if (count + 1 < splittee.length && splittee[count + 1] != null) {
                retVal.append(splitChar);
            }
        }
        return retVal.toString();
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

    // TODO needs to be synch? Probably not changed after threads have started
    private static String jmDir; // JMeter Home directory (excludes trailing separator)
    private static String jmBin; // JMeter bin directory (excludes trailing separator)


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
        return JMeterVersion.COPYRIGHT;
    }

    /**
     * Determine whether we are in 'expert' mode. Certain features may be hidden
     * from user's view unless in expert mode.
     *
     * @return true iif we're in expert mode
     */
    public static boolean isExpertMode() {
        return JMeterUtils.getPropDefault(EXPERT_MODE_PROPERTY, false);
    }

    /**
     * Find a file in the current directory or in the JMeter bin directory.
     *
     * @param fileName
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
            log.error("Unable to get local host IP address.");
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
    public static LinkedHashMap<String, String> parseHeaders(String headers) {
        LinkedHashMap<String, String> linkedHeaders = new LinkedHashMap<String, String>();
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
    
}
