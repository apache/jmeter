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
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.jmeter.samplers.Clearable;

public class MonitorModel implements Clearable, Serializable, Cloneable {

    private static final long serialVersionUID = 240L;

    private MonitorStats current = new MonitorStats(0, 0, 0, 0, 0, "", "", "", System.currentTimeMillis());

    /**
     *
     */
    public MonitorModel() {
        super();
    }

    public MonitorModel(MonitorStats stat) {
        this.current = stat;
    }

    public int getHealth() {
        return this.current.getHealth();
    }

    public int getLoad() {
        return this.current.getLoad();
    }

    public int getCpuload() {
        return this.current.getCpuLoad();
    }

    public int getMemload() {
        return this.current.getMemLoad();
    }

    public int getThreadload() {
        return this.current.getThreadLoad();
    }

    public String getHost() {
        return this.current.getHost();
    }

    public String getPort() {
        return this.current.getPort();
    }

    public String getProtocol() {
        return this.current.getProtocol();
    }

    public long getTimestamp() {
        return this.current.getTimeStamp();
    }

    public String getURL() {
        return this.current.getURL();
    }

    /**
     * Method will return a formatted date using SimpleDateFormat.
     *
     * @return String
     */
    public String getTimestampString() {
        Date date = new Date(this.current.getTimeStamp());
        SimpleDateFormat ft = new SimpleDateFormat();
        return ft.format(date);
    }

    /**
     * Method is used by DefaultMutableTreeNode to get the label for the node.
     */
    @Override
    public String toString() {
        return getURL();
    }

    /**
     * clear will create a new MonitorStats object.
     */
    public void clearData() {
        current = new MonitorStats(0, 0, 0, 0, 0, "", "", "", System.currentTimeMillis());
    }

    /**
     * a clone method is provided for convienance. In some cases, it may be
     * desirable to clone the object.
     */
    @Override
    public Object clone() {
        return new MonitorModel(cloneMonitorStats());
    }

    /**
     * a clone method to clone the stats
     *
     * @return new instance of the class
     */
    public MonitorStats cloneMonitorStats() {
        return new MonitorStats(current.getHealth(), current.getLoad(), current.getCpuLoad(), current.getMemLoad(),
                current.getThreadLoad(), current.getHost(), current.getPort(), current.getProtocol(), current
                        .getTimeStamp());
    }
}
