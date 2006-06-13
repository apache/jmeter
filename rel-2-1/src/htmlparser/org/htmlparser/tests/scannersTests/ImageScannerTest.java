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
import org.htmlparser.scanners.ImageScanner;
import org.htmlparser.scanners.TableScanner;
import org.htmlparser.tags.ImageTag;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.tags.TableColumn;
import org.htmlparser.tags.TableRow;
import org.htmlparser.tags.Tag;
import org.htmlparser.tags.data.TagData;
import org.htmlparser.tests.ParserTestCase;
import org.htmlparser.util.LinkProcessor;
import org.htmlparser.util.NodeIterator;
import org.htmlparser.util.ParserException;

public class ImageScannerTest extends ParserTestCase {

	public ImageScannerTest(String name) {
		super(name);
	}

	public void testDynamicRelativeImageScan() throws ParserException {
		createParser("<IMG SRC=\"../abc/def/mypic.jpg\">", "http://www.yahoo.com/ghi?abcdefg");
		// Register the image scanner
		parser.addScanner(new ImageScanner("-i", new LinkProcessor()));
		parseAndAssertNodeCount(1);
		assertTrue("Node identified should be HTMLImageTag", node[0] instanceof ImageTag);
		ImageTag imageTag = (ImageTag) node[0];
		assertEquals("Expected Link", "http://www.yahoo.com/abc/def/mypic.jpg", imageTag.getImageURL());
	}

	public void testEvaluate() {
		ImageScanner scanner = new ImageScanner("-i", new LinkProcessor());
		boolean retVal = scanner.evaluate("   img ", null);
		assertEquals("Evaluation of IMG tag", new Boolean(true), new Boolean(retVal));
	}

	/**
	 * This is the reproduction of a bug which causes a null pointer exception
	 */
	public void testExtractImageLocnInvertedCommasBug() throws ParserException {
		Tag tag = new Tag(
				new TagData(
						0,
						0,
						"img width=638 height=53 border=0 usemap=\"#m\" src=http://us.a1.yimg.com/us.yimg.com/i/ww/m5v5.gif alt=Yahoo",
						""));
		String link = "img width=638 height=53 border=0 usemap=\"#m\" src=http://us.a1.yimg.com/us.yimg.com/i/ww/m5v5.gif alt=Yahoo";
		String url = "c:\\cvs\\html\\binaries\\yahoo.htm";
		ImageScanner scanner = new ImageScanner("-i", new LinkProcessor());
		assertEquals("Extracted Image Locn", "http://us.a1.yimg.com/us.yimg.com/i/ww/m5v5.gif", scanner
				.extractImageLocn(tag, url));
	}

	/**
	 * This test has been improved to check for params in the image tag, based
	 * on requirement by Annette Doyle. Thereby an important bug was detected.
	 */
	public void testPlaceHolderImageScan() throws ParserException {
		createParser("<IMG width=1 height=1 alt=\"a\">", "http://www.yahoo.com/ghi?abcdefg");

		// Register the image scanner
		parser.addScanner(new ImageScanner("-i", new LinkProcessor()));
		parseAndAssertNodeCount(1);
		assertTrue("Node identified should be HTMLImageTag", node[0] instanceof ImageTag);
		ImageTag imageTag = (ImageTag) node[0];
		assertEquals("Expected Image Locn", "", imageTag.getImageURL());
		assertEquals("Image width", "1", imageTag.getAttribute("WIDTH"));
		assertEquals("Image height", "1", imageTag.getAttribute("HEIGHT"));
		assertEquals("alt", "a", imageTag.getAttribute("ALT"));
	}

