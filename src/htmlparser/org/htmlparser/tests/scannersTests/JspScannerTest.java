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

import org.htmlparser.Parser;
import org.htmlparser.scanners.JspScanner;
import org.htmlparser.tags.JspTag;
import org.htmlparser.tests.ParserTestCase;
import org.htmlparser.util.ParserException;

public class JspScannerTest extends ParserTestCase
{

    public JspScannerTest(String name)
    {
        super(name);
    }

    /**
     * In response to bug report 621117, wherein jsp tags
     * are not recognized if they occur within string nodes.
     */
    public void testScan() throws ParserException
    {
        createParser("<h1>\n" + "This is a <%=object%>\n" + "</h1>");

        // Register the Jsp Scanner
        parser.addScanner(new JspScanner("-j"));
        parseAndAssertNodeCount(4);
        // The first node should be an HTMLJspTag
        assertTrue("Third should be an HTMLJspTag", node[2] instanceof JspTag);
        JspTag tag = (JspTag) node[2];
        assertEquals("tag contents", "=object", tag.getText());
    }

    /**
     * Testcase submitted by Johan Naudts, demonstrating bug
     * 717573, <b>NullPointerException when unclosed HTML tag 
     * inside JSP tag</b>
     * @throws ParserException
     */
    public void testUnclosedTagInsideJsp() throws ParserException
    {
        createParser(
            "<%\n"
                + "public String getHref(String value) \n"
                + "{ \n"
                + "int indexs = value.indexOf(\"<A HREF=\");\n"
                + "int indexe = value.indexOf(\">\");\n"
                + "if (indexs != -1) {\n"
                + "return value.substring(indexs+9,indexe-2);\n"
                + "}\n"
                + "return value;\n"
                + "}\n"
                + "%>\n");
        Parser.setLineSeparator("\r\n");
        // Register the Jsp Scanner
        parser.addScanner(new JspScanner("-j"));
        parseAndAssertNodeCount(1);
    }
}
