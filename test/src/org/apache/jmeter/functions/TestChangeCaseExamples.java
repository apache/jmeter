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

package org.apache.jmeter.functions;

import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.functions.ChangeCase.ChangeCaseMode;
import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class TestChangeCaseExamples extends JMeterTestCase {

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] { { " spaces before and after ",
                new String[] { " SPACES BEFORE AND AFTER ",
                        " spaces before and after ",
                        " spaces before and after ", "SpacesBeforeAndAfter",
                        "spacesBeforeAndAfter" } },
                { "m4u file",
                        new String[] { "M4U FILE", "m4u file", "M4u file",
                                "M4uFile", "m4uFile" } },
                { "With Ümläuts", new String[] { "WITH ÜMLÄUTS", "with ümläuts",
                        "With Ümläuts", "WithÜmläuts", "withÜmläuts" } },
                { "some_underscores_",
                        new String[] { "SOME_UNDERSCORES_", "some_underscores_",
                                "Some_underscores_", "SomeUnderscores",
                                "someUnderscores" } },
                { "$#$more,extra-symbols!",
                        new String[] { "$#$MORE,EXTRA-SYMBOLS!",
                                "$#$more,extra-symbols!",
                                "$#$more,extra-symbols!", "MoreExtraSymbols",
                                "moreExtraSymbols" } },
                { "$ $ special space",
                        new String[] { "$ $ SPECIAL SPACE", "$ $ special space",
                                "$ $ special space", "SpecialSpace",
                                "specialSpace" } },
                { " ", new String[] { " ", " ", " ", "", "" }},

        });
    }

    private ChangeCase changeCase;
    private SampleResult result;
    private String input;
    private String[] results;

    public TestChangeCaseExamples(String input, String[] results) {
        this.input = input;
        this.results = results;
    }

    @Before
    public void setUp() {
        changeCase = new ChangeCase();
        JMeterContext jmctx = JMeterContextService.getContext();
        String data = "dummy data";
        result = new SampleResult();
        result.setResponseData(data, null);
        JMeterVariables vars = new JMeterVariables();
        jmctx.setVariables(vars);
        jmctx.setPreviousResult(result);
    }

    @Test
    public void test() throws Exception {
        int count = 0;
        for (ChangeCaseMode mode : ChangeCaseMode.values()) {
            assertThat(callChangeCase(input, mode),
                    CoreMatchers.is(results[count++]));
        }
    }

    private String execute(String... params) throws InvalidVariableException {
        List<CompoundVariable> testParams = Arrays.stream(params)
                .map(CompoundVariable::new).collect(Collectors.toList());
        changeCase.setParameters(testParams);
        return changeCase.execute(result, null);
    }

    private String callChangeCase(String input, ChangeCaseMode mode)
            throws Exception {
        return execute(input, mode.toString());
    }

}
