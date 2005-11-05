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

    public void setPath(String absolutePath);
    public String getPath();
    public void setStartTimestamp(long stamp);
    public long getStartTimestamp();
    public void setEndTimestamp(long stamp);
    public long getEndTimestamp();
    public Date getDate();
    public Set getURLs();
    public Set getStats();
    /**
     * Return the SamplingStatCalculate for a specific URL.
     * @param url
     * @return
     */
    public SamplingStatCalculator getStatistics(String url);
    /**
     * Classes implementing the method should load the data from
     * the target location. It doesn't necessarily have to be a
     * file. It could be from a database.
     */
    public void loadData();
}
