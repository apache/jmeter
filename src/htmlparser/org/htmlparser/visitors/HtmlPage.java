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

import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.RemarkNode;
import org.htmlparser.StringNode;
import org.htmlparser.scanners.TableScanner;
import org.htmlparser.tags.EndTag;
import org.htmlparser.tags.TableTag;
import org.htmlparser.tags.Tag;
import org.htmlparser.tags.TitleTag;
import org.htmlparser.util.NodeList;

public class HtmlPage extends NodeVisitor {
	private String title;

	private NodeList nodesInBody;

	private NodeList tables;

	private boolean bodyTagBegin;

	public HtmlPage(Parser parser) {
		super(false);
		parser.registerScanners();
		parser.addScanner(new TableScanner(parser));
		nodesInBody = new NodeList();
		tables = new NodeList();
		bodyTagBegin = false;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void visitTag(Tag tag) {
		addTagToBodyIfApplicable(tag);

		if (isTable(tag)) {
			tables.add(tag);
		} else {
			if (isBodyTag(tag))
				bodyTagBegin = true;
		}
	}

	private boolean isTitle(Tag tag) {
		return tag instanceof TitleTag;
	}

	private boolean isTable(Tag tag) {
		return tag instanceof TableTag;
	}

	private void addTagToBodyIfApplicable(Node node) {
		if (bodyTagBegin)
			nodesInBody.add(node);
	}

	public void visitEndTag(EndTag endTag) {
		if (isBodyTag(endTag))
			bodyTagBegin = false;
		addTagToBodyIfApplicable(endTag);
	}

	public void visitRemarkNode(RemarkNode remarkNode) {
		addTagToBodyIfApplicable(remarkNode);
	}

	public void visitStringNode(StringNode stringNode) {
		addTagToBodyIfApplicable(stringNode);
	}

	private boolean isBodyTag(Tag tag) {
		return tag.getTagName().equals("BODY");
	}

	public NodeList getBody() {
		return nodesInBody;
	}

	public TableTag[] getTables() {
		TableTag[] tableArr = new TableTag[tables.size()];
		for (int i = 0; i < tables.size(); i++)
			tableArr[i] = (TableTag) tables.elementAt(i);
		return tableArr;
	}

	public void visitTitleTag(TitleTag titleTag) {
		title = titleTag.getTitle();
	}

}
