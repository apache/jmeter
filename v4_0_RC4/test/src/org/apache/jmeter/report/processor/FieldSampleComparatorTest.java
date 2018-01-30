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
package org.apache.jmeter.report.processor;

import static org.junit.Assert.assertTrue;

import org.apache.jmeter.report.core.Sample;
import org.apache.jmeter.report.core.SampleMetadata;
import org.junit.Before;
import org.junit.Test;

public class FieldSampleComparatorTest {

    private SampleMetadata sampleMetadata;
    private FieldSampleComparator comparator;

    @Before
    public void setUp() throws Exception {
        sampleMetadata = new SampleMetadata(',', "test");
        comparator = new FieldSampleComparator("test");
        comparator.initialize(sampleMetadata);
    }

    @Test
    public void testCompare() {
        Sample s1 = new Sample(0, sampleMetadata, "1");
        Sample s2 = new Sample(1, sampleMetadata, "2");
        assertTrue(comparator.compare(s1, s2) < 0);
        assertTrue(comparator.compare(s2, s1) > 0);
        assertTrue(comparator.compare(s2, s2) == 0);
        assertTrue(comparator.compare(s1, s1) == 0);
    }

}
