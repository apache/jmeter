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

package org.apache.jmeter.functions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.gui.action.AbstractAction;
import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.JMeterUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Test the function __escapeOroRegexpCars
 *
 * To prepare for removal of Oro, we changed the behavior of the function
 * to use Oro or JDKs internal Regex implementation based on the JMeter property
 * {@code jmeter.regex.engine}
 *
 * Those two implementations have a slightly different way of escaping, so
 * test both here, until we got rid of Oro.
 */
public class TestEscapeOroRegexpChars extends JMeterTestCase {

    private SampleResult result;
    private Collection<CompoundVariable> params;
    private JMeterVariables vars;

    @BeforeEach
    void setUp() {
        result = new SampleResult();
        JMeterContext jmctx = JMeterContextService.getContext();
        String data = "The quick brown fox";
        result.setResponseData(data, null);
        vars = new JMeterVariables();
        jmctx.setVariables(vars);
        jmctx.setPreviousResult(result);
        params = new ArrayList<>();
    }

    @Test
    void testParameterCount() throws Exception {
        checkInvalidParameterCounts(new EscapeOroRegexpChars(), 1, 2);
    }

    static Collection<Arguments> functionAndParams() {
        var testValuesPerImplementation = Map.of(
                "oro", Map.of(
                    "toto1titi", "toto1titi",
                        "toto titi", "toto\\ titi",
                        "toto(.+?)titi", "toto\\(\\.\\+\\?\\)titi",
                        "[^\"].+?","\\[\\^\\\"\\]\\.\\+\\?"
                    ),
                "java", Map.of(
                        "toto1titi", "\\Qtoto1titi\\E",
                        "toto titi", "\\Qtoto titi\\E",
                        "toto(.+?)titi", "\\Qtoto(.+?)titi\\E",
                        "[^\"].+?", "\\Q[^\"].+?\\E"
                    )
        );
        Collection<Arguments> args = new ArrayList<>();
        for (var implementation: testValuesPerImplementation.entrySet()) {
            JMeterUtils.setProperty("jmeter.regex.engine", implementation.getKey());
            AbstractFunction function = new EscapeOroRegexpChars();
            for (var testValues: implementation.getValue().entrySet()) {
                args.add(Arguments.of(function, testValues.getKey(), testValues.getValue()));
            }
        }
        return args;
    }

    @ParameterizedTest
    @MethodSource("functionAndParams")
    void testEscaping(AbstractFunction function, String value, String expected) throws Exception {
        params.add(new CompoundVariable(value));
        function.setParameters(params);
        String ret = function.execute(result, null);
        Assertions.assertEquals(expected, ret);
    }

    @ParameterizedTest
    @MethodSource("functionAndParams")
    void testEscapingWithVar(AbstractFunction function, String value, String expected) throws Exception {
        params.add(new CompoundVariable(value));
        params.add(new CompoundVariable("exportedVar"));
        function.setParameters(params);
        String ret = function.execute(result, null);
        Assertions.assertEquals(expected, ret);
        Assertions.assertEquals(expected, vars.get("exportedVar"));
    }

}
