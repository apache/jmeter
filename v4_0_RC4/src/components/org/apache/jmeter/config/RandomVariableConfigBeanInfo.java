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

package org.apache.jmeter.config;

import java.beans.PropertyDescriptor;

import org.apache.jmeter.testbeans.BeanInfoSupport;

public class RandomVariableConfigBeanInfo extends BeanInfoSupport {

    // These group names must have .displayName properties
    private static final String VARIABLE_GROUP = "variable"; // $NON-NLS-1$
    private static final String OPTIONS_GROUP = "options"; // $NON-NLS-1$
    private static final String RANDOM_GROUP = "random"; // $NON-NLS-1$

    // These variable names must have .displayName properties and agree with the getXXX()/setXXX() methods
    private static final String PER_THREAD = "perThread"; // $NON-NLS-1$
    private static final String RANDOM_SEED = "randomSeed"; // $NON-NLS-1$
    private static final String MAXIMUM_VALUE = "maximumValue"; // $NON-NLS-1$
    private static final String MINIMUM_VALUE = "minimumValue"; // $NON-NLS-1$
    private static final String OUTPUT_FORMAT = "outputFormat"; // $NON-NLS-1$
    private static final String VARIABLE_NAME = "variableName"; // $NON-NLS-1$

    public    RandomVariableConfigBeanInfo() {
        super(RandomVariableConfig.class);

        PropertyDescriptor p;

        createPropertyGroup(VARIABLE_GROUP, new String[] { VARIABLE_NAME, OUTPUT_FORMAT, });

        p = property(VARIABLE_NAME);
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, ""); // $NON-NLS-1$

        p = property(OUTPUT_FORMAT);
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, ""); // $NON-NLS-1$

        createPropertyGroup(RANDOM_GROUP,
        new String[] { MINIMUM_VALUE, MAXIMUM_VALUE, RANDOM_SEED, });

        p = property(MINIMUM_VALUE);
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, "1"); // $NON-NLS-1$

        p = property(MAXIMUM_VALUE);
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, ""); // $NON-NLS-1$

        p = property(RANDOM_SEED);
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, ""); // $NON-NLS-1$

        createPropertyGroup(OPTIONS_GROUP, new String[] { PER_THREAD, });

        p = property(PER_THREAD);
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(NOT_EXPRESSION, Boolean.TRUE);
        p.setValue(NOT_OTHER, Boolean.TRUE);
        p.setValue(DEFAULT, Boolean.FALSE);
    }
}
