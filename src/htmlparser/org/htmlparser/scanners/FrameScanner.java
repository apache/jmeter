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
package org.htmlparser.scanners;

//////////////////
// Java Imports //
//////////////////
import java.util.Hashtable;

import org.htmlparser.tags.FrameTag;
import org.htmlparser.tags.Tag;
import org.htmlparser.tags.data.TagData;
import org.htmlparser.util.LinkProcessor;
import org.htmlparser.util.ParserException;

/**
 * Scans for the Frame Tag. This is a subclass of TagScanner, and is called
 * using a variant of the template method. If the evaluate() method returns
 * true, that means the given string contains an image tag. Extraction is done
 * by the scan method thereafter by the user of this class.
 */
public class FrameScanner extends TagScanner {
	/**
	 * Overriding the default constructor
	 */
	public FrameScanner() {
		super();
	}

	/**
	 * Overriding the constructor to accept the filter
	 */
	public FrameScanner(String filter) {
		super(filter);
	}

	/**
	 * Extract the location of the image, given the string to be parsed, and the
	 * url of the html page in which this tag exists.
	 * 
	 * @param s
	 *            String to be parsed
	 * @param url
	 *            URL of web page being parsed
	 */
	public String extractFrameLocn(Tag tag, String url) throws ParserException {
		try {
			Hashtable table = tag.getAttributes();
			String relativeFrame = (String) table.get("SRC");
			if (relativeFrame == null)
				return "";
			else
				return (new LinkProcessor()).extract(relativeFrame, url);
		} catch (Exception e) {
			String msg;
			if (tag != null)
				msg = tag.getText();
			else
				msg = "null";
			throw new ParserException(
					"HTMLFrameScanner.extractFrameLocn() : Error in extracting frame location from tag " + msg, e);
		}
	}

	public String extractFrameName(Tag tag, String url) {
		return tag.getAttribute("NAME");
	}

	/**
	 * @see org.htmlparser.scanners.TagScanner#getID()
	 */
	public String[] getID() {
		String[] ids = new String[1];
		ids[0] = "FRAME";
		return ids;
	}

	protected Tag createTag(TagData tagData, Tag tag, String url) throws ParserException {
		String frameUrl = extractFrameLocn(tag, url);
		String frameName = extractFrameName(tag, url);

		return new FrameTag(tagData, frameUrl, frameName);
	}

}
