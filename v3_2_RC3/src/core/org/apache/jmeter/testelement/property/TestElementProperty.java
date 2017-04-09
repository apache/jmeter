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

import org.apache.jmeter.testelement.TestElement;

public class TestElementProperty extends MultiProperty {
    private static final long serialVersionUID = 233L;

    private TestElement value;

    private transient TestElement savedValue = null;

    public TestElementProperty(String name, TestElement value) {
        super(name);
        this.value = value;
    }

    public TestElementProperty() {
        super();
    }

    /**
     * Determines if two test elements are equal.
     *
     * @return true if the value is not null and equals the other Objects value;
     *         false otherwise (even if both values are null)
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof TestElementProperty) {
            if (this == o) {
                return true;
            }
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
    public void setObjectValue(Object v) {
        if (v instanceof TestElement) {
            value = (TestElement) v;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getObjectValue() {
        return value;
    }

    public TestElement getElement() {
        return value;
    }

    public void setElement(TestElement el) {
        value = el;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TestElementProperty clone() {
        TestElementProperty prop = (TestElementProperty) super.clone();
        prop.value = (TestElement) value.clone();
        return prop;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mergeIn(JMeterProperty prop) {
        if (isEqualType(prop)) {
            value.addTestElement((TestElement) prop.getObjectValue());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void recoverRunningVersion(TestElement owner) {
        if (savedValue != null) {
            value = savedValue;
        }
        value.recoverRunningVersion();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setRunningVersion(boolean runningVersion) {
        super.setRunningVersion(runningVersion);
        value.setRunningVersion(runningVersion);
        if (runningVersion) {
            savedValue = value;
        } else {
            savedValue = null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addProperty(JMeterProperty prop) {
        value.setProperty(prop);
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
    public PropertyIterator iterator() {
        return value.propertyIterator();
    }
}
