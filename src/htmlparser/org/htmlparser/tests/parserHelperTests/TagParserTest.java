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
import java.util.HashMap;
import java.util.Map;

import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.tags.Tag;
import org.htmlparser.tests.ParserTestCase;
import org.htmlparser.util.ParserException;

public class TagParserTest extends ParserTestCase
{
    private static final String TEST_HTML =
        "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\">"
            + "<!-- Server: sf-web2 -->"
            + "<html lang=\"en\">"
            + "  <head><link rel=\"stylesheet\" type=\"text/css\" href=\"http://sourceforge.net/cssdef.php\">"
            + "	<meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\">"
            + "    <TITLE>SourceForge.net: Modify: 711073 - HTMLTagParser not threadsafe as a static variable in Tag</TITLE>"
            + "	<SCRIPT language=\"JavaScript\" type=\"text/javascript\">"
            + "	<!--"
            + "	function help_window(helpurl) {"
            + "		HelpWin = window.open( 'http://sourceforge.net' + helpurl,'HelpWindow','scrollbars=yes,resizable=yes,toolbar=no,height=400,width=400');"
            + "	}"
            + "	// -->"
            + "	</SCRIPT>"
            + "		<link rel=\"SHORTCUT ICON\" href=\"/images/favicon.ico\">"
            + "<!-- This is temp javascript for the jump button. If we could actually have a jump script on the server side that would be ideal -->"
            + "<script language=\"JavaScript\" type=\"text/javascript\">"
            + "<!--"
            + "	function jump(targ,selObj,restore){ //v3.0"
            + "	if (selObj.options[selObj.selectedIndex].value) "
            + "		eval(targ+\".location='\"+selObj.options[selObj.selectedIndex].value+\"'\");"
            + "	if (restore) selObj.selectedIndex=0;"
            + "	}"
            + "	//-->"
            + "</script>"
            + "<a href=\"http://normallink.com/sometext.html\">"
            + "<style type=\"text/css\">"
            + "<!--"
            + "A:link { text-decoration:none }"
            + "A:visited { text-decoration:none }"
            + "A:active { text-decoration:none }"
            + "A:hover { text-decoration:underline; color:#0066FF; }"
            + "-->"
            + "</style>"
            + "</head>"
            + "<body bgcolor=\"#FFFFFF\" text=\"#000000\" leftmargin=\"0\" topmargin=\"0\" marginwidth=\"0\" marginheight=\"0\" link=\"#003399\" vlink=\"#003399\" alink=\"#003399\">";
    private Map results;
    private int testProgress;

    public TagParserTest(String name)
    {
        super(name);
    }

    public void testTagWithQuotes() throws Exception
    {
        String testHtml =
            "<img src=\"http://g-images.amazon.com/images/G/01/merchants/logos/marshall-fields-logo-20.gif\" width=87 height=20 border=0 alt=\"Marshall Field's\">";

        createParser(testHtml);
        parseAndAssertNodeCount(1);
        assertType("should be Tag", Tag.class, node[0]);
        Tag tag = (Tag) node[0];
        assertStringEquals("alt", "Marshall Field's", tag.getAttribute("ALT"));
        assertStringEquals(
            "html",
            "<IMG BORDER=\"0\" ALT=\"Marshall Field's\" WIDTH=\"87\" SRC=\"http://g-images.amazon.com/images/G/01/merchants/logos/marshall-fields-logo-20.gif\" HEIGHT=\"20\">",
            tag.toHtml());
    }

    public void testEmptyTag() throws Exception
    {
        createParser("<custom/>");
        parseAndAssertNodeCount(1);
        assertType("should be Tag", Tag.class, node[0]);
        Tag tag = (Tag) node[0];
        assertStringEquals("tag name", "CUSTOM", tag.getTagName());
        assertTrue("empty tag", tag.isEmptyXmlTag());
        assertStringEquals("html", "<CUSTOM/>", tag.toHtml());
    }

