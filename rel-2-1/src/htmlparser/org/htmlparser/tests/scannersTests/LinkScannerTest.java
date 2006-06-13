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
import org.htmlparser.Parser;
import org.htmlparser.StringNode;
import org.htmlparser.scanners.LinkScanner;
import org.htmlparser.tags.EndTag;
import org.htmlparser.tags.ImageTag;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.tags.Tag;
import org.htmlparser.tags.data.TagData;
import org.htmlparser.tests.ParserTestCase;
import org.htmlparser.util.ParserException;
import org.htmlparser.util.SimpleNodeIterator;

public class LinkScannerTest extends ParserTestCase {
	public LinkScannerTest(String name) {
		super(name);
	}

	public void testAccessKey() throws ParserException {
		createParser("<a href=\"http://www.kizna.com/servlets/SomeServlet?name=Sam Joseph\" accessKey=1>Click Here</A>");
		parser.addScanner(new LinkScanner("-l"));
		parseAndAssertNodeCount(1);
		assertTrue("The node should be a link tag", node[0] instanceof LinkTag);
		LinkTag linkTag = (LinkTag) node[0];
		assertEquals("Link URL of link tag", "http://www.kizna.com/servlets/SomeServlet?name=Sam Joseph", linkTag
				.getLink());
		assertEquals("Link Text of link tag", "Click Here", linkTag.getLinkText());
		assertEquals("Access key", "1", linkTag.getAccessKey());
	}

	public void testErroneousLinkBug() throws ParserException {
		createParser("<p>Site Comments?<br>" + "<a href=\"mailto:sam@neurogrid.com?subject=Site Comments\">"
				+ "Mail Us" + "<a>" + "</p>");
		parser.registerScanners();
		parseAndAssertNodeCount(6);
		// The first node should be a Tag
		assertTrue("First node should be a Tag", node[0] instanceof Tag);
		// The second node should be a HTMLStringNode
		assertTrue("Second node should be a HTMLStringNode", node[1] instanceof StringNode);
		StringNode stringNode = (StringNode) node[1];
		assertEquals("Text of the StringNode", "Site Comments?", stringNode.getText());
		assertTrue("Third node should be a tag", node[2] instanceof Tag);

	}

	/**
	 * Test case based on a report by Raghavender Srimantula, of the parser
	 * giving out of memory exceptions. Found to occur on the following piece of
	 * html
	 * 
	 * <pre>
	 * 
	 *  &lt;a href=s/8741&gt;&lt;img src=&quot;http://us.i1.yimg.com/us.yimg.com/i/i16/mov_popc.gif&quot; height=16 width=16 border=0&gt;&lt;/img&gt;
	 * </td><td nowrap>
	 *   
	 *  &lt;a href=s/7509&gt;
	 *  
	 * </pre>
	 */
	public void testErroneousLinkBugFromYahoo2() throws ParserException {
		createParser("<td>" + "<a href=s/8741>"
				+ "<img src=\"http://us.i1.yimg.com/us.yimg.com/i/i16/mov_popc.gif\" height=16 width=16 border=0>"
				+ "</td>" + "<td nowrap> &nbsp;\n" + "<a href=s/7509><b>Yahoo! Movies</b></a>" + "</td>",
				"http://www.yahoo.com");
		parser.registerScanners();
		Node linkNodes[] = parser.extractAllNodesThatAre(LinkTag.class);

		assertEquals("number of links", 2, linkNodes.length);
		LinkTag linkTag = (LinkTag) linkNodes[0];
		assertStringEquals("Link", "http://www.yahoo.com/s/8741", linkTag.getLink());
		// Verify the link data
		assertStringEquals("Link Text", "", linkTag.getLinkText());
		// Verify the reconstruction html
		assertStringEquals(
				"toHTML",
				"<A HREF=\"s/8741\"><IMG BORDER=\"0\" WIDTH=\"16\" SRC=\"http://us.i1.yimg.com/us.yimg.com/i/i16/mov_popc.gif\" HEIGHT=\"16\"></A>",
				linkTag.toHtml());
	}

