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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Collection;
import java.util.LinkedList;

import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jorphan.test.JMeterSerialTest;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CSVReadFunctionTest extends JMeterTestCase implements JMeterSerialTest {

    private static final Logger log = LoggerFactory.getLogger(CSVReadFunctionTest.class);

    // Create the CSVRead function and set its parameters.
    private static CSVRead setCSVReadParams(String p1, String p2) throws Exception {
        CSVRead cr = new CSVRead();
        Collection<CompoundVariable> parms = new LinkedList<>();
        if (p1 != null) {
            parms.add(new CompoundVariable(p1));
        }
        if (p2 != null) {
            parms.add(new CompoundVariable(p2));
        }
        cr.setParameters(parms);
        return cr;
    }

    @Test
    public void testCSVParamsFail() throws Exception {
        try {
            setCSVReadParams(null, null);
            fail("Should have failed");
        } catch (InvalidVariableException e) {
        }
        try {
            setCSVReadParams(null, "");
            fail("Should have failed");
        } catch (InvalidVariableException e) {
        }
        try {
            setCSVReadParams("", null);
            fail("Should have failed");
        } catch (InvalidVariableException e) {
        }
    }

    @Test
    public void testCSVNoFile() throws Exception {

        CSVRead cr1 = setCSVReadParams("does/not-exist.csv", "1");
        log.info("Expecting file not found");
        assertEquals("", cr1.execute(null, null));

        CSVRead cr2 = setCSVReadParams("does/not-exist.csv", "next");
        log.info("Expecting no entry for file");
        assertEquals("", cr2.execute(null, null));

        CSVRead cr3 = setCSVReadParams("does/not-exist.csv", "*ABC");
        log.info("Expecting file not found");
        assertEquals("", cr3.execute(null, null));

        CSVRead cr4 = setCSVReadParams("*ABC", "1");
        log.info("Expecting cannot open file");
        assertEquals("", cr4.execute(null, null));
    }

    @Test
    public void testCSValias() throws Exception {
        CSVRead cr1 = setCSVReadParams("testfiles/unit/CSVReadFunctionTest.csv", "*A");
        CSVRead cr2 = setCSVReadParams("*A", "1");
        CSVRead cr3 = setCSVReadParams("*A", "next");

        CSVRead cr4 = setCSVReadParams("testfiles/unit/CSVReadFunctionTest.csv", "*B");
        CSVRead cr5 = setCSVReadParams("*B", "2");
        CSVRead cr6 = setCSVReadParams("*B", "next");

        assertEquals("", cr1.execute(null, null)); // open as *A
        assertEquals("b1", cr2.execute(null, null)); // col 1, line 1, *A
        assertEquals("", cr4.execute(null, null)); // open as *B
        assertEquals("c1", cr5.execute(null, null)); // col2 line 1
        assertEquals("", cr3.execute(null, null)); // *A next
        assertEquals("b2", cr2.execute(null, null)); // col 1, line 2, *A
        assertEquals("c1", cr5.execute(null, null)); // col2, line 1, *B
        assertEquals("", cr6.execute(null, null)); // *B next
        assertEquals("c2", cr5.execute(null, null)); // col2, line 2, *B
    }

    // Check blank lines are treated as EOF
    @Test
    public void CSVBlankLine() throws Exception {
        CSVRead csv1 = setCSVReadParams("testfiles/testblank.csv", "1");
        CSVRead csv2 = setCSVReadParams("testfiles/testblank.csv", "next");

        for (int i = 1; i <= 2; i++) {
            assertEquals("b1", csv1.execute(null, null));
            assertEquals("", csv2.execute(null, null));
            assertEquals("b2", csv1.execute(null, null));
            assertEquals("", csv2.execute(null, null));
        }
    }

    @Test
    public void CSVRun() throws Exception {
        CSVRead cr1 = setCSVReadParams("testfiles/unit/CSVReadFunctionTest.csv", "1");
        CSVRead cr2 = setCSVReadParams("testfiles/unit/CSVReadFunctionTest.csv", "2");
        CSVRead cr3 = setCSVReadParams("testfiles/unit/CSVReadFunctionTest.csv", "3");
        CSVRead cr4 = setCSVReadParams("testfiles/unit/CSVReadFunctionTest.csv", "next");
        CSVRead cr5 = setCSVReadParams("", "0");
        CSVRead cr6 = setCSVReadParams("", "next");

        assertEquals("b1", cr1.execute(null, null));
        assertEquals("c1", cr2.execute(null, null));
        assertEquals("d1", cr3.execute(null, null));

        assertEquals("", cr4.execute(null, null));
        assertEquals("b2", cr1.execute(null, null));
        assertEquals("c2", cr2.execute(null, null));
        assertEquals("d2", cr3.execute(null, null));

        assertEquals("", cr4.execute(null, null));
        assertEquals("b3", cr1.execute(null, null));
        assertEquals("c3", cr2.execute(null, null));
        assertEquals("d3", cr3.execute(null, null));

        assertEquals("", cr4.execute(null, null));
        assertEquals("b4", cr1.execute(null, null));
        assertEquals("c4", cr2.execute(null, null));
        assertEquals("d4", cr3.execute(null, null));

        assertEquals("", cr4.execute(null, null));
        assertEquals("b1", cr1.execute(null, null));
        assertEquals("c1", cr2.execute(null, null));
        assertEquals("d1", cr3.execute(null, null));

        assertEquals("a1", cr5.execute(null, null));
        assertEquals("", cr6.execute(null, null));
        assertEquals("a2", cr5.execute(null, null));
    }
}
