/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jmeter.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;

import org.apache.jmeter.assertions.AssertionResult;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XPathExecutable;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmValue;

public class XPathUtilTest {
    private final String lineSeparator = System.getProperty("line.separator");

    final String xmlDoc = JMeterUtils.getResourceFileAsText("XPathUtilTestXml.xml");

    @Test
    public void testBug63033() throws SaxonApiException {
        Processor p = new Processor(false);
        XPathCompiler c = p.newXPathCompiler();
        c.declareNamespace("age", "http://www.w3.org/2003/01/geo/wgs84_pos#");
        String xPathQuery="//Employees/Employee[1]/age:ag";
        XPathExecutable e = c.compile(xPathQuery);
        XPathSelector selector = e.load();
        selector.setContextItem(p.newDocumentBuilder().build(new StreamSource(new StringReader(xmlDoc))));
        XdmValue nodes = selector.evaluate();
        XdmItem item = nodes.itemAt(0);
        assertEquals("<age:ag xmlns:age=\"http://www.w3.org/2003/01/geo/wgs84_pos#\">29</age:ag>",item.toString());
    }

    @Test
    public void testputValuesForXPathInListUsingSaxon() throws SaxonApiException, FactoryConfigurationError{
        String xPathQuery="//Employees/Employee/role";
        ArrayList<String> matchStrings = new ArrayList<>();
        boolean fragment = false;
        String namespaces = "age=http://www.w3.org/2003/01/geo/wgs84_pos#";
        int matchNumber = 3;

        XPathUtil.putValuesForXPathInListUsingSaxon(xmlDoc, xPathQuery, matchStrings, fragment, matchNumber, namespaces);
        assertEquals("Manager",matchStrings.get(0));

        matchNumber = 0;
        xPathQuery="//Employees/Employee[1]/age:ag";
        fragment = true;
        matchStrings.clear();
        XPathUtil.putValuesForXPathInListUsingSaxon(xmlDoc, xPathQuery, matchStrings, fragment, matchNumber, namespaces);
        assertEquals("<age:ag xmlns:age=\"http://www.w3.org/2003/01/geo/wgs84_pos#\">29</age:ag>",matchStrings.get(0));
        assertEquals(1,matchStrings.size());

        matchNumber = -1;
        xPathQuery="//Employees/Employee/age:ag";
        matchStrings.clear();
        XPathUtil.putValuesForXPathInListUsingSaxon(xmlDoc, xPathQuery, matchStrings, fragment, matchNumber, namespaces);
        assertEquals("<age:ag xmlns:age=\"http://www.w3.org/2003/01/geo/wgs84_pos#\">29</age:ag>",matchStrings.get(0));
        assertEquals(4,matchStrings.size());

        fragment = false;
        matchStrings.clear();
        XPathUtil.putValuesForXPathInListUsingSaxon(xmlDoc, xPathQuery, matchStrings, fragment, matchNumber, namespaces);
        assertEquals("29",matchStrings.get(0));
        assertEquals(4,matchStrings.size());

        matchStrings.clear();
        xPathQuery="regtsgwsdfstgsdf";
        XPathUtil.putValuesForXPathInListUsingSaxon(xmlDoc, xPathQuery, matchStrings, fragment, matchNumber, namespaces);
        assertEquals(new ArrayList<String>(),matchStrings);
        assertEquals(0,matchStrings.size());

        matchStrings.clear();
        xPathQuery="//Employees/Employee[1]/age:ag";
        matchNumber = 555;
        XPathUtil.putValuesForXPathInListUsingSaxon(xmlDoc, xPathQuery, matchStrings, fragment, matchNumber, namespaces);
        assertEquals(new ArrayList<String>(),matchStrings);
        assertEquals(0,matchStrings.size());
    }

