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

package org.htmlparser;

import org.htmlparser.util.NodeList;
import org.htmlparser.visitors.NodeVisitor;

/**
 * Normal text in the html document is identified and represented by this class.
 */
public class StringNode extends Node {
	public static final String STRING_FILTER = "-string";

	/**
	 * The text of the string.
	 */
	protected StringBuffer textBuffer;

	/**
	 * Constructor takes in the text string, beginning and ending posns.
	 * 
	 * @param text
	 *            The contents of the string line
	 * @param textBegin
	 *            The beginning position of the string
	 * @param textEnd
	 *            The ending positiong of the string
	 */
	public StringNode(StringBuffer textBuffer, int textBegin, int textEnd) {
		super(textBegin, textEnd);
		this.textBuffer = textBuffer;

	}

	/**
	 * Returns the text of the string line
	 */
	public String getText() {
		return textBuffer.toString();
	}

	/**
	 * Sets the string contents of the node.
	 * 
	 * @param The
	 *            new text for the node.
	 */
	public void setText(String text) {
		textBuffer = new StringBuffer(text);
	}

	public String toPlainTextString() {
		return textBuffer.toString();
	}

	public String toHtml() {
		return textBuffer.toString();
	}

	public String toString() {
		return "Text = " + getText() + "; begins at : " + elementBegin() + "; ends at : " + elementEnd();
	}

	public void collectInto(NodeList collectionList, String filter) {
		if (filter.equals(STRING_FILTER))
			collectionList.add(this);
	}

	public void accept(NodeVisitor visitor) {
		visitor.visitStringNode(this);
	}

}
