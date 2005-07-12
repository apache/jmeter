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

import java.util.Hashtable;

import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.scanners.ScriptScanner;
import org.htmlparser.tags.ScriptTag;
import org.htmlparser.tests.ParserTestCase;
import org.htmlparser.util.ParserException;

public class ScriptScannerTest extends ParserTestCase {

	public ScriptScannerTest(String name) {
		super(name);
	}

	public void testEvaluate() {
		ScriptScanner scanner = new ScriptScanner("-s");
		boolean retVal = scanner.evaluate("   script ", null);
		assertEquals("Evaluation of SCRIPT tag", new Boolean(true), new Boolean(retVal));
	}

	public void testScan() throws ParserException {
		String testHtml = "<SCRIPT>document.write(d+\".com\")</SCRIPT>";
		createParser(testHtml, "http://www.google.com/test/index.html");
		// Register the script scanner
		parser.addScanner(new ScriptScanner("-s"));
		parseAndAssertNodeCount(1);
		assertTrue("Node should be a script tag", node[0] instanceof ScriptTag);
		// Check the data in the applet tag
		ScriptTag scriptTag = (ScriptTag) node[0];
		assertStringEquals("Expected Script Code", "document.write(d+\".com\")", scriptTag.getScriptCode());
		assertStringEquals("script tag html", testHtml, scriptTag.toHtml());
	}

	/**
	 * Bug reported by Gordon Deudney 2002-03-27 Upon parsing : &lt;SCRIPT
	 * LANGUAGE="JavaScript" SRC="../js/DetermineBrowser.js"&gt;&lt;/SCRIPT&gt;
	 * the SRC data cannot be retrieved.
	 */
	public void testScanBug() throws ParserException {
		createParser("<SCRIPT LANGUAGE=\"JavaScript\" SRC=\"../js/DetermineBrowser.js\"></SCRIPT>",
				"http://www.google.com/test/index.html");
		// Register the image scanner
		parser.addScanner(new ScriptScanner("-s"));
		parseAndAssertNodeCount(1);
		assertTrue("Node should be a script tag", node[0] instanceof ScriptTag);
		// Check the data in the applet tag
		ScriptTag scriptTag = (ScriptTag) node[0];
		Hashtable table = scriptTag.getAttributes();
		String srcExpected = (String) table.get("SRC");
		assertEquals("Expected SRC value", "../js/DetermineBrowser.js", srcExpected);
	}

	/**
	 * Bug check by Wolfgang Germund 2002-06-02 Upon parsing : &lt;script
	 * language="javascript"&gt; if(navigator.appName.indexOf("Netscape") != -1)
	 * document.write ('xxx'); else document.write ('yyy'); &lt;/script&gt;
	 * check getScriptCode().
	 */
	public void testScanBugWG() throws ParserException {
		StringBuffer sb1 = new StringBuffer();
		sb1.append("<body><script language=\"javascript\">\r\n");
		sb1.append("if(navigator.appName.indexOf(\"Netscape\") != -1)\r\n");
		sb1.append(" document.write ('xxx');\r\n");
		sb1.append("else\r\n");
		sb1.append(" document.write ('yyy');\r\n");
		sb1.append("</script>\r\n");
		String testHTML1 = new String(sb1.toString());

		createParser(testHTML1, "http://www.google.com/test/index.html");
		Parser.setLineSeparator("\r\n");
		// Register the image scanner
		parser.addScanner(new ScriptScanner("-s"));

		parseAndAssertNodeCount(2);

		StringBuffer sb2 = new StringBuffer();
		sb2.append("if(navigator.appName.indexOf(\"Netscape\") != -1)\r\n");
		sb2.append(" document.write ('xxx');\r\n");
		sb2.append("else\r\n");
		sb2.append(" document.write ('yyy');\r\n");
		String testHTML2 = new String(sb2.toString());

		assertTrue("Node should be a script tag", node[1] instanceof ScriptTag);
		// Check the data in the applet tag
		ScriptTag scriptTag = (ScriptTag) node[1];
		String s = scriptTag.getScriptCode();
		assertStringEquals("Expected Script Code", testHTML2, s);
	}

	public void testScanScriptWithLinks() throws ParserException {
		StringBuffer sb1 = new StringBuffer();
		sb1.append("<script type=\"text/javascript\">\r\n" + "<A HREF=\"http://thisisabadlink.com\">\r\n"
				+ "</script>\r\n");
		String testHTML1 = new String(sb1.toString());

		createParser(testHTML1, "http://www.hardwareextreme.com/");
		// Register the image scanner
		parser.registerScanners();
		// parser.addScanner(new HTMLScriptScanner("-s"));

		parseAndAssertNodeCount(1);
		assertTrue("Node should be a script tag", node[0] instanceof ScriptTag);
		// Check the data in the applet tag
		ScriptTag scriptTag = (ScriptTag) node[0];
		// assertStringEquals("Expected Script
		// Code",testHTML2,scriptTag.getScriptCode());
	}