    public void testTagWithCloseTagSymbolInAttribute() throws ParserException
    {
        createParser("<tag att=\"a>b\">");
        parseAndAssertNodeCount(1);
        assertType("should be Tag", Tag.class, node[0]);
        Tag tag = (Tag) node[0];
        assertStringEquals("attribute", "a>b", tag.getAttribute("att"));
    }

    public void testTagWithOpenTagSymbolInAttribute() throws ParserException
    {
        createParser("<tag att=\"a<b\">");
        parseAndAssertNodeCount(1);
        assertType("should be Tag", Tag.class, node[0]);
        Tag tag = (Tag) node[0];
        assertStringEquals("attribute", "a<b", tag.getAttribute("att"));
    }

    public void testTagWithSingleQuote() throws ParserException
    {
        createParser("<tag att=\'a<b\'>");
        parseAndAssertNodeCount(1);
        assertType("should be Tag", Tag.class, node[0]);
        Tag tag = (Tag) node[0];
        assertStringEquals("html", "<TAG ATT=\"a<b\">", tag.toHtml());
        assertStringEquals("attribute", "a<b", tag.getAttribute("att"));
    }

    /**
     * The following multi line test cases are from
     * bug #725749 Parser does not handle < and > in multi-line attributes
     * submitted by Joe Robins (zorblak)
     */

    public void testMultiLine1() throws ParserException
    {
        createParser("<meta name=\"foo\" content=\"foo<bar>\">");
        parseAndAssertNodeCount(1);
        assertType("should be Tag", Tag.class, node[0]);
        Tag tag = (Tag) node[0];
        String html = tag.toHtml();
        assertStringEquals(
            "html",
            "<META CONTENT=\"foo<bar>\" NAME=\"foo\">",
            html);
        String attribute1 = tag.getAttribute("NAME");
        assertStringEquals("attribute 1", "foo", attribute1);
        String attribute2 = tag.getAttribute("CONTENT");
        assertStringEquals("attribute 2", "foo<bar>", attribute2);
    }

    public void testMultiLine2() throws ParserException
    {
        createParser("<meta name=\"foo\" content=\"foo<bar\">");
        parseAndAssertNodeCount(1);
        assertType("should be Tag", Tag.class, node[0]);
        Tag tag = (Tag) node[0];
        String html = tag.toHtml();
        assertStringEquals(
            "html",
            "<META CONTENT=\"foo<bar\" NAME=\"foo\">",
            html);
        String attribute1 = tag.getAttribute("NAME");
        assertStringEquals("attribute 1", "foo", attribute1);
        String attribute2 = tag.getAttribute("CONTENT");
        assertStringEquals("attribute 2", "foo<bar", attribute2);
    }

    public void testMultiLine3() throws ParserException
    {
        createParser("<meta name=\"foo\" content=\"foobar>\">");
        parseAndAssertNodeCount(1);
        assertType("should be Tag", Tag.class, node[0]);
        Tag tag = (Tag) node[0];
        String html = tag.toHtml();
        assertStringEquals(
            "html",
            "<META CONTENT=\"foobar>\" NAME=\"foo\">",
            html);
        String attribute1 = tag.getAttribute("NAME");
        assertStringEquals("attribute 1", "foo", attribute1);
        String attribute2 = tag.getAttribute("CONTENT");
        assertStringEquals("attribute 2", "foobar>", attribute2);
    }

    public void testMultiLine4() throws ParserException
    {
        createParser("<meta name=\"foo\" content=\"foo\nbar>\">");
        parseAndAssertNodeCount(1);
        assertType("should be Tag", Tag.class, node[0]);
        Tag tag = (Tag) node[0];
        String html = tag.toHtml();
        assertStringEquals(
            "html",
            "<META CONTENT=\"foo\r\nbar>\" NAME=\"foo\">",
            html);
        String attribute1 = tag.getAttribute("NAME");
        assertStringEquals("attribute 1", "foo", attribute1);
        String attribute2 = tag.getAttribute("CONTENT");
        assertStringEquals("attribute 2", "foo\r\nbar>", attribute2);
    }

