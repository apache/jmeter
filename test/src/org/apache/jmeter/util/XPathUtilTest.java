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
import static org.junit.Assert.assertThat;

import java.io.PrintStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.transform.stream.StreamSource;

import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XPathExecutable;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmValue;

public class XPathUtilTest {
    private static final Logger log = LoggerFactory.getLogger(XPathUtil.class);
    final String lineSeparator = System.getProperty("line.separator");

    final String xmlDoc = JMeterUtils.getResourceFileAsText("XPathUtilTestXml.xml");
    
    @Test
    public void testBug63033() throws SaxonApiException {
        Processor p = new Processor(false);
        XPathCompiler c = p.newXPathCompiler();
        c.declareNamespace("age", "http://www.w3.org/2003/01/geo/wgs84_pos#");
        String xPathQuery="//Employees/Employee[1]/age:ag";;
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
}
