/*
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 * if any, must include the following acknowledgment:
 * "This product includes software developed by the
 * Apache Software Foundation (http://www.apache.org/)."
 * Alternately, this acknowledgment may appear in the software itself,
 * if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 * "Apache JMeter" must not be used to endorse or promote products
 * derived from this software without prior written permission. For
 * written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 * "Apache JMeter", nor may "Apache" appear in their name, without
 * prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
package org.apache.jmeter.util;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import junit.framework.TestCase;

import org.apache.log4j.Category;
/************************************************************

 *  This class finds classes that implement one or more specified interfaces.

 *  This allows us to implement a plugin-type search in jar files or

 *  directories.

 *

 *@author     Burt Beckwith

 *@created    November 30, 2000

 *@version    1.0

 ***********************************************************/
public class ClassFinder {
	private static Category catClass =
		Category.getInstance(ClassFinder.class.getName());
	private ClassFinder() {}
	// static only
	
	/************************************************************	
	 *  Convenience method for <code>findClassesThatExtend(Class[], boolean)</code>	
	 *  with the option to include inner classes in the search set to false	
	 *	
	 *@param  superClasses                Description of Parameter	
	 *@return                             ArrayList containing discovered classes.	
	 *@exception  IOException	
	 *@exception  ClassNotFoundException	
	 ***********************************************************/
	public static List findClassesThatExtend(Class[] superClasses)
		throws IOException, ClassNotFoundException {
		return findClassesThatExtend(superClasses, false);
	}
	
	/************************************************************	
	 *  Find classes in the provided path(s)/jar(s) that extend the class(es).	
	 *	
	 *@param  superClasses                Description of Parameter	
	 *@param  innerClasses                indicate whether to include inner classes	
	 *      in the search	
	 *@return                             ArrayList containing discovered classes.	
	 *@exception  IOException
	 *@exception  ClassNotFoundException	
	 ***********************************************************/
	private static String[] addJarsInPath(String[] paths) {
		Set fullList = new HashSet();
		for (int i = 0; i < paths.length; i++) {
			fullList.add(paths[i]);
			if (!paths[i].endsWith(".jar")) {
				File dir = new File(paths[i]);
				if (dir.exists() && dir.isDirectory()) {
					String[] jars = dir.list(new FilenameFilter() {
						public boolean accept(File f, String name) {
							if (name.endsWith(".jar")) {
								return true;
							}
							return false;
						}
					});
					for (int x = 0; x < jars.length; x++) {
						fullList.add(jars[x]);
					}
				}
			}
		}
		return (String[]) fullList.toArray(new String[0]);
	}
	
