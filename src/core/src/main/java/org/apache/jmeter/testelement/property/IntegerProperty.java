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
public class IntegerProperty extends NumberProperty {
    private static final long serialVersionUID = 240L;

    private int value;

    private int savedValue;

    public IntegerProperty(String name, int value) {
        super(name);
        this.value = value;
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

    public IntegerProperty(String name) {
        super(name);
    }

    public IntegerProperty() {
        super();
    }

    public void setValue(int value) {
        this.value = value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setNumberValue(Number n) {
        value = n.intValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setNumberValue(String n) throws NumberFormatException {
        value = Integer.parseInt(n);
    }

    /**
     * @see JMeterProperty#getStringValue()
     */
    @Override
    public String getStringValue() {
        return Integer.toString(value);
    }

    /**
     * @see JMeterProperty#getObjectValue()
     */
    @Override
    public Object getObjectValue() {
        return Integer.valueOf(value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IntegerProperty clone() {
        IntegerProperty prop = (IntegerProperty) super.clone();
        prop.value = value;
        return prop;
    }

    /**
     * @see JMeterProperty#getBooleanValue()
     */
    @Override
    public boolean getBooleanValue() {
        return getIntValue() > 0;
    }

    /**
     * @see JMeterProperty#getDoubleValue()
     */
    @Override
    public double getDoubleValue() {
        return value;
    }

    /**
     * @see JMeterProperty#getFloatValue()
     */
    @Override
    public float getFloatValue() {
        return value;
    }

    /**
     * @see JMeterProperty#getIntValue()
     */
    @Override
    public int getIntValue() {
        return value;
    }

    /**
     * @see JMeterProperty#getLongValue()
     */
    @Override
    public long getLongValue() {
        return value;
    }
}
