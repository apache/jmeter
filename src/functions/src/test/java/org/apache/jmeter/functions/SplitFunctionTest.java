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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SplitFunctionTest extends JMeterTestCase {

    private JMeterVariables vars = null;

    @BeforeEach
    public void setUp() {
        JMeterContext jmeterContext = JMeterContextService.getContext();
        jmeterContext.setVariables(new JMeterVariables());
        vars = jmeterContext.getVariables();
    }

    @Test
    public void shouldThrowExceptionWhenParameterCountIsInvalid() {
        InvalidVariableException invalidVariableException = assertThrows(InvalidVariableException.class, () ->
                splitParams("a,b,c", null, null),
            ""
        );
        assertEquals(
            "__split called with wrong number of parameters. Actual: 1. Expected: >= 2 and <= 3",
            invalidVariableException.getMessage()
        );
    }

    @Test
    public void shouldSplitWithoutAnyArguments() throws Exception {
        String src = "a,b,c";
        SplitFunction split;
        split = splitParams(src, "VAR1", null);
        assertEquals(src, split.execute());
        assertEquals(src, vars.get("VAR1"));
        assertEquals("3", vars.get("VAR1_n"));
        assertEquals("a", vars.get("VAR1_1"));
        assertEquals("b", vars.get("VAR1_2"));
        assertEquals("c", vars.get("VAR1_3"));
        assertNull(vars.get("VAR1_4"));

        split = splitParams(src, "VAR1", "");
        assertEquals(src, split.execute());
        assertEquals(src, vars.get("VAR1"));
        assertEquals("3", vars.get("VAR1_n"));
        assertEquals("a", vars.get("VAR1_1"));
        assertEquals("b", vars.get("VAR1_2"));
        assertEquals("c", vars.get("VAR1_3"));
        assertNull(vars.get("VAR1_4"));

        split = splitParams(src, "VAR2", ",");
        assertEquals(src, split.execute());
        assertEquals(src, vars.get("VAR2"));
        assertEquals("3", vars.get("VAR2_n"));
        assertEquals("a", vars.get("VAR2_1"));
        assertEquals("b", vars.get("VAR2_2"));
        assertEquals("c", vars.get("VAR2_3"));
        assertNull(vars.get("VAR2_4"));

        src = "a|b|c";
        split = splitParams(src, "VAR3", "|");
        assertEquals(src, split.execute());
        assertEquals(src, vars.get("VAR3"));
        assertEquals("3", vars.get("VAR3_n"));
        assertEquals("a", vars.get("VAR3_1"));
        assertEquals("b", vars.get("VAR3_2"));
        assertEquals("c", vars.get("VAR3_3"));
        assertNull(vars.get("VAR3_4"));

        src = "a|b||";
        split = splitParams(src, "VAR4", "|");
        assertEquals(src, split.execute());
        assertEquals(src, vars.get("VAR4"));
        assertEquals("4", vars.get("VAR4_n"));
        assertEquals("a", vars.get("VAR4_1"));
        assertEquals("b", vars.get("VAR4_2"));
        assertEquals("?", vars.get("VAR4_3"));
        assertNull(vars.get("VAR4_5"));

        src = "a,,c";
        vars.put("VAR", src);
        split = splitParams("${VAR}", "VAR", null);
        assertEquals(src, split.execute());
        assertEquals("3", vars.get("VAR_n"));
        assertEquals("a", vars.get("VAR_1"));
        assertEquals("?", vars.get("VAR_2"));
        assertEquals("c", vars.get("VAR_3"));
        assertNull(vars.get("VAR_4"));

        src = "a,b";
        vars.put("VAR", src);
        split = splitParams("${VAR}", "VAR", null);
        assertEquals(src, split.execute());
        assertEquals("2", vars.get("VAR_n"));
        assertEquals("a", vars.get("VAR_1"));
        assertEquals("b", vars.get("VAR_2"));
        assertNull(vars.get("VAR_3"));

        src = "a,,c,";
        vars.put("VAR", src);
        split = splitParams("${VAR}", "VAR5", null);
        assertEquals(src, split.execute());
        assertEquals("4", vars.get("VAR5_n"));
        assertEquals("a", vars.get("VAR5_1"));
        assertEquals("?", vars.get("VAR5_2"));
        assertEquals("c", vars.get("VAR5_3"));
        assertEquals("?", vars.get("VAR5_4"));
        assertNull(vars.get("VAR5_5"));
    }

    @Test
    public void shouldSplitWithPreviousResultOnly() throws Exception {
        String src = "a,,c,";
        vars.put("VAR", src);
        SplitFunction split = splitParams("${VAR}", "VAR5", null);

        SampleResult previousResult = new SampleResult();
        previousResult.setResponseData("Some data", null);

        assertEquals(src, split.execute(previousResult, null));
        assertEquals("4", vars.get("VAR5_n"));
        assertEquals("a", vars.get("VAR5_1"));
        assertEquals("?", vars.get("VAR5_2"));
        assertEquals("c", vars.get("VAR5_3"));
        assertEquals("?", vars.get("VAR5_4"));
        assertNull(vars.get("VAR5_5"));
    }

    // Create the SplitFile function and set its parameters.
    private static SplitFunction splitParams(String p1, String p2, String p3) throws Exception {
        SplitFunction split = new SplitFunction();
        Collection<CompoundVariable> parms = new ArrayList<>();
        parms.add(new CompoundVariable(p1));
        if (p2 != null) {
            parms.add(new CompoundVariable(p2));
        }
        if (p3 != null) {
            parms.add(new CompoundVariable(p3));
        }
        split.setParameters(parms);
        return split;
    }

}
