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
import org.htmlparser.scanners.LinkScanner;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.tags.data.CompositeTagData;
import org.htmlparser.tags.data.LinkData;
import org.htmlparser.tags.data.TagData;
import org.htmlparser.tests.ParserTestCase;
import org.htmlparser.util.ParserException;

public class LinkTagTest extends ParserTestCase
{

    public LinkTagTest(String name)
    {
        super(name);
    }

    /**
     * The bug being reproduced is this : <BR>
     * &lt;BODY aLink=#ff0000 bgColor=#ffffff link=#0000cc onload=setfocus() text=#000000 <BR>
     * vLink=#551a8b&gt;
     * The above line is incorrectly parsed in that, the BODY tag is not identified.
     * Creation date: (6/17/2001 4:01:06 PM)
     */
    public void testLinkNodeBug() throws ParserException
    {
        createParser(
            "<A HREF=\"../test.html\">abcd</A>",
            "http://www.google.com/test/index.html");
        // Register the image scanner
        parser.addScanner(new LinkScanner("-l"));

        parseAndAssertNodeCount(1);
        // The node should be an HTMLLinkTag
        assertTrue("Node should be a HTMLLinkTag", node[0] instanceof LinkTag);
        LinkTag linkNode = (LinkTag) node[0];
        assertEquals(
            "The image locn",
            "http://www.google.com/test.html",
            linkNode.getLink());
    }

    /**
     * The bug being reproduced is this : <BR>
     * &lt;BODY aLink=#ff0000 bgColor=#ffffff link=#0000cc onload=setfocus() text=#000000 <BR>
     * vLink=#551a8b&gt;
     * The above line is incorrectly parsed in that, the BODY tag is not identified.
     * Creation date: (6/17/2001 4:01:06 PM)
     */
    public void testLinkNodeBug2() throws ParserException
    {
        createParser(
            "<A HREF=\"../../test.html\">abcd</A>",
            "http://www.google.com/test/test/index.html");
        // Register the image scanner
        parser.addScanner(new LinkScanner("-l"));

        parseAndAssertNodeCount(1);
        // The node should be an HTMLLinkTag
        assertTrue("Node should be a HTMLLinkTag", node[0] instanceof LinkTag);
        LinkTag linkNode = (LinkTag) node[0];
        assertEquals(
            "The image locn",
            "http://www.google.com/test.html",
            linkNode.getLink());
    }

    /**
     * The bug being reproduced is this : <BR>
     * When a url ends with a slash, and the link begins with a slash,the parser puts two slashes
     * This bug was submitted by Roget Kjensrud
     * Creation date: (6/17/2001 4:01:06 PM)
     */
    public void testLinkNodeBug3() throws ParserException
    {
        createParser("<A HREF=\"/mylink.html\">abcd</A>", "http://www.cj.com/");
        // Register the image scanner
        parser.addScanner(new LinkScanner("-l"));

        parseAndAssertNodeCount(1);
        // The node should be an HTMLLinkTag
        assertTrue("Node should be a HTMLLinkTag", node[0] instanceof LinkTag);
        LinkTag linkNode = (LinkTag) node[0];
        assertEquals(
            "Link incorrect",
            "http://www.cj.com/mylink.html",
            linkNode.getLink());
    }

    /**
     * The bug being reproduced is this : <BR>
     * Simple url without index.html, doesent get appended to link
     * This bug was submitted by Roget Kjensrud
     * Creation date: (6/17/2001 4:01:06 PM)
     */
    public void testLinkNodeBug4() throws ParserException
    {
        createParser("<A HREF=\"/mylink.html\">abcd</A>", "http://www.cj.com");
        // Register the image scanner
        parser.addScanner(new LinkScanner("-l"));

        parseAndAssertNodeCount(1);
        // The node should be an HTMLLinkTag
        assertTrue("Node should be a HTMLLinkTag", node[0] instanceof LinkTag);
        LinkTag linkNode = (LinkTag) node[0];
        assertEquals(
            "Link incorrect!!",
            "http://www.cj.com/mylink.html",
            linkNode.getLink());
    }

