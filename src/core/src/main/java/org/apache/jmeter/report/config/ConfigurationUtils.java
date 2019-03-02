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

import org.apache.jmeter.report.core.ConvertException;
import org.apache.jmeter.report.core.Converters;

/**
 * The class ConfigurationUtils provides helper method for configuration
 * handling.
 *
 * @since 3.0
 */
public class ConfigurationUtils {

    private static final String NOT_SUPPORTED_CONVERSION_FMT = "Convert \"%s\" to \"%s\" is not supported";

    /**
     * Instantiates a new configuration utils.
     */
    private ConfigurationUtils() {
    }

    /**
     * Convert the specified string value to the property type.
     *
     * @param <TProperty>
     *            the type of the property
     * @param value
     *            the string value
     * @param clazz
     *            the class of the property (must not be {@code null})
     * @return the converted string
     * @throws ConfigurationException
     *             when unable to convert the string
     */
    public static <TProperty> TProperty convert(String value,
            Class<TProperty> clazz) throws ConfigurationException {
        try {
            return Converters.convert(clazz, value);
        } catch (ConvertException ex) {
            throw new ConfigurationException(String.format(
                NOT_SUPPORTED_CONVERSION_FMT, value, clazz.getName()));
        }
    }
}