	public void testScanScriptWithComments() throws ParserException {
		createParser("<SCRIPT Language=\"JavaScript\">\n" + "<!--\n" + "  function validateForm()\n" + "  {\n"
				+ "     var i = 10;\n" + "     if(i < 5)\n" + "     i = i - 1 ; \n" + "     return true;\n" + "  }\n"
				+ "// -->\n" + "</SCRIPT>", "http://www.hardwareextreme.com/");
		// Register the image scanner
		parser.registerScanners();
		parseAndAssertNodeCount(1);
		assertTrue("Node should be a script tag", node[0] instanceof ScriptTag);
		// Check the data in the applet tag
		ScriptTag scriptTag = (ScriptTag) node[0];
		String scriptCode = scriptTag.getScriptCode();
		String expectedCode = "<!--\r\n" + "  function validateForm()\r\n" + "  {\r\n" + "     var i = 10;\r\n"
				+ "     if(i < 5)\r\n" + "     i = i - 1 ; \r\n" + "     return true;\r\n" + "  }\r\n" + "// -->";
		assertStringEquals("Expected Code", expectedCode, scriptCode);
	}

	/**
	 * Submitted by Dhaval Udani - reproducing bug 664404
	 * 
	 * @throws ParserException
	 */
	public void testScriptTagComments() throws ParserException {
		String testHtml = "<SCRIPT LANGUAGE=\"JavaScript\">\r\n" + "<!--\r\n" + "// -->\r\n" + "</SCRIPT>";
		createParser(testHtml);

		parser.addScanner(new ScriptScanner("-s"));
		parseAndAssertNodeCount(1);
		ScriptTag scriptTag = (ScriptTag) node[0];
		assertStringEquals("scriptag html", testHtml, scriptTag.toHtml());
	}

	/**
	 * Duplicates bug reported by James Moliere - whereby, if script tags are
	 * generated by script code, the parser interprets them as real tags. The
	 * problem was that the string parser was not moving to the ignore state on
	 * encountering double quotes (only single quotes were previously accepted).
	 * 
	 * @throws Exception
	 */
	public void testScriptTagsGeneratedByScriptCode() throws Exception {
		createParser("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 " + "Transitional//EN\">" + "<html>" + "<head>"
				+ "<title>Untitled Document</title>" + "<meta http-equiv=\"Content-Type\" content=\"text/html; "
				+ "charset=iso-8859-1\">" + "</head>" + "<script language=\"JavaScript\">"
				+ "document.write(\"<script " + "language=\\\"JavaScript\\\">\");"
				+ "document.write(\"function onmousedown" + "(event)\");" + "document.write(\"{ // do something\"); "
				+ "document.write(\"}\"); " + "// parser thinks this is the end tag. "
				+ "document.write(\"</script>\");" + "</script>" + "<body>" + "</body>" + "</html>");
		parser.registerScanners();
		Node scriptNodes[] = parser.extractAllNodesThatAre(ScriptTag.class);
		assertType("scriptnode", ScriptTag.class, scriptNodes[0]);
		ScriptTag scriptTag = (ScriptTag) scriptNodes[0];
		assertStringEquals("script code", "document.write(\"<script " + "language=\\\"JavaScript\\\">\");"
				+ "document.write(\"function onmousedown" + "(event)\");" + "document.write(\"{ // do something\"); "
				+ "document.write(\"}\"); " + "// parser thinks this is the end tag. "
				+ "document.write(\"</script>\");", scriptTag.getScriptCode());

	}

	public void testScriptCodeExtraction() throws ParserException {
		createParser("<SCRIPT language=JavaScript>" + "document.write(\"<a href=\"1.htm\"><img src=\"1.jpg\" "
				+ "width=\"80\" height=\"20\" border=\"0\"></a>\");" + "</SCRIPT>");
		parser.registerScanners();
		parseAndAssertNodeCount(1);
		assertType("script", ScriptTag.class, node[0]);
		ScriptTag scriptTag = (ScriptTag) node[0];
		assertStringEquals("script code", "document.write(\"<a href=\"1.htm\"><img src=\"1.jpg\" "
				+ "width=\"80\" height=\"20\" border=\"0\"></a>\");", scriptTag.getScriptCode());
	}

