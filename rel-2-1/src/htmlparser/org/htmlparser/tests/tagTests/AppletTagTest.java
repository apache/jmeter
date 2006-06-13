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

import java.util.Hashtable;

import org.htmlparser.tags.AppletTag;
import org.htmlparser.tests.ParserTestCase;
import org.htmlparser.util.ParserException;

public class AppletTagTest extends ParserTestCase {

	public AppletTagTest(String name) {
		super(name);
	}

	public void testToHTML() throws ParserException {
		String[][] paramsData = { { "Param1", "Value1" }, { "Name", "Somik" }, { "Age", "23" } };
		Hashtable paramsMap = new Hashtable();
		String testHTML = new String("<APPLET CODE=Myclass.class ARCHIVE=test.jar CODEBASE=www.kizna.com>\n");
		for (int i = 0; i < paramsData.length; i++) {
			testHTML += "<PARAM NAME=\"" + paramsData[i][0] + "\" VALUE=\"" + paramsData[i][1] + "\">\n";
			paramsMap.put(paramsData[i][0], paramsData[i][1]);
		}
		testHTML += "</APPLET>\n" + "</HTML>";
		createParser(testHTML);
		// Register the image scanner
		parser.registerScanners();
		parseAndAssertNodeCount(2);
		assertTrue("Node should be an applet tag", node[0] instanceof AppletTag);
		// Check the data in the applet tag
		AppletTag appletTag = (AppletTag) node[0];
		String expectedRawString = "<APPLET CODE=\"Myclass.class\" CODEBASE=\"www.kizna.com\" ARCHIVE=\"test.jar\">\r\n"
				+ "<PARAM VALUE=\"Value1\" NAME=\"Param1\">\r\n"
				+ "<PARAM VALUE=\"Somik\" NAME=\"Name\">\r\n"
				+ "<PARAM VALUE=\"23\" NAME=\"Age\">\r\n" + "</APPLET>";
		assertStringEquals("toHTML()", expectedRawString, appletTag.toHtml());
	}
}
