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
package org.htmlparser.tests.visitorsTests;

import org.htmlparser.tests.ParserTestCase;
import org.htmlparser.visitors.StringFindingVisitor;

public class StringFindingVisitorTest extends ParserTestCase {
	private static final String HTML = "<HTML><HEAD><TITLE>This is the Title</TITLE>"
			+ "</HEAD><BODY>Hello World, this is an excellent parser</BODY></HTML>";

	private static final String HTML_TO_SEARCH = "<HTML><HEAD><TITLE>test</TITLE></HEAD>\n"
			+ "<BODY><H1>This is a test page</H1>\n" + "Writing tests is good for code. Testing is a good\n"
			+ "philosophy. Test driven development is even better.\n";

	public StringFindingVisitorTest(String name) {
		super(name);
	}

	public void testSimpleStringFind() throws Exception {
		createParser(HTML);
		StringFindingVisitor visitor = new StringFindingVisitor("Hello");
		parser.visitAllNodesWith(visitor);
		assertTrue("Hello found", visitor.stringWasFound());
	}

	public void testStringNotFound() throws Exception {
		createParser(HTML);
		StringFindingVisitor visitor = new StringFindingVisitor("industrial logic");
		parser.visitAllNodesWith(visitor);
		assertTrue("industrial logic should not have been found", !visitor.stringWasFound());
	}

	public void testStringInTagNotFound() throws Exception {
		createParser(HTML);
		StringFindingVisitor visitor = new StringFindingVisitor("HTML");
		parser.visitAllNodesWith(visitor);
		assertTrue("HTML should not have been found", !visitor.stringWasFound());
	}

	public void testStringFoundInSingleStringNode() throws Exception {
		createParser("this is some text!");
		StringFindingVisitor visitor = new StringFindingVisitor("text");
		parser.visitAllNodesWith(visitor);
		assertTrue("text should be found", visitor.stringWasFound());
	}

	public void testStringFoundCount() throws Exception {
		createParser(HTML);
		StringFindingVisitor visitor = new StringFindingVisitor("is");
		parser.visitAllNodesWith(visitor);
		assertEquals("# times 'is' was found", 2, visitor.stringFoundCount());

		visitor = new StringFindingVisitor("and");
		parser.visitAllNodesWith(visitor);
		assertEquals("# times 'and' was found", 0, visitor.stringFoundCount());
	}

	public void testStringFoundMultipleTimes() throws Exception {
		createParser(HTML_TO_SEARCH);
		StringFindingVisitor visitor = new StringFindingVisitor("TEST");
		visitor.doMultipleSearchesWithinStrings();
		parser.visitAllNodesWith(visitor);
		assertEquals("TEST found", 5, visitor.stringFoundCount());
	}

}
