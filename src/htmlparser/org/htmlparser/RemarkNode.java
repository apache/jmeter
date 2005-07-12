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
 * The remark tag is identified and represented by this class.
 */
public class RemarkNode extends Node {
	public final static String REMARK_NODE_FILTER = "-r";

	/**
	 * Tag contents will have the contents of the comment tag.
	 */
	String tagContents;

	/**
	 * The HTMLRemarkTag is constructed by providing the beginning posn, ending
	 * posn and the tag contents.
	 * 
	 * @param nodeBegin
	 *            beginning position of the tag
	 * @param nodeEnd
	 *            ending position of the tag
	 * @param tagContents
	 *            contents of the remark tag
	 * @param tagLine
	 *            The current line being parsed, where the tag was found
	 */
	public RemarkNode(int tagBegin, int tagEnd, String tagContents) {
		super(tagBegin, tagEnd);
		this.tagContents = tagContents;
	}

	/**
	 * Returns the text contents of the comment tag.
	 */
	public String getText() {
		return tagContents;
	}

	public String toPlainTextString() {
		return tagContents;
	}

	public String toHtml() {
		return "<!--" + tagContents + "-->";
	}

	/**
	 * Print the contents of the remark tag.
	 */
	public String toString() {
		return "Comment Tag : " + tagContents + "; begins at : " + elementBegin() + "; ends at : " + elementEnd()
				+ "\n";
	}

	public void collectInto(NodeList collectionList, String filter) {
		if (filter.equals(REMARK_NODE_FILTER))
			collectionList.add(this);
	}

	public void accept(NodeVisitor visitor) {
		visitor.visitRemarkNode(this);
	}

}
