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

import org.apache.jmeter.util.JMeterUtils;
import org.junit.After;
import org.junit.Before;

/**
 * Test JavaScript function with Rhino engine
 *
 */
public class TestJavascriptFunctionWithRhino extends TestJavascriptFunction {
    @Before
    public void setUp() {
        JMeterUtils.getJMeterProperties().put("javascript.use_rhino", "true");       
        super.setUp();
    }
    
    @After
    public void tearDown() {
        JMeterUtils.getJMeterProperties().remove("javascript.use_rhino");
    }
}