	public void testRelativeImageScan() throws ParserException {
		createParser("<IMG SRC=\"mypic.jpg\">", "http://www.yahoo.com");

		// Register the image scanner
		parser.addScanner(new ImageScanner("-i", new LinkProcessor()));
		parseAndAssertNodeCount(1);
		assertTrue("Node identified should be HTMLImageTag", node[0] instanceof ImageTag);
		ImageTag imageTag = (ImageTag) node[0];
		assertEquals("Expected Link", "http://www.yahoo.com/mypic.jpg", imageTag.getImageURL());
	}

	public void testRelativeImageScan2() throws ParserException {
		createParser("<IMG SRC=\"abc/def/mypic.jpg\">", "http://www.yahoo.com");
		// Register the image scanner
		parser.addScanner(new ImageScanner("-i", new LinkProcessor()));
		parseAndAssertNodeCount(1);
		assertTrue("Node identified should be HTMLImageTag", node[0] instanceof ImageTag);
		ImageTag imageTag = (ImageTag) node[0];
		assertEquals("Expected Link", "http://www.yahoo.com/abc/def/mypic.jpg", imageTag.getImageURL());
	}

	public void testRelativeImageScan3() throws ParserException {
		createParser("<IMG SRC=\"../abc/def/mypic.jpg\">", "http://www.yahoo.com/ghi");
		// Register the image scanner
		parser.addScanner(new ImageScanner("-i", new LinkProcessor()));
		parseAndAssertNodeCount(1);
		assertTrue("Node identified should be HTMLImageTag", node[0] instanceof ImageTag);
		ImageTag imageTag = (ImageTag) node[0];
		assertEquals("Expected Link", "http://www.yahoo.com/abc/def/mypic.jpg", imageTag.getImageURL());
	}

	/**
	 * Test image url which contains spaces in it. This was actually a bug
	 * reported by Sam Joseph (sam@neurogrid.net)
	 */
	public void testImageWithSpaces() throws ParserException {
		createParser("<IMG SRC=\"../abc/def/Hello World.jpg\">", "http://www.yahoo.com/ghi");
		// Register the image scanner
		parser.addScanner(new ImageScanner("-i", new LinkProcessor()));
		parseAndAssertNodeCount(1);
		assertTrue("Node identified should be HTMLImageTag", node[0] instanceof ImageTag);
		ImageTag imageTag = (ImageTag) node[0];
		assertEquals("Expected Link", "http://www.yahoo.com/abc/def/Hello World.jpg", imageTag.getImageURL());
	}

	public void testImageWithNewLineChars() throws ParserException {
		createParser("<IMG SRC=\"../abc/def/Hello \r\nWorld.jpg\">", "http://www.yahoo.com/ghi");
		Parser.setLineSeparator("\r\n");
		// Register the image scanner
		parser.addScanner(new ImageScanner("-i", new LinkProcessor()));
		parseAndAssertNodeCount(1);
		assertTrue("Node identified should be HTMLImageTag", node[0] instanceof ImageTag);
		ImageTag imageTag = (ImageTag) node[0];
		String exp = new String("http://www.yahoo.com/abc/def/Hello World.jpg");
		// assertEquals("Length of
		// image",exp.length(),imageTag.getImageLocation().length());
		assertStringEquals("Expected Image", exp, imageTag.getImageURL());
	}

