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

package org.apache.jmeter.testelement.property;

import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;

/**
 * Class that implements the Function property
 */
public class FunctionProperty extends AbstractProperty {
    private static final long serialVersionUID = 233L;

    private transient CompoundVariable function;

    private int testIteration = -1;

    private String cacheValue;

    public FunctionProperty(String name, CompoundVariable func) {
        super(name);
        function = func;
    }

    public FunctionProperty() {
        super();
    }

    @Override
    public void setObjectValue(Object v) {
        if (v instanceof CompoundVariable && !isRunningVersion()) {
            function = (CompoundVariable) v;
        } else {
            cacheValue = v.toString();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof FunctionProperty) {
            if (function != null) {
                return function.equals(((JMeterProperty) o).getObjectValue());
            }
        }
        return false;
    }

    @Override
    public int hashCode(){
        int hash = super.hashCode();
        if (function != null) {
            hash = hash*37 + function.hashCode();
        }
        return hash;
    }

    /**
     * Executes the function (and caches the value for the duration of the test
     * iteration) if the property is a running version. Otherwise, the raw
     * string representation of the function is provided.
     *
     * @see JMeterProperty#getStringValue()
     */
    @Override
    public String getStringValue() {
        JMeterContext ctx = JMeterContextService.getContext();// Expensive, so
                                                                // do
        // once
        if (!isRunningVersion() /*|| !ctx.isSamplingStarted()*/) {
            log.debug("Not running version, return raw function string");
            return function.getRawParameters();
        }
        if(!ctx.isSamplingStarted()) {
            return function.execute();
        }
        log.debug("Running version, executing function");
        int iter = ctx.getVariables() != null ? ctx.getVariables().getIteration() : -1;
        if (iter < testIteration) {
            testIteration = -1;
        }
        if (iter > testIteration || cacheValue == null) {
            testIteration = iter;
            cacheValue = function.execute();
        }
        return cacheValue;

    }

    /**
     * @see JMeterProperty#getObjectValue()
     */
    @Override
    public Object getObjectValue() {
        return function;
    }

    @Override
    public FunctionProperty clone() {
        FunctionProperty prop = (FunctionProperty) super.clone();
        prop.cacheValue = cacheValue;
        prop.testIteration = testIteration;
        prop.function = function;
        return prop;
    }

    /**
     * @see JMeterProperty#recoverRunningVersion(TestElement)
     */
    @Override
    public void recoverRunningVersion(TestElement owner) {
        cacheValue = null;
    }
}
