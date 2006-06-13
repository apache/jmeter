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
package org.htmlparser.tests.utilTests;

import junit.framework.TestSuite;

/**
 * Insert the type's description here. Creation date: (6/17/2001 6:07:04 PM)
 * 
 * @author: Administrator
 */
public class AllTests extends junit.framework.TestCase {
	/**
	 * AllTests constructor comment.
	 * 
	 * @param name
	 *            java.lang.String
	 */
	public AllTests(String name) {
		super(name);
	}

	public static void main(String[] args) {
		new junit.awtui.TestRunner().start(new String[] { "org.htmlparser.tests.AllTests" });
	}

	/**
	 * Insert the method's description here. Creation date: (6/17/2001 6:07:15
	 * PM)
	 * 
	 * @return junit.framework.TestSuite
	 */
	public static TestSuite suite() {
		TestSuite suite = new TestSuite("Utility Tests");

		suite.addTestSuite(BeanTest.class);
		suite.addTestSuite(CharacterTranslationTest.class);
		suite.addTestSuite(HTMLLinkProcessorTest.class);
		suite.addTestSuite(HTMLParserUtilsTest.class);
		suite.addTestSuite(HTMLTagParserTest.class);
		suite.addTestSuite(NodeListTest.class);

		return suite;
	}
}
