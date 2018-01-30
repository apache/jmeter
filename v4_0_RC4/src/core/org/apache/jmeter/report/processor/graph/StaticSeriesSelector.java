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

import java.util.Arrays;

import org.apache.jmeter.report.core.Sample;

/**
 * The class StaticSeriesSelector provides always the same series name.
 * 
 * @since 3.0
 */
public class StaticSeriesSelector extends AbstractSeriesSelector {

    private String seriesName;

    /**
     * Gets the name of the series.
     *
     * @return the name of the series
     */
    public final String getSeriesName() {
        return seriesName;
    }

    /**
     * Sets the name of the series.
     *
     * @param seriesName
     *            the name of the series to set
     */
    public final void setSeriesName(String seriesName) {
        this.seriesName = seriesName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.apache.jmeter.report.csv.processor.SampleSelector#select(org.apache
     * .jmeter.report.csv.core.Sample)
     */
    @Override
    public Iterable<String> select(Sample sample) {
        return Arrays.asList(seriesName);
    }

}
