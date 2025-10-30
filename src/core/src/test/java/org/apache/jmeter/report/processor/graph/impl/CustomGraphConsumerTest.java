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

package org.apache.jmeter.report.processor.graph.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.apache.jmeter.report.core.Sample;
import org.apache.jmeter.report.core.SampleMetadata;
import org.apache.jmeter.report.dashboard.JsonizerVisitor;
import org.apache.jmeter.report.processor.MapResultData;
import org.apache.jmeter.report.processor.ResultData;
import org.apache.jmeter.report.processor.graph.GroupData;
import org.apache.jmeter.report.processor.graph.GroupInfo;
import org.apache.jmeter.report.processor.graph.SeriesData;
import org.apache.jmeter.report.processor.graph.TimeStampKeysSelector;
import org.apache.jmeter.save.CSVSaveService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CustomGraphConsumerTest {

    private CustomGraphConsumer customGraphConsumer;
    private MapResultData resultData;
    private Map<String, GroupInfo> map;
    // data array can't be initialized in the init()
    private String[] data = {"1527089951383", "0", "Read-compute", "200", "OK", "setupRegion 1-1", "true", "", "492", "0", "1", "1",
            "null", "0", "0", "0", "/stream1a/master.m3u8?e=0&h=56345c61b7b415e0260c19963a153092", "null", "5500000", "null",
            "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null"};
    private SampleMetadata sampleMetaData = createTestMetaData();

    @BeforeEach
    public void init() {
        customGraphConsumer = new CustomGraphConsumer();
        customGraphConsumer.setGranularity(60000);
        customGraphConsumer.setTitle("graph title");
        customGraphConsumer.setXAxis("X axis name");
        customGraphConsumer.setYAxis("Y axis name");
        customGraphConsumer.setContentMessage("content message");
        customGraphConsumer.setSampleVariableName("ulp_lag_ratio");

        map = customGraphConsumer.createGroupInfos();

        resultData = new MapResultData();
    }

    @Test
    public void testGetters() {
        assertEquals("graph title", customGraphConsumer.getTitle());
        assertEquals("X axis name", customGraphConsumer.getXAxis());
        assertEquals("Y axis name", customGraphConsumer.getYAxis());
        assertEquals("content message", customGraphConsumer.getContentMessage());
        assertEquals("ulp_lag_ratio", customGraphConsumer.getSampleVariableName());
        assertFalse(customGraphConsumer.getIsNativeSampleVariableName());

        // bytes is one of the native sample variables names
        customGraphConsumer.setSampleVariableName(CSVSaveService.CSV_BYTES);
        assertTrue(customGraphConsumer.getIsNativeSampleVariableName());
    }

    @Test
    public void testInitializeExtraResults() {
        customGraphConsumer.initializeExtraResults(resultData);

        JsonizerVisitor jsonizer = new JsonizerVisitor();
        for (Map.Entry<String, ResultData> entrySet : resultData.entrySet()) {
            Object testedValue = entrySet.getValue().accept(jsonizer);
            String key = entrySet.getKey();

            if (key.equals("granularity")) {
                assertEquals("60000", testedValue);
            } else if (key.equals("X_Axis")) {
                assertEquals("\"X axis name\"", testedValue);
            } else if (key.equals("Y_Axis")) {
                assertEquals("\"Y axis name\"", testedValue);
            } else if (key.equals("sample_Metric_Name")) {
                assertEquals("\"ulp_lag_ratio\"", testedValue);
            } else if (key.equals("content_Message")) {
                assertEquals("\"content message\"", testedValue);
            }
        }
    }

    @Test
    public void testCreateTimeStampKeysSelector() {
        TimeStampKeysSelector keysSelector = new TimeStampKeysSelector();
        keysSelector.setSelectBeginTime(false);
        assertEquals(keysSelector.getGranularity(), customGraphConsumer.createTimeStampKeysSelector().getGranularity());
    }

    @Test
    public void testCreateGroupInfos() {
        // Testing defaults values
        assertTrue(map.containsKey("Generic group"));
        assertFalse(map.containsKey("foo"));
        assertEquals(org.apache.jmeter.report.processor.MeanAggregatorFactory.class,
                map.get("Generic group").getAggregatorFactory().getClass());
        GroupData groupData = map.get("Generic group").getGroupData();
        assertNull(groupData.getOverallSeries());
        assertEquals(new HashMap<String, SeriesData>(), groupData.getSeriesInfo());

        // Testing native sample variable
        customGraphConsumer.setSampleVariableName("bytes");
        Sample sample = new Sample(0, sampleMetaData, data);
        Double testedValue = map.get("Generic group").getValueSelector().select("bytes", sample);
        assertEquals(492.0, testedValue);

        // Testing non-native sample variable
        customGraphConsumer.setSampleVariableName("mm-miss");
        testedValue = map.get("Generic group").getValueSelector().select("mm-miss", sample);
        assertNull(testedValue);

        // Testing empty data value, the change between data and data2
        // is on the last value that switchs from "null" to ""
        String[] data2 = {"1527089951383", "0", "Read-compute", "200", "OK", "setupRegion 1-1", "true", "", "492", "0", "1", "1",
                "null", "0", "0", "0", "/stream1a/master.m3u8?e=0&h=56345c61b7b415e0260c19963a153092", "null", "5500000", "null",
                "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", ""};
        sample = new Sample(0, sampleMetaData, data2);
        testedValue = map.get("Generic group").getValueSelector().select("mm-miss", sample);
        assertNull(testedValue);
    }

    // Test the exception when the column data is not a Double
    @Test
    public void testCreateGroupInfosExceptions() {
        Sample sample = new Sample(0, sampleMetaData, data);
        customGraphConsumer.setSampleVariableName("label");

        Assertions.assertThrows(
                Exception.class,
                () -> map.get("Generic group").getValueSelector().select("label", sample));
    }

    @Test
    public void testSelectMetric() {
        Sample sample = new Sample(0, sampleMetaData, data);
        String testString = map.get("Generic group").getSeriesSelector().select(sample).toString();
        assertEquals("[ulp_lag_ratio]", testString);
    }

    // Create a static SampleMetadataObject
    private static SampleMetadata createTestMetaData() {
        String columnsString = "timeStamp,elapsed,label,responseCode,responseMessage,threadName,success,failureMessage,bytes,sentBytes,"
                + "grpThreads,allThreads,URL,Latency,IdleTime,Connect,\"stream\",\"aws_region\",\"bitrate\",\"ulp_buffer_fill\",\"ulp_lag_time\","
                + "\"ulp_play_time\",\"ulp_lag_ratio\",\"lag_ratio_wo_bf\",\"ulp_dwn_time\",\"ulp_hits\",\"ulp_avg_chunk_time\","
                + "\"ulp_avg_manifest_time\",\"mm-hit\",\"mm-miss\",\"cm-hit\",\"cm-miss\",\"ts-hit\",\"ts-miss\"";
        String[] columns = new String[34];
        int lastComa = 0;
        int columnIndex = 0;
        for (int i = 0; i < columnsString.length(); i++) {
            if (columnsString.charAt(i) == ',') {
                columns[columnIndex] = columnsString.substring(lastComa, i);
                lastComa = i + 1;
                columnIndex++;
            } else if (i + 1 == columnsString.length()) {
                columns[columnIndex] = columnsString.substring(lastComa, i + 1);
            }
        }
        return new SampleMetadata(',', columns);
    }

}
