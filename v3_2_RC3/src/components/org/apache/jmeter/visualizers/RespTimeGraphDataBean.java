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
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 * 
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jmeter.visualizers;

/**
 * Bean to hold timing information about samples
 *
 */
public class RespTimeGraphDataBean {

    private long startTime;
    
    private long time;
    
    private String samplerLabel;

    /**
     * Constructor
     * 
     * @param startTime
     *            The start time of this Sample
     * @param time
     *            The time elapsed for this sample
     * @param samplerLabel
     *            The label for this sample
     */
    public RespTimeGraphDataBean(long startTime, long time, String samplerLabel) {
        super();
        this.startTime = startTime;
        this.time = time;
        this.samplerLabel = samplerLabel;
    }

    /**
     * @return the startTime
     */
    public long getStartTime() {
        return startTime;
    }

    /**
     * @param startTime the startTime to set
     */
    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    /**
     * @return the time
     */
    public long getTime() {
        return time;
    }

    /**
     * @param time the time to set
     */
    public void setTime(long time) {
        this.time = time;
    }

    /**
     * @return the samplerLabel
     */
    public String getSamplerLabel() {
        return samplerLabel;
    }

    /**
     * @param samplerLabel the samplerLabel to set
     */
    public void setSamplerLabel(String samplerLabel) {
        this.samplerLabel = samplerLabel;
    }
    
    
}
