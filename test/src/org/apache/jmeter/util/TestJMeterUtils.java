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

/**
 * Package to test JMeterUtils methods
 */

package org.apache.jmeter.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TestJMeterUtils {

    @Test
    public void testGetResourceFileAsText() throws Exception{
        String sep = System.getProperty("line.separator");
        assertEquals("line one" + sep + "line two" + sep, JMeterUtils.getResourceFileAsText("resourcefile.txt"));
    }

    @Test
    public void testGetResourceFileAsTextWithMisingResource() throws Exception{
        assertEquals("", JMeterUtils.getResourceFileAsText("not_existant_resourcefile.txt"));
    }

    @Test
    public void testGesResStringDefaultWithNonExistantKey() throws Exception {
        assertEquals("[res_key=noValidKey]", JMeterUtils.getResString("noValidKey"));
    }
}
