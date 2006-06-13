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
package org.htmlparser.tags;

import org.htmlparser.Node;
import org.htmlparser.tags.data.TagData;
import org.htmlparser.visitors.NodeVisitor;

/**
 * EndTag can identify closing tags, like &lt;/A&gt;, &lt;/FORM&gt;, etc.
 */
public class EndTag extends Tag {
	private final static String TYPE = "END_TAG";

	public final static int ENDTAG_BEFORE_PARSING_STATE = 0;

	public final static int ENDTAG_WAIT_FOR_SLASH_STATE = 1;

	public final static int ENDTAG_BEGIN_PARSING_STATE = 2;

	public final static int ENDTAG_FINISHED_PARSING_STATE = 3;

	/**
	 * Constructor takes 3 arguments to construct an EndTag object.
	 * 
	 * @param nodeBegin
	 *            Beginning position of the end tag
	 * @param nodeEnd
	 *            Ending position of the end tag
	 * @param tagContents
	 *            Text contents of the tag
	 */
	public EndTag(TagData tagData) {
		super(tagData);
	}

	/**
	 * Locate the end tag withing the input string, by parsing from the given
	 * position
	 * 
	 * @param input
	 *            Input String
	 * @param position
	 *            Position to start parsing from
	 */
	public static Node find(String input, int position) {
		int state = ENDTAG_BEFORE_PARSING_STATE;
		StringBuffer tagContents = new StringBuffer();
		int tagBegin = 0;
		int tagEnd = 0;
		int inputLen = input.length();
		char ch;
		int i;
		for (i = position; (i < inputLen && state != ENDTAG_FINISHED_PARSING_STATE); i++) {
			ch = input.charAt(i);
			if (ch == '>' && state == ENDTAG_BEGIN_PARSING_STATE) {
				state = ENDTAG_FINISHED_PARSING_STATE;
				tagEnd = i;
			}
			if (state == ENDTAG_BEGIN_PARSING_STATE) {
				tagContents.append(ch);
			}
			if (state == ENDTAG_WAIT_FOR_SLASH_STATE) {
				if (ch == '/') {
					state = ENDTAG_BEGIN_PARSING_STATE;
				} else
					return null;
			}

			if (ch == '<') {
				if (state == ENDTAG_BEFORE_PARSING_STATE) {
					// Transition from State 0 to State 1 - Record data till >
					// is encountered
					tagBegin = i;
					state = ENDTAG_WAIT_FOR_SLASH_STATE;
				} else if (state == ENDTAG_BEGIN_PARSING_STATE) {
					state = ENDTAG_FINISHED_PARSING_STATE;
					tagEnd = i;
				}
			} else if (state == ENDTAG_BEFORE_PARSING_STATE)
				// text before the end tag
				return (null);
		}
		// If parsing did not complete, it might be possible to accept
		if (state == ENDTAG_BEGIN_PARSING_STATE) {
			tagEnd = i;
			state = ENDTAG_FINISHED_PARSING_STATE;
		}
		if (state == ENDTAG_FINISHED_PARSING_STATE)
			return new EndTag(new TagData(tagBegin, tagEnd, tagContents.toString(), input));
		else
			return null;
	}

	public String toPlainTextString() {
		return "";
	}

	public String toHtml() {
		return "</" + getTagName() + ">";
	}

	public String toString() {
		return "EndTag : " + tagContents + "; begins at : " + elementBegin() + "; ends at : " + elementEnd();
	}

	public void accept(NodeVisitor visitor) {
		visitor.visitEndTag(this);
	}

	public String getType() {
		return TYPE;
	}

}
