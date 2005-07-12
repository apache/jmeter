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
package org.htmlparser.tests.parserHelperTests;

import org.htmlparser.Parser;
import org.htmlparser.RemarkNode;
import org.htmlparser.StringNode;
import org.htmlparser.scanners.LinkScanner;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.tags.MetaTag;
import org.htmlparser.tests.ParserTestCase;
import org.htmlparser.util.ParserException;

public class StringParserTest extends ParserTestCase {

	public StringParserTest(String name) {
		super(name);
	}

	/**
	 * The bug being reproduced is this : <BR>
	 * &lt;HTML&gt;&lt;HEAD&gt;&lt;TITLE&gt;Google&lt;/TITLE&gt; <BR>
	 * The above line is incorrectly parsed in that, the text Google is missed.
	 * The presence of this bug is typically when some tag is identified before
	 * the string node is. (usually seen with the end tag). The bug lies in
	 * NodeReader.readElement(). Creation date: (6/17/2001 4:01:06 PM)
	 */
	public void testStringNodeBug1() throws ParserException {
		createParser("<HTML><HEAD><TITLE>Google</TITLE>");
		parseAndAssertNodeCount(5);
		// The fourth node should be a HTMLStringNode- with the text - Google
		assertTrue("Fourth node should be a HTMLStringNode", node[3] instanceof StringNode);
		StringNode stringNode = (StringNode) node[3];
		assertEquals("Text of the StringNode", "Google", stringNode.getText());
	}

	/**
	 * Bug reported by Kaarle Kaila of Nokia<br>
	 * For the following HTML : view these documents, you must have &lt;A
	 * href='http://www.adobe.com'&gt;Adobe <br>
	 * Acrobat Reader&lt;/A&gt; installed on your computer.<br>
	 * The first string before the link is not identified, and the space after
	 * the link is also not identified Creation date: (8/2/2001 2:07:32 AM)
	 */
	public void testStringNodeBug2() throws ParserException {
		// Register the link scanner

		createParser("view these documents, you must have <A href='http://www.adobe.com'>Adobe \n"
				+ "Acrobat Reader</A> installed on your computer.");
		Parser.setLineSeparator("\r\n");
		parser.addScanner(new LinkScanner("-l"));
		parseAndAssertNodeCount(3);
		// The first node should be a HTMLStringNode- with the text - view these
		// documents, you must have
		assertTrue("First node should be a HTMLStringNode", node[0] instanceof StringNode);
		StringNode stringNode = (StringNode) node[0];
		assertEquals("Text of the StringNode", "view these documents, you must have ", stringNode.getText());
		assertTrue("Second node should be a link node", node[1] instanceof LinkTag);
		LinkTag linkNode = (LinkTag) node[1];
		assertEquals("Link is", "http://www.adobe.com", linkNode.getLink());
		assertEquals("Link text is", "Adobe \r\nAcrobat Reader", linkNode.getLinkText());

		assertTrue("Third node should be a string node", node[2] instanceof StringNode);
		StringNode stringNode2 = (StringNode) node[2];
		assertEquals("Contents of third node", " installed on your computer.", stringNode2.getText());
	}

	/**
	 * Bug reported by Roger Sollberger<br>
	 * For the following HTML : &lt;a href="http://asgard.ch"&gt;[&lt; ASGARD
	 * &gt;&lt;/a&gt;&lt;br&gt; The string node is not correctly identified
	 */
	public void testTagCharsInStringNode() throws ParserException {
		createParser("<a href=\"http://asgard.ch\">[> ASGARD <]</a>");
		parser.addScanner(new LinkScanner("-l"));
		parseAndAssertNodeCount(1);
		assertTrue("Node identified must be a link tag", node[0] instanceof LinkTag);
		LinkTag linkTag = (LinkTag) node[0];
		assertEquals("[> ASGARD <]", linkTag.getLinkText());
		assertEquals("http://asgard.ch", linkTag.getLink());
	}

	public void testToPlainTextString() throws ParserException {
		createParser("<HTML><HEAD><TITLE>This is the Title</TITLE></HEAD><BODY>Hello World, this is the HTML Parser</BODY></HTML>");
		parseAndAssertNodeCount(10);
		assertTrue("Fourth Node identified must be a string node", node[3] instanceof StringNode);
		StringNode stringNode = (StringNode) node[3];
		assertEquals("First String Node", "This is the Title", stringNode.toPlainTextString());
		assertTrue("Eighth Node identified must be a string node", node[7] instanceof StringNode);
		stringNode = (StringNode) node[7];
		assertEquals("Second string node", "Hello World, this is the HTML Parser", stringNode.toPlainTextString());
	}

