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

import org.htmlparser.Node;
import org.htmlparser.StringNode;
import org.htmlparser.scanners.CompositeTagScanner;
import org.htmlparser.tags.CompositeTag;
import org.htmlparser.tags.EndTag;
import org.htmlparser.tags.Tag;
import org.htmlparser.tags.data.CompositeTagData;
import org.htmlparser.tags.data.TagData;
import org.htmlparser.tests.ParserTestCase;
import org.htmlparser.util.ParserException;

public class CompositeTagScannerTest extends ParserTestCase {
	private CompositeTagScanner scanner;

	private String url;

	public CompositeTagScannerTest(String name) {
		super(name);
	}

	protected void setUp() {
		String[] arr = { "SOMETHING" };
		scanner = new CompositeTagScanner(arr) {
			public Tag createTag(TagData tagData, CompositeTagData compositeTagData) throws ParserException {
				return null;
			}

			public String[] getID() {
				return null;
			}

		};
	}

	private CustomTag parseCustomTag(int expectedNodeCount) throws ParserException {
		parser.addScanner(new CustomScanner());
		parseAndAssertNodeCount(expectedNodeCount);
		assertType("node", CustomTag.class, node[0]);
		CustomTag customTag = (CustomTag) node[0];
		return customTag;
	}

	public void testEmptyCompositeTag() throws ParserException {
		createParser("<Custom/>");
		CustomTag customTag = parseCustomTag(1);
		int x = customTag.getChildCount();
		assertEquals("child count", 0, customTag.getChildCount());
		assertTrue("custom tag should be xml end tag", customTag.isEmptyXmlTag());
		assertEquals("starting loc", 0, customTag.getStartTag().elementBegin());
		assertEquals("ending loc", 8, customTag.getStartTag().elementEnd());
		assertEquals("starting line position", 1, customTag.tagData.getStartLine());
		assertEquals("ending line position", 1, customTag.tagData.getEndLine());
		assertStringEquals("html", "<CUSTOM/>", customTag.toHtml());
	}

	public void testEmptyCompositeTagAnotherStyle() throws ParserException {
		createParser("<Custom></Custom>");
		CustomTag customTag = parseCustomTag(1);
		int x = customTag.getChildCount();
		assertEquals("child count", 0, customTag.getChildCount());
		assertFalse("custom tag should not be xml end tag", customTag.isEmptyXmlTag());
		assertEquals("starting loc", 0, customTag.getStartTag().elementBegin());
		assertEquals("ending loc", 7, customTag.getStartTag().elementEnd());
		assertEquals("starting line position", 1, customTag.tagData.getStartLine());
		assertEquals("ending line position", 1, customTag.tagData.getEndLine());
		assertEquals("html", "<CUSTOM></CUSTOM>", customTag.toHtml());
	}

	public void testCompositeTagWithOneTextChild() throws ParserException {
		createParser("<Custom>" + "Hello" + "</Custom>");
		CustomTag customTag = parseCustomTag(1);
		int x = customTag.getChildCount();
		assertEquals("child count", 1, customTag.getChildCount());
		assertFalse("custom tag should not be xml end tag", customTag.isEmptyXmlTag());
		assertEquals("starting loc", 0, customTag.getStartTag().elementBegin());
		assertEquals("ending loc", 7, customTag.getStartTag().elementEnd());
		assertEquals("starting line position", 1, customTag.tagData.getStartLine());
		assertEquals("ending line position", 1, customTag.tagData.getEndLine());

		Node child = customTag.childAt(0);
		assertType("child", StringNode.class, child);
		StringNode text = (StringNode) child;
		assertStringEquals("child text", "Hello", child.toPlainTextString());
	}

	public void testCompositeTagWithTagChild() throws ParserException {
		createParser("<Custom>" + "<Hello>" + "</Custom>");
		CustomTag customTag = parseCustomTag(1);
		int x = customTag.getChildCount();
		assertEquals("child count", 1, customTag.getChildCount());
		assertFalse("custom tag should not be xml end tag", customTag.isEmptyXmlTag());
		assertEquals("starting loc", 0, customTag.getStartTag().elementBegin());
		assertEquals("ending loc", 7, customTag.getStartTag().elementEnd());
		assertEquals("custom tag starting loc", 0, customTag.elementBegin());
		assertEquals("custom tag ending loc", 23, customTag.elementEnd());

		Node child = customTag.childAt(0);
		assertType("child", Tag.class, child);
		Tag tag = (Tag) child;
		assertStringEquals("child html", "<HELLO>", child.toHtml());
	}