    static Stream<Arguments> namespaceData() {
        return Stream.of(
                Arguments.of("donald=duck", "donald", "duck", 0),
                Arguments.of("donald=duck\nmickey=mouse", "donald", "duck", 0),
                Arguments.of("donald=duck\nmickey=mouse", "mickey", "mouse", 1),
                Arguments.of("donald=duck\n\n\nmickey=mouse", "mickey", "mouse", 1),
                Arguments.of("donald=duck\n\n\nmickey=mouse", "donald", "duck", 0),
                Arguments.of("donald=duck\n     \n   \nmickey=mouse\n   \n\n", "donald", "duck", 0),
                Arguments.of("donald=duck\n     \n   \nmickey=mouse\n   \n\n", "mickey", "mouse", 1),
                Arguments.of("   \n \ndonald=duck\n     \n   \nmickey=mouse\n   \n\n", "donald", "duck", 0),
                Arguments.of("   \n \ndonald=duck\n     \n   \nmickey=mouse\n   \n\n", "mickey", "mouse", 1)
        );
    }
    @ParameterizedTest
    @MethodSource("namespaceData")
    public void testnamespacesParse(String namespaces, String key, String value, int position) {
        List<String[]> test = XPathUtil.namespacesParse(namespaces);
        assertEquals(key,test.get(position)[0]);
        assertEquals(value, test.get(position)[1]);
    }

    @Test
    public void testFormatXmlSimple() {
        assertThat(XPathUtil.formatXml("<one foo='bar'>Test</one>"),
                CoreMatchers.is("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                        + "<one foo=\"bar\">Test</one>" + lineSeparator));
    }

    @Test
    public void testFormatXmlComplex() {
        assertThat(
                XPathUtil.formatXml(
                        "<one foo='bar'><two/><three><four p=\"1\"/></three>...</one>"),
                CoreMatchers.is(String.join(lineSeparator, "<?xml version=\"1.0\" encoding=\"UTF-8\"?><one foo=\"bar\">",
                        "  <two/>",
                        "  <three>",
                        "    <four p=\"1\"/>",
                        "  </three>...</one>",
                        "")));
    }

    @Test
    public void testFormatXmlInvalid() {
        PrintStream origErr = System.err;
        try {
            // The parser will print an error, so let it go where we will not see it
            System.setErr(new PrintStream(new OutputStream() {
                @Override
                public void write(int b) throws IOException {
                    // ignore output
                }
            }));
            assertThat("No well formed xml here", CoreMatchers
                    .is(XPathUtil.formatXml("No well formed xml here")));
        } finally {
            System.setErr(origErr);
        }
    }

    @ParameterizedTest
    @CsvSource(value = {
            "/book,false,false,false",
            "/book/error,false,false,true",
            "/book/error,true,false,false",
            "/book/preface,true,false,true",
            "count(/book/page)=2,false,false,false",
            "count(/book/page)=2,true,false,true",
            "count(/book/page)=1,false,false,true",
            "count(/book/page)=1,true,false,false",
            "///book,false,true,false"
    })
    public void testMakeDocument(String xpathquery, boolean isNegated, boolean isError, boolean isFailure)
            throws ParserConfigurationException, SAXException, IOException, TidyException {
        String responseData = "<book><preface>zero</preface><page>one</page><page>two</page><empty></empty><a><b></b></a></book>";
        Document testDoc = XPathUtil.makeDocument(
                new ByteArrayInputStream(responseData.getBytes(StandardCharsets.UTF_8)),
                false, false, false, false,
                false, false, false, false, false);
        AssertionResult res = new AssertionResult("test");
        XPathUtil.computeAssertionResult(res, testDoc, xpathquery, isNegated);
        assertEquals("test isError", isError, res.isError());
        assertEquals("test isFailure", isFailure, res.isFailure());
    }

