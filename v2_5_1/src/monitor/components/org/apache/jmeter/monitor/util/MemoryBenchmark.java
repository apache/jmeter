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

import java.util.List;
import java.util.LinkedList;

import org.apache.jmeter.monitor.model.Connector;
import org.apache.jmeter.monitor.model.Jvm;
import org.apache.jmeter.monitor.model.Memory;
import org.apache.jmeter.monitor.model.ObjectFactory;
import org.apache.jmeter.monitor.model.RequestInfo;
import org.apache.jmeter.monitor.model.Status;
import org.apache.jmeter.monitor.model.ThreadInfo;
import org.apache.jmeter.monitor.model.Worker;
import org.apache.jmeter.monitor.model.Workers;
import org.apache.jmeter.visualizers.MonitorModel;
import org.apache.jmeter.visualizers.MonitorStats;

/**
 *
 * @version $Revision$
 */
public class MemoryBenchmark {

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static void main(String[] args) {
        if (args.length == 1) {
            int objects = 10000;
            if (args[0] != null) {
                objects = Integer.parseInt(args[0]);
            }
            List objs = new LinkedList();
            ObjectFactory of = ObjectFactory.getInstance();

            long bfree = Runtime.getRuntime().freeMemory();
            long btotal = Runtime.getRuntime().totalMemory();
            long bmax = Runtime.getRuntime().maxMemory();
            System.out.println("Before we create objects:");
            System.out.println("------------------------------");
            System.out.println("free: " + bfree);
            System.out.println("total: " + btotal);
            System.out.println("max: " + bmax);

            for (int idx = 0; idx < objects; idx++) {
                Connector cnn = of.createConnector();
                Workers wkrs = of.createWorkers();
                for (int idz = 0; idz < 26; idz++) {
                    Worker wk0 = of.createWorker();
                    wk0.setCurrentQueryString("/manager/status");
                    wk0.setCurrentUri("http://localhost/manager/status");
                    wk0.setMethod("GET");
                    wk0.setProtocol("http");
                    wk0.setRemoteAddr("?");
                    wk0.setRequestBytesReceived(132);
                    wk0.setRequestBytesSent(18532);
                    wk0.setStage("K");
                    wk0.setVirtualHost("?");
                    wkrs.getWorker().add(wk0);
                }
                cnn.setWorkers(wkrs);

                RequestInfo rqinfo = of.createRequestInfo();
                rqinfo.setBytesReceived(0);
                rqinfo.setBytesSent(434374);
                rqinfo.setErrorCount(10);
                rqinfo.setMaxTime(850);
                rqinfo.setProcessingTime(2634);
                rqinfo.setRequestCount(1002);
                cnn.setRequestInfo(rqinfo);

                ThreadInfo thinfo = of.createThreadInfo();
                thinfo.setCurrentThreadCount(50);
                thinfo.setCurrentThreadsBusy(12);
                thinfo.setMaxSpareThreads(50);
                thinfo.setMaxThreads(150);
                thinfo.setMinSpareThreads(10);
                cnn.setThreadInfo(thinfo);

                Jvm vm = of.createJvm();
                Memory mem = of.createMemory();
                mem.setFree(77280);
                mem.setTotal(134210000);
                mem.setMax(134217728);
                vm.setMemory(mem);

                Status st = of.createStatus();
                st.setJvm(vm);
                st.getConnector().add(cnn);

                MonitorStats mstats = new MonitorStats(Stats.calculateStatus(st), Stats.calculateLoad(st), 0, Stats
                        .calculateMemoryLoad(st), Stats.calculateThreadLoad(st), "localhost", "8080", "http", System
                        .currentTimeMillis());
                MonitorModel monmodel = new MonitorModel(mstats);
                objs.add(monmodel);
            }
            long afree = Runtime.getRuntime().freeMemory();
            long atotal = Runtime.getRuntime().totalMemory();
            long amax = Runtime.getRuntime().maxMemory();
            long delta = ((atotal - afree) - (btotal - bfree));
            System.out.println("After we create objects:");
            System.out.println("------------------------------");
            System.out.println("free: " + afree);
            System.out.println("total: " + atotal);
            System.out.println("max: " + amax);
            System.out.println("------------------------------");
            System.out.println("delta: " + (delta / 1024) + " kilobytes");
            System.out.println("delta: " + (delta / 1024 / 1024) + " megabytes");
            System.out.println("number of objects: " + objects);
            System.out.println("potential number of servers: " + (objects / 1000));

        } else {
            System.out.println("Please provide the number of objects");
        }
    }
}
