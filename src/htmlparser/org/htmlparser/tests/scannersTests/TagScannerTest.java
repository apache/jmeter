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


package org.htmlparser.tests.scannersTests;
import org.htmlparser.Node;
import org.htmlparser.NodeReader;
import org.htmlparser.Parser;
import org.htmlparser.scanners.TagScanner;
import org.htmlparser.tags.Tag;
import org.htmlparser.tests.ParserTestCase;
import org.htmlparser.util.NodeIterator;
import org.htmlparser.util.ParserException;
import org.htmlparser.util.ParserUtils;

public class TagScannerTest extends ParserTestCase
{

    public TagScannerTest(String name)
    {
        super(name);
    }

    public void testAbsorbLeadingBlanks()
    {
        String test = "   This is a test";
        String result = TagScanner.absorbLeadingBlanks(test);
        assertEquals("Absorb test", "This is a test", result);
    }

    public void testExtractXMLData() throws ParserException
    {
        createParser("<MESSAGE>\n" + "Abhi\n" + "Sri\n" + "</MESSAGE>");
        Parser.setLineSeparator("\r\n");
        NodeIterator e = parser.elements();

        Node node = e.nextNode();
        try
        {
            String result =
                TagScanner.extractXMLData(node, "MESSAGE", parser.getReader());
            assertEquals("Result", "Abhi\r\nSri\r\n", result);
        }
        catch (ParserException ex)
        {
            assertTrue(e.toString(), false);
        }
    }

    public void testExtractXMLDataSingle() throws ParserException
    {
        createParser("<MESSAGE>Test</MESSAGE>");
        NodeIterator e = parser.elements();

        Node node = (Node) e.nextNode();
        try
        {
            String result =
                TagScanner.extractXMLData(node, "MESSAGE", parser.getReader());
            assertEquals("Result", "Test", result);
        }
        catch (ParserException ex)
        {
            assertTrue(e.toString(), false);
        }
    }

    public void testTagExtraction()
    {
        String testHTML =
            "<AREA \n coords=0,0,52,52 href=\"http://www.yahoo.com/r/c1\" shape=RECT>";
        createParser(testHTML);
        Tag tag = Tag.find(parser.getReader(), testHTML, 0);
        assertNotNull(tag);
    }

    /**
     * Captures bug reported by Raghavender Srimantula
     * Problem is in isXMLTag - when it uses equals() to 
     * find a match
     */
    public void testIsXMLTag() throws ParserException
    {
        createParser("<OPTION value=\"#\">Select a destination</OPTION>");
        Node node;
        NodeIterator e = parser.elements();
        node = (Node) e.nextNode();
        assertTrue(
            "OPTION tag could not be identified",
            TagScanner.isXMLTagFound(node, "OPTION"));
    }

    public void testRemoveChars()
    {
        String test = "hello\nworld\n\tqsdsds";
        TagScanner scanner = new TagScanner()
        {
            public Tag scan(
                Tag tag,
                String url,
                NodeReader reader,
                String currLine)
            {
                return null;
            }
            public boolean evaluate(String s, TagScanner previousOpenScanner)
            {
                return false;
            }
            public String[] getID()
            {

                return null;
            }
        };
        String result = ParserUtils.removeChars(test, '\n');
        assertEquals("Removing Chars", "helloworld\tqsdsds", result);
    }

    public void testRemoveChars2()
    {
        String test = "hello\r\nworld\r\n\tqsdsds";
        TagScanner scanner = new TagScanner()
        {
            public Tag scan(
                Tag tag,
                String url,
                NodeReader reader,
                String currLine)
            {
                return null;
            }
            public boolean evaluate(String s, TagScanner previousOpenScanner)
            {
                return false;
            }
            public String[] getID()
            {
                return null;
            }

        };
        String result = scanner.removeChars(test, "\r\n");
        assertEquals("Removing Chars", "helloworld\tqsdsds", result);
    }

    /**
     * Bug report by Cedric Rosa
     * in absorbLeadingBlanks - crashes if the tag 
     * is empty
     */
    public void testAbsorbLeadingBlanksBlankTag()
    {
        String testData = new String("");
        String result = TagScanner.absorbLeadingBlanks(testData);
        assertEquals("", result);
    }

}
