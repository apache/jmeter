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
package org.htmlparser.tests.tagTests;

import org.htmlparser.*;
import org.htmlparser.tags.*;
import org.htmlparser.tests.*;
import org.htmlparser.util.*;

public class CompositeTagTest extends ParserTestCase {

	public CompositeTagTest(String name) {
		super(name);
	}

	public void testDigupStringNode() throws ParserException {
		createParser("<table>" + "<table>" + "<tr>" + "<td>" + "Hello World" + "</td>" + "</tr>" + "</table>"
				+ "</table>");
		parser.registerScanners();
		parseAndAssertNodeCount(1);
		TableTag tableTag = (TableTag) node[0];
		StringNode[] stringNode = tableTag.digupStringNode("Hello World");

		assertEquals("number of string nodes", 1, stringNode.length);
		assertNotNull("should have found string node", stringNode);
		CompositeTag parent = stringNode[0].getParent();
		assertType("should be column", TableColumn.class, parent);
		parent = parent.getParent();
		assertType("should be row", TableRow.class, parent);
		parent = parent.getParent();
		assertType("should be table", TableTag.class, parent);
		parent = parent.getParent();
		assertType("should be table again", TableTag.class, parent);
		assertSame("should be original table", tableTag, parent);
	}

	public void testFindPositionOf() throws ParserException {
		createParser("<table>" + "<table>" + "<tr>" + "<td>" + "Hi There<a><b>sdsd</b>" + "Hello World" + "</td>"
				+ "</tr>" + "</table>" + "</table>");
		parser.registerScanners();
		parseAndAssertNodeCount(1);
		TableTag tableTag = (TableTag) node[0];
		StringNode[] stringNode = tableTag.digupStringNode("Hello World");

		assertEquals("number of string nodes", 1, stringNode.length);
		assertNotNull("should have found string node", stringNode);
		CompositeTag parent = stringNode[0].getParent();
		int pos = parent.findPositionOf(stringNode[0]);
		assertEquals("position", 5, pos);
	}
}