	/**
	 * Test case to reproduce bug reported by Annette
	 */
	public void testImageTagsFromYahoo() throws ParserException {
		createParser(
				"<small><a href=s/5926>Air</a>, <a href=s/5927>Hotel</a>, <a href=s/5928>Vacations</a>, <a href=s/5929>Cruises</a></small></td><td align=center><a href=\"http://rd.yahoo.com/M=218794.2020165.3500581.220161/D=yahoo_top/S=2716149:NP/A=1041273/?http://adfarm.mediaplex.com/ad/ck/990-1736-1039-211\" target=\"_top\"><img width=230 height=33 src=\"http://us.a1.yimg.com/us.yimg.com/a/co/columbiahouse/4for49Freesh_230x33_redx2.gif\" alt=\"\" border=0></a></td><td nowrap align=center width=215>Find your match on<br><a href=s/2734><b>Yahoo! Personals</b></a></td></tr><tr><td colspan=3 align=center><input size=30 name=p>\n"
						+ "<input type=submit value=Search> <a href=r/so>advanced search</a></td></tr></table><table border=0 cellspacing=0 cellpadding=3 width=640><tr><td nowrap align=center><table border=0 cellspacing=0 cellpadding=0><tr><td><a href=s/5948><img src=\"http://us.i1.yimg.com/us.yimg.com/i/ligans/klgs/eet.gif\" width=20 height=20 border=0></a></td><td> &nbsp; &nbsp; <a href=s/1048><b>Yahooligans!</b></a> - <a href=s/5282>Eet & Ern</a>, <a href=s/5283>Games</a>, <a href=s/5284>Science</a>, <a href=s/5285>Sports</a>, <a href=s/5286>Movies</a>, <a href=s/1048>more</a> &nbsp; &nbsp; </td><td><a href=s/5948><img src=\"http://us.i1.yimg.com/us.yimg.com/i/ligans/klgs/ern.gif\" width=20 height=20 border=0></a></td></tr></table></td></tr><tr><td nowrap align=center><small><b>Shop</b>&nbsp;\n",
				"http://www.yahoo.com");
		Node[] _node = new Node[10];
		// Register the image scanner
		parser.addScanner(new ImageScanner("-i", new LinkProcessor()));
		int i = 0;
		Node thisNode;
		for (NodeIterator e = parser.elements(); e.hasMoreNodes();) {
			thisNode = e.nextNode();
			if (thisNode instanceof ImageTag)
				_node[i++] = thisNode;
		}
		assertEquals("Number of nodes identified should be 3", 3, i);
		assertTrue("Node identified should be HTMLImageTag", _node[0] instanceof ImageTag);
		ImageTag imageTag = (ImageTag) _node[0];
		assertEquals("Expected Image",
				"http://us.a1.yimg.com/us.yimg.com/a/co/columbiahouse/4for49Freesh_230x33_redx2.gif", imageTag
						.getImageURL());
		ImageTag imageTag2 = (ImageTag) _node[1];
		assertEquals("Expected Image 2", "http://us.i1.yimg.com/us.yimg.com/i/ligans/klgs/eet.gif", imageTag2
				.getImageURL());
		ImageTag imageTag3 = (ImageTag) _node[2];
		assertEquals("Expected Image 3", "http://us.i1.yimg.com/us.yimg.com/i/ligans/klgs/ern.gif", imageTag3
				.getImageURL());
	}

	/**
	 * Test case to reproduce bug reported by Annette
	 */
	public void testImageTagsFromYahooWithAllScannersRegistered() throws ParserException {
		createParser("<tr>" + "<td>" + "	<small><a href=s/5926>Air</a>, <a href=s/5927>Hotel</a>, "
				+ "<a href=s/5928>Vacations</a>, <a href=s/5929>Cruises</a></small>" + "</td>" + "<td align=center>"
				+ "<a href=\"http://rd.yahoo.com/M=218794.2020165.3500581.220161/D=yahoo_top/S="
				+ "2716149:NP/A=1041273/?http://adfarm.mediaplex.com/ad/ck/990-1736-1039-211\" "
				+ "target=\"_top\"><img width=230 height=33 src=\"http://us.a1.yimg.com/us.yimg.com/a/co/"
				+ "columbiahouse/4for49Freesh_230x33_redx2.gif\" alt=\"\" border=0></a>" + "</td>"
				+ "<td nowrap align=center width=215>" + "Find your match on<br><a href=s/2734>"
				+ "<b>Yahoo! Personals</b></a>" + "</td>" + "</tr>" + "<tr>" + "<td colspan=3 align=center>"
				+ "<input size=30 " + "name=p>\n" + "</td>" + "</tr>", "http://www.yahoo.com", 30);

		// Register the image scanner
		parser.registerScanners();
		// parser.addScanner(new TableScanner(parser));
		parseAndAssertNodeCount(2);
		assertType("first node type", TableRow.class, node[0]);
		TableRow row = (TableRow) node[0];
		TableColumn col = row.getColumns()[1];
		Node node = col.children().nextNode();
		assertType("Node identified should be HTMLLinkTag", LinkTag.class, node);
		LinkTag linkTag = (LinkTag) node;
		Node nodeInsideLink = linkTag.children().nextNode();
		assertType("Tag within link should be an image tag", ImageTag.class, nodeInsideLink);
		ImageTag imageTag = (ImageTag) nodeInsideLink;
		assertStringEquals("Expected Image",
				"http://us.a1.yimg.com/us.yimg.com/a/co/columbiahouse/4for49Freesh_230x33_redx2.gif", imageTag
						.getImageURL());
	}

