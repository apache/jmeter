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
import org.htmlparser.Parser;
import org.htmlparser.RemarkNode;
import org.htmlparser.scanners.FormScanner;
import org.htmlparser.scanners.LinkScanner;
import org.htmlparser.tags.FormTag;
import org.htmlparser.tags.InputTag;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.tags.TextareaTag;
import org.htmlparser.tests.ParserTestCase;
import org.htmlparser.util.ParserException;
import org.htmlparser.util.SimpleNodeIterator;

public class FormScannerTest extends ParserTestCase {
	public static final String FORM_HTML = "<FORM METHOD=\""
			+ FormTag.POST
			+ "\" ACTION=\"do_login.php\" NAME=\"login_form\" onSubmit=\"return CheckData()\">\n"
			+ "<TR><TD ALIGN=\"center\">&nbsp;</TD></TR>\n"
			+ "<TR><TD ALIGN=\"center\"><FONT face=\"Arial, verdana\" size=2><b>User Name</b></font></TD></TR>\n"
			+ "<TR><TD ALIGN=\"center\"><INPUT TYPE=\"text\" NAME=\"name\" SIZE=\"20\"></TD></TR>\n"
			+ "<TR><TD ALIGN=\"center\"><FONT face=\"Arial, verdana\" size=2><b>Password</b></font></TD></TR>\n"
			+ "<TR><TD ALIGN=\"center\"><INPUT TYPE=\"password\" NAME=\"passwd\" SIZE=\"20\"></TD></TR>\n"
			+ "<TR><TD ALIGN=\"center\">&nbsp;</TD></TR>\n"
			+ "<TR><TD ALIGN=\"center\"><INPUT TYPE=\"submit\" NAME=\"submit\" VALUE=\"Login\"></TD></TR>\n"
			+ "<TR><TD ALIGN=\"center\">&nbsp;</TD></TR>\n"
			+ "<TEXTAREA name=\"Description\" rows=\"15\" cols=\"55\" wrap=\"virtual\" class=\"composef\" tabindex=\"5\">Contents of TextArea</TEXTAREA>\n"
			+
			// "<TEXTAREA name=\"AnotherDescription\" rows=\"15\" cols=\"55\"
			// wrap=\"virtual\" class=\"composef\" tabindex=\"5\">\n"+
			"<INPUT TYPE=\"hidden\" NAME=\"password\" SIZE=\"20\">\n" + "<INPUT TYPE=\"submit\">\n" + "</FORM>";

	public static final String EXPECTED_FORM_HTML_FORMLINE = "<FORM ACTION=\"http://www.google.com/test/do_login.php\" NAME=\"login_form\" ONSUBMIT=\"return CheckData()\" METHOD=\""
			+ FormTag.POST + "\">\r\n";

	public static final String EXPECTED_FORM_HTML_REST_OF_FORM = "<TR><TD ALIGN=\"center\">&nbsp;</TD></TR>\r\n"
			+ "<TR><TD ALIGN=\"center\"><FONT FACE=\"Arial, verdana\" SIZE=\"2\"><B>User Name</B></FONT></TD></TR>\r\n"
			+ "<TR><TD ALIGN=\"center\"><INPUT NAME=\"name\" SIZE=\"20\" TYPE=\"text\"></TD></TR>\r\n"
			+ "<TR><TD ALIGN=\"center\"><FONT FACE=\"Arial, verdana\" SIZE=\"2\"><B>Password</B></FONT></TD></TR>\r\n"
			+ "<TR><TD ALIGN=\"center\"><INPUT NAME=\"passwd\" SIZE=\"20\" TYPE=\"password\"></TD></TR>\r\n"
			+ "<TR><TD ALIGN=\"center\">&nbsp;</TD></TR>\r\n"
			+ "<TR><TD ALIGN=\"center\"><INPUT VALUE=\"Login\" NAME=\"submit\" TYPE=\"submit\"></TD></TR>\r\n"
			+ "<TR><TD ALIGN=\"center\">&nbsp;</TD></TR>\r\n"
			+ "<TEXTAREA TABINDEX=\"5\" ROWS=\"15\" COLS=\"55\" CLASS=\"composef\" NAME=\"Description\" WRAP=\"virtual\">Contents of TextArea</TEXTAREA>\r\n"
			+
			// "<TEXTAREA TABINDEX=\"5\" ROWS=\"15\" COLS=\"55\"
			// CLASS=\"composef\" NAME=\"AnotherDescription\"
			// WRAP=\"virtual\">\r\n"+
			"<INPUT NAME=\"password\" SIZE=\"20\" TYPE=\"hidden\">\r\n" + "<INPUT TYPE=\"submit\">\r\n" + "</FORM>";

