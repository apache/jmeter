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

package org.htmlparser.tests;

import java.util.Arrays;

import junit.framework.TestSuite;

import org.htmlparser.tests.scannersTests.CompositeTagScannerTest.CustomScanner;
import org.htmlparser.tests.scannersTests.CompositeTagScannerTest.CustomTag;
import org.htmlparser.util.ParserException;
/**
 * @author Somik Raha
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class LineNumberAssignedByNodeReaderTest extends ParserTestCase
{

    public LineNumberAssignedByNodeReaderTest(String name)
    {
        super(name);
    }

    /**
     * Test to ensure that the <code>Tag</code> being created by the
     * <code>CompositeTagScanner</code> has the correct startLine and endLine
     * information in the <code>TagData</code> it is constructed with. 
     * @throws ParserException if there is a problem parsing the test data
     */
    public void testLineNumbers() throws ParserException
    {
        testLineNumber("<Custom/>", 1, 0, 1, 1);
        testLineNumber("<Custom />", 1, 0, 1, 1);
        testLineNumber("<Custom></Custom>", 1, 0, 1, 1);
        testLineNumber("<Custom>Content</Custom>", 1, 0, 1, 1);
        testLineNumber("<Custom>Content<Custom></Custom>", 1, 0, 1, 1);
        testLineNumber(
            "<Custom>\n" + "	Content\n" + "</Custom>",
            1,
            0,
            1,
            3);
        testLineNumber(
            "Foo\n" + "<Custom>\n" + "	Content\n" + "</Custom>",
            2,
            1,
            2,
            4);
        testLineNumber(
            "Foo\n"
                + "<Custom>\n"
                + "	<Custom>SubContent</Custom>\n"
                + "</Custom>",
            2,
            1,
            2,
            4);
        char[] oneHundredNewLines = new char[100];
        Arrays.fill(oneHundredNewLines, '\n');
        testLineNumber(
            "Foo\n"
                + new String(oneHundredNewLines)
                + "<Custom>\n"
                + "	<Custom>SubContent</Custom>\n"
                + "</Custom>",
            2,
            1,
            102,
            104);
    }

    /**
     * Helper method to ensure that the <code>Tag</code> being created by the
     * <code>CompositeTagScanner</code> has the correct startLine and endLine
     * information in the <code>TagData</code> it is constructed with.
     * @param xml String containing HTML or XML to parse, containing a Custom tag
     * @param numNodes int number of expected nodes returned by parser
     * @param useNode int index of the node to test (should be of type CustomTag) 
     * @param startLine int the expected start line number of the tag
     * @param endLine int the expected end line number of the tag
     * @throws ParserException if there is an exception during parsing
     */
    private void testLineNumber(
        String xml,
        int numNodes,
        int useNode,
        int expectedStartLine,
        int expectedEndLine)
        throws ParserException
    {
        createParser(xml);
        parser.addScanner(new CustomScanner());
        parseAndAssertNodeCount(numNodes);
        assertType("custom node", CustomTag.class, node[useNode]);
        CustomTag tag = (CustomTag) node[useNode];
        assertEquals(
            "start line",
            expectedStartLine,
            tag.tagData.getStartLine());
        assertEquals("end line", expectedEndLine, tag.tagData.getEndLine());

    }

    public static TestSuite suite()
    {
        TestSuite suite = new TestSuite("Line Number Tests");
        suite.addTestSuite(LineNumberAssignedByNodeReaderTest.class);
        return (suite);
    }
}
