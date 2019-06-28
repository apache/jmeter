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
import org.apache.jorphan.util.JMeterStopThreadException;
import org.junit.Test;

public class StringFromFileFunctionTest extends JMeterTestCase {

    @Test
    public void SFFTest1() throws Exception {
        StringFromFile sff1 = SFFParams("testfiles/SFFTest#'.'txt", "", "1", "3");
        assertEquals("uno", sff1.execute());
        assertEquals("dos", sff1.execute());
        assertEquals("tres", sff1.execute());
        assertEquals("cuatro", sff1.execute());
        assertEquals("cinco", sff1.execute());
        assertEquals("one", sff1.execute());
        assertEquals("two", sff1.execute());
        sff1.execute();
        sff1.execute();
        assertEquals("five", sff1.execute());
        assertEquals("eins", sff1.execute());
        sff1.execute();
        sff1.execute();
        sff1.execute();
        assertEquals("fuenf", sff1.execute());
        try {
            sff1.execute();
            fail("Should have thrown JMeterStopThreadException");
        } catch (JMeterStopThreadException e) {
            // expected
        }
    }

    @Test
    public void SFFTest2() throws Exception {
        StringFromFile sff = SFFParams("testfiles/SFFTest1.txt", "", null, null);
        assertEquals("uno", sff.execute());
        assertEquals("dos", sff.execute());
        assertEquals("tres", sff.execute());
        assertEquals("cuatro", sff.execute());
        assertEquals("cinco", sff.execute());
        assertEquals("uno", sff.execute()); // Restarts
        assertEquals("dos", sff.execute());
        assertEquals("tres", sff.execute());
        assertEquals("cuatro", sff.execute());
        assertEquals("cinco", sff.execute());
    }

    @Test
    public void SFFTest3() throws Exception {
        StringFromFile sff = SFFParams("testfiles/SFFTest1.txt", "", "", "");
        assertEquals("uno", sff.execute());
        assertEquals("dos", sff.execute());
        assertEquals("tres", sff.execute());
        assertEquals("cuatro", sff.execute());
        assertEquals("cinco", sff.execute());
        assertEquals("uno", sff.execute()); // Restarts
        assertEquals("dos", sff.execute());
        assertEquals("tres", sff.execute());
        assertEquals("cuatro", sff.execute());
        assertEquals("cinco", sff.execute());
    }

    @Test
    public void SFFTest4() throws Exception {
        StringFromFile sff = SFFParams("xxtestfiles/SFFTest1.txt", "", "", "");
        assertEquals(StringFromFile.ERR_IND, sff.execute());
        assertEquals(StringFromFile.ERR_IND, sff.execute());
    }

    // Test that only loops twice
    @Test
    public void SFFTest5() throws Exception {
        StringFromFile sff = SFFParams("testfiles/SFFTest1.txt", "", "", "2");
        assertEquals("uno", sff.execute());
        assertEquals("dos", sff.execute());
        assertEquals("tres", sff.execute());
        assertEquals("cuatro", sff.execute());
        assertEquals("cinco", sff.execute());
        assertEquals("uno", sff.execute());
        assertEquals("dos", sff.execute());
        assertEquals("tres", sff.execute());
        assertEquals("cuatro", sff.execute());
        assertEquals("cinco", sff.execute());
        try {
            sff.execute();
            fail("Should have thrown JMeterStopThreadException");
        } catch (JMeterStopThreadException e) {
            // expected
        }
    }

    // Create the StringFromFile function and set its parameters.
    private static StringFromFile SFFParams(String p1, String p2, String p3, String p4) throws Exception {
        StringFromFile sff = new StringFromFile();
        Collection<CompoundVariable> parms = new LinkedList<>();
        if (p1 != null) {
            parms.add(new CompoundVariable(p1));
        }
        if (p2 != null) {
            parms.add(new CompoundVariable(p2));
        }
        if (p3 != null) {
            parms.add(new CompoundVariable(p3));
        }
        if (p4 != null) {
            parms.add(new CompoundVariable(p4));
        }
        sff.setParameters(parms);
        return sff;
    }
}
