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

import org.htmlparser.tags.DoctypeTag;
import org.htmlparser.tests.ParserTestCase;
import org.htmlparser.util.ParserException;

public class DoctypeTagTest extends ParserTestCase {

	public DoctypeTagTest(String name) {
		super(name);
	}

	public void testToHTML() throws ParserException {
		String testHTML = new String("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0//EN\">\n" + "<HTML>\n" + "<HEAD>\n"
				+ "<TITLE>Cogs of Chicago</TITLE>\n" + "</HEAD>\n" + "<BODY>\n" + "...\n" + "</BODY>\n" + "</HTML>\n");
		createParser(testHTML);
		parser.registerScanners();
		parseAndAssertNodeCount(9);
		// The node should be an HTMLLinkTag
		assertTrue("Node should be a HTMLDoctypeTag", node[0] instanceof DoctypeTag);
		DoctypeTag docTypeTag = (DoctypeTag) node[0];
		assertStringEquals("toHTML()", "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0//EN\">", docTypeTag.toHtml());
	}
}
