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

import org.htmlparser.Parser;
import org.htmlparser.tags.TableColumn;
import org.htmlparser.tags.TableRow;
import org.htmlparser.tags.Tag;
import org.htmlparser.tags.data.CompositeTagData;
import org.htmlparser.tags.data.TagData;
import org.htmlparser.util.NodeList;

public class TableRowScanner extends CompositeTagScanner {
	private final static String MATCH_STRING[] = { "TR" };

	public TableRowScanner(Parser parser) {
		this("", parser);
	}

	public TableRowScanner(String filter, Parser parser) {
		this(filter, parser, MATCH_STRING, new String[] {}, new String[] {}, false);
	}

	public TableRowScanner(String filter, Parser parser, String[] nameOfTagToMatch, String[] tagEnders,
			String[] endTagEnders, boolean allowSelfChildren) {
		super(filter, nameOfTagToMatch, tagEnders, endTagEnders, allowSelfChildren);
		parser.addScanner(new TableColumnScanner());
	}

	public Tag createTag(TagData tagData, CompositeTagData compositeTagData) {
		NodeList columns = compositeTagData.getChildren().searchFor(TableColumn.class);
		return new TableRow(tagData, compositeTagData, columns);
	}

	public String[] getID() {
		return MATCH_STRING;
	}

}
