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

import org.htmlparser.scanners.FrameScanner;
import org.htmlparser.scanners.FrameSetScanner;
import org.htmlparser.tags.FrameSetTag;
import org.htmlparser.tags.FrameTag;
import org.htmlparser.tests.ParserTestCase;
import org.htmlparser.util.ParserException;

public class FrameSetScannerTest extends ParserTestCase {

	public FrameSetScannerTest(String name) {
		super(name);
	}

	public void testEvaluate() {
		String line1 = "frameset rows=\"115,*\" frameborder=\"NO\" border=\"0\" framespacing=\"0\"";
		String line2 = "FRAMESET rows=\"115,*\" frameborder=\"NO\" border=\"0\" framespacing=\"0\"";
		String line3 = "Frameset rows=\"115,*\" frameborder=\"NO\" border=\"0\" framespacing=\"0\"";
		FrameSetScanner frameSetScanner = new FrameSetScanner("");
		assertTrue("Line 1", frameSetScanner.evaluate(line1, null));
		assertTrue("Line 2", frameSetScanner.evaluate(line2, null));
		assertTrue("Line 3", frameSetScanner.evaluate(line3, null));
	}

	public void testScan() throws ParserException {
		createParser(
				"<frameset rows=\"115,*\" frameborder=\"NO\" border=\"0\" framespacing=\"0\">\n"
						+ "<frame name=\"topFrame\" noresize src=\"demo_bc_top.html\" scrolling=\"NO\" frameborder=\"NO\">\n"
						+ "<frame name=\"mainFrame\" src=\"http://www.kizna.com/web_e/\" scrolling=\"AUTO\">\n"
						+ "</frameset>", "http://www.google.com/test/index.html");

		parser.addScanner(new FrameSetScanner(""));
		parser.addScanner(new FrameScanner());

		parseAndAssertNodeCount(1);
		assertTrue("Node 0 should be End Tag", node[0] instanceof FrameSetTag);
		FrameSetTag frameSetTag = (FrameSetTag) node[0];
		// Find the details of the frameset itself
		assertEquals("Rows", "115,*", frameSetTag.getAttribute("rows"));
		assertEquals("FrameBorder", "NO", frameSetTag.getAttribute("FrameBorder"));
		assertEquals("FrameSpacing", "0", frameSetTag.getAttribute("FrameSpacing"));
		assertEquals("Border", "0", frameSetTag.getAttribute("Border"));
		// Now check the frames
		FrameTag topFrame = frameSetTag.getFrame("topFrame");
		FrameTag mainFrame = frameSetTag.getFrame("mainFrame");
		assertNotNull("Top Frame should not be null", topFrame);
		assertNotNull("Main Frame should not be null", mainFrame);
		assertEquals("Top Frame Name", "topFrame", topFrame.getFrameName());
		assertEquals("Top Frame Location", "http://www.google.com/test/demo_bc_top.html", topFrame.getFrameLocation());
		assertEquals("Main Frame Name", "mainFrame", mainFrame.getFrameName());
		assertEquals("Main Frame Location", "http://www.kizna.com/web_e/", mainFrame.getFrameLocation());
		assertEquals("Scrolling in Main Frame", "AUTO", mainFrame.getAttribute("Scrolling"));
	}
}
