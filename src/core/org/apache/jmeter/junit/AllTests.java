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
package org.apache.jmeter.junit;

import java.io.*;
import java.util.*;
import junit.framework.*;
import org.apache.jmeter.util.ClassFinder;
import org.apache.jmeter.util.JMeterUtils;

/************************************************************
 *  Title: Apache JMeter Description: Copyright: Copyright (c) 2000 Company:
 *  Apache Foundation
 *
 *@author     Michael Stover
 *@created    $Date$
 *@version    1.0
 ***********************************************************/
public class AllTests
{
	/************************************************************
	 *  Constructor for the AllTests object
	 ***********************************************************/
	public AllTests()
	{
	}

	/************************************************************
	 *  The main program for the AllTests class
	 *
	 *@param  args  The command line arguments
	 ***********************************************************/
	public static void main(String[] args)
	{
		if (args.length < 1)
		{
			System.out.println("Need an argument specifying the jmeter property file");
			System.exit(0);
		}
		JMeterUtils.getProperties(args[0]);
		JMeterUtils.setJMeterHome(new File(System.getProperty("user.dir")).getParent());
		if (System.getProperty("log4j.configuration") == null)
		{
			File conf = new File(JMeterUtils.getJMeterHome(), "bin" + File.separator + "log4j.conf");
			System.setProperty("log4j.configuration", "file:" + conf);
		}
		// end : added - 11 July 2001
		try
		{
			TestSuite suite = suite();
			PrintStream out = new PrintStream(new FileOutputStream("testresults.txt"));
			junit.textui.TestRunner runner = new junit.textui.TestRunner(out);
			runner.run(suite);
			out.close();
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		System.exit(0);
	}

	/************************************************************
	 *  A unit test suite for JUnit
	 *
	 *@return    The test suite
	 ***********************************************************/
	public static TestSuite suite()
	{
		TestSuite suite = new TestSuite();
		try
		{
			Iterator classes = ClassFinder.findClassesThatExtend(new Class[]{TestCase.class},true).iterator();
			while (classes.hasNext())
			{
				String name = (String)classes.next();
				try
				{
					suite.addTest(new TestSuite(Class.forName(name)));
				}
				catch (Exception ex)
				{
					System.out.println("error adding test :"+ex);
				}
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (ClassNotFoundException e)
		{
			e.printStackTrace();
		}
		return suite;
	}
}


