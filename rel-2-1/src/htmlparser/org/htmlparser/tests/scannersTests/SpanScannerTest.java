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
package org.htmlparser.tests.scannersTests;

import org.htmlparser.Node;
import org.htmlparser.scanners.SpanScanner;
import org.htmlparser.scanners.TableScanner;
import org.htmlparser.tags.Span;
import org.htmlparser.tags.TableColumn;
import org.htmlparser.tests.ParserTestCase;

public class SpanScannerTest extends ParserTestCase {

	private static final String HTML_WITH_SPAN = "<TD BORDER=\"0.0\" VALIGN=\"Top\" COLSPAN=\"4\" WIDTH=\"33.33%\">"
			+ "	<DIV>"
			+ "		<SPAN>Flavor: small(90 to 120 minutes)<BR /></SPAN>"
			+ "		<SPAN>The short version of our Refactoring Challenge gives participants a general feel for the smells in the code base and includes time for participants to find and implement important refactorings.&#013;<BR /></SPAN>"
			+ "	</DIV>" + "</TD>";

	public SpanScannerTest(String name) {
		super(name);
	}

	public void testScan() throws Exception {
		createParser(HTML_WITH_SPAN);
		parser.addScanner(new TableScanner(parser));
		parser.addScanner(new SpanScanner());
		parseAndAssertNodeCount(1);
		assertType("node", TableColumn.class, node[0]);
		TableColumn col = (TableColumn) node[0];
		Node spans[] = col.searchFor(Span.class).toNodeArray();
		assertEquals("number of spans found", 2, spans.length);
		assertStringEquals("span 1", "Flavor: small(90 to 120 minutes)", spans[0].toPlainTextString());
		assertStringEquals(
				"span 2",
				"The short version of our Refactoring Challenge gives participants a general feel for the smells in the code base and includes time for participants to find and implement important refactorings.&#013;",
				spans[1].toPlainTextString());

	}
}