	public void testCompositeTagWithAnotherTagChild() throws ParserException {
		createParser("<Custom>" + "<Another/>" + "</Custom>");
		parser.addScanner(new AnotherScanner());
		CustomTag customTag = parseCustomTag(1);
		int x = customTag.getChildCount();
		assertEquals("child count", 1, customTag.getChildCount());
		assertFalse("custom tag should not be xml end tag", customTag.isEmptyXmlTag());
		assertEquals("starting loc", 0, customTag.getStartTag().elementBegin());
		assertEquals("ending loc", 7, customTag.getStartTag().elementEnd());
		assertEquals("custom tag starting loc", 0, customTag.elementBegin());
		assertEquals("custom tag ending loc", 26, customTag.elementEnd());

		Node child = customTag.childAt(0);
		assertType("child", AnotherTag.class, child);
		AnotherTag tag = (AnotherTag) child;
		assertEquals("another tag start pos", 8, tag.elementBegin());
		assertEquals("another tag ending pos", 17, tag.elementEnd());

		assertEquals("custom end tag start pos", 18, customTag.getEndTag().elementBegin());
		assertStringEquals("child html", "<ANOTHER/>", child.toHtml());
	}

	public void testParseTwoCompositeTags() throws ParserException {
		createParser("<Custom>" + "</Custom>" + "<Custom/>");
		parser.addScanner(new CustomScanner());
		parseAndAssertNodeCount(2);
		assertType("tag 1", CustomTag.class, node[0]);
		assertType("tag 2", CustomTag.class, node[1]);
	}

	public void testXmlTypeCompositeTags() throws ParserException {
		createParser("<Custom>" + "<Another name=\"subtag\"/>" + "<Custom />" + "</Custom>" + "<Custom/>");
		parser.addScanner(new CustomScanner());
		parser.addScanner(new AnotherScanner());
		parseAndAssertNodeCount(2);
		assertType("first node", CustomTag.class, node[0]);
		assertType("second node", CustomTag.class, node[1]);
		CustomTag customTag = (CustomTag) node[0];
		Node node = customTag.childAt(0);
		assertType("first child", AnotherTag.class, node);
		node = customTag.childAt(1);
		assertType("second child", CustomTag.class, node);
	}

	public void testCompositeTagWithNestedTag() throws ParserException {
		createParser("<Custom>" + "<Another>" + "Hello" + "</Another>" + "<Custom/>" + "</Custom>" + "<Custom/>");
		parser.addScanner(new CustomScanner());
		parser.addScanner(new AnotherScanner());
		parseAndAssertNodeCount(2);
		assertType("first node", CustomTag.class, node[0]);
		assertType("second node", CustomTag.class, node[1]);
		CustomTag customTag = (CustomTag) node[0];
		Node node = customTag.childAt(0);
		assertType("first child", AnotherTag.class, node);
		AnotherTag anotherTag = (AnotherTag) node;
		assertEquals("another tag children count", 1, anotherTag.getChildCount());
		node = anotherTag.childAt(0);
		assertType("nested child", StringNode.class, node);
		StringNode text = (StringNode) node;
		assertEquals("text", "Hello", text.toPlainTextString());
	}

	public void testCompositeTagWithTwoNestedTags() throws ParserException {
		createParser("<Custom>" + "<Another>" + "Hello" + "</Another>" + "<unknown>" + "World" + "</unknown>"
				+ "<Custom/>" + "</Custom>" + "<Custom/>");
		parser.addScanner(new CustomScanner());
		parser.addScanner(new AnotherScanner());
		parseAndAssertNodeCount(2);
		assertType("first node", CustomTag.class, node[0]);
		assertType("second node", CustomTag.class, node[1]);
		CustomTag customTag = (CustomTag) node[0];
		assertEquals("first custom tag children count", 5, customTag.getChildCount());
		Node node = customTag.childAt(0);
		assertType("first child", AnotherTag.class, node);
		AnotherTag anotherTag = (AnotherTag) node;
		assertEquals("another tag children count", 1, anotherTag.getChildCount());
		node = anotherTag.childAt(0);
		assertType("nested child", StringNode.class, node);
		StringNode text = (StringNode) node;
		assertEquals("text", "Hello", text.toPlainTextString());
	}

