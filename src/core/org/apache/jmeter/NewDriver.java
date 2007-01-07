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

package org.apache.jmeter;

// N.B. this must only use standard Java packages
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Main class for JMeter - sets up initial classpath.
 * 
 * @author Michael Stover
 */
public final class NewDriver {
	/** The class loader to use for loading JMeter classes. */
	private static DynamicClassLoader loader;

	/** The directory JMeter is installed in. */
	private static String jmDir;

	static {
		List jars = new LinkedList();
		String cp = System.getProperty("java.class.path");

		// Find JMeter home dir
		StringTokenizer tok = new StringTokenizer(cp, File.pathSeparator);
		if (tok.countTokens() == 1) {
			File jar = new File(tok.nextToken());
			try {
				jmDir = jar.getCanonicalFile().getParentFile().getParent();
			} catch (IOException e) {
			}
		} else {
			File userDir = new File(System.getProperty("user.dir"));
			jmDir = userDir.getAbsoluteFile().getParent();
		}

		/*
		 * Does the system support UNC paths? If so, may need to fix them up
		 * later
		 */
		boolean usesUNC = System.getProperty("os.name").startsWith("Windows");

		StringBuffer classpath = new StringBuffer();
		File[] libDirs = new File[] { new File(jmDir + File.separator + "lib"),
				new File(jmDir + File.separator + "lib" + File.separator + "ext"),
                new File(jmDir + File.separator + "lib" + File.separator + "junit")};
		for (int a = 0; a < libDirs.length; a++) {
			File[] libJars = libDirs[a].listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return name.endsWith(".jar");
				}
			});
			if (libJars == null) {
				new Throwable("Could not access " + libDirs[a]).printStackTrace();
				continue;
			}
			for (int i = 0; i < libJars.length; i++) {
				try {
					String s = libJars[i].getPath();

					// Fix path to allow the use of UNC URLs
					if (usesUNC) {
						if (s.startsWith("\\\\") && !s.startsWith("\\\\\\")) {
							s = "\\\\" + s;
						} else if (s.startsWith("//") && !s.startsWith("///")) {
							s = "//" + s;
						}
					} // usesUNC

					jars.add(new URL("file", "", s));
					classpath.append(System.getProperty("path.separator"));
					classpath.append(s);
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
			}
		}

		// ClassFinder needs this
		System.setProperty("java.class.path", System.getProperty("java.class.path") + classpath.toString());
		loader = new DynamicClassLoader((URL[]) jars.toArray(new URL[0]));
	}

	/**
	 * Prevent instantiation.
	 */
	private NewDriver() {
	}

    public static void addURL(String url) {
        File furl = new File(url);
        try {
            loader.addURL(furl.toURL());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }
    
    public static void addURL(URL url) {
        loader.addURL(url);
    }
    
    public static void addPath(String path) throws MalformedURLException {
		URL url = new URL("file","",path);
        loader.addURL(url);
    	StringBuffer sb = new StringBuffer(System.getProperty("java.class.path"));
    	sb.append(System.getProperty("path.separator"));
    	sb.append(path);
		// ClassFinder needs this
		System.setProperty("java.class.path",sb.toString());
    }
    
	/**
	 * Get the directory where JMeter is installed. This is the absolute path
	 * name.
	 * 
	 * @return the directory where JMeter is installed.
	 */
	public static String getJMeterDir() {
		return jmDir;
	}

	/**
	 * The main program which actually runs JMeter.
	 * 
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String[] args) {
		Thread.currentThread().setContextClassLoader(loader);
		if (System.getProperty("log4j.configuration") == null) {
			File conf = new File(jmDir, "bin" + File.separator + "log4j.conf");
			System.setProperty("log4j.configuration", "file:" + conf);
		}

        if (args != null && args.length > 0 && args[0].equals("report")) {
            try {
                Class JMeterReport = loader.loadClass("org.apache.jmeter.JMeterReport");
                Object instance = JMeterReport.newInstance();
                Method startup = JMeterReport.getMethod("start", new Class[] { (new String[0]).getClass() });
                startup.invoke(instance, new Object[] { args });

            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("JMeter home directory was detected as: "+jmDir);
            }
        } else {
            try {
                Class JMeter = loader.loadClass("org.apache.jmeter.JMeter");
                Object instance = JMeter.newInstance();
                Method startup = JMeter.getMethod("start", new Class[] { (new String[0]).getClass() });
                startup.invoke(instance, new Object[] { args });

            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("JMeter home directory was detected as: "+jmDir);
           }
        }
	}
}
