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
import org.htmlparser.scanners.StyleScanner;
import org.htmlparser.tags.StyleTag;
import org.htmlparser.tests.ParserTestCase;
import org.htmlparser.util.ParserException;

public class StyleScannerTest extends ParserTestCase
{

    public StyleScannerTest(String name)
    {
        super(name);
    }

    public void testEvaluate()
    {
        StyleScanner scanner = new StyleScanner("-s");
        boolean retVal = scanner.evaluate("style ", null);
        assertEquals(
            "Evaluation of STYLE tag",
            new Boolean(true),
            new Boolean(retVal));
    }

    public void testScan()
    {
        createParser(
            "<STYLE TYPE=\"text/css\"><!--\n\n" + "</STYLE>",
            "http://www.yle.fi/");
        parser.addScanner(new StyleScanner("-s"));
        try
        {
            parseAndAssertNodeCount(1);
            assertTrue("Should've thrown exception", false);
        }
        catch (ParserException e)
        {

        }
    }

    public void testScanBug() throws ParserException
    {
        createParser(
            "<html><head><title>Yahoo!</title><base href=http://www.yahoo.com/ target=_top><meta http-equiv=\"PICS-Label\" content='(PICS-1.1 \"http://www.icra.org/ratingsv02.html\" l r (cz 1 lz 1 nz 1 oz 1 vz 1) gen true for \"http://www.yahoo.com\" r (cz 1 lz 1 nz 1 oz 1 vz 1) \"http://www.rsac.org/ratingsv01.html\" l r (n 0 s 0 v 0 l 0) gen true for \"http://www.yahoo.com\" r (n 0 s 0 v 0 l 0))'><style>a.h{background-color:#ffee99}</style></head>",
            "http://www.google.com/test/index.html");
        parser.registerScanners();
        parseAndAssertNodeCount(7);
        assertTrue(
            "Second last node should be a style tag",
            node[5] instanceof StyleTag);
        StyleTag styleTag = (StyleTag) node[5];
        assertEquals(
            "Style Code",
            "a.h{background-color:#ffee99}",
            styleTag.getStyleCode());
    }

    /**
     * This is a bug reported by Kaarle Kaaila. 
     */
    public void testScanBug2() throws ParserException
    {
        createParser(
            "<STYLE TYPE=\"text/css\"><!--\n\n"
                + "input{font-family: arial, helvetica, sans-serif; font-size:11px;}\n\n"
                + "i {font-family: times; font-size:10pt; font-weight:normal;}\n\n"
                + ".ruuhka {font-family: arial, helvetica, sans-serif; font-size:11px;}\n\n"
                + ".paalinkit {font-family: arial, helvetica, sans-serif; font-size:12px;}\n\n"
                + ".shortselect{font-family: arial, helvetica, sans-serif; font-size:12px; width:130;}\n\n"
                + ".cityselect{font-family: arial, helvetica, sans-serif; font-size:11px; width:100;}\n\n"
                + ".longselect{font-family: arial, helvetica, sans-serif; font-size:12px;}\n\n"
                + "---></STYLE>",
            "http://www.yle.fi/");
        parser.addScanner(new StyleScanner("-s"));
        parseAndAssertNodeCount(1);
        assertTrue(node[0] instanceof StyleTag);
    }

    /**
     * This is a bug reported by Dr. Wes Munsil, with the parser crashing on Google
     */
    public void testScanBug3() throws ParserException
    {
        createParser(
            "<html><head><META HTTP-EQUIV=\"content-type\" CONTENT=\"text/html; charset=ISO-8859-1\"><title>Google</title><style><!--\n"
                + "body,td,a,p,.h{font-family:arial,sans-serif;} .h{font-size: 20px;} .h{color:} .q{text-decoration:none; color:#0000cc;}\n"
                + "//--></style>",
            "http://www.yle.fi/");
        parser.registerScanners();
        parseAndAssertNodeCount(5);
        assertTrue(node[4] instanceof StyleTag);
        StyleTag styleTag = (StyleTag) node[4];
        String expectedCode =
            "<!--\r\n"
                + "body,td,a,p,.h{font-family:arial,sans-serif;} .h{font-size: 20px;} .h{color:} .q{text-decoration:none; color:#0000cc;}\r\n"
                + "//-->";
        assertStringEquals(
            "Expected Style Code",
            expectedCode,
            styleTag.getStyleCode());
    }

}
