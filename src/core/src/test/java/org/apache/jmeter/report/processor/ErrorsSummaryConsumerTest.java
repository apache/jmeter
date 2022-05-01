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

package org.apache.jmeter.report.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.apache.jmeter.report.core.Sample;
import org.apache.jmeter.report.core.SampleMetadata;
import org.apache.jmeter.report.utils.MetricUtils;
import org.apache.jmeter.save.CSVSaveService;
import org.junit.jupiter.api.Test;

public class ErrorsSummaryConsumerTest {

    @Test
    public void testGetErrorKey() {
        SampleMetadata metadata = new SampleMetadata(',', new String[] { CSVSaveService.SUCCESSFUL,
                CSVSaveService.RESPONSE_CODE, CSVSaveService.RESPONSE_MESSAGE, CSVSaveService.FAILURE_MESSAGE });
        Sample sample = new Sample(0, metadata, new String[] { "false", "", "", "FailureMessage" });
        assertEquals("FailureMessage", ErrorsSummaryConsumer.getErrorKey(sample));

        sample = new Sample(0, metadata, new String[] { "false", "200", "", "FailureMessage" });
        assertEquals("FailureMessage", ErrorsSummaryConsumer.getErrorKey(sample));

        sample = new Sample(0, metadata, new String[] { "false", "200", "", "" });
        assertEquals(MetricUtils.ASSERTION_FAILED, ErrorsSummaryConsumer.getErrorKey(sample));

        sample = new Sample(0, metadata, new String[] { "false", "200", "",
                "Test failed: text expected to contain /<title>Some html text</title>/" });
        assertEquals("Test failed: text expected to contain /&lt;title&gt;Some html text&lt;/title&gt;/",
                ErrorsSummaryConsumer.getErrorKey(sample));

        sample = new Sample(0, metadata, new String[] { "false", "200", "",
                "Test failed: text expected to contain /{\"glossary\": { \"title\": \"example glossary\"}}/" });
        assertEquals("Test failed: text expected to contain /{&quot;glossary&quot;: { &quot;title&quot;: &quot;example glossary&quot;}}/",
                ErrorsSummaryConsumer.getErrorKey(sample));

        sample = new Sample(0, metadata, new String[] { "true", "200", "", "" });
        assertEquals("", ErrorsSummaryConsumer.getErrorKey(sample));

        sample = new Sample(0, metadata, new String[] { "false", "403", "", "" });
        assertEquals("403", ErrorsSummaryConsumer.getErrorKey(sample));

        sample = new Sample(0, metadata, new String[] { "false", "500", "Server Error", "" });
        assertEquals("500/Server Error", ErrorsSummaryConsumer.getErrorKey(sample));
    }

    @Test
    public void testErrorSampleCounter() {
        ErrorsSummaryConsumer consumer = new ErrorsSummaryConsumer();
        Sample sample = createSample(false);
        AbstractSummaryConsumer<Long>.SummaryInfo info = consumer.new SummaryInfo(false);
        assertNull(info.getData());
        consumer.updateData(info, sample);
        assertEquals(Long.valueOf(1), info.getData());
        consumer.updateData(info, sample);
        assertEquals(Long.valueOf(2), info.getData());
    }

    private Sample createSample(boolean success) {
        SampleMetadata metadata = new SampleMetadata(',', CSVSaveService.SUCCESSFUL);
        return new Sample(0, metadata, String.valueOf(success));
    }

}
