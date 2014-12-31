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
package org.apache.jmeter.monitor.util;

import org.apache.jmeter.monitor.model.Connector;
import org.apache.jmeter.monitor.model.Status;

/**
 *
 * Description:
 * <p>
 * Stats is responsible for calculating the load and health of a given server.
 * It uses tomcat's status servlet results. A schema was generated for the XML
 * output and JAXB was used to generate classes.
 * <p>
 * The equations are:
 * <p>
 * memory weight = (int)(50 * (free/max))<br>
 * thread weight = (int)(50 * (current/max))
 * <p>
 * The load factors are stored in the properties files. Simply change the values
 * in the properties to change how load is calculated. The defaults values are
 * memory (50) and threads (50). The sum of the factors must equal 100.
 */
public class Stats {

    public static final int DEAD = 0;

    public static final int ACTIVE = 2;

    public static final int WARNING = 1;

    public static final int HEALTHY = 3;

    public static final int DEFAULT_MEMORY_FACTOR = 50;

    public static final int DEFAULT_THREAD_FACTOR = 50;

    public static final double HEALTHY_PER = 0.00;

    public static final double ACTIVE_PER = 0.25;

    public static final double WARNING_PER = 0.67;

    /**
     * The method is responsible for taking a status object and calculating an
     * int value from 1 to 100. We use a combination of free memory and free
     * threads. The current factor is 50/50.
     * <p>
     *
     * @param stat status information about the server
     * @return calculated load value
     */
    public static int calculateLoad(Status stat) {
        if (stat != null) {
            // equation for calculating the weight
            // w = (int)(33 * (used/max))
            long totMem = stat.getJvm().getMemory().getTotal();
            long freeMem = stat.getJvm().getMemory().getFree();
            long usedMem = totMem - freeMem;
            double memdiv = (double) usedMem / (double) totMem;
            double memWeight = DEFAULT_MEMORY_FACTOR * memdiv;

            // changed the logic for BEA Weblogic in the case a
            // user uses Tomcat's status servlet without any
            // modifications. Weblogic will return nothing for
            // the connector, therefore we need to check the size
            // of the list. Peter 12.22.04
            double threadWeight = 0;
            if (stat.getConnector().size() > 0) {
                Connector cntr = fetchConnector(stat);
                int maxThread = cntr.getThreadInfo().getMaxThreads();
                int curThread = cntr.getThreadInfo().getCurrentThreadsBusy();
                double thdiv = (double) curThread / (double) maxThread;
                threadWeight = DEFAULT_THREAD_FACTOR * thdiv;
            }
            return (int) (memWeight + threadWeight);
        } else {
            return 0;
        }
    }

    /**
     * Method should calculate if the server is: dead, active, warning or
     * healthy. We do this by looking at the current busy threads.
     * <ol>
     * <li>free &gt; spare is {@link Stats#HEALTHY healthy}</li>
     * <li>free &lt; spare is {@link Stats#ACTIVE active}</li>
     * <li>busy threads &gt; 75% is {@link Stats#WARNING warning}</li>
     * <li>none of the above is {@link Stats#DEAD dead}</li>
     * </ol>
     *
     * @param stat status information about the server
     * @return integer representing the status (one of {@link Stats#HEALTHY
     *         HEALTHY}, {@link Stats#ACTIVE ACTIVE}, {@link Stats#WARNING
     *         WARNING} or {@link Stats#DEAD DEAD})
     */
    public static int calculateStatus(Status stat) {
        if (stat != null && stat.getConnector().size() > 0) {
            Connector cntr = fetchConnector(stat);
            int max = cntr.getThreadInfo().getMaxThreads();
            int current = cntr.getThreadInfo().getCurrentThreadsBusy();
            // int spare = cntr.getThreadInfo().getMaxSpareThreads();
            double per = (double) current / (double) max;
            if (per > WARNING_PER) {
                return WARNING;
            } else if (per >= ACTIVE_PER && per <= WARNING_PER) {
                return ACTIVE;
            } else if (per < ACTIVE_PER && per >= HEALTHY_PER) {
                return HEALTHY;
            } else {
                return DEAD;
            }
        } else {
            return DEAD;
        }
    }

    /**
     * Method will calculate the memory load: used / max = load. The load value
     * is an integer between 1 and 100. It is the percent memory used. Changed
     * this to be more like other system monitors. Peter Lin 2-11-05
     *
     * @param stat status information about the jvm
     * @return memory load
     */
    public static int calculateMemoryLoad(Status stat) {
        double load = 0;
        if (stat != null) {
            double total = stat.getJvm().getMemory().getTotal();
            double free = stat.getJvm().getMemory().getFree();
            double used = total - free;
            load = (used / total);
        }
        return (int) (load * 100);
    }

    /**
     * Method will calculate the thread load: busy / max = load. The value is an
     * integer between 1 and 100. It is the percent busy.
     *
     * @param stat status information about the server
     * @return thread load
     */
    public static int calculateThreadLoad(Status stat) {
        int load = 0;
        if (stat != null && stat.getConnector().size() > 0) {
            Connector cntr = fetchConnector(stat);
            double max = cntr.getThreadInfo().getMaxThreads();
            double current = cntr.getThreadInfo().getCurrentThreadsBusy();
            load = (int) ((current / max) * 100);
        }
        return load;
    }

    /**
     * Method to get connector to use for calculate server status
     *
     * @param stat
     * @return connector
     */
    private static Connector fetchConnector(Status stat) {
        Connector cntr = null;
        String connectorPrefix = stat.getConnectorPrefix();
        if (connectorPrefix != null && connectorPrefix.length() > 0) {
           // loop to fetch desired connector
           for (int i = 0; i < stat.getConnector().size(); i++) {
               cntr = stat.getConnector().get(i);
               if (cntr.getName().startsWith(connectorPrefix)) {
                   return cntr;
               }
           }
        }
        // default : get first connector
        cntr = stat.getConnector().get(0);
        return cntr;
    }

}
