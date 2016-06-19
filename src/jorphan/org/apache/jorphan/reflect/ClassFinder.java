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
import java.io.FilenameFilter;
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
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.util.JOrphanUtils;
import org.apache.log.Logger;

/**
 * This class finds classes that extend one of a set of parent classes
 *
 */
public final class ClassFinder {
    private static final Logger log = LoggingManager.getLoggerForClass();

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
                    String[] jars = dir.list(new FilenameFilter() {
                        @Override
                        public boolean accept(File f, String name) {
                            return name.endsWith(DOT_JAR);
                        }
                    });
                    // jars cannot be null
                    Collections.addAll(fullList, jars);
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
            log.debug("searchPathsOrJars : " + Arrays.toString(searchPathsOrJars));
            log.debug("superclass : " + Arrays.toString(classNames));
            log.debug("innerClasses : " + innerClasses + " annotations: " + annotations);
            log.debug("contains: " + contains + " notContains: " + notContains);
        }

        
        ClassFilter filter = null;
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
    
    public static List<String> findClasses(String[] searchPathsOrJars, ClassFilter filter) throws IOException  {
        if (log.isDebugEnabled()) {
            log.debug("searchPathsOrJars : " + Arrays.toString(searchPathsOrJars));
        }
    
        // Find all jars in the search path
        String[] strPathsOrJars = addJarsInPath(searchPathsOrJars);
        for (int k = 0; k < strPathsOrJars.length; k++) {
            strPathsOrJars[k] = fixPathEntry(strPathsOrJars[k]);
        }
    
        // Now eliminate any classpath entries that do not "match" the search
        List<String> listPaths = getClasspathMatches(strPathsOrJars);
        if (log.isDebugEnabled()) {
            for (String path : listPaths) {
                log.debug("listPaths : " + path);
            }
        }
    
        Set<String> listClasses = new TreeSet<>();
        // first get all the classes
        for (String path : listPaths) {
            findClassesInOnePath(path, listClasses, filter);
        }
        
        if (log.isDebugEnabled()) {
            log.debug("listClasses.size()="+listClasses.size());
            for (String clazz : listClasses) {
                log.debug("listClasses : " + clazz);
            }
        }

        return new ArrayList<>(listClasses);
    }

    /*
     * Returns the classpath entries that match the search list of jars and paths
     */
    private static List<String> getClasspathMatches(String[] strPathsOrJars) {
        final String javaClassPath = System.getProperty("java.class.path"); // $NON-NLS-1$
        StringTokenizer stPaths =
            new StringTokenizer(javaClassPath, File.pathSeparator);
        if (log.isDebugEnabled()) {
            log.debug("Classpath = " + javaClassPath);
            for (int i = 0; i < strPathsOrJars.length; i++) {
                log.debug("strPathsOrJars[" + i + "] : " + strPathsOrJars[i]);
            }
        }

        // find all jar files or paths that end with strPathOrJar
        List<String> listPaths = new ArrayList<>();
        String strPath = null;
        while (stPaths.hasMoreTokens()) {
            strPath = fixPathEntry(stPaths.nextToken());
            if (strPathsOrJars == null) {
                log.debug("Adding: " + strPath);
                listPaths.add(strPath);
            } else {
                boolean found = false;
                for (int i = 0; i < strPathsOrJars.length; i++) {
                    if (strPath.endsWith(strPathsOrJars[i])) {
                        found = true;
                        log.debug("Adding " + strPath + " found at " + i);
                        listPaths.add(strPath);
                        break;// no need to look further
                    }
                }
                if (!found) {
                    log.debug("Did not find: " + strPath);
                }
            }
        }
        return listPaths;
    }

    /**
     * Fix a path:
     * - replace "." by current directory
     * - trim any trailing spaces
     * - replace \ by /
     * - replace // by /
     * - remove all trailing /
     */
    private static String fixPathEntry(String path){
        if (path == null ) {
            return null;
        }
        if (path.equals(".")) { // $NON-NLS-1$
            return System.getProperty("user.dir"); // $NON-NLS-1$
        }
        path = path.trim().replace('\\', '/'); // $NON-NLS-1$ // $NON-NLS-2$
        path = JOrphanUtils.substitute(path, "//", "/"); // $NON-NLS-1$// $NON-NLS-2$

        while (path.endsWith("/")) { // $NON-NLS-1$
            path = path.substring(0, path.length() - 1);
        }
        return path;
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
                    | NoClassDefFoundError e) {
                log.debug(e.getLocalizedMessage());
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
        } catch (NoClassDefFoundError | ClassNotFoundException ignored) {
            log.debug(ignored.getLocalizedMessage());
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
            ZipFile zipFile = null;
            try {
                zipFile = new ZipFile(file);
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
                log.warn("Can not open the jar " + strPath + " " + e.getLocalizedMessage(),e);
            }
            finally {
                if(zipFile != null) {
                    try {zipFile.close();} catch (Exception e) {}
                }
            }
        }
    }


    private static void findClassesInPathsDir(String strPathElement, File dir, Set<String> listClasses, ClassFilter filter) throws IOException {
        String[] list = dir.list();
        if(list == null) {
            log.warn(dir.getAbsolutePath()+" is not a folder");
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
