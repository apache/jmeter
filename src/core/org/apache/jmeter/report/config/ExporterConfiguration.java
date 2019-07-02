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
package org.apache.jmeter.report.config;

import java.util.HashMap;
import java.util.Map;

/**
 * The class ExporterConfiguration describes the configuration of an exporter.
 *
 * @since 3.0
 */
public class ExporterConfiguration extends InstanceConfiguration {

    private final HashMap<String, SubConfiguration> graphExtraConfigurations = new HashMap<>();

    private boolean filtersOnlySampleSeries;
    private String seriesFilter;
    private boolean showControllerSeriesOnly;

    /**
     * Gets the extra configurations for graphs.
     *
     * @return the extra configurations for graphs
     */
    public Map<String, SubConfiguration> getGraphExtraConfigurations(){
        return graphExtraConfigurations;
    }

    /**
     * Indicates whether series filter apply only on sample series
     *
     * @return true if series filter apply only on sample series; false otherwise
     */
    public final boolean filtersOnlySampleSeries() {
        return filtersOnlySampleSeries;
    }

    /**
     * Sets the series filters apply only on sample series.
     *
     * @param filtersOnlySampleSeries true if series filter apply only on sample series; false otherwise
     */
    public final void filtersOnlySampleSeries(boolean filtersOnlySampleSeries) {
        this.filtersOnlySampleSeries = filtersOnlySampleSeries;
    }

    /**
     * Gets the series filter.
     *
     * @return the series filter
     */
    public final String getSeriesFilter() {
        return seriesFilter;
    }

    /**
     * Sets the series filter.
     *
     * @param seriesFilter the series filter to set
     */
    public final void setSeriesFilter(String seriesFilter) {
        this.seriesFilter = seriesFilter;
    }

    /**
     * Indicates whether only controller series are shown.
     *
     * @return true if only controller series are shown; false otherwise
     */
    public final boolean showControllerSeriesOnly() {
        return showControllerSeriesOnly;
    }

    /**
     * Sets the flag defining whether only controllers series are shown
     *
     * @param showControllerSeriesOnly indicates whether only controllers series are shown
     */
    public final void showControllerSeriesOnly(boolean showControllerSeriesOnly) {
        this.showControllerSeriesOnly = showControllerSeriesOnly;
    }
}
