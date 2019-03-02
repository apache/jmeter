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

import static org.apache.jmeter.functions.FunctionTestHelper.makeParams;
import static org.junit.Assert.assertEquals;

import java.util.Collection;

import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.junit.Before;
import org.junit.Test;

public class VariableTest extends JMeterTestCase {

    private JMeterContext jmctx = null;
    private JMeterVariables vars = null;

    @Before
    public void setUp() {
        jmctx = JMeterContextService.getContext();
        jmctx.setVariables(new JMeterVariables());
        vars = jmctx.getVariables();
    }

    @Test
    public void variableTest1() throws Exception {
        Variable r = new Variable();
        vars.put("A_1","a1");
        vars.put("A_2","a2");
        vars.put("one","1");
        vars.put("two","2");
        vars.put("V","A");
        Collection<CompoundVariable> parms;
        String s;

        parms = makeParams("V",null,null);
        r.setParameters(parms);
        s = r.execute(null,null);
        assertEquals("A",s);

        parms = makeParams("V","DEFAULT",null);
        r.setParameters(parms);
        s = r.execute(null,null);
        assertEquals("A",s);

        parms = makeParams("EMPTY","DEFAULT",null);
        r.setParameters(parms);
        s = r.execute(null,null);
        assertEquals("DEFAULT",s);

        parms = makeParams("X",null,null);
        r.setParameters(parms);
        s = r.execute(null,null);
        assertEquals("X",s);

        parms = makeParams("A${X}",null,null);
        r.setParameters(parms);
        s = r.execute(null,null);
        assertEquals("A${X}",s);

        parms = makeParams("A_1",null,null);
        r.setParameters(parms);
        s = r.execute(null,null);
        assertEquals("a1",s);

        parms = makeParams("A_2",null,null);
        r.setParameters(parms);
        s = r.execute(null,null);
        assertEquals("a2",s);

        parms = makeParams("A_${two}",null,null);
        r.setParameters(parms);
        s = r.execute(null,null);
        assertEquals("a2",s);

        parms = makeParams("${V}_${one}",null,null);
        r.setParameters(parms);
        s = r.execute(null,null);
        assertEquals("a1",s);
    }
}
