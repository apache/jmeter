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

/**
 */
public class StringProperty extends AbstractProperty {
    private static final long serialVersionUID = 233L;

    private String value;

    private transient String savedValue;

    public StringProperty(String name, String value) {
        super(name);
        this.value = value;
    }

    public StringProperty() {
        super();
    }

    /**
     * @see JMeterProperty#setRunningVersion(boolean)
     */
    @Override
    public void setRunningVersion(boolean runningVersion) {
        super.setRunningVersion(runningVersion);
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
    public void setObjectValue(Object v) {
        value = v.toString();
    }

    /**
     * @see JMeterProperty#getStringValue()
     */
    @Override
    public String getStringValue() {
        return value;
    }

    /**
     * @see JMeterProperty#getObjectValue()
     */
    @Override
    public Object getObjectValue() {
        return value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StringProperty clone() {
        StringProperty prop = (StringProperty) super.clone();
        prop.value = value;
        return prop;
    }

    /**
     * Sets the value.
     *
     * @param value
     *            The value to set
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void recoverRunningVersion(TestElement owner) {
        if (savedValue != null) {
            value = savedValue;
        }
    }
}
