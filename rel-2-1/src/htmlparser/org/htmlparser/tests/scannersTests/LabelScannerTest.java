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

import java.util.*;
import junit.framework.TestSuite;
import org.htmlparser.scanners.LabelScanner;
import org.htmlparser.tags.LabelTag;
import org.htmlparser.tests.ParserTestCase;
import org.htmlparser.util.ParserException;

public class LabelScannerTest extends ParserTestCase {

	public LabelScannerTest(String name) {
		super(name);
	}

	public void testSimpleLabels() throws ParserException {
		createParser("<label>This is a label tag</label>");
		LabelScanner labelScanner = new LabelScanner("-l");
		parser.addScanner(labelScanner);
		parseAndAssertNodeCount(1);
		assertTrue(node[0] instanceof LabelTag);
		// check the title node
		LabelTag labelTag = (LabelTag) node[0];
		assertEquals("Label", "This is a label tag", labelTag.getChildrenHTML());
		assertEquals("Label", "This is a label tag", labelTag.getLabel());
		assertStringEquals("Label", "<LABEL>This is a label tag</LABEL>", labelTag.toHtml());
		assertEquals("Label Scanner", labelScanner, labelTag.getThisScanner());
	}

	public void testLabelWithJspTag() throws ParserException {
		createParser("<label><%=labelValue%></label>");
		parser.registerScanners();
		LabelScanner labelScanner = new LabelScanner("-l");
		parser.addScanner(labelScanner);
		parseAndAssertNodeCount(1);
		assertTrue(node[0] instanceof LabelTag);
		// check the title node
		LabelTag labelTag = (LabelTag) node[0];
		assertStringEquals("Label", "<LABEL><%=labelValue%></LABEL>", labelTag.toHtml());
		assertEquals("Label Scanner", labelScanner, labelTag.getThisScanner());
	}

	public void testLabelWithOtherTags() throws ParserException {
		createParser("<label><span>Span within label</span></label>");
		parser.registerScanners();
		LabelScanner labelScanner = new LabelScanner("-l");
		parser.addScanner(labelScanner);
		parseAndAssertNodeCount(1);
		assertTrue(node[0] instanceof LabelTag);
		// check the title node
		LabelTag labelTag = (LabelTag) node[0];
		assertEquals("Label value", "Span within label", labelTag.getLabel());
		assertStringEquals("Label", "<LABEL><SPAN>Span within label</SPAN></LABEL>", labelTag.toHtml());
		assertEquals("Label Scanner", labelScanner, labelTag.getThisScanner());
	}

	public void testLabelWithManyCompositeTags() throws ParserException {
		createParser("<label><span>Jane <b> Doe </b> Smith</span></label>");
		parser.registerScanners();
		LabelScanner labelScanner = new LabelScanner("-l");
		parser.addScanner(labelScanner);
		parseAndAssertNodeCount(1);
		assertTrue(node[0] instanceof LabelTag);
		LabelTag labelTag = (LabelTag) node[0];
		assertEquals("Label value", "<SPAN>Jane <B> Doe </B> Smith</SPAN>", labelTag.getChildrenHTML());
		assertEquals("Label value", "Jane  Doe  Smith", labelTag.getLabel());
		assertStringEquals("Label", "<LABEL><SPAN>Jane <B> Doe </B> Smith</SPAN></LABEL>", labelTag.toHtml());
		assertEquals("Label Scanner", labelScanner, labelTag.getThisScanner());
	}

	public void testLabelsID() throws ParserException {
		createParser("<label>John Doe</label>");
		parser.registerScanners();
		LabelScanner labelScanner = new LabelScanner("-l");
		parser.addScanner(labelScanner);
		parseAndAssertNodeCount(1);
		assertTrue(node[0] instanceof LabelTag);

		LabelTag labelTag = (LabelTag) node[0];
		assertStringEquals("Label", "<LABEL>John Doe</LABEL>", labelTag.toHtml());
		Hashtable attr = labelTag.getAttributes();
		assertNull("ID", attr.get("id"));
	}

	public void testNestedLabels() throws ParserException {
		createParser("<label id=\"attr1\"><label>Jane Doe");
		parser.registerScanners();
		LabelScanner labelScanner = new LabelScanner("-l");
		parser.addScanner(labelScanner);
		parseAndAssertNodeCount(2);
		assertTrue(node[0] instanceof LabelTag);
		assertTrue(node[1] instanceof LabelTag);

		LabelTag labelTag = (LabelTag) node[0];
		assertStringEquals("Label", "<LABEL ID=\"attr1\" ></LABEL>", labelTag.toHtml());
		labelTag = (LabelTag) node[1];
		assertStringEquals("Label", "<LABEL>Jane Doe</LABEL>", labelTag.toHtml());
		Hashtable attr = labelTag.getAttributes();
		assertNull("ID", attr.get("id"));
	}

