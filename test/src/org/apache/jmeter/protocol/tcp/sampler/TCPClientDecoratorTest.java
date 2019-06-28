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
 * Test class for TCPClientDecorator utility methods.
 *
 */
package org.apache.jmeter.protocol.tcp.sampler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

public class TCPClientDecoratorTest {


    @Test
    public void testIntToByteArray() throws Exception {
        byte[] ba;
        int len = 2;
        ba = TCPClientDecorator.intToByteArray(0, len);
        assertEquals(len, ba.length);
        assertEquals(0, ba[0]);
        assertEquals(0, ba[1]);

        ba = TCPClientDecorator.intToByteArray(15, len);
        assertEquals(len, ba.length);
        assertEquals(0, ba[0]);
        assertEquals(15, ba[1]);

        ba = TCPClientDecorator.intToByteArray(255, len);
        assertEquals(len, ba.length);
        assertEquals(0, ba[0]);
        assertEquals(-1, ba[1]);

        ba = TCPClientDecorator.intToByteArray(256, len);
        assertEquals(len, ba.length);
        assertEquals(1, ba[0]);
        assertEquals(0, ba[1]);

        ba = TCPClientDecorator.intToByteArray(-1, len);
        assertEquals(len, ba.length);
        assertEquals(-1, ba[0]);
        assertEquals(-1, ba[1]);

        ba = TCPClientDecorator.intToByteArray(Short.MAX_VALUE, len);
        assertEquals(len, ba.length);
        assertEquals(127, ba[0]);
        assertEquals(-1, ba[1]);

        ba = TCPClientDecorator.intToByteArray(Short.MIN_VALUE, len);
        assertEquals(len, ba.length);
        assertEquals(-128, ba[0]);
        assertEquals(0, ba[1]);

        try {
            ba = TCPClientDecorator.intToByteArray(Short.MIN_VALUE-1, len);
            fail();
        } catch (IllegalArgumentException iae) {
        }

        try {
            ba = TCPClientDecorator.intToByteArray(Short.MAX_VALUE+1, len);
            fail();
        } catch (IllegalArgumentException iae) {
        }

        len = 4;
        ba = TCPClientDecorator.intToByteArray(0, len);
        assertEquals(len, ba.length);
        assertEquals(0, ba[0]);
        assertEquals(0, ba[1]);
        assertEquals(0, ba[2]);
        assertEquals(0, ba[3]);

        ba = TCPClientDecorator.intToByteArray(15, len);
        assertEquals(len, ba.length);
        assertEquals(0, ba[0]);
        assertEquals(0, ba[1]);
        assertEquals(0, ba[2]);
        assertEquals(15, ba[3]);

        ba = TCPClientDecorator.intToByteArray(255, len);
        assertEquals(len, ba.length);
        assertEquals(0, ba[0]);
        assertEquals(0, ba[1]);
        assertEquals(0, ba[2]);
        assertEquals(-1, ba[3]);

        ba = TCPClientDecorator.intToByteArray(-1, len);
        assertEquals(len, ba.length);
        assertEquals(-1, ba[0]);
        assertEquals(-1, ba[1]);
        assertEquals(-1, ba[2]);
        assertEquals(-1, ba[3]);

        ba = TCPClientDecorator.intToByteArray(256, len);
        assertEquals(len, ba.length);
        assertEquals(0, ba[0]);
        assertEquals(0, ba[1]);
        assertEquals(1, ba[2]);
        assertEquals(0, ba[3]);

        ba = TCPClientDecorator.intToByteArray(65535, len);
        assertEquals(len, ba.length);
        assertEquals(0, ba[0]);
        assertEquals(0, ba[1]);
        assertEquals(-1, ba[2]);
        assertEquals(-1, ba[3]);

        ba = TCPClientDecorator.intToByteArray(65536, len);
        assertEquals(len, ba.length);
        assertEquals(0, ba[0]);
        assertEquals(1, ba[1]);
        assertEquals(0, ba[2]);
        assertEquals(0, ba[3]);

        ba = TCPClientDecorator.intToByteArray(Integer.MIN_VALUE, len);
        assertEquals(len, ba.length);
        assertEquals(-128, ba[0]);
        assertEquals(0, ba[1]);
        assertEquals(0, ba[2]);
        assertEquals(0, ba[3]);

        ba = TCPClientDecorator.intToByteArray(Integer.MAX_VALUE, len);
        assertEquals(len, ba.length);
        assertEquals(127, ba[0]);
        assertEquals(-1, ba[1]);
        assertEquals(-1, ba[2]);
        assertEquals(-1, ba[3]);

        // Check illegal array lengths
        try {
            ba = TCPClientDecorator.intToByteArray(0, 0);
            fail();
        } catch (IllegalArgumentException iae) {
        }

        try {
            ba = TCPClientDecorator.intToByteArray(0, 1);
            fail();
        } catch (IllegalArgumentException iae) {
        }

        try {
            ba = TCPClientDecorator.intToByteArray(0, 3);
            fail();
        } catch (IllegalArgumentException iae) {
        }
        try {
            TCPClientDecorator.intToByteArray(0, 5);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException iae) {
        }
    }

