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

package org.apache.jmeter.visualizers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.apache.jmeter.samplers.SampleResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestSamplingStatCalculator {

    private SamplingStatCalculator ssc;
    @BeforeEach
    public void setUp(){
        ssc = new SamplingStatCalculator("JUnit");
    }

    @Test
    public void testGetCurrentSample() {
        assertNotNull(ssc.getCurrentSample()); // probably needed to avoid NPEs with GUIs
    }

    @Test
    public void testGetAvgPageBytes() {
        SampleResult res = new SampleResult();
        assertEquals(0, ssc.getAvgPageBytes(), 0);
        res.setResponseData("abcdef", "UTF-8");
        ssc.addSample(res);
        res.setResponseData("abcde", "UTF-8");
        ssc.addSample(res);
        res.setResponseData("abcd", "UTF-8");
        ssc.addSample(res);
        assertEquals(5, ssc.getAvgPageBytes(), 0);
    }
}