	public static final String EXPECTED_FORM_HTML = EXPECTED_FORM_HTML_FORMLINE + EXPECTED_FORM_HTML_REST_OF_FORM;

	public FormScannerTest(String name) {
		super(name);
	}

	public void testEvaluate() {
		String line1 = "form method=\"post\" onsubmit=\"return implementsearch()\" name=frmsearch id=form";
		String line2 = "FORM method=\"post\" onsubmit=\"return implementsearch()\" name=frmsearch id=form";
		String line3 = "Form method=\"post\" onsubmit=\"return implementsearch()\" name=frmsearch id=form";
		FormScanner formScanner = new FormScanner("", Parser.createParser(""));
		assertTrue("Line 1", formScanner.evaluate(line1, null));
		assertTrue("Line 2", formScanner.evaluate(line2, null));
		assertTrue("Line 3", formScanner.evaluate(line3, null));
	}

	public void assertTypeNameSize(String description, String type, String name, String size, InputTag inputTag) {
		assertEquals(description + " type", type, inputTag.getAttribute("TYPE"));
		assertEquals(description + " name", name, inputTag.getAttribute("NAME"));
		assertEquals(description + " size", size, inputTag.getAttribute("SIZE"));
	}

	public void assertTypeNameValue(String description, String type, String name, String value, InputTag inputTag) {
		assertEquals(description + " type", type, inputTag.getAttribute("TYPE"));
		assertEquals(description + " name", name, inputTag.getAttribute("NAME"));
		assertEquals(description + " value", value, inputTag.getAttribute("VALUE"));
	}

	public void testScan() throws ParserException {
		createParser(FORM_HTML, "http://www.google.com/test/index.html");
		parser.addScanner(new FormScanner("", parser));
		parseAndAssertNodeCount(1);
		assertTrue("Node 0 should be Form Tag", node[0] instanceof FormTag);
		FormTag formTag = (FormTag) node[0];
		assertStringEquals("Method", FormTag.POST, formTag.getFormMethod());
		assertStringEquals("Location", "http://www.google.com/test/do_login.php", formTag.getFormLocation());
		assertStringEquals("Name", "login_form", formTag.getFormName());
		InputTag nameTag = formTag.getInputTag("name");
		InputTag passwdTag = formTag.getInputTag("passwd");
		InputTag submitTag = formTag.getInputTag("submit");
		InputTag dummyTag = formTag.getInputTag("dummy");
		assertNotNull("Input Name Tag should not be null", nameTag);
		assertNotNull("Input Password Tag should not be null", passwdTag);
		assertNotNull("Input Submit Tag should not be null", submitTag);
		assertNull("Input dummy tag should be null", dummyTag);

		assertTypeNameSize("Input Name Tag", "text", "name", "20", nameTag);
		assertTypeNameSize("Input Password Tag", "password", "passwd", "20", passwdTag);
		assertTypeNameValue("Input Submit Tag", "submit", "submit", "Login", submitTag);

		TextareaTag textAreaTag = formTag.getTextAreaTag("Description");
		assertNotNull("Text Area Tag should have been found", textAreaTag);
		assertEquals("Text Area Tag Contents", "Contents of TextArea", textAreaTag.getValue());
		assertNull("Should have been null", formTag.getTextAreaTag("junk"));

		assertStringEquals("toHTML", EXPECTED_FORM_HTML, formTag.toHtml());
	}

