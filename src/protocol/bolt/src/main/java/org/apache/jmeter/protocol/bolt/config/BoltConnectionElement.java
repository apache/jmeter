/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jmeter.protocol.bolt.config;

import org.apache.jmeter.config.ConfigElement;
import org.apache.jmeter.gui.TestElementMetadata;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.testbeans.TestBeanHelper;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Config;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@TestElementMetadata(labelResource = "displayName")
public class BoltConnectionElement extends AbstractTestElement
        implements ConfigElement, TestStateListener, TestBean {

    private static final Logger log = LoggerFactory.getLogger(BoltConnectionElement.class);
    private String boltUri;
    private String username;
    private String password;
    private int maxConnectionPoolSize;
    private Driver driver;

    public static final String BOLT_CONNECTION = "boltConnection";

    public BoltConnectionElement() {
    }

    @Override
    public void addConfigElement(ConfigElement config) {

    }

    @Override
    public boolean expectsModification() {
        return false;
    }

    @Override
    public void testStarted() {
        this.setRunningVersion(true);
        TestBeanHelper.prepare(this);
        JMeterVariables variables = getThreadContext().getVariables();
        if (variables.getObject(BOLT_CONNECTION) != null) {
            log.error("Bolt connection already exists");
        } else {
            synchronized (this) {
                Config config = Config.builder()
                        .withMaxConnectionPoolSize( getMaxConnectionPoolSize() )
                        .build();
                driver = GraphDatabase.driver(getBoltUri(), AuthTokens.basic(getUsername(), getPassword()), config);
                variables.putObject(BOLT_CONNECTION, driver);
            }
        }
    }

    @Override
    public void testStarted(String host) {
        testStarted();
    }

    @Override
    public void testEnded() {
        synchronized (this) {
            if (driver != null) {
                driver.close();
                driver = null;
            }
        }

    }

    @Override
    public void testEnded(String host) {
        testEnded();
    }

    public String getBoltUri() {
        return boltUri;
    }

    public void setBoltUri(String boltUri) {
        this.boltUri = boltUri;
    }

    public int getMaxConnectionPoolSize() {
        return maxConnectionPoolSize;
    }

    public void setMaxConnectionPoolSize(int maxConnectionPoolSize) {
        this.maxConnectionPoolSize = maxConnectionPoolSize;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public static Driver getDriver() {
        return (Driver) JMeterContextService.getContext().getVariables().getObject(BOLT_CONNECTION);
    }
}