    /**
     * Test multiline tag like attribute.
     * See feature request #725749 Handle < and > in multi-line attributes.
     * Only perform this test if it's version 1.4 or higher.
     */
    public void testMultiLine5() throws ParserException
    {
        // <meta name="foo" content="<foo>
        // bar">
        createParser("<meta name=\"foo\" content=\"<foo>\nbar\">");
        if (1.4 <= Parser.getVersionNumber())
        {
            parseAndAssertNodeCount(1);
            assertType("should be Tag", Tag.class, node[0]);
            Tag tag = (Tag) node[0];
            String html = tag.toHtml();
            assertStringEquals(
                "html",
                "<META CONTENT=\"<foo>\r\nbar\" NAME=\"foo\">",
                html);
            String attribute1 = tag.getAttribute("NAME");
            assertStringEquals("attribute 1", "foo", attribute1);
            String attribute2 = tag.getAttribute("CONTENT");
            assertStringEquals("attribute 2", "<foo>\r\nbar", attribute2);
        }
    }

    /**
     * Test multiline broken tag like attribute.
     * See feature request #725749 Handle < and > in multi-line attributes.
     * Only perform this test if it's version 1.4 or higher.
     */
    public void testMultiLine6() throws ParserException
    {
        // <meta name="foo" content="foo>
        // bar">
        createParser("<meta name=\"foo\" content=\"foo>\nbar\">");
        if (1.4 <= Parser.getVersionNumber())
        {
            parseAndAssertNodeCount(1);
            assertType("should be Tag", Tag.class, node[0]);
            Tag tag = (Tag) node[0];
            String html = tag.toHtml();
            assertStringEquals(
                "html",
                "<META CONTENT=\"foo>\r\nbar\" NAME=\"foo\">",
                html);
            String attribute1 = tag.getAttribute("NAME");
            assertStringEquals("attribute 1", "foo", attribute1);
            String attribute2 = tag.getAttribute("CONTENT");
            assertStringEquals("attribute 2", "foo>\r\nbar", attribute2);
        }
    }

    /**
     * Test multiline split tag like attribute.
     * See feature request #725749 Handle < and > in multi-line attributes.
     * Only perform this test if it's version 1.4 or higher.
     */
    public void testMultiLine7() throws ParserException
    {
        // <meta name="foo" content="<foo
        // bar">
        createParser("<meta name=\"foo\" content=\"<foo\nbar\"");
        if (1.4 <= Parser.getVersionNumber())
        {
            parseAndAssertNodeCount(1);
            assertType("should be Tag", Tag.class, node[0]);
            Tag tag = (Tag) node[0];
            String html = tag.toHtml();
            assertStringEquals(
                "html",
                "<META CONTENT=\"<foo\r\nbar\" NAME=\"foo\">",
                html);
            String attribute1 = tag.getAttribute("NAME");
            assertStringEquals("attribute 1", "foo", attribute1);
            String attribute2 = tag.getAttribute("CONTENT");
            assertStringEquals("attribute 2", "<foo\r\nbar", attribute2);
        }
    }

    /**
     * End of multi line test cases.
     */