	/**
	 * Test case based on a report by Raghavender Srimantula, of the parser
	 * giving out of memory exceptions. Found to occur on the following piece of
	 * html
	 * 
	 * <pre>
	 * 
	 *  &lt;a href=s/8741&gt;&lt;img src=&quot;http://us.i1.yimg.com/us.yimg.com/i/i16/mov_popc.gif&quot; height=16 width=16 border=0&gt;&lt;/img&gt;This is test
	 *  &lt;a href=s/7509&gt;
	 *  
	 * </pre>
	 */
	public void testErroneousLinkBugFromYahoo() throws ParserException {
		createParser("<a href=s/8741>" + "<img src=\"http://us.i1.yimg.com/us.yimg.com/i/i16/mov_popc.gif\" "
				+ "height=16 " + "width=16 " + "border=0>" + "This is a test\n" + "<a href=s/7509>"
				+ "<b>Yahoo! Movies</b>" + "</a>", "http://www.yahoo.com");

		parser.registerScanners();
		parseAndAssertNodeCount(2);
		// The first node should be a Tag
		assertTrue("First node should be a HTMLLinkTag", node[0] instanceof LinkTag);
		// The second node should be a HTMLStringNode
		assertTrue("Second node should be a HTMLLinkTag", node[1] instanceof LinkTag);
		LinkTag linkTag = (LinkTag) node[0];
		assertEquals("Link", "http://www.yahoo.com/s/8741", linkTag.getLink());
		// Verify the link data
		assertEquals("Link Text", "This is a test\r\n", linkTag.getLinkText());
		// Verify the reconstruction html
		assertStringEquals(
				"toHTML()",
				"<A HREF=\"s/8741\"><IMG BORDER=\"0\" WIDTH=\"16\" SRC=\"http://us.i1.yimg.com/us.yimg.com/i/i16/mov_popc.gif\" HEIGHT=\"16\">This is a test\r\n</A>",
				linkTag.toHtml());
	}

	public void testEvaluate() {
		LinkScanner scanner = new LinkScanner("-l");
		boolean retVal = scanner.evaluate("   a href ", null);
		assertEquals("Evaluation of the Link tag", new Boolean(true), new Boolean(retVal));
	}

	/**
	 * This is the reproduction of a bug which causes a null pointer exception
	 */
	public void testExtractLinkInvertedCommasBug() throws ParserException {
		String tagContents = "a href=r/anorth/top.html";
		Tag tag = new Tag(new TagData(0, 0, tagContents, ""));
		String url = "c:\\cvs\\html\\binaries\\yahoo.htm";
		LinkScanner scanner = new LinkScanner("-l");
		assertEquals("Extracted Link", "r/anorth/top.html", scanner.extractLink(tag, url));
	}

	/**
	 * This is the reproduction of a bug which produces multiple text copies.
	 */
	public void testExtractLinkInvertedCommasBug2() throws ParserException {
		createParser("<a href=\"http://cbc.ca/artsCanada/stories/greatnorth271202\" class=\"lgblacku\">Vancouver schools plan 'Great Northern Way'</a>");
		parser.addScanner(new LinkScanner("-l"));
		parseAndAssertNodeCount(1);
		assertTrue("The node should be a link tag", node[0] instanceof LinkTag);
		LinkTag linkTag = (LinkTag) node[0];
		assertStringEquals("Extracted Text", "Vancouver schools plan 'Great Northern Way'", linkTag.getLinkText());
	}

	/**
	 * Bug pointed out by Sam Joseph (sam@neurogrid.net) Links with spaces in
	 * them will get their spaces absorbed
	 */
	public void testLinkSpacesBug() throws ParserException {
		createParser("<a href=\"http://www.kizna.com/servlets/SomeServlet?name=Sam Joseph\">Click Here</A>");
		parser.addScanner(new LinkScanner("-l"));
		parseAndAssertNodeCount(1);
		assertTrue("The node should be a link tag", node[0] instanceof LinkTag);
		LinkTag linkTag = (LinkTag) node[0];
		assertEquals("Link URL of link tag", "http://www.kizna.com/servlets/SomeServlet?name=Sam Joseph", linkTag
				.getLink());
		assertEquals("Link Text of link tag", "Click Here", linkTag.getLinkText());
	}

