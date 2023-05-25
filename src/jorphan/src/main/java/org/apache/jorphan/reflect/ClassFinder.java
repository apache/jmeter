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

package org.apache.jorphan.reflect;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.JarFile;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apiguardian.api.API;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class finds classes that extend one of a set of parent classes.
 */
public final class ClassFinder {
    private static final Logger log = LoggerFactory.getLogger(ClassFinder.class);

    private static final String DOT_JAR = ".jar"; // $NON-NLS-1$
    private static final String DOT_CLASS = ".class"; // $NON-NLS-1$
    private static final int DOT_CLASS_LEN = DOT_CLASS.length();

    private static final ThreadLocal<Boolean> SKIP_JARS_WITH_JMETER_SKIP_ATTRIBUTE = new ThreadLocal<>();

    public static final String JMETER_SKIP_CLASS_SCANNING_ATTRIBUTE = "JMeter-Skip-Class-Scanning";

    @API(status = API.Status.EXPERIMENTAL, since = "5.6")
    public interface Closeable extends AutoCloseable {
        @Override
        void close();
    }

    // static only
    private ClassFinder() {
    }

    @API(status = API.Status.EXPERIMENTAL, since = "5.6")
    public static boolean getSkipJarsWithJmeterSkipClassScanningAttribute() {
        return Objects.equals(SKIP_JARS_WITH_JMETER_SKIP_ATTRIBUTE.get(), Boolean.TRUE);
    }

    /**
     * Configures if {@link ClassFinder} should skip jar files that have {@code JMeter-Skip-Class-Scanning: true}
     * manifest attribute.
     * JMeter will skip such jars when it uses both {@link java.util.ServiceLoader} and {@link ClassFinder}.
     * However, {@link ClassFinder} was public, so it was possible that custom plugins could use it, and they should
     * be able to find the implementations even if they are in jars with {@code JMeter-Skip-Class-Scanning: true}.
     * <p>
     * Sample usage:
     * <pre>
     * List&lt;String&gt; classNames;
     * try (ClassFinder.Closeable ignored = ClassFinder.skipJarsWithJmeterSkipClassScanningAttribute()) {
     *   // findClassesThatExtend will not skip jars with JMeter-Skip-Class-Scanning: true manifest attribute
     *   classNames = ClassFinder.findClassesThatExtend(...);
     * </pre>
     *
     * @return closeable that will reset "skip jar files with manifest entry" flag when closed. Use it in try-with-resources
     */
    @API(status = API.Status.INTERNAL, since = "5.6")
    public static Closeable skipJarsWithJmeterSkipClassScanningAttribute() {
        SKIP_JARS_WITH_JMETER_SKIP_ATTRIBUTE.set(true);
        return SKIP_JARS_WITH_JMETER_SKIP_ATTRIBUTE::remove;
    }

    /**
     * Loads services implementing a given interface.
     * This is an intended replacement for {@code findClassesThatExtend}.
     *
     * @param service interface that services should extend.
     * @param serviceLoader ServiceLoader to fetch services.
     * @param exceptionHandler exception handler to use for services that fail to load.
     * @return collection of services that load successfully
     * @param <S> type of service (class or interface)
     */
    public static <S> Collection<S> loadServices(
            @SuppressWarnings("BoundedWildcard") Class<S> service,
            ServiceLoader<S> serviceLoader,
            ServiceLoadExceptionHandler<? super S> exceptionHandler
    ) {
        List<S> result = new ArrayList<>();
        @SuppressWarnings("ForEachIterable")
        Iterator<S> it = serviceLoader.iterator();
        while (it.hasNext()) {
            try {
                // This can't be for-each loop because we need to catch exceptions from next()
                result.add(it.next());
            } catch (ServiceConfigurationError e) {
                // Java does not expose class name of the problematic class in question, so we extract it
                // from the message
                String message = e.getMessage();
                String className = "";
                if (message.startsWith(service.getName())) {
                    if (message.endsWith(" Unable to get public no-arg constructor")) {
                        className = message.substring(
                                service.getName().length() + ": ".length(),
                                message.length() - " Unable to get public no-arg constructor".length()
                        );
                    } else if (message.endsWith(" not a subtype")) {
                        className = message.substring(
                                service.getName().length() + ": ".length(),
                                message.length() - " not a subtype".length()
                        );
                    } else if (message.endsWith(" could not be instantiated")) {
                        className = message.substring(
                                service.getName().length() + ": ".length(),
                                message.length() - " could not be instantiated".length()
                        );
                    }
                    if (className.startsWith("Provider ")) {
                        className = className.substring("Provider ".length());
                    }
                }
                exceptionHandler.handle(service, className, e);
            }
        }
        return Collections.unmodifiableCollection(result);
    }

