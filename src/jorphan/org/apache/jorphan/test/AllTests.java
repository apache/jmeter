/*
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001,2003 The Apache Software Foundation.  All rights
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
package org.apache.jorphan.test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Properties;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.reflect.ClassFinder;
import org.apache.jorphan.util.JOrphanUtils;
import org.apache.log.Logger;

/**
 * Provides a quick and easy way to run all
 * <a href="http://junit.sourceforge.net">junit</a> unit tests in your java
 * project.  It will find all unit test classes and run all their test methods.
 * There is no need to configure it in any way to find these classes except to
 * give it a path to search.
 * <p>
 * Here is an example Ant target (See Ant at
 * <a href="http://jakarta.apache.org/ant">Apache</a>) that runs all your unit
 * tests:
 * <pre>
 *      &lt;target name="test" depends="compile"&gt;
 *          &lt;java classname="org.apache.jorphan.test.AllTests" fork="yes"&gt;
 *              &lt;classpath&gt;
 *                  &lt;path refid="YOUR_CLASSPATH"/&gt;
 *                  &lt;pathelement location="ROOT_DIR_OF_YOUR_COMPILED_CLASSES"/&gt;
 *              &lt;/classpath&gt;
 *              &lt;arg value="SEARCH_PATH/"/&gt;
 *              &lt;arg value="PROPERTY_FILE"/&gt;
 *              &lt;arg value="NAME_OF_UNITTESTMANAGER_CLASS"/&gt;
 *          &lt;/java&gt;
 *      &lt;/target&gt;
 * </pre>
 * 
 * <dl>
 *  <dt>YOUR_CLASSPATH</dt>
 *  <dd>Refers to the classpath that includes all jars and libraries need to
 *      run your unit tests</dd>
 * 
 *  <dt>ROOT_DIR_OF_YOUR_COMPILED_CLASSES</dt>
 *  <dd>The classpath should include the directory where all your project's
 *      classes are compiled to, if it doesn't already.</dd>
 * 
 *  <dt>SEARCH_PATH</dt>
 *  <dd>The first argument tells AllTests where to look for unit test classes
 *      to execute.  In most cases, it is identical to
 *      ROOT_DIR_OF_YOUR_COMPILED_CLASSES.  You can specify multiple
 *      directories or jars to search by providing a comma-delimited list.</dd>
 * 
 *  <dt>PROPERTY_FILE</dt>
 *  <dd>A simple property file that sets logging parameters.  It is optional
 *      and is only relevant if you use the same logging packages that JOrphan
 *      uses.</dd>
 * 
 *  <dt>NAME_OF_UNITTESTMANAGER_CLASS</dt>
 *  <dd>If your system requires some configuration to run correctly, you can
 *      implement the {@link UnitTestManager} interface and be given an
 *      opportunity to initialize your system from a configuration file.</dd>
 * </dl>
 *
 * @see UnitTestManager
 * @author     Michael Stover (mstover1 at apache.org)
 * @version $Revision$
 */
public final class AllTests
{
    transient private static Logger log = LoggingManager.getLoggerForClass();

    /**
     * Private constructor to prevent instantiation.
     */
    private AllTests()
    {
    }

    /**
     * Starts a run through all unit tests found in the specified classpaths.
     * The first argument should be a list of paths to search.  The second
     * argument is optional and specifies a properties file used to initialize
     * logging.  The third argument is also optional, and specifies a class
     * that implements the UnitTestManager interface.  This provides a means of
     * initializing your application with a configuration file prior to the
     * start of any unit tests.
     *
     * @param  args  the command line arguments
     */
    public static void main(String[] args)
    {
        if (args.length < 1)
        {
            System.out.println(
                "You must specify a comma-delimited list of paths to search " +
                "for unit tests");
            System.exit(0);
        }
        initializeLogging(args);
        initializeManager(args);
        // end : added - 11 July 2001

//++
// GUI tests throw the error 
// testArgumentCreation(org.apache.jmeter.config.gui.ArgumentsPanel$Test)java.lang.NoClassDefFoundError
// 	at java.lang.Class.forName0(Native Method)
// 	at java.lang.Class.forName(Class.java:141)
// 	at java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment(GraphicsEnvironment.java:62)
//
//  Try to find out why this is ...

        String e = "java.awt.headless";
		String g="java.awt.graphicsenv";
		System.out.println("+++++++++++");
        System.out.println(e+"="+System.getProperty(e));
        String n=System.getProperty(g);
		System.out.println(g+"="+n);
//
//		try {//
//			Class c = Class.forName(n);
//			System.out.println("Found class:  "+n);
////			c.newInstance();
////			System.out.println("Instantiated: "+n);
//		} catch (Exception e1) {
//			System.out.println("Error finding class "+n+" "+e1);
//		} catch (java.lang.InternalError e1){
//			System.out.println("Error finding class "+n+" "+e1);
//		}
//
		System.out.println("------------");
// don't call isHeadless() here, as it has a side effect.
//--
        System.out.println("Creating test suite");
        TestSuite suite = suite(args[0]);
		System.out.println("Starting test run");
		
        // Jeremy Arnold: This method used to attempt to write results to
        // a file, but it had a bug and instead just wrote to System.out.
        // Since nobody has complained about this behavior, I'm changing
        // the code to not attempt to write to a file, so it will continue
        // behaving as it did before.  It would be simple to make it write
        // to a file instead if that is the desired behavior.
        TestRunner.run(suite);
//++
//      Recheck settings:
		System.out.println("+++++++++++");
//		System.out.println(e+"="+System.getProperty(e));
//		System.out.println(g+"="+System.getProperty(g));
		System.out.println("Headless? "+java.awt.GraphicsEnvironment.isHeadless());
//		try {
//			Class c = Class.forName(n);
//			System.out.println("Found class:  "+n);
//			c.newInstance();
//			System.out.println("Instantiated: "+n);
//		} catch (Exception e1) {
//			System.out.println("Error with class "+n+" "+e1);
//		} catch (java.lang.InternalError e1){
//		    System.out.println("Error with class "+n+" "+e1);
//	    }
		System.out.println("------------");
//--
        System.exit(0);
    }

