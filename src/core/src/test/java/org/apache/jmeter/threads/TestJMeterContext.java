/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jmeter.threads;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class TestJMeterContext {

    @Test
    void threadStateOutlivesSamplesAndClear() {
        JMeterContext context = new JMeterContext();
        Object state = new Object();
        context.getThreadState().put("state", state);
        context.getSamplerContext().put("sample", new Object());

        context.cleanAfterSample();
        assertNull(context.getSamplerContext().get("sample"), "sampler context should be cleaned after a sample");
        assertEquals(state, context.getThreadState().get("state"), "thread state should be kept after a sample");

        // JMeterThread clears the context before the thread listeners release what the thread state holds
        context.clear();
        assertEquals(state, context.getThreadState().get("state"), "thread state should be kept by clear()");
    }
}
