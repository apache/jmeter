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

import org.htmlparser.scanners.OptionTagScanner;
import org.htmlparser.scanners.SelectTagScanner;
import org.htmlparser.tags.OptionTag;
import org.htmlparser.tags.SelectTag;
import org.htmlparser.tests.ParserTestCase;
import org.htmlparser.util.ParserException;

public class SelectTagScannerTest extends ParserTestCase {

	private String testHTML = new String("<Select name=\"Remarks\">" + "<option value='option1'>option1</option>"
			+ "</Select>" + "<Select name=\"something\">" + "<option value='option2'>option2</option>" + "</Select>"
			+ "<Select></Select>" + "<Select name=\"Remarks\">The death threats of the organization\n"
			+ "refused to intimidate the soldiers</Select>"
			+ "<Select name=\"Remarks\">The death threats of the LTTE\n"
			+ "refused to intimidate the Tamilians\n</Select>");

	private SelectTagScanner scanner;

	public SelectTagScannerTest(String name) {
		super(name);
	}

	public void testScan() throws ParserException {

		scanner = new SelectTagScanner("-i");
		createParser(testHTML, "http://www.google.com/test/index.html");
		scanner = new SelectTagScanner("-ta");
		parser.addScanner(scanner);
		parser.addScanner(new OptionTagScanner(""));

		parseAndAssertNodeCount(5);
		assertTrue(node[0] instanceof SelectTag);
		assertTrue(node[1] instanceof SelectTag);
		assertTrue(node[2] instanceof SelectTag);
		assertTrue(node[3] instanceof SelectTag);
		assertTrue(node[4] instanceof SelectTag);

		// check the Select node
		for (int j = 0; j < nodeCount; j++) {
			SelectTag SelectTag = (SelectTag) node[j];
			assertEquals("Select Scanner", scanner, SelectTag.getThisScanner());
		}

		SelectTag selectTag = (SelectTag) node[0];
		OptionTag[] optionTags = selectTag.getOptionTags();
		assertEquals("option tag array length", 1, optionTags.length);
		assertEquals("option tag value", "option1", optionTags[0].getOptionText());
	}

	/**
	 * Bug reproduction based on report by gumirov@ccfit.nsu.ru
	 */
	public void testSelectTagWithComments() throws Exception {
		createParser("<form>" + "<select> " + "<!-- 1 --><option selected>123 " + "<option>345 " + "</select> "
				+ "</form>");
		parser.registerScanners();
		parseAndAssertNodeCount(1);

	}
}
