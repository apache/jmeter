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
package org.apache.jmeter.report.processor.graph;

import org.apache.jmeter.report.core.Sample;

/**
 * The class TimeStampKeysSelector provides a projection from a sample to its
 * begin or end time.
 *
 * @since 3.0
 */
public class TimeStampKeysSelector implements GraphKeysSelector {

    private long granularity = 1;
    private boolean selectBeginTime = false;

    /**
     * Gets the granularity.
     *
     * @return the granularity
     */
    public long getGranularity() {
        return granularity;
    }

    /**
     * Sets the granularity.
     *
     * @param granularity
     *            the granularity to set
     */
    public void setGranularity(long granularity) {
        this.granularity = granularity;
    }

    /**
     * Gets a status defining whether the projection is done with the begin or
     * end time of the sample.
     *
     * @return true if the begin time is used; false otherwise.
     */
    public final boolean selectsBeginTime() {
        return selectBeginTime;
    }

    /**
     * Sets the status defining whether the projection is done with the begin or
     * end time of the sample.
     *
     * @param selectBeginTime
     *            the status to set
     */
    public final void setSelectBeginTime(boolean selectBeginTime) {
        this.selectBeginTime = selectBeginTime;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.apache.jmeter.report.core.SampleSelector#select(org.apache.jmeter
     * .report.core.Sample)
     */
    @Override
    public Double select(Sample sample) {
        long time = selectBeginTime ? sample.getStartTime() : sample.getEndTime();
        return Double.valueOf((double) time - time % granularity);
    }

}
