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

package org.apache.jmeter.visualizers.backend;

import java.util.Iterator;
import java.util.Map;

import org.apache.jmeter.config.Arguments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * BackendListenerContext is used to provide context information to a
 * BackendListenerClient implementation. This currently consists of the
 * initialization parameters which were specified in the GUI. 
 * @since 2.13
 */
public class BackendListenerContext {
    /*
     * Implementation notes:
     *
     * All of the methods in this class are currently read-only. If update
     * methods are included in the future, they should be defined so that a
     * single instance of BackendListenerContext can be associated with each thread.
     * Therefore, no synchronization should be needed. The same instance should
     * be used for the call to setupTest, all calls to runTest, and the call to
     * teardownTest.
     */

    private static final Logger log = LoggerFactory.getLogger(BackendListenerContext.class);

    /**
     * Map containing the initialization parameters for the BackendListenerClient.
     */
    private final Map<String, String> params;

    /**
     * @param args
     *            the initialization parameters.
     */
    public BackendListenerContext(Arguments args) {
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
     * @throws IllegalArgumentException
     *             if no value defined
     * @throws NumberFormatException
     *             if the parameter is not specified or is not an integer
     *
     * @see java.lang.Integer#decode(java.lang.String)
     */
    public int getIntParameter(String name)  {
        if (params == null || !params.containsKey(name)) {
            throw new IllegalArgumentException("No value for parameter named '" + name + "'.");
        }

        return Integer.parseInt(params.get(name));
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

        final String valueString = params.get(name);
        try {
            return Integer.parseInt(valueString);
        } catch (NumberFormatException e) {
            log.warn("Value for parameter '{}' not an integer: '{}'.  Using default: '{}'.", name, valueString,
                    defaultValue, e);
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
    public long getLongParameter(String name) {
        if (params == null || !params.containsKey(name)) {
            throw new IllegalArgumentException("No value for parameter named '" + name + "'.");
        }

        return Long.parseLong(params.get(name));
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
        final String valueString = params.get(name);
        try {
            return Long.decode(valueString).longValue();
        } catch (NumberFormatException e) {
            log.warn("Value for parameter '{}' not a long: '{}'.  Using default: '{}'.", name, valueString,
                    defaultValue, e);
            return defaultValue;
        }
    }

    /**
     * @param name Parameter name
     * @param defaultValue Default value used if name is not in params
     * @return boolean
     */
    public boolean getBooleanParameter(String name, boolean defaultValue) {
        if (params == null || !params.containsKey(name)) {
            return defaultValue;
        }
        return Boolean.parseBoolean(params.get(name));
    }
}
