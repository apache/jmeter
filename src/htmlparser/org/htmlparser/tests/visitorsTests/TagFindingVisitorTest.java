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

import org.htmlparser.Node;
import org.htmlparser.tags.Tag;
import org.htmlparser.tests.ParserTestCase;
import org.htmlparser.visitors.TagFindingVisitor;

public class TagFindingVisitorTest extends ParserTestCase {
	private String html = "<HTML><HEAD><TITLE>This is the Title</TITLE></HEAD>"
			+ "<BODY>Hello World, this is an excellent parser</BODY>" + "<UL><LI><LI></UL>"
			+ "<A href=\"http://www.industriallogic.com\">Industrial Logic</a>" + "</HTML>";

	public TagFindingVisitorTest(String name) {
		super(name);
	}

	public void setUp() {
		createParser(html);
	}

	public void testTagFound() throws Exception {
		TagFindingVisitor visitor = new TagFindingVisitor(new String[] { "HEAD" });
		parser.visitAllNodesWith(visitor);
		assertEquals("HEAD found", 1, visitor.getTagCount(0));
	}

	public void testTagsFound() throws Exception {
		TagFindingVisitor visitor = new TagFindingVisitor(new String[] { "LI" });
		parser.visitAllNodesWith(visitor);
		assertEquals("LI tags found", 2, visitor.getTagCount(0));
	}

	public void testMultipleTags() throws Exception {
		TagFindingVisitor visitor = new TagFindingVisitor(new String[] { "LI", "BODY", "UL", "A" });
		parser.visitAllNodesWith(visitor);
		assertEquals("LI tags found", 2, visitor.getTagCount(0));
		assertEquals("BODY tag found", 1, visitor.getTagCount(1));
		assertEquals("UL tag found", 1, visitor.getTagCount(2));
		assertEquals("A tag found", 1, visitor.getTagCount(3));
	}

	public void testEndTags() throws Exception {
		TagFindingVisitor visitor = new TagFindingVisitor(new String[] { "LI", "BODY", "UL", "A" }, true);
		parser.visitAllNodesWith(visitor);
		assertEquals("LI tags found", 2, visitor.getTagCount(0));
		assertEquals("BODY tag found", 1, visitor.getTagCount(1));
		assertEquals("UL tag found", 1, visitor.getTagCount(2));
		assertEquals("A tag found", 1, visitor.getTagCount(3));
		assertEquals("BODY end tag found", 1, visitor.getEndTagCount(1));
	}

	public void assertTagNameShouldBe(String message, Node node, String expectedTagName) {
		Tag tag = (Tag) node;
		assertStringEquals(message, expectedTagName, tag.getTagName());
	}
}
