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

import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

public class TestJMESPathExtractor {
    private static final String DEFAULT_VALUE = "NONE"; // $NON-NLS-1$
    private static final String REFERENCE_NAME = "varname"; // $NON-NLS-1$
    private static final String REFERENCE_NAME_MATCH_NUMBER = "varname_matchNr"; // $NON-NLS-1$
    
    private JMESPathExtractor setupProcessor(JMeterContext context, String matchNumbers) {
        JMESPathExtractor processor = new JMESPathExtractor();
        processor.setThreadContext(context);
        processor.setRefName(REFERENCE_NAME);
        processor.setMatchNumbers(matchNumbers);
        processor.setDefaultValue(DEFAULT_VALUE);
        processor.setScopeVariable("contentvar");
        return processor;
    }
    @Test
    public void testJMESPathExtractorZeroMatch() {
        // test1
        JMeterContext context = JMeterContextService.getContext();
        JMESPathExtractor processor = setupProcessor(context, "-1");
        JMeterVariables vars = new JMeterVariables();
        context.setVariables(vars);
        processor.setJmesPathExpression("a.b.c.f");
        vars.put("contentvar", "{\"a\": {\"b\": {\"c\": {\"d\": \"value\"}}}}");
        processor.process();
        assertThat(vars.get(REFERENCE_NAME), CoreMatchers.is(DEFAULT_VALUE));
        assertThat(vars.get(REFERENCE_NAME_MATCH_NUMBER), CoreMatchers.is("0"));
    }

    @Test
    public void testJMESPathExtractorAllElementsOneMatch() {
        // test1
        JMeterContext context = JMeterContextService.getContext();
        JMESPathExtractor processor = setupProcessor(context, "-1");
        JMeterVariables vars = new JMeterVariables();
        processor.setJmesPathExpression("[*]");
        context.setVariables(vars);
        vars.put("contentvar", "[\"one\"]");
        processor.process();
        assertThat(vars.get(REFERENCE_NAME), CoreMatchers.is(CoreMatchers.nullValue()));
        assertThat(vars.get(REFERENCE_NAME+"_1"), CoreMatchers.is("\"one\""));
        assertThat(vars.get(REFERENCE_NAME_MATCH_NUMBER), CoreMatchers.is("1"));
        // test2
        processor.setJmesPathExpression("a.b.c.d");
        context.setVariables(vars);
        vars.put("contentvar", "{\"a\": {\"b\": {\"c\": {\"d\": \"value\"}}}}");
        processor.process();
        assertThat(vars.get(REFERENCE_NAME), CoreMatchers.is(CoreMatchers.nullValue()));
        assertThat(vars.get(REFERENCE_NAME+"_1"), CoreMatchers.is("\"value\""));
        assertThat(vars.get(REFERENCE_NAME_MATCH_NUMBER), CoreMatchers.is("1"));
        // test3
        vars = new JMeterVariables();
        processor.setJmesPathExpression("people[2]");
        context.setVariables(vars);
        vars.put("contentvar",
                "{\r\n" + "  \"people\": [\r\n" + "    {\"first\": \"James\", \"last\": \"d\"},\r\n"
                        + "    {\"first\": \"Jacob\", \"last\": \"e\"},\r\n"
                        + "    {\"first\": \"Jayden\", \"last\": \"f\"},\r\n" + "    {\"missing\": \"different\"}\r\n"
                        + "  ],\r\n" + "  \"foo\": {\"bar\": \"baz\"}\r\n" + "}");
        processor.process();
        assertThat(vars.get(REFERENCE_NAME+"_1"), CoreMatchers.is("{\"first\":\"Jayden\",\"last\":\"f\"}"));
    }

