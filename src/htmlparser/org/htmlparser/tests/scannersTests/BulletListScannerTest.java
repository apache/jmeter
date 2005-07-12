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
import org.htmlparser.StringNode;
import org.htmlparser.tags.Bullet;
import org.htmlparser.tags.BulletList;
import org.htmlparser.tags.CompositeTag;
import org.htmlparser.tests.ParserTestCase;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

/**
 * @author Somik Raha
 * 
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class BulletListScannerTest extends ParserTestCase {

	public BulletListScannerTest(String name) {
		super(name);
	}

	public void testScan() throws ParserException {
		createParser("<ul TYPE=DISC>" + "<ul TYPE=\"DISC\"><li>Energy supply\n"
				+ " (Campbell)  <A HREF=\"/hansard/37th3rd/h20307p.htm#1646\">1646</A>\n"
				+ " (MacPhail)  <A HREF=\"/hansard/37th3rd/h20307p.htm#1646\">1646</A>\n"
				+ "</ul><A NAME=\"calpinecorp\"></A><B>Calpine Corp.</B>\n"
				+ "<ul TYPE=\"DISC\"><li>Power plant projects\n"
				+ " (Neufeld)  <A HREF=\"/hansard/37th3rd/h20314p.htm#1985\">1985</A>\n" + "</ul>" + "</ul>");
		parser.registerScanners();
		parseAndAssertNodeCount(1);

		NodeList nestedBulletLists = ((CompositeTag) node[0]).searchFor(BulletList.class);
		assertEquals("bullets in first list", 2, nestedBulletLists.size());
		BulletList firstList = (BulletList) nestedBulletLists.elementAt(0);
		Bullet firstBullet = (Bullet) firstList.childAt(0);
		Node firstNodeInFirstBullet = firstBullet.childAt(0);
		assertType("first child in bullet", StringNode.class, firstNodeInFirstBullet);
		assertStringEquals("expected text", "Energy supply\r\n" + " (Campbell)  ", firstNodeInFirstBullet
				.toPlainTextString());
	}
}