	public void testToHTML() throws ParserException {
		createParser("<HTML><HEAD><TITLE>This is the Title</TITLE></HEAD><BODY>Hello World, this is the HTML Parser</BODY></HTML>");
		parseAndAssertNodeCount(10);
		assertTrue("Fourth Node identified must be a string node", node[3] instanceof StringNode);
		StringNode stringNode = (StringNode) node[3];
		assertEquals("First String Node", "This is the Title", stringNode.toHtml());
		assertTrue("Eighth Node identified must be a string node", node[7] instanceof StringNode);
		stringNode = (StringNode) node[7];
		assertEquals("Second string node", "Hello World, this is the HTML Parser", stringNode.toHtml());
	}

	public void testEmptyLines() throws ParserException {
		createParser("David Nirenberg (Center for Advanced Study in the Behavorial Sciences, Stanford).<br>\n"
				+ "                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      \n"
				+ "<br>");
		parseAndAssertNodeCount(4);
		assertTrue("Third Node identified must be a string node", node[2] instanceof StringNode);
	}

	/**
	 * This is a bug reported by John Zook (586222), where the first few chars
	 * before a remark is being missed, if its on the same line.
	 */
	public void testStringBeingMissedBug() throws ParserException {
		createParser("Before Comment <!-- Comment --> After Comment");
		parseAndAssertNodeCount(3);
		assertTrue("First node should be HTMLStringNode", node[0] instanceof StringNode);
		assertTrue("Second node should be HTMLRemarkNode", node[1] instanceof RemarkNode);
		assertTrue("Third node should be HTMLStringNode", node[2] instanceof StringNode);
		StringNode stringNode = (StringNode) node[0];
		assertEquals("First String node contents", "Before Comment ", stringNode.getText());
		StringNode stringNode2 = (StringNode) node[2];
		assertEquals("Second String node contents", " After Comment", stringNode2.getText());
		RemarkNode remarkNode = (RemarkNode) node[1];
		assertEquals("Remark Node contents", " Comment ", remarkNode.getText());

	}

	/**
	 * Based on a bug report submitted by Cedric Rosa, if the last line contains
	 * a single character, StringNode does not return the string node correctly.
	 */
	public void testLastLineWithOneChar() throws ParserException {
		createParser("a");
		parseAndAssertNodeCount(1);
		assertTrue("First node should be HTMLStringNode", node[0] instanceof StringNode);
		StringNode stringNode = (StringNode) node[0];
		assertEquals("First String node contents", "a", stringNode.getText());
	}

	public void testStringWithEmptyLine() throws ParserException {
		createParser("a\n\nb");
		parseAndAssertNodeCount(1);
		assertTrue("First node should be HTMLStringNode", node[0] instanceof StringNode);
		StringNode stringNode = (StringNode) node[0];
		assertStringEquals("First String node contents", "a\r\n\r\nb", stringNode.getText());
	}

	/**
	 * An attempt to reproduce bug 677176, which passes.
	 * 
	 * @throws Exception
	 */
	public void testStringParserBug() throws Exception {
		createParser("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 " + "Transitional//EN\">" + "<html>" + "<head>"
				+ "<title>Untitled Document</title>" + "<meta http-equiv=\"Content-Type\" content=\"text/html; "
				+ "charset=iso-8859-1\">" + "</head>" + "<script language=\"JavaScript\" type=\"text/JavaScript\">"
				+ "// if this fails, output a 'hello' " + "if (true) " + "{ " + "//something good... " + "} "
				+ "</script>" + "<body>" + "</body>" + "</html>");
		parser.registerScanners();
		parseAndAssertNodeCount(10);
		assertType("fourth node", MetaTag.class, node[4]);
		MetaTag metaTag = (MetaTag) node[4];

		assertStringEquals("content", "text/html; charset=iso-8859-1", metaTag.getAttribute("CONTENT"));
	}

	public void testStringWithLineBreaks() throws Exception {
		createParser("Testing &\nRefactoring");
		parseAndAssertNodeCount(1);
		assertType("first node", StringNode.class, node[0]);
		StringNode stringNode = (StringNode) node[0];
		assertStringEquals("text", "Testing &\r\nRefactoring", stringNode.toPlainTextString());
	}

}
