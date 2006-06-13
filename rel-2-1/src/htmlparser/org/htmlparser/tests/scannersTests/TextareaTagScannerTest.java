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

import org.htmlparser.scanners.TextareaTagScanner;
import org.htmlparser.tags.TextareaTag;
import org.htmlparser.tests.ParserTestCase;
import org.htmlparser.util.ParserException;

public class TextareaTagScannerTest extends ParserTestCase {

	private String testHTML = new String(
			"<TEXTAREA name=\"Remarks\">The intervention by the UN proved beneficial</TEXTAREA>"
					+ "<TEXTAREA>The capture of the Somali warloard was elusive</TEXTAREA>" + "<TEXTAREA></TEXTAREA>"
					+ "<TEXTAREA name=\"Remarks\">The death threats of the organization\n"
					+ "refused to intimidate the soldiers</TEXTAREA>"
					+ "<TEXTAREA name=\"Remarks\">The death threats of the LTTE\n"
					+ "refused to intimidate the Tamilians\n</TEXTAREA>");

	private TextareaTagScanner scanner;

	public TextareaTagScannerTest(String name) {
		super(name);
	}

	public void testScan() throws ParserException {
		scanner = new TextareaTagScanner("-i");
		createParser(testHTML);
		scanner = new TextareaTagScanner("-ta");
		parser.addScanner(scanner);
		parseAndAssertNodeCount(5);
		assertTrue(node[0] instanceof TextareaTag);
		assertTrue(node[1] instanceof TextareaTag);
		assertTrue(node[2] instanceof TextareaTag);
		assertTrue(node[3] instanceof TextareaTag);
		assertTrue(node[4] instanceof TextareaTag);

		// check the Textarea node
		for (int j = 0; j < nodeCount; j++) {
			TextareaTag TextareaTag = (TextareaTag) node[j];
			assertEquals("Textarea Scanner", scanner, TextareaTag.getThisScanner());
		}
	}
}