	public void testErroneousCompositeTag() throws ParserException {
		createParser("<custom>");
		CustomTag customTag = parseCustomTag(1);
		int x = customTag.getChildCount();
		assertEquals("child count", 0, customTag.getChildCount());
		assertFalse("custom tag should be xml end tag", customTag.isEmptyXmlTag());
		assertEquals("starting loc", 0, customTag.getStartTag().elementBegin());
		assertEquals("ending loc", 7, customTag.getStartTag().elementEnd());
		assertEquals("starting line position", 1, customTag.tagData.getStartLine());
		assertEquals("ending line position", 1, customTag.tagData.getEndLine());
		assertStringEquals("html", "<CUSTOM></CUSTOM>", customTag.toHtml());
	}

	public void testErroneousCompositeTagWithChildren() throws ParserException {
		createParser("<custom>" + "<firstChild>" + "<secondChild>");
		CustomTag customTag = parseCustomTag(1);
		int x = customTag.getChildCount();
		assertEquals("child count", 2, customTag.getChildCount());
		assertFalse("custom tag should be xml end tag", customTag.isEmptyXmlTag());
		assertEquals("starting loc", 0, customTag.getStartTag().elementBegin());
		assertEquals("ending loc", 7, customTag.getStartTag().elementEnd());
		assertEquals("starting line position", 1, customTag.tagData.getStartLine());
		assertEquals("ending line position", 1, customTag.tagData.getEndLine());
		assertStringEquals("html", "<CUSTOM><FIRSTCHILD><SECONDCHILD></CUSTOM>", customTag.toHtml());
	}

	public void testErroneousCompositeTagWithChildrenAndLineBreak() throws ParserException {
		createParser("<custom>" + "<firstChild>\n" + "<secondChild>");
		CustomTag customTag = parseCustomTag(1);
		int x = customTag.getChildCount();
		assertEquals("child count", 2, customTag.getChildCount());
		assertFalse("custom tag should not be xml end tag", customTag.isEmptyXmlTag());
		assertEquals("starting loc", 0, customTag.getStartTag().elementBegin());
		assertEquals("ending loc", 7, customTag.getStartTag().elementEnd());
		assertEquals("starting line position", 1, customTag.tagData.getStartLine());
		assertEquals("ending line position", 2, customTag.tagData.getEndLine());
		assertStringEquals("html", "<CUSTOM><FIRSTCHILD>\r\n" + "<SECONDCHILD>" + "</CUSTOM>", customTag.toHtml());
	}

	public void testTwoConsecutiveErroneousCompositeTags() throws ParserException {
		createParser("<custom>something" + "<custom></endtag>");
		parser.addScanner(new CustomScanner(false));
		parseAndAssertNodeCount(2);
		CustomTag customTag = (CustomTag) node[0];
		int x = customTag.getChildCount();
		assertEquals("child count", 1, customTag.getChildCount());
		assertFalse("custom tag should not be xml end tag", customTag.isEmptyXmlTag());
		assertEquals("starting loc", 0, customTag.getStartTag().elementBegin());
		assertEquals("ending loc", 7, customTag.getStartTag().elementEnd());
		assertEquals("ending loc of custom tag", 25, customTag.elementEnd());
		assertEquals("starting line position", 1, customTag.tagData.getStartLine());
		assertEquals("ending line position", 1, customTag.tagData.getEndLine());
		assertStringEquals("first custom tag", "<CUSTOM>something</CUSTOM>", customTag.toHtml());
		customTag = (CustomTag) node[1];
		assertStringEquals("second custom tag", "<CUSTOM></ENDTAG></CUSTOM>", customTag.toHtml());
	}

	public void testCompositeTagWithErroneousAnotherTagAndLineBreak() throws ParserException {
		createParser("<another>" + "<custom>\n" + "</custom>");
		parser.addScanner(new AnotherScanner());
		parser.addScanner(new CustomScanner());
		parseAndAssertNodeCount(2);
		AnotherTag anotherTag = (AnotherTag) node[0];
		assertEquals("another tag child count", 0, anotherTag.getChildCount());

		CustomTag customTag = (CustomTag) node[1];
		int x = customTag.getChildCount();
		assertEquals("child count", 0, customTag.getChildCount());
		assertFalse("custom tag should not be xml end tag", customTag.isEmptyXmlTag());
		assertEquals("starting loc", 9, customTag.getStartTag().elementBegin());
		assertEquals("ending loc", 16, customTag.getStartTag().elementEnd());
		assertEquals("starting line position", 1, customTag.tagData.getStartLine());
		assertEquals("ending line position", 2, customTag.tagData.getEndLine());
		assertStringEquals("another tag html", "<ANOTHER></ANOTHER>", anotherTag.toHtml());
		assertStringEquals("custom tag html", "<CUSTOM>\r\n</CUSTOM>", customTag.toHtml());
	}

