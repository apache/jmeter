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

import java.util.Hashtable;

import org.htmlparser.Parser;
import org.htmlparser.parserHelper.AttributeParser;
import org.htmlparser.tags.Tag;
import org.htmlparser.tags.data.TagData;
import org.htmlparser.tests.ParserTestCase;

public class AttributeParserTest extends ParserTestCase
{
    private AttributeParser parser;
    private Tag tag;
    private Hashtable table;

    public AttributeParserTest(String name)
    {
        super(name);
    }

    protected void setUp()
    {
        parser = new AttributeParser();
    }

    public void getParameterTableFor(String tagContents)
    {
        tag = new Tag(new TagData(0, 0, tagContents, ""));
        table = parser.parseAttributes(tag);

    }

    public void testParseParameters()
    {
        getParameterTableFor("a b = \"c\"");
        assertEquals("Value", "c", table.get("B"));
    }

    public void testParseTokenValues()
    {
        getParameterTableFor("a b = \"'\"");
        assertEquals("Value", "'", table.get("B"));
    }

    public void testParseEmptyValues()
    {
        getParameterTableFor("a b = \"\"");
        assertEquals("Value", "", table.get("B"));
    }

    public void testParseMissingEqual()
    {
        getParameterTableFor("a b\"c\"");
        assertEquals("ValueB", "", table.get("B"));

    }

    public void testTwoParams()
    {
        getParameterTableFor("PARAM NAME=\"Param1\" VALUE=\"Somik\">\n");
        assertEquals("Param1", "Param1", table.get("NAME"));
        assertEquals("Somik", "Somik", table.get("VALUE"));
    }

    public void testPlainParams()
    {
        getParameterTableFor("PARAM NAME=Param1 VALUE=Somik");
        assertEquals("Param1", "Param1", table.get("NAME"));
        assertEquals("Somik", "Somik", table.get("VALUE"));
    }

    public void testValueMissing()
    {
        getParameterTableFor("INPUT type=\"checkbox\" name=\"Authorize\" value=\"Y\" checked");
        assertEquals("Name of Tag", "INPUT", table.get(Tag.TAGNAME));
        assertEquals("Type", "checkbox", table.get("TYPE"));
        assertEquals("Name", "Authorize", table.get("NAME"));
        assertEquals("Value", "Y", table.get("VALUE"));
        assertEquals("Checked", "", table.get("CHECKED"));
    }

    /**
     * This is a simulation of a bug reported by Dhaval Udani - wherein 
     * a space before the end of the tag causes a problem - there is a key
     * in the table with just a space in it and an empty value
     */
    public void testIncorrectSpaceKeyBug()
    {
        getParameterTableFor("TEXTAREA name=\"Remarks\" ");
        // There should only be two keys..
        assertEquals("There should only be two keys", 2, table.size());
        // The first key is name
        String key1 = "NAME";
        String value1 = (String) table.get(key1);
        assertEquals("Expected value 1", "Remarks", value1);
        String key2 = Tag.TAGNAME;
        assertEquals("Expected Value 2", "TEXTAREA", table.get(key2));
    }

    public void testNullTag()
    {
        getParameterTableFor("INPUT type=");
        assertEquals("Name of Tag", "INPUT", table.get(Tag.TAGNAME));
        assertEquals("Type", "", table.get("TYPE"));
    }

    public void testAttributeWithSpuriousEqualTo()
    {
        getParameterTableFor("a class=rlbA href=/news/866201.asp?0sl=-32");
        assertStringEquals(
            "href",
            "/news/866201.asp?0sl=-32",
            (String) table.get("HREF"));
    }

    public void testQuestionMarksInAttributes()
    {
        getParameterTableFor("a href=\"mailto:sam@neurogrid.com?subject=Site Comments\"");
        assertStringEquals(
            "href",
            "mailto:sam@neurogrid.com?subject=Site Comments",
            (String) table.get("HREF"));
        assertStringEquals("tag name", "A", (String) table.get(Tag.TAGNAME));
    }

    /**
     * Believe it or not Moi (vincent_aumont) wants htmlparser to parse a text file
     * containing something that looks nearly like a tag:
     * <pre>
     * "                        basic_string&lt;char, string_char_traits&lt;char&gt;, &lt;&gt;&gt;::basic_string()"
     * </pre>
     * This was throwing a null pointer exception when the empty &lt;&gt; was encountered.
     * Bug #725420 NPE in StringBean.visitTag
     **/
    public void testEmptyTag()
    {
        getParameterTableFor("");
        assertNotNull("No Tag.TAGNAME", table.get(Tag.TAGNAME));
    }

    /**
     * Test attributes when they contain scriptlets.
     * Submitted by Cory Seefurth
     * See also feature request #725376 Handle script in attributes.
     * Only perform this test if it's version 1.4 or higher.
     */
    public void testJspWithinAttributes()
    {
        Parser parser;

        parser = new Parser();
        if (1.4 <= Parser.getVersionNumber())
        {
            getParameterTableFor("a href=\"<%=Application(\"sURL\")%>/literature/index.htm");
            assertStringEquals(
                "href",
                "<%=Application(\"sURL\")%>/literature/index.htm",
                (String) table.get("HREF"));
        }
    }

    /**
     * Test Script in attributes.
     * See feature request #725376 Handle script in attributes.
     * Only perform this test if it's version 1.4 or higher.
     */
    public void testScriptedTag()
    {
        Parser parser;

        parser = new Parser();
        if (1.4 <= Parser.getVersionNumber())
        {
            getParameterTableFor("body onLoad=defaultStatus=''");
            String name = (String) table.get(Tag.TAGNAME);
            assertNotNull("No Tag.TAGNAME", name);
            assertStringEquals("tag name parsed incorrectly", "BODY", name);
            String value = (String) table.get("ONLOAD");
            assertStringEquals(
                "parameter parsed incorrectly",
                "defaultStatus=''",
                value);
        }
    }
}