    /**
     * Filter updates by only storing classes
     * that extend one of the parent classes
     */
    private static class ExtendsClassFilter implements ClassFilter {

        private final Class<?>[] parents; // parent classes to check
        private final boolean inner; // are inner classes OK?

        // hack to reduce the need to load every class in non-GUI mode, which only needs functions
        // TODO perhaps use BCEL to scan class files instead?
        private final String contains; // class name should contain this string
        private final String notContains; // class name should not contain this string

        private final ClassLoader contextClassLoader
            = Thread.currentThread().getContextClassLoader(); // Potentially expensive; do it once

        ExtendsClassFilter(Class<?>[] parents, boolean inner, String contains, String notContains) {
            this.parents = parents;
            this.inner = inner;
            this.contains = contains;
            this.notContains = notContains;
        }

        @Override
        public boolean accept(String className) {
            if (contains != null && !className.contains(contains)) {
                return false; // It does not contain a required string
            }
            if (notContains != null && className.contains(notContains)) {
                return false; // It contains a banned string
            }
            if (!className.contains("$") || inner) { // $NON-NLS-1$
                return isChildOf(parents, className, contextClassLoader);
            }
            return false;
        }

        /**
         * @param parentClasses      list of classes to check for
         * @param strClassName       name of class to be checked
         * @param contextClassLoader the classloader to use
         * @return true if the class is a non-abstract, non-interface instance of at least one of the parent classes
         */
        private static boolean isChildOf(
                Class<?>[] parentClasses, String strClassName, ClassLoader contextClassLoader) {
            try {
                Class<?> targetClass = Class.forName(strClassName, false, contextClassLoader);

                if (!targetClass.isInterface()
                        && !Modifier.isAbstract(targetClass.getModifiers())) {
                    return Arrays.stream(parentClasses)
                            .anyMatch(parent -> parent.isAssignableFrom(targetClass));
                }
            } catch (UnsupportedClassVersionError | ClassNotFoundException
                    | NoClassDefFoundError | VerifyError e) {
                log.debug(e.getLocalizedMessage(), e);
            }
            return false;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return "ExtendsClassFilter [parents=" +
                    (parents != null ? Arrays.toString(parents) : "null") + ", inner=" + inner + ", contains="
                    + contains + ", notContains=" + notContains + "]";
        }
    }

    private static class AnnoClassFilter implements ClassFilter {

        private final boolean inner; // are inner classes OK?

        private final Class<? extends Annotation>[] annotations; // annotation classes to check
        private final ClassLoader contextClassLoader
            = Thread.currentThread().getContextClassLoader(); // Potentially expensive; do it once

        AnnoClassFilter(Class<? extends Annotation> []annotations, boolean inner){
            this.annotations = annotations;
            this.inner = inner;
        }

        @Override
        public boolean accept(String className) {
            if (!className.contains("$") || inner) { // $NON-NLS-1$
                return hasAnnotationOnMethod(annotations,className, contextClassLoader);
            }
            return false;
        }

        private static boolean hasAnnotationOnMethod(
                Class<? extends Annotation>[] annotations,
                String classInQuestion,
                ClassLoader contextClassLoader) {
            try {
                Class<?> c = Class.forName(classInQuestion, false, contextClassLoader);
                return Arrays.stream(c.getMethods())
                        .anyMatch(method -> Arrays.stream(annotations).anyMatch(method::isAnnotationPresent));
            } catch (NoClassDefFoundError | ClassNotFoundException | UnsupportedClassVersionError | VerifyError ignored) {
                log.debug(ignored.getLocalizedMessage(), ignored);
            }
            return false;
        }

