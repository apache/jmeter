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

package org.apache.jmeter.visualizers;

import java.io.Serializable;
import java.text.Format;
import java.util.Date;

/**
 * Class to hold data for the TableVisualiser.
 */
public class TableSample implements Serializable, Comparable<TableSample> {
    private static final long serialVersionUID = 240L;

    private final long totalSamples;
    
    private final int sampleCount; // number of samples in this entry

    private final long startTime;

    private final String threadName;
    
    private final String label;

    private final long elapsed;

    private final boolean success;

    private final long bytes;

    /**
     * @deprecated for unit test code only
     */
    @Deprecated
    public TableSample() {
        this(0, 0, 0, "", "", 0, true, 0);
    }

    public TableSample(long totalSamples, int sampleCount, long startTime, String threadName,
            String label,
            long elapsed, boolean success, long bytes) {
        this.totalSamples = totalSamples;
        this.sampleCount = sampleCount;
        this.startTime = startTime;
        this.threadName = threadName;
        this.label = label;
        this.elapsed = elapsed/sampleCount;
        this.success = success;
        this.bytes = bytes/sampleCount;
    }

    // The following getters may appear not to be used - however they are invoked via the Functor class

    public long getBytes() {
        return bytes;
    }

    public String getSampleNumberString(){
        StringBuilder sb = new StringBuilder();
        if (sampleCount > 1) {
        sb.append(totalSamples-sampleCount+1);
        sb.append('-');
        }
        sb.append(totalSamples);
        return sb.toString();
    }

    public long getElapsed() {
        return elapsed;
    }

    public boolean isSuccess() {
        return success;
    }

    public long getStartTime() {
        return startTime;
    }

    /**
     * @return the start time using the specified format
     * Intended for use from Functors
     */
    public String getStartTimeFormatted(Format format) {
        return format.format(new Date(getStartTime()));
    }

    public String getThreadName() {
        return threadName;
    }

    public String getLabel() {
        return label;
    }

    public int compareTo(TableSample o) {
        TableSample oo = o;
        return ((totalSamples - oo.totalSamples) < 0 ? -1 : (totalSamples == oo.totalSamples ? 0 : 1));
    }

    // TODO should equals and hashCode depend on field other than count?
    
    @Override
    public boolean equals(Object o){
        return (
                (o instanceof TableSample) &&
                (this.compareTo((TableSample) o) == 0)
                );
    }

    @Override
    public int hashCode(){
        return (int)(totalSamples ^ (totalSamples >>> 32));
    }

}