    @Test
    public void testByteArrayToInt() throws Exception {
        byte[] ba;

        ba = new byte[] { 0, 0 };
        assertEquals(0, TCPClientDecorator.byteArrayToInt(ba));

        ba = new byte[] { 0, 15 };
        assertEquals(15, TCPClientDecorator.byteArrayToInt(ba));

        ba = new byte[] { 0, -1 };
        assertEquals(255, TCPClientDecorator.byteArrayToInt(ba));

        ba = new byte[] { 1, 0 };
        assertEquals(256, TCPClientDecorator.byteArrayToInt(ba));

        ba = new byte[] { -1, -1 };
        assertEquals(-1, TCPClientDecorator.byteArrayToInt(ba));

        ba = new byte[] { 0, 0, -1, -1 };
        assertEquals(65535, TCPClientDecorator.byteArrayToInt(ba));

        ba = new byte[] { 0, 1, 0, 0 };
        assertEquals(65536, TCPClientDecorator.byteArrayToInt(ba));

        ba = new byte[] { 0, 0, 0, 0 };
        assertEquals(0, TCPClientDecorator.byteArrayToInt(ba));

        ba = new byte[] { -128, 0, 0, 0 };
        assertEquals(Integer.MIN_VALUE, TCPClientDecorator.byteArrayToInt(ba));

        ba = new byte[] { 127, -1, -1, -1 };
        assertEquals(Integer.MAX_VALUE, TCPClientDecorator.byteArrayToInt(ba));

        // test invalid byte arrays
        try {
            TCPClientDecorator.byteArrayToInt(null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected){
            // ignored
        }

        try {
            TCPClientDecorator.byteArrayToInt(new byte[]{});
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected){
            // ignored
        }

        try {
            TCPClientDecorator.byteArrayToInt(new byte[]{0});
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected){
            // ignored
        }

        try {
            TCPClientDecorator.byteArrayToInt(new byte[]{0,0,0});
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected){
            // ignored
        }

        try {
            TCPClientDecorator.byteArrayToInt(new byte[]{0,0,0});
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected){
            // ignored
        }

    }


    @Test
    public void testLoopBack() throws Exception {
        assertEquals(Short.MIN_VALUE, TCPClientDecorator.byteArrayToInt(TCPClientDecorator.intToByteArray(Short.MIN_VALUE, 2)));
        assertEquals(Short.MAX_VALUE, TCPClientDecorator.byteArrayToInt(TCPClientDecorator.intToByteArray(Short.MAX_VALUE, 2)));
        assertEquals(Integer.MIN_VALUE, TCPClientDecorator.byteArrayToInt(TCPClientDecorator.intToByteArray(Integer.MIN_VALUE, 4)));
        assertEquals(Integer.MAX_VALUE, TCPClientDecorator.byteArrayToInt(TCPClientDecorator.intToByteArray(Integer.MAX_VALUE, 4)));
    }
}
