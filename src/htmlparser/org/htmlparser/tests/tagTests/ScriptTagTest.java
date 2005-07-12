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

import org.htmlparser.Parser;
import org.htmlparser.StringNode;
import org.htmlparser.scanners.ScriptScanner;
import org.htmlparser.tags.ScriptTag;
import org.htmlparser.tags.data.CompositeTagData;
import org.htmlparser.tags.data.TagData;
import org.htmlparser.tests.ParserTestCase;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

public class ScriptTagTest extends ParserTestCase {
	private ScriptScanner scriptScanner;

	public ScriptTagTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		scriptScanner = new ScriptScanner();
	}

	public void testCreation() {
		StringNode stringNode = new StringNode(new StringBuffer("Script Code"), 0, 0);
		NodeList childVector = new NodeList();
		childVector.add(stringNode);
		ScriptTag scriptTag = new ScriptTag(new TagData(0, 10, "Tag Contents", "tagline"), new CompositeTagData(null,
				null, childVector));

		assertNotNull("Script Tag object creation", scriptTag);
		assertEquals("Script Tag Begin", 0, scriptTag.elementBegin());
		assertEquals("Script Tag End", 10, scriptTag.elementEnd());
		assertEquals("Script Tag Contents", "Tag Contents", scriptTag.getText());
		assertEquals("Script Tag Code", "Script Code", scriptTag.getScriptCode());
		assertEquals("Script Tag Line", "tagline", scriptTag.getTagLine());
	}

	public void testToHTML() throws ParserException {
		createParser("<SCRIPT>document.write(d+\".com\")</SCRIPT>");
		// Register the image scanner
		parser.addScanner(new ScriptScanner("-s"));

		parseAndAssertNodeCount(1);
		assertTrue("Node should be a script tag", node[0] instanceof ScriptTag);
		// Check the data in the applet tag
		ScriptTag scriptTag = (ScriptTag) node[0];
		assertEquals("Expected Raw String", "<SCRIPT>document.write(d+\".com\")</SCRIPT>", scriptTag.toHtml());
	}

	/**
	 * Bug check by Wolfgang Germund 2002-06-02 Upon parsing : &lt;script
	 * language="javascript"&gt; if(navigator.appName.indexOf("Netscape") != -1)
	 * document.write ('xxx'); else document.write ('yyy'); &lt;/script&gt;
	 * check toRawString().
	 */
	public void testToHTMLWG() throws ParserException {
		StringBuffer sb1 = new StringBuffer();
		sb1.append("<body><script language=\"javascript\">\r\n");
		sb1.append("if(navigator.appName.indexOf(\"Netscape\") != -1)\r\n");
		sb1.append(" document.write ('xxx');\r\n");
		sb1.append("else\r\n");
		sb1.append(" document.write ('yyy');\r\n");
		sb1.append("</script>\r\n");
		String testHTML1 = new String(sb1.toString());

		createParser(testHTML1);
		Parser.setLineSeparator("\r\n");
		// Register the image scanner
		parser.addScanner(new ScriptScanner("-s"));

		StringBuffer sb2 = new StringBuffer();
		sb2.append("<SCRIPT LANGUAGE=\"javascript\">\r\n");
		sb2.append("if(navigator.appName.indexOf(\"Netscape\") != -1)\r\n");
		sb2.append(" document.write ('xxx');\r\n");
		sb2.append("else\r\n");
		sb2.append(" document.write ('yyy');\r\n");
		sb2.append("</SCRIPT>");
		String expectedHTML = new String(sb2.toString());

		parseAndAssertNodeCount(2);
		assertTrue("Node should be a script tag", node[1] instanceof ScriptTag);
		// Check the data in the applet tag
		ScriptTag scriptTag = (ScriptTag) node[1];
		assertStringEquals("Expected Script Code", expectedHTML, scriptTag.toHtml());
	}

	public void testParamExtraction() throws ParserException {
		StringBuffer sb1 = new StringBuffer();
		sb1.append("<script src=\"/adb.js\" language=\"javascript\">\r\n");
		sb1.append("if(navigator.appName.indexOf(\"Netscape\") != -1)\r\n");
		sb1.append(" document.write ('xxx');\r\n");
		sb1.append("else\r\n");
		sb1.append(" document.write ('yyy');\r\n");
		sb1.append("</script>\r\n");
		createParser(sb1.toString());

		// Register the image scanner
		parser.addScanner(new ScriptScanner("-s"));
		parseAndAssertNodeCount(1);
		assertTrue("Node should be a script tag", node[0] instanceof ScriptTag);
		ScriptTag scriptTag = (ScriptTag) node[0];
		assertEquals("Script Src", "/adb.js", scriptTag.getAttribute("src"));
		assertEquals("Script Language", "javascript", scriptTag.getAttribute("language"));
	}

	public void testVariableDeclarations() throws ParserException {
		StringBuffer sb1 = new StringBuffer();
		sb1.append("<script language=\"javascript\">\n");
		sb1.append("var lower = '<%=lowerValue%>';\n");
		sb1.append("</script>\n");
		createParser(sb1.toString());

		// Register the image scanner
		parser.addScanner(new ScriptScanner("-s"));
		// parser.registerScanners();
		parseAndAssertNodeCount(1);
		assertTrue("Node should be a script tag", node[0] instanceof ScriptTag);
		ScriptTag scriptTag = (ScriptTag) node[0];
		assertStringEquals("Script toHTML()",
				"<SCRIPT LANGUAGE=\"javascript\">\r\nvar lower = '<%=lowerValue%>';\r\n</SCRIPT>", scriptTag.toHtml());
	}

	public void testSingleApostropheParsingBug() throws ParserException {
		StringBuffer sb1 = new StringBuffer();
		sb1.append("<script src='<%=sourceFileName%>'></script>");
		createParser(sb1.toString());

		// Register the image scanner
		parser.addScanner(new ScriptScanner("-s"));
		parseAndAssertNodeCount(1);
		assertTrue("Node should be a script tag", node[0] instanceof ScriptTag);
		ScriptTag scriptTag = (ScriptTag) node[0];
		assertStringEquals("Script toHTML()", "<SCRIPT SRC=\"<%=sourceFileName%>\"></SCRIPT>", scriptTag.toHtml());
	}

}
