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

import org.apache.jmeter.report.core.Sample;
import org.apache.jmeter.report.core.SampleMetadata;
import org.apache.jmeter.report.utils.MetricUtils;
import org.apache.jmeter.save.CSVSaveService;
import org.junit.Assert;
import org.junit.Test;

public class ErrorsSummaryConsumerTest {

    @Test
    public void testGetErrorKey() {
        SampleMetadata metadata = new SampleMetadata(',', new String[] { CSVSaveService.SUCCESSFUL,
                CSVSaveService.RESPONSE_CODE, CSVSaveService.RESPONSE_MESSAGE, CSVSaveService.FAILURE_MESSAGE });
        Sample sample = new Sample(0, metadata, new String[] { "false", "", "", "FailureMessage" });
        Assert.assertEquals("FailureMessage", ErrorsSummaryConsumer.getErrorKey(sample));

        sample = new Sample(0, metadata, new String[] { "false", "200", "", "FailureMessage" });
        Assert.assertEquals("FailureMessage", ErrorsSummaryConsumer.getErrorKey(sample));

        sample = new Sample(0, metadata, new String[] { "false", "200", "",
                "Test failed: text expected to contain /<title>Some html text</title>/" });
        Assert.assertEquals("Test failed: text expected to contain \\/&lt;title&gt;Some html text&lt;\\/title&gt;\\/",
                ErrorsSummaryConsumer.getErrorKey(sample));

        sample = new Sample(0, metadata, new String[] { "false", "200", "",
                "Test failed: text expected to contain /{\"glossary\": { \"title\": \"example glossary\"}}/" });
        Assert.assertEquals("Test failed: text expected to contain \\/{&quot;glossary&quot;: { &quot;title&quot;: &quot;example glossary&quot;}}\\/",
                ErrorsSummaryConsumer.getErrorKey(sample));

        sample = new Sample(0, metadata, new String[] { "true", "200", "", "" });
        Assert.assertEquals(MetricUtils.ASSERTION_FAILED, ErrorsSummaryConsumer.getErrorKey(sample));

        sample = new Sample(0, metadata, new String[] { "false", "500", "Server Error", "FailureMessage" });
        Assert.assertEquals("500/Server Error", ErrorsSummaryConsumer.getErrorKey(sample));
    }


    @Test
    public void testErrorSampleCounter() {
        ErrorsSummaryConsumer consumer = new ErrorsSummaryConsumer();
        Sample sample = createSample(false);
        AbstractSummaryConsumer<Long>.SummaryInfo info = consumer.new SummaryInfo(
                false);
        Assert.assertEquals(null, info.getData());
        consumer.updateData(info, sample);
        Assert.assertEquals(Long.valueOf(1), info.getData());
        consumer.updateData(info, sample);
        Assert.assertEquals(Long.valueOf(2), info.getData());
    }

    /**
     * Create a dummy sample that is either successful or a failure depending on
     * the {@code success} flag
     *
     * @param success
     *            flag do determine if the sample should be successful or not
     * @return newly created sample
     */
    private Sample createSample(boolean success) {
        SampleMetadata metadata = new SampleMetadata(',',
                CSVSaveService.SUCCESSFUL);
        Sample sample = new Sample(0, metadata, String.valueOf(success));
        return sample;
    }

}
