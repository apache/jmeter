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

import org.apache.jmeter.monitor.util.Stats;
import org.apache.jmeter.testelement.AbstractTestElement;

/*
 *  TODO - convert this into an immutable class using plain variables
 *  The current implementation is quite inefficient, as it stores everything
 *  in properties.
 *
 *  This will require changes to ResultCollector.recordStats()
 *  and SaveService.saveTestElement() which are both currently only used by Monitor classes
 */
public class MonitorStats extends AbstractTestElement implements Serializable {

    private static final long serialVersionUID = 240L;

    private static final String HEALTH = "stats.health";

    private static final String LOAD = "stats.load";

    private static final String CPULOAD = "stats.cpuload";

    private static final String MEMLOAD = "stats.memload";

    private static final String THREADLOAD = "stats.threadload";

    private static final String HOST = "stats.host";

    private static final String PORT = "stats.port";

    private static final String PROTOCOL = "stats.protocol";

    private static final String TIMESTAMP = "stats.timestamp";

    /**
     *
     */
    public MonitorStats() {
        super();
    }

    /**
     * Default constructor
     *
     * @param health
     *            Health of the server. Has to be one of {@link Stats#HEALTHY
     *            HEALTHY}, {@link Stats#ACTIVE ACTIVE}, {@link Stats#WARNING
     *            WARNING} or {@link Stats#DEAD DEAD}
     * @param load
     *            load of the server as integer from a range in between 1 and
     *            100
     * @param cpuload
     *            cpu load of the server as integer from range between 1 and 100
     * @param memload
     *            load of the server as integer from a range in between 1 and
     *            100
     * @param threadload
     *            thread load of the server as an integer from a range in
     *            between 1 and 100
     * @param host
     *            name of the host from which the status was taken
     * @param port
     *            port from which the status was taken
     * @param protocol
     *            over which the status was taken
     * @param time
     *            time in milliseconds when this status was created
     */
    public MonitorStats(int health, int load, int cpuload, int memload, int threadload, String host, String port,
            String protocol, long time) {
        this.setHealth(health);
        this.setLoad(load);
        this.setCpuLoad(cpuload);
        this.setMemLoad(memload);
        this.setThreadLoad(threadload);
        this.setHost(host);
        this.setPort(port);
        this.setProtocol(protocol);
        this.setTimeStamp(time);
    }

    /**
     * For convienance, this method returns the protocol, host and port as a
     * URL.
     *
     * @return protocol://host:port
     */
    public String getURL() {
        return this.getProtocol() + "://" + this.getHost() + ":" + this.getPort();
    }

    public void setHealth(int health) {
        this.setProperty(HEALTH, String.valueOf(health));
    }

    public void setLoad(int load) {
        this.setProperty(LOAD, String.valueOf(load));
    }

    public void setCpuLoad(int load) {
        this.setProperty(CPULOAD, String.valueOf(load));
    }

    public void setMemLoad(int load) {
        this.setProperty(MEMLOAD, String.valueOf(load));
    }

    public void setThreadLoad(int load) {
        this.setProperty(THREADLOAD, String.valueOf(load));
    }

    public void setHost(String host) {
        this.setProperty(HOST, host);
    }

    public void setPort(String port) {
        this.setProperty(PORT, port);
    }

    public void setProtocol(String protocol) {
        this.setProperty(PROTOCOL, protocol);
    }

    public void setTimeStamp(long time) {
        this.setProperty(TIMESTAMP, String.valueOf(time));
    }

    public int getHealth() {
        return this.getPropertyAsInt(HEALTH);
    }

    public int getLoad() {
        return this.getPropertyAsInt(LOAD);
    }

    public int getCpuLoad() {
        return this.getPropertyAsInt(CPULOAD);
    }

    public int getMemLoad() {
        return this.getPropertyAsInt(MEMLOAD);
    }

    public int getThreadLoad() {
        return this.getPropertyAsInt(THREADLOAD);
    }

    public String getHost() {
        return this.getPropertyAsString(HOST);
    }

    public String getPort() {
        return this.getPropertyAsString(PORT);
    }

    public String getProtocol() {
        return this.getPropertyAsString(PROTOCOL);
    }

    public long getTimeStamp() {
        return this.getPropertyAsLong(TIMESTAMP);
    }
}
