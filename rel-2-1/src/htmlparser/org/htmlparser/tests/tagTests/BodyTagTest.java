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
package org.htmlparser.tests.tagTests;

import java.util.Hashtable;
import junit.framework.TestSuite;
import org.htmlparser.Node;

import org.htmlparser.scanners.BodyScanner;
import org.htmlparser.tags.BodyTag;
import org.htmlparser.tests.ParserTestCase;
import org.htmlparser.util.NodeIterator;
import org.htmlparser.util.ParserException;

public class BodyTagTest extends ParserTestCase {
	private BodyTag bodyTag;

	public BodyTagTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		createParser("<html><head><title>body tag test</title></head><body>Yahoo!</body></html>");
		parser.registerScanners();
		parser.addScanner(new BodyScanner("-b"));
		parseAndAssertNodeCount(6);
		assertTrue(node[4] instanceof BodyTag);
		bodyTag = (BodyTag) node[4];
	}

	public void testToPlainTextString() throws ParserException {
		// check the label node
		assertEquals("Body", "Yahoo!", bodyTag.toPlainTextString());
	}

	public void testToHTML() throws ParserException {
		assertStringEquals("Raw String", "<BODY>Yahoo!</BODY>", bodyTag.toHtml());
	}

	public void testToString() throws ParserException {
		assertEquals("Body", "BODY: Yahoo!", bodyTag.toString());
	}

	public void testAttributes() {
		NodeIterator iterator;
		Node node;
		Hashtable attributes;

		try {
			createParser("<body style=\"margin-top:4px; margin-left:20px;\" title=\"body\">");
			parser.addScanner(new BodyScanner("-b"));
			iterator = parser.elements();
			node = null;
			while (iterator.hasMoreNodes()) {
				node = iterator.nextNode();
				if (node instanceof BodyTag) {
					attributes = ((BodyTag) node).getAttributes();
					assertTrue("no style attribute", attributes.containsKey("STYLE"));
					assertTrue("no title attribute", attributes.containsKey("TITLE"));
				} else
					fail("not a body tag");
				assertTrue("more than one node", !iterator.hasMoreNodes());
			}
			assertNotNull("no elements", node);
		} catch (ParserException pe) {
			fail("exception thrown " + pe.getMessage());
		}
	}

	public static TestSuite suite() {
		return new TestSuite(BodyTagTest.class);
	}

	public static void main(String[] args) {
		new junit.awtui.TestRunner().start(new String[] { BodyTagTest.class.getName() });
	}
}
