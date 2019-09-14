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

import java.util.Arrays;
import java.util.Collection;

import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Enclosed.class)
public class TestJMESPathExtractor {
    private static final String DEFAULT_VALUE = "NONE"; // $NON-NLS-1$
    private static final String REFERENCE_NAME = "varname"; // $NON-NLS-1$
    private static final String REFERENCE_NAME_MATCH_NUMBER = "varname_matchNr"; // $NON-NLS-1$

    private static JMESPathExtractor setupProcessor(JMeterContext context, String matchNumbers) {
        JMESPathExtractor processor = new JMESPathExtractor();
        processor.setThreadContext(context);
        processor.setRefName(REFERENCE_NAME);
        processor.setMatchNumber(matchNumbers);
        processor.setDefaultValue(DEFAULT_VALUE);
        processor.setScopeVariable("contentvar");
        return processor;
    }

    @RunWith(Parameterized.class)
    public static class OneMatchOnAllExtractedValues {

        @Parameters
        public static Collection<String[]> data() {
            return Arrays.asList(new String[][] {
                {"[\"one\"]", "[*]", "\"one\"", "1"},
                {"{\"a\": {\"b\": {\"c\": {\"d\": \"value\"}}}}", "a.b.c.d", "\"value\"", "1"},
                {"{\r\n" + "  \"people\": [\r\n" + "    {\"first\": \"James\", \"last\": \"d\"},\r\n"
                        + "    {\"first\": \"Jacob\", \"last\": \"e\"},\r\n"
                        + "    {\"first\": \"Jayden\", \"last\": \"f\"},\r\n" + "    {\"missing\": \"different\"}\r\n"
                        + "  ],\r\n" + "  \"foo\": {\"bar\": \"baz\"}\r\n" + "}", "people[2]",
                        "{\"first\":\"Jayden\",\"last\":\"f\"}",
                        "1"}
            });
        }

        private String varContent;
        private String jmesPath;
        private String expectedResult;
        private String expectedMatchNumber;

        public OneMatchOnAllExtractedValues(String varContent, String jmesPath, String expectedResult, String expectedMatchNumber) {
            this.varContent = varContent;
            this.jmesPath = jmesPath;
            this.expectedResult = expectedResult;
            this.expectedMatchNumber = expectedMatchNumber;
        }

        @Test
        public void test() {
            JMeterContext context = JMeterContextService.getContext();
            JMESPathExtractor processor = setupProcessor(context, "-1");
            JMeterVariables vars = new JMeterVariables();
            context.setVariables(vars);
            vars.put("contentvar", varContent);
            processor.setJmesPathExpression(jmesPath);
            processor.process();
            assertThat(vars.get(REFERENCE_NAME), CoreMatchers.is(CoreMatchers.nullValue()));
            assertThat(vars.get(REFERENCE_NAME + "_1"), CoreMatchers.is(expectedResult));
            assertThat(vars.get(REFERENCE_NAME_MATCH_NUMBER), CoreMatchers.is(expectedMatchNumber));
        }
    }

    @RunWith(Parameterized.class)
    public static class MultipleMatchesOnAllExtractedValues {

        @Parameters
        public static Collection<Object[]> data() {
            return Arrays.asList(new Object[][] {
                {"[\"one\", \"two\"]", "[*]", new String[] {"\"one\"", "\"two\""}, "2"},
                {"[\"a\", \"b\", \"c\", \"d\", \"e\", \"f\"]", "[0:3]", new String[] {"\"a\"", "\"b\"","\"c\""}, "3"},
                {"{\r\n" + "  \"people\": [\r\n" + "    {\"first\": \"James\", \"last\": \"d\"},\r\n"
                        + "    {\"first\": \"Jacob\", \"last\": \"e\"},\r\n"
                        + "    {\"first\": \"Jayden\", \"last\": \"f\"},\r\n" + "    {\"missing\": \"different\"}\r\n"
                        + "  ],\r\n" + "  \"foo\": {\"bar\": \"baz\"}\r\n" + "}", "people[:2].first", new String[] {"\"James\"", "\"Jacob\""}, "2" },
            });
        }

