/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

package org.apache.jmeter.extractor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.UnsupportedEncodingException;

import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.junit.Before;
import org.junit.Test;

public class TestXPath2Extractor {
    private static final String VAL_NAME = "value";
    private static final String VAL_NAME_NR = "value_matchNr";

    private XPath2Extractor extractor;
    private SampleResult result;
    private String data;
    private JMeterVariables vars;
    private JMeterContext jmctx;

    @Before
    public void setUp() throws UnsupportedEncodingException {
        jmctx = JMeterContextService.getContext();
        extractor = new XPath2Extractor();
        extractor.setThreadContext(jmctx);// This would be done by the run
                                          // command
        extractor.setRefName(VAL_NAME);
        extractor.setDefaultValue("Default");
        result = new SampleResult();
        data = "<book><preface title='Intro'>zero</preface><page>one</page><page>two</page><empty></empty><a><b></b></a></book>";
        result.setResponseData(data.getBytes("UTF-8"));
        vars = new JMeterVariables();
        jmctx.setVariables(vars);
        jmctx.setPreviousResult(result);
    }

    @Test
    public void testAttributeExtraction() throws Exception {
        extractor.setXPathQuery("/book/preface/@title");
        extractor.process();
        assertEquals("Intro", vars.get(VAL_NAME));
        assertEquals("1", vars.get(VAL_NAME_NR));
        assertEquals("Intro", vars.get(VAL_NAME + "_1"));
        assertNull(vars.get(VAL_NAME + "_2"));

        extractor.setXPathQuery("/book/preface[@title]");
        extractor.process();
        assertEquals("zero", vars.get(VAL_NAME));
        assertEquals("1", vars.get(VAL_NAME_NR));
        assertEquals("zero", vars.get(VAL_NAME + "_1"));
        assertNull(vars.get(VAL_NAME + "_2"));

        extractor.setXPathQuery("/book/preface[@title='Intro']");
        extractor.process();
        assertEquals("zero", vars.get(VAL_NAME));
        assertEquals("1", vars.get(VAL_NAME_NR));
        assertEquals("zero", vars.get(VAL_NAME + "_1"));
        assertNull(vars.get(VAL_NAME + "_2"));

        extractor.setXPathQuery("/book/preface[@title='xyz']");
        extractor.process();
        assertEquals("Default", vars.get(VAL_NAME));
        assertEquals("0", vars.get(VAL_NAME_NR));
        assertNull(vars.get(VAL_NAME + "_1"));
    }

    @Test
    public void testVariableExtraction() throws Exception {
        extractor.setXPathQuery("/book/preface");
        extractor.process();
        assertEquals("zero", vars.get(VAL_NAME));
        assertEquals("1", vars.get(VAL_NAME_NR));
        assertEquals("zero", vars.get(VAL_NAME + "_1"));
        assertNull(vars.get(VAL_NAME + "_2"));

        // Test match 1
        extractor.setXPathQuery("/book/page");
        extractor.setMatchNumber(1);
        extractor.process();
        assertEquals("one", vars.get(VAL_NAME));
        assertEquals("1", vars.get(VAL_NAME_NR));
        assertEquals("one", vars.get(VAL_NAME + "_1"));
        assertNull(vars.get(VAL_NAME + "_2"));
        assertNull(vars.get(VAL_NAME + "_3"));

        // Test match 1 in String
        extractor.setXPathQuery("/book/page");
        extractor.setMatchNumber("1");
        extractor.process();
        assertEquals("one", vars.get(VAL_NAME));
        assertEquals("1", vars.get(VAL_NAME_NR));
        assertEquals("one", vars.get(VAL_NAME + "_1"));
        assertNull(vars.get(VAL_NAME + "_2"));
        assertNull(vars.get(VAL_NAME + "_3"));

        // Test match 2
        extractor.setXPathQuery("/book/page");
        extractor.setMatchNumber(2);
        extractor.process();
        assertEquals("two", vars.get(VAL_NAME));
        assertEquals("1", vars.get(VAL_NAME_NR));
        assertEquals("two", vars.get(VAL_NAME + "_1"));
        assertNull(vars.get(VAL_NAME + "_2"));
        assertNull(vars.get(VAL_NAME + "_3"));

        // Test more than one match
        extractor.setMatchNumber(-1);
        extractor.setXPathQuery("/book/page");
        extractor.process();
        assertEquals("2", vars.get(VAL_NAME_NR));
        assertEquals("one", vars.get(VAL_NAME + "_1"));
        assertEquals("one", vars.get(VAL_NAME));
        assertEquals("two", vars.get(VAL_NAME + "_2"));
        assertNull(vars.get(VAL_NAME + "_3"));

        // Put back default value
        extractor.setMatchNumber(-1);

        extractor.setXPathQuery("/book/page[2]");
        extractor.process();
        assertEquals("two", vars.get(VAL_NAME));
        assertEquals("1", vars.get(VAL_NAME_NR));
        assertEquals("two", vars.get(VAL_NAME + "_1"));
        assertNull(vars.get(VAL_NAME + "_2"));
        assertNull(vars.get(VAL_NAME + "_3"));

        extractor.setXPathQuery("/book/index");
        extractor.process();
        assertEquals("Default", vars.get(VAL_NAME));
        assertEquals("0", vars.get(VAL_NAME_NR));
        assertNull(vars.get(VAL_NAME + "_1"));

        extractor.setMatchNumber(-1);
        // Test fragment
        extractor.setXPathQuery("/book/page[2]");
        extractor.setFragment(true);
        extractor.process();
        assertEquals("<page>two</page>", vars.get(VAL_NAME));
        // Now get its text
        extractor.setXPathQuery("/book/page[2]/text()");
        extractor.process();
        assertEquals("two", vars.get(VAL_NAME));

    }

