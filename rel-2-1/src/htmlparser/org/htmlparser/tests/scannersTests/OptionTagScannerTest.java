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
import org.htmlparser.scanners.OptionTagScanner;
import org.htmlparser.tags.OptionTag;
import org.htmlparser.tests.ParserTestCase;
import org.htmlparser.util.ParserException;

public class OptionTagScannerTest extends ParserTestCase {

	private String testHTML = new String("<OPTION value=\"Google Search\">Google</OPTION>"
			+ "<OPTION value=\"AltaVista Search\">AltaVista" + "<OPTION value=\"Lycos Search\"></OPTION>"
			+ "<OPTION>Yahoo!</OPTION>" + "<OPTION>\nHotmail</OPTION>" + "<OPTION>Mailcity\n</OPTION>"
			+ "<OPTION>\nIndiatimes\n</OPTION>" + "<OPTION>\nRediff\n</OPTION>\n" + "<OPTION>Cricinfo");

	private OptionTagScanner scanner;

	private Node[] node;

	private int i;

	public OptionTagScannerTest(String name) {
		super(name);
	}

	public void testScan() throws ParserException {
		scanner = new OptionTagScanner("-i");
		createParser(testHTML, "http://www.google.com/test/index.html");
		parser.addScanner(scanner);
		parseAndAssertNodeCount(9);
		for (int j = 0; j < i; j++) {
			assertTrue("Node " + j + " should be Option Tag", node[j] instanceof OptionTag);
			OptionTag OptionTag = (OptionTag) node[j];
			assertEquals("Option Scanner", scanner, OptionTag.getThisScanner());
		}
	}

	public static void main(String[] args) {
		new junit.awtui.TestRunner().start(new String[] { OptionTagScannerTest.class.getName() });
	}

}
