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

import java.lang.reflect.Method;
import java.rmi.Remote;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import net.jini.core.discovery.LookupLocator;
import net.jini.core.lookup.ServiceTemplate;
import net.jini.lookup.entry.Name;

import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.engine.util.ConfigMergabilityIndicator;
import org.apache.jmeter.protocol.jini.config.JiniConfiguration;
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
 * A Sampler for testing a Jini server.
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
    public SampleResult sample(Entry entry) {
        log.debug("sampling jini");

        log.info("configurationName=" + configurationName);

        JiniConfiguration jiniConfiguration = getJiniConfiguration();

        SampleResult res = new SampleResult();
        res.setSampleLabel(getName());
        res.setSamplerData(toString());
        res.setDataType(SampleResult.BINARY);
        res.setContentType("text/plain"); // $NON-NLS-1$
        // res.setDataEncoding(ENCODING);

        // Assume we will be successful
        res.setSuccessful(true);
        res.setResponseMessageOK();
        res.setResponseCodeOK();

        res.sampleStart();

        try {
            Remote remoteObject;

            try {
                remoteObject = lookupRemoteService(jiniConfiguration.getRmiRegistryUrl(), jiniConfiguration.getServiceName(), jiniConfiguration.getServiceInterface());
            } finally {
                // FIXME: there is separate connect time field now
                res.latencyEnd(); // use latency to measure connection time
            }

            Object result = invokeRemoteMethod(jiniConfiguration, remoteObject);
            // res.setResponseHeaders(jiniConfiguration);
            // res.setResponseData(result.toString().getBytes());
            res.setResponseData("OK");
        } catch (Exception ex) {
            log.error("Rmi call failed", ex);
            res.setResponseMessage(ex.toString());
            res.setResponseCode("-1");
            res.setResponseData(ex.getMessage().getBytes());
            res.setSuccessful(false);
        }

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

    private JiniConfiguration getJiniConfiguration() {
        Object poolObject = JMeterContextService.getContext().getVariables().getObject(configurationName);
        if (poolObject == null) {
            throw new RuntimeException("No Jini configuration found named: '" + configurationName + "', ensure Variable Name matches Variable Name of Jini Configuration");
        } else {
            if (poolObject instanceof JiniConfiguration) {
                JiniConfiguration jiniConnectionDetails = (JiniConfiguration) poolObject;
                log.info("JiniSampler.getJiniConfiguration() JiniConnectionDetails:" + jiniConnectionDetails);
                return jiniConnectionDetails;
            } else {
                String errorMsg = "Found object stored under variable:'" + configurationName + "' with class:" + poolObject.getClass().getName() + " and value: '" + poolObject
                        + " but it's not a " + JiniConfiguration.class.getName() + ", check you're not already using this name as another variable";
                log.error(errorMsg);
                throw new RuntimeException(errorMsg);
            }
        }
    }

    private Object invokeRemoteMethod(JiniConfiguration jiniConfiguration, Remote remoteObject) throws Exception {

        Class<?>[] methodArgumentTypes = getMethodArgumentTypes();

        Method remoteMethod = Class.forName(jiniConfiguration.getServiceInterface()).getDeclaredMethod(methodName, methodArgumentTypes);

        Object[] methodArguments = getMethodArguments(methodArgumentTypes);

        return remoteMethod.invoke(remoteObject, methodArguments);

    }

    private Remote lookupRemoteService(String jiniUrl, String serviceName, String serviceInterface) throws Exception {
        LookupLocator lookupLocator = new LookupLocator(jiniUrl);
        return (Remote) lookupLocator.getRegistrar().lookup(new ServiceTemplate(null,
                new Class[] { Class.forName(serviceInterface) },
                new net.jini.core.entry.Entry[] { new Name(serviceName) }));
    }

    private Class<?>[] getMethodArgumentTypes() throws ClassNotFoundException {
        if (methodParamTypes == null || methodParamTypes.trim().length() == 0) {
            return null;
        }
        String[] classNames = methodParamTypes.split(",");
        Class<?>[] classes = new Class<?>[classNames.length];
        for (int i = 0; i < classNames.length; i++) {
            String className = classNames[i];
            classes[i] = Class.forName(className);
        }
        return classes;
    }

    private Object[] getMethodArguments(Class<?>[] methodArgumentTypes) {
        if (methodArguments == null || methodArguments.trim().length() == 0) {
            if (methodArgumentTypes != null) {
                throw new IllegalArgumentException("The no. of arguments were 0, but the no. of argument types were " + methodArgumentTypes.length);
            }
            return null;
        }

        String[] methodArgumentsAsStrings = methodArguments.split(",");

        if (methodArgumentsAsStrings.length != methodArgumentTypes.length) {
            throw new IllegalArgumentException("the no. of arguments and their types do not match");
        }

        Object[] methodArguments = new Object[methodArgumentsAsStrings.length];

        for (int i = 0; i < methodArgumentsAsStrings.length; i++) {
            String methodArgumentsAsString = methodArgumentsAsStrings[i];
            methodArguments[i] = getObjectFromString(methodArgumentsAsString, methodArgumentTypes[i]);
        }

        return methodArguments;

    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private Object getObjectFromString(String methodArgumentsAsString, Class<?> methodArgumentType) {
        if (methodArgumentsAsString.trim().length() == 0) {
            return null;
        } else if (methodArgumentType == String.class) {
            return methodArgumentsAsString;
        } else if (methodArgumentType == Integer.class) {
            return Integer.valueOf(methodArgumentsAsString);
        } else if (methodArgumentType == Long.class) {
            return Long.valueOf(methodArgumentsAsString);
        } else if (methodArgumentType == Float.class) {
            return Float.valueOf(methodArgumentsAsString);
        } else if (methodArgumentType == Double.class) {
            return Double.valueOf(methodArgumentsAsString);
        } else if (methodArgumentType == Date.class) {
            String format = "yyyy-MM-dd";
            SimpleDateFormat dateFormat = new SimpleDateFormat(format);
            try {
                return dateFormat.parse(methodArgumentsAsString);
            } catch (ParseException e) {
                throw new RuntimeException("could not format the given date: " + methodArgumentsAsString + ". Expecting the date to be in the format: " + format, e);
            }
        } else if (Enum.class.isAssignableFrom(methodArgumentType)) {
            return Enum.valueOf((Class<? extends Enum>) methodArgumentType, methodArgumentsAsString);
        } else {
            throw new UnsupportedOperationException("The class " + methodArgumentType.getName() + " is not yet supprted");
        }
    }
}
