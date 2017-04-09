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
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CSVReadFunctionTest extends JMeterTestCase {

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
        String s;

        CSVRead cr1 = setCSVReadParams("xtestfiles/test.csv", "1");
        log.info("Expecting file not found");
        s = cr1.execute(null, null);
        assertEquals("", s);

        CSVRead cr2 = setCSVReadParams("xtestfiles/test.csv", "next");
        log.info("Expecting no entry for file");
        s = cr2.execute(null, null);
        assertEquals("", s);

        CSVRead cr3 = setCSVReadParams("xtestfiles/test.csv", "*ABC");
        log.info("Expecting file not found");
        s = cr3.execute(null, null);
        assertEquals("", s);

        CSVRead cr4 = setCSVReadParams("*ABC", "1");
        log.info("Expecting cannot open file");
        s = cr4.execute(null, null);
        assertEquals("", s);
    }
    
    @Test
    public void testCSValias() throws Exception {
        CSVRead cr1 = setCSVReadParams("testfiles/test.csv", "*A");
        CSVRead cr2 = setCSVReadParams("*A", "1");
        CSVRead cr3 = setCSVReadParams("*A", "next");

        CSVRead cr4 = setCSVReadParams("testfiles/test.csv", "*B");
        CSVRead cr5 = setCSVReadParams("*B", "2");
        CSVRead cr6 = setCSVReadParams("*B", "next");

        String s;

        s = cr1.execute(null, null); // open as *A
        assertEquals("", s);
        s = cr2.execute(null, null); // col 1, line 1, *A
        assertEquals("b1", s);

        s = cr4.execute(null, null);// open as *B
        assertEquals("", s);
        s = cr5.execute(null, null);// col2 line 1
        assertEquals("c1", s);

        s = cr3.execute(null, null);// *A next
        assertEquals("", s);
        s = cr2.execute(null, null);// col 1, line 2, *A
        assertEquals("b2", s);

        s = cr5.execute(null, null);// col2, line 1, *B
        assertEquals("c1", s);

        s = cr6.execute(null, null);// *B next
        assertEquals("", s);

        s = cr5.execute(null, null);// col2, line 2, *B
        assertEquals("c2", s);
    }
    
    // Check blank lines are treated as EOF
    @Test
    public void CSVBlankLine() throws Exception {
        CSVRead csv1 = setCSVReadParams("testfiles/testblank.csv", "1");
        CSVRead csv2 = setCSVReadParams("testfiles/testblank.csv", "next");

        String s;

        for (int i = 1; i <= 2; i++) {
            s = csv1.execute(null, null);
            assertEquals("b1", s);

            s = csv2.execute(null, null);
            assertEquals("", s);

            s = csv1.execute(null, null);
            assertEquals("b2", s);

            s = csv2.execute(null, null);
            assertEquals("", s);
        }
    }
    
    @Test
    public void CSVRun() throws Exception {
        CSVRead cr1 = setCSVReadParams("testfiles/test.csv", "1");
        CSVRead cr2 = setCSVReadParams("testfiles/test.csv", "2");
        CSVRead cr3 = setCSVReadParams("testfiles/test.csv", "3");
        CSVRead cr4 = setCSVReadParams("testfiles/test.csv", "next");
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
