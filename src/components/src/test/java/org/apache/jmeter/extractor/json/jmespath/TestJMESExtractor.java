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
package org.apache.jmeter.extractor.json.jmespath;

import static org.junit.Assert.assertThat;

import org.apache.jmeter.extractor.json.jsonpath.JMESExtractor;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

public class TestJMESExtractor {
    private static final String DEFAULT_VALUE = "NONE"; // $NON-NLS-1$
    private static final String REFERENCE_NAME = "varname"; // $NON-NLS-1$
    private static final String REFERENCE_NAME_MATCH_NUMBER = "varname_matchNr"; // $NON-NLS-1$
    private JMESExtractor setupProcessor(JMeterContext context, String matchNumbers) {
        JMESExtractor processor = new JMESExtractor();
        processor.setThreadContext(context);
        processor.setRefName(REFERENCE_NAME);
        processor.setMatchNumbers(matchNumbers);
        processor.setDefaultValue(DEFAULT_VALUE);
        processor.setScopeVariable("contentvar");
        return processor;
    }
    @Test
    public void testJMESExtractorZeroMatch() {
        // test1
        JMeterContext context = JMeterContextService.getContext();
        JMESExtractor processor = setupProcessor(context, "-1");
        JMeterVariables vars = new JMeterVariables();
        context.setVariables(vars);
        processor.setJsonPathExpression("a.b.c.f");
        vars.put("contentvar", "{\"a\": {\"b\": {\"c\": {\"d\": \"value\"}}}}");
        processor.process();
        assertThat(vars.get(REFERENCE_NAME), CoreMatchers.is(DEFAULT_VALUE));
        assertThat(vars.get(REFERENCE_NAME_MATCH_NUMBER), CoreMatchers.is("0"));
    }

    @Test
    public void testJMESExtractorAllElementsOneMatch() {
        // test1
        JMeterContext context = JMeterContextService.getContext();
        JMESExtractor processor = setupProcessor(context, "-1");
        JMeterVariables vars = new JMeterVariables();
        processor.setJsonPathExpression("[*]");
        context.setVariables(vars);
        vars.put("contentvar", "[\"one\"]");
        processor.process();
        assertThat(vars.get(REFERENCE_NAME), CoreMatchers.is(CoreMatchers.nullValue()));
        assertThat(vars.get(REFERENCE_NAME+"_1"), CoreMatchers.is("one"));
        assertThat(vars.get(REFERENCE_NAME_MATCH_NUMBER), CoreMatchers.is("1"));
        // test2
        processor.setJsonPathExpression("a.b.c.d");
        context.setVariables(vars);
        vars.put("contentvar", "{\"a\": {\"b\": {\"c\": {\"d\": \"value\"}}}}");
        processor.process();
        assertThat(vars.get(REFERENCE_NAME), CoreMatchers.is(CoreMatchers.nullValue()));
        assertThat(vars.get(REFERENCE_NAME+"_1"), CoreMatchers.is("value"));
        assertThat(vars.get(REFERENCE_NAME_MATCH_NUMBER), CoreMatchers.is("1"));
        // test3
        vars = new JMeterVariables();
        processor.setJsonPathExpression("people[2]");
        context.setVariables(vars);
        vars.put("contentvar",
                "{\r\n" + "  \"people\": [\r\n" + "    {\"first\": \"James\", \"last\": \"d\"},\r\n"
                        + "    {\"first\": \"Jacob\", \"last\": \"e\"},\r\n"
                        + "    {\"first\": \"Jayden\", \"last\": \"f\"},\r\n" + "    {\"missing\": \"different\"}\r\n"
                        + "  ],\r\n" + "  \"foo\": {\"bar\": \"baz\"}\r\n" + "}");
        processor.process();
        assertThat(vars.get(REFERENCE_NAME+"_1"), CoreMatchers.is("{ \"first\" : \"Jayden\" , \"last\" : \"f\"}"));
    }