	public void testNestedLabels2() throws ParserException {
		String testHTML = new String("<LABEL value=\"Google Search\">Google</LABEL>"
				+ "<LABEL value=\"AltaVista Search\">AltaVista" + "<LABEL value=\"Lycos Search\"></LABEL>"
				+ "<LABEL>Yahoo!</LABEL>" + "<LABEL>\nHotmail</LABEL>" + "<LABEL value=\"ICQ Messenger\">"
				+ "<LABEL>Mailcity\n</LABEL>" + "<LABEL>\nIndiatimes\n</LABEL>" + "<LABEL>\nRediff\n</LABEL>\n"
				+ "<LABEL>Cricinfo" + "<LABEL value=\"Microsoft Passport\">"
				+ "<LABEL value=\"AOL\"><SPAN>AOL</SPAN></LABEL>"
				+ "<LABEL value=\"Time Warner\">Time <B>Warner <SPAN>AOL </SPAN>Inc.</B>");
		createParser(testHTML);
		// parser.registerScanners();
		LabelScanner labelScanner = new LabelScanner("-l");
		parser.addScanner(labelScanner);
		parseAndAssertNodeCount(13);

		// for(int j=0;j<nodeCount;j++)
		// {
		// //assertTrue("Node " + j + " should be Label Tag",node[j] instanceof
		// LabelTag);
		// System.out.println(node[j].getClass().getName());
		// System.out.println(node[j].toHtml());
		// }

		LabelTag LabelTag;
		LabelTag = (LabelTag) node[0];
		assertStringEquals("HTML String", "<LABEL VALUE=\"Google Search\">Google</LABEL>", LabelTag.toHtml());
		LabelTag = (LabelTag) node[1];
		assertStringEquals("HTML String", "<LABEL VALUE=\"AltaVista Search\">AltaVista</LABEL>", LabelTag.toHtml());
		LabelTag = (LabelTag) node[2];
		assertStringEquals("HTML String", "<LABEL VALUE=\"Lycos Search\"></LABEL>", LabelTag.toHtml());
		LabelTag = (LabelTag) node[3];
		assertStringEquals("HTML String", "<LABEL>Yahoo!</LABEL>", LabelTag.toHtml());
		LabelTag = (LabelTag) node[4];
		assertStringEquals("HTML String", "<LABEL>\r\nHotmail</LABEL>", LabelTag.toHtml());
		LabelTag = (LabelTag) node[5];
		assertStringEquals("HTML String", "<LABEL VALUE=\"ICQ Messenger\"></LABEL>", LabelTag.toHtml());
		LabelTag = (LabelTag) node[6];
		assertStringEquals("HTML String", "<LABEL>Mailcity\r\n</LABEL>", LabelTag.toHtml());
		LabelTag = (LabelTag) node[7];
		assertStringEquals("HTML String", "<LABEL>\r\nIndiatimes\r\n</LABEL>", LabelTag.toHtml());
		LabelTag = (LabelTag) node[8];
		assertStringEquals("HTML String", "<LABEL>\r\nRediff\r\n</LABEL>", LabelTag.toHtml());
		LabelTag = (LabelTag) node[9];
		assertStringEquals("HTML String", "<LABEL>Cricinfo</LABEL>", LabelTag.toHtml());
		LabelTag = (LabelTag) node[10];
		assertStringEquals("HTML String", "<LABEL VALUE=\"Microsoft Passport\"></LABEL>", LabelTag.toHtml());
		LabelTag = (LabelTag) node[11];
		assertStringEquals("HTML String", "<LABEL VALUE=\"AOL\"><SPAN>AOL</SPAN></LABEL>", LabelTag.toHtml());
		LabelTag = (LabelTag) node[12];
		assertStringEquals("HTML String",
				"<LABEL VALUE=\"Time Warner\">Time <B>Warner <SPAN>AOL </SPAN>Inc.</B></LABEL>", LabelTag.toHtml());
	}

	public static TestSuite suite() {
		return new TestSuite(LabelScannerTest.class);
	}

	public static void main(String[] args) {
		new junit.awtui.TestRunner().start(new String[] { LabelScannerTest.class.getName() });
	}

}
