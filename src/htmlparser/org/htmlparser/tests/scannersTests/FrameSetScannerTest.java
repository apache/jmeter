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

import org.htmlparser.scanners.FrameScanner;
import org.htmlparser.scanners.FrameSetScanner;
import org.htmlparser.tags.FrameSetTag;
import org.htmlparser.tags.FrameTag;
import org.htmlparser.tests.ParserTestCase;
import org.htmlparser.util.ParserException;

public class FrameSetScannerTest extends ParserTestCase
{

    public FrameSetScannerTest(String name)
    {
        super(name);
    }

    public void testEvaluate()
    {
        String line1 =
            "frameset rows=\"115,*\" frameborder=\"NO\" border=\"0\" framespacing=\"0\"";
        String line2 =
            "FRAMESET rows=\"115,*\" frameborder=\"NO\" border=\"0\" framespacing=\"0\"";
        String line3 =
            "Frameset rows=\"115,*\" frameborder=\"NO\" border=\"0\" framespacing=\"0\"";
        FrameSetScanner frameSetScanner = new FrameSetScanner("");
        assertTrue("Line 1", frameSetScanner.evaluate(line1, null));
        assertTrue("Line 2", frameSetScanner.evaluate(line2, null));
        assertTrue("Line 3", frameSetScanner.evaluate(line3, null));
    }

    public void testScan() throws ParserException
    {
        createParser(
            "<frameset rows=\"115,*\" frameborder=\"NO\" border=\"0\" framespacing=\"0\">\n"
                + "<frame name=\"topFrame\" noresize src=\"demo_bc_top.html\" scrolling=\"NO\" frameborder=\"NO\">\n"
                + "<frame name=\"mainFrame\" src=\"http://www.kizna.com/web_e/\" scrolling=\"AUTO\">\n"
                + "</frameset>",
            "http://www.google.com/test/index.html");

        parser.addScanner(new FrameSetScanner(""));
        parser.addScanner(new FrameScanner());

        parseAndAssertNodeCount(1);
        assertTrue("Node 0 should be End Tag", node[0] instanceof FrameSetTag);
        FrameSetTag frameSetTag = (FrameSetTag) node[0];
        // Find the details of the frameset itself
        assertEquals("Rows", "115,*", frameSetTag.getAttribute("rows"));
        assertEquals(
            "FrameBorder",
            "NO",
            frameSetTag.getAttribute("FrameBorder"));
        assertEquals(
            "FrameSpacing",
            "0",
            frameSetTag.getAttribute("FrameSpacing"));
        assertEquals("Border", "0", frameSetTag.getAttribute("Border"));
        // Now check the frames
        FrameTag topFrame = frameSetTag.getFrame("topFrame");
        FrameTag mainFrame = frameSetTag.getFrame("mainFrame");
        assertNotNull("Top Frame should not be null", topFrame);
        assertNotNull("Main Frame should not be null", mainFrame);
        assertEquals("Top Frame Name", "topFrame", topFrame.getFrameName());
        assertEquals(
            "Top Frame Location",
            "http://www.google.com/test/demo_bc_top.html",
            topFrame.getFrameLocation());
        assertEquals("Main Frame Name", "mainFrame", mainFrame.getFrameName());
        assertEquals(
            "Main Frame Location",
            "http://www.kizna.com/web_e/",
            mainFrame.getFrameLocation());
        assertEquals(
            "Scrolling in Main Frame",
            "AUTO",
            mainFrame.getAttribute("Scrolling"));
    }
}
