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
import java.util.Iterator;
import java.util.Properties;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.reflect.ClassFinder;
import org.apache.jorphan.util.JOrphanUtils;
import org.apache.log.Logger;

/************************************************************
 *  Provides a quick and easy way to run all <a href="http://junit.sourceforge.net">junit</a> unit tests in your java project.  It will 
 * find all unit test classes and run all their test methods.  There is no need to configure
 * it in any way to find these classes except to give it a path to search.
 * <p>
 * Here is an example Ant target (See Ant at <a href="http://jakarta.apache.org/ant">Apache</a>) that
 * runs all your unit tests:
 * <pre>
 *		&lt;target name="test" depends="compile"&gt;
 *			&lt;java classname="org.apache.jorphan.test.AllTests" fork="yes"&gt; 
 *				&lt;classpath&gt;
 *					&lt;path refid="YOUR_CLASSPATH"/&gt;
 *					&lt;pathelement location="ROOT_DIR_OF_YOUR_COMPILED_CLASSES"/&gt;
 *				&lt;/classpath&gt;
 *				&lt;arg value="SEARCH_PATH/"/&gt;
 *				&lt;arg value="PROPERTY_FILE"/&gt;
 *				&lt;arg value="NAME_OF_UNITTESTMANAGER_CLASS"/&gt;
 *			&lt;/java&gt;
 *		&lt;/target&gt;
 * </pre>
 * 
 * <DL><dt>YOUR_CLASSPATH
 * 	<DD>Refers to the classpath that includes all jars and libraries need to run your unit tests
 *</dd>
 * </dt><dt>ROOT_DIR_OF_YOUR_COMPILED_CLASSES
 * <dd>The classpath should include the directory where all your project's classes are compiled
 * to, if it doesn't already.  </dd></dt>
 * <dt>SEARCH_PATH
 * <dd>The first argument tells AllTests where to 
 * look for unit test classes to execute.  In most cases, it is identical to 
 * ROOT_DIR_OF_YOUR_COMPILED_CLASSES.  You can specify multiple directories or jars to 
 * search by providing a comma-delimited list.</dd></dt>
 * <dt>PROPERTY_FILE
 * <dd>A simple property file that sets logging parameters.  It is optional and is only relevant
 * if you use the same logging packages that JOrphan uses.</dd></dt>
 * <dt>NAME_OF_UNITTESTMANAGER_CLASS
 * <dd>If your system requires some configuration to run correctly, you can implement the
 * {@link UnitTestManager} interface and be given an opportunity to initialize your system from a configuration 
 * file.</dd></dt></dl>
 *
 *@author     Michael Stover (mstover1 at apache.org)
 *@see UnitTestManager
 ***********************************************************/
public class AllTests
{
	transient private static Logger log = LoggingManager.getLoggerForClass();
	/************************************************************
	 *  Constructor for the AllTests object
	 ***********************************************************/
	public AllTests()
	{
	}

	/************************************************************
	 *  Starts a run through all unit tests found in the specified classpaths.
	 * The first argument should be a list of paths to search.
	 * The second argument is optional and specifies a properties file used to
	 * initialize logging.
	 * The third argument is also optional, and specifies a class that implements the 
	 * UnitTestManager interface.  This provides a means of initializing your application
	 * with a configuration file prior to the start of any unit tests.
	 *
	 *@param  args  The command line arguments
	 ***********************************************************/
	public static void main(String[] args)
	{
		if(args.length < 1)
		{
			System.out.println("You must specify a comma-delimited list of paths to search for unit tests");
			System.exit(0);
		}
		initializeLogging(args);
		initializeManager(args);
		// end : added - 11 July 2001

		TestSuite suite = suite(args[0]);
        // Jeremy Arnold: This method used to attempt to write results to
        // a file, but it had a bug and instead just wrote to System.out.
        // Since nobody has complained about this behavior, I'm changing
        // the code to not attempt to write to a file, so it will continue
        // behaving as it did before.  It would be simple to make it write
        // to a file instead if that is the desired behavior.
		TestRunner.run(suite);
		System.exit(0);
	}

	/**
	 * An overridable method that initializes the logging for the unit test run, using
	 * the properties file passed in as the second argument.
	 * @param args 
	 */
	protected static void initializeLogging(String[] args)
	{
		if (args.length >= 2)
		{
			Properties props = new Properties();
			try
			{
				System.out.println("setting up logging props using file: "+args[1]);
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
	 * An overridable method that that instantiates a UnitTestManager (if one was
	 * specified in the command-line arguments), and hands it the name of the 
	 * properties file to use to configure the system.
	 * @param args
	 */
	protected static void initializeManager(String[] args)
	{
		if(args.length >= 3)
		{
			try
			{
				UnitTestManager um = (UnitTestManager)Class.forName(args[2]).newInstance();
				um.initializeProperties(args[1]);
			}
			catch (Exception e)
			{
				System.out.println("Couldn't create: "+args[2]);
				e.printStackTrace();
			}
		}
	}

	/************************************************************
	 *  A unit test suite for JUnit
	 *
	 *@return    The test suite
	 ***********************************************************/
	private static TestSuite suite(String searchPaths)
	{
		TestSuite suite = new TestSuite();
		try
		{
			Iterator classes = ClassFinder.findClassesThatExtend(
			    		JOrphanUtils.split(searchPaths, ","),
					new Class[]{TestCase.class},true).iterator();
			while (classes.hasNext())
			{
				String name = (String)classes.next();
				try
				{
					suite.addTest(new TestSuite(Class.forName(name)));
				}
				catch (Exception ex)
				{
					log.error("error adding test :"+ex);
				}
			}
		}
		catch (IOException e)
		{
			log.error("",e);
		}
		catch (ClassNotFoundException e)
		{
			log.error("",e);
		}
		return suite;
	}
}


