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

package org.apache.jmeter.gui.util;

import static org.junit.Assert.assertFalse;

import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.junit.categories.NeedGuiTests;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(NeedGuiTests.class)
public final class TestMenuFactory extends JMeterTestCase {

    private static void check(String s, int i) throws Exception {
        assertFalse("The number of " + s + " should not be 0", 0 == i);
    }

    @Test
    public void testMenu() throws Exception {
        
        check("menumap", MenuFactory.menuMap_size());

        check("assertions", MenuFactory.assertions_size());
        check("configElements", MenuFactory.configElements_size());
        check("controllers", MenuFactory.controllers_size());
        check("listeners", MenuFactory.listeners_size());
        check("nonTestElements", MenuFactory.nonTestElements_size());
        check("postProcessors", MenuFactory.postProcessors_size());
        check("preProcessors", MenuFactory.preProcessors_size());
        check("samplers", MenuFactory.samplers_size());
        check("timers", MenuFactory.timers_size());

        check("elementstoskip", MenuFactory.elementsToSkip_size());
    }
}