	public void testCompositeTagWithErroneousAnotherTag() throws ParserException {
		createParser("<custom>" + "<another>" + "</custom>");
		parser.addScanner(new AnotherScanner(true));
		CustomTag customTag = parseCustomTag(1);
		int x = customTag.getChildCount();
		assertEquals("child count", 1, customTag.getChildCount());
		assertFalse("custom tag should be xml end tag", customTag.isEmptyXmlTag());
		assertEquals("starting loc", 0, customTag.getStartTag().elementBegin());
		assertEquals("ending loc", 7, customTag.getStartTag().elementEnd());
		AnotherTag anotherTag = (AnotherTag) customTag.childAt(0);
		assertEquals("another tag ending loc", 26, anotherTag.elementEnd());
		assertEquals("starting line position", 1, customTag.tagData.getStartLine());
		assertEquals("ending line position", 1, customTag.tagData.getEndLine());
		assertStringEquals("html", "<CUSTOM><ANOTHER></ANOTHER></CUSTOM>", customTag.toHtml());
	}

	public void testCompositeTagWithDeadlock() throws ParserException {
		createParser("<custom>" + "<another>something" + "</custom>" + "<custom>" + "<another>else</another>"
				+ "</custom>");
		parser.addScanner(new AnotherScanner(true));
		CustomTag customTag = parseCustomTag(2);
		int x = customTag.getChildCount();
		assertEquals("child count", 1, customTag.getChildCount());
		assertFalse("custom tag should not be xml end tag", customTag.isEmptyXmlTag());
		assertEquals("starting loc", 0, customTag.getStartTag().elementBegin());
		assertEquals("ending loc", 7, customTag.getStartTag().elementEnd());
		assertEquals("starting line position", 1, customTag.tagData.getStartLine());
		assertEquals("ending line position", 1, customTag.tagData.getEndLine());
		AnotherTag anotherTag = (AnotherTag) customTag.childAt(0);
		assertEquals("anotherTag child count", 1, anotherTag.getChildCount());
		StringNode stringNode = (StringNode) anotherTag.childAt(0);
		assertStringEquals("anotherTag child text", "something", stringNode.toPlainTextString());
		assertStringEquals("first custom tag html", "<CUSTOM><ANOTHER>something</ANOTHER></CUSTOM>", customTag.toHtml());
		customTag = (CustomTag) node[1];
		assertStringEquals("second custom tag html", "<CUSTOM><ANOTHER>else</ANOTHER></CUSTOM>", customTag.toHtml());
	}

	public void testCompositeTagCorrectionWithSplitLines() throws ParserException {
		createParser("<custom>" + "<another><abcdefg>\n" + "</custom>");
		parser.addScanner(new AnotherScanner(true));
		CustomTag customTag = parseCustomTag(1);
		int x = customTag.getChildCount();
		assertEquals("child count", 1, customTag.getChildCount());
		assertFalse("custom tag should not be xml end tag", customTag.isEmptyXmlTag());
		assertEquals("starting loc", 0, customTag.getStartTag().elementBegin());
		assertEquals("ending loc", 7, customTag.getStartTag().elementEnd());
		AnotherTag anotherTag = (AnotherTag) customTag.childAt(0);
		assertEquals("anotherTag child count", 1, anotherTag.getChildCount());
		assertEquals("anotherTag end loc", 9, anotherTag.elementEnd());
		assertEquals("custom end tag begin loc", 10, customTag.getEndTag().elementBegin());
		assertEquals("custom end tag end loc", 8, customTag.getEndTag().elementEnd());
	}

	public void testCompositeTagWithSelfChildren() throws ParserException {
		createParser("<custom>" + "<custom>something</custom>" + "</custom>");
		parser.addScanner(new CustomScanner(false));
		parser.addScanner(new AnotherScanner());
		parseAndAssertNodeCount(3);

		CustomTag customTag = (CustomTag) node[0];
		int x = customTag.getChildCount();
		assertEquals("child count", 0, customTag.getChildCount());
		assertFalse("custom tag should not be xml end tag", customTag.isEmptyXmlTag());

		assertStringEquals("first custom tag html", "<CUSTOM></CUSTOM>", customTag.toHtml());
		customTag = (CustomTag) node[1];
		assertStringEquals("first custom tag html", "<CUSTOM>something</CUSTOM>", customTag.toHtml());
		EndTag endTag = (EndTag) node[2];
		assertStringEquals("first custom tag html", "</CUSTOM>", endTag.toHtml());
	}

