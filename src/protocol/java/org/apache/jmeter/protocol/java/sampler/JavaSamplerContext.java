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

package org.apache.jmeter.protocol.java.sampler;

import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.test.JavaTest;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.JMeterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JavaSamplerContext is used to provide context information to a
 * JavaSamplerClient implementation. This currently consists of the
 * initialization parameters which were specified in the GUI. Additional data
 * may be accessible through JavaSamplerContext in the future.
 *
 */
public class JavaSamplerContext {
    /*
     * Implementation notes:
     *
     * All of the methods in this class are currently read-only. If update
     * methods are included in the future, they should be defined so that a
     * single instance of JavaSamplerContext can be associated with each thread.
     * Therefore, no synchronization should be needed. The same instance should
     * be used for the call to setupTest, all calls to runTest, and the call to
     * teardownTest.
     */

    /** Logging */
    private static final Logger log = LoggerFactory.getLogger(JavaTest.class);

    /**
     * Map containing the initialization parameters for the JavaSamplerClient.
     */
    private final Map<String, String> params;

    /**
     * Create a new JavaSampler with the specified initialization parameters.
     *
     * @param args
     *            the initialization parameters.
     */
    public JavaSamplerContext(Arguments args) {
        this.params = args.getArgumentsAsMap();
    }

    /**
     * Determine whether or not a value has been specified for the parameter
     * with this name.
     *
     * @param name
     *            the name of the parameter to test
     * @return true if the parameter value has been specified, false otherwise.
     */
    public boolean containsParameter(String name) {
        return params.containsKey(name);
    }

    /**
     * Get an iterator of the parameter names. Each entry in the Iterator is a
     * String.
     *
     * @return an Iterator of Strings listing the names of the parameters which
     *         have been specified for this test.
     */
    public Iterator<String> getParameterNamesIterator() {
        return params.keySet().iterator();
    }

    /**
     * Get the value of a specific parameter as a String, or null if the value
     * was not specified.
     *
     * @param name
     *            the name of the parameter whose value should be retrieved
     * @return the value of the parameter, or null if the value was not
     *         specified
     */
    public String getParameter(String name) {
        return getParameter(name, null);
    }

    /**
     * Get the value of a specified parameter as a String, or return the
     * specified default value if the value was not specified.
     *
     * @param name
     *            the name of the parameter whose value should be retrieved
     * @param defaultValue
     *            the default value to return if the value of this parameter was
     *            not specified
     * @return the value of the parameter, or the default value if the parameter
     *         was not specified
     */
    public String getParameter(String name, String defaultValue) {
        if (params == null || !params.containsKey(name)) {
            return defaultValue;
        }
        return params.get(name);
    }

    /**
     * Get the value of a specified parameter as an integer. An exception will
     * be thrown if the parameter is not specified or if it is not an integer.
     * The value may be specified in decimal, hexadecimal, or octal, as defined
     * by Integer.decode().
     *
     * @param name
     *            the name of the parameter whose value should be retrieved
     * @return the value of the parameter
     *
     * @throws NumberFormatException
     *             if the parameter is not specified or is not an integer
     *
     * @see java.lang.Integer#decode(java.lang.String)
     */
    public int getIntParameter(String name) throws NumberFormatException {
        if (params == null || !params.containsKey(name)) {
            throw new NumberFormatException("No value for parameter named '" + name + "'.");
        }

        return Integer.decode(params.get(name)).intValue();
    }

    /**
     * Get the value of a specified parameter as an integer, or return the
     * specified default value if the value was not specified or is not an
     * integer. A warning will be logged if the value is not an integer. The
     * value may be specified in decimal, hexadecimal, or octal, as defined by
     * Integer.decode().
     *
     * @param name
     *            the name of the parameter whose value should be retrieved
     * @param defaultValue
     *            the default value to return if the value of this parameter was
     *            not specified
     * @return the value of the parameter, or the default value if the parameter
     *         was not specified
     *
     * @see java.lang.Integer#decode(java.lang.String)
     */
    public int getIntParameter(String name, int defaultValue) {
        if (params == null || !params.containsKey(name)) {
            return defaultValue;
        }

        try {
            return Integer.decode(params.get(name)).intValue();
        } catch (NumberFormatException e) {
            log.warn("Value for parameter '" + name + "' not an integer: '" + params.get(name) + "'.  Using default: '"
                    + defaultValue + "'.", e);
            return defaultValue;
        }
    }

    /**
     * Get the value of a specified parameter as a long. An exception will be
     * thrown if the parameter is not specified or if it is not a long. The
     * value may be specified in decimal, hexadecimal, or octal, as defined by
     * Long.decode().
     *
     * @param name
     *            the name of the parameter whose value should be retrieved
     * @return the value of the parameter
     *
     * @throws NumberFormatException
     *             if the parameter is not specified or is not a long
     *
     * @see Long#decode(String)
     */
    public long getLongParameter(String name) throws NumberFormatException {
        if (params == null || !params.containsKey(name)) {
            throw new NumberFormatException("No value for parameter named '" + name + "'.");
        }

        return Long.decode(params.get(name)).longValue();
    }

    /**
     * Get the value of a specified parameter as along, or return the specified
     * default value if the value was not specified or is not a long. A warning
     * will be logged if the value is not a long. The value may be specified in
     * decimal, hexadecimal, or octal, as defined by Long.decode().
     *
     * @param name
     *            the name of the parameter whose value should be retrieved
     * @param defaultValue
     *            the default value to return if the value of this parameter was
     *            not specified
     * @return the value of the parameter, or the default value if the parameter
     *         was not specified
     *
     * @see Long#decode(String)
     */
    public long getLongParameter(String name, long defaultValue) {
        if (params == null || !params.containsKey(name)) {
            return defaultValue;
        }
        try {
            return Long.decode(params.get(name)).longValue();
        } catch (NumberFormatException e) {
            log.warn("Value for parameter '" + name + "' not a long: '" + params.get(name) + "'.  Using default: '"
                    + defaultValue + "'.", e);
            return defaultValue;
        }
    }
    
    /**
     * 
     * @return {@link JMeterContext}
     */
    public JMeterContext getJMeterContext() {
        return JMeterContextService.getContext();
    }
    /**
     * @return {@link JMeterVariables}
     */
    public final JMeterVariables getJMeterVariables() {
        return JMeterContextService.getContext().getVariables();
    }
    
    /**
     * 
     * @return {@link Properties} JMeter properties
     */
    public final Properties getJMeterProperties() {
        return JMeterUtils.getJMeterProperties();
    }
    
}
