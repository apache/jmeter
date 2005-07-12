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
import org.htmlparser.scanners.LinkScanner;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.tags.data.CompositeTagData;
import org.htmlparser.tags.data.LinkData;
import org.htmlparser.tags.data.TagData;
import org.htmlparser.tests.ParserTestCase;
import org.htmlparser.util.ParserException;

public class LinkTagTest extends ParserTestCase {

	public LinkTagTest(String name) {
		super(name);
	}

	/**
	 * The bug being reproduced is this : <BR>
	 * &lt;BODY aLink=#ff0000 bgColor=#ffffff link=#0000cc onload=setfocus()
	 * text=#000000 <BR>
	 * vLink=#551a8b&gt; The above line is incorrectly parsed in that, the BODY
	 * tag is not identified. Creation date: (6/17/2001 4:01:06 PM)
	 */
	public void testLinkNodeBug() throws ParserException {
		createParser("<A HREF=\"../test.html\">abcd</A>", "http://www.google.com/test/index.html");
		// Register the image scanner
		parser.addScanner(new LinkScanner("-l"));

		parseAndAssertNodeCount(1);
		// The node should be an HTMLLinkTag
		assertTrue("Node should be a HTMLLinkTag", node[0] instanceof LinkTag);
		LinkTag linkNode = (LinkTag) node[0];
		assertEquals("The image locn", "http://www.google.com/test.html", linkNode.getLink());
	}

	/**
	 * The bug being reproduced is this : <BR>
	 * &lt;BODY aLink=#ff0000 bgColor=#ffffff link=#0000cc onload=setfocus()
	 * text=#000000 <BR>
	 * vLink=#551a8b&gt; The above line is incorrectly parsed in that, the BODY
	 * tag is not identified. Creation date: (6/17/2001 4:01:06 PM)
	 */
	public void testLinkNodeBug2() throws ParserException {
		createParser("<A HREF=\"../../test.html\">abcd</A>", "http://www.google.com/test/test/index.html");
		// Register the image scanner
		parser.addScanner(new LinkScanner("-l"));

		parseAndAssertNodeCount(1);
		// The node should be an HTMLLinkTag
		assertTrue("Node should be a HTMLLinkTag", node[0] instanceof LinkTag);
		LinkTag linkNode = (LinkTag) node[0];
		assertEquals("The image locn", "http://www.google.com/test.html", linkNode.getLink());
	}

	/**
	 * The bug being reproduced is this : <BR>
	 * When a url ends with a slash, and the link begins with a slash,the parser
	 * puts two slashes This bug was submitted by Roget Kjensrud Creation date:
	 * (6/17/2001 4:01:06 PM)
	 */
	public void testLinkNodeBug3() throws ParserException {
		createParser("<A HREF=\"/mylink.html\">abcd</A>", "http://www.cj.com/");
		// Register the image scanner
		parser.addScanner(new LinkScanner("-l"));

		parseAndAssertNodeCount(1);
		// The node should be an HTMLLinkTag
		assertTrue("Node should be a HTMLLinkTag", node[0] instanceof LinkTag);
		LinkTag linkNode = (LinkTag) node[0];
		assertEquals("Link incorrect", "http://www.cj.com/mylink.html", linkNode.getLink());
	}

	/**
	 * The bug being reproduced is this : <BR>
	 * Simple url without index.html, doesent get appended to link This bug was
	 * submitted by Roget Kjensrud Creation date: (6/17/2001 4:01:06 PM)
	 */
	public void testLinkNodeBug4() throws ParserException {
		createParser("<A HREF=\"/mylink.html\">abcd</A>", "http://www.cj.com");
		// Register the image scanner
		parser.addScanner(new LinkScanner("-l"));

		parseAndAssertNodeCount(1);
		// The node should be an HTMLLinkTag
		assertTrue("Node should be a HTMLLinkTag", node[0] instanceof LinkTag);
		LinkTag linkNode = (LinkTag) node[0];
		assertEquals("Link incorrect!!", "http://www.cj.com/mylink.html", linkNode.getLink());
	}