        private String varContent;
        private String jmesPath;
        private String[] expectedResults;
        private String expectedMatchNumber;

        public MultipleMatchesOnAllExtractedValues(String varContent, String jmesPath, String[] expectedResults, String expectedMatchNumber) {
            this.varContent = varContent;
            this.jmesPath = jmesPath;
            this.expectedResults = expectedResults;
            this.expectedMatchNumber = expectedMatchNumber;
        }

        @Test
        public void test() {
            JMeterContext context = JMeterContextService.getContext();
            JMESPathExtractor processor = setupProcessor(context, "-1");
            JMeterVariables vars = new JMeterVariables();
            context.setVariables(vars);
            // test1
            processor.setJmesPathExpression(jmesPath);
            vars.put("contentvar", varContent);
            processor.process();
            assertThat(vars.get(REFERENCE_NAME), CoreMatchers.is(CoreMatchers.nullValue()));
            for (int i = 0; i < expectedResults.length; i++) {
                assertThat(vars.get(REFERENCE_NAME + "_"+(i+1)), CoreMatchers.is(expectedResults[i]));
            }
            assertThat(vars.get(REFERENCE_NAME_MATCH_NUMBER), CoreMatchers.is(expectedMatchNumber));
        }
    }

    @RunWith(Parameterized.class)
    public static class MatchNumberMoreThanZeroOn1ExtractedValue {

        private static final String TEST_DATA = "{\r\n" + "  \"people\": [\r\n" + "    {\"first\": \"James\", \"last\": \"d\", \"age\":10},\r\n"
                + "    {\"first\": \"Jacob\", \"last\": \"e\", \"age\":20},\r\n"
                + "    {\"first\": \"Jayden\", \"last\": \"f\", \"age\":30},\r\n"
                + "    {\"missing\": \"different\"}\r\n" + "  ],\r\n" + "  \"foo\": {\"bar\": \"baz\"}\r\n"
                + "}";

        @Parameters
        public static Collection<String[]> data() {
            return Arrays.asList(new String[][] {
                {TEST_DATA, "people[:3].first", "1", "\"James\"", "3"},
                {TEST_DATA, "people[:3].first", "2", "\"Jacob\"", "3"},
                {TEST_DATA, "people[:3].first", "3", "\"Jayden\"", "3"},
                {TEST_DATA, "people[:3].age", "3", "30", "3"},
                {TEST_DATA, "people[:3].first", "4", DEFAULT_VALUE, "3"}
            });
        }

        private String varContent;
        private String jmesPath;
        private String expectedResult;
        private String expectedMatchNumber;
        private String matchNumber;

        public MatchNumberMoreThanZeroOn1ExtractedValue(String varContent, String jmesPath,
                String matchNumber, String expectedResult, String expectedMatchNumber) {
            this.varContent = varContent;
            this.jmesPath = jmesPath;
            this.expectedResult = expectedResult;
            this.matchNumber = matchNumber;
            this.expectedMatchNumber = expectedMatchNumber;
        }

        @Test
        public void test() {
            JMeterContext context = JMeterContextService.getContext();
            JMESPathExtractor processor = setupProcessor(context, "1");
            JMeterVariables vars = new JMeterVariables();
            context.setVariables(vars);
            vars.put("contentvar",
                    varContent);
            processor.setMatchNumber(matchNumber);
            processor.setJmesPathExpression(jmesPath);
            processor.process();
            assertThat(vars.get(REFERENCE_NAME), CoreMatchers.is(expectedResult));
            assertThat(vars.get(REFERENCE_NAME_MATCH_NUMBER), CoreMatchers.is(expectedMatchNumber));
        }
    }

    public static class NotParameterizedPart {