    public void testLinkNodeBug5() throws ParserException
    {
        createParser(
            "<a href=http://note.kimo.com.tw/>筆記</a>&nbsp; <a \n"
                + "href=http://photo.kimo.com.tw/>相簿</a>&nbsp; <a\n"
                + "href=http://address.kimo.com.tw/>通訊錄</a>&nbsp;&nbsp;",
            "http://www.cj.com");
        Parser.setLineSeparator("\r\n");
        // Register the image scanner
        parser.addScanner(new LinkScanner("-l"));

        parseAndAssertNodeCount(6);
        // The node should be an LinkTag
        assertTrue("Node should be a LinkTag", node[0] instanceof LinkTag);
        LinkTag linkNode = (LinkTag) node[2];
        assertStringEquals(
            "Link incorrect!!",
            "http://photo.kimo.com.tw",
            linkNode.getLink());
        assertEquals(
            "Link beginning",
            new Integer(48),
            new Integer(linkNode.elementBegin()));
        assertEquals(
            "Link ending",
            new Integer(38),
            new Integer(linkNode.elementEnd()));

        LinkTag linkNode2 = (LinkTag) node[4];
        assertStringEquals(
            "Link incorrect!!",
            "http://address.kimo.com.tw",
            linkNode2.getLink());
        assertEquals(
            "Link beginning",
            new Integer(46),
            new Integer(linkNode2.elementBegin()));
        assertEquals(
            "Link ending",
            new Integer(42),
            new Integer(linkNode2.elementEnd()));
    }

    /**
     * This bug occurs when there is a null pointer exception thrown while scanning a tag using LinkScanner.
     * Creation date: (7/1/2001 2:42:13 PM)
     */
    public void testLinkNodeBugNullPointerException() throws ParserException
    {
        createParser(
            "<FORM action=http://search.yahoo.com/bin/search name=f><MAP name=m><AREA\n"
                + "coords=0,0,52,52 href=\"http://www.yahoo.com/r/c1\" shape=RECT><AREA"
                + "coords=53,0,121,52 href=\"http://www.yahoo.com/r/p1\" shape=RECT><AREA"
                + "coords=122,0,191,52 href=\"http://www.yahoo.com/r/m1\" shape=RECT><AREA"
                + "coords=441,0,510,52 href=\"http://www.yahoo.com/r/wn\" shape=RECT>",
            "http://www.cj.com/");
        // Register the image scanner
        parser.addScanner(new LinkScanner("-l"));
        parseAndAssertNodeCount(6);
    }

    /**
     * This bug occurs when there is a null pointer exception thrown while scanning a tag using LinkScanner.
     * Creation date: (7/1/2001 2:42:13 PM)
     */
    public void testLinkNodeMailtoBug() throws ParserException
    {
        createParser(
            "<A HREF='mailto:somik@yahoo.com'>hello</A>",
            "http://www.cj.com/");
        // Register the image scanner
        parser.addScanner(new LinkScanner("-l"));

        parseAndAssertNodeCount(1);
        assertTrue("Node should be a HTMLLinkTag", node[0] instanceof LinkTag);
        LinkTag linkNode = (LinkTag) node[0];
        assertStringEquals(
            "Link incorrect",
            "somik@yahoo.com",
            linkNode.getLink());
        assertEquals(
            "Link Type",
            new Boolean(true),
            new Boolean(linkNode.isMailLink()));
    }

    /**
     * This bug occurs when there is a null pointer exception thrown while scanning a tag using LinkScanner.
     * Creation date: (7/1/2001 2:42:13 PM)
     */
    public void testLinkNodeSingleQuoteBug() throws ParserException
    {
        createParser("<A HREF='abcd.html'>hello</A>", "http://www.cj.com/");

        // Register the image scanner
        parser.addScanner(new LinkScanner("-l"));

        parseAndAssertNodeCount(1);
        assertTrue("Node should be a HTMLLinkTag", node[0] instanceof LinkTag);
        LinkTag linkNode = (LinkTag) node[0];
        assertEquals(
            "Link incorrect",
            "http://www.cj.com/abcd.html",
            linkNode.getLink());
    }

