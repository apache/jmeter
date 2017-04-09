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
 * A null property.
 *
 */
public final class NullProperty extends AbstractProperty {
    private static final long serialVersionUID = 240L;

    private JMeterProperty tempValue; // TODO - why does null property have a value?

    public NullProperty(String name) {
        super(name);
    }

    public NullProperty() {
        super();
    }

    /**
     * @see JMeterProperty#getStringValue()
     */
    @Override
    public String getStringValue() {
        if (tempValue != null) {
            return tempValue.getStringValue();
        }
        return "";
    }

    @Override
    public void setObjectValue(Object v) {
        // NOOP
    }

    /**
     * @see JMeterProperty#getObjectValue()
     */
    @Override
    public Object getObjectValue() {
        return null;
    }

    /**
     * @see JMeterProperty#isRunningVersion()
     */
    @Override
    public boolean isRunningVersion() {
        return false;
    }

    /**
     * @see JMeterProperty#mergeIn(JMeterProperty)
     */
    @Override
    public void mergeIn(JMeterProperty prop) {
        tempValue = prop;
    }

    @Override
    public NullProperty clone() {
        return this;
    }

    /**
     * @see JMeterProperty#getBooleanValue()
     */
    @Override
    public boolean getBooleanValue() {
        return false;
    }

    /**
     * @see JMeterProperty#getDoubleValue()
     */
    @Override
    public double getDoubleValue() {
        return 0;
    }

    /**
     * @see JMeterProperty#getFloatValue()
     */
    @Override
    public float getFloatValue() {
        return 0;
    }

    /**
     * @see JMeterProperty#getIntValue()
     */
    @Override
    public int getIntValue() {
        return 0;
    }

    /**
     * @see JMeterProperty#getLongValue()
     */
    @Override
    public long getLongValue() {
        return 0;
    }

    /**
     * @see JMeterProperty#recoverRunningVersion(TestElement)
     */
    @Override
    public void recoverRunningVersion(TestElement owner) {
        tempValue = null;
    }

}
