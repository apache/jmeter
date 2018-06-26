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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.report.core.ConvertException;
import org.apache.jmeter.report.core.Converters;
import org.apache.jmeter.report.core.Sample;
import org.apache.jmeter.report.processor.MapResultData;
import org.apache.jmeter.report.processor.SampleConsumer;
import org.apache.jmeter.report.processor.TimeRateAggregatorFactory;
import org.apache.jmeter.report.processor.ValueResultData;
import org.apache.jmeter.report.processor.graph.AbstractGraphConsumer;
import org.apache.jmeter.report.processor.graph.AbstractOverTimeGraphConsumer;
import org.apache.jmeter.report.processor.graph.AbstractSeriesSelector;
import org.apache.jmeter.report.processor.graph.GraphValueSelector;
import org.apache.jmeter.report.processor.graph.GroupInfo;
import org.apache.jmeter.report.processor.graph.TimeStampKeysSelector;

/**
 * The class CustomGraphConsumer is added by the custom Graphs plugin.
 * It provides all the graphs the user defined in user.properties.
 *
 * @since 4.1
 */

public class CustomGraphConsumer extends AbstractOverTimeGraphConsumer implements SampleConsumer{
    
    public static final String RESULT_Y_AXIS = "Y_Axis"; //$NON-NLS-1$
    public static final String RESULT_X_AXIS = "X_Axis"; //$NON-NLS-1$
    public static final String RESULT_SAMPLE_VARIABLE_NAME = "sample_Metric_Name"; //$NON-NLS-1$
    public static final String RESULT_CONTENT_MESSAGE = "content_Message"; //$NON-NLS-1$
    public static final String REPORT_GENERATOR_PROPERTIES = "jmeter.reportgenerator.graph.customGraph.property"; //$NON-NLS-1$

    private String yAxis;
    private String xAxis;
    private String contentMessage;
    private String sampleVariableName;
    private boolean isNativeSampleVariableName = false;
    
    /**
     * Only used for junit tests.
     * Indicates if the sampleVariableName 
     * is native
     * 
     * @return the nativeSampleVariableName
     */
    public boolean getIsNativeSampleVariableName() {
        return isNativeSampleVariableName;
    }

    /**
     * Gets the Y Axis.
     *
     * @return the yAxis
     */
    public String getYAxis() {
        return yAxis;
    }
    
    /**
     * Gets the X Axis.
     *
     * @return the xAxis
     */
    public String getXAxis() {
        return xAxis;
    }

    /**
     * Sets the yAxis.
     *
     * @param axis
     * the yAxis to set
     */
    public void setYAxis(String axis) {
        yAxis=axis;
    }
    
    /**
     * Sets the xAxis.
     *
     * @param axis
     * the xAxis to set
     */
    public void setXAxis(String axis) {
        xAxis=axis;
    }
    
    /**
     * Sets the contentMessage.
     *
     * @param message
     * the message to set
     */
    public void setContentMessage(String message) {
        contentMessage=message;
    }
    
    /**
     * Gets the content message.
     *
     * @return the contentMessage
     */
    public String getContentMessage() {
        return contentMessage;
    }
    
    /**
     * Gets the sampleVariableName.
     *
     * @return the sampleVariableName
     */
    public String getSampleVariableName() {
        return sampleVariableName;
    }
    
    /**
     * Sets the sampleVariableName.
     * Sets the boolean isNativesSampleVariableName
     *
     * @param sampleVarName
     * the sampleVariableName to set
     */
    public void setSampleVariableName(String sampleVarName) {
        sampleVariableName = sampleVarName;
        // this if contains every native sample variables names
        if(sampleVarName.equals("timeStamp") || sampleVarName.equals("elapsed") 
                || sampleVarName.equals("label") || sampleVarName.equals("responseCode") 
                || sampleVarName.equals("threadName") || sampleVarName.equals("success") 
                || sampleVarName.equals("failureMessage") || sampleVarName.equals("bytes") 
                || sampleVarName.equals("sentBytes") || sampleVarName.equals("grpThreads") 
                || sampleVarName.equals("allThreads") || sampleVarName.equals("URL") 
                || sampleVarName.equals("Latency") || sampleVarName.equals("IdleTime") 
                || sampleVarName.equals("Connect")) {
            isNativeSampleVariableName = true;
        }else {
            isNativeSampleVariableName = false;
        }
    }
    
        
    @Override
    protected void initializeExtraResults(MapResultData parentResult) {
        parentResult.setResult(RESULT_CTX_GRANULARITY, new ValueResultData(Long.valueOf(getGranularity())));
        parentResult.setResult(RESULT_Y_AXIS, new ValueResultData(getYAxis()));
        parentResult.setResult(RESULT_X_AXIS, new ValueResultData(getXAxis()));
        parentResult.setResult(RESULT_SAMPLE_VARIABLE_NAME, new ValueResultData(getSampleVariableName()));
        parentResult.setResult(RESULT_CONTENT_MESSAGE, new ValueResultData(getContentMessage()));
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * org.apache.jmeter.report.csv.processor.impl.AbstractOverTimeGraphConsumer
     * #createTimeStampKeysSelector()
     */
    @Override
    protected TimeStampKeysSelector createTimeStampKeysSelector() {
        TimeStampKeysSelector keysSelector = new TimeStampKeysSelector();
        keysSelector.setSelectBeginTime(false);
        return keysSelector;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.jmeter.report.csv.processor.impl.AbstractGraphConsumer#
     * createGroupInfos()
     */
    @Override
    protected Map<String, GroupInfo> createGroupInfos() {
        
        HashMap<String, GroupInfo> groupInfos = new HashMap<>(); 
        groupInfos.put(AbstractGraphConsumer.DEFAULT_GROUP,
                new GroupInfo(
                new TimeRateAggregatorFactory(), 
                new AbstractSeriesSelector() {
                    private final Iterable<String> values = Arrays.asList(sampleVariableName);

                  @Override
                  public Iterable<String> select(Sample sample) {
                      return values;
                  }
                },
                // We ignore Transaction Controller results
                new GraphValueSelector() {
                  @Override
                  public Double select(String series, Sample sample) {
                      String value="";
                      if(isNativeSampleVariableName) {
                          value = sample.getData(sampleVariableName);
                      }else {
                          value = sample.getData("\""+sampleVariableName+"\"");
                      }
                      if(StringUtils.isEmpty(value) || value.equals("null")) {
                          return null;
                      }
                      else {
                          try {
                            return Converters.convert(Double.class, value);
                        } catch (ConvertException e) {
                            throw new IllegalArgumentException("Double converter failed : {}",e);
                        }
                      }
              }}, false, false));
        return groupInfos;
    }
    
    @Override
    public void initialize() {
        super.initialize();
        // Override the granularity of the aggregators factory
        ((TimeRateAggregatorFactory) getGroupInfos().get(
                AbstractGraphConsumer.DEFAULT_GROUP).getAggregatorFactory())
                .setGranularity(getGranularity());
    }
}
