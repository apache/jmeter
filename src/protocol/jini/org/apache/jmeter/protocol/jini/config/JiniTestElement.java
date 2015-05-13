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
package org.apache.jmeter.protocol.jini.config;

import org.apache.jmeter.config.ConfigElement;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.testbeans.TestBeanHelper;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.util.JOrphanUtils;
import org.apache.log.Logger;

public class JiniTestElement extends AbstractTestElement implements ConfigElement, TestStateListener, TestBean {

    private static final long serialVersionUID = 1L;

    private static final Logger log = LoggingManager.getLoggerForClass();

    private transient String configurationName;
    private transient String rmiRegistryUrl;
    private transient String serviceName;
    private transient String serviceInterface;

    @Override
    public void testStarted() {
        this.setRunningVersion(true);
        TestBeanHelper.prepare(this);
        JMeterVariables variables = getThreadContext().getVariables();

        System.out.println("JiniTestElement.testStarted() configurationName=" + configurationName);
        if (JOrphanUtils.isBlank(configurationName)) {
            throw new IllegalArgumentException("Variable Name must not be empty for element:" + getName());
        } else if (variables.getObject(configurationName) != null) {
            log.error("Jini configuration already defined for: " + configurationName);
        } else {
            synchronized (this) {
                variables.putObject(configurationName, new JiniConfiguration(rmiRegistryUrl, serviceName, serviceInterface));
            }
        }
    }

    @Override
    public void testStarted(String host) {
        testStarted();
    }

    @Override
    public void testEnded() {

    }

    @Override
    public void testEnded(String host) {
        testEnded();
    }

    @Override
    public void addConfigElement(ConfigElement config) {

    }

    @Override
    public boolean expectsModification() {
        return false;
    }

    public String getConfigurationName() {
        return configurationName;
    }

    public void setConfigurationName(String configurationName) {
        this.configurationName = configurationName;
    }

    public String getRmiRegistryUrl() {
        return rmiRegistryUrl;
    }

    public void setRmiRegistryUrl(String rmiRegistryUrl) {
        this.rmiRegistryUrl = rmiRegistryUrl;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServiceInterface() {
        return serviceInterface;
    }

    public void setServiceInterface(String serviceInterface) {
        this.serviceInterface = serviceInterface;
    }

}
