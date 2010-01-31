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
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.jmeter.samplers.Clearable;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * The model that collects the average of the set of pages to be sampled.
 *
 */

public class GraphAccumModel implements Clearable, Serializable {
    private static final Logger log = LoggingManager.getLoggerForClass();

    private static final long serialVersionUID = 240L;

    private String name;

    private final List<SampleResult> samples;

    private final List<GraphAccumListener> listeners;

    private long max = 1;

    private boolean bigChange = false;

    private SampleResult current;

    /**
     * Constructor.
     */
    public GraphAccumModel() {
        log.debug("Start : GraphAccumModel1");
        listeners = new LinkedList<GraphAccumListener>();
        samples = Collections.synchronizedList(new LinkedList<SampleResult>());
        log.debug("End : GraphAccumModel1");
    }

    /**
     * Sets the Name attribute of the GraphModel object.
     *
     * @param name
     *            the new Name value
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the SampleCount attribute of the GraphAccumModel object.
     *
     * @return the SampleCount value
     */
    public int getSampleCount() {
        return samples.size();
    }

    /**
     * Gets the List attribute of the GraphAccumModel object.
     *
     * @return the List value
     */
    public List<SampleResult> getList() {
        return samples;
    }

    /**
     * Gets the Name attribute of the GraphModel object.
     *
     * @return the Name value
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the Max attribute of the GraphAccumModel object.
     *
     * @return the Max value
     */
    public long getMax() {
        if (log.isDebugEnabled()) {
            log.debug("getMax1 : Returning - " + max);
        }
        return max;
    }

    /**
     * Adds a feature to the ModelListener attribute of the GraphAccumModel
     * object.
     *
     * @param listener
     *            the feature to be added to the GraphAccumListener attribute.
     */
    public void addGraphAccumListener(GraphAccumListener listener) {
        listeners.add(listener);
    }

    /**
     * Clear the results.
     */
    public void clearData() {
        log.debug("Start : clear1");
        samples.clear();
        max = 1;
        bigChange = true;
        this.fireDataChanged();
        log.debug("End : clear1");
    }

    /**
     * Add the new sample to the results.
     *
     * @param res
     *            sample containing the results
     */
    public void addNewSample(SampleResult res) {
        log.debug("Start : addNewSample1");
        // Set time to time taken to load this url without components (e.g.
        // images etc)
        long totalTime = res.getTime();

        if (log.isDebugEnabled()) {
            log.debug("addNewSample1 : time - " + totalTime);
            log.debug("addNewSample1 : max - " + max);
        }
        if (totalTime > max) {
            bigChange = true;
            max = totalTime;
        }
        current = res;
        samples.add(res);
        log.debug("End : addNewSample1");
        fireDataChanged();
    }

    /**
     * Depending on whether the graph needs to be rescale call the appropriate
     * methods.
     */
    protected void fireDataChanged() {
        log.debug("Start : fireDataChanged1");
        Iterator<GraphAccumListener> iter = listeners.iterator();

        if (bigChange) {
            while (iter.hasNext()) {
                iter.next().updateGui();
            }
            bigChange = false;
        } else {
            quickUpdate(current);
        }
        log.debug("End : fireDataChanged1");
    }

    /**
     * The sample to be added did not exceed the current set of samples so do
     * not need to rescale graph.
     */
    protected void quickUpdate(SampleResult s) {
        Iterator<GraphAccumListener> iter = listeners.iterator();
        {
            while (iter.hasNext()) {
                iter.next().updateGui(s);
            }
        }
    }
}
