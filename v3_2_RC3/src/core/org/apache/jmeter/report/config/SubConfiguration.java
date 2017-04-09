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
package org.apache.jmeter.report.config;

import java.util.HashMap;
import java.util.Map;

/**
 * The class SubConfiguration describes a sub configuration item
 *
 * @since 3.0
 */
public class SubConfiguration {

    private HashMap<String, String> properties = new HashMap<>();

    /**
     * Gets the properties of the item.
     *
     * @return the properties of the item
     */
    public final Map<String, String> getProperties() {
        return properties;
    }

    /**
     * Gets the value of the specified property.
     *
     * @param <TProperty>
     *            the type of the property
     * @param key
     *            the key identifier of the property
     * @param defaultValue
     *            the default value of the property
     * @param clazz
     *            the class of the property
     * @return the value of property if found; defaultValue otherwise
     * @throws ConfigurationException
     *             if cannot convert property
     */
    public final <TProperty> TProperty getProperty(String key,
            TProperty defaultValue, Class<TProperty> clazz)
                    throws ConfigurationException {
        String value = properties.get(key);
        TProperty result;
        if (value == null) {
            result = defaultValue;
        } else {
            result = ConfigurationUtils.convert(value, clazz);
        }
        return result;
    }
}
