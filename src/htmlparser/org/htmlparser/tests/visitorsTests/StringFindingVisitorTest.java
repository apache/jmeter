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

package org.htmlparser.tests.visitorsTests;

import org.htmlparser.tests.ParserTestCase;
import org.htmlparser.visitors.StringFindingVisitor;

public class StringFindingVisitorTest extends ParserTestCase
{
    private static final String HTML =
        "<HTML><HEAD><TITLE>This is the Title</TITLE>"
            + "</HEAD><BODY>Hello World, this is an excellent parser</BODY></HTML>";

    private static final String HTML_TO_SEARCH =
        "<HTML><HEAD><TITLE>test</TITLE></HEAD>\n"
            + "<BODY><H1>This is a test page</H1>\n"
            + "Writing tests is good for code. Testing is a good\n"
            + "philosophy. Test driven development is even better.\n";

    public StringFindingVisitorTest(String name)
    {
        super(name);
    }

    public void testSimpleStringFind() throws Exception
    {
        createParser(HTML);
        StringFindingVisitor visitor = new StringFindingVisitor("Hello");
        parser.visitAllNodesWith(visitor);
        assertTrue("Hello found", visitor.stringWasFound());
    }

    public void testStringNotFound() throws Exception
    {
        createParser(HTML);
        StringFindingVisitor visitor =
            new StringFindingVisitor("industrial logic");
        parser.visitAllNodesWith(visitor);
        assertTrue(
            "industrial logic should not have been found",
            !visitor.stringWasFound());
    }

    public void testStringInTagNotFound() throws Exception
    {
        createParser(HTML);
        StringFindingVisitor visitor = new StringFindingVisitor("HTML");
        parser.visitAllNodesWith(visitor);
        assertTrue(
            "HTML should not have been found",
            !visitor.stringWasFound());
    }

    public void testStringFoundInSingleStringNode() throws Exception
    {
        createParser("this is some text!");
        StringFindingVisitor visitor = new StringFindingVisitor("text");
        parser.visitAllNodesWith(visitor);
        assertTrue("text should be found", visitor.stringWasFound());
    }

    public void testStringFoundCount() throws Exception
    {
        createParser(HTML);
        StringFindingVisitor visitor = new StringFindingVisitor("is");
        parser.visitAllNodesWith(visitor);
        assertEquals("# times 'is' was found", 2, visitor.stringFoundCount());

        visitor = new StringFindingVisitor("and");
        parser.visitAllNodesWith(visitor);
        assertEquals("# times 'and' was found", 0, visitor.stringFoundCount());
    }

    public void testStringFoundMultipleTimes() throws Exception
    {
        createParser(HTML_TO_SEARCH);
        StringFindingVisitor visitor = new StringFindingVisitor("TEST");
        visitor.doMultipleSearchesWithinStrings();
        parser.visitAllNodesWith(visitor);
        assertEquals("TEST found", 5, visitor.stringFoundCount());
    }

}
