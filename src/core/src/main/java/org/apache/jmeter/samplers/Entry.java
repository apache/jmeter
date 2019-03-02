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

package org.apache.jmeter.samplers;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.jmeter.assertions.Assertion;
import org.apache.jmeter.config.ConfigElement;

// TODO - the class contents are not used at present - could perhaps be removed
public class Entry {

    private Map<Class<?>, ConfigElement> configSet;

    private Class<?> sampler;

    private List<Assertion> assertions;

    public Entry() {
        configSet = new HashMap<>();
        assertions = new LinkedList<>();
    }

    public void addAssertion(Assertion assertion) {
        assertions.add(assertion);
    }

    public List<Assertion> getAssertions() {
        return assertions;
    }

    public void setSamplerClass(Class<?> samplerClass) {
        this.sampler = samplerClass;
    }

    public Class<?> getSamplerClass() {
        return this.sampler;
    }

    public ConfigElement getConfigElement(Class<?> configClass) {
        return configSet.get(configClass);
    }

    public void addConfigElement(ConfigElement config) {
        addConfigElement(config, config.getClass());
    }

    /**
     * Add a config element as a specific class. Usually this is done to add a
     * subclass as one of it's parent classes.
     * 
     * @param config
     *            the {@link ConfigElement} to be added
     * @param asClass
     *            the {@link Class} under which the {@link ConfigElement} should
     *            be registered
     */
    public void addConfigElement(ConfigElement config, Class<?> asClass) {
        if (config != null) {
            ConfigElement current = configSet.get(asClass);
            if (current == null) {
                configSet.put(asClass, cloneIfNecessary(config));
            } else {
                current.addConfigElement(config);
            }
        }
    }

    private ConfigElement cloneIfNecessary(ConfigElement config) {
        if (config.expectsModification()) {
            return config;
        }
        return (ConfigElement) config.clone();
    }
}
