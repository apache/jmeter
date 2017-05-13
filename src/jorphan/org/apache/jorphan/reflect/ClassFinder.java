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

package org.apache.jorphan.reflect;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.jorphan.util.JOrphanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class finds classes that extend one of a set of parent classes
 *
 */
public final class ClassFinder {
    private static final Logger log = LoggerFactory.getLogger(ClassFinder.class);

    private static final String DOT_JAR = ".jar"; // $NON-NLS-1$
    private static final String DOT_CLASS = ".class"; // $NON-NLS-1$
    private static final int DOT_CLASS_LEN = DOT_CLASS.length();

    // static only
    private ClassFinder() {
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

        private final transient ClassLoader contextClassLoader
            = Thread.currentThread().getContextClassLoader(); // Potentially expensive; do it once

        ExtendsClassFilter(Class<?> []parents, boolean inner, String contains, String notContains){
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
                if (isChildOf(parents, className, contextClassLoader)) {
                    return true;
                }
            }
            return false;
        }
    }
    
    private static class AnnoClassFilter implements ClassFilter {
        
        private final boolean inner; // are inner classes OK?

        private final Class<? extends Annotation>[] annotations; // annotation classes to check
        private final transient ClassLoader contextClassLoader
            = Thread.currentThread().getContextClassLoader(); // Potentially expensive; do it once
        
        AnnoClassFilter(Class<? extends Annotation> []annotations, boolean inner){
            this.annotations = annotations;
            this.inner = inner;
        }
        
        @Override
        public boolean accept(String className) {
            if (!className.contains("$") || inner) { // $NON-NLS-1$
                if (hasAnnotationOnMethod(annotations,className, contextClassLoader)) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Convenience method for
     * {@link #findClassesThatExtend(String[], Class[], boolean)} with the
     * option to include inner classes in the search set to false.
     *
     * @param paths
     *            pathnames or jarfiles to search for classes
     * @param superClasses
     *            required parent class(es)
     * @return List of Strings containing discovered class names.
     * @throws IOException
     *             when scanning the classes fails
     */
    public static List<String> findClassesThatExtend(String[] paths, Class<?>[] superClasses)
        throws IOException {
        return findClassesThatExtend(paths, superClasses, false);
    }

    // For each directory in the search path, add all the jars found there
    private static String[] addJarsInPath(String[] paths) {
        Set<String> fullList = new HashSet<>();
        for (final String path : paths) {
            fullList.add(path); // Keep the unexpanded path
            // TODO - allow directories to end with .jar by removing this check?
            if (!path.endsWith(DOT_JAR)) {
                File dir = new File(path);
                if (dir.exists() && dir.isDirectory()) {
                    String[] jars = dir.list((f, name) -> name.endsWith(DOT_JAR));
                    if(jars != null) {
                        Collections.addAll(fullList, jars);
                    }
                }
            }
        }
        return fullList.toArray(new String[fullList.size()]);
    }

    /**
     * Find classes in the provided path(s)/jar(s) that extend the class(es).
     * @param strPathsOrJars - pathnames or jarfiles to search for classes
     * @param superClasses - required parent class(es)
     * @param innerClasses - should we include inner classes?
     *
     * @return List containing discovered classes
     * @throws IOException when scanning for classes fails
     */
    public static List<String> findClassesThatExtend(String[] strPathsOrJars,
            final Class<?>[] superClasses, final boolean innerClasses)
            throws IOException  {
        return findClassesThatExtend(strPathsOrJars,superClasses,innerClasses,null,null);
    }

    /**
     * Find classes in the provided path(s)/jar(s) that extend the class(es).
     * @param strPathsOrJars - pathnames or jarfiles to search for classes
     * @param superClasses - required parent class(es)
     * @param innerClasses - should we include inner classes?
     * @param contains - classname should contain this string
     * @param notContains - classname should not contain this string
     *
     * @return List containing discovered classes
     * @throws IOException when scanning classes fails
     */
    public static List<String> findClassesThatExtend(String[] strPathsOrJars,
            final Class<?>[] superClasses, final boolean innerClasses,
            String contains, String notContains)
            throws IOException  {
        return findClassesThatExtend(strPathsOrJars, superClasses, innerClasses, contains, notContains, false);
    }

    /**
     * Find classes in the provided path(s)/jar(s) that extend the class(es).
     * @param strPathsOrJars - pathnames or jarfiles to search for classes
     * @param annotations - required annotations
     * @param innerClasses - should we include inner classes?
     *
     * @return List containing discovered classes
     * @throws IOException when scanning classes fails
     */
    public static List<String> findAnnotatedClasses(String[] strPathsOrJars,
            final Class<? extends Annotation>[] annotations, final boolean innerClasses)
            throws IOException  {
        return findClassesThatExtend(strPathsOrJars, annotations, innerClasses, null, null, true);
    }

    /**
     * Find classes in the provided path(s)/jar(s) that extend the class(es).
     * Inner classes are not searched.
     *
     * @param strPathsOrJars - pathnames or jarfiles to search for classes
     * @param annotations - required annotations
     *
     * @return List containing discovered classes
     * @throws IOException when scanning classes fails
     */
    public static List<String> findAnnotatedClasses(String[] strPathsOrJars,
            final Class<? extends Annotation>[] annotations)
            throws IOException  {
        return findClassesThatExtend(strPathsOrJars, annotations, false, null, null, true);
    }

    /**
     * Find classes in the provided path(s)/jar(s) that extend the class(es).
     * @param searchPathsOrJars - pathnames or jarfiles to search for classes
     * @param classNames - required parent class(es) or annotations
     * @param innerClasses - should we include inner classes?
     * @param contains - classname should contain this string
     * @param notContains - classname should not contain this string
     * @param annotations - true if classnames are annotations
     *
     * @return List containing discovered classes
     * @throws IOException when scanning classes fails
     */
    public static List<String> findClassesThatExtend(String[] searchPathsOrJars,
                final Class<?>[] classNames, final boolean innerClasses,
                String contains, String notContains, boolean annotations)
                throws IOException  {
        if (log.isDebugEnabled()) {
            log.debug("searchPathsOrJars : {}", Arrays.toString(searchPathsOrJars));
            log.debug("superclass : {}", Arrays.toString(classNames));
            log.debug("innerClasses : {} annotations: {}", innerClasses, annotations);
            log.debug("contains: {}, notContains: {}", contains, notContains);
        }

        
        ClassFilter filter;
        if(annotations) {
            @SuppressWarnings("unchecked") // Should only be called with classes that extend annotations
            final Class<? extends Annotation>[] annoclassNames = (Class<? extends Annotation>[]) classNames;
            filter = new AnnoClassFilter(annoclassNames, innerClasses);
        }
        else {
            filter = new ExtendsClassFilter(classNames, innerClasses, contains, notContains);
        }
        
        return findClasses(searchPathsOrJars, filter);
    }

    /**
     * Find all classes in the given jars that passes the class filter.
     * 
     * @param searchPathsOrJars
     *            list of strings representing the jar locations
     * @param filter
     *            {@link ClassFilter} that the classes in the jars should
     *            conform to
     * @return list of all classes in the jars, that conform to {@code filter}
     * @throws IOException
     *             when reading the jar files fails
     */
    public static List<String> findClasses(String[] searchPathsOrJars, ClassFilter filter) throws IOException  {
        if (log.isDebugEnabled()) {
            log.debug("searchPathsOrJars : {}", Arrays.toString(searchPathsOrJars));
        }
    
        // Find all jars in the search path
        List<String> strPathsOrJars = Arrays.asList(addJarsInPath(searchPathsOrJars)).stream()
                .map(ClassFinder::fixPathEntry).collect(Collectors.toList());

        // Now eliminate any classpath entries that do not "match" the search
        List<String> listPaths = getClasspathMatches(strPathsOrJars);
        if (log.isDebugEnabled()) {
            for (String path : listPaths) {
                log.debug("listPaths : {}", path);
            }
        }
    
        Set<String> listClasses = new TreeSet<>();
        // first get all the classes
        for (String path : listPaths) {
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
     * Returns the classpath entries that match the search list of jars and paths
     * @param List can contain {@code null} element but must not be {@code null}
     * @return List of paths (jars or folders) that ends with one of the rows of strPathsOrJars
     */
    private static List<String> getClasspathMatches(List<String> strPathsOrJars) {
        final String javaClassPath = System.getProperty("java.class.path"); // $NON-NLS-1$
        if (log.isDebugEnabled()) {
            log.debug("Classpath = {}", javaClassPath);
            for (int i = 0; i < strPathsOrJars.size(); i++) {
                log.debug("strPathsOrJars[{}] : {}", i, strPathsOrJars.get(i));
            }
        }

        // find all jar files or paths that end with strPathOrJar
        List<String> listPaths = new ArrayList<>();
        String classpathElement = null;
        StringTokenizer classpathElements =
                new StringTokenizer(javaClassPath, File.pathSeparator);

        while (classpathElements.hasMoreTokens()) {
            classpathElement = fixPathEntry(classpathElements.nextToken());
            if(classpathElement == null) {
                continue;
            }
            boolean found = false;
            for (String currentStrPathOrJar : strPathsOrJars) {
                if (currentStrPathOrJar != null && classpathElement.endsWith(currentStrPathOrJar)) {
                    found = true;
                    log.debug("Adding {}", classpathElement);
                    listPaths.add(classpathElement);
                    break;// no need to look further
                }
            }
            if (!found) {
                log.debug("Did not find: {}", classpathElement);
            }
        }
        return listPaths;
    }

    /**
     * Fix a path:
     * <ul>
     * <li>replace "{@code .}" by current directory</li>
     * <li>upcase the first character if it appears to be a drive letter</li>
     * <li>trim any trailing spaces</li>
     * <li>replace {@code \} by {@code /}</li>
     * <li>replace {@code //} by {@code /}</li>
     * <li>remove all trailing {@code /}</li>
     * </ul>
     */
    private static String fixPathEntry(String path){
        if (path == null ) {
            return null;
        }
        if (path.equals(".")) { // $NON-NLS-1$
            return System.getProperty("user.dir"); // $NON-NLS-1$
        }
        String resultPath = path;
        if (path.length() > 3 && path.matches("[a-z]:\\\\.*")) { // lower-case drive letter?
            resultPath = path.substring(0, 1).toUpperCase(Locale.ROOT) + path.substring(1);
        }
        resultPath = resultPath.trim().replace('\\', '/'); // $NON-NLS-1$ // $NON-NLS-2$
        resultPath = JOrphanUtils.substitute(resultPath, "//", "/"); // $NON-NLS-1$// $NON-NLS-2$

        while (resultPath.endsWith("/")) { // $NON-NLS-1$
            resultPath = resultPath.substring(0, resultPath.length() - 1);
        }
        return resultPath;
    }

    /**
     *
     * @param parentClasses list of classes to check for
     * @param strClassName name of class to be checked
     * @param contextClassLoader the classloader to use
     * @return true if the class is a non-abstract, non-interface instance of at least one of the parent classes
     */
    private static boolean isChildOf(Class<?> [] parentClasses, String strClassName,
            ClassLoader contextClassLoader){
            // might throw an exception, assume this is ignorable
            try {
                Class<?> c = Class.forName(strClassName, false, contextClassLoader);

                if (!c.isInterface() && !Modifier.isAbstract(c.getModifiers())) {
                    for (Class<?> parentClass : parentClasses) {
                        if (parentClass.isAssignableFrom(c)) {
                            return true;
                        }
                    }
                }
            } catch (UnsupportedClassVersionError | ClassNotFoundException
                    | NoClassDefFoundError | VerifyError e) {
                log.debug(e.getLocalizedMessage(), e);
            }
        return false;
    }

    private static boolean hasAnnotationOnMethod(Class<? extends Annotation>[] annotations, String classInQuestion,
        ClassLoader contextClassLoader ){
        try{
            Class<?> c = Class.forName(classInQuestion, false, contextClassLoader);
            for(Method method : c.getMethods()) {
                for(Class<? extends Annotation> annotation : annotations) {
                    if(method.isAnnotationPresent(annotation)) {
                        return true;
                    }
                }
            }
        } catch (NoClassDefFoundError | ClassNotFoundException | UnsupportedClassVersionError | VerifyError ignored) {
            log.debug(ignored.getLocalizedMessage(), ignored);
        }
        return false;
    }


    /*
     * Converts a class file from the text stored in a Jar file to a version
     * that can be used in Class.forName().
     *
     * @param strClassName
     *            the class name from a Jar file
     * @return String the Java-style dotted version of the name
     */
    private static String fixClassName(String strClassName) {
        strClassName = strClassName.replace('\\', '.'); // $NON-NLS-1$ // $NON-NLS-2$
        strClassName = strClassName.replace('/', '.'); // $NON-NLS-1$ // $NON-NLS-2$
        // remove ".class"
        strClassName = strClassName.substring(0, strClassName.length() - DOT_CLASS_LEN);
        return strClassName;
    }

    
    private static void findClassesInOnePath(String strPath, Set<String> listClasses, ClassFilter filter) throws IOException {
        File file = new File(strPath);
        if (file.isDirectory()) {
            findClassesInPathsDir(strPath, file, listClasses, filter);
        } else if (file.exists()) {
            try (ZipFile zipFile = new ZipFile(file);){
                Enumeration<? extends ZipEntry> entries = zipFile.entries();
                while (entries.hasMoreElements()) {
                    String strEntry = entries.nextElement().toString();
                    if (strEntry.endsWith(DOT_CLASS)) {
                        String fixedClassName = fixClassName(strEntry);
                        if(filter.accept(fixedClassName)) {
                            listClasses.add(fixedClassName);
                        }
                    }
                }
            } catch (IOException e) {
                log.warn("Can not open the jar {}, message: {}", strPath, e.getLocalizedMessage(),e);
            }
        }
    }


    private static void findClassesInPathsDir(String strPathElement, File dir, Set<String> listClasses, ClassFilter filter) throws IOException {
        String[] list = dir.list();
        if(list == null) {
            log.warn("{} is not a folder", dir.getAbsolutePath());
            return;
        }
        
        for (String aList : list) {
            File file = new File(dir, aList);
            if (file.isDirectory()) {
                // Recursive call
                findClassesInPathsDir(strPathElement, file, listClasses, filter);
            }
            else if (aList.endsWith(DOT_CLASS) && file.exists() && (file.length() != 0)) {
                final String path = file.getPath();
                String className = path.substring(strPathElement.length() + 1,
                        path.lastIndexOf('.')) // $NON-NLS-1$
                        .replace(File.separator.charAt(0), '.');// $NON-NLS-1$
                if(filter.accept(className)) {
                    listClasses.add(className);
                }
            }
        }
    }
    
}
