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

import org.htmlparser.scanners.OptionTagScanner;
import org.htmlparser.tags.OptionTag;
import org.htmlparser.tests.ParserTestCase;
import org.htmlparser.util.ParserException;

public class OptionTagTest extends ParserTestCase
{
    private String testHTML =
        new String(
            "<OPTION value=\"Google Search\">Google</OPTION>"
            + "<OPTION value=\"AltaVista Search\">AltaVista"
            + "<OPTION value=\"Lycos Search\"></OPTION>"
            + "<OPTION>Yahoo!</OPTION>"
            + "<OPTION>\nHotmail</OPTION>"
            + "<OPTION value=\"ICQ Messenger\">"
            + "<OPTION>Mailcity\n</OPTION>"
            + "<OPTION>\nIndiatimes\n</OPTION>"
            + "<OPTION>\nRediff\n</OPTION>\n"
            + "<OPTION>Cricinfo"
            + "<OPTION value=\"Microsoft Passport\">"
        //									"<OPTION value=\"AOL\"><SPAN>AOL</SPAN></OPTION>" +
        //									"<OPTION value=\"Time Warner\">Time <LABEL>Warner <SPAN>AOL </SPAN>Inc.</LABEL>"
    );

    public OptionTagTest(String name)
    {
        super(name);
    }

    protected void setUp() throws Exception
    {
        super.setUp();
        createParser(testHTML);
        parser.addScanner(new OptionTagScanner("-option"));
        parseAndAssertNodeCount(11);
    }

    public void testToHTML() throws ParserException
    {
        for (int j = 0; j < nodeCount; j++)
        {
            //assertTrue("Node " + j + " should be Option Tag",node[j] instanceof OptionTag);
            System.out.println(node[j].getClass().getName());
            System.out.println(node[j].toHtml());
        }
        OptionTag OptionTag;
        OptionTag = (OptionTag) node[0];
        assertStringEquals(
            "HTML String",
            "<OPTION VALUE=\"Google Search\">Google</OPTION>",
            OptionTag.toHtml());
        OptionTag = (OptionTag) node[1];
        assertStringEquals(
            "HTML String",
            "<OPTION VALUE=\"AltaVista Search\">AltaVista</OPTION>",
            OptionTag.toHtml());
        OptionTag = (OptionTag) node[2];
        assertStringEquals(
            "HTML String",
            "<OPTION VALUE=\"Lycos Search\"></OPTION>",
            OptionTag.toHtml());
        OptionTag = (OptionTag) node[3];
        assertStringEquals(
            "HTML String",
            "<OPTION>Yahoo!</OPTION>",
            OptionTag.toHtml());
        OptionTag = (OptionTag) node[4];
        assertStringEquals(
            "HTML String",
            "<OPTION>\r\nHotmail</OPTION>",
            OptionTag.toHtml());
        OptionTag = (OptionTag) node[5];
        assertStringEquals(
            "HTML String",
            "<OPTION VALUE=\"ICQ Messenger\"></OPTION>",
            OptionTag.toHtml());
        OptionTag = (OptionTag) node[6];
        assertStringEquals(
            "HTML String",
            "<OPTION>Mailcity\r\n</OPTION>",
            OptionTag.toHtml());
        OptionTag = (OptionTag) node[7];
        assertStringEquals(
            "HTML String",
            "<OPTION>\r\nIndiatimes\r\n</OPTION>",
            OptionTag.toHtml());
        OptionTag = (OptionTag) node[8];
        assertStringEquals(
            "HTML String",
            "<OPTION>\r\nRediff\r\n</OPTION>",
            OptionTag.toHtml());
        OptionTag = (OptionTag) node[9];
        assertStringEquals(
            "HTML String",
            "<OPTION>Cricinfo</OPTION>",
            OptionTag.toHtml());
        OptionTag = (OptionTag) node[10];
        assertStringEquals(
            "HTML String",
            "<OPTION VALUE=\"Microsoft Passport\"></OPTION>",
            OptionTag.toHtml());
        /*		OptionTag = (OptionTag) node[11];
        		assertStringEquals("HTML String","<OPTION VALUE=\"AOL\"><SPAN>AOL</SPAN></OPTION>",OptionTag.toHtml());
        		OptionTag = (OptionTag) node[12];
        		assertStringEquals("HTML String","<OPTION value=\"Time Warner\">Time <LABEL>Warner <SPAN>AOL </SPAN>Inc.</LABEL></OPTION>",OptionTag.toHtml());
        */
    }

    public void testToString() throws ParserException
    {
        for (int j = 0; j < 11; j++)
        {
            assertTrue(
                "Node " + j + " should be Option Tag",
                node[j] instanceof OptionTag);
        }
        OptionTag OptionTag;
        OptionTag = (OptionTag) node[0];
        assertEquals(
            "HTML Raw String",
            "OPTION VALUE: Google Search TEXT: Google\n",
            OptionTag.toString());
        OptionTag = (OptionTag) node[1];
        assertEquals(
            "HTML Raw String",
            "OPTION VALUE: AltaVista Search TEXT: AltaVista\n",
            OptionTag.toString());
        OptionTag = (OptionTag) node[2];
        assertEquals(
            "HTML Raw String",
            "OPTION VALUE: Lycos Search TEXT: \n",
            OptionTag.toString());
        OptionTag = (OptionTag) node[3];
        assertEquals(
            "HTML Raw String",
            "OPTION VALUE: null TEXT: Yahoo!\n",
            OptionTag.toString());
        OptionTag = (OptionTag) node[4];
        assertEquals(
            "HTML Raw String",
            "OPTION VALUE: null TEXT: Hotmail\n",
            OptionTag.toString());
        OptionTag = (OptionTag) node[5];
        assertEquals(
            "HTML Raw String",
            "OPTION VALUE: ICQ Messenger TEXT: \n",
            OptionTag.toString());
        OptionTag = (OptionTag) node[6];
        assertEquals(
            "HTML Raw String",
            "OPTION VALUE: null TEXT: Mailcity\r\n\n",
            OptionTag.toString());
        OptionTag = (OptionTag) node[7];
        assertEquals(
            "HTML Raw String",
            "OPTION VALUE: null TEXT: Indiatimes\r\n\n",
            OptionTag.toString());
        OptionTag = (OptionTag) node[8];
        assertEquals(
            "HTML Raw String",
            "OPTION VALUE: null TEXT: Rediff\r\n\n",
            OptionTag.toString());
        OptionTag = (OptionTag) node[9];
        assertEquals(
            "HTML Raw String",
            "OPTION VALUE: null TEXT: Cricinfo\n",
            OptionTag.toString());
        OptionTag = (OptionTag) node[10];
        assertEquals(
            "HTML Raw String",
            "OPTION VALUE: Microsoft Passport TEXT: \n",
            OptionTag.toString());
        /*		OptionTag = (OptionTag) node[11];
        		assertEquals("HTML Raw String","OPTION VALUE: AOL TEXT: AOL\n",OptionTag.toString());
        		OptionTag = (OptionTag) node[12];
        		assertEquals("HTML Raw String","OPTION VALUE: Time Warner TEXT: Time Warner AOL Inc.\n",OptionTag.toString());
        */
    }

    public static void main(String[] args)
    {
        new junit.awtui.TestRunner().start(
            new String[] { OptionTagTest.class.getName()});
    }

}