	public void testLinkNodeBug5() throws ParserException {
		createParser("<a href=http://note.kimo.com.tw/>筆記</a>&nbsp; <a \n"
				+ "href=http://photo.kimo.com.tw/>相簿</a>&nbsp; <a\n"
				+ "href=http://address.kimo.com.tw/>通訊錄</a>&nbsp;&nbsp;", "http://www.cj.com");
		Parser.setLineSeparator("\r\n");
		// Register the image scanner
		parser.addScanner(new LinkScanner("-l"));

		parseAndAssertNodeCount(6);
		// The node should be an LinkTag
		assertTrue("Node should be a LinkTag", node[0] instanceof LinkTag);
		LinkTag linkNode = (LinkTag) node[2];
		assertStringEquals("Link incorrect!!", "http://photo.kimo.com.tw", linkNode.getLink());
		assertEquals("Link beginning", new Integer(48), new Integer(linkNode.elementBegin()));
		assertEquals("Link ending", new Integer(38), new Integer(linkNode.elementEnd()));

		LinkTag linkNode2 = (LinkTag) node[4];
		assertStringEquals("Link incorrect!!", "http://address.kimo.com.tw", linkNode2.getLink());
		assertEquals("Link beginning", new Integer(46), new Integer(linkNode2.elementBegin()));
		assertEquals("Link ending", new Integer(42), new Integer(linkNode2.elementEnd()));
	}

	/**
	 * This bug occurs when there is a null pointer exception thrown while
	 * scanning a tag using LinkScanner. Creation date: (7/1/2001 2:42:13 PM)
	 */
	public void testLinkNodeBugNullPointerException() throws ParserException {
		createParser("<FORM action=http://search.yahoo.com/bin/search name=f><MAP name=m><AREA\n"
				+ "coords=0,0,52,52 href=\"http://www.yahoo.com/r/c1\" shape=RECT><AREA"
				+ "coords=53,0,121,52 href=\"http://www.yahoo.com/r/p1\" shape=RECT><AREA"
				+ "coords=122,0,191,52 href=\"http://www.yahoo.com/r/m1\" shape=RECT><AREA"
				+ "coords=441,0,510,52 href=\"http://www.yahoo.com/r/wn\" shape=RECT>", "http://www.cj.com/");
		// Register the image scanner
		parser.addScanner(new LinkScanner("-l"));
		parseAndAssertNodeCount(6);
	}

	/**
	 * This bug occurs when there is a null pointer exception thrown while
	 * scanning a tag using LinkScanner. Creation date: (7/1/2001 2:42:13 PM)
	 */
	public void testLinkNodeMailtoBug() throws ParserException {
		createParser("<A HREF='mailto:somik@yahoo.com'>hello</A>", "http://www.cj.com/");
		// Register the image scanner
		parser.addScanner(new LinkScanner("-l"));

		parseAndAssertNodeCount(1);
		assertTrue("Node should be a HTMLLinkTag", node[0] instanceof LinkTag);
		LinkTag linkNode = (LinkTag) node[0];
		assertStringEquals("Link incorrect", "somik@yahoo.com", linkNode.getLink());
		assertEquals("Link Type", new Boolean(true), new Boolean(linkNode.isMailLink()));
	}

	/**
	 * This bug occurs when there is a null pointer exception thrown while
	 * scanning a tag using LinkScanner. Creation date: (7/1/2001 2:42:13 PM)
	 */
	public void testLinkNodeSingleQuoteBug() throws ParserException {
		createParser("<A HREF='abcd.html'>hello</A>", "http://www.cj.com/");

		// Register the image scanner
		parser.addScanner(new LinkScanner("-l"));

		parseAndAssertNodeCount(1);
		assertTrue("Node should be a HTMLLinkTag", node[0] instanceof LinkTag);
		LinkTag linkNode = (LinkTag) node[0];
		assertEquals("Link incorrect", "http://www.cj.com/abcd.html", linkNode.getLink());
	}

