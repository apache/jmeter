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
 */
package org.apache.jmeter.visualizers;

import java.io.Serializable;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.jmeter.monitor.model.ObjectFactory;
import org.apache.jmeter.monitor.model.Status;
import org.apache.jmeter.monitor.util.Stats;
import org.apache.jmeter.protocol.http.sampler.HTTPSampleResult;
import org.apache.jmeter.samplers.Clearable;
import org.apache.jmeter.samplers.SampleResult;

public class MonitorAccumModel implements Clearable, Serializable {

    private static final long serialVersionUID = 240L;

    private final Map<String, List<MonitorModel>> serverListMap;

    /**
     * we use this to set the current monitorModel so that we can save the stats
     * to the resultcolllector.
     */
    private MonitorModel current;

    private final List<MonitorListener> listeners;

    /**
     * By default, we set the default to 800
     */
    private int defaultBufferSize = 800;

    // optional connector name prefix
    private String connectorPrefix = null;

    /**
     *
     */
    public MonitorAccumModel() {
        serverListMap = new HashMap<String, List<MonitorModel>>();
        listeners = new LinkedList<MonitorListener>();
    }

    public int getBufferSize() {
        return defaultBufferSize;
    }

    public void setBufferSize(int buffer) {
        defaultBufferSize = buffer;
    }

    public void setPrefix(String prefix) {
        connectorPrefix = prefix;
    }

    /**
     * Added this method we that we can save the calculated stats.
     *
     * @return current sample
     */
    public MonitorModel getLastSample() {
        return this.current;
    }

    /**
     * Method will look up the server in the map. The MonitorModel will be added
     * to an existing list, or a new one will be created.
     *
     * @param model the {@link MonitorModel} to be added
     */
    public void addSample(MonitorModel model) {
        this.current = model;
        if (serverListMap.containsKey(model.getURL())) {
            List<MonitorModel> newlist = updateArray(model, serverListMap.get(model.getURL()));
            serverListMap.put(model.getURL(), newlist);
        } else {
            List<MonitorModel> samples = Collections.synchronizedList(new LinkedList<MonitorModel>());
            samples.add(model);
            serverListMap.put(model.getURL(), samples);
        }
    }

    /**
     * We want to keep only 240 entries for each server, so we handle the object
     * array ourselves.
     *
     * @param model
     */
    private List<MonitorModel> updateArray(MonitorModel model, List<MonitorModel> list) {
        if (list.size() < defaultBufferSize) {
            list.add(model);
        } else {
            list.add(model);
            list.remove(0);
        }
        return list;
    }

    /**
     * Get all MonitorModels matching the URL.
     *
     * @param url to be matched against
     * @return list
     */
    public List<MonitorModel> getAllSamples(String url) {
        if (!serverListMap.containsKey(url)) {
            return Collections.synchronizedList(new LinkedList<MonitorModel>());
        } else {
            return serverListMap.get(url);
        }
    }

    /**
     * Get the MonitorModel matching the url.
     *
     * @param url
     *            to be matched against
     * @return the first {@link MonitorModel} registered for this
     *         <code>url</code>
     */
    public MonitorModel getSample(String url) {
        if (serverListMap.containsKey(url)) {
            return serverListMap.get(url).get(0);
        } else {
            return null;
        }
    }

    /**
     * Method will try to parse the response data. If the request was a monitor
     * request, but the response was incomplete, bad or the server refused the
     * connection, we will set the server's health to "dead". If the request was
     * not a monitor sample, the method will ignore it.
     *
     * @param sample
     *            {@link SampleResult} with the result of the status request
     */
    public void addSample(SampleResult sample) {
        URL surl = null;
        if (sample instanceof HTTPSampleResult) {
            surl = ((HTTPSampleResult) sample).getURL();
            // String rescontent = new String(sample.getResponseData());
            if (sample.isResponseCodeOK() && ((HTTPSampleResult) sample).isMonitor()) {
                ObjectFactory of = ObjectFactory.getInstance();
                Status st = of.parseBytes(sample.getResponseData());
                st.setConnectorPrefix(connectorPrefix);
                if (surl != null) {// surl can be null if read from a file
                    MonitorStats stat = new MonitorStats(Stats.calculateStatus(st), Stats.calculateLoad(st), 0, Stats
                            .calculateMemoryLoad(st), Stats.calculateThreadLoad(st), surl.getHost(), String.valueOf(surl
                            .getPort()), surl.getProtocol(), System.currentTimeMillis());
                    MonitorModel mo = new MonitorModel(stat);
                    this.addSample(mo);
                    notifyListeners(mo);
                } 
                // This part of code throws NullPointerException
                // Don't think Monitor results can be loaded from files
                // see https://issues.apache.org/bugzilla/show_bug.cgi?id=51810
//                else {
//                    noResponse(surl);
//                }
            } else if (((HTTPSampleResult) sample).isMonitor()) {
                noResponse(surl);
            }
        }
    }

    /**
     * If there is no response from the server, we create a new MonitorStats
     * object with the current timestamp and health "dead".
     *
     * @param url
     *            URL from where the status should have come
     */
    public void noResponse(URL url) {
        notifyListeners(createNewMonitorModel(url));
    }

    /**
     * Method will return a new MonitorModel object with the given URL. This is
     * used when the server fails to respond fully, or is dead.
     *
     * @param url
     *            URL from where the status should have come
     * @return new MonitorModel
     */
    public MonitorModel createNewMonitorModel(URL url) {
        MonitorStats stat = new MonitorStats(Stats.DEAD, 0, 0, 0, 0, url.getHost(), String.valueOf(url.getPort()), url
                .getProtocol(), System.currentTimeMillis());
        return new MonitorModel(stat);
    }

    /**
     * Clears everything except the listener. Do not clear the listeners. If we
     * clear listeners, subsequent "run" will not notify the gui of data
     * changes.
     */
    @Override
    public void clearData() {
        for (List<MonitorModel> modelList : this.serverListMap.values()) {
            modelList.clear();
        }
        this.serverListMap.clear();
    }

    /**
     * notify the listeners with the MonitorModel object.
     *
     * @param model
     *            the {@link MonitorModel} that should be sent to the listeners
     */
    public void notifyListeners(MonitorModel model) {
        for (int idx = 0; idx < listeners.size(); idx++) {
            MonitorListener ml = listeners.get(idx);
            ml.addSample(model);
        }
    }

    /**
     * Add a listener. When samples are added, the class will notify the
     * listener of the change.
     *
     * @param listener
     *            the {@link MonitorListener} that should be added
     */
    public void addListener(MonitorListener listener) {
        listeners.add(listener);
    }
}
