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

/*
 * Test class for BinaryTCPClientImpl utility methods.
 *
 */
package org.apache.jmeter.protocol.tcp.sampler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.apache.jmeter.samplers.SampleResult;
import org.apache.jorphan.util.JOrphanUtils;
import org.junit.Test;

public class BinaryTCPClientImplTest {

    @Test
    public void testHexStringToByteArray() throws Exception {
        byte [] ba;
        ba = BinaryTCPClientImpl.hexStringToByteArray("");
        assertEquals(0, ba.length);

        ba = BinaryTCPClientImpl.hexStringToByteArray("00");
        assertEquals(1, ba.length);
        assertEquals(0, ba[0]);

        ba = BinaryTCPClientImpl.hexStringToByteArray("0f107F8081ff");
        assertEquals(6, ba.length);
        assertEquals(15,   ba[0]);
        assertEquals(16,   ba[1]);
        assertEquals(127,  ba[2]);
        assertEquals(-128, ba[3]);
        assertEquals(-127, ba[4]);
        assertEquals(-1,   ba[5]);
        try {
            ba = BinaryTCPClientImpl.hexStringToByteArray("0f107f8081ff1");// odd chars
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected){
            // ignored
        }
        try {
            BinaryTCPClientImpl.hexStringToByteArray("0f107xxf8081ff"); // invalid
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected){
            // ignored
        }
    }

    @Test
    public void testLoopBack() throws Exception {
        assertEquals("0f107f8081ff", JOrphanUtils.baToHexString(BinaryTCPClientImpl.hexStringToByteArray("0f107f8081ff")));
    }

    @Test
    public void testRoundTrip() throws Exception {
        BinaryTCPClientImpl bi = new BinaryTCPClientImpl();
        InputStream is = null;
        try {
            bi.write(null, is);
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException expected) {
            // ignored
        }
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        bi.write(os, "3132333435"); // '12345'
        os.close();
        assertEquals("12345",os.toString("ISO-8859-1"));
        ByteArrayInputStream bis = new ByteArrayInputStream(os.toByteArray());
        assertEquals("3132333435",bi.read(bis, new SampleResult()));
    }
}
