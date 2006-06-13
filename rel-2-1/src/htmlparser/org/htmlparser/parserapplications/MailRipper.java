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

import java.util.Enumeration;
import java.util.Vector;

import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.DefaultParserFeedback;
import org.htmlparser.util.NodeIterator;
import org.htmlparser.util.ParserException;

/**
 * MailRipper will rip out all the mail addresses from a given web page Pass a
 * web site (or html file on your local disk) as an argument.
 */
public class MailRipper {
	private org.htmlparser.Parser parser;

	/**
	 * MailRipper c'tor takes the url to be ripped
	 * 
	 * @param resourceLocation
	 *            url to be ripped
	 */
	public MailRipper(String resourceLocation) {
		try {
			parser = new Parser(resourceLocation, new DefaultParserFeedback());
			parser.registerScanners();
		} catch (ParserException e) {
			System.err.println("Could not create parser object");
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		System.out.println("Mail Ripper v" + Parser.getVersion());
		if (args.length < 1 || args[0].equals("-help")) {
			System.out.println();
			System.out
					.println("Syntax : java -classpath htmlparser.jar org.htmlparser.parserapplications.MailRipper <resourceLocn/website>");
			System.out.println();
			System.out.println("   <resourceLocn> the name of the file to be parsed (with complete path ");
			System.out.println("                  if not in current directory)");
			System.out.println("   -help This screen");
			System.out.println();
			System.out.println("HTML Parser home page : http://htmlparser.sourceforge.net");
			System.out.println();
			System.out
					.println("Example : java -classpath htmlparser.jar com.kizna.parserapplications.MailRipper http://htmlparser.sourceforge.net");
			System.out.println();
			System.out
					.println("If you have any doubts, please join the HTMLParser mailing list (user/developer) from the HTML Parser home page instead of mailing any of the contributors directly. You will be surprised with the quality of open source support. ");
			System.exit(-1);
		}
		String resourceLocation = "http://htmlparser.sourceforge.net";
		if (args.length != 0)
			resourceLocation = args[0];

		MailRipper ripper = new MailRipper(resourceLocation);
		System.out.println("Ripping Site " + resourceLocation);
		try {
			for (Enumeration e = ripper.rip(); e.hasMoreElements();) {
				LinkTag tag = (LinkTag) e.nextElement();
				System.out.println("Ripped mail address : " + tag.getLink());
			}
		} catch (ParserException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Rip all mail addresses from the given url, and return an enumeration of
	 * such mail addresses.
	 * 
	 * @return Enumeration of mail addresses (a vector of LinkTag)
	 */
	public Enumeration rip() throws ParserException {
		Node node;
		Vector mailAddresses = new Vector();
		for (NodeIterator e = parser.elements(); e.hasMoreNodes();) {
			node = e.nextNode();
			if (node instanceof LinkTag) {
				LinkTag linkTag = (LinkTag) node;
				if (linkTag.isMailLink())
					mailAddresses.addElement(linkTag);
			}
		}
		return mailAddresses.elements();
	}
}
