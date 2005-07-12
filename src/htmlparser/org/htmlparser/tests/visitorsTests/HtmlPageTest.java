// $Header$
/*
 * ====================================================================
 * Copyright 2002-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

public class HtmlPageTest extends ParserTestCase {

	private static final String SIMPLE_PAGE = "<html>" + "<head>" + "<title>Welcome to the HTMLParser website</title>"
			+ "</head>" + "<body>" + "Welcome to HTMLParser" + "</body>" + "</html>";

	private static final String PAGE_WITH_TABLE = "<html>" + "<head>"
			+ "<title>Welcome to the HTMLParser website</title>" + "</head>" + "<body>" + "Welcome to HTMLParser"
			+ "<table>" + "<tr>" + "<td>cell 1</td>" + "<td>cell 2</td>" + "</tr>" + "</table>" + "</body>" + "</html>";

	public HtmlPageTest(String name) {
		super(name);
	}

	public void testCreateSimplePage() throws Exception {
		createParser(SIMPLE_PAGE);
		HtmlPage page = new HtmlPage(parser);
		parser.visitAllNodesWith(page);
		assertStringEquals("title", "Welcome to the HTMLParser website", page.getTitle());
		NodeList bodyNodes = page.getBody();
		assertEquals("number of nodes in body", 1, bodyNodes.size());
		Node node = bodyNodes.elementAt(0);
		assertTrue("expected stringNode but was " + node.getClass().getName(), node instanceof StringNode);
		assertStringEquals("body contents", "Welcome to HTMLParser", page.getBody().asString());
	}

	public void testCreatePageWithTables() throws Exception {
		createParser(PAGE_WITH_TABLE);
		HtmlPage page = new HtmlPage(parser);
		parser.visitAllNodesWith(page);
		NodeList bodyNodes = page.getBody();
		assertEquals("number of nodes in body", 2, bodyNodes.size());
		assertXmlEquals("body html", "Welcome to HTMLParser" + "<table>" + "<tr>" + "	<td>cell 1</td>"
				+ "	<td>cell 2</td>" + "</tr>" + "</table>", bodyNodes.asHtml());
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