    /**
     * The bug being reproduced is this : <BR>
     * &lt;BODY aLink=#ff0000 bgColor=#ffffff link=#0000cc onload=setfocus() text=#000000 <BR>
     * vLink=#551a8b&gt;
     * The above line is incorrectly parsed in that, the BODY tag is not identified.
     * Creation date: (6/17/2001 4:01:06 PM)
     */
    public void testLinkTag() throws ParserException
    {
        createParser(
            "<A HREF=\"test.html\">abcd</A>",
            "http://www.google.com/test/index.html");
        // Register the image scanner
        parser.addScanner(new LinkScanner("-l"));

        parseAndAssertNodeCount(1);
        // The node should be an HTMLLinkTag
        assertTrue("Node should be a HTMLLinkTag", node[0] instanceof LinkTag);
        LinkTag LinkTag = (LinkTag) node[0];
        assertEquals(
            "The image locn",
            "http://www.google.com/test/test.html",
            LinkTag.getLink());
    }

    /**
     * The bug being reproduced is this : <BR>
     * &lt;BODY aLink=#ff0000 bgColor=#ffffff link=#0000cc onload=setfocus() text=#000000 <BR>
     * vLink=#551a8b&gt;
     * The above line is incorrectly parsed in that, the BODY tag is not identified.
     * Creation date: (6/17/2001 4:01:06 PM)
     */
    public void testLinkTagBug() throws ParserException
    {
        createParser(
            "<A HREF=\"../test.html\">abcd</A>",
            "http://www.google.com/test/index.html");
        // Register the image scanner
        parser.addScanner(new LinkScanner("-l"));

        parseAndAssertNodeCount(1);
        // The node should be an HTMLLinkTag
        assertTrue("Node should be a HTMLLinkTag", node[0] instanceof LinkTag);
        LinkTag LinkTag = (LinkTag) node[0];
        assertEquals(
            "The image locn",
            "http://www.google.com/test.html",
            LinkTag.getLink());
    }

    /**
     * The bug being reproduced is this : <BR>
     * &lt;A HREF=&gt;Something&lt;A&gt;<BR>
     * vLink=#551a8b&gt;
     * The above line is incorrectly parsed in that, the BODY tag is not identified.
     * Creation date: (6/17/2001 4:01:06 PM)
     */
    public void testNullTagBug() throws ParserException
    {
        createParser(
            "<A HREF=>Something</A>",
            "http://www.google.com/test/index.html");
        // Register the image scanner
        parser.addScanner(new LinkScanner("-l"));

        parseAndAssertNodeCount(1);
        // The node should be an HTMLLinkTag
        assertTrue("Node should be a HTMLLinkTag", node[0] instanceof LinkTag);
        LinkTag linkTag = (LinkTag) node[0];
        assertEquals("The link location", "", linkTag.getLink());
        assertEquals("The link text", "Something", linkTag.getLinkText());
    }

    public void testToPlainTextString() throws ParserException
    {
        createParser(
            "<A HREF='mailto:somik@yahoo.com'>hello</A>",
            "http://www.cj.com/");
        // Register the image scanner
        parser.addScanner(new LinkScanner("-l"));

        parseAndAssertNodeCount(1);
        assertTrue("Node should be a HTMLLinkTag", node[0] instanceof LinkTag);
        LinkTag linkTag = (LinkTag) node[0];
        assertEquals("Link Plain Text", "hello", linkTag.toPlainTextString());
    }

    public void testToHTML() throws ParserException
    {
        createParser(
            "<A HREF='mailto:somik@yahoo.com'>hello</A>\n"
                + "<LI><font color=\"FF0000\" size=-1><b>Tech Samachar:</b></font><a \n"
                + "href=\"http://ads.samachar.com/bin/redirect/tech.txt?http://www.samachar.com/tech\n"
                + "nical.html\"> Journalism 3.0</a> by Rajesh Jain",
            "http://www.cj.com/");
        Parser.setLineSeparator("\r\n");
        // Register the image scanner
        parser.addScanner(new LinkScanner("-l"));

        parseAndAssertNodeCount(9);
        assertTrue(
            "First Node should be a HTMLLinkTag",
            node[0] instanceof LinkTag);
        LinkTag linkTag = (LinkTag) node[0];
        assertStringEquals(
            "Link Raw Text",
            "<A HREF=\"mailto:somik@yahoo.com\">hello</A>",
            linkTag.toHtml());
        assertTrue(
            "Eighth Node should be a HTMLLinkTag",
            node[7] instanceof LinkTag);
        linkTag = (LinkTag) node[7];
        assertStringEquals(
            "Link Raw Text",
            "<A HREF=\"http://ads.samachar.com/bin/redirect/tech.txt?http://www.samachar.com/tech\r\nnical.html\"> Journalism 3.0</A>",
            linkTag.toHtml());
    }