	/**
	 * Bug reported by Raj Sharma,5-Apr-2002, upon parsing
	 * http://www.samachar.com, the entire page could not be picked up. The
	 * problem was occurring after parsing a particular link after which the
	 * parsing would not proceed. This link was spread over three lines. The bug
	 * has been reproduced and fixed.
	 */
	public void testMultipleLineBug() throws ParserException {
		createParser("<LI><font color=\"FF0000\" size=-1><b>Tech Samachar:</b></font><a \n"
				+ "href=\"http://ads.samachar.com/bin/redirect/tech.txt?http://www.samachar.com/tech\n"
				+ "nical.html\"> Journalism 3.0</a> by Rajesh Jain");
		Parser.setLineSeparator("\r\n");
		parser.addScanner(new LinkScanner("-l"));
		parseAndAssertNodeCount(8);
		assertTrue("Seventh node should be a link tag", node[6] instanceof LinkTag);
		LinkTag linkTag = (LinkTag) node[6];
		String exp = new String("http://ads.samachar.com/bin/redirect/tech.txt?http://www.samachar.com/technical.html");
		// assertEquals("Length of link tag",exp.length(),
		// linkTag.getLink().length());
		assertStringEquals("Link URL of link tag", exp, linkTag.getLink());
		assertEquals("Link Text of link tag", " Journalism 3.0", linkTag.getLinkText());
		assertTrue("Eight node should be a string node", node[7] instanceof StringNode);
		StringNode stringNode = (StringNode) node[7];
		assertEquals("String node contents", " by Rajesh Jain", stringNode.getText());
	}

	public void testRelativeLinkScan() throws ParserException {
		createParser("<A HREF=\"mytest.html\"> Hello World</A>", "http://www.yahoo.com");
		// Register the image scanner
		parser.addScanner(new LinkScanner("-l"));
		parseAndAssertNodeCount(1);
		assertTrue("Node identified should be HTMLLinkTag", node[0] instanceof LinkTag);
		LinkTag linkTag = (LinkTag) node[0];
		assertEquals("Expected Link", "http://www.yahoo.com/mytest.html", linkTag.getLink());
	}

	public void testRelativeLinkScan2() throws ParserException {
		createParser("<A HREF=\"abc/def/mytest.html\"> Hello World</A>", "http://www.yahoo.com");
		// Register the image scanner
		parser.addScanner(new LinkScanner("-l"));
		parseAndAssertNodeCount(1);
		assertTrue("Node identified should be HTMLLinkTag", node[0] instanceof LinkTag);
		LinkTag linkTag = (LinkTag) node[0];
		assertStringEquals("Expected Link", "http://www.yahoo.com/abc/def/mytest.html", linkTag.getLink());
	}

	public void testRelativeLinkScan3() throws ParserException {
		createParser("<A HREF=\"../abc/def/mytest.html\"> Hello World</A>", "http://www.yahoo.com/ghi");
		// Register the image scanner
		parser.addScanner(new LinkScanner("-l"));
		parseAndAssertNodeCount(1);
		assertTrue("Node identified should be HTMLLinkTag", node[0] instanceof LinkTag);
		LinkTag linkTag = (LinkTag) node[0];
		assertStringEquals("Expected Link", "http://www.yahoo.com/abc/def/mytest.html", linkTag.getLink());
	}

	/**
	 * Test scan with data which is of diff nodes type
	 */
	public void testScan() throws ParserException {
		createParser("<A HREF=\"mytest.html\"><IMG SRC=\"abcd.jpg\">Hello World</A>", "http://www.yahoo.com");
		// Register the image scanner
		LinkScanner linkScanner = new LinkScanner("-l");
		parser.addScanner(linkScanner);
		parser.addScanner(linkScanner.createImageScanner("-i"));

		parseAndAssertNodeCount(1);
		assertTrue("Node should be a link node", node[0] instanceof LinkTag);

		LinkTag linkTag = (LinkTag) node[0];
		// Get the link data and cross-check
		Node[] dataNode = new Node[10];
		int i = 0;
		for (SimpleNodeIterator e = linkTag.children(); e.hasMoreNodes();) {
			dataNode[i++] = e.nextNode();
		}
		assertEquals("Number of data nodes", new Integer(2), new Integer(i));
		assertTrue("First data node should be an Image Node", dataNode[0] instanceof ImageTag);
		assertTrue("Second data node shouls be a String Node", dataNode[1] instanceof StringNode);

		// Check the contents of each data node
		ImageTag imageTag = (ImageTag) dataNode[0];
		assertEquals("Image URL", "http://www.yahoo.com/abcd.jpg", imageTag.getImageURL());
		StringNode stringNode = (StringNode) dataNode[1];
		assertEquals("String Contents", "Hello World", stringNode.getText());
	}

