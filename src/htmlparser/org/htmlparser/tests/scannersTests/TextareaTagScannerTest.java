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

import org.htmlparser.scanners.TextareaTagScanner;
import org.htmlparser.tags.TextareaTag;
import org.htmlparser.tests.ParserTestCase;
import org.htmlparser.util.ParserException;

public class TextareaTagScannerTest extends ParserTestCase
{

    private String testHTML =
        new String(
            "<TEXTAREA name=\"Remarks\">The intervention by the UN proved beneficial</TEXTAREA>"
                + "<TEXTAREA>The capture of the Somali warloard was elusive</TEXTAREA>"
                + "<TEXTAREA></TEXTAREA>"
                + "<TEXTAREA name=\"Remarks\">The death threats of the organization\n"
                + "refused to intimidate the soldiers</TEXTAREA>"
                + "<TEXTAREA name=\"Remarks\">The death threats of the LTTE\n"
                + "refused to intimidate the Tamilians\n</TEXTAREA>");
    private TextareaTagScanner scanner;

    public TextareaTagScannerTest(String name)
    {
        super(name);
    }

    public void testScan() throws ParserException
    {
        scanner = new TextareaTagScanner("-i");
        createParser(testHTML);
        scanner = new TextareaTagScanner("-ta");
        parser.addScanner(scanner);
        parseAndAssertNodeCount(5);
        assertTrue(node[0] instanceof TextareaTag);
        assertTrue(node[1] instanceof TextareaTag);
        assertTrue(node[2] instanceof TextareaTag);
        assertTrue(node[3] instanceof TextareaTag);
        assertTrue(node[4] instanceof TextareaTag);

        // check the Textarea node
        for (int j = 0; j < nodeCount; j++)
        {
            TextareaTag TextareaTag = (TextareaTag) node[j];
            assertEquals(
                "Textarea Scanner",
                scanner,
                TextareaTag.getThisScanner());
        }
    }
}
