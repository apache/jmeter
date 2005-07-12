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
//
// This class was contributed by Joshua Kerievsky
package org.htmlparser.visitors;

import org.htmlparser.Parser;
import org.htmlparser.StringNode;
import org.htmlparser.scanners.LinkScanner;
import org.htmlparser.tags.EndTag;
import org.htmlparser.tags.ImageTag;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.tags.Tag;

public class UrlModifyingVisitor extends NodeVisitor {
	private String linkPrefix;

	private StringBuffer modifiedResult;

	private Parser parser;

	public UrlModifyingVisitor(Parser parser, String linkPrefix) {
		super(true, false);
		this.parser = parser;
		LinkScanner linkScanner = new LinkScanner();
		parser.addScanner(linkScanner);
		parser.addScanner(linkScanner.createImageScanner(ImageTag.IMAGE_TAG_FILTER));
		this.linkPrefix = linkPrefix;
		modifiedResult = new StringBuffer();
	}

	public void visitLinkTag(LinkTag linkTag) {
		linkTag.setLink(linkPrefix + linkTag.getLink());
	}

	public void visitImageTag(ImageTag imageTag) {
		imageTag.setImageURL(linkPrefix + imageTag.getImageURL());
		modifiedResult.append(imageTag.toHtml());
	}

	public void visitEndTag(EndTag endTag) {
		modifiedResult.append(endTag.toHtml());
	}

	public void visitStringNode(StringNode stringNode) {
		modifiedResult.append(stringNode.toHtml());
	}

	public void visitTag(Tag tag) {
		modifiedResult.append(tag.toHtml());
	}

	public String getModifiedResult() {
		return modifiedResult.toString();
	}
}
