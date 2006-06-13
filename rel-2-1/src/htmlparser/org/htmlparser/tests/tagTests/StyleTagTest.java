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

import org.htmlparser.Parser;
import org.htmlparser.tags.StyleTag;
import org.htmlparser.tests.ParserTestCase;
import org.htmlparser.util.ParserException;

public class StyleTagTest extends ParserTestCase {

	public StyleTagTest(String name) {
		super(name);
	}

	public void testToHTML() throws ParserException {
		createParser("<style>a.h{background-color:#ffee99}</style>");
		parser.registerScanners();
		parseAndAssertNodeCount(1);
		assertTrue(node[0] instanceof StyleTag);
		StyleTag styleTag = (StyleTag) node[0];
		assertEquals("Raw String", "<STYLE>a.h{background-color:#ffee99}</STYLE>", styleTag.toHtml());
	}

	/**
	 * Reproducing a bug reported by Dhaval Udani relating to style tag
	 * attributes being missed
	 */
	public void testToHTML_Attriubtes() throws ParserException {
		createParser("<STYLE type=\"text/css\">\n" + "<!--" + "{something....something}" + "-->" + "</STYLE>");

		Parser.setLineSeparator("\r\n");
		parser.registerScanners();
		parseAndAssertNodeCount(1);
		assertTrue(node[0] instanceof StyleTag);
		StyleTag styleTag = (StyleTag) node[0];
		assertStringEquals("Raw String", "<STYLE TYPE=\"text/css\">\r\n" + "<!--" + "{something....something}" + "-->"
				+ "</STYLE>", styleTag.toHtml());
	}
}
