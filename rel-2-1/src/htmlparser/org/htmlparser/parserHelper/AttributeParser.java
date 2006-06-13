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
package org.htmlparser.parserHelper;

import java.util.Hashtable;
import java.util.StringTokenizer;

import org.htmlparser.tags.Tag;

/**
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates. To enable and disable the creation of type
 * comments go to Window>Preferences>Java>Code Generation.
 * 
 * @author Somik Raha, Kaarle Kaila
 * @version 7 AUG 2001
 */
public class AttributeParser {
	private final String delima = " \t\r\n\f=\"'>";

	private final String delimb = " \t\r\n\f\"'>";

	private final char doubleQuote = '\"';

	private final char singleQuote = '\'';

	private String delim;

	/**
	 * Method to break the tag into pieces.
	 * 
	 * @param returns
	 *            a Hastable with elements containing the pieces of the tag. The
	 *            tag-name has the value field set to the constant Tag.TAGNAME.
	 *            In addition the tag-name is stored into the Hashtable with the
	 *            name Tag.TAGNAME where the value is the name of the tag. Tag
	 *            parameters without value has the value "". Parameters with
	 *            value are represented in the Hastable by a name/value pair. As
	 *            html is case insensitive but Hastable is not are all names
	 *            converted into UPPERCASE to the Hastable E.g extract the href
	 *            values from A-tag's and print them
	 * 
	 * <pre>
	 * 
	 * 
	 * Tag tag;
	 * Hashtable h;
	 * String tmp;
	 * try {
	 * 	NodeReader in = new NodeReader(new FileReader(path), 2048);
	 * 	Parser p = new Parser(in);
	 * 	Enumeration en = p.elements();
	 * 	while (en.hasMoreElements()) {
	 * 		try {
	 * 			tag = (Tag) en.nextElement();
	 * 			h = tag.parseParameters();
	 * 			tmp = (String) h.get(tag.TAGNAME);
	 * 			if (tmp != null &amp;&amp; tmp.equalsIgnoreCase(&quot;A&quot;)) {
	 * 				;
	 * 				System.out.println(&quot;URL is :&quot; + h.get(&quot;HREF&quot;));
	 * 			}
	 * 		} catch (ClassCastException ce) {
	 * 		}
	 * 	}
	 * } catch (IOException ie) {
	 * 	ie.printStackTrace();
	 * }
	 * </pre>
	 * 
	 */
	public Hashtable parseAttributes(Tag tag) {
		Hashtable h = new Hashtable();
		String element, name, value, nextPart = null;
		String empty = null;
		name = null;
		value = null;
		element = null;
		boolean waitingForEqual = false;
		delim = delima;
		StringTokenizer tokenizer = new StringTokenizer(tag.getText(), delim, true);
		while (true) {
			nextPart = getNextPart(tokenizer, delim);
			delim = delima;
			if (element == null && nextPart != null && !nextPart.equals("=")) {
				element = nextPart;
				putDataIntoTable(h, element, null, true);
			} else {
				if (nextPart != null) {
					if (name == null) {
						if (0 < nextPart.length() && !nextPart.substring(0, 1).equals(" ")) {
							name = nextPart;
							waitingForEqual = true;
						}
					} else {
						if (waitingForEqual) {
							if (nextPart.equals("=")) {
								waitingForEqual = false;
								delim = delimb;
							} else {
								putDataIntoTable(h, name, "", false);
								name = nextPart;
								value = null;
							}
						}
						if (!waitingForEqual && !nextPart.equals("=")) {
							value = nextPart;
							putDataIntoTable(h, name, value, false);
							name = null;
							value = null;
						}
					}
				} else {
					if (name != null) {
						if (name.equals("/")) {
							putDataIntoTable(h, Tag.EMPTYTAG, "", false);
						} else {
							putDataIntoTable(h, name, "", false);
						}
						name = null;
						value = null;
					}
					break;
				}
			}
		}
		if (null == element) // handle no tag contents
			putDataIntoTable(h, "", null, true);
		return h;
	}

	private String getNextPart(StringTokenizer tokenizer, String deli) {
		String tokenAccumulator = null;
		boolean isDoubleQuote = false;
		boolean isSingleQuote = false;
		boolean isDataReady = false;
		String currentToken;
		while (isDataReady == false && tokenizer.hasMoreTokens()) {
			currentToken = tokenizer.nextToken(deli);
			//
			// First let's combine tokens that are inside "" or ''
			//
			if (isDoubleQuote || isSingleQuote) {
				if (isDoubleQuote && currentToken.charAt(0) == doubleQuote) {
					isDoubleQuote = false;
					isDataReady = true;
				} else if (isSingleQuote && currentToken.charAt(0) == singleQuote) {
					isSingleQuote = false;
					isDataReady = true;
				} else {
					tokenAccumulator += currentToken;
					continue;
				}
			} else if (currentToken.charAt(0) == doubleQuote) {
				isDoubleQuote = true;
				tokenAccumulator = "";
				continue;
			} else if (currentToken.charAt(0) == singleQuote) {
				isSingleQuote = true;
				tokenAccumulator = "";
				continue;
			} else
				tokenAccumulator = currentToken;

			if (tokenAccumulator.equals(currentToken)) {

				if (delim.indexOf(tokenAccumulator) >= 0) {
					if (tokenAccumulator.equals("=")) {
						isDataReady = true;
					}
				} else {

					isDataReady = true;
				}
			} else
				isDataReady = true;

		}
		return tokenAccumulator;
	}

	private void putDataIntoTable(Hashtable h, String name, String value, boolean isName) {
		if (isName && value == null)
			value = Tag.TAGNAME;
		else if (value == null)
			value = ""; // Hashtable does not accept nulls
		if (isName) {
			// store tagname as tag.TAGNAME,tag
			h.put(value, name.toUpperCase());
		} else {
			// store tag parameters as NAME, value
			h.put(name.toUpperCase(), value);
		}
	}
}
