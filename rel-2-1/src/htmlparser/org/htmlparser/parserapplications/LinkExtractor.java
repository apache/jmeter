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

package org.htmlparser.parserapplications;

import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.ParserException;

/**
 * LinkExtractor extracts all the links from the given webpage and prints them
 * on standard output.
 */
public class LinkExtractor {
	private String location;

	private Parser parser;

	public LinkExtractor(String location) {
		this.location = location;
		try {
			this.parser = new Parser(location); // Create the parser object
			parser.registerScanners();
			// Register standard scanners (Very Important)
		} catch (ParserException e) {
			e.printStackTrace();
		}

	}

	public void extractLinks() throws ParserException {
		System.out.println("Parsing " + location + " for links...");
		Node[] links = parser.extractAllNodesThatAre(LinkTag.class);
		for (int i = 0; i < links.length; i++) {
			LinkTag linkTag = (LinkTag) links[i];
			// Print it
			// System.out.println(linkTag.toString());
			System.out.println(linkTag.getLink());
			// To extract only mail addresses, uncomment the following line
			// if (linkTag.isMailLink()) System.out.println(linkTag.getLink());
		}
	}

	public static void main(String[] args) {
		if (args.length < 0) {
			System.err.println("Syntax Error : Please provide the location(URL or file) to parse");
			System.exit(-1);
		}
		LinkExtractor linkExtractor = new LinkExtractor(args[0]);
		try {
			linkExtractor.extractLinks();
		} catch (ParserException e) {
			e.printStackTrace();
		}
	}
}