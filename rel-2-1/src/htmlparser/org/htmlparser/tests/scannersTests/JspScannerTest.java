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
package org.htmlparser.tests.scannersTests;

import org.htmlparser.Parser;
import org.htmlparser.scanners.JspScanner;
import org.htmlparser.tags.JspTag;
import org.htmlparser.tests.ParserTestCase;
import org.htmlparser.util.ParserException;

public class JspScannerTest extends ParserTestCase {

	public JspScannerTest(String name) {
		super(name);
	}

	/**
	 * In response to bug report 621117, wherein jsp tags are not recognized if
	 * they occur within string nodes.
	 */
	public void testScan() throws ParserException {
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
	 * Testcase submitted by Johan Naudts, demonstrating bug 717573,
	 * <b>NullPointerException when unclosed HTML tag inside JSP tag</b>
	 * 
	 * @throws ParserException
	 */
	public void testUnclosedTagInsideJsp() throws ParserException {
		createParser("<%\n" + "public String getHref(String value) \n" + "{ \n"
				+ "int indexs = value.indexOf(\"<A HREF=\");\n" + "int indexe = value.indexOf(\">\");\n"
				+ "if (indexs != -1) {\n" + "return value.substring(indexs+9,indexe-2);\n" + "}\n" + "return value;\n"
				+ "}\n" + "%>\n");
		Parser.setLineSeparator("\r\n");
		// Register the Jsp Scanner
		parser.addScanner(new JspScanner("-j"));
		parseAndAssertNodeCount(1);
	}
}
