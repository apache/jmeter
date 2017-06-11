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

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Readonly wrapper around {@link JMeterVariables}
 * @since 3.3
 */
class UnmodifiableJMeterVariables extends JMeterVariables {
    private JMeterVariables variables;
    /**
     * 
     */
    public UnmodifiableJMeterVariables(JMeterVariables variables) {
        this.variables = variables;
    }
    /**
     * @return
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return variables.hashCode();
    }
    /**
     * @return
     * @see org.apache.jmeter.threads.JMeterVariables#getThreadName()
     */
    public String getThreadName() {
        return variables.getThreadName();
    }
    /**
     * @return
     * @see org.apache.jmeter.threads.JMeterVariables#getIteration()
     */
    public int getIteration() {
        return variables.getIteration();
    }
    /**
     * 
     * @see org.apache.jmeter.threads.JMeterVariables#incIteration()
     */
    public void incIteration() {
        throw new UnsupportedOperationException();
    }
    /**
     * @param key
     * @return
     * @see org.apache.jmeter.threads.JMeterVariables#remove(java.lang.String)
     */
    public Object remove(String key) {
        throw new UnsupportedOperationException();
    }
    /**
     * @param key
     * @param value
     * @see org.apache.jmeter.threads.JMeterVariables#put(java.lang.String, java.lang.String)
     */
    public void put(String key, String value) {
        throw new UnsupportedOperationException();
    }
    /**
     * @param key
     * @param value
     * @see org.apache.jmeter.threads.JMeterVariables#putObject(java.lang.String, java.lang.Object)
     */
    public void putObject(String key, Object value) {
        throw new UnsupportedOperationException();
    }
    /**
     * @param vars
     * @see org.apache.jmeter.threads.JMeterVariables#putAll(java.util.Map)
     */
    public void putAll(Map<String, ?> vars) {
        throw new UnsupportedOperationException();
    }
    /**
     * @param vars
     * @see org.apache.jmeter.threads.JMeterVariables#putAll(org.apache.jmeter.threads.JMeterVariables)
     */
    public void putAll(JMeterVariables vars) {
        throw new UnsupportedOperationException();
    }
    /**
     * @param key
     * @return
     * @see org.apache.jmeter.threads.JMeterVariables#get(java.lang.String)
     */
    public String get(String key) {
        return variables.get(key);
    }
    /**
     * @param obj
     * @return
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        return variables.equals(obj);
    }
    /**
     * @param key
     * @return
     * @see org.apache.jmeter.threads.JMeterVariables#getObject(java.lang.String)
     */
    public Object getObject(String key) {
        return variables.getObject(key);
    }
    /**
     * @return
     * @see org.apache.jmeter.threads.JMeterVariables#getIterator()
     */
    public Iterator<Entry<String, Object>> getIterator() {
        return variables.getIterator();
    }
    /**
     * @return
     * @see org.apache.jmeter.threads.JMeterVariables#entrySet()
     */
    public Set<Entry<String, Object>> entrySet() {
        return variables.entrySet();
    }
    /**
     * @return
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return variables.toString();
    }

}
