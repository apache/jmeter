/*
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001-2003 The Apache Software Foundation.  All rights
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
 * 
 */

// The developers of JMeter and Apache are greatful to the developers
// of HTMLParser for giving Apache Software Foundation a non-exclusive
// license. The performance benefits of HTMLParser are clear and the
// users of JMeter will benefit from the hard work the HTMLParser
// team. For detailed information about HTMLParser, the project is
// hosted on sourceforge at http://htmlparser.sourceforge.net/.
//
// HTMLParser was originally created by Somik Raha in 2000. Since then
// a healthy community of users has formed and helped refine the
// design so that it is able to tackle the difficult task of parsing
// dirty HTML. Derrick Oswald is the current lead developer and was kind
// enough to assist JMeter.

package org.htmlparser.tests.parserHelperTests;

import junit.framework.TestSuite;

public class AllTests extends junit.framework.TestCase
{

    public AllTests(String name)
    {
        super(name);
    }

    public static TestSuite suite()
    {
        TestSuite suite = new TestSuite("Parser Helper Tests");

        suite.addTestSuite(AttributeParserTest.class);
        suite.addTestSuite(CompositeTagScannerHelperTest.class);
        suite.addTestSuite(RemarkNodeParserTest.class);
        suite.addTestSuite(StringParserTest.class);
        suite.addTestSuite(TagParserTest.class);

        return suite;
    }

    /**
     * Mainline for all suites of tests.
     * @param args Command line arguments. The following options
     * are understood:
     * <pre>
     * -text  -- use junit.textui.TestRunner
     * -awt   -- use junit.awtui.TestRunner
     * -swing -- use junit.swingui.TestRunner (default)
     * </pre>
     * All other options are passed on to the junit framework.
     */
    public static void main(String[] args)
    {
        String runner;
        int i;
        String arguments[];
        Class cls;

        runner = null;
        for (i = 0;(i < args.length) && (null == runner); i++)
        {
            if (args[i].equalsIgnoreCase("-text"))
                runner = "junit.textui.TestRunner";
            else if (args[i].equalsIgnoreCase("-awt"))
                runner = "junit.awtui.TestRunner";
            else if (args[i].equalsIgnoreCase("-swing"))
                runner = "junit.swingui.TestRunner";
        }
        if (null != runner)
        {
            // remove it from the arguments
            arguments = new String[args.length - 1];
            System.arraycopy(args, 0, arguments, 0, i - 1);
            System.arraycopy(args, i, arguments, i - 1, args.length - i);
            args = arguments;
        }
        else
            runner = "junit.swingui.TestRunner";

        // append the test class
        arguments = new String[args.length + 1];
        System.arraycopy(args, 0, arguments, 0, args.length);
        arguments[args.length] =
            "org.htmlparser.tests.parserHelperTests.AllTests";

        // invoke main() of the test runner
        try
        {
            cls = Class.forName(runner);
            java.lang.reflect.Method method =
                cls.getDeclaredMethod("main", new Class[] { String[].class });
            method.invoke(null, new Object[] { arguments });
        }
        catch (Throwable t)
        {
            System.err.println("cannot run unit test (" + t.getMessage() + ")");
        }
    }
}