    /**
     * Test multiple threads running against the parser.
     * See feature request #736144 Handle multi-threaded operation.
     * Only perform this test if it's version 1.4 or higher.
     */
    public void testThreadSafety() throws Exception
    {
        createParser("<html></html>");
        if (1.4 <= Parser.getVersionNumber())
        {
            String testHtml1 =
                "<a HREF=\"/cgi-bin/view_search?query_text=postdate>20020701&txt_clr=White&bg_clr=Red&url=http://localhost/Testing/Report1.html\">20020702 Report 1</A>"
                    + TEST_HTML;

            String testHtml2 =
                "<a href=\"http://normallink.com/sometext.html\">" + TEST_HTML;
            ParsingThread parsingThread[] = new ParsingThread[100];
            results = new HashMap();
            testProgress = 0;
            for (int i = 0; i < parsingThread.length; i++)
            {
                if (i < parsingThread.length / 2)
                    parsingThread[i] =
                        new ParsingThread(i, testHtml1, parsingThread.length);
                else
                    parsingThread[i] =
                        new ParsingThread(i, testHtml2, parsingThread.length);

                Thread thread = new Thread(parsingThread[i]);
                thread.start();
            }

            int completionValue = computeCompletionValue(parsingThread.length);

            do
            {
                try
                {
                    Thread.sleep(50);
                }
                catch (InterruptedException e)
                {
                }
            }
            while (testProgress != completionValue);
            for (int i = 0; i < parsingThread.length; i++)
            {
                if (!parsingThread[i].passed())
                {
                    assertNotNull(
                        "Thread " + i + " link 1",
                        parsingThread[i].getLink1());
                    assertNotNull(
                        "Thread " + i + " link 2",
                        parsingThread[i].getLink2());
                    if (i < parsingThread.length / 2)
                    {
                        assertStringEquals(
                            "Thread " + i + ", link 1:",
                            "/cgi-bin/view_search?query_text=postdate>20020701&txt_clr=White&bg_clr=Red&url=http://localhost/Testing/Report1.html",
                            parsingThread[i].getLink1().getLink());
                        assertStringEquals(
                            "Thread " + i + ", link 2:",
                            "http://normallink.com/sometext.html",
                            parsingThread[i].getLink2().getLink());
                    }
                    else
                    {
                        assertStringEquals(
                            "Thread " + i + ", link 1:",
                            "http://normallink.com/sometext.html",
                            parsingThread[i].getLink1().getLink());
                        assertNotNull(
                            "Thread " + i + " link 2",
                            parsingThread[i].getLink2());
                        assertStringEquals(
                            "Thread " + i + ", link 2:",
                            "/cgi-bin/view_search?query_text=postdate>20020701&txt_clr=White&bg_clr=Red&url=http://localhost/Testing/Report1.html",
                            parsingThread[i].getLink2().getLink());
                    }
                }
            }
        }

    }

    private int computeCompletionValue(int numThreads)
    {
        return numThreads * (numThreads - 1) / 2;
    }

    class ParsingThread implements Runnable
    {
        Parser parser;
        int id;
        LinkTag link1, link2;
        boolean result;
        int max;

        ParsingThread(int id, String testHtml, int max)
        {
            this.id = id;
            this.max = max;
            this.parser = Parser.createParser(testHtml);
            parser.registerScanners();
        }

        public void run()
        {
            try
            {
                result = false;
                Node linkTag[] = parser.extractAllNodesThatAre(LinkTag.class);
                link1 = (LinkTag) linkTag[0];
                link2 = (LinkTag) linkTag[1];
                if (id < max / 2)
                {
                    if (link1
                        .getLink()
                        .equals("/cgi-bin/view_search?query_text=postdate>20020701&txt_clr=White&bg_clr=Red&url=http://localhost/Testing/Report1.html")
                        && link2.getLink().equals(
                            "http://normallink.com/sometext.html"))
                        result = true;
                }
                else
                {
                    if (link1
                        .getLink()
                        .equals("http://normallink.com/sometext.html")
                        && link2.getLink().equals(
                            "http://normallink.com/sometext.html"))
                        result = true;
                }
            }
            catch (ParserException e)
            {
                System.err.println("Parser Exception");
                e.printStackTrace();
            }
            finally
            {
                testProgress += id;
            }
        }

        public LinkTag getLink1()
        {
            return link1;
        }

        public LinkTag getLink2()
        {
            return link2;
        }

        public boolean passed()
        {
            return result;
        }
    }
}