	public void testReplaceFaultyTagWithEndTag() throws ParserException {
		String currentLine = "<p>Site Comments?<br><a href=\"mailto:sam@neurogrid.com?subject=Site Comments\">Mail Us<a></p>";
		Tag tag = new Tag(new TagData(85, 87, "a", currentLine));
		LinkScanner linkScanner = new LinkScanner();
		String newLine = linkScanner.replaceFaultyTagWithEndTag(tag, currentLine);
		assertEquals("Expected replacement",
				"<p>Site Comments?<br><a href=\"mailto:sam@neurogrid.com?subject=Site Comments\">Mail Us</A></p>",
				newLine);
	}

	public void testInsertEndTagBeforeTag() throws ParserException {
		String currentLine = "<a href=s/7509><b>Yahoo! Movies</b></a>";
		Tag tag = new Tag(new TagData(0, 14, "a href=s/7509", currentLine));
		LinkScanner linkScanner = new LinkScanner();
		String newLine = linkScanner.insertEndTagBeforeNode(tag, currentLine);
		assertEquals("Expected insertion", "</A><a href=s/7509><b>Yahoo! Movies</b></a>", newLine);
	}

	/**
	 * A bug in the freshmeat page - really bad html tag -
	 * &lt;A&gt;Revision&lt;\a&gt; Reported by Mazlan Mat
	 */
	public void testFreshMeatBug() throws ParserException {
		createParser("<a>Revision</a>", "http://www.yahoo.com");
		// Register the image scanner
		parser.addScanner(new LinkScanner("-l"));

		parseAndAssertNodeCount(3);
		assertTrue("Node 0 should be a tag", node[0] instanceof Tag);
		Tag tag = (Tag) node[0];
		assertEquals("Tag Contents", "a", tag.getText());
		assertTrue("Node 1 should be a string node", node[1] instanceof StringNode);
		StringNode stringNode = (StringNode) node[1];
		assertEquals("StringNode Contents", "Revision", stringNode.getText());
		assertTrue("Node 2 should be a string node", node[2] instanceof EndTag);
		EndTag endTag = (EndTag) node[2];
		assertEquals("End Tag Contents", "a", endTag.getText());
	}

	/**
	 * Test suggested by Cedric Rosa A really bad link tag sends parser into
	 * infinite loop
	 */
	public void testBrokenLink() throws ParserException {
		createParser("<a href=\"faq.html\">" + "<br>\n" + "<img src=\"images/46revues.gif\" " + "width=\"100\" "
				+ "height=\"46\" " + "border=\"0\" " + "alt=\"Rejoignez revues.org!\" " + "align=\"middle\">",
				"http://www.yahoo.com");
		// Register the image scanner
		parser.addScanner(new LinkScanner("-l"));

		parseAndAssertNodeCount(1);
		assertTrue("Node 0 should be a link tag", node[0] instanceof LinkTag);
		LinkTag linkTag = (LinkTag) node[0];
		assertNotNull(linkTag.toString());
	}

