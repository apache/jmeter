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

import java.io.Serializable;

import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.TestElement;

public class ConfigTestElement extends AbstractTestElement implements Serializable, ConfigElement {
    private static final long serialVersionUID = 240L;

    public static final String USERNAME = "ConfigTestElement.username";

    public static final String PASSWORD = "ConfigTestElement.password"; //NOSONAR this is not a hardcoded password

    public ConfigTestElement() {
    }

    @Override
    public void addTestElement(TestElement parm1) {
        if (parm1 instanceof ConfigTestElement) {
            mergeIn(parm1);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addConfigElement(ConfigElement config) {
        mergeIn((TestElement) config);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean expectsModification() {
        return false;
    }
}
