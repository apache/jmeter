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

import org.htmlparser.scanners.BaseHrefScanner;
import org.htmlparser.scanners.LinkScanner;
import org.htmlparser.scanners.TitleScanner;
import org.htmlparser.tags.BaseHrefTag;
import org.htmlparser.tests.ParserTestCase;
import org.htmlparser.util.LinkProcessor;
import org.htmlparser.util.ParserException;

public class BaseHREFScannerTest extends ParserTestCase {

	private BaseHrefScanner scanner;

	public BaseHREFScannerTest(String arg0) {
		super(arg0);
	}

	protected void setUp() {
		scanner = new BaseHrefScanner();
	}

	public void testRemoveLastSlash() {
		String url1 = "http://www.yahoo.com/";
		String url2 = "http://www.google.com";
		String modifiedUrl1 = LinkProcessor.removeLastSlash(url1);
		String modifiedUrl2 = LinkProcessor.removeLastSlash(url2);
		assertEquals("Url1", "http://www.yahoo.com", modifiedUrl1);
		assertEquals("Url2", "http://www.google.com", modifiedUrl2);
	}

	public void testEvaluate() {
		String testData1 = "BASE HREF=\"http://www.abc.com/\"";
		assertTrue("Data 1 Should have evaluated true", scanner.evaluate(testData1, null));
		String testData2 = "Base href=\"http://www.abc.com/\"";
		assertTrue("Data 2 Should have evaluated true", scanner.evaluate(testData2, null));
	}

	public void testScan() throws ParserException {
		createParser(
				"<html><head><TITLE>test page</TITLE><BASE HREF=\"http://www.abc.com/\"><a href=\"home.cfm\">Home</a>...</html>",
				"http://www.google.com/test/index.html");
		LinkScanner linkScanner = new LinkScanner("-l");
		parser.addScanner(linkScanner);
		parser.addScanner(new TitleScanner("-t"));
		parser.addScanner(linkScanner.createBaseHREFScanner("-b"));
		parseAndAssertNodeCount(7);
		// Base href tag should be the 4th tag
		assertTrue(node[3] instanceof BaseHrefTag);
		BaseHrefTag baseRefTag = (BaseHrefTag) node[3];
		assertEquals("Base HREF Url", "http://www.abc.com/", baseRefTag.getBaseUrl());
	}

}
