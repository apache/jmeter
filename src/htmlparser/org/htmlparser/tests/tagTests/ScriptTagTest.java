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

package org.htmlparser.tests.tagTests;

import org.htmlparser.Parser;
import org.htmlparser.StringNode;
import org.htmlparser.scanners.ScriptScanner;
import org.htmlparser.tags.ScriptTag;
import org.htmlparser.tags.data.CompositeTagData;
import org.htmlparser.tags.data.TagData;
import org.htmlparser.tests.ParserTestCase;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

public class ScriptTagTest extends ParserTestCase
{
    private ScriptScanner scriptScanner;

    public ScriptTagTest(String name)
    {
        super(name);
    }

    protected void setUp() throws Exception
    {
        super.setUp();
        scriptScanner = new ScriptScanner();
    }

    public void testCreation()
    {
        StringNode stringNode =
            new StringNode(new StringBuffer("Script Code"), 0, 0);
        NodeList childVector = new NodeList();
        childVector.add(stringNode);
        ScriptTag scriptTag =
            new ScriptTag(
                new TagData(0, 10, "Tag Contents", "tagline"),
                new CompositeTagData(null, null, childVector));

        assertNotNull("Script Tag object creation", scriptTag);
        assertEquals("Script Tag Begin", 0, scriptTag.elementBegin());
        assertEquals("Script Tag End", 10, scriptTag.elementEnd());
        assertEquals(
            "Script Tag Contents",
            "Tag Contents",
            scriptTag.getText());
        assertEquals(
            "Script Tag Code",
            "Script Code",
            scriptTag.getScriptCode());
        assertEquals("Script Tag Line", "tagline", scriptTag.getTagLine());
    }

    public void testToHTML() throws ParserException
    {
        createParser("<SCRIPT>document.write(d+\".com\")</SCRIPT>");
        // Register the image scanner
        parser.addScanner(new ScriptScanner("-s"));

        parseAndAssertNodeCount(1);
        assertTrue("Node should be a script tag", node[0] instanceof ScriptTag);
        // Check the data in the applet tag
        ScriptTag scriptTag = (ScriptTag) node[0];
        assertEquals(
            "Expected Raw String",
            "<SCRIPT>document.write(d+\".com\")</SCRIPT>",
            scriptTag.toHtml());
    }

    /** 
    * Bug check by Wolfgang Germund 2002-06-02 
    * Upon parsing : 
    * &lt;script language="javascript"&gt; 
    * if(navigator.appName.indexOf("Netscape") != -1) 
    * document.write ('xxx'); 
    * else 
    * document.write ('yyy'); 
    * &lt;/script&gt; 
    * check toRawString(). 
    */
    public void testToHTMLWG() throws ParserException
    {
        StringBuffer sb1 = new StringBuffer();
        sb1.append("<body><script language=\"javascript\">\r\n");
        sb1.append("if(navigator.appName.indexOf(\"Netscape\") != -1)\r\n");
        sb1.append(" document.write ('xxx');\r\n");
        sb1.append("else\r\n");
        sb1.append(" document.write ('yyy');\r\n");
        sb1.append("</script>\r\n");
        String testHTML1 = new String(sb1.toString());

        createParser(testHTML1);
        Parser.setLineSeparator("\r\n");
        // Register the image scanner 
        parser.addScanner(new ScriptScanner("-s"));

        StringBuffer sb2 = new StringBuffer();
        sb2.append("<SCRIPT LANGUAGE=\"javascript\">\r\n");
        sb2.append("if(navigator.appName.indexOf(\"Netscape\") != -1)\r\n");
        sb2.append(" document.write ('xxx');\r\n");
        sb2.append("else\r\n");
        sb2.append(" document.write ('yyy');\r\n");
        sb2.append("</SCRIPT>");
        String expectedHTML = new String(sb2.toString());

        parseAndAssertNodeCount(2);
        assertTrue("Node should be a script tag", node[1] instanceof ScriptTag);
        // Check the data in the applet tag 
        ScriptTag scriptTag = (ScriptTag) node[1];
        assertStringEquals(
            "Expected Script Code",
            expectedHTML,
            scriptTag.toHtml());
    }

    public void testParamExtraction() throws ParserException
    {
        StringBuffer sb1 = new StringBuffer();
        sb1.append("<script src=\"/adb.js\" language=\"javascript\">\r\n");
        sb1.append("if(navigator.appName.indexOf(\"Netscape\") != -1)\r\n");
        sb1.append(" document.write ('xxx');\r\n");
        sb1.append("else\r\n");
        sb1.append(" document.write ('yyy');\r\n");
        sb1.append("</script>\r\n");
        createParser(sb1.toString());

        // Register the image scanner 
        parser.addScanner(new ScriptScanner("-s"));
        parseAndAssertNodeCount(1);
        assertTrue("Node should be a script tag", node[0] instanceof ScriptTag);
        ScriptTag scriptTag = (ScriptTag) node[0];
        assertEquals("Script Src", "/adb.js", scriptTag.getAttribute("src"));
        assertEquals(
            "Script Language",
            "javascript",
            scriptTag.getAttribute("language"));
    }

    public void testVariableDeclarations() throws ParserException
    {
        StringBuffer sb1 = new StringBuffer();
        sb1.append("<script language=\"javascript\">\n");
        sb1.append("var lower = '<%=lowerValue%>';\n");
        sb1.append("</script>\n");
        createParser(sb1.toString());

        // Register the image scanner 
        parser.addScanner(new ScriptScanner("-s"));
        //parser.registerScanners();
        parseAndAssertNodeCount(1);
        assertTrue("Node should be a script tag", node[0] instanceof ScriptTag);
        ScriptTag scriptTag = (ScriptTag) node[0];
        assertStringEquals(
            "Script toHTML()",
            "<SCRIPT LANGUAGE=\"javascript\">\r\nvar lower = '<%=lowerValue%>';\r\n</SCRIPT>",
            scriptTag.toHtml());
    }

    public void testSingleApostropheParsingBug() throws ParserException
    {
        StringBuffer sb1 = new StringBuffer();
        sb1.append("<script src='<%=sourceFileName%>'></script>");
        createParser(sb1.toString());

        // Register the image scanner 
        parser.addScanner(new ScriptScanner("-s"));
        parseAndAssertNodeCount(1);
        assertTrue("Node should be a script tag", node[0] instanceof ScriptTag);
        ScriptTag scriptTag = (ScriptTag) node[0];
        assertStringEquals(
            "Script toHTML()",
            "<SCRIPT SRC=\"<%=sourceFileName%>\"></SCRIPT>",
            scriptTag.toHtml());
    }

}
