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

import org.htmlparser.Node;
import org.htmlparser.tags.Bullet;
import org.htmlparser.tests.ParserTestCase;
import org.htmlparser.util.NodeIterator;
import org.htmlparser.util.ParserException;

public class BulletScannerTest extends ParserTestCase {

	public BulletScannerTest(String name) {
		super(name);
	}

	public void testBulletFound() throws Exception {
		createParser("<LI><A HREF=\"collapseHierarchy.html\">Collapse Hierarchy</A>\n" + "</LI>");
		parser.registerScanners();
		parseAndAssertNodeCount(1);
		assertType("should be a bullet", Bullet.class, node[0]);
	}

	public void testOutOfMemoryBug() throws ParserException {
		createParser("<html>" + "<head>" + "<title>Foo</title>" + "</head>" + "<body>" + "    <ul>" + "        <li>"
				+ "            <a href=\"http://foo.com/c.html\">bibliographies on:" + "                <ul>"
				+ "                    <li>chironomidae</li>" + "                </ul>" + "            </a>"
				+ "        </li>" + "    </ul>" + "" + "</body>" + "</html>");
		parser.registerScanners();
		for (NodeIterator i = parser.elements(); i.hasMoreNodes();) {
			Node node = i.nextNode();
			System.out.println(node.toHtml());
		}
	}

	public void testNonEndedBullets() throws ParserException {
		createParser("<li>forest practices legislation penalties for non-compliance\n"
				+ " (Kwan)  <A HREF=\"/hansard/37th3rd/h21107a.htm#4384\">4384-5</A>\n"
				+ "<li>passenger rail service\n"
				+ " (MacPhail)  <A HREF=\"/hansard/37th3rd/h21021p.htm#3904\">3904</A>\n"
				+ "<li>referendum on principles for treaty negotiations\n"
				+ " (MacPhail)  <A HREF=\"/hansard/37th3rd/h20313p.htm#1894\">1894</A>\n"
				+ "<li>transportation infrastructure projects\n"
				+ " (MacPhail)  <A HREF=\"/hansard/37th3rd/h21022a.htm#3945\">3945-7</A>\n" + "<li>tuition fee freeze");
		parser.registerScanners();
		parseAndAssertNodeCount(5);
		for (int i = 0; i < nodeCount; i++) {
			assertType("node " + i, Bullet.class, node[i]);
		}
	}
}
