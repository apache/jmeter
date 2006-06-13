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
import org.htmlparser.NodeReader;
import org.htmlparser.Parser;
import org.htmlparser.scanners.TagScanner;
import org.htmlparser.tags.Tag;
import org.htmlparser.tests.ParserTestCase;
import org.htmlparser.util.NodeIterator;
import org.htmlparser.util.ParserException;
import org.htmlparser.util.ParserUtils;

public class TagScannerTest extends ParserTestCase {

	public TagScannerTest(String name) {
		super(name);
	}

	public void testAbsorbLeadingBlanks() {
		String test = "   This is a test";
		String result = TagScanner.absorbLeadingBlanks(test);
		assertEquals("Absorb test", "This is a test", result);
	}

	public void testExtractXMLData() throws ParserException {
		createParser("<MESSAGE>\n" + "Abhi\n" + "Sri\n" + "</MESSAGE>");
		Parser.setLineSeparator("\r\n");
		NodeIterator e = parser.elements();

		Node _node = e.nextNode();
		try {
			String result = TagScanner.extractXMLData(_node, "MESSAGE", parser.getReader());
			assertEquals("Result", "Abhi\r\nSri\r\n", result);
		} catch (ParserException ex) {
			assertTrue(e.toString(), false);
		}
	}

	public void testExtractXMLDataSingle() throws ParserException {
		createParser("<MESSAGE>Test</MESSAGE>");
		NodeIterator e = parser.elements();

		Node _node = e.nextNode();
		try {
			String result = TagScanner.extractXMLData(_node, "MESSAGE", parser.getReader());
			assertEquals("Result", "Test", result);
		} catch (ParserException ex) {
			assertTrue(e.toString(), false);
		}
	}

	public void testTagExtraction() {
		String testHTML = "<AREA \n coords=0,0,52,52 href=\"http://www.yahoo.com/r/c1\" shape=RECT>";
		createParser(testHTML);
		Tag tag = Tag.find(parser.getReader(), testHTML, 0);
		assertNotNull(tag);
	}

	/**
	 * Captures bug reported by Raghavender Srimantula Problem is in isXMLTag -
	 * when it uses equals() to find a match
	 */
	public void testIsXMLTag() throws ParserException {
		createParser("<OPTION value=\"#\">Select a destination</OPTION>");
		Node _node;
		NodeIterator e = parser.elements();
		_node = e.nextNode();
		assertTrue("OPTION tag could not be identified", TagScanner.isXMLTagFound(_node, "OPTION"));
	}

	public void testRemoveChars() {
		String test = "hello\nworld\n\tqsdsds";
		TagScanner _scanner = new TagScanner() {// TODO: NOTUSED
			public Tag scan(Tag tag, String url, NodeReader _reader, String currLine) {
				return null;
			}

			public boolean evaluate(String s, TagScanner previousOpenScanner) {
				return false;
			}

			public String[] getID() {

				return null;
			}
		};
		String result = ParserUtils.removeChars(test, '\n');
		assertEquals("Removing Chars", "helloworld\tqsdsds", result);
	}

	public void testRemoveChars2() {
		String test = "hello\r\nworld\r\n\tqsdsds";
		TagScanner scanner = new TagScanner() {
			public Tag scan(Tag tag, String url, NodeReader _reader, String currLine) {
				return null;
			}

			public boolean evaluate(String s, TagScanner previousOpenScanner) {
				return false;
			}

			public String[] getID() {
				return null;
			}

		};
		String result = scanner.removeChars(test, "\r\n");
		assertEquals("Removing Chars", "helloworld\tqsdsds", result);
	}

	/**
	 * Bug report by Cedric Rosa in absorbLeadingBlanks - crashes if the tag is
	 * empty
	 */
	public void testAbsorbLeadingBlanksBlankTag() {
		String testData = new String("");
		String result = TagScanner.absorbLeadingBlanks(testData);
		assertEquals("", result);
	}

}
