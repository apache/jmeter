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

package org.apache.jmeter.protocol.mongodb.config;

import java.net.UnknownHostException;

import org.apache.jmeter.config.ConfigElement;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.protocol.mongodb.mongo.MongoDB;
import org.apache.jmeter.protocol.mongodb.mongo.MongoUtils;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.threads.JMeterContextService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.MongoClientOptions;
import com.mongodb.WriteConcern;

/**
 */
public class MongoSourceElement
    extends ConfigTestElement
        implements TestStateListener, TestBean {

    /**
     *
     */
    private static final long serialVersionUID = 2100L;

    private static final Logger log = LoggerFactory.getLogger(MongoSourceElement.class);

    private String connection;
    private String source;
    private boolean autoConnectRetry;
    private int connectionsPerHost;
    private int connectTimeout;
    private long maxAutoConnectRetryTime;
    private int maxWaitTime;
    private int socketTimeout;
    private boolean socketKeepAlive;
    private int threadsAllowedToBlockForConnectionMultiplier;
    private boolean fsync;
    private boolean safe;
    private boolean waitForJournaling;
    private int writeOperationNumberOfServers;
    private int writeOperationTimeout;
    private boolean continueOnInsertError;

    public String getTitle() {
        return this.getName();
    }

    public String getConnection() {
        return connection;
    }

    public void setConnection(String connection) {
        this.connection = connection;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public static MongoDB getMongoDB(String source) {

        Object mongoSource = JMeterContextService.getContext().getVariables().getObject(source);

        if(mongoSource == null) {
            throw new IllegalStateException("mongoSource is null");
        } else {
            if(mongoSource instanceof MongoDB) {
                return (MongoDB)mongoSource;
            } else {
                throw new IllegalStateException("Variable:"+ source +" is not a MongoDB instance, class:"+mongoSource.getClass());
            }
        }
    }

    @Override
    public void addConfigElement(ConfigElement configElement) {
    }

    @Override
    public boolean expectsModification() {
        return false;
    }

    @Override
    public void testStarted() {
        if(log.isDebugEnabled()) {
            log.debug(getTitle() + " testStarted");
        }

        MongoClientOptions.Builder builder = MongoClientOptions.builder()
                .autoConnectRetry(getAutoConnectRetry())
                .connectTimeout(getConnectTimeout())
                .connectionsPerHost(getConnectionsPerHost())
                .maxAutoConnectRetryTime(getMaxAutoConnectRetryTime())
                .maxWaitTime(getMaxWaitTime())
                .socketKeepAlive(getSocketKeepAlive())
                .socketTimeout(getSocketTimeout())
                .threadsAllowedToBlockForConnectionMultiplier(
                        getThreadsAllowedToBlockForConnectionMultiplier());

        if(getSafe()) {
            builder.writeConcern(WriteConcern.SAFE);
        } else {
            builder.writeConcern(new WriteConcern(
                    getWriteOperationNumberOfServers(),
                    getWriteOperationTimeout(),
                    getFsync(),
                    getWaitForJournaling(),
                    getContinueOnInsertError()
                    ));
        }
        MongoClientOptions mongoOptions = builder.build();

        if(log.isDebugEnabled()) {
            log.debug("options : " + mongoOptions.toString());
        }

        if(getThreadContext().getVariables().getObject(getSource()) != null) {
            if(log.isWarnEnabled()) {
                log.warn(getSource() + " has already been defined.");
            }
        }
        else {
            if(log.isDebugEnabled()) {
                log.debug(getSource() + "  is being defined.");
            }
            try {
                getThreadContext().getVariables().putObject(
                        getSource(), new MongoDB(MongoUtils.toServerAddresses(getConnection()), mongoOptions));
            } catch (UnknownHostException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    @Override
    public void testStarted(String s) {
        testStarted();
    }

    @Override
    public void testEnded() {
        if(log.isDebugEnabled()) {
            log.debug(getTitle() + " testEnded");
        }
        ((MongoDB)getThreadContext().getVariables().getObject(getSource())).clear();
    }

    @Override
    public void testEnded(String s) {
        testEnded();
    }

    /**
     * @return the autoConnectRetry
     */
    public boolean getAutoConnectRetry() {
        return autoConnectRetry;
    }

    /**
     * @param autoConnectRetry the autoConnectRetry to set
     */
    public void setAutoConnectRetry(boolean autoConnectRetry) {
        this.autoConnectRetry = autoConnectRetry;
    }

    /**
     * @return the connectionsPerHost
     */
    public int getConnectionsPerHost() {
        return connectionsPerHost;
    }

    /**
     * @param connectionsPerHost the connectionsPerHost to set
     */
    public void setConnectionsPerHost(int connectionsPerHost) {
        this.connectionsPerHost = connectionsPerHost;
    }

    /**
     * @return the connectTimeout
     */
    public int getConnectTimeout() {
        return connectTimeout;
    }

    /**
     * @param connectTimeout the connectTimeout to set
     */
    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    /**
     * @return the maxAutoConnectRetryTime
     */
    public long getMaxAutoConnectRetryTime() {
        return maxAutoConnectRetryTime;
    }

    /**
     * @param maxAutoConnectRetryTime the maxAutoConnectRetryTime to set
     */
    public void setMaxAutoConnectRetryTime(long maxAutoConnectRetryTime) {
        this.maxAutoConnectRetryTime = maxAutoConnectRetryTime;
    }

    /**
     * @return the maxWaitTime
     */
    public int getMaxWaitTime() {
        return maxWaitTime;
    }

    /**
     * @param maxWaitTime the maxWaitTime to set
     */
    public void setMaxWaitTime(int maxWaitTime) {
        this.maxWaitTime = maxWaitTime;
    }

    /**
     * @return the socketTimeout
     */
    public int getSocketTimeout() {
        return socketTimeout;
    }

    /**
     * @param socketTimeout the socketTimeout to set
     */
    public void setSocketTimeout(int socketTimeout) {
        this.socketTimeout = socketTimeout;
    }

    /**
     * @return the socketKeepAlive
     */
    public boolean getSocketKeepAlive() {
        return socketKeepAlive;
    }

    /**
     * @param socketKeepAlive the socketKeepAlive to set
     */
    public void setSocketKeepAlive(boolean socketKeepAlive) {
        this.socketKeepAlive = socketKeepAlive;
    }

    /**
     * @return the threadsAllowedToBlockForConnectionMultiplier
     */
    public int getThreadsAllowedToBlockForConnectionMultiplier() {
        return threadsAllowedToBlockForConnectionMultiplier;
    }

    /**
     * @param threadsAllowedToBlockForConnectionMultiplier the threadsAllowedToBlockForConnectionMultiplier to set
     */
    public void setThreadsAllowedToBlockForConnectionMultiplier(
            int threadsAllowedToBlockForConnectionMultiplier) {
        this.threadsAllowedToBlockForConnectionMultiplier = threadsAllowedToBlockForConnectionMultiplier;
    }

    /**
     * @return the fsync
     */
    public boolean getFsync() {
        return fsync;
    }

    /**
     * @param fsync the fsync to set
     */
    public void setFsync(boolean fsync) {
        this.fsync = fsync;
    }

    /**
     * @return the safe
     */
    public boolean getSafe() {
        return safe;
    }

    /**
     * @param safe the safe to set
     */
    public void setSafe(boolean safe) {
        this.safe = safe;
    }

    /**
     * @return the waitForJournaling
     */
    public boolean getWaitForJournaling() {
        return waitForJournaling;
    }

    /**
     * @param waitForJournaling the waitForJournaling to set
     */
    public void setWaitForJournaling(boolean waitForJournaling) {
        this.waitForJournaling = waitForJournaling;
    }

    /**
     * @return the writeOperationNumberOfServers
     */
    public int getWriteOperationNumberOfServers() {
        return writeOperationNumberOfServers;
    }

    /**
     * @param writeOperationNumberOfServers the writeOperationNumberOfServers to set
     */
    public void setWriteOperationNumberOfServers(int writeOperationNumberOfServers) {
        this.writeOperationNumberOfServers = writeOperationNumberOfServers;
    }

    /**
     * @return the writeOperationTimeout
     */
    public int getWriteOperationTimeout() {
        return writeOperationTimeout;
    }

    /**
     * @param writeOperationTimeout the writeOperationTimeout to set
     */
    public void setWriteOperationTimeout(int writeOperationTimeout) {
        this.writeOperationTimeout = writeOperationTimeout;
    }

    /**
     * @return the continueOnInsertError
     */
    public boolean getContinueOnInsertError() {
        return continueOnInsertError;
    }

    /**
     * @param continueOnInsertError the continueOnInsertError to set
     */
    public void setContinueOnInsertError(boolean continueOnInsertError) {
        this.continueOnInsertError = continueOnInsertError;
    }
}