        /**
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return "AnnoClassFilter [inner=" + inner + ", annotations=" +
                    (annotations != null ? Arrays.toString(annotations) : "null")+ "]";
        }
    }

    /**
     * Convenience method for
     * {@link #findClassesThatExtend(String[], Class[], boolean)} with the
     * option to include inner classes in the search set to false.
     *
     * @param paths        pathnames or jarfiles to search for classes
     * @param superClasses required parent class(es)
     * @return List of Strings containing discovered class names.
     * @throws IOException when scanning the classes fails
     * @deprecated use {@link #loadServices(Class, ServiceLoader, ServiceLoadExceptionHandler)} or {@code JMeterUtils#loadServicesAndScanJars}
     */
    @Deprecated
    public static List<String> findClassesThatExtend(String[] paths, Class<?>[] superClasses)
            throws IOException {
        return findClassesThatExtend(paths, superClasses, false);
    }

    // For each directory in the search path, add all the jars found there
    private static Set<File> addJarsInPath(String[] paths) {
        Set<File> fullList = new HashSet<>();
        for (final String path : paths) {
            File dir = new File(path);
            fullList.add(dir);
            if (dir.exists() && dir.isDirectory()) {
                File[] jars = dir.listFiles(f -> f.isFile() && f.getName().endsWith(DOT_JAR));
                if (jars != null) {
                    Collections.addAll(fullList, jars);
                }
            }
        }
        return fullList;
    }

    /**
     * Find classes in the provided path(s)/jar(s) that extend the class(es).
     *
     * @param strPathsOrJars pathnames or jarfiles to search for classes
     * @param superClasses   required parent class(es)
     * @param innerClasses   should we include inner classes?
     * @return List containing discovered classes
     * @throws IOException when scanning for classes fails
     * @deprecated use {@link #loadServices(Class, ServiceLoader, ServiceLoadExceptionHandler)} or {@code JMeterUtils#loadServicesAndScanJars}
     */
    @Deprecated
    public static List<String> findClassesThatExtend(String[] strPathsOrJars,
            final Class<?>[] superClasses, final boolean innerClasses)
            throws IOException  {
        return findClassesThatExtend(strPathsOrJars,superClasses,innerClasses,null,null);
    }

    /**
     * Find classes in the provided path(s)/jar(s) that extend the class(es).
     *
     * @param strPathsOrJars pathnames or jarfiles to search for classes
     * @param superClasses   required parent class(es)
     * @param innerClasses   should we include inner classes?
     * @param contains       classname should contain this string
     * @param notContains    classname should not contain this string
     * @return List containing discovered classes
     * @throws IOException when scanning classes fails
     * @deprecated use {@link #loadServices(Class, ServiceLoader, ServiceLoadExceptionHandler)} or {@code JMeterUtils#loadServicesAndScanJars}
     */
    @API(status = API.Status.DEPRECATED, since = "5.6")
    @Deprecated
    public static List<String> findClassesThatExtend(String[] strPathsOrJars,
            final Class<?>[] superClasses, final boolean innerClasses,
            String contains, String notContains)
            throws IOException  {
        return findClassesThatExtend(strPathsOrJars, superClasses, innerClasses, contains, notContains, false);
    }

    /**
     * Find classes in the provided path(s)/jar(s) that extend the class(es).
     *
     * @param strPathsOrJars pathnames or jarfiles to search for classes
     * @param annotations    required annotations
     * @param innerClasses   should we include inner classes?
     * @return List containing discovered classes
     * @throws IOException when scanning classes fails
     * @deprecated use {@link #loadServices(Class, ServiceLoader, ServiceLoadExceptionHandler)} or {@code JMeterUtils#loadServicesAndScanJars}
     */
    @API(status = API.Status.DEPRECATED, since = "5.6")
    @Deprecated
    public static List<String> findAnnotatedClasses(String[] strPathsOrJars,
            final Class<? extends Annotation>[] annotations, final boolean innerClasses)
            throws IOException  {
        return findClassesThatExtend(strPathsOrJars, annotations, innerClasses, null, null, true);
    }

