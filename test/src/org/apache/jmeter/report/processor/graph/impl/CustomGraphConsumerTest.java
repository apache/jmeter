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
package org.apache.jmeter.report.processor.graph.impl;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

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
import org.junit.Before;
import org.junit.Test;

public class CustomGraphConsumerTest {
    
    private CustomGraphConsumer customGraphConsumer;
    private MapResultData resultData;
    private TimeStampKeysSelector keysSelector;
    private Map<String, GroupInfo> map;
    // data array can't be initialized in the init()
    private String[] data = {"1527089951383", "0", "Read-compute", "200", "OK", "setupRegion 1-1", "true", "", "492", "0", "1", "1",
            "null", "0", "0", "0", "/stream1a/master.m3u8?e=0&h=56345c61b7b415e0260c19963a153092", "null", "5500000", "null",
            "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null"}; 
    private SampleMetadata sampleMetaData = createTestMetaData();
    
    @Before
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
        assertThat(customGraphConsumer.getTitle(), equalTo("graph title"));
        assertThat(customGraphConsumer.getXAxis(), equalTo("X axis name"));
        assertThat(customGraphConsumer.getYAxis(), equalTo("Y axis name"));
        assertThat(customGraphConsumer.getContentMessage(), equalTo("content message"));
        assertThat(customGraphConsumer.getSampleVariableName(), equalTo("ulp_lag_ratio"));
        assertThat(customGraphConsumer.getIsNativeSampleVariableName(), equalTo(false));
        
        // bytes is one of the native sample variables names
        customGraphConsumer.setSampleVariableName(CSVSaveService.CSV_BYTES);
        assertThat(customGraphConsumer.getIsNativeSampleVariableName(), equalTo(true));
    }
    
    
    @Test
    public void testInitializeExtraResults() {
        customGraphConsumer.initializeExtraResults(resultData);
        
        JsonizerVisitor jsonizer = new JsonizerVisitor();
        for(Entry<String, ResultData> entrySet : resultData.entrySet()) {
            Object testedValue = entrySet.getValue().accept(jsonizer);
            String key = entrySet.getKey();
            
            if(key.equals("granularity")) {
                assertThat(testedValue, equalTo("60000"));
            }else if(key.equals("X_Axis")) {
                assertThat(testedValue, equalTo("\"X axis name\""));
            }else if(key.equals("Y_Axis")) {
                assertThat(testedValue, equalTo("\"Y axis name\""));
            }else if(key.equals("sample_Metric_Name")) {
                assertThat(testedValue, equalTo("\"ulp_lag_ratio\""));
            }else if(key.equals("content_Message")) {
                assertThat(testedValue, equalTo("\"content message\""));
            }
        }
    }
    
    @Test
    public void testCreateTimeStampKeysSelector() {
        keysSelector = new TimeStampKeysSelector();
        keysSelector.setSelectBeginTime(false);
        assertThat(customGraphConsumer.createTimeStampKeysSelector().getGranularity(), equalTo(keysSelector.getGranularity()));
    }
    
    @Test
    public void testCreateGroupInfos() {
        // Testing defaults values
        assertThat(map.containsKey("Generic group"), equalTo(true));
        assertThat(map.containsKey("foo"), equalTo(false));
        assertThat(map.get("Generic group").getAggregatorFactory().getClass(), 
                equalTo(org.apache.jmeter.report.processor.MeanAggregatorFactory.class));
        GroupData groupData = map.get("Generic group").getGroupData();
        assertThat(groupData.getOverallSeries(), equalTo(null));
        assertThat(groupData.getSeriesInfo(), equalTo(new HashMap<String, SeriesData>()));
        
        // Testing native sample variable
        customGraphConsumer.setSampleVariableName("bytes");
        Sample sample = new Sample(0, sampleMetaData, data);
        Double testedValue = map.get("Generic group").getValueSelector().select("bytes", sample);
        assertThat(testedValue, equalTo((Double) 492.0));
        
        // Testing non-native sample variable
        customGraphConsumer.setSampleVariableName("mm-miss");
        testedValue = map.get("Generic group").getValueSelector().select("mm-miss", sample);
        assertThat(testedValue, equalTo(null));
        
        // Testing empty data value, the change between data and data2
        // is on the last value that switchs from "null" to ""
        String[] data2 = {"1527089951383", "0", "Read-compute", "200", "OK", "setupRegion 1-1", "true", "", "492", "0", "1", "1",
                "null", "0", "0", "0", "/stream1a/master.m3u8?e=0&h=56345c61b7b415e0260c19963a153092", "null", "5500000", "null",
                "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", ""};
        sample = new Sample(0, sampleMetaData, data2);
        testedValue = map.get("Generic group").getValueSelector().select("mm-miss", sample);
        assertThat(testedValue, equalTo(null));
    }

    // Test the exception when the column data is not a Double
    @Test(expected=IllegalArgumentException.class)
    public void testCreateGroupInfosExceptions() {
        Sample sample = new Sample(0, sampleMetaData, data);
        customGraphConsumer.setSampleVariableName("label");
        
        // The following line is giving the exception
        map.get("Generic group").getValueSelector().select("label", sample);
    }
    
    @Test
    public void testSelectMetric() {
        Sample sample = new Sample(0, sampleMetaData, data);
        String testString = map.get("Generic group").getSeriesSelector().select(sample).toString();
        assertThat(testString, equalTo("[ulp_lag_ratio]"));
    }
    
    // Create a static SampleMetadatObject
    private SampleMetadata createTestMetaData() {
        String columnsString = "timeStamp,elapsed,label,responseCode,responseMessage,threadName,success,failureMessage,bytes,sentBytes,"
                + "grpThreads,allThreads,URL,Latency,IdleTime,Connect,\"stream\",\"aws_region\",\"bitrate\",\"ulp_buffer_fill\",\"ulp_lag_time\","
                + "\"ulp_play_time\",\"ulp_lag_ratio\",\"lag_ratio_wo_bf\",\"ulp_dwn_time\",\"ulp_hits\",\"ulp_avg_chunk_time\","
                + "\"ulp_avg_manifest_time\",\"mm-hit\",\"mm-miss\",\"cm-hit\",\"cm-miss\",\"ts-hit\",\"ts-miss\"";
        String[] columns = new String[34];
        int lastComa = 0;
        int columnIndex = 0;
        for(int i = 0; i < columnsString.length(); i++) {
            if (columnsString.charAt(i)==',') {
                columns[columnIndex] = columnsString.substring(lastComa, i);
                lastComa=i+1;
                columnIndex++;
            }else if(i+1 == columnsString.length()) {
                columns[columnIndex] = columnsString.substring(lastComa, i+1);
            }
        }
        return new SampleMetadata(',',columns);
    }
    
}