    @Test
    public void testJMESExtractorAllElementsMultipleMatches() {
        JMeterContext context = JMeterContextService.getContext();
        JMESExtractor processor = setupProcessor(context, "-1");
        JMeterVariables vars = new JMeterVariables();
        // test1
        processor.setJsonPathExpression("[*]");
        context.setVariables(vars);
        vars.put("contentvar", "[\"one\", \"two\"]");
        processor.process();
        assertThat(vars.get(REFERENCE_NAME), CoreMatchers.is(CoreMatchers.nullValue()));
        assertThat(vars.get(REFERENCE_NAME+"_1"), CoreMatchers.is("one"));
        assertThat(vars.get(REFERENCE_NAME+"_2"), CoreMatchers.is("two"));
        assertThat(vars.get(REFERENCE_NAME_MATCH_NUMBER), CoreMatchers.is("2"));
        // test2
        context.setVariables(vars);
        vars = new JMeterVariables();
        vars.put("contentvar", "[\"a\", \"b\", \"c\", \"d\", \"e\", \"f\"]");
        context.setVariables(vars);
        processor.setJsonPathExpression("[0:3]");
        processor.process();
        assertThat(vars.get(REFERENCE_NAME+"_1"), CoreMatchers.is("a"));
        assertThat(vars.get(REFERENCE_NAME+"_2"), CoreMatchers.is("b"));
        assertThat(vars.get(REFERENCE_NAME_MATCH_NUMBER), CoreMatchers.is("3"));
        // test3
        processor.setJsonPathExpression("people[:2].first");
        context.setVariables(vars);
        vars = new JMeterVariables();
        vars.put("contentvar",
                "{\r\n" + "  \"people\": [\r\n" + "    {\"first\": \"James\", \"last\": \"d\"},\r\n"
                        + "    {\"first\": \"Jacob\", \"last\": \"e\"},\r\n"
                        + "    {\"first\": \"Jayden\", \"last\": \"f\"},\r\n" + "    {\"missing\": \"different\"}\r\n"
                        + "  ],\r\n" + "  \"foo\": {\"bar\": \"baz\"}\r\n" + "}");
        context.setVariables(vars);
        processor.process();
        assertThat(vars.get(REFERENCE_NAME+"_1"), CoreMatchers.is("James"));
        assertThat(vars.get(REFERENCE_NAME+"_2"), CoreMatchers.is("Jacob"));
        assertThat(vars.get(REFERENCE_NAME_MATCH_NUMBER), CoreMatchers.is("2"));
    }

    @Test
    public void testMatchNumberMoreThanZeroInJMESExtractor() {
        JMeterContext context = JMeterContextService.getContext();
        JMESExtractor processor = setupProcessor(context, "1");
        JMeterVariables vars = new JMeterVariables();
        // test1
        processor.setJsonPathExpression("people[:3].first");
        context.setVariables(vars);
        vars = new JMeterVariables();
        vars.put("contentvar",
                "{\r\n" + "  \"people\": [\r\n" + "    {\"first\": \"James\", \"last\": \"d\"},\r\n"
                        + "    {\"first\": \"Jacob\", \"last\": \"e\"},\r\n"
                        + "    {\"first\": \"Jayden\", \"last\": \"f\"},\r\n" + "    {\"missing\": \"different\"}\r\n"
                        + "  ],\r\n" + "  \"foo\": {\"bar\": \"baz\"}\r\n" + "}");
        context.setVariables(vars);
        processor.process();
        assertThat(vars.get(REFERENCE_NAME), CoreMatchers.is("James"));
        assertThat(vars.get(REFERENCE_NAME_MATCH_NUMBER), CoreMatchers.is("3"));
        // test2
        processor.setMatchNumbers("2");
        processor.setJsonPathExpression("people[:3].first");
        context.setVariables(vars);
        processor.process();
        assertThat(vars.get(REFERENCE_NAME), CoreMatchers.is("Jacob"));
        assertThat(vars.get(REFERENCE_NAME_MATCH_NUMBER), CoreMatchers.is("3"));
        // test3
        processor.setMatchNumbers("3");
        processor.setJsonPathExpression("people[:3].first");
        context.setVariables(vars);
        context.setVariables(vars);
        processor.process();
        assertThat(vars.get(REFERENCE_NAME), CoreMatchers.is("Jayden"));
        assertThat(vars.get(REFERENCE_NAME_MATCH_NUMBER), CoreMatchers.is("3"));
        // test4
        processor.setMatchNumbers("4");
        processor.setJsonPathExpression("people[:3].first");
        context.setVariables(vars);
        context.setVariables(vars);
        processor.process();
        assertThat(vars.get(REFERENCE_NAME), CoreMatchers.is(DEFAULT_VALUE));
        assertThat(vars.get(REFERENCE_NAME_MATCH_NUMBER), CoreMatchers.is("3"));
    }

