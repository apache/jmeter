/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 */

package org.apache.jmeter.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;

import org.apache.jmeter.assertions.AssertionResult;
import org.apache.jmeter.assertions.gui.XPath2Panel;
import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
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
        ArrayList<String> matchStrings = new ArrayList<String>();
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

    @Test
    public void testnamespacesParse() {
        String namespaces = "donald=duck";
        List<String[]> test = XPathUtil.namespacesParse(namespaces);
        assertEquals("donald",test.get(0)[0]);
        assertEquals("duck",test.get(0)[1]);

        namespaces = "donald=duck\nmickey=mouse";
        test = XPathUtil.namespacesParse(namespaces);
        assertEquals("donald",test.get(0)[0]);
        assertEquals("duck",test.get(0)[1]);
        assertEquals("mickey",test.get(1)[0]);
        assertEquals("mouse",test.get(1)[1]);

        namespaces = "donald=duck\n\n\nmickey=mouse";
        test = XPathUtil.namespacesParse(namespaces);
        assertEquals("mickey",test.get(1)[0]);
        assertEquals("mouse",test.get(1)[1]);

        namespaces = "geo=patate\n       \n   \n\nmickey=mouse\n\n      \n";
        test = XPathUtil.namespacesParse(namespaces);
        assertEquals("mickey",test.get(1)[0]);
        assertEquals("mouse",test.get(1)[1]);
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

    @Test()
    public void testFormatXmlInvalid() {
        PrintStream origErr = System.err;
        // The parser will print an error, so let it go somewhere, where we will
        // not see it
        System.setErr(null);
        assertThat("No well formed xml here", CoreMatchers
                .is(XPathUtil.formatXml("No well formed xml here")));
        System.setErr(origErr);
    }

    @Test()
    public void testValidateXPath2() throws ParserConfigurationException {
        Document testDoc = XPathUtil.makeDocumentBuilder(false, false, false, false).newDocument();
        Element el = testDoc.createElement("root"); //$NON-NLS-1$
        testDoc.appendChild(el);
        String namespaces = "a=http://www.w3.org/2003/01/geo/wgs84_pos# b=http://www.w3.org/2003/01/geo/wgs85_pos#";
        String xPathQuery = "//Employees/b:Employee[1]/a:ag";
        assertTrue("When the user give namspaces, the result of validation should be true",
                XPath2Panel.validXPath(xPathQuery, false, namespaces));
        namespaces = "a=http://www.w3.org/2003/01/geo/wgs84_pos#";
        assertFalse("When the user doesn't give namspaces, the result of validation should be false",
                XPath2Panel.validXPath(xPathQuery, false, namespaces));
    }

    @Test()
    public void testMakeDocument() throws ParserConfigurationException, SAXException, IOException, TidyException {
        String responseData = "<book><page>one</page><page>two</page><empty></empty><a><b></b></a></book>";
        Document testDoc = XPathUtil.makeDocument(
                new ByteArrayInputStream(responseData.getBytes(StandardCharsets.UTF_8)), false, false, false, false,
                false, false, false, false, false);
        AssertionResult res = new AssertionResult("test");
        String xpathquery = "/book";
        XPathUtil.computeAssertionResult(res, testDoc, xpathquery, false);
        assertFalse("Should not be an error", res.isError());
        assertFalse("Should not be a failure", res.isFailure());
        xpathquery = "/book/error";
        XPathUtil.computeAssertionResult(res, testDoc, xpathquery, false);
        assertFalse("Should not be an error", res.isError());
        assertTrue("Should be a failure", res.isFailure());
        xpathquery = "count(/book/page)=2";
        XPathUtil.computeAssertionResult(res, testDoc, xpathquery, false);
        assertFalse("Should not be an error", res.isError());
        assertFalse("Should not be a failure", res.isFailure());
        xpathquery = "count(/book/page)=1";
        XPathUtil.computeAssertionResult(res, testDoc, xpathquery, false);
        assertFalse("Should not be an error", res.isError());
        assertTrue("Should be a failure", res.isFailure());
        xpathquery = "///book";
        XPathUtil.computeAssertionResult(res, testDoc, xpathquery, false);
        assertTrue("Should be an error", res.isError());
    }

    @Test()
    public void testMakeDocumentIsnegated()
            throws ParserConfigurationException, SAXException, IOException, TidyException {
        String responseData = "<book><preface>zero</preface><page>one</page><page>two</page><empty></empty><a><b></b></a></book>";
        Document testDoc = XPathUtil.makeDocument(
                new ByteArrayInputStream(responseData.getBytes(StandardCharsets.UTF_8)), false, false, false, false,
                false, false, false, false, false);
        AssertionResult res = new AssertionResult("test");
        String xpathquery = "/book/error";
        XPathUtil.computeAssertionResult(res, testDoc, xpathquery, true);
        assertFalse("Should not be an error", res.isError());
        assertFalse("Should not be a failure", res.isFailure());
        xpathquery = "/book/preface";
        XPathUtil.computeAssertionResult(res, testDoc, xpathquery, true);
        assertFalse("Should not be an error", res.isError());
        assertTrue("Should be a failure", res.isFailure());
        XPathUtil.computeAssertionResult(res, testDoc, xpathquery, true);
        assertFalse("Should not be an error", res.isError());
        assertTrue("Should be a failure", res.isFailure());
        xpathquery = "count(/book/page)=1";
        XPathUtil.computeAssertionResult(res, testDoc, xpathquery, true);
        assertFalse("Should not be an error", res.isError());
        assertFalse("Should not be a failure", res.isFailure());
    }

    @Test()
    public void testGetNamespaces() throws XMLStreamException, FactoryConfigurationError {
        String responseData = "<age:ag xmlns:age=\"http://www.w3.org/wgs84_pos#\">\n"
                + "<hd:head xmlns:hd=\"http://www.w3.org/wgs85_pos#\"><title>test</title></hd:head></age:ag>";
        List<String[]> res = XPathUtil.getNamespaces(responseData);
        assertEquals("age", res.get(0)[0]);
        assertEquals("http://www.w3.org/wgs84_pos#", res.get(0)[1]);
        assertEquals("hd", res.get(1)[0]);
        assertEquals("http://www.w3.org/wgs85_pos#", res.get(1)[1]);
    }

    @Test()
    public void testComputeAssertionResultUsingSaxon() throws SaxonApiException, FactoryConfigurationError {
        //test xpath2 assertion
        AssertionResult res = new AssertionResult("test");
        String responseData = "<book><page>one</page><page>two</page><empty></empty><a><b></b></a></book>";
        String xpathquery = "/book";
        XPathUtil.computeAssertionResultUsingSaxon(res, responseData, xpathquery, "", false);
        assertFalse("Should not be an error", res.isError());
        assertFalse("Should not be a failure", res.isFailure());
        //test xpath2 assertion
        xpathquery = "/b";
        XPathUtil.computeAssertionResultUsingSaxon(res, responseData, xpathquery, "", false);
        assertFalse("Should not be an error", res.isError());
        assertTrue("Should be a failure", res.isFailure());
        //test xpath2 assertion boolean
        xpathquery = "count(//page)=2";
        XPathUtil.computeAssertionResultUsingSaxon(res, responseData, xpathquery, "", false);
        assertFalse("Should not be an error", res.isError());
        assertFalse("Should not be a failure", res.isFailure());
        //test xpath2 assertion boolean
        xpathquery = "count(//page)=3";
        XPathUtil.computeAssertionResultUsingSaxon(res, responseData, xpathquery, "", false);
        assertFalse("Should not be an error", res.isError());
        assertTrue("Should be a failure", res.isFailure());
    }
    @Test()
    public void testPutValuesForXPathInList() throws ParserConfigurationException, SAXException, IOException, TidyException, TransformerException {
        String responseData = "<book><page>one</page><page>two</page><empty></empty><a><b></b></a></book>";
        Document testDoc = XPathUtil.makeDocument(
                new ByteArrayInputStream(responseData.getBytes(StandardCharsets.UTF_8)), false, false, false, false,
                false, false, false, false, false);
        String xpathquery = "/book/page";
        List<String> matchs=new ArrayList<>();
        XPathUtil.putValuesForXPathInList(testDoc, xpathquery, matchs, true);
        assertEquals("<page>one</page>", matchs.get(0));
        assertEquals("<page>two</page>", matchs.get(1));
        matchs=new ArrayList<>();
        XPathUtil.putValuesForXPathInList(testDoc, xpathquery, matchs, false);
        assertEquals("one", matchs.get(0));
        assertEquals("two", matchs.get(1));
    }
}
