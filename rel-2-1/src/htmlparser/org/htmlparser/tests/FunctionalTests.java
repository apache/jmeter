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
package org.htmlparser.tests;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.scanners.ImageScanner;
import org.htmlparser.tags.ImageTag;
import org.htmlparser.util.DefaultParserFeedback;
import org.htmlparser.util.LinkProcessor;
import org.htmlparser.util.NodeIterator;
import org.htmlparser.util.ParserException;

public class FunctionalTests extends TestCase {

	public FunctionalTests(String arg0) {
		super(arg0);
	}

	/**
	 * Based on a suspected bug report by Annette Doyle, to check if the no of
	 * image tags are correctly identified by the parser
	 */
	public void testNumImageTagsInYahooWithoutRegisteringScanners() throws ParserException {
		// First count the image tags as is
		int imgTagCount;
		imgTagCount = findImageTagCount();
		try {
			int parserImgTagCount = countImageTagsWithHTMLParser();
			assertEquals("Image Tag Count", imgTagCount, parserImgTagCount);
		} catch (ParserException e) {
			throw new ParserException("Error thrown in call to countImageTagsWithHTMLParser()", e);
		}

	}

	public int findImageTagCount() {
		int imgTagCount = 0;
		try {
			URL url = new URL("http://www.yahoo.com");
			InputStream is = url.openStream();
			BufferedReader reader;
			reader = new BufferedReader(new InputStreamReader(is));
			imgTagCount = countImageTagsWithoutHTMLParser(reader);
			is.close();
		} catch (MalformedURLException e) {
			System.err.println("URL was malformed!");
		} catch (IOException e) {
			System.err.println("IO Exception occurred while trying to open stream");
		}
		return imgTagCount;
	}

	public int countImageTagsWithHTMLParser() throws ParserException {
		Parser parser = new Parser("http://www.yahoo.com", new DefaultParserFeedback());
		parser.addScanner(new ImageScanner("-i", new LinkProcessor()));
		int parserImgTagCount = 0;
		Node node;
		for (NodeIterator e = parser.elements(); e.hasMoreNodes();) {
			node = e.nextNode();
			if (node instanceof ImageTag) {
				parserImgTagCount++;
			}
		}
		return parserImgTagCount;
	}

	public int countImageTagsWithoutHTMLParser(BufferedReader reader) throws IOException {
		String line;
		int imgTagCount = 0;
		do {
			line = reader.readLine();
			if (line != null) {
				// Check the line for image tags
				String newline = line.toUpperCase();
				int fromIndex = -1;
				do {
					fromIndex = newline.indexOf("<IMG", fromIndex + 1);
					if (fromIndex != -1) {
						imgTagCount++;
					}
				} while (fromIndex != -1);
			}
		} while (line != null);
		return imgTagCount;
	}

	public static TestSuite suite() {
		return new TestSuite(FunctionalTests.class);
	}
}
