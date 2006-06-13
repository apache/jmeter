//$Header$
/*
 * Copyright 2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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
package org.apache.jmeter.report;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.jmeter.visualizers.SamplingStatCalculator;
import org.apache.jmeter.visualizers.Visualizer;

/**
 * @author Peter Lin
 *
 * DataSet extends Visualizer so that it can be used with ResultCollector.
 * Classes implementing the interface should create a new instance of
 * ResultCollector and call setListener(Visualizer) passing itself.
 * When the ResultCollector.loadExistingFile is called, it will pass
 * the SampleResults.
 */
public interface DataSet extends Visualizer {

    /**
     * Depending on the implementation, the datasouce could be a file
     * or a RDBMS. It's up to the implementing class to decide.
     * @param datasource
     */
    public void setDataSource(String datasource);
    /**
     * Return the datasource. For files, it should be the absolute path.
     * For databases, it should be the datasource name created in jmeter.
     * @return
     */
    public String getDataSource();
    /**
     * In some cases, we may want to return a string that isn't the full
     * datasource string or something different. For example, we may
     * want to return just the filename and not the absolutePath of
     * a JTL file.
     * @return
     */
    public String getDataSourceName();
    /**
     * Set the timestamp using the first result from the datasource
     * @param stamp
     */
    public void setStartTimestamp(long stamp);
    /**
     * return the timestamp in millisecond format.
     * @return
     */
    public long getStartTimestamp();
    /**
     * Set the timestamp using the last result from the datasource
     * @param stamp
     */
    public void setEndTimestamp(long stamp);
    /**
     * return the timestamp in millisecond format.
     * @return
     */
    public long getEndTimestamp();
    /**
     * Return the Date object using the start timestamp
     * @return
     */
    public Date getDate();
    /**
     * convienance method for getting the date in mmdd format
     * @return
     */
    public String getMonthDayDate();
    /**
     * convienant method for getting the date in yyyymmdd format
     * @return
     */
    public String getMonthDayYearDate();
    /**
     * Classes implementing the method should return the URL's in the 
     * DataSet. It is up to the class to return Strings or URL.
     * @return
     */
    public Set getURLs();
    /**
     * Classes implementing the method should return instance of 
     * SamplingStatCalculator.
     * @return
     */
    public Set getStats();
    /**
     * Return the SamplingStatCalculate for a specific URL.
     * @param url
     * @return
     */
    public SamplingStatCalculator getStatistics(String url);
    /**
     * Convienance method for getting all the SamplingStatCalculator for
     * a given URL.
     * @param urls
     * @return
     */
    public List getStats(List urls);
    /**
     * Classes implementing the method should load the data from
     * the target location. It doesn't necessarily have to be a
     * file. It could be from a database.
     */
    public void loadData();
}
