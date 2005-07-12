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

import org.htmlparser.tags.BgSoundTag;
import org.htmlparser.tags.Tag;
import org.htmlparser.tags.data.TagData;
import org.htmlparser.util.LinkProcessor;
import org.htmlparser.util.ParserException;
import org.htmlparser.util.ParserUtils;

/**
 * Scans for the bgsound Tag. This is a subclass of TagScanner, and is called
 * using a variant of the template method. If the evaluate() method returns
 * true, that means the given string contains an bgsound tag. Extraction is done
 * by the scan method thereafter by the user of this class.
 */
public class BgSoundScanner extends TagScanner {
	public static final String BGSOUND_SCANNER_ID = "BGSOUND";

	private Hashtable table;

	private LinkProcessor processor;

	/**
	 * Overriding the default constructor
	 */
	public BgSoundScanner() {
		super();
		processor = new LinkProcessor();
	}

	/**
	 * Overriding the constructor to accept the filter
	 */
	public BgSoundScanner(String filter, LinkProcessor processor) {
		super(filter);
		this.processor = processor;
	}

	/**
	 * Extract the location of the bgsound, given the string to be parsed, and
	 * the url of the html page in which this tag exists.
	 * 
	 * @param s
	 *            String to be parsed
	 * @param url
	 *            URL of web page being parsed
	 */
	public String extractBgSoundLocn(Tag tag, String url) throws ParserException {
		String relativeLink = null;
		try {
			table = tag.getAttributes();
			relativeLink = (String) table.get("SRC");

			if (relativeLink != null) {
				relativeLink = ParserUtils.removeChars(relativeLink, '\n');
				relativeLink = ParserUtils.removeChars(relativeLink, '\r');
			}
			if (relativeLink == null || relativeLink.length() == 0) {
				// try fix
				String tagText = tag.getText().toUpperCase();
				int indexSrc = tagText.indexOf("SRC");
				if (indexSrc != -1) {
					// There is a missing equals.
					tag.setText(tag.getText().substring(0, indexSrc + 3) + "="
							+ tag.getText().substring(indexSrc + 3, tag.getText().length()));
					table = tag.redoParseAttributes();
					relativeLink = (String) table.get("SRC");

				}
			}
			if (relativeLink == null)
				return "";
			else
				return processor.extract(relativeLink, url);
		} catch (Exception e) {
			throw new ParserException(
					"HTMLbgsoundScanner.extractbgsoundLocn() : Error in extracting image location, relativeLink = "
							+ relativeLink + ", url = " + url, e);
		}
	}

	public String[] getID() {
		String[] ids = new String[1];
		ids[0] = BGSOUND_SCANNER_ID;
		return ids;
	}

	protected Tag createTag(TagData tagData, Tag tag, String url) throws ParserException {
		String link = extractBgSoundLocn(tag, url);
		return new BgSoundTag(tagData, link);
	}

}
