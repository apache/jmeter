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
public class BooleanProperty extends AbstractProperty {
    private static final long serialVersionUID = 233L;

    private boolean value;

    private transient boolean savedValue;

    public BooleanProperty(String name, boolean v) {
        super(name);
        value = v;
    }

    public BooleanProperty() {
        super();
    }

    @Override
    public void setObjectValue(Object v) {
        if (v instanceof Boolean) {
            value = ((Boolean) v).booleanValue();
        } else {
            value = Boolean.parseBoolean(v.toString());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getStringValue() {
        return Boolean.toString(value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getObjectValue() {
        return Boolean.valueOf(value);
    }

    @Override
    public BooleanProperty clone() {
        BooleanProperty prop = (BooleanProperty) super.clone();
        prop.value = value;
        return prop;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getBooleanValue() {
        return value;
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