	public void testLinkDataContents() throws ParserException {
		createParser(
				"<a href=\"http://transfer.go.com/cgi/atransfer.pl?goto=http://www.signs.movies.com&name=114332&srvc=nws&context=283&guid=4AD5723D-C802-4310-A388-0B24E1A79689\" target=\"_new\"><img src=\"http://ad.abcnews.com/ad/sponsors/buena_vista_pictures/bvpi-ban0003.gif\" width=468 height=60 border=\"0\" alt=\"See Signs in Theaters 8-2 - Starring Mel Gibson\" align=><font face=\"verdana,arial,helvetica\" SIZE=\"1\"><b></b></font></a>",
				"http://transfer.go.com");
		// Register the image scanner
		LinkScanner linkScanner = new LinkScanner("-l");
		parser.addScanner(linkScanner);
		parser.addScanner(linkScanner.createImageScanner("-i"));

		parseAndAssertNodeCount(1);
		assertTrue("Node 0 should be a link tag", node[0] instanceof LinkTag);
		LinkTag linkTag = (LinkTag) node[0];
		assertEquals(
				"Link URL",
				"http://transfer.go.com/cgi/atransfer.pl?goto=http://www.signs.movies.com&name=114332&srvc=nws&context=283&guid=4AD5723D-C802-4310-A388-0B24E1A79689",
				linkTag.getLink());
		assertEquals("Link Text", "", linkTag.getLinkText());
		Node[] containedNodes = new Node[10];
		int i = 0;
		for (SimpleNodeIterator e = linkTag.children(); e.hasMoreNodes();) {
			containedNodes[i++] = e.nextNode();
		}
		assertEquals("There should be 5 contained nodes in the link tag", 5, i);
		assertTrue("First contained node should be an image tag", containedNodes[0] instanceof ImageTag);
		ImageTag imageTag = (ImageTag) containedNodes[0];
		assertEquals("Image Location", "http://ad.abcnews.com/ad/sponsors/buena_vista_pictures/bvpi-ban0003.gif",
				imageTag.getImageURL());
		assertEquals("Image Height", "60", imageTag.getAttribute("HEIGHT"));
		assertEquals("Image Width", "468", imageTag.getAttribute("WIDTH"));
		assertEquals("Image Border", "0", imageTag.getAttribute("BORDER"));
		assertEquals("Image Alt", "See Signs in Theaters 8-2 - Starring Mel Gibson", imageTag.getAttribute("ALT"));
		assertTrue("Second contained node should be Tag", containedNodes[1] instanceof Tag);
		Tag tag1 = (Tag) containedNodes[1];
		assertEquals("Tag Contents", "font face=\"verdana,arial,helvetica\" SIZE=\"1\"", tag1.getText());
		assertTrue("Third contained node should be Tag", containedNodes[2] instanceof Tag);
		Tag tag2 = (Tag) containedNodes[2];
		assertEquals("Tag Contents", "b", tag2.getText());
		assertTrue("Fourth contained node should be HTMLEndTag", containedNodes[3] instanceof EndTag);
		EndTag endTag1 = (EndTag) containedNodes[3];
		assertEquals("Fourth Tag contents", "b", endTag1.getText());
		assertTrue("Fifth contained node should be HTMLEndTag", containedNodes[4] instanceof EndTag);
		EndTag endTag2 = (EndTag) containedNodes[4];
		assertEquals("Fifth Tag contents", "font", endTag2.getText());

	}

	public void testBaseRefLink() throws ParserException {
		createParser("<html>\n" + "<head>\n" + "<TITLE>test page</TITLE>\n" + "<BASE HREF=\"http://www.abc.com/\">\n"
				+ "<a href=\"home.cfm\">Home</a>\n" + "...\n" + "</html>", "http://transfer.go.com");
		// Register the image scanner
		parser.registerScanners();
		parseAndAssertNodeCount(7);
		assertTrue("Node 4 should be a link tag", node[4] instanceof LinkTag);
		LinkTag linkTag = (LinkTag) node[4];
		assertEquals("Resolved Link", "http://www.abc.com/home.cfm", linkTag.getLink());
		assertEquals("Resolved Link Text", "Home", linkTag.getLinkText());
	}

