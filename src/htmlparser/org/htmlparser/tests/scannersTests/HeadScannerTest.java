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

import junit.framework.TestSuite;
import org.htmlparser.scanners.*;
import org.htmlparser.tags.*;
import org.htmlparser.tests.ParserTestCase;
import org.htmlparser.util.ParserException;

public class HeadScannerTest extends ParserTestCase {

	public HeadScannerTest(String name) {
		super(name);
	}

	public void testSimpleHead() throws ParserException {
		createParser("<HTML><HEAD></HEAD></HTML>");
		HeadScanner headScanner = new HeadScanner();
		parser.registerDomScanners();
		parseAndAssertNodeCount(1);
		assertTrue(node[0] instanceof Html);
		Html htmlTag = (Html) node[0];
		assertTrue(htmlTag.getChild(0) instanceof HeadTag);
	}

	public void testSimpleHeadWithoutEndTag() throws ParserException {
		createParser("<HTML><HEAD></HTML>");
		HeadScanner headScanner = new HeadScanner();
		parser.registerDomScanners();
		parseAndAssertNodeCount(1);
		assertTrue(node[0] instanceof Html);
		Html htmlTag = (Html) node[0];
		assertTrue(htmlTag.getChild(0) instanceof HeadTag);
		HeadTag headTag = (HeadTag) htmlTag.getChild(0);
		assertEquals("toHtml()", "<HEAD></HEAD>", headTag.toHtml());
		assertEquals("toHtml()", "<HTML><HEAD></HEAD></HTML>", htmlTag.toHtml());
	}

	public void testSimpleHeadWithBody() throws ParserException {
		createParser("<HTML><HEAD><BODY></HTML>");
		HeadScanner headScanner = new HeadScanner();
		parser.registerDomScanners();
		parseAndAssertNodeCount(1);
		assertTrue(node[0] instanceof Html);
		Html htmlTag = (Html) node[0];
		assertTrue(htmlTag.getChild(0) instanceof HeadTag);
		// assertTrue(htmlTag.getChild(1) instanceof BodyTag);
		HeadTag headTag = (HeadTag) htmlTag.getChild(0);
		assertEquals("toHtml()", "<HEAD></HEAD>", headTag.toHtml());
		assertEquals("toHtml()", "<HTML><HEAD></HEAD><BODY></BODY></HTML>", htmlTag.toHtml());
	}

	public static TestSuite suite() {
		return new TestSuite(HeadScannerTest.class);
	}

	public static void main(String[] args) {
		new junit.awtui.TestRunner().start(new String[] { HeadScannerTest.class.getName() });
	}

}
