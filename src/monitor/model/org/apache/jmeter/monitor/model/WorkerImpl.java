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

    @Override
    public int getRequestProcessingTime() {
        return this.requestProcessingTime;
    }

    @Override
    public void setRequestProcessingTime(int value) {
        this.requestProcessingTime = value;
    }

    @Override
    public long getRequestBytesSent() {
        return this.requestBytesSent;
    }

    @Override
    public void setRequestBytesSent(long value) {
        this.requestBytesSent = value;
    }

    @Override
    public String getCurrentQueryString() {
        return this.currentQueryString;
    }

    @Override
    public void setCurrentQueryString(String value) {
        this.currentQueryString = value;
    }

    @Override
    public String getRemoteAddr() {
        return this.remoteAddr;
    }

    @Override
    public void setRemoteAddr(String value) {
        this.remoteAddr = value;
    }

    @Override
    public String getCurrentUri() {
        return this.currentUri;
    }

    @Override
    public void setCurrentUri(String value) {
        this.currentUri = value;
    }

    @Override
    public String getStage() {
        return this.stage;
    }

    @Override
    public void setStage(String value) {
        this.stage = value;
    }

    @Override
    public String getVirtualHost() {
        return this.virtualHost;
    }

    @Override
    public void setVirtualHost(String value) {
        this.virtualHost = value;
    }

    @Override
    public String getProtocol() {
        return this.protocol;
    }

    @Override
    public void setProtocol(String value) {
        this.protocol = value;
    }

    @Override
    public long getRequestBytesReceived() {
        return this.requestBytesReceived;
    }

    @Override
    public void setRequestBytesReceived(long value) {
        this.requestBytesReceived = value;
    }

    @Override
    public String getMethod() {
        return this.method;
    }

    @Override
    public void setMethod(String value) {
        this.method = value;
    }

}