	/**
	 * The bug being reproduced is this : <BR>
	 * &lt;BODY aLink=#ff0000 bgColor=#ffffff link=#0000cc onload=setfocus()
	 * text=#000000 <BR>
	 * vLink=#551a8b&gt; The above line is incorrectly parsed in that, the BODY
	 * tag is not identified. Creation date: (6/17/2001 4:01:06 PM)
	 */
	public void testLinkTag() throws ParserException {
		createParser("<A HREF=\"test.html\">abcd</A>", "http://www.google.com/test/index.html");
		// Register the image scanner
		parser.addScanner(new LinkScanner("-l"));

		parseAndAssertNodeCount(1);
		// The node should be an HTMLLinkTag
		assertTrue("Node should be a HTMLLinkTag", node[0] instanceof LinkTag);
		LinkTag LinkTag = (LinkTag) node[0];
		assertEquals("The image locn", "http://www.google.com/test/test.html", LinkTag.getLink());
	}

	/**
	 * The bug being reproduced is this : <BR>
	 * &lt;BODY aLink=#ff0000 bgColor=#ffffff link=#0000cc onload=setfocus()
	 * text=#000000 <BR>
	 * vLink=#551a8b&gt; The above line is incorrectly parsed in that, the BODY
	 * tag is not identified. Creation date: (6/17/2001 4:01:06 PM)
	 */
	public void testLinkTagBug() throws ParserException {
		createParser("<A HREF=\"../test.html\">abcd</A>", "http://www.google.com/test/index.html");
		// Register the image scanner
		parser.addScanner(new LinkScanner("-l"));

		parseAndAssertNodeCount(1);
		// The node should be an HTMLLinkTag
		assertTrue("Node should be a HTMLLinkTag", node[0] instanceof LinkTag);
		LinkTag LinkTag = (LinkTag) node[0];
		assertEquals("The image locn", "http://www.google.com/test.html", LinkTag.getLink());
	}

	/**
	 * The bug being reproduced is this : <BR>
	 * &lt;A HREF=&gt;Something&lt;A&gt;<BR>
	 * vLink=#551a8b&gt; The above line is incorrectly parsed in that, the BODY
	 * tag is not identified. Creation date: (6/17/2001 4:01:06 PM)
	 */
	public void testNullTagBug() throws ParserException {
		createParser("<A HREF=>Something</A>", "http://www.google.com/test/index.html");
		// Register the image scanner
		parser.addScanner(new LinkScanner("-l"));

		parseAndAssertNodeCount(1);
		// The node should be an HTMLLinkTag
		assertTrue("Node should be a HTMLLinkTag", node[0] instanceof LinkTag);
		LinkTag linkTag = (LinkTag) node[0];
		assertEquals("The link location", "", linkTag.getLink());
		assertEquals("The link text", "Something", linkTag.getLinkText());
	}

	public void testToPlainTextString() throws ParserException {
		createParser("<A HREF='mailto:somik@yahoo.com'>hello</A>", "http://www.cj.com/");
		// Register the image scanner
		parser.addScanner(new LinkScanner("-l"));

		parseAndAssertNodeCount(1);
		assertTrue("Node should be a HTMLLinkTag", node[0] instanceof LinkTag);
		LinkTag linkTag = (LinkTag) node[0];
		assertEquals("Link Plain Text", "hello", linkTag.toPlainTextString());
	}