	public void testScriptCodeExtractionWithMultipleQuotes() throws ParserException {
		createParser("<SCRIPT language=JavaScript>" + "document.write(\"<a href=\\\"1.htm\\\"><img src=\\\"1.jpg\\\" "
				+ "width=\\\"80\\\" height=\\\"20\\\" border=\\\"0\\\"></a>\");" + "</SCRIPT>");
		parser.registerScanners();
		parseAndAssertNodeCount(1);
		assertType("script", ScriptTag.class, node[0]);
		ScriptTag scriptTag = (ScriptTag) node[0];
		assertStringEquals("script code", "document.write(\"<a href=\\\"1.htm\\\"><img src=\\\"1.jpg\\\" "
				+ "width=\\\"80\\\" height=\\\"20\\\" border=\\\"0\\\"></a>\");", scriptTag.getScriptCode());
	}

	public void testScriptWithinComments() throws Exception {
		createParser("<script language=\"JavaScript1.2\">"
				+ "\n"
				+ "var linkset=new Array()"
				+ "\n"
				+ "var ie4=document.all&&navigator.userAgent.indexOf(\"Opera\")==-1"
				+ "\n"
				+ "var ns6=document.getElementById&&!document.all"
				+ "\n"
				+ "var ns4=document.layers"
				+ "\n"
				+ "\n"
				+ "\n"
				+ "function showmenu(e,which){"
				+ "\n"
				+ "\n"
				+ "\n"
				+ "if (!document.all&&!document.getElementById&&!document.layers)"
				+ "\n"
				+ "return"
				+ "\n"
				+ "\n"
				+ "\n"
				+ "clearhidemenu()"
				+ "\n"
				+ "\n"
				+ "\n"
				+ "menuobj=ie4? document.all.popmenu : ns6? document.getElementById(\"popmenu\") : ns4? document.popmenu : \"\"\n"
				+ "\n"
				+ "menuobj.thestyle=(ie4||ns6)? menuobj.style : menuobj"
				+ "\n"
				+ "\n"
				+ "\n"
				+ "if (ie4||ns6)"
				+ "\n"
				+ "menuobj.innerHTML=which"
				+ "\n"
				+ "else{"
				+ "\n"
				+ "menuobj.document.write('<layer name=gui bgColor=#E6E6E6 width=165 onmouseover=\"clearhidemenu()\" onmouseout=\"hidemenu()\">'+which+'</layer>')"
				+ "\n"
				+ "menuobj.document.close()"
				+ "\n"
				+ "}"
				+ "\n"
				+ "\n"
				+ "\n"
				+ "menuobj.contentwidth=(ie4||ns6)? menuobj.offsetWidth : menuobj.document.gui.document.width"
				+ "\n"
				+ "menuobj.contentheight=(ie4||ns6)? menuobj.offsetHeight : menuobj.document.gui.document.height"
				+ "\n"
				+ "eventX=ie4? event.clientX : ns6? e.clientX : e.x"
				+ "\n"
				+ "eventY=ie4? event.clientY : ns6? e.clientY : e.y"
				+ "\n"
				+ "\n"
				+ "\n"
				+ "//Find out how close the mouse is to the corner of the window"
				+ "\n"
				+ "var rightedge=ie4? document.body.clientWidth-eventX : window.innerWidth-eventX"
				+ "\n"
				+ "var bottomedge=ie4? document.body.clientHeight-eventY : window.innerHeight-eventY"
				+ "\n"
				+ "\n"
				+ "\n"
				+ "//if the horizontal distance isn't enough to accomodate the width of the context menu"
				+ "\n"
				+ "if (rightedge < menuobj.contentwidth)"
				+ "\n"
				+ "//move the horizontal position of the menu to the left by it's width"
				+ "\n"
				+ "menuobj.thestyle.left=ie4? document.body.scrollLeft+eventX-menuobj.contentwidth : ns6? window.pageXOffset+eventX-menuobj.contentwidth : eventX-menuobj.contentwidth"
				+ "\n"
				+ "else"
				+ "\n"
				+ "//position the horizontal position of the menu where the mouse was clicked"
				+ "\n"
				+ "menuobj.thestyle.left=ie4? document.body.scrollLeft+eventX : ns6? window.pageXOffset+eventX : eventX"
				+ "\n"
				+ "\n"
				+ "\n"
				+ "//same concept with the vertical position"
				+ "\n"
				+ "if (bottomedge<menuobj.contentheight)"
				+ "\n"
				+ "menuobj.thestyle.top=ie4? document.body.scrollTop+eventY-menuobj.contentheight : ns6? window.pageYOffset+eventY-menuobj.contentheight : eventY-menuobj.contentheight"
				+ "\n"
				+ "else"
				+ "\n"
				+ "menuobj.thestyle.top=ie4? document.body.scrollTop+event.clientY : ns6? window.pageYOffset+eventY : eventY"
				+ "\n" + "menuobj.thestyle.visibility=\"visible\"\n" + "\n" + "return false" + "\n" + "}" + "\n" + "\n"
				+ "\n" + "function contains_ns6(a, b) {" + "\n"
				+ "//Determines if 1 element in contained in another- by Brainjar.com" + "\n" + "while (b.parentNode)"
				+ "\n" + "if ((b = b.parentNode) == a)" + "\n" + "return true;" + "\n" + "return false;" + "\n" + "}"
				+ "\n" + "\n" + "\n" + "function hidemenu(){" + "\n" + "if (window.menuobj)" + "\n"
				+ "menuobj.thestyle.visibility=(ie4||ns6)? \"hidden\" : \"hide\"\n" + "\n" + "}" + "\n" + "\n" + "\n"
				+ "function dynamichide(e){" + "\n" + "if (ie4&&!menuobj.contains(e.toElement))" + "\n" + "hidemenu()"
				+ "\n"
				+ "else if (ns6&&e.currentTarget!= e.relatedTarget&& !contains_ns6(e.currentTarget, e.relatedTarget))"
				+ "\n" + "hidemenu()" + "\n" + "}" + "\n" + "\n" + "\n" + "function delayhidemenu(){" + "\n"
				+ "if (ie4||ns6||ns4)" + "\n" + "delayhide=setTimeout(\"hidemenu()\",500)" + "\n" + "}" + "\n" + "\n"
				+ "\n" + "function clearhidemenu(){" + "\n" + "if (window.delayhide)" + "\n"
				+ "clearTimeout(delayhide)" + "\n" + "}" + "\n" + "\n" + "\n" + "function highlightmenu(e,state){"
				+ "\n" + "if (document.all)" + "\n" + "source_el=event.srcElement" + "\n"
				+ "else if (document.getElementById)" + "\n" + "source_el=e.target" + "\n"
				+ "if (source_el.className==\"menuitems\"){" + "\n"
				+ "source_el.id=(state==\"on\")? \"mouseoverstyle\" : \"\"\n" + "\n" + "}" + "\n" + "else{" + "\n"
				+ "while(source_el.id!=\"popmenu\"){" + "\n"
				+ "source_el=document.getElementById? source_el.parentNode : source_el.parentElement" + "\n"
				+ "if (source_el.className==\"menuitems\"){" + "\n"
				+ "source_el.id=(state==\"on\")? \"mouseoverstyle\" : \"\"\n" + "\n" + "}" + "\n" + "}" + "\n" + "}"
				+ "\n" + "}" + "\n" + "\n" + "\n" + "if (ie4||ns6)" + "\n" + "document.onclick=hidemenu" + "\n" + "\n"
				+ "\n" + "</script>");
		parser.registerScanners();
		parseAndAssertNodeCount(1);

	}

