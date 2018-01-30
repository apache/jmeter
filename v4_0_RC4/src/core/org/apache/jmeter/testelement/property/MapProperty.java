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

package org.apache.jmeter.testelement.property;

import java.util.Map;

import org.apache.jmeter.testelement.TestElement;

public class MapProperty extends MultiProperty {

    private static final long serialVersionUID = 221L; // Remember to change this when the class changes ...

    private Map<String, JMeterProperty> value;

    private transient Map<String, JMeterProperty> savedValue = null;

    public MapProperty(String name, Map<?,?> value) {
        super(name);
        log.info("map = " + value);
        this.value = normalizeMap(value);
        log.info("normalized map = " + this.value);
    }

    public MapProperty() {
        super();
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object o) {
        if (o instanceof MapProperty) {
            if (value != null) {
                return value.equals(((JMeterProperty) o).getObjectValue());
            }
        }
        return false;
    }

    @Override
    public int hashCode(){
        int hash = super.hashCode();
        if (value != null) {
            hash = hash*37 + value.hashCode();
        }
        return hash;
    }

    /** {@inheritDoc} */
    @Override
    public void setObjectValue(Object v) {
        if (v instanceof Map<?, ?>) {
            setMap((Map<?, ?>) v);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void addProperty(JMeterProperty prop) {
        addProperty(prop.getName(), prop);
    }

    public JMeterProperty get(String key) {
        return value.get(key);
    }

    /**
     * Figures out what kind of properties this collection is holding and
     * returns the class type.
     *
     * @see AbstractProperty#getPropertyType()
     */
    @Override
    protected Class<? extends JMeterProperty> getPropertyType() {
        if (value.size() > 0) {
            return valueIterator().next().getClass();
        }
        return NullProperty.class;
    }

    /** {@inheritDoc} */
    @Override
    public String getStringValue() {
        return value.toString();
    }

    /** {@inheritDoc} */
    @Override
    public Object getObjectValue() {
        return value;
    }

    /** {@inheritDoc} */
    @Override
    public MapProperty clone() {
        MapProperty prop = (MapProperty) super.clone();
        prop.value = cloneMap();
        return prop;
    }

    private Map<String, JMeterProperty> cloneMap() {
        try {
            @SuppressWarnings("unchecked") // value is the correct class
            Map<String, JMeterProperty> newCol = value.getClass().newInstance();
            PropertyIterator iter = valueIterator();
            while (iter.hasNext()) {
                JMeterProperty item = iter.next();
                newCol.put(item.getName(), item.clone());
            }
            return newCol;
        } catch (Exception e) {
            log.error("Couldn't clone map", e);
            return value;
        }
    }

    public PropertyIterator valueIterator() {
        return getIterator(value.values());
    }

    public void addProperty(String name, JMeterProperty prop) {
        if (!value.containsKey(name)) {
            value.put(name, prop);
        }
    }

    public void setMap(Map<?,?> newMap) {
        value = normalizeMap(newMap);
    }

    /** {@inheritDoc} */
    @Override
    public void recoverRunningVersion(TestElement owner) {
        if (savedValue != null) {
            value = savedValue;
        }
        recoverRunningVersionOfSubElements(owner);
    }

    /** {@inheritDoc} */
    @Override
    public void clear() {
        value.clear();
    }

    /** {@inheritDoc} */
    @Override
    public PropertyIterator iterator() {
        return valueIterator();
    }

    /** {@inheritDoc} */
    @Override
    public void setRunningVersion(boolean running) {
        super.setRunningVersion(running);
        if (running) {
            savedValue = value;
        } else {
            savedValue = null;
        }
    }
}
