// $Header$
/*
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.zip.ZipFile;

import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * This class finds classes that implement one or more specified interfaces.
 *
 * @author  Burt Beckwith
 * @author  Michael Stover (mstover1 at apache.org)
 * @version $Revision$
 */
public final class ClassFinder
{
    transient private static Logger log = LoggingManager.getLoggerForClass();
    private ClassFinder()
    {
    }

    // static only
    /**
     * Convenience method for <code>findClassesThatExtend(Class[],
     * boolean)</code> with the option to include inner classes in the search
     * set to false.
     *
     * @return ArrayList containing discovered classes.
     */
    public static List findClassesThatExtend(
        String[] paths,
        Class[] superClasses)
        throws IOException, ClassNotFoundException
    {
        return findClassesThatExtend(paths, superClasses, false);
    }

    /**
     * Find classes in the provided path(s)/jar(s) that extend the class(es).
     *
     * @return ArrayList containing discovered classes
     */
    private static String[] addJarsInPath(String[] paths)
    {
        Set fullList = new HashSet();
        for (int i = 0; i < paths.length; i++)
        {
            fullList.add(paths[i]);
            if (!paths[i].endsWith(".jar"))
            {
                File dir = new File(paths[i]);
                if (dir.exists() && dir.isDirectory())
                {
                    String[] jars = dir.list(new FilenameFilter()
                    {
                        public boolean accept(File f, String name)
                        {
                            if (name.endsWith(".jar"))
                            {
                                return true;
                            }
                            return false;
                        }
                    });
                    for (int x = 0; x < jars.length; x++)
                    {
                        fullList.add(jars[x]);
                    }
                }
            }
        }
        return (String[]) fullList.toArray(new String[0]);
    }
    public static List findClassesThatExtend(
        String[] strPathsOrJars,
        Class[] superClasses,
        boolean innerClasses)
        throws IOException, ClassNotFoundException
    {
        List listPaths = null;
        Set listClasses = null;
        List listSuperClasses = null;
        strPathsOrJars = addJarsInPath(strPathsOrJars);
        if (log.isDebugEnabled())
        {
            for (int k = 0; k < strPathsOrJars.length; k++)
            {
                log.debug("strPathsOrJars : " + strPathsOrJars[k]);
            }
        }
        listPaths = getClasspathMatches(strPathsOrJars);
        if (log.isDebugEnabled())
        {
            Iterator tIter = listPaths.iterator();
            for (; tIter.hasNext();)
            {
                log.debug("listPaths : " + tIter.next());
            }
        }
        listClasses = new TreeSet();
        listSuperClasses = new ArrayList();
        for (int i = 0; i < superClasses.length; i++)
        {
            listSuperClasses.add(superClasses[i].getName());
        }
        // first get all the classes
        findClassesInPaths(listPaths, listClasses);
        if (log.isDebugEnabled())
        {
            Iterator tIter = listClasses.iterator();
            for (; tIter.hasNext();)
            {
                log.debug("listClasses : " + tIter.next());
            }
        }
        Set subClassList =
            findAllSubclasses(listSuperClasses, listClasses, innerClasses);
        return new ArrayList(subClassList);
    }

    private static List getClasspathMatches(String[] strPathsOrJars)
    {
        ArrayList listPaths = null;
        StringTokenizer stPaths = null;
        String strPath = null;
        int i;
        listPaths = new ArrayList();
        log.debug("Classpath = " + System.getProperty("java.class.path"));
        stPaths =
            new StringTokenizer(
                System.getProperty("java.class.path"),
                System.getProperty("path.separator"));
        if (strPathsOrJars != null)
        {
            strPathsOrJars = fixDotDirs(strPathsOrJars);
            strPathsOrJars = fixSlashes(strPathsOrJars);
            strPathsOrJars = fixEndingSlashes(strPathsOrJars);
        }
		if (log.isDebugEnabled())
		{
		    for (i = 0; i < strPathsOrJars.length; i++)
		    {
				log.debug("strPathsOrJars[" + i + "] : " + strPathsOrJars[i]);
			}
		}
		
        // find all jar files or paths that end with strPathOrJar
        while (stPaths.hasMoreTokens())
        {
            strPath = fixDotDir((String) stPaths.nextToken());
            strPath = fixSlashes(strPath);
            strPath = fixEndingSlashes(strPath);
            if (strPathsOrJars == null)
            {
				log.debug("Adding: " + strPath);
                listPaths.add(strPath);
            }
            else
            {
                boolean found=false;
                for (i = 0; i < strPathsOrJars.length; i++)
                {
                    if (strPath.endsWith(strPathsOrJars[i]))
                    {
                    	found=true;
                        log.debug("Adding "+strPath+" found at "+i);
                        listPaths.add(strPath);
                        break;// no need to look further
                    }
                }
                if (!found){
                	log.debug("Did not find: "+strPath);
                }
            }
        }
        return listPaths;
    }
    