    /**
     * Find classes in the provided path(s)/jar(s) that extend the class(es).
     * Inner classes are not searched.
     *
     * @param strPathsOrJars pathnames or jarfiles to search for classes
     * @param annotations    required annotations
     * @return List containing discovered classes
     * @throws IOException when scanning classes fails
     * @deprecated use {@link #loadServices(Class, ServiceLoader, ServiceLoadExceptionHandler)} or {@code JMeterUtils#loadServicesAndScanJars}
     */
    @API(status = API.Status.DEPRECATED, since = "5.6")
    @Deprecated
    public static List<String> findAnnotatedClasses(String[] strPathsOrJars,
            final Class<? extends Annotation>[] annotations)
            throws IOException  {
        return findClassesThatExtend(strPathsOrJars, annotations, false, null, null, true);
    }

    /**
     * Find classes in the provided path(s)/jar(s) that extend the class(es).
     *
     * @param searchPathsOrJars pathnames or jarfiles to search for classes
     * @param classNames        required parent class(es) or annotations
     * @param innerClasses      should we include inner classes?
     * @param contains          classname should contain this string
     * @param notContains       classname should not contain this string
     * @param annotations       true if classnames are annotations
     * @return List containing discovered classes
     * @throws IOException when scanning classes fails
     * @deprecated use {@link #loadServices(Class, ServiceLoader, ServiceLoadExceptionHandler)} or {@code JMeterUtils#loadServicesAndScanJars}
     */
    @API(status = API.Status.DEPRECATED, since = "5.6")
    @Deprecated
    public static List<String> findClassesThatExtend(String[] searchPathsOrJars,
                final Class<?>[] classNames, final boolean innerClasses,
                String contains, String notContains, boolean annotations)
                throws IOException  {
        if (log.isDebugEnabled()) {
            log.debug("findClassesThatExtend with searchPathsOrJars : {}, superclass : {}"+
                    " innerClasses : {} annotations: {} contains: {}, notContains: {}",
                    Arrays.toString(searchPathsOrJars),
                    Arrays.toString(classNames),
                    innerClasses, annotations,
                    contains, notContains);
        }

        ClassFilter filter;
        if (annotations) {
            @SuppressWarnings("unchecked")
            // Should only be called with classes that extend annotations
            final Class<? extends Annotation>[] annoclassNames = (Class<? extends Annotation>[]) classNames;
            filter = new AnnoClassFilter(annoclassNames, innerClasses);
        } else {
            filter = new ExtendsClassFilter(classNames, innerClasses, contains, notContains);
        }

        return findClasses(searchPathsOrJars, filter);
    }

    /**
     * Find all classes in the given jars that passes the class filter.
     *
     * @param searchPathsOrJars list of strings representing the jar locations
     * @param filter            {@link ClassFilter} that the classes in the jars should
     *                          conform to
     * @return list of all classes in the jars, that conform to {@code filter}
     * @throws IOException when reading the jar files fails
     * @deprecated use {@link #loadServices(Class, ServiceLoader, ServiceLoadExceptionHandler)} or {@code JMeterUtils#loadServicesAndScanJars}
     */
    @API(status = API.Status.DEPRECATED, since = "5.6")
    @Deprecated
    public static List<String> findClasses(String[] searchPathsOrJars, ClassFilter filter) throws IOException {
        if (log.isDebugEnabled()) {
            log.debug("findClasses with searchPathsOrJars : {} and classFilter : {}",
                    Arrays.toString(searchPathsOrJars), filter);
        }

        // Find all jars in the search path
        Collection<File> strPathsOrJars = addJarsInPath(searchPathsOrJars);

        // Some of the jars might be out of classpath, however java.class.path does not represent
        // the actual ClassLoader in use. For instance, NewDriver builds its own classpath

        Set<String> listClasses = new TreeSet<>();
        // first get all the classes
        for (File path : strPathsOrJars) {
            findClassesInOnePath(path, listClasses, filter);
        }

        if (log.isDebugEnabled()) {
            log.debug("listClasses.size()={}", listClasses.size());
            for (String clazz : listClasses) {
                log.debug("listClasses : {}", clazz);
            }
        }

        return new ArrayList<>(listClasses);
    }