    public void testTypeHttps() throws ParserException
    {
        LinkTag linkTag =
            new LinkTag(
                new TagData(0, 0, "", ""),
                new CompositeTagData(null, null, null),
                new LinkData("https://www.someurl.com", "", "", false, false));
        assertTrue("This is a https link", linkTag.isHTTPSLink());
    }

    public void testTypeFtp() throws ParserException
    {
        LinkTag linkTag =
            new LinkTag(
                new TagData(0, 0, "", ""),
                new CompositeTagData(null, null, null),
                new LinkData("ftp://www.someurl.com", "", "", false, false));
        assertTrue("This is an ftp link", linkTag.isFTPLink());
    }

    public void testTypeJavaScript() throws ParserException
    {
        LinkTag linkTag =
            new LinkTag(
                new TagData(0, 0, "", ""),
                new CompositeTagData(null, null, null),
                new LinkData(
                    "javascript://www.someurl.com",
                    "",
                    "",
                    false,
                    true));
        assertTrue("This is a javascript link", linkTag.isJavascriptLink());
    }

    public void testTypeHttpLink() throws ParserException
    {
        LinkTag linkTag =
            new LinkTag(
                new TagData(0, 0, "", ""),
                new CompositeTagData(null, null, null),
                new LinkData("http://www.someurl.com", "", "", false, false));
        assertTrue(
            "This is a http link : " + linkTag.getLink(),
            linkTag.isHTTPLink());
        linkTag =
            new LinkTag(
                new TagData(0, 0, "", ""),
                new CompositeTagData(null, null, null),
                new LinkData("somePage.html", "", "", false, false));
        assertTrue(
            "This relative link is alsp a http link : " + linkTag.getLink(),
            linkTag.isHTTPLink());
        linkTag =
            new LinkTag(
                new TagData(0, 0, "", ""),
                new CompositeTagData(null, null, null),
                new LinkData("ftp://somePage.html", "", "", false, false));
        assertTrue(
            "This is not a http link : " + linkTag.getLink(),
            !linkTag.isHTTPLink());
    }

    public void testTypeHttpLikeLink() throws ParserException
    {
        LinkTag linkTag =
            new LinkTag(
                new TagData(0, 0, "", ""),
                new CompositeTagData(null, null, null),
                new LinkData("http://", "", "", false, false));
        assertTrue("This is a http link", linkTag.isHTTPLikeLink());
        LinkTag linkTag2 =
            new LinkTag(
                new TagData(0, 0, "", ""),
                new CompositeTagData(null, null, null),
                new LinkData("https://www.someurl.com", "", "", false, false));
        assertTrue("This is a https link", linkTag2.isHTTPLikeLink());
    }

    /**
     * Bug #738504 MailLink != HTTPLink
     */
    public void testMailToIsNotAHTTPLink() throws ParserException
    {
        LinkTag link;

        createParser(
            "<A HREF='mailto:derrickoswald@users.sourceforge.net'>Derrick</A>",
            "http://sourceforge.net");
        // Register the link scanner
        parser.addScanner(new LinkScanner("-l"));

        parseAndAssertNodeCount(1);
        assertTrue("Node should be a HTMLLinkTag", node[0] instanceof LinkTag);
        link = (LinkTag) node[0];
        assertTrue("bug #738504 MailLink != HTTPLink", !link.isHTTPLink());
        assertTrue("bug #738504 MailLink != HTTPSLink", !link.isHTTPSLink());
    }
}
