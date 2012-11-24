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
package org.apache.jmeter.monitor.model;

/**
 *
 * @version $Revision$
 */
public class ConnectorImpl implements Connector {
    private ThreadInfo threadinfo = null;

    private RequestInfo requestinfo = null;

    private Workers workers = null;

    private String name = null;

    /**
     *
     */
    public ConnectorImpl() {
        super();
    }

    @Override
    public ThreadInfo getThreadInfo() {
        return this.threadinfo;
    }

    @Override
    public void setThreadInfo(ThreadInfo value) {
        this.threadinfo = value;
    }

    @Override
    public RequestInfo getRequestInfo() {
        return this.requestinfo;
    }

    @Override
    public void setRequestInfo(RequestInfo value) {
        this.requestinfo = value;
    }

    @Override
    public Workers getWorkers() {
        return this.workers;
    }

    @Override
    public void setWorkers(Workers value) {
        this.workers = value;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void setName(String value) {
        this.name = value;
    }

}