    @Test
    public void testJMESPathExtractorAllElementsMultipleMatches() {
        JMeterContext context = JMeterContextService.getContext();
        JMESPathExtractor processor = setupProcessor(context, "-1");
        JMeterVariables vars = new JMeterVariables();
        // test1
        processor.setJmesPathExpression("[*]");
        context.setVariables(vars);
        vars.put("contentvar", "[\"one\", \"two\"]");
        processor.process();
        assertThat(vars.get(REFERENCE_NAME), CoreMatchers.is(CoreMatchers.nullValue()));
        assertThat(vars.get(REFERENCE_NAME+"_1"), CoreMatchers.is("\"one\""));
        assertThat(vars.get(REFERENCE_NAME+"_2"), CoreMatchers.is("\"two\""));
        assertThat(vars.get(REFERENCE_NAME_MATCH_NUMBER), CoreMatchers.is("2"));
        // test2
        context.setVariables(vars);
        vars = new JMeterVariables();
        vars.put("contentvar", "[\"a\", \"b\", \"c\", \"d\", \"e\", \"f\"]");
        context.setVariables(vars);
        processor.setJmesPathExpression("[0:3]");
        processor.process();
        assertThat(vars.get(REFERENCE_NAME+"_1"), CoreMatchers.is("\"a\""));
        assertThat(vars.get(REFERENCE_NAME+"_2"), CoreMatchers.is("\"b\""));
        assertThat(vars.get(REFERENCE_NAME_MATCH_NUMBER), CoreMatchers.is("3"));
        // test3
        processor.setJmesPathExpression("people[:2].first");
        context.setVariables(vars);
        vars = new JMeterVariables();
        vars.put("contentvar",
                "{\r\n" + "  \"people\": [\r\n" + "    {\"first\": \"James\", \"last\": \"d\"},\r\n"
                        + "    {\"first\": \"Jacob\", \"last\": \"e\"},\r\n"
                        + "    {\"first\": \"Jayden\", \"last\": \"f\"},\r\n" + "    {\"missing\": \"different\"}\r\n"
                        + "  ],\r\n" + "  \"foo\": {\"bar\": \"baz\"}\r\n" + "}");
        context.setVariables(vars);
        processor.process();
        assertThat(vars.get(REFERENCE_NAME+"_1"), CoreMatchers.is("\"James\""));
        assertThat(vars.get(REFERENCE_NAME+"_2"), CoreMatchers.is("\"Jacob\""));
        assertThat(vars.get(REFERENCE_NAME_MATCH_NUMBER), CoreMatchers.is("2"));
    }

    @Test
    public void testMatchNumberMoreThanZeroInJMESPathExtractor() {
        JMeterContext context = JMeterContextService.getContext();
        JMESPathExtractor processor = setupProcessor(context, "1");
        JMeterVariables vars = new JMeterVariables();
        // test1
        processor.setJmesPathExpression("people[:3].first");
        context.setVariables(vars);
        vars = new JMeterVariables();
        vars.put("contentvar",
                "{\r\n" + "  \"people\": [\r\n" + "    {\"first\": \"James\", \"last\": \"d\", \"age\":10},\r\n"
                        + "    {\"first\": \"Jacob\", \"last\": \"e\", \"age\":20},\r\n"
                        + "    {\"first\": \"Jayden\", \"last\": \"f\", \"age\":30},\r\n" + "    {\"missing\": \"different\"}\r\n"
                        + "  ],\r\n" + "  \"foo\": {\"bar\": \"baz\"}\r\n" + "}");
        context.setVariables(vars);
        processor.process();
        assertThat(vars.get(REFERENCE_NAME), CoreMatchers.is("\"James\""));
        assertThat(vars.get(REFERENCE_NAME_MATCH_NUMBER), CoreMatchers.is("3"));
        // test2
        processor.setMatchNumbers("2");
        processor.setJmesPathExpression("people[:3].first");
        context.setVariables(vars);
        processor.process();
        assertThat(vars.get(REFERENCE_NAME), CoreMatchers.is("\"Jacob\""));
        assertThat(vars.get(REFERENCE_NAME_MATCH_NUMBER), CoreMatchers.is("3"));
        // test3
        processor.setMatchNumbers("3");
        processor.setJmesPathExpression("people[:3].first");
        context.setVariables(vars);
        processor.process();
        assertThat(vars.get(REFERENCE_NAME), CoreMatchers.is("\"Jayden\""));
        assertThat(vars.get(REFERENCE_NAME_MATCH_NUMBER), CoreMatchers.is("3"));
        
        processor.setJmesPathExpression("people[:3].age");
        context.setVariables(vars);
        processor.process();
        assertThat(vars.get(REFERENCE_NAME), CoreMatchers.is("30"));
        assertThat(vars.get(REFERENCE_NAME_MATCH_NUMBER), CoreMatchers.is("3"));
        
        // test4
        processor.setMatchNumbers("4");
        processor.setJmesPathExpression("people[:3].first");
        context.setVariables(vars);
        processor.process();
        assertThat(vars.get(REFERENCE_NAME), CoreMatchers.is(DEFAULT_VALUE));
        assertThat(vars.get(REFERENCE_NAME_MATCH_NUMBER), CoreMatchers.is("3"));
    }

