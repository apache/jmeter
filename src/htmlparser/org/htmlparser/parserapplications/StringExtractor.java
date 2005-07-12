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

import org.htmlparser.beans.StringBean;
import org.htmlparser.util.ParserException;

public class StringExtractor {
	private String resource;

	/**
	 * Construct a StringExtractor to read from the given resource.
	 * 
	 * @param resource
	 *            Either a URL or a file name.
	 */
	public StringExtractor(String resource) {
		this.resource = resource;
	}

	/**
	 * Extract the text from a page.
	 * 
	 * @param links
	 *            if <code>true</code> include hyperlinks in output.
	 * @return The textual contents of the page.
	 */
	public String extractStrings(boolean links) throws ParserException {
		StringBean sb;

		sb = new StringBean();
		sb.setLinks(links);
		sb.setURL(resource);

		return (sb.getStrings());
	}

	/**
	 * Mainline.
	 * 
	 * @param args
	 *            The command line arguments.
	 */
	public static void main(String[] args) {
		boolean links;
		String url;
		StringExtractor se;

		links = false;
		url = null;
		for (int i = 0; i < args.length; i++)
			if (args[i].equalsIgnoreCase("-links"))
				links = true;
			else
				url = args[i];
		if (null != url) {
			se = new StringExtractor(url);
			try {
				System.out.println(se.extractStrings(links));
			} catch (ParserException e) {
				e.printStackTrace();
			}
		} else
			System.out
					.println("Usage: java -classpath htmlparser.jar org.htmlparser.parserapplications.StringExtractor [-links] url");
	}
}
