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

package org.htmlparser.tests.visitorsTests;

import org.htmlparser.Node;
import org.htmlparser.StringNode;
import org.htmlparser.tags.TableColumn;
import org.htmlparser.tags.TableRow;
import org.htmlparser.tags.TableTag;
import org.htmlparser.tests.ParserTestCase;
import org.htmlparser.util.NodeList;
import org.htmlparser.visitors.HtmlPage;

public class HtmlPageTest extends ParserTestCase
{

    private static final String SIMPLE_PAGE =
        "<html>"
            + "<head>"
            + "<title>Welcome to the HTMLParser website</title>"
            + "</head>"
            + "<body>"
            + "Welcome to HTMLParser"
            + "</body>"
            + "</html>";

    private static final String PAGE_WITH_TABLE =
        "<html>"
            + "<head>"
            + "<title>Welcome to the HTMLParser website</title>"
            + "</head>"
            + "<body>"
            + "Welcome to HTMLParser"
            + "<table>"
            + "<tr>"
            + "<td>cell 1</td>"
            + "<td>cell 2</td>"
            + "</tr>"
            + "</table>"
            + "</body>"
            + "</html>";

    public HtmlPageTest(String name)
    {
        super(name);
    }

    public void testCreateSimplePage() throws Exception
    {
        createParser(SIMPLE_PAGE);
        HtmlPage page = new HtmlPage(parser);
        parser.visitAllNodesWith(page);
        assertStringEquals(
            "title",
            "Welcome to the HTMLParser website",
            page.getTitle());
        NodeList bodyNodes = page.getBody();
        assertEquals("number of nodes in body", 1, bodyNodes.size());
        Node node = bodyNodes.elementAt(0);
        assertTrue(
            "expected stringNode but was " + node.getClass().getName(),
            node instanceof StringNode);
        assertStringEquals(
            "body contents",
            "Welcome to HTMLParser",
            page.getBody().asString());
    }

    public void testCreatePageWithTables() throws Exception
    {
        createParser(PAGE_WITH_TABLE);
        HtmlPage page = new HtmlPage(parser);
        parser.visitAllNodesWith(page);
        NodeList bodyNodes = page.getBody();
        assertEquals("number of nodes in body", 2, bodyNodes.size());
        assertXmlEquals(
            "body html",
            "Welcome to HTMLParser"
                + "<table>"
                + "<tr>"
                + "	<td>cell 1</td>"
                + "	<td>cell 2</td>"
                + "</tr>"
                + "</table>",
            bodyNodes.asHtml());
        TableTag tables[] = page.getTables();
        assertEquals("number of tables", 1, tables.length);
        assertEquals("number of rows", 1, tables[0].getRowCount());
        TableRow row = tables[0].getRow(0);
        assertEquals("number of columns", 2, row.getColumnCount());
        TableColumn[] col = row.getColumns();
        assertEquals("column contents", "cell 1", col[0].toPlainTextString());
        assertEquals("column contents", "cell 2", col[1].toPlainTextString());
    }
}
