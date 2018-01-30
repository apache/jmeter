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

import org.apache.jmeter.samplers.SampleResult;
import org.junit.Test;

public class LengthPrefixedBinaryTCPClientImplTest {

    @Test
    public void testError() throws Exception {
        ByteArrayOutputStream os = null;
        ByteArrayInputStream is = null;
        LengthPrefixedBinaryTCPClientImpl lp = new LengthPrefixedBinaryTCPClientImpl();
        try {
            lp.write(os, is);
            fail("Expected java.lang.UnsupportedOperationException");
        } catch (java.lang.UnsupportedOperationException expected) {
        }
    }

    @Test
    public void testValid() throws Exception {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        LengthPrefixedBinaryTCPClientImpl lp = new LengthPrefixedBinaryTCPClientImpl();
        final String DATA = "31323334353637";
        lp.write(os, DATA);
        os.close();
        final byte[] byteArray = os.toByteArray();
        assertEquals(2+(DATA.length()/2), byteArray.length);
        ByteArrayInputStream is = new ByteArrayInputStream(byteArray);
        assertEquals(DATA, lp.read(is, new SampleResult()));
    }

}
