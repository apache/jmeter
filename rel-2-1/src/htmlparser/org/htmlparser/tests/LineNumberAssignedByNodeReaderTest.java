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
package org.htmlparser.tests;

import java.util.Arrays;

import junit.framework.TestSuite;

import org.htmlparser.tests.scannersTests.CompositeTagScannerTest.CustomScanner;
import org.htmlparser.tests.scannersTests.CompositeTagScannerTest.CustomTag;
import org.htmlparser.util.ParserException;

/**
 * @author Somik Raha
 * 
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class LineNumberAssignedByNodeReaderTest extends ParserTestCase {

	public LineNumberAssignedByNodeReaderTest(String name) {
		super(name);
	}

	/**
	 * Test to ensure that the <code>Tag</code> being created by the
	 * <code>CompositeTagScanner</code> has the correct startLine and endLine
	 * information in the <code>TagData</code> it is constructed with.
	 * 
	 * @throws ParserException
	 *             if there is a problem parsing the test data
	 */
	public void testLineNumbers() throws ParserException {
		testLineNumber("<Custom/>", 1, 0, 1, 1);
		testLineNumber("<Custom />", 1, 0, 1, 1);
		testLineNumber("<Custom></Custom>", 1, 0, 1, 1);
		testLineNumber("<Custom>Content</Custom>", 1, 0, 1, 1);
		testLineNumber("<Custom>Content<Custom></Custom>", 1, 0, 1, 1);
		testLineNumber("<Custom>\n" + "	Content\n" + "</Custom>", 1, 0, 1, 3);
		testLineNumber("Foo\n" + "<Custom>\n" + "	Content\n" + "</Custom>", 2, 1, 2, 4);
		testLineNumber("Foo\n" + "<Custom>\n" + "	<Custom>SubContent</Custom>\n" + "</Custom>", 2, 1, 2, 4);
		char[] oneHundredNewLines = new char[100];
		Arrays.fill(oneHundredNewLines, '\n');
		testLineNumber("Foo\n" + new String(oneHundredNewLines) + "<Custom>\n" + "	<Custom>SubContent</Custom>\n"
				+ "</Custom>", 2, 1, 102, 104);
	}

	/**
	 * Helper method to ensure that the <code>Tag</code> being created by the
	 * <code>CompositeTagScanner</code> has the correct startLine and endLine
	 * information in the <code>TagData</code> it is constructed with.
	 * 
	 * @param xml
	 *            String containing HTML or XML to parse, containing a Custom
	 *            tag
	 * @param numNodes
	 *            int number of expected nodes returned by parser
	 * @param useNode
	 *            int index of the node to test (should be of type CustomTag)
	 * @param startLine
	 *            int the expected start line number of the tag
	 * @param endLine
	 *            int the expected end line number of the tag
	 * @throws ParserException
	 *             if there is an exception during parsing
	 */
	private void testLineNumber(String xml, int numNodes, int useNode, int expectedStartLine, int expectedEndLine)
			throws ParserException {
		createParser(xml);
		parser.addScanner(new CustomScanner());
		parseAndAssertNodeCount(numNodes);
		assertType("custom node", CustomTag.class, node[useNode]);
		CustomTag tag = (CustomTag) node[useNode];
		assertEquals("start line", expectedStartLine, tag.tagData.getStartLine());
		assertEquals("end line", expectedEndLine, tag.tagData.getEndLine());

	}

	public static TestSuite suite() {
		TestSuite suite = new TestSuite("Line Number Tests");
		suite.addTestSuite(LineNumberAssignedByNodeReaderTest.class);
		return (suite);
	}
}
