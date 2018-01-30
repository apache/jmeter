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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.jmeter.testelement.TestElement;

public class CollectionProperty extends MultiProperty {

    private static final long serialVersionUID = 221L; // Remember to change this when the class changes ...

    private Collection<JMeterProperty> value;

    private transient Collection<JMeterProperty> savedValue;

    public CollectionProperty(String name, Collection<?> value) {
        super(name);
        this.value = normalizeList(value);
    }

    public CollectionProperty() {
        super();
        value = new ArrayList<>();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof CollectionProperty) {
            if (value != null) {
                return value.equals(((JMeterProperty) o).getObjectValue());
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return value == null ? 0 : value.hashCode();
    }

    public void remove(String prop) {
        PropertyIterator iter = iterator();
        while (iter.hasNext()) {
            if (iter.next().getName().equals(prop)) {
                iter.remove();
            }
        }
    }

    public void set(int index, String prop) {
        if (value instanceof List<?>) {
            ((List<JMeterProperty>) value).set(index, new StringProperty(prop, prop));
        }
    }

    public void set(int index, JMeterProperty prop) {
        if (value instanceof List<?>) {
            ((List<JMeterProperty>) value).set(index, prop);
        }
    }

    public JMeterProperty get(int row) {
        if (value instanceof List<?>) {
            return ((List<JMeterProperty>) value).get(row);
        }
        return null;
    }

    public void remove(int index) {
        if (value instanceof List<?>) {
            ((List<?>) value).remove(index);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setObjectValue(Object v) {
        if (v instanceof Collection<?>) {
            setCollection((Collection<?>) v);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PropertyIterator iterator() {
        return getIterator(value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getStringValue() {
        return value.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getObjectValue() {
        return value;
    }

    public int size() {
        return value.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CollectionProperty clone() {
        CollectionProperty prop = (CollectionProperty) super.clone();
        prop.value = cloneCollection();
        return prop;
    }

    private Collection<JMeterProperty> cloneCollection() {
        try {
            @SuppressWarnings("unchecked") // value is of type Collection<JMeterProperty>
            Collection<JMeterProperty> newCol = value.getClass().newInstance();
            for (JMeterProperty jMeterProperty : this) {
                newCol.add(jMeterProperty.clone());
            }
            return newCol;
        } catch (Exception e) {
            log.error("Couldn't clone collection", e);
            return value;
        }
    }

    public void setCollection(Collection<?> coll) {
        value = normalizeList(coll);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear() {
        value.clear();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addProperty(JMeterProperty prop) {
        value.add(prop);
    }

    public void addItem(Object item) {
        addProperty(convertObject(item));
    }

    /**
     * Figures out what kind of properties this collection is holding and
     * returns the class type.
     *
     * @see AbstractProperty#getPropertyType()
     */
    @Override
    protected Class<? extends JMeterProperty> getPropertyType() {
        if (value != null && value.size() > 0) {
            return value.iterator().next().getClass();
        }
        return NullProperty.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void recoverRunningVersion(TestElement owner) {
        if (savedValue != null) {
            value = savedValue;
        }
        recoverRunningVersionOfSubElements(owner);
    }

    /**
     * {@inheritDoc}
     */
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
