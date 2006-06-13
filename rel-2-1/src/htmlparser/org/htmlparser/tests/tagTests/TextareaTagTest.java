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

import org.htmlparser.scanners.TextareaTagScanner;
import org.htmlparser.tags.TextareaTag;
import org.htmlparser.tests.ParserTestCase;
import org.htmlparser.util.ParserException;

public class TextareaTagTest extends ParserTestCase {
	private String testHTML = new String(
			"<TEXTAREA name=\"Remarks\" >The intervention by the UN proved beneficial</TEXTAREA>"
					+ "<TEXTAREA>The capture of the Somali warloard was elusive</TEXTAREA>" + "<TEXTAREA></TEXTAREA>"
					+ "<TEXTAREA name=\"Remarks\">The death threats of the organization\n"
					+ "refused to intimidate the soldiers</TEXTAREA>"
					+ "<TEXTAREA name=\"Remarks\">The death threats of the LTTE\n"
					+ "refused to intimidate the Tamilians\n</TEXTAREA>");

	public TextareaTagTest(String name) {
		super(name);
	}

	public void setUp() throws Exception {
		super.setUp();
		createParser(testHTML);
		parser.addScanner(new TextareaTagScanner("-t"));
		parseAndAssertNodeCount(5);
	}

	public void testToHTML() throws ParserException {
		assertTrue("Node 1 should be Textarea Tag", node[0] instanceof TextareaTag);
		assertTrue("Node 2 should be Textarea Tag", node[1] instanceof TextareaTag);
		assertTrue("Node 3 should be Textarea Tag", node[2] instanceof TextareaTag);
		assertTrue("Node 4 should be Textarea Tag", node[3] instanceof TextareaTag);
		assertTrue("Node 5 should be Textarea Tag", node[4] instanceof TextareaTag);
		TextareaTag textareaTag;
		textareaTag = (TextareaTag) node[0];
		assertStringEquals("HTML String 1",
				"<TEXTAREA NAME=\"Remarks\">The intervention by the UN proved beneficial</TEXTAREA>", textareaTag
						.toHtml());
		textareaTag = (TextareaTag) node[1];
		assertStringEquals("HTML String 2", "<TEXTAREA>The capture of the Somali warloard was elusive</TEXTAREA>",
				textareaTag.toHtml());
		textareaTag = (TextareaTag) node[2];
		assertStringEquals("HTML String 3", "<TEXTAREA></TEXTAREA>", textareaTag.toHtml());
		textareaTag = (TextareaTag) node[3];
		assertStringEquals("HTML String 4", "<TEXTAREA NAME=\"Remarks\">The death threats of the organization\r\n"
				+ "refused to intimidate the soldiers</TEXTAREA>", textareaTag.toHtml());
		textareaTag = (TextareaTag) node[4];
		assertStringEquals("HTML String 5", "<TEXTAREA NAME=\"Remarks\">The death threats of the LTTE\r\n"
				+ "refused to intimidate the Tamilians\r\n</TEXTAREA>", textareaTag.toHtml());

	}

	public void testToString() throws ParserException {
		assertTrue("Node 1 should be Textarea Tag", node[0] instanceof TextareaTag);
		assertTrue("Node 2 should be Textarea Tag", node[1] instanceof TextareaTag);
		assertTrue("Node 3 should be Textarea Tag", node[2] instanceof TextareaTag);
		assertTrue("Node 4 should be Textarea Tag", node[3] instanceof TextareaTag);
		assertTrue("Node 5 should be Textarea Tag", node[4] instanceof TextareaTag);
		TextareaTag textareaTag;
		textareaTag = (TextareaTag) node[0];
		assertStringEquals("HTML Raw String 1",
				"TEXTAREA TAG\n--------\nNAME : Remarks\nVALUE : The intervention by the UN proved beneficial\n",
				textareaTag.toString());
		textareaTag = (TextareaTag) node[1];
		assertStringEquals("HTML Raw String 2",
				"TEXTAREA TAG\n--------\nVALUE : The capture of the Somali warloard was elusive\n", textareaTag
						.toString());
		textareaTag = (TextareaTag) node[2];
		assertStringEquals("HTML Raw String 3", "TEXTAREA TAG\n--------\nVALUE : \n", textareaTag.toString());
		textareaTag = (TextareaTag) node[3];
		assertStringEquals("HTML Raw String 4",
				"TEXTAREA TAG\n--------\nNAME : Remarks\nVALUE : The death threats of the organization\r\n"
						+ "refused to intimidate the soldiers\n", textareaTag.toString());
		textareaTag = (TextareaTag) node[4];
		assertStringEquals("HTML Raw String 5",
				"TEXTAREA TAG\n--------\nNAME : Remarks\nVALUE : The death threats of the LTTE\r\n"
						+ "refused to intimidate the Tamilians\r\n\n", textareaTag.toString());
	}

}
