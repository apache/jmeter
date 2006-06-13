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

import junit.framework.TestSuite;

public class AssertXmlEqualsTest extends ParserTestCase {

	public AssertXmlEqualsTest(String name) {
		super(name);
	}

	public void testNestedTagWithText() throws Exception {
		assertXmlEquals("nested with text", "<hello>   <hi>My name is Nothing</hi></hello>",
				"<hello><hi>My name is Nothing</hi>  </hello>");
	}

	public void testThreeTagsDifferent() throws Exception {
		assertXmlEquals("two tags different", "<someTag></someTag><someOtherTag>", "<someTag/><someOtherTag>");
	}

	public void testOneTag() throws Exception {
		assertXmlEquals("one tag", "<someTag>", "<someTag>");
	}

	public void testTwoTags() throws Exception {
		assertXmlEquals("two tags", "<someTag></someTag>", "<someTag></someTag>");
	}

	public void testTwoTagsDifferent() throws Exception {
		assertXmlEquals("two tags different", "<someTag></someTag>", "<someTag/>");
	}

	public void testTwoTagsDifferent2() throws Exception {
		assertXmlEquals("two tags different", "<someTag/>", "<someTag></someTag>");
	}

	public void testTwoTagsWithSameAttributes() throws Exception {
		assertXmlEquals("attributes", "<tag name=\"John\" age=\"22\" sex=\"M\"/>",
				"<tag sex=\"M\" name=\"John\" age=\"22\"/>");
	}

	public void testTagWithText() throws Exception {
		assertXmlEquals("text", "<hello>   My name is Nothing</hello>", "<hello>My name is Nothing  </hello>");
	}

	public void testStringWithLineBreaks() throws Exception {
		assertXmlEquals("string with line breaks", "testing & refactoring", "testing &\nrefactoring");
	}

	public static TestSuite suite() {
		TestSuite suite = new TestSuite("XML Tests");
		suite.addTestSuite(AssertXmlEqualsTest.class);
		return (suite);
	}
}
