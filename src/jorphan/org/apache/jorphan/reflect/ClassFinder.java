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
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
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
     * Filter updates to TreeSet by only storing classes
     * that extend one of the parent classes
     *
     *
     */
    private static class FilterTreeSet extends TreeSet<String>{
        private static final long serialVersionUID = 234L;

        private final Class<?>[] parents; // parent classes to check
        private final boolean inner; // are inner classes OK?

        // hack to reduce the need to load every class in non-GUI mode, which only needs functions
        // TODO perhaps use BCEL to scan class files instead?
        private final String contains; // class name should contain this string
        private final String notContains; // class name should not contain this string

        private final transient ClassLoader contextClassLoader
            = Thread.currentThread().getContextClassLoader(); // Potentially expensive; do it once

        FilterTreeSet(Class<?> []parents, boolean inner, String contains, String notContains){
            super();
            this.parents=parents;
            this.inner=inner;
            this.contains=contains;
            this.notContains=notContains;
        }

        /**
         * Override the superclass so we only add classnames that
         * meet the criteria.
         *
         * @param s - classname (must be a String)
         * @return true if it is a new entry
         *
         * @see java.util.TreeSet#add(java.lang.Object)
         */
        @Override
        public boolean add(String s){
            if (contains(s)) {
                return false;// No need to check it again
            }
            if (contains!=null && s.indexOf(contains) == -1){
                return false; // It does not contain a required string
            }
            if (notContains!=null && s.indexOf(notContains) != -1){
                return false; // It contains a banned string
            }
            if ((s.indexOf("$") == -1) || inner) { // $NON-NLS-1$
                if (isChildOf(parents,s, contextClassLoader)) {
                    return super.add(s);
                }
            }
            return false;
        }
    }

    private static class AnnoFilterTreeSet extends TreeSet<String>{
        private static final long serialVersionUID = 240L;

        private final boolean inner; // are inner classes OK?

        private final Class<? extends Annotation>[] annotations; // annotation classes to check
        private final transient ClassLoader contextClassLoader
            = Thread.currentThread().getContextClassLoader(); // Potentially expensive; do it once
        AnnoFilterTreeSet(Class<? extends Annotation> []annotations, boolean inner){
            super();
            this.annotations = annotations;
            this.inner=inner;
        }
        /**
         * Override the superclass so we only add classnames that
         * meet the criteria.
         *
         * @param s - classname (must be a String)
         * @return true if it is a new entry
         *
         * @see java.util.TreeSet#add(java.lang.Object)
         */
        @Override
        public boolean add(String s){
            if (contains(s)) {
                return false;// No need to check it again
            }
            if ((s.indexOf("$") == -1) || inner) { // $NON-NLS-1$
                if (hasAnnotationOnMethod(annotations,s, contextClassLoader)) {
                    return super.add(s);
                }
            }
            return false;
        }
    }

    /**
     * Convenience method for
     * {@link #findClassesThatExtend(String[], Class[], boolean)}
     * with the option to include inner classes in the search set to false.
     *
     * @return List of Strings containing discovered class names.
     */
    public static List<String> findClassesThatExtend(String[] paths, Class<?>[] superClasses)
        throws IOException {
        return findClassesThatExtend(paths, superClasses, false);
    }

    // For each directory in the search path, add all the jars found there
    private static String[] addJarsInPath(String[] paths) {
        Set<String> fullList = new HashSet<String>();
        for (int i = 0; i < paths.length; i++) {
            final String path = paths[i];
            fullList.add(path); // Keep the unexpanded path
            // TODO - allow directories to end with .jar by removing this check?
            if (!path.endsWith(DOT_JAR)) {
                File dir = new File(path);
                if (dir.exists() && dir.isDirectory()) {
                    String[] jars = dir.list(new FilenameFilter() {
                        public boolean accept(File f, String name) {
                            return name.endsWith(DOT_JAR);
                        }
                    });
                    for (int x = 0; x < jars.length; x++) {
                        fullList.add(jars[x]);
                    }
                }
            }
        }
        return fullList.toArray(new String[0]);
    }

    /**
     * Find classes in the provided path(s)/jar(s) that extend the class(es).
     * @param strPathsOrJars - pathnames or jarfiles to search for classes
     * @param superClasses - required parent class(es)
     * @param innerClasses - should we include inner classes?
     *
     * @return List containing discovered classes
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
     */
    public static List<String> findAnnotatedClasses(String[] strPathsOrJars,
            final Class<? extends Annotation>[] annotations)
            throws IOException  {
        return findClassesThatExtend(strPathsOrJars, annotations, false, null, null, true);
    }

    /**
     * Find classes in the provided path(s)/jar(s) that extend the class(es).
     * @param strPathsOrJars - pathnames or jarfiles to search for classes
     * @param classNames - required parent class(es) or annotations
     * @param innerClasses - should we include inner classes?
     * @param contains - classname should contain this string
     * @param notContains - classname should not contain this string
     * @param annotations - true if classnames are annotations
     *
     * @return List containing discovered classes
     */
    private static List<String> findClassesThatExtend(String[] strPathsOrJars,
                final Class<?>[] classNames, final boolean innerClasses,
                String contains, String notContains, boolean annotations)
                throws IOException  {
        if (log.isDebugEnabled()) {
            for (int i = 0; i < classNames.length ; i++){
                log.debug("superclass: "+classNames[i].getName());
            }
        }

        // Find all jars in the search path
        strPathsOrJars = addJarsInPath(strPathsOrJars);
        for (int k = 0; k < strPathsOrJars.length; k++) {
            strPathsOrJars[k] = fixPathEntry(strPathsOrJars[k]);
            if (log.isDebugEnabled()) {
                log.debug("strPathsOrJars : " + strPathsOrJars[k]);
            }
        }

        // Now eliminate any classpath entries that do not "match" the search
        List<String> listPaths = getClasspathMatches(strPathsOrJars);
        if (log.isDebugEnabled()) {
            Iterator<String> tIter = listPaths.iterator();
            while (tIter.hasNext()) {
                log.debug("listPaths : " + tIter.next());
            }
        }

        @SuppressWarnings("unchecked") // Should only be called with classes that extend annotations
        final Class<? extends Annotation>[] annoclassNames = (Class<? extends Annotation>[]) classNames;
        Set<String> listClasses =
            annotations ?
                new AnnoFilterTreeSet(annoclassNames, innerClasses)
                :
                new FilterTreeSet(classNames, innerClasses, contains, notContains);
        // first get all the classes
        findClassesInPaths(listPaths, listClasses);
        if (log.isDebugEnabled()) {
            log.debug("listClasses.size()="+listClasses.size());
            Iterator<String> tIter = listClasses.iterator();
            while (tIter.hasNext()) {
                log.debug("listClasses : " + tIter.next());
            }
        }

//        // Now keep only the required classes
//        Set subClassList = findAllSubclasses(superClasses, listClasses, innerClasses);
//        if (log.isDebugEnabled()) {
//            log.debug("subClassList.size()="+subClassList.size());
//            Iterator tIter = subClassList.iterator();
//            while (tIter.hasNext()) {
//                log.debug("subClassList : " + tIter.next());
//            }
//        }

        return new ArrayList<String>(listClasses);//subClassList);
    }

    /*
     * Returns the classpath entries that match the search list of jars and paths
     */
    private static List<String> getClasspathMatches(String[] strPathsOrJars) {
        final String javaClassPath = System.getProperty("java.class.path"); // $NON-NLS-1$
        StringTokenizer stPaths =
            new StringTokenizer(javaClassPath,
                System.getProperty("path.separator")); // $NON-NLS-1$
        if (log.isDebugEnabled()) {
            log.debug("Classpath = " + javaClassPath);
            for (int i = 0; i < strPathsOrJars.length; i++) {
                log.debug("strPathsOrJars[" + i + "] : " + strPathsOrJars[i]);
            }
        }

        // find all jar files or paths that end with strPathOrJar
        ArrayList<String> listPaths = new ArrayList<String>();
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

    /*
     * NOTUSED * Determine if the class implements the interface.
     *
     * @param theClass
     *            the class to check
     * @param theInterface
     *            the interface to look for
     * @return boolean true if it implements
     *
     * private static boolean classImplementsInterface( Class theClass, Class
     * theInterface) { HashMap mapInterfaces = new HashMap(); String strKey =
     * null; // pass in the map by reference since the method is recursive
     * getAllInterfaces(theClass, mapInterfaces); Iterator iterInterfaces =
     * mapInterfaces.keySet().iterator(); while (iterInterfaces.hasNext()) {
     * strKey = (String) iterInterfaces.next(); if (mapInterfaces.get(strKey) ==
     * theInterface) { return true; } } return false; }
     */

    /*
     * Finds all classes that extend the classes in the listSuperClasses
     * ArrayList, searching in the listAllClasses ArrayList.
     *
     * @param superClasses
     *            the base classes to find subclasses for
     * @param listAllClasses
     *            the collection of classes to search in
     * @param innerClasses
     *            indicate whether to include inner classes in the search
     * @return ArrayList of the subclasses
     */
//  private static Set findAllSubclasses(Class []superClasses, Set listAllClasses, boolean innerClasses) {
//      Set listSubClasses = new TreeSet();
//      for (int i=0; i< superClasses.length; i++) {
//          findAllSubclassesOneClass(superClasses[i], listAllClasses, listSubClasses, innerClasses);
//      }
//      return listSubClasses;
//  }

    /*
     * Finds all classes that extend the class, searching in the listAllClasses
     * ArrayList.
     *
     * @param theClass
     *            the parent class
     * @param listAllClasses
     *            the collection of classes to search in
     * @param listSubClasses
     *            the collection of discovered subclasses
     * @param innerClasses
     *            indicates whether inners classes should be included in the
     *            search
     */
//  private static void findAllSubclassesOneClass(Class theClass, Set listAllClasses, Set listSubClasses,
//          boolean innerClasses) {
//        Iterator iterClasses = listAllClasses.iterator();
//      while (iterClasses.hasNext()) {
//            String strClassName = (String) iterClasses.next();
//          // only check classes if they are not inner classes
//          // or we intend to check for inner classes
//          if ((strClassName.indexOf("$") == -1) || innerClasses) { // $NON-NLS-1$
//              // might throw an exception, assume this is ignorable
//              try {
//                  Class c = Class.forName(strClassName, false, Thread.currentThread().getContextClassLoader());
//
//                  if (!c.isInterface() && !Modifier.isAbstract(c.getModifiers())) {
//                        if(theClass.isAssignableFrom(c)){
//                            listSubClasses.add(strClassName);
//                        }
//                    }
//              } catch (Throwable ignored) {
//                    log.debug(ignored.getLocalizedMessage());
//              }
//          }
//      }
//  }

    /**
     *
     * @param parentClasses list of classes to check for
     * @param strClassName name of class to be checked
     * @param innerClasses should we allow inner classes?
     * @param contextClassLoader the classloader to use
     * @return
     */
    private static boolean isChildOf(Class<?> [] parentClasses, String strClassName,
            ClassLoader contextClassLoader){
            // might throw an exception, assume this is ignorable
            try {
                Class<?> c = Class.forName(strClassName, false, contextClassLoader);

                if (!c.isInterface() && !Modifier.isAbstract(c.getModifiers())) {
                    for (int i=0; i< parentClasses.length; i++) {
                        if(parentClasses[i].isAssignableFrom(c)){
                            return true;
                        }
                    }
                }
            } catch (UnsupportedClassVersionError ignored) {
                log.debug(ignored.getLocalizedMessage());
            } catch (NoClassDefFoundError ignored) {
                log.debug(ignored.getLocalizedMessage());
            } catch (ClassNotFoundException ignored) {
                log.debug(ignored.getLocalizedMessage());
            }
        return false;
    }

    private static boolean hasAnnotationOnMethod(Class<? extends Annotation>[] annotations, String classInQuestion,
        ClassLoader contextClassLoader ){
        try{
            Class<?> c = Class.forName(classInQuestion, false, contextClassLoader);
            for(Method method : c.getMethods()) {
                for(int i = 0;i<annotations.length;i++) {
                    Class<? extends Annotation> annotation = annotations[i];
                    if(method.isAnnotationPresent(annotation)) {
                        return true;
                    }
                }
            }
        } catch (NoClassDefFoundError ignored) {
            log.debug(ignored.getLocalizedMessage());
        } catch (ClassNotFoundException ignored) {
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

    private static void findClassesInOnePath(String strPath, Set<String> listClasses) throws IOException {
        File file = new File(strPath);
        if (file.isDirectory()) {
            findClassesInPathsDir(strPath, file, listClasses);
        } else if (file.exists()) {
            ZipFile zipFile = null;
            try {
                zipFile = new ZipFile(file);
                Enumeration<? extends ZipEntry> entries = zipFile.entries();
                while (entries.hasMoreElements()) {
                    String strEntry = entries.nextElement().toString();
                    if (strEntry.endsWith(DOT_CLASS)) {
                        listClasses.add(fixClassName(strEntry));
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

    private static void findClassesInPaths(List<String> listPaths, Set<String> listClasses) throws IOException {
        Iterator<String> iterPaths = listPaths.iterator();
        while (iterPaths.hasNext()) {
            findClassesInOnePath(iterPaths.next(), listClasses);
        }
    }

    private static void findClassesInPathsDir(String strPathElement, File dir, Set<String> listClasses) throws IOException {
        String[] list = dir.list();
        for (int i = 0; i < list.length; i++) {
            File file = new File(dir, list[i]);
            if (file.isDirectory()) {
                // Recursive call
                findClassesInPathsDir(strPathElement, file, listClasses);
            } else if (list[i].endsWith(DOT_CLASS) && file.exists() && (file.length() != 0)) {
                final String path = file.getPath();
                listClasses.add(path.substring(strPathElement.length() + 1,
                        path.lastIndexOf(".")) // $NON-NLS-1$
                        .replace(File.separator.charAt(0), '.')); // $NON-NLS-1$
            }
        }
    }
}