	/**
	 * There was a bug in the ScriptScanner when there was multiline script and
	 * the last line did not have a newline before the end script tag. For
	 * example:
	 * 
	 * &lt;script&gt;alert() alert()&lt;/script&gt;
	 * 
	 * Would generate the following "scriptCode()" result: alert()alert()
	 * 
	 * But should actually return: alert() alert()
	 * 
	 * This was fixed in ScriptScanner, which this test verifies
	 */
	public void testScriptCodeExtractionWithNewlines() throws ParserException {
		String scriptContents = "alert()\r\nalert()";
		createParser("<script>" + scriptContents + "</script>");
		parser.registerScanners();
		parseAndAssertNodeCount(1);
		assertType("script", ScriptTag.class, node[0]);
		ScriptTag scriptTag = (ScriptTag) node[0];
		assertStringEquals("script code", scriptContents, scriptTag.getScriptCode());
	}

	/**
	 * Tests a bug in ScriptScanner where a NPE would be thrown if the script
	 * tag was not closed before the document ended.
	 */
	public void testScanNoEndTag() throws ParserException {
		createParser("<script>");
		parser.addScanner(new ScriptScanner("-s"));
		parseAndAssertNodeCount(1);
	}

	/**
	 * See bug #741769 ScriptScanner doesn't handle quoted </script> tags
	 */
	public void testScanQuotedEndTag() throws ParserException {
		createParser("<SCRIPT language=\"JavaScript\">document.write('</SCRIPT>');</SCRIPT>");
		parser.addScanner(new ScriptScanner("-s"));
		parseAndAssertNodeCount(1);
		String s = node[0].toHtml();
		assertEquals("Parse error", "<SCRIPT LANGUAGE=\"JavaScript\">document.write('</SCRIPT>');</SCRIPT>", s);
	}
}