    /**
     * Get all interfaces that the class implements, including parent
     * interfaces. This keeps us from having to instantiate and check
     * instanceof, which wouldn't work anyway since instanceof requires a
     * hard-coded class or interface name.
     *
     * @param  theClass     the class to get interfaces for
     * @param  hInterfaces  a Map to store the discovered interfaces in
     *
     * NOTUSED
    private static void getAllInterfaces(Class theClass, Map hInterfaces)
    {
        Class[] interfaces = theClass.getInterfaces();
        for (int i = 0; i < interfaces.length; i++)
        {
            hInterfaces.put(interfaces[i].getName(), interfaces[i]);
            getAllInterfaces(interfaces[i], hInterfaces);
        }
    }
    */
    private static String[] fixDotDirs(String[] paths)
    {
        for (int i = 0; i < paths.length; i++)
        {
            paths[i] = fixDotDir(paths[i]);
        }
        return paths;
    }
    private static String fixDotDir(String path)
    {
    	if (path == null) return null;
        if (path.equals("."))
        {
            return System.getProperty("user.dir");
        }
        else
        {
            return path.trim();
        }
    }
    private static String[] fixEndingSlashes(String[] strings)
    {
        String[] strNew = new String[strings.length];
        for (int i = 0; i < strings.length; i++)
        {
            strNew[i] = fixEndingSlashes(strings[i]);
        }
        return strNew;
    }
    private static String fixEndingSlashes(String string)
    {
        if (string.endsWith("/") || string.endsWith("\\"))
        {
            string = string.substring(0, string.length() - 1);
            string = fixEndingSlashes(string);
        }
        return string;
    }
    private static String[] fixSlashes(String[] strings)
    {
        String[] strNew = new String[strings.length];
        for (int i = 0; i < strings.length; i++)
        {
            strNew[i] = fixSlashes(strings[i]) /*.toLowerCase()*/;
        }
        return strNew;
    }
    private static String fixSlashes(String str)
    {
        // replace \ with /
        str = str.replace('\\', '/');
        // compress multiples into singles;
        // do in 2 steps with dummy string
        // to avoid infinte loop
        str = replaceString(str, "//", "_____");
        str = replaceString(str, "_____", "/");
        return str;
    }
    private static String replaceString(
        String s,
        String strToFind,
        String strToReplace)
    {
        int index;
        int currentPos;
        StringBuffer buffer = null;
        if (s.indexOf(strToFind) == -1)
        {
            return s;
        }
        currentPos = 0;
        buffer = new StringBuffer();
        while (true)
        {
            index = s.indexOf(strToFind, currentPos);
            if (index == -1)
            {
                break;
            }
            buffer.append(s.substring(currentPos, index));
            buffer.append(strToReplace);
            currentPos = index + strToFind.length();
        }
        buffer.append(s.substring(currentPos));
        return buffer.toString();
    }
    
    /**
     *   NOTUSED
     *  * Determine if the class implements the interface.
     *
     * @param  theClass      the class to check
     * @param  theInterface  the interface to look for
     * @return               boolean true if it implements
     *
    private static boolean classImplementsInterface(
        Class theClass,
        Class theInterface)
    {
        HashMap mapInterfaces = new HashMap();
        String strKey = null;
        // pass in the map by reference since the method is recursive
        getAllInterfaces(theClass, mapInterfaces);
        Iterator iterInterfaces = mapInterfaces.keySet().iterator();
        while (iterInterfaces.hasNext())
        {
            strKey = (String) iterInterfaces.next();
            if (mapInterfaces.get(strKey) == theInterface)
            {
                return true;
            }
        }
        return false;
    }
    */
    
    /**
     * Convenience method for <code>findAllSubclasses(List, List,
     * boolean)</code> with the option to include inner classes in the search
     * set to false.
     *
     * @param  listSuperClasses  the base classes to find subclasses for
     * @param  listAllClasses    the collection of classes to search in
     * @return                   ArrayList of the subclasses
     *
     * NOTUSED
    private static ArrayList findAllSubclasses(
        List listSuperClasses,
        List listAllClasses)
    {
        return findAllSubclasses(listSuperClasses, listAllClasses, false);
    }
    */
    
    /**
     * Finds all classes that extend the classes in the listSuperClasses
     * ArrayList, searching in the listAllClasses ArrayList.
     *
     * @param  listSuperClasses  the base classes to find subclasses for
     * @param  listAllClasses    the collection of classes to search in
     * @param  innerClasses      indicate whether to include inner classes in
     *                           the search
     *@return                    ArrayList of the subclasses
     */
    private static Set findAllSubclasses(
        List listSuperClasses,
        Set listAllClasses,
        boolean innerClasses)
    {
        Iterator iterClasses = null;
        Set listSubClasses = null;
        String strClassName = null;
        Class tempClass = null;
        listSubClasses = new TreeSet();
        iterClasses = listSuperClasses.iterator();
        while (iterClasses.hasNext())
        {
            strClassName = (String) iterClasses.next();
            // only check classes if they are not inner classes
            // or we intend to check for inner classes
            if ((strClassName.indexOf("$") == -1) || innerClasses)
            {
                // might throw an exception, assume this is ignorable
                try
                {
                    tempClass =
                        Class.forName(
                            strClassName,
                            false,
                            Thread.currentThread().getContextClassLoader());
                    findAllSubclassesOneClass(
                        tempClass,
                        listAllClasses,
                        listSubClasses,
                        innerClasses);
                    // call by reference - recursive
                }
                catch (Throwable ignored)
                {
                }
            }
        }
        return listSubClasses;
    }
    
