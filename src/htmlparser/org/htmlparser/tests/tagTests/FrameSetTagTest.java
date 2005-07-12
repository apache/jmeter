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

import org.htmlparser.scanners.FrameScanner;
import org.htmlparser.scanners.FrameSetScanner;
import org.htmlparser.tags.FrameSetTag;
import org.htmlparser.tests.ParserTestCase;
import org.htmlparser.util.ParserException;

public class FrameSetTagTest extends ParserTestCase {

	public FrameSetTagTest(String name) {
		super(name);
	}

	public void testToHTML() throws ParserException {
		createParser("<frameset rows=\"115,*\" frameborder=\"NO\" border=\"0\" framespacing=\"0\">\n"
				+ "<frame name=\"topFrame\" noresize src=\"demo_bc_top.html\" scrolling=\"NO\" frameborder=\"NO\">\n"
				+ "<frame name=\"mainFrame\" src=\"http://www.kizna.com/web_e/\" scrolling=\"AUTO\">\n" + "</frameset>");

		parser.addScanner(new FrameSetScanner(""));
		parser.addScanner(new FrameScanner(""));

		parseAndAssertNodeCount(1);
		assertTrue("Node 0 should be End Tag", node[0] instanceof FrameSetTag);
		FrameSetTag frameSetTag = (FrameSetTag) node[0];
		assertStringEquals(
				"HTML Contents",
				"<FRAMESET BORDER=\"0\" ROWS=\"115,*\" FRAMESPACING=\"0\" FRAMEBORDER=\"NO\">\r\n"
						+ "<FRAME SCROLLING=\"NO\" FRAMEBORDER=\"NO\" SRC=\"demo_bc_top.html\" NAME=\"topFrame\" NORESIZE=\"\">\r\n"
						+ "<FRAME SCROLLING=\"AUTO\" SRC=\"http://www.kizna.com/web_e/\" NAME=\"mainFrame\">\r\n"
						+ "</FRAMESET>", frameSetTag.toHtml());
	}
}
