/*
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001-2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 * if any, must include the following acknowledgment:
 * "This product includes software developed by the
 * Apache Software Foundation (http://www.apache.org/)."
 * Alternately, this acknowledgment may appear in the software itself,
 * if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 * "Apache JMeter" must not be used to endorse or promote products
 * derived from this software without prior written permission. For
 * written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 * "Apache JMeter", nor may "Apache" appear in their name, without
 * prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
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
import org.htmlparser.scanners.JspScanner;
import org.htmlparser.tags.JspTag;
import org.htmlparser.tags.Tag;
import org.htmlparser.tests.ParserTestCase;
import org.htmlparser.util.ParserException;

public class JspTagTest extends ParserTestCase
{

    public JspTagTest(String name)
    {
        super(name);
    }

    /**
     * Check if the JSP Tag is being correctly recognized.
     * Our test html is : <BR>
     * &lt;%@ taglib uri="/WEB-INF/struts.tld" prefix="struts" %&gt;<BR>
     * &lt;jsp:useBean id="transfer" scope="session" class="com.bank.PageBean"/&gt;<BR>
     * &lt;%<BR>
     *   org.apache.struts.util.BeanUtils.populate(transfer, request);<BR>
     *    if(request.getParameter("marker") == null)<BR>
     *      // initialize a pseudo-property<BR>
     *        transfer.set("days", java.util.Arrays.asList(<BR>
     *            new String[] {"1", "2", "3", "4", "31"}));<BR>
     *    else <BR>
     *        if(transfer.validate(request))<BR>
     *            %&gt;&lt;jsp:forward page="transferConfirm.jsp"/&gt;&lt;%
     * %&gt;
     * Creation date: (6/17/2001 4:01:06 PM)
     */
    public void testJspTag() throws ParserException
    {
        createParser(
            "<%@ taglib uri=\"/WEB-INF/struts.tld\" prefix=\"struts\" %>\n"
                + "<jsp:useBean id=\"transfer\" scope=\"session\" class=\"com.bank.PageBean\"/>\n"
                + "<%\n"
                + "    org.apache.struts.util.BeanUtils.populate(transfer, request);\n"
                + "    if(request.getParameter(\"marker\") == null)\n"
                + "        // initialize a pseudo-property\n"
                + "        transfer.set(\"days\", java.util.Arrays.asList(\n"
                + "            new String[] {\"1\", \"2\", \"3\", \"4\", \"31\"}));\n"
                + "    else \n"
                + "        if(transfer.validate(request))\n"
                + "            %><jsp:forward page=\"transferConfirm.jsp\"/><%\n"
                + "%>\n");
        Parser.setLineSeparator("\r\n");
        // Register the Jsp Scanner
        parser.addScanner(new JspScanner("-j"));
        parseAndAssertNodeCount(5);
        // The first node should be an HTMLJspTag
        assertTrue("Node 1 should be an HTMLJspTag", node[0] instanceof JspTag);
        JspTag tag = (JspTag) node[0];
        assertStringEquals(
            "Contents of the tag",
            "@ taglib uri=\"/WEB-INF/struts.tld\" prefix=\"struts\" ",
            tag.getText());

        // The second node should be a normal tag
        assertTrue("Node 2 should be an Tag", node[1] instanceof Tag);
        Tag htag = (Tag) node[1];
        assertStringEquals(
            "Contents of the tag",
            "jsp:useBean id=\"transfer\" scope=\"session\" class=\"com.bank.PageBean\"",
            htag.getText());
        assertStringEquals(
            "html",
            "<JSP:USEBEAN ID=\"transfer\" SCOPE=\"session\" CLASS=\"com.bank.PageBean\"/>",
            htag.toHtml());
        // The third node should be an HTMLJspTag
        assertTrue("Node 3 should be an HTMLJspTag", node[2] instanceof JspTag);
        JspTag tag2 = (JspTag) node[2];
        String expected =
            "\r\n"
                + "    org.apache.struts.util.BeanUtils.populate(transfer, request);\r\n"
                + "    if(request.getParameter(\"marker\") == null)\r\n"
                + "        // initialize a pseudo-property\r\n"
                + "        transfer.set(\"days\", java.util.Arrays.asList(\r\n"
                + "            new String[] {\"1\", \"2\", \"3\", \"4\", \"31\"}));\r\n"
                + "    else \r\n"
                + "        if(transfer.validate(request))\r\n"
                + "            ";
        assertEquals("Contents of the tag", expected, tag2.getText());

    }

    /**
     * Check if the JSP Tag is being correctly recognized.
     * Our test html is : <BR>
     * &lt;%@ taglib uri="/WEB-INF/struts.tld" prefix="struts" %&gt;<BR>
     * &lt;jsp:useBean id="transfer" scope="session" class="com.bank.PageBean"/&gt;<BR>
     * &lt;%<BR>
     *   org.apache.struts.util.BeanUtils.populate(transfer, request);<BR>
     *    if(request.getParameter("marker") == null)<BR>
     *      // initialize a pseudo-property<BR>
     *        transfer.set("days", java.util.Arrays.asList(<BR>
     *            new String[] {"1", "2", "3", "4", "31"}));<BR>
     *    else <BR>
     *        if(transfer.validate(request))<BR>
     *            %&gt;&lt;jsp:forward page="transferConfirm.jsp"/&gt;&lt;%
     * %&gt;
     * Creation date: (6/17/2001 4:01:06 PM)
     */
    public void testToHTML() throws ParserException
    {
        createParser(
            "<%@ taglib uri=\"/WEB-INF/struts.tld\" prefix=\"struts\" %>\n"
                + "<jsp:useBean id=\"transfer\" scope=\"session\" class=\"com.bank.PageBean\"/>\n"
                + "<%\n"
                + "    org.apache.struts.util.BeanUtils.populate(transfer, request);\n"
                + "    if(request.getParameter(\"marker\") == null)\n"
                + "        // initialize a pseudo-property\n"
                + "        transfer.set(\"days\", java.util.Arrays.asList(\n"
                + "            new String[] {\"1\", \"2\", \"3\", \"4\", \"31\"}));\n"
                + "    else \n"
                + "        if(transfer.validate(request))\n"
                + "            %><jsp:forward page=\"transferConfirm.jsp\"/><%\n"
                + "%>\n");
        Parser.setLineSeparator("\r\n");
        // Register the Jsp Scanner
        parser.addScanner(new JspScanner("-j"));
        parseAndAssertNodeCount(5);
        // The first node should be an HTMLJspTag
        assertTrue("Node 1 should be an HTMLJspTag", node[0] instanceof JspTag);
        JspTag tag = (JspTag) node[0];
        assertEquals(
            "Raw String of the first JSP tag",
            "<%@ taglib uri=\"/WEB-INF/struts.tld\" prefix=\"struts\" %>",
            tag.toHtml());

        // The third node should be an HTMLJspTag
        assertTrue("Node 2 should be an HTMLJspTag", node[2] instanceof JspTag);
        JspTag tag2 = (JspTag) node[2];
        String expected =
            "<%\r\n"
                + "    org.apache.struts.util.BeanUtils.populate(transfer, request);\r\n"
                + "    if(request.getParameter(\"marker\") == null)\r\n"
                + "        // initialize a pseudo-property\r\n"
                + "        transfer.set(\"days\", java.util.Arrays.asList(\r\n"
                + "            new String[] {\"1\", \"2\", \"3\", \"4\", \"31\"}));\r\n"
                + "    else \r\n"
                + "        if(transfer.validate(request))\r\n"
                + "            %>";
        assertEquals(
            "Raw String of the second JSP tag",
            expected,
            tag2.toHtml());
        assertTrue("Node 4 should be an HTMLJspTag", node[4] instanceof JspTag);
        JspTag tag4 = (JspTag) node[4];
        expected = "<%\r\n" + "%>";
        assertEquals(
            "Raw String of the fourth JSP tag",
            expected,
            tag4.toHtml());

    }
    public void testSpecialCharacters() throws ParserException
    {
        StringBuffer sb1 = new StringBuffer();
        sb1.append("<% for (i=0;i<j;i++);%>");
        createParser(sb1.toString());

        // Register the jsp scanner 
        parser.addScanner(new JspScanner("-j"));
        parseAndAssertNodeCount(1);
        //assertTrue("Node should be a jsp tag",node[1] instanceof HTMLJspTag);
        JspTag jspTag = (JspTag) node[0];
        assertEquals(
            "jsp toHTML()",
            "<% for (i=0;i<j;i++);%>",
            jspTag.toHtml());
    }
}