	public void testToHTML() throws ParserException {
		createParser("<A HREF='mailto:somik@yahoo.com'>hello</A>\n"
				+ "<LI><font color=\"FF0000\" size=-1><b>Tech Samachar:</b></font><a \n"
				+ "href=\"http://ads.samachar.com/bin/redirect/tech.txt?http://www.samachar.com/tech\n"
				+ "nical.html\"> Journalism 3.0</a> by Rajesh Jain", "http://www.cj.com/");
		Parser.setLineSeparator("\r\n");
		// Register the image scanner
		parser.addScanner(new LinkScanner("-l"));

		parseAndAssertNodeCount(9);
		assertTrue("First Node should be a HTMLLinkTag", node[0] instanceof LinkTag);
		LinkTag linkTag = (LinkTag) node[0];
		assertStringEquals("Link Raw Text", "<A HREF=\"mailto:somik@yahoo.com\">hello</A>", linkTag.toHtml());
		assertTrue("Eighth Node should be a HTMLLinkTag", node[7] instanceof LinkTag);
		linkTag = (LinkTag) node[7];
		assertStringEquals(
				"Link Raw Text",
				"<A HREF=\"http://ads.samachar.com/bin/redirect/tech.txt?http://www.samachar.com/tech\r\nnical.html\"> Journalism 3.0</A>",
				linkTag.toHtml());
	}

	public void testTypeHttps() throws ParserException {
		LinkTag linkTag = new LinkTag(new TagData(0, 0, "", ""), new CompositeTagData(null, null, null), new LinkData(
				"https://www.someurl.com", "", "", false, false));
		assertTrue("This is a https link", linkTag.isHTTPSLink());
	}

	public void testTypeFtp() throws ParserException {
		LinkTag linkTag = new LinkTag(new TagData(0, 0, "", ""), new CompositeTagData(null, null, null), new LinkData(
				"ftp://www.someurl.com", "", "", false, false));
		assertTrue("This is an ftp link", linkTag.isFTPLink());
	}

	public void testTypeJavaScript() throws ParserException {
		LinkTag linkTag = new LinkTag(new TagData(0, 0, "", ""), new CompositeTagData(null, null, null), new LinkData(
				"javascript://www.someurl.com", "", "", false, true));
		assertTrue("This is a javascript link", linkTag.isJavascriptLink());
	}

	public void testTypeHttpLink() throws ParserException {
		LinkTag linkTag = new LinkTag(new TagData(0, 0, "", ""), new CompositeTagData(null, null, null), new LinkData(
				"http://www.someurl.com", "", "", false, false));
		assertTrue("This is a http link : " + linkTag.getLink(), linkTag.isHTTPLink());
		linkTag = new LinkTag(new TagData(0, 0, "", ""), new CompositeTagData(null, null, null), new LinkData(
				"somePage.html", "", "", false, false));
		assertTrue("This relative link is alsp a http link : " + linkTag.getLink(), linkTag.isHTTPLink());
		linkTag = new LinkTag(new TagData(0, 0, "", ""), new CompositeTagData(null, null, null), new LinkData(
				"ftp://somePage.html", "", "", false, false));
		assertTrue("This is not a http link : " + linkTag.getLink(), !linkTag.isHTTPLink());
	}

	public void testTypeHttpLikeLink() throws ParserException {
		LinkTag linkTag = new LinkTag(new TagData(0, 0, "", ""), new CompositeTagData(null, null, null), new LinkData(
				"http://", "", "", false, false));
		assertTrue("This is a http link", linkTag.isHTTPLikeLink());
		LinkTag linkTag2 = new LinkTag(new TagData(0, 0, "", ""), new CompositeTagData(null, null, null), new LinkData(
				"https://www.someurl.com", "", "", false, false));
		assertTrue("This is a https link", linkTag2.isHTTPLikeLink());
	}

	/**
	 * Bug #738504 MailLink != HTTPLink
	 */
	public void testMailToIsNotAHTTPLink() throws ParserException {
		LinkTag link;

		createParser("<A HREF='mailto:derrickoswald@users.sourceforge.net'>Derrick</A>", "http://sourceforge.net");
		// Register the link scanner
		parser.addScanner(new LinkScanner("-l"));

		parseAndAssertNodeCount(1);
		assertTrue("Node should be a HTMLLinkTag", node[0] instanceof LinkTag);
		link = (LinkTag) node[0];
		assertTrue("bug #738504 MailLink != HTTPLink", !link.isHTTPLink());
		assertTrue("bug #738504 MailLink != HTTPSLink", !link.isHTTPSLink());
	}
}
