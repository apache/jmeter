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
 */
public class WorkerImpl implements Worker {
    private int requestProcessingTime = 0;

    private long requestBytesSent = 0;

    private long requestBytesReceived = 0;

    private String currentQueryString = null;

    private String remoteAddr = null;

    private String currentUri = null;

    private String method = null;

    private String protocol = null;

    private String stage = null;

    private String virtualHost = null;

    /**
     *
     */
    public WorkerImpl() {
        super();
    }

    public int getRequestProcessingTime() {
        return this.requestProcessingTime;
    }

    public void setRequestProcessingTime(int value) {
        this.requestProcessingTime = value;
    }

    public long getRequestBytesSent() {
        return this.requestBytesSent;
    }

    public void setRequestBytesSent(long value) {
        this.requestBytesSent = value;
    }

    public String getCurrentQueryString() {
        return this.currentQueryString;
    }

    public void setCurrentQueryString(String value) {
        this.currentQueryString = value;
    }

    public String getRemoteAddr() {
        return this.remoteAddr;
    }

    public void setRemoteAddr(String value) {
        this.remoteAddr = value;
    }

    public String getCurrentUri() {
        return this.currentUri;
    }

    public void setCurrentUri(String value) {
        this.currentUri = value;
    }

    public String getStage() {
        return this.stage;
    }

    public void setStage(String value) {
        this.stage = value;
    }

    public String getVirtualHost() {
        return this.virtualHost;
    }

    public void setVirtualHost(String value) {
        this.virtualHost = value;
    }

    public String getProtocol() {
        return this.protocol;
    }

    public void setProtocol(String value) {
        this.protocol = value;
    }

    public long getRequestBytesReceived() {
        return this.requestBytesReceived;
    }

    public void setRequestBytesReceived(long value) {
        this.requestBytesReceived = value;
    }

    public String getMethod() {
        return this.method;
    }

    public void setMethod(String value) {
        this.method = value;
    }

}