	public void testScanFormWithNoEnding() throws Exception {
		createParser(
				"<TABLE>\n"
						+ "<FORM METHOD=\"post\" ACTION=\"do_login.php\" NAME=\"login_form\" onSubmit=\"return CheckData()\">\n"
						+ "<TR><TD ALIGN=\"center\">&nbsp;</TD></TR>\n"
						+ "<TR><TD ALIGN=\"center\"><FONT face=\"Arial, verdana\" size=2><b>User Name</b></font></TD></TR>\n"
						+ "<TR><TD ALIGN=\"center\"><INPUT TYPE=\"text\" NAME=\"name\" SIZE=\"20\"></TD></TR>\n"
						+ "<TR><TD ALIGN=\"center\"><FONT face=\"Arial, verdana\" size=2><b>Password</b></font></TD></TR>\n"
						+ "<TR><TD ALIGN=\"center\"><INPUT TYPE=\"password\" NAME=\"passwd\" SIZE=\"20\"></TD></TR>\n"
						+ "<TR><TD ALIGN=\"center\">&nbsp;</TD></TR>\n"
						+ "<TR><TD ALIGN=\"center\"><INPUT TYPE=\"submit\" NAME=\"submit\" VALUE=\"Login\"></TD></TR>\n"
						+ "<TR><TD ALIGN=\"center\">&nbsp;</TD></TR>\n"
						+ "<INPUT TYPE=\"hidden\" NAME=\"password\" SIZE=\"20\">\n" + "</TABLE>",
				"http://www.google.com/test/index.html");

		parser.addScanner(new FormScanner("", parser));

		parseAndAssertNodeCount(2);
	}

	/**
	 * Bug reported by Pavan Podila - forms with links are not being parsed
	 * Sample html is from google
	 */
	public void testScanFormWithLinks() throws ParserException {
		createParser("<form action=\"/search\" name=f><table cellspacing=0 cellpadding=0><tr><td width=75>&nbsp;"
				+ "</td><td align=center><input type=hidden name=hl value=en><input type=hidden name=ie "
				+ "value=\"UTF-8\"><input type=hidden name=oe value=\"UTF-8\"><input maxLength=256 size=55"
				+ " name=q value=\"\"><br><input type=submit value=\"Google Search\" name=btnG><input type="
				+ "submit value=\"I'm Feeling Lucky\" name=btnI></td><td valign=top nowrap><font size=-2>"
				+ "&nbsp;&#8226;&nbsp;<a href=/advanced_search?hl=en>Advanced&nbsp;Search</a><br>&nbsp;&#8226;"
				+ "&nbsp;<a href=/preferences?hl=en>Preferences</a><br>&nbsp;&#8226;&nbsp;<a href=/"
				+ "language_tools?hl=en>Language Tools</a></font></td></tr></table></form>");

		parser.addScanner(new FormScanner("", parser));
		parser.addScanner(new LinkScanner());
		parseAndAssertNodeCount(1);
		assertTrue("Should be a HTMLFormTag", node[0] instanceof FormTag);
		FormTag formTag = (FormTag) node[0];
		LinkTag[] linkTag = new LinkTag[10];
		int i = 0;
		for (SimpleNodeIterator e = formTag.children(); e.hasMoreNodes();) {
			Node formNode = e.nextNode();
			if (formNode instanceof LinkTag) {
				linkTag[i++] = (LinkTag) formNode;
			}
		}
		assertEquals("Link Tag Count", 3, i);
		assertEquals("First Link Tag Text", "Advanced&nbsp;Search", linkTag[0].getLinkText());
		assertEquals("Second Link Tag Text", "Preferences", linkTag[1].getLinkText());
		assertEquals("Third Link Tag Text", "Language Tools", linkTag[2].getLinkText());
	}