    @Test
    public void testJMESPathExtractorRandomElementOneMatches() {
        JMeterContext context = JMeterContextService.getContext();
        JMESPathExtractor processor = setupProcessor(context, "0");
        JMeterVariables vars = new JMeterVariables();
        processor.setJmesPathExpression("a.b.c.d");
        processor.setScopeVariable("contentvar");
        context.setVariables(vars);
        vars.put("contentvar", "{\"a\": {\"b\": {\"c\": {\"d\": \"value\"}}}}");
        processor.process();
        assertThat(vars.get(REFERENCE_NAME), CoreMatchers.is("\"value\""));
        assertThat(vars.get(REFERENCE_NAME+"_1"), CoreMatchers.is(CoreMatchers.nullValue()));
        assertThat(vars.get(REFERENCE_NAME_MATCH_NUMBER), CoreMatchers.is("1"));
    }

    @Test
    public void testJMESPathExtractorRandomElementMultipleMatches() {
        JMeterContext context = JMeterContextService.getContext();
        JMESPathExtractor processor = setupProcessor(context, "0");
        JMeterVariables vars = new JMeterVariables();
        processor.setJmesPathExpression("[*]");
        processor.setScopeVariable("contentvar");
        context.setVariables(vars);
        vars.put("contentvar", "[\"one\", \"two\"]");
        processor.process();
        assertThat(vars.get(REFERENCE_NAME),
                CoreMatchers.is(CoreMatchers.anyOf(CoreMatchers.is("\"one\""), CoreMatchers.is("\"two\""))));
        assertThat(vars.get(REFERENCE_NAME+"_1"), CoreMatchers.is(CoreMatchers.nullValue()));
        assertThat(vars.get(REFERENCE_NAME+"_2"), CoreMatchers.is(CoreMatchers.nullValue()));
        assertThat(vars.get(REFERENCE_NAME_MATCH_NUMBER), CoreMatchers.is("2"));
    }

    @Test
    public void testEmptyExpression() {
        JMeterContext context = JMeterContextService.getContext();
        JMESPathExtractor processor = setupProcessor(context, "-1");
        JMeterVariables vars = new JMeterVariables();
        processor.setJmesPathExpression("[*]");
        vars.put("contentvar", "");
        context.setVariables(vars);
        processor.process();
        assertThat(vars.get(REFERENCE_NAME_MATCH_NUMBER), CoreMatchers.is(CoreMatchers.nullValue()));
        assertThat(vars.get(REFERENCE_NAME), CoreMatchers.is(DEFAULT_VALUE));
    }

    @Test
    public void testErrorJsonPath() {
        JMeterContext context = JMeterContextService.getContext();
        JMESPathExtractor processor = setupProcessor(context, "-1");
        JMeterVariables vars = new JMeterVariables();
        processor.setJmesPathExpression("k");
        context.setVariables(vars);
        vars.put("contentvar", "{\"a\": {\"b\": {\"c\": {\"d\": \"value\"}}}}");
        processor.process();
        assertThat(vars.get(REFERENCE_NAME), CoreMatchers.is(DEFAULT_VALUE));
        assertThat(vars.get(REFERENCE_NAME_MATCH_NUMBER), CoreMatchers.is("0"));
    }
}
