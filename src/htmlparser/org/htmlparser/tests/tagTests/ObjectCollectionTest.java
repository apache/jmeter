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

import org.htmlparser.Node;
import org.htmlparser.scanners.DivScanner;
import org.htmlparser.scanners.SpanScanner;
import org.htmlparser.scanners.TableScanner;
import org.htmlparser.tags.Div;
import org.htmlparser.tags.Span;
import org.htmlparser.tags.TableTag;
import org.htmlparser.tests.ParserTestCase;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

public class ObjectCollectionTest extends ParserTestCase
{

    public ObjectCollectionTest(String name)
    {
        super(name);
    }

    private void assertSpanContent(Node[] spans)
    {
        assertEquals("number of span objects expected", 2, spans.length);
        assertType("span", Span.class, spans[0]);
        assertType("span", Span.class, spans[1]);
        assertStringEquals(
            "span[0] text",
            "The Refactoring Challenge",
            spans[0].toPlainTextString());
        assertStringEquals(
            "span[1] text",
            "&#013;id: 6",
            spans[1].toPlainTextString());
    }

    public void testSimpleSearch() throws ParserException
    {
        createParser(
            "<SPAN>The Refactoring Challenge</SPAN>"
                + "<SPAN>&#013;id: 6</SPAN>");
        parser.registerScanners();
        parser.addScanner(new SpanScanner());
        assertSpanContent(parser.extractAllNodesThatAre(Span.class));
    }

    public void testOneLevelNesting() throws ParserException
    {
        createParser(
            "<DIV>"
                + "	<SPAN>The Refactoring Challenge</SPAN>"
                + "	<SPAN>&#013;id: 6</SPAN>"
                + "</DIV>");
        parser.registerScanners();
        parser.addScanner(new DivScanner());
        parser.addScanner(new SpanScanner());
        parseAndAssertNodeCount(1);
        Div div = (Div) node[0];
        NodeList nodeList = new NodeList();
        div.collectInto(nodeList, Span.class);
        Node[] spans = nodeList.toNodeArray();
        assertSpanContent(spans);
    }

    public void testTwoLevelNesting() throws ParserException
    {
        createParser(
            "<table>"
                + "	<DIV>"
                + "		<SPAN>The Refactoring Challenge</SPAN>"
                + "		<SPAN>&#013;id: 6</SPAN>"
                + "	</DIV>"
                + "</table>");
        parser.registerScanners();
        parser.addScanner(new DivScanner());
        parser.addScanner(new SpanScanner());
        parser.addScanner(new TableScanner(parser));
        parseAndAssertNodeCount(1);
        TableTag tableTag = (TableTag) node[0];
        NodeList nodeList = new NodeList();
        tableTag.collectInto(nodeList, Span.class);
        Node[] spans = nodeList.toNodeArray();
        assertSpanContent(spans);
    }
}
