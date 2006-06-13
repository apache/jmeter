/*
 * Copyright 2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

/**
 * Package to test JMeterUtils methods 
 */
     
package org.apache.jmeter.util;

import junit.framework.TestCase;

public class TestJMeterUtils extends TestCase {

    public TestJMeterUtils() {
        super();
    }

    public TestJMeterUtils(String arg0) {
        super(arg0);
    }
    

        public void testSplit1() {
            String in = "a,bc,,"; // Test ignore trailing split characters
            String out[] = JMeterUtils.split(in, ",","?");// with default
            assertEquals(3, out.length);
            assertEquals("a", out[0]);
            assertEquals("bc", out[1]);
            assertEquals("?", out[2]);
        }

        public void testSplit1a() {
            String in = "a,bc,,"; // Test ignore trailing split characters
            String out[] = JMeterUtils.split(in, ",",""); // with no default
            assertEquals(3, out.length);
            assertEquals("a", out[0]);
            assertEquals("bc", out[1]);
            assertEquals("", out[2]);
        }

        public void testSplit2() {
            String in = ",,a,bc"; // Test leading split characters with default
            String out[] = JMeterUtils.split(in, ",","?");
            assertEquals(3, out.length);
            assertEquals("?", out[0]);
            assertEquals("a", out[1]);
            assertEquals("bc", out[2]);
        }
        
        public void testSplit3() {
            String in = ",,a,bc"; // Test leading split characters no default
            String out[] = JMeterUtils.split(in, ",","");
            assertEquals(3, out.length);
            assertEquals("", out[0]);
            assertEquals("a", out[1]);
            assertEquals("bc", out[2]);
        }        

        public void testSplit4() {
            String in = ",,,a,bc"; // Test leading split characters no default
            String out[] = JMeterUtils.split(in, ",","");
            assertEquals(4, out.length);
            assertEquals("", out[0]);
            assertEquals("", out[1]);
            assertEquals("a", out[2]);
            assertEquals("bc", out[3]);
        }        
}
