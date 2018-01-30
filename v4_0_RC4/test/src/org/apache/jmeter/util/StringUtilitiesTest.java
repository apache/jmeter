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

package org.apache.jmeter.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class StringUtilitiesTest {


    @Test
    public void testServer() throws Exception {
        BeanShellServer bshs = new BeanShellServer(9876, "");
        assertNotNull(bshs);
        // Not sure we can test anything else here
    }
    
    @Test
    public void testSub1() throws Exception {
        String input = "http://jakarta.apache.org/jmeter/index.html";
        String pattern = "jakarta.apache.org";
        String sub = "${server}";
        assertEquals("http://${server}/jmeter/index.html", StringUtilities.substitute(input, pattern, sub));
    }

    @Test
    public void testSub2() throws Exception {
        String input = "arg1=param1;param1";
        String pattern = "param1";
        String sub = "${value}";
        assertEquals("arg1=${value};${value}", StringUtilities.substitute(input, pattern, sub));
    }

    @Test
    public void testSub3() throws Exception {
        String input = "jakarta.apache.org";
        String pattern = "jakarta.apache.org";
        String sub = "${server}";
        assertEquals("${server}", StringUtilities.substitute(input, pattern, sub));
    }

    @Test
    public void testSub4() throws Exception {
        String input = "//a///b////c";
        String pattern = "//";
        String sub = "/";
        assertEquals("/a//b//c", StringUtilities.substitute(input, pattern, sub));
    }

}