	/**
	 * This is a reproduction of bug 617228, reported by Stephen J. Harrington.
	 * When faced with a link like : &lt;A
	 * HREF="/cgi-bin/view_search?query_text=postdate&gt;20020701&txt_clr=White&bg_clr=Red&url=http://loc
	 * al host/Testing/Report 1.html"&gt;20020702 Report 1&lt;/A&gt;
	 * 
	 * parser is unable to handle the link correctly due to the greater than
	 * symbol being confused to be the end of the tag.
	 */
	public void testQueryLink() throws ParserException {
		createParser(
				"<A \n"
						+ "HREF=\"/cgi-bin/view_search?query_text=postdate>20020701&txt_clr=White&bg_clr=Red&url=http://localhost/Testing/Report1.html\">20020702 Report 1</A>",
				"http://transfer.go.com");
		// Register the image scanner
		parser.registerScanners();
		parseAndAssertNodeCount(1);
		assertTrue("Node 1 should be a link tag", node[0] instanceof LinkTag);
		LinkTag linkTag = (LinkTag) node[0];
		assertStringEquals(
				"Resolved Link",
				"http://transfer.go.com/cgi-bin/view_search?query_text=postdate>20020701&txt_clr=White&bg_clr=Red&url=http://localhost/Testing/Report1.html",
				linkTag.getLink());
		assertEquals("Resolved Link Text", "20020702 Report 1", linkTag.getLinkText());

	}

	public void testNotMailtoLink() throws ParserException {
		createParser("<A HREF=\"mailto.html\">not@for.real</A>", "http://www.cj.com/");
		parser.addScanner(new LinkScanner("-l"));
		parseAndAssertNodeCount(1);
		assertTrue("Node should be a HTMLLinkTag", node[0] instanceof LinkTag);
		LinkTag linkTag = (LinkTag) node[0];

		assertEquals("Link Plain Text", "not@for.real", linkTag.toPlainTextString());
		assertTrue("Link is not a mail link", !linkTag.isMailLink());
	}

	public void testMailtoLink() throws ParserException {
		createParser("<A HREF=\"mailto:this@is.real\">this@is.real</A>", "http://www.cj.com/");
		parser.addScanner(new LinkScanner("-l"));
		parseAndAssertNodeCount(1);
		assertTrue("Node should be a HTMLLinkTag", node[0] instanceof LinkTag);
		LinkTag linkTag = (LinkTag) node[0];
		assertEquals("Link Plain Text", "this@is.real", linkTag.toPlainTextString());
		assertTrue("Link is a mail link", linkTag.isMailLink());
	}

	public void testJavascriptLink() throws ParserException {
		createParser("<A HREF=\"javascript:alert('hello');\">say hello</A>", "http://www.cj.com/");
		parser.addScanner(new LinkScanner("-l"));
		parseAndAssertNodeCount(1);
		assertTrue("Node should be a HTMLLinkTag", node[0] instanceof LinkTag);
		LinkTag linkTag = (LinkTag) node[0];

		assertEquals("Link Plain Text", "say hello", linkTag.toPlainTextString());
		assertTrue("Link is a Javascript command", linkTag.isJavascriptLink());
	}

	public void testNotJavascriptLink() throws ParserException {
		createParser("<A HREF=\"javascript_not.html\">say hello</A>", "http://www.cj.com/");
		parser.addScanner(new LinkScanner("-l"));
		parseAndAssertNodeCount(1);
		assertTrue("Node should be a HTMLLinkTag", node[0] instanceof LinkTag);
		LinkTag linkTag = (LinkTag) node[0];

		assertEquals("Link Plain Text", "say hello", linkTag.toPlainTextString());
		assertTrue("Link is not a Javascript command", !linkTag.isJavascriptLink());
	}

	public void testFTPLink() throws ParserException {
		createParser("<A HREF=\"ftp://some.where.it\">my ftp</A>", "http://www.cj.com/");
		parser.addScanner(new LinkScanner("-l"));
		parseAndAssertNodeCount(1);
		assertTrue("Node should be a HTMLLinkTag", node[0] instanceof LinkTag);
		LinkTag linkTag = (LinkTag) node[0];

		assertEquals("Link Plain Text", "my ftp", linkTag.toPlainTextString());
		assertTrue("Link is a FTP site", linkTag.isFTPLink());
	}

	public void testNotFTPLink() throws ParserException {
		createParser("<A HREF=\"ftp.html\">my ftp</A>", "http://www.cj.com/");
		parser.addScanner(new LinkScanner("-l"));
		parseAndAssertNodeCount(1);
		assertTrue("Node should be a HTMLLinkTag", node[0] instanceof LinkTag);
		LinkTag linkTag = (LinkTag) node[0];

		assertEquals("Link Plain Text", "my ftp", linkTag.toPlainTextString());
		assertTrue("Link is not a FTP site", !linkTag.isFTPLink());
	}

