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

package org.apache.jorphan;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jorphan.util.XMLBuffer;
import org.junit.Test;

public class TestXMLBuffer extends JMeterTestCase {

    @Test
    public void test1() throws Exception{
        XMLBuffer xb = new XMLBuffer();
        xb.openTag("start");
        assertEquals("<start></start>\n",xb.toString());
    }

    @Test
    public void test2() throws Exception{
        XMLBuffer xb = new XMLBuffer();
        xb.tag("start","now");
        assertEquals("<start>now</start>\n",xb.toString());
    }
    @Test
    public void test3() throws Exception{
        XMLBuffer xb = new XMLBuffer();
        xb.openTag("abc");
        xb.closeTag("abc");
        assertEquals("<abc></abc>\n",xb.toString());
    }
    @Test
    public void test4() throws Exception{
        XMLBuffer xb = new XMLBuffer();
        xb.openTag("abc");
        try {
            xb.closeTag("abcd");
            fail("Should have caused IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
    }
}
