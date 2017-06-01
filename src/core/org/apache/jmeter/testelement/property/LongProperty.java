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
public class LongProperty extends NumberProperty {
    private static final long serialVersionUID = 240L;

    private long value;

    private long savedValue;

    public LongProperty(String name, long value) {
        super(name);
        this.value = value;
    }

    public LongProperty() {
        super();
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

    public void setValue(int value) {
        this.value = value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setNumberValue(Number n) {
        value = n.longValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setNumberValue(String n) throws NumberFormatException {
        value = Long.parseLong(n);
    }

    /**
     * @see JMeterProperty#getStringValue()
     */
    @Override
    public String getStringValue() {
        return Long.toString(value);
    }

    /**
     * @see JMeterProperty#getObjectValue()
     */
    @Override
    public Object getObjectValue() {
        return Long.valueOf(value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LongProperty clone() {
        LongProperty prop = (LongProperty) super.clone();
        prop.value = value;
        return prop;
    }

    /**
     * @see JMeterProperty#getBooleanValue()
     */
    @Override
    public boolean getBooleanValue() {
        return getLongValue() > 0;
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
        return (int) value;
    }

    /**
     * @see JMeterProperty#getLongValue()
     */
    @Override
    public long getLongValue() {
        return value;
    }
}
