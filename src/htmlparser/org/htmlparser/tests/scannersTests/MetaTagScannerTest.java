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

import org.htmlparser.scanners.MetaTagScanner;
import org.htmlparser.tags.EndTag;
import org.htmlparser.tags.MetaTag;
import org.htmlparser.tests.ParserTestCase;
import org.htmlparser.util.ParserException;

public class MetaTagScannerTest extends ParserTestCase
{

    public MetaTagScannerTest(String name)
    {
        super(name);
    }

    public void testScan() throws ParserException
    {
        createParser(
            "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0//EN\">\n"
                + "<html>\n"
                + "<head><title>SpamCop - Welcome to SpamCop\n"
                + "</title>\n"
                + "<META name=\"description\" content=\"Protecting the internet community through technology, not legislation.  SpamCop eliminates spam.  Automatically file spam reports with the network administrators who can stop spam at the source.  Subscribe, and filter your email through powerful statistical analysis before it reaches your inbox.\">\n"
                + "<META name=\"keywords\" content=\"SpamCop spam cop email filter abuse header headers parse parser utility script net net-abuse filter mail program system trace traceroute dns\">\n"
                + "<META name=\"language\" content=\"en\">\n"
                + "<META name=\"owner\" content=\"service@admin.spamcop.net\">\n"
                + "<META HTTP-EQUIV=\"content-type\" CONTENT=\"text/html; charset=ISO-8859-1\">",
            "http://www.google.com/test/index.html");
        MetaTagScanner scanner = new MetaTagScanner("-t");
        parser.addScanner(scanner);

        parseAndAssertNodeCount(11);
        assertTrue("Node 5 should be End Tag", node[5] instanceof EndTag);
        assertTrue("Node 6 should be META Tag", node[6] instanceof MetaTag);
        MetaTag metaTag;
        metaTag = (MetaTag) node[6];
        assertEquals(
            "Meta Tag 6 Name",
            "description",
            metaTag.getMetaTagName());
        assertEquals(
            "Meta Tag 6 Contents",
            "Protecting the internet community through technology, not legislation.  SpamCop eliminates spam.  Automatically file spam reports with the network administrators who can stop spam at the source.  Subscribe, and filter your email through powerful statistical analysis before it reaches your inbox.",
            metaTag.getMetaContent());

        assertTrue("Node 7 should be META Tag", node[7] instanceof MetaTag);
        assertTrue("Node 8 should be META Tag", node[8] instanceof MetaTag);
        assertTrue("Node 9 should be META Tag", node[9] instanceof MetaTag);

        metaTag = (MetaTag) node[7];
        assertEquals("Meta Tag 7 Name", "keywords", metaTag.getMetaTagName());
        assertEquals(
            "Meta Tag 7 Contents",
            "SpamCop spam cop email filter abuse header headers parse parser utility script net net-abuse filter mail program system trace traceroute dns",
            metaTag.getMetaContent());
        assertNull("Meta Tag 7 Http-Equiv", metaTag.getHttpEquiv());

        metaTag = (MetaTag) node[8];
        assertEquals("Meta Tag 8 Name", "language", metaTag.getMetaTagName());
        assertEquals("Meta Tag 8 Contents", "en", metaTag.getMetaContent());
        assertNull("Meta Tag 8 Http-Equiv", metaTag.getHttpEquiv());

        metaTag = (MetaTag) node[9];
        assertEquals("Meta Tag 9 Name", "owner", metaTag.getMetaTagName());
        assertEquals(
            "Meta Tag 9 Contents",
            "service@admin.spamcop.net",
            metaTag.getMetaContent());
        assertNull("Meta Tag 9 Http-Equiv", metaTag.getHttpEquiv());

        metaTag = (MetaTag) node[10];
        assertNull("Meta Tag 10 Name", metaTag.getMetaTagName());
        assertEquals(
            "Meta Tag 10 Contents",
            "text/html; charset=ISO-8859-1",
            metaTag.getMetaContent());
        assertEquals(
            "Meta Tag 10 Http-Equiv",
            "content-type",
            metaTag.getHttpEquiv());

        assertEquals("This Scanner", scanner, metaTag.getThisScanner());
    }

    public void testScanTagsInMeta() throws ParserException
    {
        createParser(
            "<META NAME=\"Description\" CONTENT=\"Ethnoburb </I>versus Chinatown: Two Types of Urban Ethnic Communities in Los Angeles\">",
            "http://www.google.com/test/index.html");
        MetaTagScanner scanner = new MetaTagScanner("-t");
        parser.addScanner(scanner);
        parseAndAssertNodeCount(1);
        assertTrue("Node should be meta tag", node[0] instanceof MetaTag);
        MetaTag metaTag = (MetaTag) node[0];
        assertEquals("Meta Tag Name", "Description", metaTag.getMetaTagName());
        assertEquals(
            "Content",
            "Ethnoburb </I>versus Chinatown: Two Types of Urban Ethnic Communities in Los Angeles",
            metaTag.getMetaContent());
    }

    /**
     * Tried to reproduce bug 707447 but test passes
     * @throws ParserException
     */
    public void testMetaTagBug() throws ParserException
    {
        createParser(
            "<html>"
                + "<head>"
                + "<meta http-equiv=\"content-type\""
                + " content=\"text/html;"
                + " charset=windows-1252\">"
                + "</head>"
                + "</html>");
        parser.registerScanners();
        parseAndAssertNodeCount(5);
        assertType("Meta Tag expected", MetaTag.class, node[2]);
        MetaTag metaTag = (MetaTag) node[2];

        assertStringEquals(
            "http-equiv",
            "content-type",
            metaTag.getHttpEquiv());
        assertStringEquals(
            "content",
            "text/html; charset=windows-1252",
            metaTag.getMetaContent());
    }

    /**
     * Bug report 702547 by Joe Robbins being reproduced.
     * @throws ParserException
     */
    public void testMetaTagWithOpenTagSymbol() throws ParserException
    {
        createParser(
            "<html>"
                + "<head>"
                + "<title>Parser Test 2</title>"
                + "<meta name=\"foo\" content=\"a<b\">"
                + "</head>"
                + "<body>"
                + "<a href=\"http://www.yahoo.com/\">Yahoo!</a><br>"
                + "<a href=\"http://www.excite.com\">Excite</a>"
                + "</body>"
                + "</html>");
        parser.registerScanners();
        parseAndAssertNodeCount(11);
        assertType("meta tag", MetaTag.class, node[3]);
        MetaTag metaTag = (MetaTag) node[3];
        assertStringEquals("meta content", "a<b", metaTag.getMetaContent());
    }
}
