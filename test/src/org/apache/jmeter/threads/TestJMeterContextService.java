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

package org.apache.jmeter.threads;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TestJMeterContextService {

    @Test
    public void testCounts(){
        assertEquals(0,JMeterContextService.getNumberOfThreads());
        assertEquals(0,JMeterContextService.getTotalThreads());
        incrNumberOfThreads();
        assertEquals(1,JMeterContextService.getNumberOfThreads());
        assertEquals(0,JMeterContextService.getTotalThreads());
        decrNumberOfThreads();
        assertEquals(0,JMeterContextService.getTotalThreads());
        assertEquals(0,JMeterContextService.getNumberOfThreads());
        JMeterContextService.addTotalThreads(27);
        JMeterContextService.addTotalThreads(27);
        assertEquals(54,JMeterContextService.getTotalThreads());
        assertEquals(0,JMeterContextService.getNumberOfThreads());
    }

    // Give access to the method for test code
    public static void incrNumberOfThreads(){
        JMeterContextService.incrNumberOfThreads();
    }
    // Give access to the method for test code
    public static void decrNumberOfThreads(){
        JMeterContextService.decrNumberOfThreads();
    }
}
