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

package org.apache.jmeter.config;

import java.beans.PropertyDescriptor;

import org.apache.jmeter.testbeans.BeanInfoSupport;

/**
 * Keystore Configuration BeanInfo
 */
public class KeystoreConfigBeanInfo extends BeanInfoSupport {

    private static final String ALIASES_GROUP = "aliases";
    private static final String ALIAS_END_INDEX = "endIndex";
    private static final String ALIAS_START_INDEX = "startIndex";
    private static final String CLIENT_CERT_ALIAS_VAR_NAME = "clientCertAliasVarName";
    private static final String PRELOAD = "preload";

    /**
     * Constructor
     */
    public KeystoreConfigBeanInfo() {
        super(KeystoreConfig.class);

        createPropertyGroup(ALIASES_GROUP, new String[] { 
                PRELOAD, CLIENT_CERT_ALIAS_VAR_NAME, ALIAS_START_INDEX, ALIAS_END_INDEX });

        PropertyDescriptor p = property(PRELOAD);
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, "true"); // $NON-NLS-1$
        p.setValue(NOT_EXPRESSION, Boolean.TRUE);
        p.setValue(NOT_OTHER, Boolean.TRUE);
        p.setValue(TAGS, new String[]{"True", "False"}); // $NON-NLS-1$ $NON-NLS-2$

        p = property(CLIENT_CERT_ALIAS_VAR_NAME);
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, ""); // $NON-NLS-1$

        p = property(ALIAS_START_INDEX);
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, ""); // $NON-NLS-1$

        p = property(ALIAS_END_INDEX);
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, ""); // $NON-NLS-1$       
    }
}
