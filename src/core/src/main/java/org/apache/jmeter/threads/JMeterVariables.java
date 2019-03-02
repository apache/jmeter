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

package org.apache.jmeter.threads;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.jmeter.util.JMeterUtils;

/**
 * Class which defines JMeter variables.
 * These are similar to properties, but they are local to a single thread.
 */
public class JMeterVariables {
    private final Map<String, Object> variables = new HashMap<>();

    private int iteration = 0;

    // Property names to preload into JMeter variables:
    private static final String [] PRE_LOAD = {
      "START.MS",     // $NON-NLS-1$
      "START.YMD",    // $NON-NLS-1$
      "START.HMS",    //$NON-NLS-1$
      "TESTSTART.MS", // $NON-NLS-1$
    };

    static final String VAR_IS_SAME_USER_KEY = "__jmv_SAME_USER";

    /**
     * Constructor, that preloads the variables from the JMeter properties
     */
    public JMeterVariables() {
        preloadVariables();
    }

    private void preloadVariables(){
        for (String property : PRE_LOAD) {
            String value = JMeterUtils.getProperty(property);
            if (value != null) {
                variables.put(property, value);
            }
        }
    }

    /**
     * @return the name of the currently running thread
     */
    public String getThreadName() {
        return Thread.currentThread().getName();
    }

    /**
     * @return the current number of iterations
     */
    public int getIteration() {
        return iteration;
    }

    /**
     * Increase the current number of iterations
     */
    public void incIteration() {
        iteration++;
    }

    /**
     * Remove a variable.
     *
     * @param key the variable name to remove
     *
     * @return the variable value, or {@code null} if there was no such variable
     */
    public Object remove(String key) {
        return variables.remove(key);
    }

    /**
     * Creates or updates a variable with a String value.
     *
     * @param key the variable name
     * @param value the variable value
     */
    public void put(String key, String value) {
        variables.put(key, value);
    }

    /**
     * Creates or updates a variable with a value that does not have to be a String.
     *
     * @param key the variable name
     * @param value the variable value
     */
    public void putObject(String key, Object value) {
        variables.put(key, value);
    }

    /**
     * Updates the variables with all entries found in the {@link Map} {@code vars}
     * @param vars map with the entries to be updated
     */
    public void putAll(Map<String, ?> vars) {
        variables.putAll(vars);
    }

    /**
     * Updates the variables with all entries found in the variables in {@code vars}
     * @param vars {@link JMeterVariables} with the entries to be updated
     */
    public void putAll(JMeterVariables vars) {
        putAll(vars.variables);
    }

    /**
     * Gets the value of a variable, converted to a String.
     *
     * @param key the name of the variable
     * @return the value of the variable or a toString called on it if it's non String, or {@code null} if it does not exist
     */
    public String get(String key) {
        Object o = variables.get(key);
        if(o instanceof String) {
            return (String) o;
        } else if (o != null) {
            return o.toString();
        } else {
            return null;
        }
    }

    /**
     * Gets the value of a variable (not converted to String).
     *
     * @param key the name of the variable
     * @return the value of the variable, or {@code null} if it does not exist
     */
    public Object getObject(String key) {
        return variables.get(key);
    }

    /**
     * Gets a read-only Iterator over the variables.
     *
     * @return the iterator
     */
    public Iterator<Entry<String, Object>> getIterator(){
        return Collections.unmodifiableMap(variables).entrySet().iterator() ;
    }

    // Used by DebugSampler
    /**
     * @return an unmodifiable view of the entries contained in {@link JMeterVariables}
     */
    public Set<Entry<String, Object>> entrySet(){
        return Collections.unmodifiableMap(variables).entrySet();
    }

    /**
     * @return boolean true if user is the same on next iteration of Thread loop, false otherwise
     */
    public boolean isSameUserOnNextIteration() {
        return Boolean.TRUE.equals(variables.get(VAR_IS_SAME_USER_KEY));
    }
}