	/**
	 * Bug 652674 - forms with comments are not being parsed
	 */
	public void testScanFormWithComments() throws ParserException {
		createParser("<form action=\"/search\" name=f><table cellspacing=0 cellpadding=0><tr><td width=75>&nbsp;"
				+ "</td><td align=center><input type=hidden name=hl value=en><input type=hidden name=ie "
				+ "value=\"UTF-8\"><input type=hidden name=oe value=\"UTF-8\"><!-- Hello World -->"
				+ "<input maxLength=256 size=55"
				+ " name=q value=\"\"><br><input type=submit value=\"Google Search\" name=btnG><input type="
				+ "submit value=\"I'm Feeling Lucky\" name=btnI></td><td valign=top nowrap><font size=-2>"
				+ "&nbsp;&#8226;&nbsp;<a href=/advanced_search?hl=en>Advanced&nbsp;Search</a><br>&nbsp;&#8226;"
				+ "&nbsp;<a href=/preferences?hl=en>Preferences</a><br>&nbsp;&#8226;&nbsp;<a href=/"
				+ "language_tools?hl=en>Language Tools</a></font></td></tr></table></form>");

		parser.addScanner(new FormScanner("", parser));
		parseAndAssertNodeCount(1);
		assertTrue("Should be a HTMLFormTag", node[0] instanceof FormTag);
		FormTag formTag = (FormTag) node[0];
		RemarkNode[] remarkNode = new RemarkNode[10];
		int i = 0;
		for (SimpleNodeIterator e = formTag.children(); e.hasMoreNodes();) {
			Node formNode = e.nextNode();
			if (formNode instanceof RemarkNode) {
				remarkNode[i++] = (RemarkNode) formNode;
			}
		}
		assertEquals("Remark Node Count", 1, i);
		assertEquals("First Remark Node", " Hello World ", remarkNode[0].toPlainTextString());
	}

	/**
	 * Bug 652674 - forms with comments are not being parsed
	 */
	public void testScanFormWithComments2() throws ParserException {
		createParser("<FORM id=\"id\" name=\"name\" action=\"http://some.site/aPage.asp?id=97\" method=\"post\">\n"
				+ "	<!--\n" + "	Just a Comment\n" + "	-->\n" + "</FORM>");
		parser.registerScanners();
		parseAndAssertNodeCount(1);
		assertTrue("Should be a HTMLFormTag", node[0] instanceof FormTag);
		FormTag formTag = (FormTag) node[0];
		RemarkNode[] remarkNode = new RemarkNode[10];
		int i = 0;
		for (SimpleNodeIterator e = formTag.children(); e.hasMoreNodes();) {
			Node formNode = e.nextNode();
			if (formNode instanceof RemarkNode) {
				remarkNode[i++] = (RemarkNode) formNode;
			}
		}
		assertEquals("Remark Node Count", 1, i);
	}

	/**
	 * Bug 656870 - a form tag with a previously open link causes infinite loop
	 * on encounter
	 */
	public void testScanFormWithPreviousOpenLink() throws ParserException {
		createParser("<A HREF=\"http://www.oygevalt.org/\">Home</A>\n" + "<P>\n" + "And now, the good stuff:\n"
				+ "<P>\n" + "<A HREF=\"http://www.yahoo.com\">Yahoo!\n" + "<FORM ACTION=\".\" METHOD=\"GET\">\n"
				+ "<INPUT TYPE=\"TEXT\">\n" + "<BR>\n" + "<A HREF=\"http://www.helpme.com\">Help</A> "
				+ "<INPUT TYPE=\"checkbox\">\n" + "<P>\n" + "<INPUT TYPE=\"SUBMIT\">\n" + "</FORM>");
		parser.addScanner(new FormScanner("", parser));
		parser.addScanner(new LinkScanner());
		parseAndAssertNodeCount(6);
		assertTrue("Fifth Node is a link", node[4] instanceof LinkTag);
		LinkTag linkTag = (LinkTag) node[4];
		assertEquals("Link Text", "Yahoo!\r\n", linkTag.getLinkText());
		assertEquals("Link URL", "http://www.yahoo.com", linkTag.getLink());
		assertType("Sixth Node", FormTag.class, node[5]);
	}

	/**
	 * Bug 713907 reported by Dhaval Udani, erroneous parsing of form tag (even
	 * when form scanner is not registered)
	 */
	public void testFormScanningShouldNotHappen() throws Exception {
		String testHTML = "<HTML><HEAD><TITLE>Test Form Tag</TITLE></HEAD>"
				+ "<BODY><FORM name=\"form0\"><INPUT type=\"text\" name=\"text0\"></FORM>" + "</BODY></HTML>";
		createParser(testHTML);
		parser.registerScanners();
		parser.removeScanner(new FormScanner("", parser));
		Node[] nodes = parser.extractAllNodesThatAre(FormTag.class);
		assertEquals("shouldnt have found form tag", 0, nodes.length);
	}
}