	/**
	 * This is the reproduction of a bug reported by Annette Doyle
	 */
	public void testImageTagOnMultipleLines() throws ParserException {
		createParser("<td rowspan=3>" + "<img height=49 \n\n"
				+ "alt=\"Central Intelligence Agency, Director of Central Intelligence\" \n\n"
				+ "src=\"graphics/images_home2/cia_banners_template3_01.gif\" \n\n" + "width=241>" + "</td>",
				"http://www.cia.gov");

		// Register the image scanner
		parser.registerScanners();
		parser.addScanner(new TableScanner(parser));
		parseAndAssertNodeCount(1);
		assertType("node should be", TableColumn.class, node[0]);
		TableColumn col = (TableColumn) node[0];
		Node node = col.children().nextNode();
		assertType("node inside column", ImageTag.class, node);
		ImageTag imageTag = (ImageTag) node;
		// Get the data from the node
		assertEquals("Image location", "http://www.cia.gov/graphics/images_home2/cia_banners_template3_01.gif",
				imageTag.getImageURL());
		assertEquals("Alt Value", "Central Intelligence Agency, Director of Central Intelligence", imageTag
				.getAttribute("ALT"));
		assertEquals("Width", "241", imageTag.getAttribute("WIDTH"));
		assertEquals("Height", "49", imageTag.getAttribute("HEIGHT"));
	}

	public void testDirectRelativeLinks() throws ParserException {
		createParser("<IMG SRC  = \"/images/lines/li065.jpg\">",
				"http://www.cybergeo.presse.fr/REVGEO/ttsavoir/joly.htm");

		// Register the image scanner
		parser.registerScanners();
		parseAndAssertNodeCount(1);
		assertTrue("Node identified should be HTMLImageTag", node[0] instanceof ImageTag);
		ImageTag imageTag = (ImageTag) node[0];
		assertEquals("Image Location", "http://www.cybergeo.presse.fr/images/lines/li065.jpg", imageTag.getImageURL());

	}

	/**
	 * Based on a page submitted by Claude Duguay, the image tag has IMG
	 * SRC"somefile.jpg" - a missing equal to sign
	 */
	public void testMissingEqualTo() throws ParserException {
		createParser("<img src\"/images/spacer.gif\" width=\"1\" height=\"1\" alt=\"\">",
				"http://www.htmlparser.org/subdir1/subdir2");

		// Register the image scanner
		parser.registerScanners();
		parseAndAssertNodeCount(1);
		assertTrue("Node identified should be HTMLImageTag", node[0] instanceof ImageTag);
		ImageTag imageTag = (ImageTag) node[0];
		assertStringEquals("Image Location", "http://www.htmlparser.org/images/spacer.gif", imageTag.getImageURL());
		assertEquals("Width", "1", imageTag.getAttribute("WIDTH"));
		assertEquals("Height", "1", imageTag.getAttribute("HEIGHT"));
		assertEquals("Alt", "", imageTag.getAttribute("ALT"));
	}
}
