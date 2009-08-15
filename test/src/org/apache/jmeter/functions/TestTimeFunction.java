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

import java.util.Collection;
import java.util.LinkedList;
import java.util.Locale;

import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;

public class TestTimeFunction extends JMeterTestCase {
        private Function variable;

        private SampleResult result;

        private Collection<CompoundVariable> params;

        private JMeterVariables vars;

        private JMeterContext jmctx = null;

        private String value;
        
        public TestTimeFunction(String name) {
            super(name);
        }

        @Override
        public void setUp() {
            jmctx = JMeterContextService.getContext();
            vars = new JMeterVariables();
            jmctx.setVariables(vars);
            jmctx.setPreviousResult(result);
            params = new LinkedList<CompoundVariable>();
            result = new SampleResult();
            variable = new TimeFunction();
        }

        public void testDefault() throws Exception {
            variable.setParameters(params);
            long before = System.currentTimeMillis();
            value = variable.execute(result, null);
            long now= Long.parseLong(value);
            long after = System.currentTimeMillis();
            assertTrue(now >= before && now <= after);
        }
        
        public void testDefault1() throws Exception {
            params.add(new CompoundVariable());
            variable.setParameters(params);
            long before = System.currentTimeMillis();
            value = variable.execute(result, null);
            long now= Long.parseLong(value);
            long after = System.currentTimeMillis();
            assertTrue(now >= before && now <= after);
        }
        
        public void testDefault2() throws Exception {
            params.add(new CompoundVariable());
            params.add(new CompoundVariable());
            variable.setParameters(params);
            long before = System.currentTimeMillis();
            value = variable.execute(result, null);
            long now= Long.parseLong(value);
            long after = System.currentTimeMillis();
            assertTrue(now >= before && now <= after);
        }
        
        public void testDefaultNone() throws Exception {
            long before = System.currentTimeMillis();
            value = variable.execute(result, null);
            long now= Long.parseLong(value);
            long after = System.currentTimeMillis();
            assertTrue(now >= before && now <= after);
        }
        
        public void testTooMany() throws Exception {
            params.add(new CompoundVariable("YMD"));
            params.add(new CompoundVariable("NAME"));
            params.add(new CompoundVariable("YMD"));
            try {
                variable.setParameters(params);
                fail("Should have raised InvalidVariableException");
            } catch (InvalidVariableException ignored){                
            }
        }
        
        public void testYMD() throws Exception {
            params.add(new CompoundVariable("YMD"));
            params.add(new CompoundVariable("NAME"));
            variable.setParameters(params);
            value = variable.execute(result, null);
            assertEquals(8,value.length());
            assertEquals(value,vars.get("NAME"));
        }

        public void testYMDnoV() throws Exception {
            params.add(new CompoundVariable("YMD"));
            variable.setParameters(params);
            value = variable.execute(result, null);
            assertEquals(8,value.length());
            assertNull(vars.get("NAME"));
        }

        public void testHMS() throws Exception {
            params.add(new CompoundVariable("HMS"));
            variable.setParameters(params);
            value = variable.execute(result, null);
            assertEquals(6,value.length());
        }

        public void testYMDHMS() throws Exception {
            params.add(new CompoundVariable("YMDHMS"));
            variable.setParameters(params);
            value = variable.execute(result, null);
            assertEquals(15,value.length());
        }

        public void testUSER1() throws Exception {
            params.add(new CompoundVariable("USER1"));
            variable.setParameters(params);
            value = variable.execute(result, null);
            assertEquals(0,value.length());
        }

        public void testUSER2() throws Exception {
            params.add(new CompoundVariable("USER2"));
            variable.setParameters(params);
            value = variable.execute(result, null);
            assertEquals(0,value.length());
        }

        public void testFixed() throws Exception {
            params.add(new CompoundVariable("'Fixed text'"));
            variable.setParameters(params);
            value = variable.execute(result, null);
            assertEquals("Fixed text",value);
        }

        public void testMixed() throws Exception {
            params.add(new CompoundVariable("G"));
            variable.setParameters(params);
            Locale locale = Locale.getDefault();
            Locale.setDefault(Locale.ENGLISH);
            value = variable.execute(result, null);
            Locale.setDefault(locale);
            assertEquals("AD",value);
        }
}
