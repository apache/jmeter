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
     * Wrap the {@code variables} to make them unmodifiable.
     * @param variables to wrap
     */
    public UnmodifiableJMeterVariables(JMeterVariables variables) {
        this.variables = variables;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((variables == null) ? 0 : variables.hashCode());
        return result;
    }


    @Override
    public String getThreadName() {
        return variables.getThreadName();
    }

    @Override
    public int getIteration() {
        return variables.getIteration();
    }

    @Override
    public void incIteration() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object remove(String key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void put(String key, String value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putObject(String key, Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(Map<String, ?> vars) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(JMeterVariables vars) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String get(String key) {
        return variables.get(key);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        UnmodifiableJMeterVariables other = (UnmodifiableJMeterVariables) obj;
        if (variables == null) {
            if (other.variables != null) {
                return false;
            }
        } else if (!variables.equals(other.variables)) {
            return false;
        }
        return true;
    }

    @Override
    public Object getObject(String key) {
        return variables.getObject(key);
    }

    @Override
    public Iterator<Entry<String, Object>> getIterator() {
        return variables.getIterator();
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        return variables.entrySet();
    }

    public String toString() {
        return variables.toString();
    }

}