    /**
     * Converts a class file from the text stored in a Jar file to a version
     * that can be used in Class.forName().
     *
     * @param strClassName the class name from a Jar file
     * @return String the Java-style dotted version of the name
     */
    private static String fixClassName(String strClassName) {
        String fixedClassName = strClassName.replace('\\', '.'); // $NON-NLS-1$ // $NON-NLS-2$
        fixedClassName = fixedClassName.replace('/', '.'); // $NON-NLS-1$ // $NON-NLS-2$
        // remove ".class"
        fixedClassName = fixedClassName.substring(0, fixedClassName.length() - DOT_CLASS_LEN);
        return fixedClassName;
    }


    private static void findClassesInOnePath(File file, Set<? super String> listClasses, ClassFilter filter) {
        if (file.isDirectory()) {
            findClassesInPathsDir(file.getAbsolutePath(), file, listClasses, filter);
        } else if (file.exists()) {
            if (getSkipJarsWithJmeterSkipClassScanningAttribute() && file.getName().endsWith(DOT_JAR)) {
                // Ignore jars with JMeter-Skip-Class-Scanning attribute
                try (JarFile jar = new JarFile(file)) {
                    String value = jar.getManifest().getMainAttributes().getValue(JMETER_SKIP_CLASS_SCANNING_ATTRIBUTE);
                    if (Boolean.parseBoolean(value)) {
                        log.debug(
                                "Will skip scanning jar {} with filter {} since the jar has {}={} attribute",
                                file, filter, JMETER_SKIP_CLASS_SCANNING_ATTRIBUTE, value
                        );
                        return;
                    }
                    log.info(
                            "Will scan jar {} with filter {}. Consider exposing JMeter plugins via META-INF/services, " +
                                    "and add {}=true manifest attribute so JMeter can skip classfile scanning",
                            file, filter, JMETER_SKIP_CLASS_SCANNING_ATTRIBUTE
                    );
                } catch (IOException e) {
                    log.warn("Can not open the jar {}, message: {}", file.getAbsolutePath(), e.getLocalizedMessage(), e);
                }
            }
            try (ZipFile zipFile = new ZipFile(file);
                 Stream<? extends ZipEntry> entries = zipFile.stream()) {
                entries.filter(entry -> entry.getName().endsWith(DOT_CLASS))
                        .forEach(entry -> {
                                    String fixedClassName = fixClassName(entry.getName());
                                    applyFiltering(listClasses, filter, fixedClassName);
                                }
                        );
            } catch (IOException e) {
                log.warn("Can not open the jar {}, message: {}", file.getAbsolutePath(), e.getLocalizedMessage(), e);
            }
        }
    }


    private static void findClassesInPathsDir(String strPathElement, File dir, Set<? super String> listClasses, ClassFilter filter) {
        File[] list = dir.listFiles();
        if (list == null) {
            log.warn("{} is not a folder", dir.getAbsolutePath());
            return;
        }

        for (File file : list) {
            if (file.isDirectory()) {
                // Recursive call
                findClassesInPathsDir(strPathElement, file, listClasses, filter);
            } else if (file.getPath().endsWith(DOT_CLASS) && file.exists() && (file.length() != 0)) {
                final String path = file.getPath();
                String className = path.substring(strPathElement.length() + 1,
                        path.lastIndexOf('.')) // $NON-NLS-1$
                        .replace(File.separator.charAt(0), '.');// $NON-NLS-1$
                applyFiltering(listClasses, filter, className);
            }
        }
    }

    /**
     * Run {@link ClassFilter#accept(String)} on className and add to listClasses if accept returns true
     * In case of Throwable, className will not be added
     * @param classesSet Set of class names
     * @param filter {@link ClassFilter}
     * @param className Full class name
     */
    private static void applyFiltering(Set<? super String> classesSet, ClassFilter filter, String className) {
        try {
            if (filter.accept(className)) {
                classesSet.add(className);
            }
        } catch (Throwable e) { // NOSONAR : We need to trap also Errors
            log.error("Error filtering class {}, it will be ignored", className, e);
        }
    }

}