    @Test
    public void testGetNamespaces() throws XMLStreamException, FactoryConfigurationError {
        String responseData = "<age:ag xmlns:age=\"http://www.w3.org/wgs84_pos#\">\n"
                + "<hd:head xmlns:hd=\"http://www.w3.org/wgs85_pos#\"><title>test</title></hd:head></age:ag>";
        List<String[]> res = XPathUtil.getNamespaces(responseData);
        assertEquals("age", res.get(0)[0]);
        assertEquals("http://www.w3.org/wgs84_pos#", res.get(0)[1]);
        assertEquals("hd", res.get(1)[0]);
        assertEquals("http://www.w3.org/wgs85_pos#", res.get(1)[1]);
    }

    @ParameterizedTest
    @CsvSource(value = {
            "/book,false,false,false",
            "/book,true,false,true",
            "/b,false,false,true",
            "/b,true,false,false",
            "count(//page)=2,false,false,false",
            "count(//page)=2,true,false,true",
            "count(//page)=3,false,false,true",
            "count(//page)=3,true,false,false"
    })
    public void testComputeAssertionResultUsingSaxon(String xpathquery, boolean isNegated, boolean isError, boolean isFailure)
            throws SaxonApiException, FactoryConfigurationError {
        //test xpath2 assertion
        AssertionResult res = new AssertionResult("test");
        String responseData = "<book><page>one</page><page>two</page><empty></empty><a><b></b></a></book>";
        XPathUtil.computeAssertionResultUsingSaxon(res, responseData, xpathquery, "", isNegated);
        assertEquals(isError, res.isError());
        assertEquals(isFailure, res.isFailure());
    }

    @Test
    public void testPutValuesForXPathInList()
            throws ParserConfigurationException, SAXException, IOException, TidyException, TransformerException {
        String responseData = "<book><page>one</page><page>two</page><empty></empty><a><b></b></a></book>";
        Document testDoc = XPathUtil.makeDocument(
                new ByteArrayInputStream(
                        responseData.getBytes(StandardCharsets.UTF_8)),
                false, false, false, false,
                false, false, false, false, false);
        String xpathquery = "/book/page";
        List<String> matchs=new ArrayList<>();
        XPathUtil.putValuesForXPathInList(testDoc, xpathquery, matchs, true);
        assertEquals("<page>one</page>", matchs.get(0));
        assertEquals("<page>two</page>", matchs.get(1));
        matchs=new ArrayList<>();
        XPathUtil.putValuesForXPathInList(testDoc, xpathquery, matchs, false);
        assertEquals(2, matchs.size());
        assertEquals("one", matchs.get(0));
        assertEquals("two", matchs.get(1));
        matchs=new ArrayList<>();
        XPathUtil.putValuesForXPathInList(testDoc, "/book/a", matchs, false);
        assertEquals(1, matchs.size());
        assertNull(matchs.get(0));
    }

    @Test
    public void testSelectNodeList() throws ParserConfigurationException, SAXException, IOException, TidyException, TransformerException {
        String responseData = "<book><page>one</page><page>two</page><empty></empty><a><b></b></a></book>";
        Document testDoc = XPathUtil.makeDocument(
                new ByteArrayInputStream(responseData.getBytes(StandardCharsets.UTF_8)), false, false, false, false,
                false, false, false, false, false);
        String xpathquery = "/book/page";
        NodeList nodeList = XPathUtil.selectNodeList(testDoc, xpathquery);
        assertEquals(2, nodeList.getLength());
        Element e0 = (Element) nodeList.item(0);
        Element e1 = (Element) nodeList.item(1);
        assertEquals("one", e0.getTextContent());
        assertEquals("two", e1.getTextContent());
    }

    @Test()
    public void testSelectNodeListWithInvalidXPath() throws Exception {
        String responseData = "<book><page>one</page><page>two</page><empty></empty><a><b></b></a></book>";
        Document testDoc = XPathUtil.makeDocument(
                new ByteArrayInputStream(responseData.getBytes(StandardCharsets.UTF_8)), false, false, false, false,
                false, false, false, false, false);
        String xpathquery = "<";
        assertThrows(TransformerException.class, () -> XPathUtil.selectNodeList(testDoc, xpathquery));
    }
}
