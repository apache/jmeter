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

package org.apache.jmeter.visualizers.backend.elasticsearch;

import static org.junit.Assert.assertNotNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;

import org.apache.jmeter.samplers.SampleResult;
import org.junit.Before;
import org.junit.Test;

public class TestElasticsearchBackend {
    private ElasticSearchMetric metricNoCI;

    private ElasticSearchMetric metricCI;

    @Before
    public void setUp() throws Exception {
        metricCI = new ElasticSearchMetric(
                new SampleResult(),
                "info",
                "yyyy-MM-dd'T'HH:mm:ss.SSSZZ",
                1,
                false,
                false,
                new HashSet<String>()
        );
        metricNoCI = new ElasticSearchMetric(
                new SampleResult(),
                "info",
                "yyyy-MM-dd'T'HH:mm:ss.SSSZZ",
                0,
                false,
                false,
                new HashSet<String>()
        );
    }

    @Test
    public void testGetElapsedTimeNoCI() throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date testDate = this.metricNoCI.getElapsedTime(false);
        assertNotNull("testDate = " + sdf.format(testDate), sdf.format(testDate));
    }

    @Test
    public void testGetElapsedTimeCI() throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date testDate = this.metricCI.getElapsedTime(true);
        assertNotNull("testDate = " + sdf.format(testDate), sdf.format(testDate));
    }
}