	public void testParentConnections() throws ParserException {
		createParser("<custom>" + "<custom>something</custom>" + "</custom>");
		parser.addScanner(new CustomScanner(false));
		parser.addScanner(new AnotherScanner());
		parseAndAssertNodeCount(3);

		CustomTag customTag = (CustomTag) node[0];

		assertStringEquals("first custom tag html", "<CUSTOM></CUSTOM>", customTag.toHtml());
		assertNull("first custom tag should have no parent", customTag.getParent());

		customTag = (CustomTag) node[1];
		assertStringEquals("first custom tag html", "<CUSTOM>something</CUSTOM>", customTag.toHtml());
		assertNull("second custom tag should have no parent", customTag.getParent());

		Node firstChild = customTag.childAt(0);
		assertType("firstChild", StringNode.class, firstChild);
		CompositeTag parent = firstChild.getParent();
		assertNotNull("first child parent should not be null", parent);
		assertSame("parent and custom tag should be the same", customTag, parent);

		EndTag endTag = (EndTag) node[2];
		assertStringEquals("first custom tag html", "</CUSTOM>", endTag.toHtml());
		assertNull("end tag should have no parent", endTag.getParent());

	}

	public void testUrlBeingProvidedToCreateTag() throws ParserException {
		createParser("<Custom/>", "http://www.yahoo.com");

		parser.addScanner(new CustomScanner() {
			public Tag createTag(TagData tagData, CompositeTagData compositeTagData) {
				url = tagData.getUrlBeingParsed();
				return super.createTag(tagData, compositeTagData);
			}
		});
		parseAndAssertNodeCount(1);
		assertStringEquals("url", "http://www.yahoo.com", url);
	}

	public void testComplexNesting() throws ParserException {
		createParser("<custom>" + "<custom>" + "<another>" + "</custom>" + "<custom>" + "<another>" + "</custom>"
				+ "</custom>");
		parser.addScanner(new CustomScanner());
		parser.addScanner(new AnotherScanner(false));
		parseAndAssertNodeCount(1);
		assertType("root node", CustomTag.class, node[0]);
		CustomTag root = (CustomTag) node[0];
		assertNodeCount("child count", 2, root.getChildrenAsNodeArray());
		Node child = root.childAt(0);
		assertType("child", CustomTag.class, child);
		CustomTag customChild = (CustomTag) child;
		assertNodeCount("grand child count", 1, customChild.getChildrenAsNodeArray());
		Node grandchild = customChild.childAt(0);
		assertType("grandchild", AnotherTag.class, grandchild);
	}

	public void testDisallowedChildren() throws ParserException {
		createParser("<custom>\n" + "Hello" + "<custom>\n" + "World" + "<custom>\n" + "Hey\n" + "</custom>");
		parser.addScanner(new CustomScanner(false));
		parseAndAssertNodeCount(3);
		for (int i = 0; i < nodeCount; i++) {
			assertType("node " + i, CustomTag.class, node[i]);
		}
	}

	public static class CustomScanner extends CompositeTagScanner {
		private static final String MATCH_NAME[] = { "CUSTOM" };

		public CustomScanner() {
			this(true);
		}

		public CustomScanner(boolean selfChildrenAllowed) {
			super("", MATCH_NAME, new String[] {}, selfChildrenAllowed);
		}

		public String[] getID() {
			return MATCH_NAME;
		}

		public Tag createTag(TagData tagData, CompositeTagData compositeTagData) {
			return new CustomTag(tagData, compositeTagData);
		}
	}

	public static class AnotherScanner extends CompositeTagScanner {
		private static final String MATCH_NAME[] = { "ANOTHER" };

		public AnotherScanner() {
			super("", MATCH_NAME, new String[] { "CUSTOM" });
		}

		public AnotherScanner(boolean acceptCustomTagsButDontAcceptCustomEndTags) {
			super("", MATCH_NAME, new String[] {}, new String[] { "CUSTOM" }, true);
		}

		public String[] getID() {
			return MATCH_NAME;
		}

		public Tag createTag(TagData tagData, CompositeTagData compositeTagData) {
			return new AnotherTag(tagData, compositeTagData);
		}

		protected boolean isBrokenTag() {
			return false;
		}

	}

	public static class CustomTag extends CompositeTag {
		public TagData tagData;

		public CustomTag(TagData tagData, CompositeTagData compositeTagData) {
			super(tagData, compositeTagData);
			this.tagData = tagData;
		}
	}

	public static class AnotherTag extends CompositeTag {
		public AnotherTag(TagData tagData, CompositeTagData compositeTagData) {
			super(tagData, compositeTagData);
		}
	}

}
