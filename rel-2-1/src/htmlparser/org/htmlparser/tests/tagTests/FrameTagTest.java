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
import org.htmlparser.tags.FrameTag;
import org.htmlparser.tests.ParserTestCase;
import org.htmlparser.util.ParserException;

public class FrameTagTest extends ParserTestCase {

	public FrameTagTest(String name) {
		super(name);
	}

	public void testToHTML() throws ParserException {
		createParser("<frameset rows=\"115,*\" frameborder=\"NO\" border=\"0\" framespacing=\"0\">\n"
				+ "<frame name=\"topFrame\" noresize src=\"demo_bc_top.html\" scrolling=\"NO\" frameborder=\"NO\">\n"
				+ "<frame name=\"mainFrame\" src=\"http://www.kizna.com/web_e/\" scrolling=\"AUTO\">\n" + "</frameset>");
		parser.addScanner(new FrameScanner(""));

		parseAndAssertNodeCount(4);
		assertTrue("Node 1 should be Frame Tag", node[1] instanceof FrameTag);
		assertTrue("Node 2 should be Frame Tag", node[2] instanceof FrameTag);

		FrameTag frameTag1 = (FrameTag) node[1];
		FrameTag frameTag2 = (FrameTag) node[2];

		assertStringEquals("Frame 1 toHTML()",
				"<FRAME SCROLLING=\"NO\" FRAMEBORDER=\"NO\" SRC=\"demo_bc_top.html\" NAME=\"topFrame\" NORESIZE=\"\">",
				frameTag1.toHtml());
		assertStringEquals("Frame 2 toHTML()",
				"<FRAME SCROLLING=\"AUTO\" SRC=\"http://www.kizna.com/web_e/\" NAME=\"mainFrame\">", frameTag2.toHtml());
	}
}
