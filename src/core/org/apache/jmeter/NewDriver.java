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
import java.security.AccessController;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Main class for JMeter - sets up initial classpath and the loader.
 *
 */
public final class NewDriver {

    private static final String CLASSPATH_SEPARATOR = File.pathSeparator;

    private static final String OS_NAME = System.getProperty("os.name");// $NON-NLS-1$

    private static final String OS_NAME_LC = OS_NAME.toLowerCase(java.util.Locale.ENGLISH);

    private static final String JAVA_CLASS_PATH = "java.class.path";// $NON-NLS-1$

    /** The class loader to use for loading JMeter classes. */
    private static final DynamicClassLoader loader;

    /** The directory JMeter is installed in. */
    private static final String jmDir;

    static {
        final List<URL> jars = new LinkedList<URL>();
        final String initial_classpath = System.getProperty(JAVA_CLASS_PATH);

        // Find JMeter home dir from the initial classpath
        String tmpDir=null;
        StringTokenizer tok = new StringTokenizer(initial_classpath, File.pathSeparator);
        if (tok.countTokens() == 1
                || (tok.countTokens()  == 2 // Java on Mac OS can add a second entry to the initial classpath
                    && OS_NAME_LC.startsWith("mac os x")// $NON-NLS-1$
                   )
           ) {
            File jar = new File(tok.nextToken());
            try {
                tmpDir = jar.getCanonicalFile().getParentFile().getParent();
            } catch (IOException e) {
            }
        } else {// e.g. started from IDE with full classpath
            tmpDir = System.getProperty("jmeter.home","");// Allow override $NON-NLS-1$ $NON-NLS-2$
            if (tmpDir.length() == 0) {
                File userDir = new File(System.getProperty("user.dir"));// $NON-NLS-1$
                tmpDir = userDir.getAbsoluteFile().getParent();
            }
        }
        jmDir=tmpDir;

        /*
         * Does the system support UNC paths? If so, may need to fix them up
         * later
         */
        boolean usesUNC = OS_NAME_LC.startsWith("windows");// $NON-NLS-1$

        // Add standard jar locations to initial classpath
        StringBuilder classpath = new StringBuilder();
        File[] libDirs = new File[] { new File(jmDir + File.separator + "lib"),// $NON-NLS-1$ $NON-NLS-2$
                new File(jmDir + File.separator + "lib" + File.separator + "ext"),// $NON-NLS-1$ $NON-NLS-2$
                new File(jmDir + File.separator + "lib" + File.separator + "junit")};// $NON-NLS-1$ $NON-NLS-2$
        for (File libDir : libDirs) {
            File[] libJars = libDir.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {// only accept jar files
                    return name.endsWith(".jar");// $NON-NLS-1$
                }
            });
            if (libJars == null) {
                new Throwable("Could not access " + libDir).printStackTrace();
                continue;
            }
            Arrays.sort(libJars); // Bug 50708 Ensure predictable order of jars
            for (File libJar : libJars) {
                try {
                    String s = libJar.getPath();

                    // Fix path to allow the use of UNC URLs
                    if (usesUNC) {
                        if (s.startsWith("\\\\") && !s.startsWith("\\\\\\")) {// $NON-NLS-1$ $NON-NLS-2$
                            s = "\\\\" + s;// $NON-NLS-1$
                        } else if (s.startsWith("//") && !s.startsWith("///")) {// $NON-NLS-1$ $NON-NLS-2$
                            s = "//" + s;// $NON-NLS-1$
                        }
                    } // usesUNC

                    jars.add(new File(s).toURI().toURL());// See Java bug 4496398
                    classpath.append(CLASSPATH_SEPARATOR);
                    classpath.append(s);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
        }

        // ClassFinder needs the classpath
        System.setProperty(JAVA_CLASS_PATH, initial_classpath + classpath.toString());
        loader = AccessController.doPrivileged(
                new java.security.PrivilegedAction<DynamicClassLoader>() {
                    @Override
                    public DynamicClassLoader run() {
                        return new DynamicClassLoader(jars.toArray(new URL[jars.size()]));
                    }
                }
        );
    }

    /**
     * Prevent instantiation.
     */
    private NewDriver() {
    }

    /**
     * Generate an array of jar files located in a directory.
     * Jar files located in sub directories will not be added.
     *
     * @param dir to search for the jar files.
     */
    private static File[] listJars(File dir) {
        if (dir.isDirectory()) {
            return dir.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File f, String name) {
                    if (name.endsWith(".jar")) {// $NON-NLS-1$
                        File jar = new File(f, name);
                        return jar.isFile() && jar.canRead();
                    }
                    return false;
                }
            });
        }
        return new File[0];
    }

    /**
     * Add a URL to the loader classpath only; does not update the system classpath.
     *
     * @param path to be added.
     */
    public static void addURL(String path) {
        File furl = new File(path);
        try {
            loader.addURL(furl.toURI().toURL()); // See Java bug 4496398
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        File[] jars = listJars(furl);
        for (File jar : jars) {
            try {
                loader.addURL(jar.toURI().toURL()); // See Java bug 4496398
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Add a URL to the loader classpath only; does not update the system
     * classpath.
     *
     * @param url
     *            The {@link URL} to add to the classpath
     */
    public static void addURL(URL url) {
        loader.addURL(url);
    }

    /**
     * Add a directory or jar to the loader and system classpaths.
     *
     * @param path
     *            to add to the loader and system classpath
     * @throws MalformedURLException
     *             if <code>path</code> can not be transformed to a valid
     *             {@link URL}
     */
    public static void addPath(String path) throws MalformedURLException {
        File file = new File(path);
        // Ensure that directory URLs end in "/"
        if (file.isDirectory() && !path.endsWith("/")) {// $NON-NLS-1$
            file = new File(path + "/");// $NON-NLS-1$
        }
        loader.addURL(file.toURI().toURL()); // See Java bug 4496398
        StringBuilder sb = new StringBuilder(System.getProperty(JAVA_CLASS_PATH));
        sb.append(CLASSPATH_SEPARATOR);
        sb.append(path);
        File[] jars = listJars(file);
        for (File jar : jars) {
            try {
                loader.addURL(jar.toURI().toURL()); // See Java bug 4496398
                sb.append(CLASSPATH_SEPARATOR);
                sb.append(jar.getPath());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }

        // ClassFinder needs this
        System.setProperty(JAVA_CLASS_PATH,sb.toString());
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
        if (System.getProperty("log4j.configuration") == null) {// $NON-NLS-1$ $NON-NLS-2$
            File conf = new File(jmDir, "bin" + File.separator + "log4j.conf");// $NON-NLS-1$ $NON-NLS-2$
            System.setProperty("log4j.configuration", "file:" + conf);
        }

        try {
            Class<?> initialClass;
            if (args != null && args.length > 0 && args[0].equals("report")) {// $NON-NLS-1$
                initialClass = loader.loadClass("org.apache.jmeter.JMeterReport");// $NON-NLS-1$
            } else {
                initialClass = loader.loadClass("org.apache.jmeter.JMeter");// $NON-NLS-1$
            }
            Object instance = initialClass.newInstance();
            Method startup = initialClass.getMethod("start", new Class[] { new String[0].getClass() });// $NON-NLS-1$
            startup.invoke(instance, new Object[] { args });
        } catch(Throwable e){
            e.printStackTrace();
            System.err.println("JMeter home directory was detected as: "+jmDir);
        }
    }
}