	public static List findClassesThatExtend(
		Class[] superClasses,
		boolean innerClasses)
		throws IOException, ClassNotFoundException 
	{
		List listPaths = null;
		ArrayList listClasses = null;
		List listSuperClasses = null;
		String[] strPathsOrJars =
			JMeterUtils.split(
				JMeterUtils.getPropDefault("search_paths", "lib/ext"),
				";");
		// paths are relative to jmDir - convert to absolutec
		for (int i = 0; i < strPathsOrJars.length; i++) {
			File file = new File(strPathsOrJars[i]);
			if (!file.isAbsolute()) {
				file = new File(JMeterUtils.getJMeterHome(),
					strPathsOrJars[i]);
				strPathsOrJars[i] = file.getPath();
			}
		}
		strPathsOrJars = addJarsInPath(strPathsOrJars);
		if (catClass.isDebugEnabled()) {
			for (int k = 0; k < strPathsOrJars.length; k++) {
				catClass.debug("strPathsOrJars : " + strPathsOrJars[k]);
			}
		}
		listPaths = getClasspathMatches(strPathsOrJars);
		if (catClass.isDebugEnabled()) {
			Iterator tIter = listPaths.iterator();
			for (; tIter.hasNext();) {
				catClass.debug("listPaths : " + tIter.next());
			}
		}
		listClasses = new ArrayList();
		listSuperClasses = new ArrayList();
		for (int i = 0; i < superClasses.length; i++) {
			listSuperClasses.add(superClasses[i].getName());
		}
		// first get all the classes
		findClassesInPaths(listPaths, listClasses);
		if (catClass.isDebugEnabled()) {
			Iterator tIter = listClasses.iterator();
			for (; tIter.hasNext();) {
				catClass.debug("listClasses : " + tIter.next());
			}
		}
		List subClassList =
			findAllSubclasses(listSuperClasses, listClasses, innerClasses);
		return subClassList;
	}
	/************************************************************
	
	 *  Convenience method to <code>findClassesByInterface(String[], Class[],
	
	 *  boolean)</code> with option to include inner classes in the search set to
	
	 *  false
	
	 *
	
	 *@param  interfaces                  Array of interfaces to search for.
	
	 *@param  strPathsOrJars              paths and jars to search
	
	 *@return                             ArrayList containing discovered classes.
	
	 *@exception  IOException
	
	 *@exception  ClassNotFoundException  !ToDo (Exception description)
	
	 ***********************************************************/
	public static ArrayList findClassesByInterface(
		String[] strPathsOrJars,
		Class[] interfaces)
		throws IOException, ClassNotFoundException {
		return findClassesByInterface(strPathsOrJars, interfaces, false);
	}
	/************************************************************
	
	 *  Find classes in the provided path(s)/jar(s) that implement the
	
	 *  interface(s).
	
	 *
	
	 *@param  interfaces                  Array of interfaces to search for.
	
	 *@param  strPathsOrJars              paths and jars to search
	
	 *@param  innerClasses                Indicate whether the search includes
	
	 *      inner classes
	
	 *@return                             ArrayList containing discovered classes.
	
	 *@exception  IOException
	
	 *@exception  ClassNotFoundException
	
	 ***********************************************************/
	public static ArrayList findClassesByInterface(
		String[] strPathsOrJars,
		Class[] interfaces,
		boolean innerClasses)
		throws IOException, ClassNotFoundException {
		List listPaths = null;
		ArrayList listClasses = null;
		ArrayList listClassesWithInterface = null;
		String strClassName = null;
		Iterator iterClasses = null;
		Class tempClass = null;
		boolean foundIt;
		int i;
		listPaths = getClasspathMatches(strPathsOrJars);
		listClasses = new ArrayList();
		// first get all the classes
		findClassesInPaths(listPaths, listClasses);
		// then filter by the specified interfaces;
		// if the array is null or empty, keep all classes
		if (interfaces == null || interfaces.length == 0) {
			listClassesWithInterface = listClasses;
		} else {
			// make a copy of the ArrayList, and remove all classes that
			// don't implement one of the interefaces
			listClassesWithInterface = (ArrayList) listClasses.clone();
			iterClasses = listClasses.iterator();
			while (iterClasses.hasNext()) {
				strClassName = (String) iterClasses.next();
				foundIt = false;
				// only check classes if they are not inner classes
				// or we intend to check for inner classes
				if ((strClassName.indexOf("$") == -1) || innerClasses) {
					// might throw an exception, assume this is ignorable
					try {
						tempClass = Class.forName(strClassName,true,Thread.currentThread().getContextClassLoader());
						for (i = 0; i < interfaces.length; i++) {
							if (classImplementsInterface(tempClass, interfaces[i])) {
								foundIt = true;
								break;
							}
						}
					} catch (Throwable ignored) {}
				}
				if (!foundIt) {
					listClassesWithInterface.remove(strClassName);
				}
			}
		}
		// listClassesWithInterface only contains classes that directly
		// implement the interface(s);  to get all classes we need
		// subclasses of these, too
		return findAllSubclasses(listClassesWithInterface, listClasses, innerClasses);
	}
	private static List getClasspathMatches(String[] strPathsOrJars) {
		ArrayList listPaths = null;
		StringTokenizer stPaths = null;
		String strPath = null;
		int i;
		listPaths = new ArrayList();
		stPaths =
			new StringTokenizer(
				System.getProperty("java.class.path"),
				System.getProperty("path.separator"));
		if (strPathsOrJars != null) {
			strPathsOrJars = fixDotDirs(strPathsOrJars);
			strPathsOrJars = fixSlashes(strPathsOrJars);
			strPathsOrJars = fixEndingSlashes(strPathsOrJars);
		}
		// find all jar files or paths that end with strPathOrJar
		while (stPaths.hasMoreTokens()) {
			strPath = fixDotDir((String) stPaths.nextToken());
			strPath = fixSlashes(strPath);
			strPath = fixEndingSlashes(strPath);
			if (strPathsOrJars == null) {
				listPaths.add(strPath);
			} else {
				for (i = 0; i < strPathsOrJars.length; i++) {
					if (catClass.isDebugEnabled()) {
						catClass.debug("strPath(lower) : " + strPath.toLowerCase());
						catClass.debug("strPathsOrJars[" + i + "] : " + strPathsOrJars[i]);
					}
					if (strPath.endsWith(strPathsOrJars[i])) {
						catClass.debug("match!!!");
						listPaths.add(strPath);
					}
				}
			}
		}
		return listPaths;
	}
	
