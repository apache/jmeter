/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 */

/*
 * Created on Sep 16, 2004
 */
package org.apache.jmeter.testelement.property;

import org.apache.jmeter.testelement.TestElement;

public class ObjectProperty extends AbstractProperty {
    private static final long serialVersionUID = 1;

    private Object value;

    private Object savedValue;

    /**
     * {@inheritDoc}
     */
    public void recoverRunningVersion(TestElement owner) {
        if (savedValue != null) {
            value = savedValue;
        }
    }

    /**
     * {@inheritDoc}
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
    public Object clone() {
        ObjectProperty p = (ObjectProperty) super.clone();
        p.value = value;
        return p;
    }

    /**
     *
     */
    public ObjectProperty() {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * @param name
     */
    public ObjectProperty(String name) {
        super(name);
    }

    public ObjectProperty(String name, Object p) {
        super(name);
        value = p;
    }

    /**
     * {@inheritDoc}
     */
    public String getStringValue() {
        return value.toString();
    }

    /**
     * {@inheritDoc}
     */
    public Object getObjectValue() {
        return value;
    }

    /**
     * {@inheritDoc}
     */
    public void setObjectValue(Object value) {
        this.value = value;

    }
}
