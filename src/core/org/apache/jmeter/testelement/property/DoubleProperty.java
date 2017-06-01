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
public class DoubleProperty extends NumberProperty {
    private static final long serialVersionUID = 240L;

    private double value;

    private double savedValue;

    public DoubleProperty(String name, double value) {
        super(name);
        this.value = value;
    }

    public DoubleProperty() {
    }

    public void setValue(float value) {
        this.value = value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setNumberValue(Number n) {
        value = n.doubleValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setNumberValue(String n) throws NumberFormatException {
        value = Double.parseDouble(n);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getStringValue() {
        return Double.toString(value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getObjectValue() {
        return Double.valueOf(value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DoubleProperty clone() {
        DoubleProperty prop = (DoubleProperty) super.clone();
        prop.value = value;
        return prop;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getBooleanValue() {
        return value > 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getDoubleValue() {
        return value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float getFloatValue() {
        return (float) value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getIntValue() {
        return (int) value;
    }

    /**
     * @see JMeterProperty#getLongValue()
     */
    @Override
    public long getLongValue() {
        return (long) value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setRunningVersion(boolean runningVersion) {
        savedValue = value;
        super.setRunningVersion(runningVersion);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void recoverRunningVersion(TestElement owner) {
        value = savedValue;
    }
}