    /**
     * Convenience method for <code>findAllSubclassesOneClass(Class, List, List,
     * boolean)</code> with option to include inner classes in the search set to
     * false.
     *
     * @param  theClass        the parent class
     * @param  listAllClasses  the collection of classes to search in
     * @param  listSubClasses  the collection of discovered subclasses
     *
     *   NOTUSED
    private static void findAllSubclassesOneClass(
        Class theClass,
        List listAllClasses,
        List listSubClasses)
    {
        findAllSubclassesOneClass(
            theClass,
            listAllClasses,
            listSubClasses,
            false);
    }
    */
    
    /**
     * Finds all classes that extend the class, searching in the listAllClasses
     * ArrayList.
     *
     * @param  theClass        the parent class
     * @param  listAllClasses  the collection of classes to search in
     * @param  listSubClasses  the collection of discovered subclasses
     * @param  innerClasses    indicates whether inners classes should be
     *                         included in the search
     */
    private static void findAllSubclassesOneClass(
        Class theClass,
        Set listAllClasses,
        Set listSubClasses,
        boolean innerClasses)
    {
        Iterator iterClasses = null;
        String strClassName = null;
        Class c = null;
        boolean bIsSubclass = false;
        iterClasses = listAllClasses.iterator();
        while (iterClasses.hasNext())
        {
            strClassName = (String) iterClasses.next();
            // only check classes if they are not inner classes
            // or we intend to check for inner classes
            if ((strClassName.indexOf("$") == -1) || innerClasses)
            {
                // might throw an exception, assume this is ignorable
                try
                {
                    c =
                        Class.forName(
                            strClassName,
                            false,
                            Thread.currentThread().getContextClassLoader());

                    if (!c.isInterface()
                        && !Modifier.isAbstract(c.getModifiers()))
                    {
                        bIsSubclass = theClass.isAssignableFrom(c);
                    }
                    else
                    {
                        bIsSubclass = false;
                    }
                    if (bIsSubclass)
                    {
                        listSubClasses.add(strClassName);
                    }
                }
                catch (Throwable ignored)
                {
                }
            }
        }
    }
    
    /**
     * Converts a class file from the text stored in a Jar file to a version
     * that can be used in Class.forName().
     *
     * @param  strClassName  the class name from a Jar file
     * @return               String the Java-style dotted version of the name
     */
    private static String fixClassName(String strClassName)
    {
        strClassName = strClassName.replace('\\', '.');
        strClassName = strClassName.replace('/', '.');
        strClassName = strClassName.substring(0, strClassName.length() - 6);
        // remove ".class"
        return strClassName;
    }
    
    private static void findClassesInOnePath(String strPath, Set listClasses)
        throws IOException
    {
        File file = null;
        ZipFile zipFile = null;
        Enumeration entries = null;
        String strEntry = null;
        file = new File(strPath);
        if (file.isDirectory())
        {
            findClassesInPathsDir(strPath, file, listClasses);
        }
        else if (file.exists())
        {
            zipFile = new ZipFile(file);
            entries = zipFile.entries();
            while (entries.hasMoreElements())
            {
                strEntry = entries.nextElement().toString();
                if (strEntry.endsWith(".class"))
                {
                    listClasses.add(fixClassName(strEntry));
                }
            }
        }
    }

    private static void findClassesInPaths(List listPaths, Set listClasses)
        throws IOException
    {
        Iterator iterPaths = listPaths.iterator();
        while (iterPaths.hasNext())
        {
            findClassesInOnePath((String) iterPaths.next(), listClasses);
        }
    }

    private static void findClassesInPathsDir(
        String strPathElement,
        File dir,
        Set listClasses)
        throws IOException
    {
        File file = null;
        String[] list = dir.list();
        for (int i = 0; i < list.length; i++)
        {
            file = new File(dir, list[i]);
            if (file.isDirectory())
            {
                findClassesInPathsDir(strPathElement, file, listClasses);
            }
            else if (
                file.exists()
                    && (file.length() != 0)
                    && list[i].endsWith(".class"))
            {
                listClasses.add(
                    file
                        .getPath()
                        .substring(
                            strPathElement.length() + 1,
                            file.getPath().lastIndexOf("."))
                        .replace(File.separator.charAt(0), '.'));
            }
        }
    }
}