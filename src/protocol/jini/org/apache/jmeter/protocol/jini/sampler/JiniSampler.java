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

package org.apache.jmeter.protocol.jini.sampler;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.engine.util.ConfigMergabilityIndicator;
import org.apache.jmeter.protocol.jini.config.JiniConnectionDetails;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * A sampler which understands JDBC database requests.
 *
 */
public class JiniSampler extends AbstractTestElement implements Sampler, TestBean, ConfigMergabilityIndicator {
    private static final Set<String> APPLIABLE_CONFIG_CLASSES = new HashSet<String>(Arrays.asList(new String[] { "org.apache.jmeter.config.gui.SimpleConfigGui" }));

    private static final long serialVersionUID = 1L;

    private static final Logger log = LoggingManager.getLoggerForClass();

    private String configurationName;
    private String methodName;
    private String methodParamTypes;
    private String methodArguments;

    public JiniSampler() {
    }

    @Override
    public SampleResult sample(Entry e) {
        log.debug("sampling jini");

        System.out.println("configurationName=" + configurationName);

        Object poolObject = JMeterContextService.getContext().getVariables().getObject(configurationName);
        if (poolObject == null) {
            throw new RuntimeException("No Jini configuration found named: '" + configurationName + "', ensure Variable Name matches Variable Name of Jini Configuration");
        } else {
            if (poolObject instanceof JiniConnectionDetails) {
                JiniConnectionDetails jiniConnectionDetails = (JiniConnectionDetails) poolObject;
                System.out.println("JiniSampler.sample() JiniConnectionDetails:" + jiniConnectionDetails);
            } else {
                String errorMsg = "Found object stored under variable:'" + configurationName + "' with class:" + poolObject.getClass().getName() + " and value: '" + poolObject
                        + " but it's not a " + JiniConnectionDetails.class.getName() + ", check you're not already using this name as another variable";
                log.error(errorMsg);
                throw new RuntimeException(errorMsg);
            }
        }

        SampleResult res = new SampleResult();
        res.setSampleLabel(getName());
        res.setSamplerData(toString());
        res.setDataType(SampleResult.TEXT);
        res.setContentType("text/plain"); // $NON-NLS-1$
        // res.setDataEncoding(ENCODING);

        // Assume we will be successful
        res.setSuccessful(true);
        res.setResponseMessageOK();
        res.setResponseCodeOK();

        res.sampleStart();
        // Connection conn = null;
        //
        // try {
        // if (JOrphanUtils.isBlank(getDataSource())) {
        // throw new
        // IllegalArgumentException("Variable Name must not be null in " +
        // getName());
        // }
        //
        // try {
        // conn = DataSourceElement.getConnection(getDataSource());
        // } finally {
        // // FIXME: there is separate connect time field now
        // res.latencyEnd(); // use latency to measure connection time
        // }
        // res.setResponseHeaders(conn.toString());
        // res.setResponseData(execute(conn));
        // } catch (SQLException ex) {
        // final String errCode = Integer.toString(ex.getErrorCode());
        // res.setResponseMessage(ex.toString());
        // res.setResponseCode(ex.getSQLState() + " " + errCode);
        // res.setResponseData(ex.getMessage().getBytes());
        // res.setSuccessful(false);
        // } catch (Exception ex) {
        // res.setResponseMessage(ex.toString());
        // res.setResponseCode("000");
        // res.setResponseData(ex.getMessage().getBytes());
        // res.setSuccessful(false);
        // }

        // TODO: process warnings? Set Code and Message to success?
        res.sampleEnd();
        return res;
    }

    @Override
    public boolean applies(ConfigTestElement configElement) {
        String guiClass = configElement.getProperty(TestElement.GUI_CLASS).getStringValue();
        return APPLIABLE_CONFIG_CLASSES.contains(guiClass);
    }

    public String getConfigurationName() {
        return configurationName;
    }

    public void setConfigurationName(String configurationName) {
        this.configurationName = configurationName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getMethodParamTypes() {
        return methodParamTypes;
    }

    public void setMethodParamTypes(String methodParamTypes) {
        this.methodParamTypes = methodParamTypes;
    }

    public String getMethodArguments() {
        return methodArguments;
    }

    public void setMethodArguments(String methodArguments) {
        this.methodArguments = methodArguments;
    }

}