    /**
     * An overridable method that initializes the logging for the unit test
     * run, using the properties file passed in as the second argument.
     * @param args 
     */
    protected static void initializeLogging(String[] args)
    {
        if (args.length >= 2)
        {
            Properties props = new Properties();
            try
            {
                System.out.println(
                    "Setting up logging props using file: " + args[1]);
                props.load(new FileInputStream(args[1]));
                LoggingManager.initializeLogging(props);
            }
            catch (FileNotFoundException e)
            {
            }
            catch (IOException e)
            {
            }
        }
    }

    /**
     * An overridable method that that instantiates a UnitTestManager (if one
     * was specified in the command-line arguments), and hands it the name of
     * the properties file to use to configure the system.
     * @param args
     */
    protected static void initializeManager(String[] args)
    {
        if (args.length >= 3)
        {
            try
            {
				System.out.println(
                    "Using initializeProperties() from " + args[2]);
                UnitTestManager um =
                    (UnitTestManager) Class.forName(args[2]).newInstance();
				System.out.println(
					"Setting up initial properties using: " + args[1]);
                um.initializeProperties(args[1]);
            }
            catch (Exception e)
            {
                System.out.println("Couldn't create: " + args[2]);
                e.printStackTrace();
            }
        }
    }

    /*
     * Externally callable suite() method for use by JUnit
     * Allows tests to be run directly under JUnit, rather than using the
     * startup code in the rest of the module. No parameters can be passed in,
     * so it is less flexible.
     */
    public static TestSuite suite()
    {
    	String args[] = { "../lib/ext",
    		              "./jmetertest.properties",
                          "org.apache.jmeter.util.JMeterUtils"
                        };

		initializeManager(args);
		return suite(args[0]);
    }
    
    /**
     * A unit test suite for JUnit.
     *
     * @return    The test suite
     */
    private static TestSuite suite(String searchPaths)
    {
        TestSuite suite = new TestSuite();
        try
        {
            Iterator classes =
                ClassFinder
                    .findClassesThatExtend(
                        JOrphanUtils.split(searchPaths, ","),
                        new Class[] { TestCase.class },
                        true)
                    .iterator();
            while (classes.hasNext())
            {
                String name = (String) classes.next();
                try
                {
                	/*
                	 * TestSuite only finds testXXX() methods, and does not look for
                	 * suite() methods.
                	 *
                	 * To provide more compatibilty with stand-alone tests, where JUnit
                	 * does look for a suite() method, check for it first here.
                	 *  
                	 */

                	Class clazz = Class.forName(name);
					try
					{
						Method m = clazz.getMethod("suite", new Class[0]);
						TestSuite t = (TestSuite) m.invoke(clazz,null);
						suite.addTest(t);
					}
					catch (Exception e)
					{
						TestSuite ts = new TestSuite(clazz);
						suite.addTest(ts);
                	}
                }
                catch (Exception ex)
                {
                	System.out.println("Error adding test for class "+name+" "+ex.toString());
                    log.error("error adding test :" + ex);
                }
            }
        }
        catch (IOException e)
        {
            log.error("", e);
        }
        catch (ClassNotFoundException e)
        {
            log.error("", e);
        }
        return suite;
    }
}
