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

import org.htmlparser.scanners.ImageScanner;
import org.htmlparser.tags.ImageTag;
import org.htmlparser.tests.ParserTestCase;
import org.htmlparser.util.LinkProcessor;
import org.htmlparser.util.ParserException;

public class ImageTagTest extends ParserTestCase
{
    public ImageTagTest(String name)
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
    public void testImageTag() throws ParserException
    {
        createParser(
            "<IMG alt=Google height=115 src=\"goo/title_homepage4.gif\" width=305>",
            "http://www.google.com/test/index.html");
        // Register the image scanner
        parser.addScanner(new ImageScanner("-i", new LinkProcessor()));

        parseAndAssertNodeCount(1);
        // The node should be an HTMLImageTag
        assertTrue(
            "Node should be a HTMLImageTag",
            node[0] instanceof ImageTag);
        ImageTag imageTag = (ImageTag) node[0];
        assertEquals(
            "The image locn",
            "http://www.google.com/test/goo/title_homepage4.gif",
            imageTag.getImageURL());
    }

    /**
     * The bug being reproduced is this : <BR>
     * &lt;BODY aLink=#ff0000 bgColor=#ffffff link=#0000cc onload=setfocus() text=#000000 <BR>
     * vLink=#551a8b&gt;
     * The above line is incorrectly parsed in that, the BODY tag is not identified.
     * Creation date: (6/17/2001 4:01:06 PM)
     */
    public void testImageTagBug() throws ParserException
    {
        createParser(
            "<IMG alt=Google height=115 src=\"../goo/title_homepage4.gif\" width=305>",
            "http://www.google.com/test/");
        // Register the image scanner
        parser.addScanner(new ImageScanner("-i", new LinkProcessor()));

        parseAndAssertNodeCount(1);
        // The node should be an HTMLImageTag
        assertTrue(
            "Node should be a HTMLImageTag",
            node[0] instanceof ImageTag);
        ImageTag imageTag = (ImageTag) node[0];
        assertEquals(
            "The image locn",
            "http://www.google.com/goo/title_homepage4.gif",
            imageTag.getImageURL());
    }

    /**
     * The bug being reproduced is this : <BR>
     * &lt;BODY aLink=#ff0000 bgColor=#ffffff link=#0000cc onload=setfocus() text=#000000 <BR>
     * vLink=#551a8b&gt;
     * The above line is incorrectly parsed in that, the BODY tag is not identified.
     * Creation date: (6/17/2001 4:01:06 PM)
     */
    public void testImageTageBug2() throws ParserException
    {
        createParser(
            "<IMG alt=Google height=115 src=\"../../goo/title_homepage4.gif\" width=305>",
            "http://www.google.com/test/test/index.html");
        // Register the image scanner
        parser.addScanner(new ImageScanner("-i", new LinkProcessor()));

        parseAndAssertNodeCount(1);
        // The node should be an HTMLImageTag
        assertTrue(
            "Node should be a HTMLImageTag",
            node[0] instanceof ImageTag);
        ImageTag imageTag = (ImageTag) node[0];
        assertEquals(
            "The image locn",
            "http://www.google.com/goo/title_homepage4.gif",
            imageTag.getImageURL());
    }

    /**
     * This bug occurs when there is a null pointer exception thrown while scanning a tag using LinkScanner.
     * Creation date: (7/1/2001 2:42:13 PM)
     */
    public void testImageTagSingleQuoteBug() throws ParserException
    {
        createParser("<IMG SRC='abcd.jpg'>", "http://www.cj.com/");
        // Register the image scanner
        parser.addScanner(new ImageScanner("-i", new LinkProcessor()));

        parseAndAssertNodeCount(1);
        assertTrue(
            "Node should be a HTMLImageTag",
            node[0] instanceof ImageTag);
        ImageTag imageTag = (ImageTag) node[0];
        assertEquals(
            "Image incorrect",
            "http://www.cj.com/abcd.jpg",
            imageTag.getImageURL());
    }

    /**
     * The bug being reproduced is this : <BR>
     * &lt;A HREF=&gt;Something&lt;A&gt;<BR>
     * vLink=#551a8b&gt;
     * The above line is incorrectly parsed in that, the BODY tag is not identified.
     * Creation date: (6/17/2001 4:01:06 PM)
     */
    public void testNullImageBug() throws ParserException
    {
        createParser("<IMG SRC=>", "http://www.google.com/test/index.html");
        // Register the image scanner
        parser.addScanner(new ImageScanner("-i", new LinkProcessor()));

        parseAndAssertNodeCount(1);
        // The node should be an HTMLLinkTag
        assertTrue(
            "Node should be a HTMLImageTag",
            node[0] instanceof ImageTag);
        ImageTag imageTag = (ImageTag) node[0];
        assertStringEquals("The image location", "", imageTag.getImageURL());
    }

    public void testToHTML() throws ParserException
    {
        createParser(
            "<IMG alt=Google height=115 src=\"../../goo/title_homepage4.gif\" width=305>",
            "http://www.google.com/test/test/index.html");
        // Register the image scanner
        parser.addScanner(new ImageScanner("-i", new LinkProcessor()));

        parseAndAssertNodeCount(1);
        // The node should be an HTMLImageTag
        assertTrue(
            "Node should be a HTMLImageTag",
            node[0] instanceof ImageTag);
        ImageTag imageTag = (ImageTag) node[0];
        assertStringEquals(
            "The image locn",
            "<IMG WIDTH=\"305\" ALT=\"Google\" SRC=\"../../goo/title_homepage4.gif\" HEIGHT=\"115\">",
            imageTag.toHtml());
        assertEquals("Alt", "Google", imageTag.getAttribute("alt"));
        assertEquals("Height", "115", imageTag.getAttribute("height"));
        assertEquals("Width", "305", imageTag.getAttribute("width"));
    }
}