    //
    @Test
    public void testScope() {
        extractor.setXPathQuery("/book/preface");
        extractor.process();
        assertEquals("zero", vars.get(VAL_NAME));
        assertEquals("1", vars.get(VAL_NAME_NR));
        assertEquals("zero", vars.get(VAL_NAME + "_1"));
        assertNull(vars.get(VAL_NAME + "_2"));

        extractor.setScopeChildren(); // There aren't any
        extractor.process();
        assertEquals("Default", vars.get(VAL_NAME));
        assertEquals("0", vars.get(VAL_NAME_NR));
        assertNull(vars.get(VAL_NAME + "_1"));

        extractor.setScopeAll(); // same as Parent
        extractor.process();
        assertEquals("zero", vars.get(VAL_NAME));
        assertEquals("1", vars.get(VAL_NAME_NR));
        assertEquals("zero", vars.get(VAL_NAME + "_1"));
        assertNull(vars.get(VAL_NAME + "_2"));

        // Try to get data from subresult
        result.sampleStart(); // Needed for addSubResult()
        result.sampleEnd();
        SampleResult subResult = new SampleResult();
        subResult.sampleStart();
        subResult.setResponseData(result.getResponseData());
        subResult.sampleEnd();
        result.addSubResult(subResult);

        // Get data from both
        extractor.setScopeAll();
        extractor.process();
        assertEquals("zero", vars.get(VAL_NAME));
        assertEquals("2", vars.get(VAL_NAME_NR));
        assertEquals("zero", vars.get(VAL_NAME + "_1"));
        assertEquals("zero", vars.get(VAL_NAME + "_2"));
        assertNull(vars.get(VAL_NAME + "_3"));

        // get data from child
        extractor.setScopeChildren();
        extractor.process();
        assertEquals("zero", vars.get(VAL_NAME));
        assertEquals("1", vars.get(VAL_NAME_NR));
        assertEquals("zero", vars.get(VAL_NAME + "_1"));
        assertNull(vars.get(VAL_NAME + "_2"));

        // get data from child
        extractor.setScopeVariable("result");
        result = new SampleResult();
        vars.put("result", data);
        extractor.process();
        assertEquals("zero", vars.get(VAL_NAME));
        assertEquals("1", vars.get(VAL_NAME_NR));
        assertEquals("zero", vars.get(VAL_NAME + "_1"));
        assertNull(vars.get(VAL_NAME + "_2"));

        // get data from child
        extractor.setScopeVariable("result");
        result = new SampleResult();
        vars.remove("result");
        extractor.process();
        assertEquals("Default", vars.get(VAL_NAME));
        assertEquals("0", vars.get(VAL_NAME_NR));
    }

    @Test
    public void testWithNamespace() throws Exception {
        result.setResponseData(
                "<age:ag xmlns:age=\"http://www.w3.org/wgs84_pos#\"><head><title>test</title></head></age:ag>", null);
        String namespaces = "age=http://www.w3.org/wgs84_pos#";
        String xPathQuery = "/age:ag/head/title";
        extractor.setXPathQuery(xPathQuery);
        extractor.setNamespaces(namespaces);
        extractor.process();
        assertEquals("test", vars.get(VAL_NAME));
        assertEquals("1", vars.get(VAL_NAME_NR));
        assertEquals("test", vars.get(VAL_NAME + "_1"));
        assertNull(vars.get(VAL_NAME + "_2"));
    }

    @Test
    public void testWithNamespaces() throws Exception {
        result.setResponseData(
                "<age:ag xmlns:age=\"http://www.w3.org/wgs84_pos#\">"
                        + "<hd:head xmlns:hd=\"http://www.w3.org/wgs85_pos#\"><title>test</title></hd:head></age:ag>",
                null);
        String namespaces = "age=http://www.w3.org/wgs84_pos#" + "\n" + "hd=http://www.w3.org/wgs85_pos#";
        String xPathQuery = "/age:ag/hd:head/title";
        extractor.setXPathQuery(xPathQuery);
        extractor.setNamespaces(namespaces);
        extractor.process();
        assertEquals("test", vars.get(VAL_NAME));
        assertEquals("1", vars.get(VAL_NAME_NR));
        assertEquals("test", vars.get(VAL_NAME + "_1"));
        assertNull(vars.get(VAL_NAME + "_2"));
    }

    @Test
    public void testWithoutNamespace() throws Exception {
        result.setResponseData(
                "<age:ag xmlns:age=\"http://www.w3.org/wgs84_pos#\"><head><title>test</title></head></age:ag>", null);
        String xPathQuery = "/age:ag/head/title";
        extractor.setXPathQuery(xPathQuery);
        extractor.process();
        assertEquals("Default", vars.get(VAL_NAME));
        assertEquals("0", vars.get(VAL_NAME_NR));
    }

    @Test
    public void testPreviousResultIsEmpty() throws Exception {
        JMeterContext jmc = JMeterContextService.getContext();
        extractor = new XPath2Extractor();
        extractor.setThreadContext(jmctx);// This would be done by the run
                                          // command
        extractor.setRefName(VAL_NAME);
        extractor.setDefaultValue("Default");
        jmc.setPreviousResult(null);
        extractor.setXPathQuery("/book/preface");
        extractor.process();
        assertEquals(null, vars.get(VAL_NAME));
    }
}