    @Test
    public void testJMESExtractorRandomElementOneMatches() {
        JMeterContext context = JMeterContextService.getContext();
        JMESExtractor processor = setupProcessor(context, "0");
        JMeterVariables vars = new JMeterVariables();
        processor.setJsonPathExpression("a.b.c.d");
        processor.setScopeVariable("contentvar");
        context.setVariables(vars);
        vars.put("contentvar", "{\"a\": {\"b\": {\"c\": {\"d\": \"value\"}}}}");
        processor.process();
        assertThat(vars.get(REFERENCE_NAME), CoreMatchers.is("value"));
        assertThat(vars.get(REFERENCE_NAME+"_1"), CoreMatchers.is(CoreMatchers.nullValue()));
        assertThat(vars.get(REFERENCE_NAME_MATCH_NUMBER), CoreMatchers.is("1"));
    }

    @Test
    public void testJMESExtractorRandomElementMultipleMatches() {
        JMeterContext context = JMeterContextService.getContext();
        JMESExtractor processor = setupProcessor(context, "0");
        JMeterVariables vars = new JMeterVariables();
        processor.setJsonPathExpression("[*]");
        processor.setScopeVariable("contentvar");
        context.setVariables(vars);
        vars.put("contentvar", "[\"one\", \"two\"]");
        processor.process();
        assertThat(vars.get(REFERENCE_NAME),
                CoreMatchers.is(CoreMatchers.anyOf(CoreMatchers.is("one"), CoreMatchers.is("two"))));
        assertThat(vars.get(REFERENCE_NAME+"_1"), CoreMatchers.is(CoreMatchers.nullValue()));
        assertThat(vars.get(REFERENCE_NAME+"_2"), CoreMatchers.is(CoreMatchers.nullValue()));
        assertThat(vars.get(REFERENCE_NAME_MATCH_NUMBER), CoreMatchers.is("2"));
    }

    @Test
    public void testEmptyExpression() {
        JMeterContext context = JMeterContextService.getContext();
        JMESExtractor processor = setupProcessor(context, "-1");
        JMeterVariables vars = new JMeterVariables();
        processor.setJsonPathExpression("[*]");
        vars.put("contentvar", "");
        context.setVariables(vars);
        processor.process();
        assertThat(vars.get(REFERENCE_NAME_MATCH_NUMBER), CoreMatchers.is(CoreMatchers.nullValue()));
        assertThat(vars.get(REFERENCE_NAME), CoreMatchers.is(DEFAULT_VALUE));
    }

    @Test
    public void testErrorJsonPath() {
        JMeterContext context = JMeterContextService.getContext();
        JMESExtractor processor = setupProcessor(context, "-1");
        JMeterVariables vars = new JMeterVariables();
        processor.setJsonPathExpression("k");
        context.setVariables(vars);
        vars.put("contentvar", "{\"a\": {\"b\": {\"c\": {\"d\": \"value\"}}}}");
        processor.process();
        assertThat(vars.get(REFERENCE_NAME), CoreMatchers.is(DEFAULT_VALUE));
        assertThat(vars.get(REFERENCE_NAME_MATCH_NUMBER), CoreMatchers.is("0"));
    }
}