	public void testRelativeLinkNotHTMLBug() throws ParserException {
		createParser("<A HREF=\"newpage.html\">New Page</A>", "http://www.mysite.com/books/some.asp");
		parser.addScanner(new LinkScanner("-l"));
		parseAndAssertNodeCount(1);
		assertTrue("Node should be a HTMLLinkTag", node[0] instanceof LinkTag);
		LinkTag linkTag = (LinkTag) node[0];
		assertEquals("Link", "http://www.mysite.com/books/newpage.html", linkTag.getLink());
	}

	public void testBadImageInLinkBug() throws ParserException {
		createParser("<a href=\"registration.asp?EventID=1272\"><img border=\"0\" src=\"\\images\\register.gif\"</a>",
				"http://www.fedpage.com/Event.asp?EventID=1272");
		parser.registerScanners();
		parseAndAssertNodeCount(1);
		assertTrue("Node should be a HTMLLinkTag", node[0] instanceof LinkTag);
		LinkTag linkTag = (LinkTag) node[0];
		// Get the image tag from the link

		Node insideNodes[] = new Node[10];
		int j = 0;
		for (SimpleNodeIterator e = linkTag.children(); e.hasMoreNodes();) {
			insideNodes[j++] = e.nextNode();
		}
		assertEquals("Number of contained internal nodes", 1, j);
		assertTrue(insideNodes[0] instanceof ImageTag);
		ImageTag imageTag = (ImageTag) insideNodes[0];
		assertEquals("Image Tag Location", "http://www.fedpage.com/images\\register.gif", imageTag.getImageURL());
	}

	/**
	 * This is an attempt to reproduce bug 677874 reported by James Moliere. A
	 * link tag of the form <code>
	 * <a class=rlbA href=/news/866201.asp?0sl=-
	 * 32>Shoe bomber handed life sentence</a>
	 * </code>
	 * is not parsed correctly. The second '=' sign in the link causes the
	 * parser to treat it as a seperate attribute
	 */
	public void testLinkContainsEqualTo() throws Exception {
		createParser("<a class=rlbA href=/news/866201.asp?0sl=-" + "32>Shoe bomber handed life sentence</a>");
		parser.registerScanners();
		parseAndAssertNodeCount(1);
		assertType("node type", LinkTag.class, node[0]);
		LinkTag linkTag = (LinkTag) node[0];
		assertStringEquals("link text", "Shoe bomber handed life sentence", linkTag.getLinkText());
		assertStringEquals("link url", "/news/866201.asp?0sl=-32", linkTag.getLink());
	}

	/**
	 * Bug report by Cory Seefurth
	 * 
	 * @throws Exception
	 */
	public void _testLinkWithJSP() throws Exception {
		createParser("<a href=\"<%=Application(\"sURL\")% " + ">/literature/index.htm\">Literature</a>");
		parser.registerScanners();
		parseAndAssertNodeCount(1);
		assertType("should be link tag", LinkTag.class, node[0]);
		LinkTag linkTag = (LinkTag) node[0];
		assertStringEquals("expected link", "<%=Application(\"sURL\")%>/literature/index.htm", linkTag.getLink());
	}

	public void testLinkScannerFilter() throws Exception {
		LinkScanner linkScanner = new LinkScanner(LinkTag.LINK_TAG_FILTER);
		assertEquals("linkscanner filter", LinkTag.LINK_TAG_FILTER, linkScanner.getFilter());
	}

	public void testTagSymbolsInLinkText() throws Exception {
		createParser("<a href=\"/cataclysm/Langy-AnEmpireReborn-Ch2.shtml#story\""
				+ "><< An Empire Reborn: Chapter 2 <<</a>");
		parser.registerScanners();
		parseAndAssertNodeCount(1);
		assertType("node", LinkTag.class, node[0]);
		LinkTag linkTag = (LinkTag) node[0];
		assertEquals("link text", "<< An Empire Reborn: Chapter 2 <<", linkTag.getLinkText());
	}
}
