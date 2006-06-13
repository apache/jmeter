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

import org.htmlparser.scanners.OptionTagScanner;
import org.htmlparser.scanners.SelectTagScanner;
import org.htmlparser.tags.OptionTag;
import org.htmlparser.tags.SelectTag;
import org.htmlparser.tests.ParserTestCase;
import org.htmlparser.util.ParserException;

public class SelectTagTest extends ParserTestCase {
	private String testHTML = new String("<SELECT name=\"Nominees\">\n" + "<option value=\"Spouse\">Spouse"
			+ "<option value=\"Father\"></option>\n" + "<option value=\"Mother\">Mother\n"
			+ "<option value=\"Son\">\nSon\n</option>" + "<option value=\"Daughter\">\nDaughter\n"
			+ "<option value=\"Nephew\">\nNephew</option>\n" + "<option value=\"Niece\">Niece\n" + "</select>");

	private SelectTag selectTag;

	public SelectTagTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		createParser(testHTML);
		parser.addScanner(new SelectTagScanner("-s"));
		parser.addScanner(new OptionTagScanner("-o"));
		parseAndAssertNodeCount(1);
		assertTrue("Node 1 should be Select Tag", node[0] instanceof SelectTag);
		selectTag = (SelectTag) node[0];
	}

	public void testToHTML() throws ParserException {
		assertStringEquals("HTML String", "<SELECT NAME=\"Nominees\">\r\n" + "<OPTION VALUE=\"Spouse\">Spouse</OPTION>"
				+ "<OPTION VALUE=\"Father\"></OPTION>\r\n" + "<OPTION VALUE=\"Mother\">Mother\r\n</OPTION>"
				+ "<OPTION VALUE=\"Son\">\r\nSon\r\n</OPTION>" + "<OPTION VALUE=\"Daughter\">\r\nDaughter\r\n</OPTION>"
				+ "<OPTION VALUE=\"Nephew\">\r\nNephew</OPTION>\r\n" + "<OPTION VALUE=\"Niece\">Niece\r\n</OPTION>"
				+ "</SELECT>", selectTag.toHtml());
	}

	public void testToString() throws ParserException {
		assertTrue("Node 1 should be Select Tag", node[0] instanceof SelectTag);
		SelectTag selectTag;
		selectTag = (SelectTag) node[0];
		assertStringEquals("HTML Raw String", "SELECT TAG\n--------\nNAME : Nominees\n"
				+ "OPTION VALUE: Spouse TEXT: Spouse\n\n" + "OPTION VALUE: Father TEXT: \n\n"
				+ "OPTION VALUE: Mother TEXT: Mother\r\n\n\n" + "OPTION VALUE: Son TEXT: Son\r\n\n\n"
				+ "OPTION VALUE: Daughter TEXT: Daughter\r\n\n\n" + "OPTION VALUE: Nephew TEXT: Nephew\n\n"
				+ "OPTION VALUE: Niece TEXT: Niece\r\n\n\n", selectTag.toString());
	}

	public void testGetOptionTags() {
		OptionTag[] optionTags = selectTag.getOptionTags();
		assertEquals("option tag array length", 7, optionTags.length);
		assertEquals("option tag 1", "Spouse", optionTags[0].getOptionText());
		assertEquals("option tag 7", "Niece\r\n", optionTags[6].getOptionText());
	}

	public static void main(String[] args) {
		new junit.awtui.TestRunner().start(new String[] { SelectTagTest.class.getName() });
	}

}