	/************************************************************	
	 *  Get all interfaces that the class implements, including parent interfaces.
	 *  This keeps us from having to instantiate and check instanceof, which	
	 *  wouldn't work anyway since instanceof requires a hard-coded class or	
	 *  interface name.	
	 *	
	 *@param  theClass     the class to get interfaces for	
	 *@param  hInterfaces  a Map to store the discovered interfaces in	
	 ***********************************************************/
	private static void getAllInterfaces(Class theClass, Map hInterfaces) {
		Class[] interfaces = theClass.getInterfaces();
		for (int i = 0; i < interfaces.length; i++) {
			hInterfaces.put(interfaces[i].getName(), interfaces[i]);
			getAllInterfaces(interfaces[i], hInterfaces);
		}
	}
	private static String[] fixDotDirs(String[] paths) {
		for (int i = 0; i < paths.length; i++) {
			paths[i] = fixDotDir(paths[i]);
		}
		return paths;
	}
	private static String fixDotDir(String path) {
		if (path != null && path.equals(".")) {
			return System.getProperty("user.dir");
		} else {
			return path.trim();
		}
	}
	private static String[] fixEndingSlashes(String[] strings) {
		String[] strNew = new String[strings.length];
		for (int i = 0; i < strings.length; i++) {
			strNew[i] = fixEndingSlashes(strings[i]);
		}
		return strNew;
	}
	private static String fixEndingSlashes(String string) {
		if (string.endsWith("/") || string.endsWith("\\")) {
			string = string.substring(0, string.length() - 1);
			string = fixEndingSlashes(string);
		}
		return string;
	}
	private static String[] fixSlashes(String[] strings) {
		String[] strNew = new String[strings.length];
		for (int i = 0; i < strings.length; i++) {
			strNew[i] = fixSlashes(strings[i]) /*.toLowerCase()*/;
		}
		return strNew;
	}
	private static String fixSlashes(String str) {
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
		String strToReplace) {
		int index;
		int currentPos;
		StringBuffer buffer = null;
		if (s.indexOf(strToFind) == -1) {
			return s;
		}
		currentPos = 0;
		buffer = new StringBuffer();
		while (true) {
			index = s.indexOf(strToFind, currentPos);
			if (index == -1) {
				break;
			}
			buffer.append(s.substring(currentPos, index));
			buffer.append(strToReplace);
			currentPos = index + strToFind.length();
		}
		buffer.append(s.substring(currentPos));
		return buffer.toString();
	}
	/************************************************************
	
	 *  Determine if the class implements the interface.
	
	 *
	
	 *@param  theClass      the class to check
	
	 *@param  theInterface  the interface to look for
	
	 *@return               boolean true if it implements
	
	 ***********************************************************/
	private static boolean classImplementsInterface(
		Class theClass,
		Class theInterface) {
		HashMap mapInterfaces = new HashMap();
		String strKey = null;
		// pass in the map by reference since the method is recursive
		getAllInterfaces(theClass, mapInterfaces);
		Iterator iterInterfaces = mapInterfaces.keySet().iterator();
		while (iterInterfaces.hasNext()) {
			strKey = (String) iterInterfaces.next();
			if (mapInterfaces.get(strKey) == theInterface) {
				return true;
			}
		}
		return false;
	}
	/************************************************************
	
	 *  Convenience method for <code>findAllSubclasses(List, List, boolean)</code>
	
	 *  with the option to include inner classes in the search set to false
	
	 *
	
	 *@param  listSuperClasses  the base classes to find subclasses for
	
	 *@param  listAllClasses    the collection of classes to search in
	
	 *@return                   ArrayList of the subclasses
	
	 ***********************************************************/
	private static ArrayList findAllSubclasses(
		List listSuperClasses,
		List listAllClasses) {
		return findAllSubclasses(listSuperClasses, listAllClasses, false);
	}
	/************************************************************
	
	 *  Finds all classes that extend the classes in the listSuperClasses
	
	 *  ArrayList, searching in the listAllClasses ArrayList.
	
	 *
	
	 *@param  listSuperClasses  the base classes to find subclasses for
	
	 *@param  listAllClasses    the collection of classes to search in
	
	 *@param  innerClasses      indicate whether to include inner classes in the
	
	 *      search
	
	 *@return                   ArrayList of the subclasses
	
	 ***********************************************************/
	private static ArrayList findAllSubclasses(
		List listSuperClasses,
		List listAllClasses,
		boolean innerClasses) {
		Iterator iterClasses = null;
		ArrayList listSubClasses = null;
		String strClassName = null;
		Class tempClass = null;
		listSubClasses = new ArrayList();
		iterClasses = listSuperClasses.iterator();
		while (iterClasses.hasNext()) {
			strClassName = (String) iterClasses.next();
			// only check classes if they are not inner classes
			// or we intend to check for inner classes
			if ((strClassName.indexOf("$") == -1) || innerClasses) {
				// might throw an exception, assume this is ignorable
				try {
					tempClass = Class.forName(strClassName,true,Thread.currentThread().getContextClassLoader());
					findAllSubclassesOneClass(
						tempClass,
						listAllClasses,
						listSubClasses,
						innerClasses);
					// call by reference - recursive
				} catch (Throwable ignored) {}
			}
		}
		return listSubClasses;
	}
	/************************************************************
	
	 *  Convenience method for <code>findAllSubclassesOneClass(Class, List, List,
	
	 *  boolean)</code> with option to include inner classes in the search set to
	
	 *  false
	
	 *
	
	 *@param  theClass        the parent class
	
	 *@param  listAllClasses  the collection of classes to search in
	
	 *@param  listSubClasses  the collection of discovered subclasses
	
	 ***********************************************************/
	private static void findAllSubclassesOneClass(
		Class theClass,
		List listAllClasses,
		List listSubClasses) {
		findAllSubclassesOneClass(theClass, listAllClasses, listSubClasses, false);
	}
	/************************************************************
	
	 *  Finds all classes that extend the class, searching in the listAllClasses
	
	 *  ArrayList.
	
	 *
	
	 *@param  theClass        the parent class
	
	 *@param  listAllClasses  the collection of classes to search in
	
	 *@param  listSubClasses  the collection of discovered subclasses
	
	 *@param  innerClasses    indicates whether inners classes should be included
	
	 *      in the search
	
	 ***********************************************************/
	private static void findAllSubclassesOneClass(
		Class theClass,
		List listAllClasses,
		List listSubClasses,
		boolean innerClasses) {
		Iterator iterClasses = null;
		String strClassName = null;
		String strSuperClassName = null;
		Class c = null;
		Class cParent = null;
		boolean bIsSubclass = false;
		strSuperClassName = theClass.getName();
		iterClasses = listAllClasses.iterator();
		while (iterClasses.hasNext()) {
			strClassName = (String) iterClasses.next();
			// only check classes if they are not inner classes
			// or we intend to check for inner classes
			if ((strClassName.indexOf("$") == -1) || innerClasses) {
				// might throw an exception, assume this is ignorable
				try {
					// Class.forName() doesn't like nulls
					if (strClassName == null)
						continue;
					c = Class.forName(strClassName,true,Thread.currentThread().getContextClassLoader());
					if (!c.isInterface() && !Modifier.isAbstract(c.getModifiers())) {
						bIsSubclass = theClass.isAssignableFrom(c);
					} else {
						bIsSubclass = false;
					}
					if (bIsSubclass) {
						listSubClasses.add(strClassName);
					}
				} catch (Throwable ignored) {}
			}
		}
	}
	/************************************************************
	
	 *  Converts a class file from the text stored in a Jar file to a version that
	
	 *  can be used in Class.forName().
	
	 *
	
	 *@param  strClassName  the class name from a Jar file
	
	 *@return               String the Java-style dotted version of the name
	
	 ***********************************************************/
	private static String fixClassName(String strClassName) {
		strClassName = strClassName.replace('\\', '.');
		strClassName = strClassName.replace('/', '.');
		strClassName = strClassName.substring(0, strClassName.length() - 6);
		// remove ".class"
		return strClassName;
	}
	/************************************************************
	
	 *  Description of the Method
	
	 *
	
	 *@param  strPath          Description of Parameter
	
	 *@param  listClasses      Description of Parameter
	
	 *@exception  IOException  Description of Exception
	
	 ***********************************************************/
	private static void findClassesInOnePath(String strPath, List listClasses)
		throws IOException {
		File file = null;
		String strPathName = null;
		ZipFile zipFile = null;
		ZipEntry zipEntry = null;
		Enumeration entries = null;
		String strEntry = null;
		file = new File(strPath);
		if (file.isDirectory()) {
			findClassesInPathsDir(strPath, file, listClasses);
		} else
			if (file.exists()) {
				zipFile = new ZipFile(file);
				entries = zipFile.entries();
				while (entries.hasMoreElements()) {
					strEntry = entries.nextElement().toString();
					if (strEntry.endsWith(".class")) {
						listClasses.add(fixClassName(strEntry));
					}
				}
			}
	}
	/************************************************************
	
	 *  Description of the Method
	
	 *
	
	 *@param  listPaths        Description of Parameter
	
	 *@param  listClasses      Description of Parameter
	
	 *@exception  IOException  Description of Exception
	
	 ***********************************************************/
	private static void findClassesInPaths(List listPaths, List listClasses)
		throws IOException {
		Iterator iterPaths = listPaths.iterator();
		while (iterPaths.hasNext()) {
			findClassesInOnePath((String) iterPaths.next(), listClasses);
		}
	}
	/************************************************************
	
	 *  Description of the Method
	
	 *
	
	 *@param  strPathElement   Description of Parameter
	
	 *@param  dir              Description of Parameter
	
	 *@param  listClasses      Description of Parameter
	
	 *@exception  IOException  Description of Exception
	
	 ***********************************************************/
	private static void findClassesInPathsDir(
		String strPathElement,
		File dir,
		List listClasses)
		throws IOException {
		File file = null;
		String[] list = dir.list();
		for (int i = 0; i < list.length; i++) {
			file = new File(dir, list[i]);
			if (file.isDirectory()) {
				findClassesInPathsDir(strPathElement, file, listClasses);
			} else
				if (file.exists() && (file.length() != 0) && list[i].endsWith(".class")) {
					listClasses.add(
						file
							.getPath()
							.substring(strPathElement.length() + 1, file.getPath().lastIndexOf("."))
							.replace(File.separator.charAt(0), '.'));
				}
		}
	}
	/************************************************************
	
	 *  !ToDo (Class description)
	
	 *
	
	 *@author     $Author$
	
	 *@created    $Date$
	
	 *@version    $Revision$
	
	 ***********************************************************/
	public static class Test extends TestCase {
		/************************************************************
		
		 *  !ToDo (Constructor description)
		
		 *
		
		 *@param  name  !ToDo (Parameter description)
		
		 ***********************************************************/
		public Test(String name) {
			super(name);
		}
		// For a given testPath, get the name of the classes in the path and its
		// subpaths. Append the first class name found to the testPath to ensure
		// that the file is found.
		/************************************************************
		
		 *  !ToDo
		
		 ***********************************************************/
		public void testFindClassesInPaths() {
			String testPath = JMeterUtils.getJMeterHome() + "/build";
			List listPaths = new ArrayList();
			ArrayList listClasses = new ArrayList();
			// listPaths will contain the path/jars where all classes will be listed
			listPaths.add(testPath);
			try {
				findClassesInPaths(listPaths, listClasses);
			} catch (IOException e) {
				fail("IOException thrown");
			}
			Iterator iter = listClasses.iterator();
			if (iter.hasNext()) {
				String fullname = null;
				// check if file exist
				String fileSeparator = System.getProperty("file.separator");
				String filename = (String) iter.next();
				filename = filename.replace('.', fileSeparator.charAt(0));
				if (testPath.endsWith(fileSeparator)) {
					fullname = testPath + filename + ".class";
				} else {
					fullname = testPath + fileSeparator + filename + ".class";
				}
				File file = new File(fullname);
				this.assertTrue(fullname + " is not a file", file.isFile());
			} else {
				// if testPath is empty, fail the test
				fail(testPath + " is empty. Please choose a path which has at least one file");
			}
		}
	}
}