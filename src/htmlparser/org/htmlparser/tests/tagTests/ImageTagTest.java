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

import org.htmlparser.scanners.ImageScanner;
import org.htmlparser.tags.ImageTag;
import org.htmlparser.tests.ParserTestCase;
import org.htmlparser.util.LinkProcessor;
import org.htmlparser.util.ParserException;

public class ImageTagTest extends ParserTestCase {
	public ImageTagTest(String name) {
		super(name);
	}

	/**
	 * The bug being reproduced is this : <BR>
	 * &lt;BODY aLink=#ff0000 bgColor=#ffffff link=#0000cc onload=setfocus()
	 * text=#000000 <BR>
	 * vLink=#551a8b&gt; The above line is incorrectly parsed in that, the BODY
	 * tag is not identified. Creation date: (6/17/2001 4:01:06 PM)
	 */
	public void testImageTag() throws ParserException {
		createParser("<IMG alt=Google height=115 src=\"goo/title_homepage4.gif\" width=305>",
				"http://www.google.com/test/index.html");
		// Register the image scanner
		parser.addScanner(new ImageScanner("-i", new LinkProcessor()));

		parseAndAssertNodeCount(1);
		// The node should be an HTMLImageTag
		assertTrue("Node should be a HTMLImageTag", node[0] instanceof ImageTag);
		ImageTag imageTag = (ImageTag) node[0];
		assertEquals("The image locn", "http://www.google.com/test/goo/title_homepage4.gif", imageTag.getImageURL());
	}

	/**
	 * The bug being reproduced is this : <BR>
	 * &lt;BODY aLink=#ff0000 bgColor=#ffffff link=#0000cc onload=setfocus()
	 * text=#000000 <BR>
	 * vLink=#551a8b&gt; The above line is incorrectly parsed in that, the BODY
	 * tag is not identified. Creation date: (6/17/2001 4:01:06 PM)
	 */
	public void testImageTagBug() throws ParserException {
		createParser("<IMG alt=Google height=115 src=\"../goo/title_homepage4.gif\" width=305>",
				"http://www.google.com/test/");
		// Register the image scanner
		parser.addScanner(new ImageScanner("-i", new LinkProcessor()));

		parseAndAssertNodeCount(1);
		// The node should be an HTMLImageTag
		assertTrue("Node should be a HTMLImageTag", node[0] instanceof ImageTag);
		ImageTag imageTag = (ImageTag) node[0];
		assertEquals("The image locn", "http://www.google.com/goo/title_homepage4.gif", imageTag.getImageURL());
	}

	/**
	 * The bug being reproduced is this : <BR>
	 * &lt;BODY aLink=#ff0000 bgColor=#ffffff link=#0000cc onload=setfocus()
	 * text=#000000 <BR>
	 * vLink=#551a8b&gt; The above line is incorrectly parsed in that, the BODY
	 * tag is not identified. Creation date: (6/17/2001 4:01:06 PM)
	 */
	public void testImageTageBug2() throws ParserException {
		createParser("<IMG alt=Google height=115 src=\"../../goo/title_homepage4.gif\" width=305>",
				"http://www.google.com/test/test/index.html");
		// Register the image scanner
		parser.addScanner(new ImageScanner("-i", new LinkProcessor()));

		parseAndAssertNodeCount(1);
		// The node should be an HTMLImageTag
		assertTrue("Node should be a HTMLImageTag", node[0] instanceof ImageTag);
		ImageTag imageTag = (ImageTag) node[0];
		assertEquals("The image locn", "http://www.google.com/goo/title_homepage4.gif", imageTag.getImageURL());
	}

	/**
	 * This bug occurs when there is a null pointer exception thrown while
	 * scanning a tag using LinkScanner. Creation date: (7/1/2001 2:42:13 PM)
	 */
	public void testImageTagSingleQuoteBug() throws ParserException {
		createParser("<IMG SRC='abcd.jpg'>", "http://www.cj.com/");
		// Register the image scanner
		parser.addScanner(new ImageScanner("-i", new LinkProcessor()));

		parseAndAssertNodeCount(1);
		assertTrue("Node should be a HTMLImageTag", node[0] instanceof ImageTag);
		ImageTag imageTag = (ImageTag) node[0];
		assertEquals("Image incorrect", "http://www.cj.com/abcd.jpg", imageTag.getImageURL());
	}

	/**
	 * The bug being reproduced is this : <BR>
	 * &lt;A HREF=&gt;Something&lt;A&gt;<BR>
	 * vLink=#551a8b&gt; The above line is incorrectly parsed in that, the BODY
	 * tag is not identified. Creation date: (6/17/2001 4:01:06 PM)
	 */
	public void testNullImageBug() throws ParserException {
		createParser("<IMG SRC=>", "http://www.google.com/test/index.html");
		// Register the image scanner
		parser.addScanner(new ImageScanner("-i", new LinkProcessor()));

		parseAndAssertNodeCount(1);
		// The node should be an HTMLLinkTag
		assertTrue("Node should be a HTMLImageTag", node[0] instanceof ImageTag);
		ImageTag imageTag = (ImageTag) node[0];
		assertStringEquals("The image location", "", imageTag.getImageURL());
	}

	public void testToHTML() throws ParserException {
		createParser("<IMG alt=Google height=115 src=\"../../goo/title_homepage4.gif\" width=305>",
				"http://www.google.com/test/test/index.html");
		// Register the image scanner
		parser.addScanner(new ImageScanner("-i", new LinkProcessor()));

		parseAndAssertNodeCount(1);
		// The node should be an HTMLImageTag
		assertTrue("Node should be a HTMLImageTag", node[0] instanceof ImageTag);
		ImageTag imageTag = (ImageTag) node[0];
		assertStringEquals("The image locn",
				"<IMG WIDTH=\"305\" ALT=\"Google\" SRC=\"../../goo/title_homepage4.gif\" HEIGHT=\"115\">", imageTag
						.toHtml());
		assertEquals("Alt", "Google", imageTag.getAttribute("alt"));
		assertEquals("Height", "115", imageTag.getAttribute("height"));
		assertEquals("Width", "305", imageTag.getAttribute("width"));
	}
}
