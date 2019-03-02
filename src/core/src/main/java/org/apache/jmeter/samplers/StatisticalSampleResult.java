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

package org.apache.jmeter.samplers;

import java.io.Serializable;

/**
 * Aggregates sample results for use by the Statistical remote batch mode.
 * Samples are aggregated by the key defined by getKey().
 * TODO: merge error count into parent class?
 */
public class StatisticalSampleResult extends SampleResult implements
        Serializable {

    private static final long serialVersionUID = 240L;

    private int errorCount;

    // Need to maintain our own elapsed timer to ensure more accurate aggregation
    private long elapsed;

    public StatisticalSampleResult(){// May be called by XStream
    }

    /**
     * Allow CsvSaveService to generate a suitable result when sample/error counts have been saved.
     *
     * @deprecated Needs to be replaced when multiple sample results are sorted out
     *
     * @param stamp this may be a start time or an end time (both in milliseconds)
     * @param elapsed time in milliseconds
     */
    @Deprecated
    public StatisticalSampleResult(long stamp, long elapsed) {
        super(stamp, elapsed);
        this.elapsed = elapsed;
    }

    /**
     * Create a statistical sample result from an ordinary sample result.
     * 
     * @param res the sample result 
     */
    public StatisticalSampleResult(SampleResult res) {
        // Copy data that is shared between samples (i.e. the key items):
        setSampleLabel(res.getSampleLabel());
        
        setThreadName(res.getThreadName());

        setSuccessful(true); // Assume result is OK
        setSampleCount(0); // because we add the sample count in later
        elapsed = 0;
    }

    public void add(SampleResult res) {
        // Add Sample Counter
        setSampleCount(getSampleCount() + res.getSampleCount());

        setBytes(getBytesAsLong() + res.getBytesAsLong());
        setSentBytes(getSentBytes() + res.getSentBytes());

        // Add Error Counter
        if (!res.isSuccessful()) {
            errorCount++;
            this.setSuccessful(false);
        }

        // Set start/end times
        if (getStartTime()==0){ // Bug 40954 - ensure start time gets started!
            this.setStartTime(res.getStartTime());
        } else {
            this.setStartTime(Math.min(getStartTime(), res.getStartTime()));
        }
        this.setEndTime(Math.max(getEndTime(), res.getEndTime()));

        setLatency(getLatency()+ res.getLatency());
        setConnectTime(getConnectTime()+ res.getConnectTime());

        elapsed += res.getTime();
    }

    @Override
    public long getTime() {
        return elapsed;
    }

    @Override
    public long getTimeStamp() {
        return getEndTime();
    }

    @Override
    public int getErrorCount() {// Overrides SampleResult
        return errorCount;
    }

    @Override
    public void setErrorCount(int e) {// for reading CSV files
        errorCount = e;
    }

    /**
     * Generates the key to be used for aggregating samples as follows:<br>
     * <code>sampleLabel</code> "-" <code>[threadName|threadGroup]</code>
     * <p>
     * N.B. the key should agree with the fixed items that are saved in the sample.
     *
     * @param event sample event whose key is to be calculated
     * @param keyOnThreadName true if key should use thread name, otherwise use thread group
     * @return the key to use for aggregating samples
     */
    public static String getKey(SampleEvent event, boolean keyOnThreadName) {
        StringBuilder sb = new StringBuilder(80);
        sb.append(event.getResult().getSampleLabel());
        if (keyOnThreadName){
            sb.append('-').append(event.getResult().getThreadName());
        } else {
            sb.append('-').append(event.getThreadGroup());
        }
        return sb.toString();
    }
}
