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

import org.htmlparser.scanners.InputTagScanner;
import org.htmlparser.tags.InputTag;
import org.htmlparser.tests.ParserTestCase;
import org.htmlparser.util.ParserException;

public class InputTagTest extends ParserTestCase {
	private String testHTML = new String("<INPUT type=\"text\" name=\"Google\">");

	public InputTagTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		createParser(testHTML, "http://www.google.com/test/index.html");
		parser.addScanner(new InputTagScanner("-i"));
	}

	public void testToHTML() throws ParserException {
		parseAndAssertNodeCount(1);
		assertTrue("Node 1 should be INPUT Tag", node[0] instanceof InputTag);
		InputTag InputTag;
		InputTag = (InputTag) node[0];
		assertEquals("HTML String", "<INPUT NAME=\"Google\" TYPE=\"text\">", InputTag.toHtml());
	}

	public void testToString() throws ParserException {
		parseAndAssertNodeCount(1);
		assertTrue("Node 1 should be INPUT Tag", node[0] instanceof InputTag);
		InputTag InputTag;
		InputTag = (InputTag) node[0];
		assertEquals("HTML Raw String", "INPUT TAG\n--------\nNAME : Google\nTYPE : text\n", InputTag.toString());
	}

	/**
	 * Reproduction of bug report 663038
	 * 
	 * @throws ParserException
	 */
	public void testToHTML2() throws ParserException {
		String testHTML = new String("<INPUT type=\"checkbox\" " + "name=\"cbCheck\" checked>");
		createParser(testHTML);
		parser.addScanner(new InputTagScanner("-i"));

		parseAndAssertNodeCount(1);
		assertTrue("Node 1 should be INPUT Tag", node[0] instanceof InputTag);
		InputTag InputTag;
		InputTag = (InputTag) node[0];
		assertStringEquals("HTML String", "<INPUT CHECKED=\"\" NAME=\"cbCheck\" TYPE=\"checkbox\">", InputTag.toHtml());
	}

}