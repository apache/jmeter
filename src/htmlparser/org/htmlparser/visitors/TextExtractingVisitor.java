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
package org.htmlparser.visitors;

import org.htmlparser.StringNode;
import org.htmlparser.tags.EndTag;
import org.htmlparser.tags.Tag;
import org.htmlparser.tags.TitleTag;
import org.htmlparser.util.Translate;

/**
 * Extracts text from a web page. Usage: <code>
 * Parser parser = new Parser(...);
 * TextExtractingVisitor visitor = new TextExtractingVisitor();
 * parser.visitAllNodesWith(visitor);
 * String textInPage = visitor.getExtractedText();
 * </code>
 */
public class TextExtractingVisitor extends NodeVisitor {
	private StringBuffer textAccumulator;

	private boolean preTagBeingProcessed;

	public TextExtractingVisitor() {
		textAccumulator = new StringBuffer();
		preTagBeingProcessed = false;
	}

	public String getExtractedText() {
		return textAccumulator.toString();
	}

	public void visitStringNode(StringNode stringNode) {
		String text = stringNode.getText();
		if (!preTagBeingProcessed) {
			text = Translate.decode(text);
			text = replaceNonBreakingSpaceWithOrdinarySpace(text);
		}
		textAccumulator.append(text);
	}

	public void visitTitleTag(TitleTag titleTag) {
		textAccumulator.append(titleTag.getTitle());
	}

	private String replaceNonBreakingSpaceWithOrdinarySpace(String text) {
		return text.replace('\u00a0', ' ');
	}

	public void visitEndTag(EndTag endTag) {
		if (isPreTag(endTag))
			preTagBeingProcessed = false;
	}

	public void visitTag(Tag tag) {
		if (isPreTag(tag))
			preTagBeingProcessed = true;
	}

	private boolean isPreTag(Tag tag) {
		return tag.getTagName().equals("PRE");
	}

}
