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

import org.htmlparser.Node;
import org.htmlparser.NodeReader;
import org.htmlparser.scanners.CompositeTagScanner;
import org.htmlparser.tags.CompositeTag;
import org.htmlparser.tags.EndTag;
import org.htmlparser.tags.Tag;
import org.htmlparser.tags.data.CompositeTagData;
import org.htmlparser.tags.data.TagData;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

public class CompositeTagScannerHelper {
	private CompositeTagScanner scanner;

	private Tag tag;

	private String url;

	private NodeReader reader;

	private String currLine;

	private Tag endTag;

	private NodeList nodeList;

	private boolean endTagFound;

	private int startingLineNumber;

	private int endingLineNumber;

	private boolean balance_quotes;

	public CompositeTagScannerHelper(CompositeTagScanner scanner, Tag tag, String url, NodeReader reader,
			String currLine, boolean balance_quotes) {

		this.scanner = scanner;
		this.tag = tag;
		this.url = url;
		this.reader = reader;
		this.currLine = currLine;
		this.endTag = null;
		this.nodeList = new NodeList();
		this.endTagFound = false;
		this.balance_quotes = balance_quotes;
	}

	public Tag scan() throws ParserException {
		this.startingLineNumber = reader.getLastLineNumber();
		if (shouldCreateEndTagAndExit()) {
			return createEndTagAndRepositionReader();
		}
		scanner.beforeScanningStarts();
		Node currentNode = tag;

		doEmptyXmlTagCheckOn(currentNode);
		if (!endTagFound) {
			do {
				currentNode = reader.readElement(balance_quotes);
				if (currentNode == null)
					continue;
				currLine = reader.getCurrentLine();
				if (currentNode instanceof Tag)
					doForceCorrectionCheckOn((Tag) currentNode);

				doEmptyXmlTagCheckOn(currentNode);
				if (!endTagFound)
					doChildAndEndTagCheckOn(currentNode);
			} while (currentNode != null && !endTagFound);
		}
		if (endTag == null) {
			createCorrectionEndTagBefore(reader.getLastReadPosition() + 1);
		}

		this.endingLineNumber = reader.getLastLineNumber();
		return createTag();
	}

	private boolean shouldCreateEndTagAndExit() {
		return scanner.shouldCreateEndTagAndExit();
	}

	private Tag createEndTagAndRepositionReader() {
		createCorrectionEndTagBefore(tag.elementBegin());
		reader.setPosInLine(tag.elementBegin());
		reader.setDontReadNextLine(true);
		return endTag;
	}

	private void createCorrectionEndTagBefore(int pos) {
		String endTagName = tag.getTagName();
		int endTagBegin = pos;
		int endTagEnd = endTagBegin + endTagName.length() + 2;
		endTag = new EndTag(new TagData(endTagBegin, endTagEnd, endTagName, currLine));
	}

	private void createCorrectionEndTagBefore(Tag possibleEndTagCauser) {
		String endTagName = tag.getTagName();
		int endTagBegin = possibleEndTagCauser.elementBegin();
		int endTagEnd = endTagBegin + endTagName.length() + 2;
		possibleEndTagCauser.setTagBegin(endTagEnd + 1);
		reader.addNextParsedNode(possibleEndTagCauser);
		endTag = new EndTag(new TagData(endTagBegin, endTagEnd, endTagName, currLine));
	}

    // NOTUSED ??
	private StringBuffer createModifiedLine(String endTagName, int endTagBegin) {
		StringBuffer newLine = new StringBuffer();
		newLine.append(currLine.substring(0, endTagBegin));
		newLine.append("</");
		newLine.append(endTagName);
		newLine.append(">");
		newLine.append(currLine.substring(endTagBegin, currLine.length()));
		return newLine;
	}

	private Tag createTag() throws ParserException {
		CompositeTag newTag = (CompositeTag) scanner.createTag(new TagData(tag.elementBegin(), endTag.elementEnd(),
				startingLineNumber, endingLineNumber, tag.getText(), currLine, url, tag.isEmptyXmlTag()),
				new CompositeTagData(tag, endTag, nodeList));
		for (int i = 0; i < newTag.getChildCount(); i++) {
			Node child = newTag.childAt(i);
			child.setParent(newTag);
		}
		return newTag;
	}

	private void doChildAndEndTagCheckOn(Node currentNode) {
		if (currentNode instanceof EndTag) {
			EndTag possibleEndTag = (EndTag) currentNode;
			if (isExpectedEndTag(possibleEndTag)) {
				endTagFound = true;
				endTag = possibleEndTag;
				return;
			}
		}
		nodeList.add(currentNode);
		scanner.childNodeEncountered(currentNode);
	}

	private boolean isExpectedEndTag(EndTag possibleEndTag) {
		return possibleEndTag.getTagName().equals(tag.getTagName());
	}

	private void doEmptyXmlTagCheckOn(Node currentNode) {
		if (currentNode instanceof Tag) {
			Tag possibleEndTag = (Tag) currentNode;
			if (isXmlEndTag(tag)) {
				endTag = possibleEndTag;
				endTagFound = true;
			}
		}
	}

	private void doForceCorrectionCheckOn(Tag possibleEndTagCauser) {
		if (isEndTagMissing(possibleEndTagCauser)) {
			createCorrectionEndTagBefore(possibleEndTagCauser);

			endTagFound = true;
		}
	}

	private boolean isEndTagMissing(Tag possibleEndTag) {
		return scanner.isTagToBeEndedFor(possibleEndTag) || isSelfChildTagRecievedIncorrectly(possibleEndTag);
	}

	private boolean isSelfChildTagRecievedIncorrectly(Tag possibleEndTag) {
		return (!(possibleEndTag instanceof EndTag) && !scanner.isAllowSelfChildren() && possibleEndTag.getTagName()
				.equals(tag.getTagName()));
	}

	public boolean isXmlEndTag(Tag _tag) {
		String tagText = _tag.getText();
		int lastSlash = tagText.lastIndexOf("/");
		return (lastSlash == tagText.length() - 1 || _tag.isEmptyXmlTag()) && _tag.getText().indexOf("://") == -1;
	}
}