        @Test
        public void testRandomElementOneMatch() {
            JMeterContext context = JMeterContextService.getContext();
            JMESPathExtractor processor = setupProcessor(context, "0");
            JMeterVariables vars = new JMeterVariables();
            context.setVariables(vars);

            processor.setJmesPathExpression("a.b.c.d");
            vars.put("contentvar", "{\"a\": {\"b\": {\"c\": {\"d\": \"value\"}}}}");
            processor.process();
            assertThat(vars.get(REFERENCE_NAME), CoreMatchers.is("\"value\""));
            assertThat(vars.get(REFERENCE_NAME + "_1"), CoreMatchers.is(CoreMatchers.nullValue()));
            assertThat(vars.get(REFERENCE_NAME_MATCH_NUMBER), CoreMatchers.is("1"));
        }

        @Test
        public void testRandomElementMultipleMatches() {
            JMeterContext context = JMeterContextService.getContext();
            JMESPathExtractor processor = setupProcessor(context, "0");
            JMeterVariables vars = new JMeterVariables();
            context.setVariables(vars);

            vars.put("contentvar", "[\"one\", \"two\"]");
            processor.setJmesPathExpression("[*]");
            processor.process();
            assertThat(vars.get(REFERENCE_NAME),
                    CoreMatchers.is(CoreMatchers.anyOf(CoreMatchers.is("\"one\""), CoreMatchers.is("\"two\""))));
            assertThat(vars.get(REFERENCE_NAME + "_1"), CoreMatchers.is(CoreMatchers.nullValue()));
            assertThat(vars.get(REFERENCE_NAME + "_2"), CoreMatchers.is(CoreMatchers.nullValue()));
            assertThat(vars.get(REFERENCE_NAME_MATCH_NUMBER), CoreMatchers.is("2"));
        }

        @Test
        public void testEmptySourceData() {
            JMeterContext context = JMeterContextService.getContext();
            JMESPathExtractor processor = setupProcessor(context, "-1");
            JMeterVariables vars = new JMeterVariables();
            context.setVariables(vars);

            vars.put("contentvar", "");
            processor.setJmesPathExpression("[*]");
            processor.process();
            assertThat(vars.get(REFERENCE_NAME), CoreMatchers.is(DEFAULT_VALUE));
            assertThat(vars.get(REFERENCE_NAME_MATCH_NUMBER), CoreMatchers.is(CoreMatchers.nullValue()));
        }

        @Test
        public void testErrorInJMESPath() {
            JMeterContext context = JMeterContextService.getContext();
            JMESPathExtractor processor = setupProcessor(context, "-1");
            JMeterVariables vars = new JMeterVariables();
            context.setVariables(vars);

            vars.put("contentvar", "{\"a\": {\"b\": {\"c\": {\"d\": \"value\"}}}}");
            processor.setJmesPathExpression("$.k");
            processor.process();
            assertThat(vars.get(REFERENCE_NAME), CoreMatchers.is(DEFAULT_VALUE));
            assertThat(vars.get(REFERENCE_NAME+ "_1"), CoreMatchers.nullValue());
            assertThat(vars.get(REFERENCE_NAME_MATCH_NUMBER), CoreMatchers.nullValue());
        }

        @Test
        public void testNoMatch() {
            JMeterContext context = JMeterContextService.getContext();
            JMESPathExtractor processor = setupProcessor(context, "-1");
            JMeterVariables vars = new JMeterVariables();
            context.setVariables(vars);
            vars.put("contentvar", "{\"a\": {\"b\": {\"c\": {\"d\": \"value\"}}}}");
            processor.setJmesPathExpression("a.b.c.f");
            processor.process();
            assertThat(vars.get(REFERENCE_NAME), CoreMatchers.is(DEFAULT_VALUE));
            assertThat(vars.get(REFERENCE_NAME+ "_1"), CoreMatchers.nullValue());
            assertThat(vars.get(REFERENCE_NAME_MATCH_NUMBER), CoreMatchers.is("0"));
        }
    }
}
