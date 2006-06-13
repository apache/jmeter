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

import org.htmlparser.scanners.MetaTagScanner;
import org.htmlparser.scanners.StyleScanner;
import org.htmlparser.scanners.TitleScanner;
import org.htmlparser.tags.TitleTag;
import org.htmlparser.tests.ParserTestCase;
import org.htmlparser.util.ParserException;

public class TitleScannerTest extends ParserTestCase {

	public TitleScannerTest(String name) {
		super(name);
	}

	public void testScan() throws ParserException {
		createParser("<html><head><title>Yahoo!</title><base href=http://www.yahoo.com/ target=_top><meta http-equiv=\"PICS-Label\" content='(PICS-1.1 \"http://www.icra.org/ratingsv02.html\" l r (cz 1 lz 1 nz 1 oz 1 vz 1) gen true for \"http://www.yahoo.com\" r (cz 1 lz 1 nz 1 oz 1 vz 1) \"http://www.rsac.org/ratingsv01.html\" l r (n 0 s 0 v 0 l 0) gen true for \"http://www.yahoo.com\" r (n 0 s 0 v 0 l 0))'><style>a.h{background-color:#ffee99}</style></head>");
		TitleScanner titleScanner = new TitleScanner("-t");
		parser.addScanner(titleScanner);
		parser.addScanner(new StyleScanner("-s"));
		parser.addScanner(new MetaTagScanner("-m"));
		parseAndAssertNodeCount(7);
		assertTrue(node[2] instanceof TitleTag);
		// check the title node
		TitleTag titleTag = (TitleTag) node[2];
		assertEquals("Title", "Yahoo!", titleTag.getTitle());
		assertEquals("Title Scanner", titleScanner, titleTag.getThisScanner());
	}

	/**
	 * Testcase to reproduce a bug reported by Cedric Rosa, on not ending the
	 * title tag correctly, we would get null pointer exceptions..
	 */
	public void testIncompleteTitle() throws ParserException {
		createParser("<TITLE>SISTEMA TERRA, VOL. VI , No. 1-3, December 1997</TITLE\n" + "</HEAD>");
		TitleScanner titleScanner = new TitleScanner("-t");
		parser.addScanner(titleScanner);
		parseAndAssertNodeCount(2);
		assertTrue("First Node is a title tag", node[0] instanceof TitleTag);
		TitleTag titleTag = (TitleTag) node[0];
		assertEquals("Title", "SISTEMA TERRA, VOL. VI , No. 1-3, December 1997", titleTag.getTitle());

	}

	/**
	 * If there are duplicates of the title tag, the parser crashes. This bug
	 * was reported by Claude Duguay
	 */
	public void testDoubleTitleTag() throws ParserException {
		createParser("<html><head><TITLE>\n" + "<html><head><TITLE>\n" + "Double tags can hang the code\n"
				+ "</TITLE></head><body>\n" + "<body><html>");
		TitleScanner titleScanner = new TitleScanner("-t");
		parser.addScanner(titleScanner);
		parseAndAssertNodeCount(7);
		assertTrue("Third tag should be a title tag", node[2] instanceof TitleTag);
		TitleTag titleTag = (TitleTag) node[2];
		assertEquals("Title", "Double tags can hang the code\r\n", titleTag.getTitle());

	}

	/**
	 * Testcase based on Claude Duguay's report. This proves that the parser
	 * throws exceptions when faced with malformed html
	 */
	public void testNoEndTitleTag() throws ParserException {
		createParser("<TITLE>KRP VALIDATION<PROCESS/TITLE>");
		TitleScanner titleScanner = new TitleScanner("-t");
		parser.addScanner(titleScanner);
		parseAndAssertNodeCount(1);
		TitleTag titleTag = (TitleTag) node[0];
		assertEquals("Expected title", "KRP VALIDATION", titleTag.getTitle());
	}

	public void testTitleTagContainsJspTag() throws ParserException {
		createParser("<html><head><title><%=gTitleString%></title><base href=http://www.yahoo.com/ target=_top><meta http-equiv=\"PICS-Label\" content='(PICS-1.1 \"http://www.icra.org/ratingsv02.html\" l r (cz 1 lz 1 nz 1 oz 1 vz 1) gen true for \"http://www.yahoo.com\" r (cz 1 lz 1 nz 1 oz 1 vz 1) \"http://www.rsac.org/ratingsv01.html\" l r (n 0 s 0 v 0 l 0) gen true for \"http://www.yahoo.com\" r (n 0 s 0 v 0 l 0))'><style>a.h{background-color:#ffee99}</style></head>");
		parser.registerScanners();
		parseAndAssertNodeCount(7);
		assertTrue(node[2] instanceof TitleTag);
		TitleTag titleTag = (TitleTag) node[2];
		assertStringEquals("HTML Rendering", "<TITLE><%=gTitleString%></TITLE>", titleTag.toHtml());
	}
}
