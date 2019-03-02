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

public class EvalFunctionTest extends JMeterTestCase {

    private JMeterContext jmctx = null;
    private JMeterVariables vars = null;
    
    @Before
    public void setUp() {
        jmctx = JMeterContextService.getContext();
        jmctx.setVariables(new JMeterVariables());
        vars = jmctx.getVariables();
    }
    
    @Test
    public void evalTest1() throws Exception {
        EvalFunction eval = new EvalFunction();
        vars.put("query","select ${column} from ${table}");
        vars.put("column","name");
        vars.put("table","customers");
        Collection<CompoundVariable> parms;
        String s;
        
        parms = makeParams("${query}",null,null);
        eval.setParameters(parms);
        s = eval.execute(null,null);
        assertEquals("select name from customers",s);
        
    }

    @Test
    public void evalTest2() throws Exception {
        EvalVarFunction evalVar = new EvalVarFunction();
        vars.put("query","select ${column} from ${table}");
        vars.put("column","name");
        vars.put("table","customers");
        Collection<CompoundVariable> parms;
        String s;
        
        parms = makeParams("query",null,null);
        evalVar.setParameters(parms);
        s = evalVar.execute(null,null);
        assertEquals("select name from customers",s);
    }
}
