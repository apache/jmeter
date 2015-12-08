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

import java.io.File;
import java.util.HashMap;

/**
 * The class Converters provides converters of string.
 *
 * @since 2.14
 */
public final class Converters {

    private static HashMap<Class<?>, StringConverter<?>> converters = new HashMap<>();

    static {

        StringConverter<Double> doubleConverter = new StringConverter<Double>() {

            @Override
            public Double convert(String value) throws ConvertException {
                try {
                    return Double.valueOf(value);
                } catch (NumberFormatException ex) {
                    throw new ConvertException(value, Double.class.getName());
                }
            }
        };
        converters.put(Double.class, doubleConverter);
        converters.put(double.class, doubleConverter);

        StringConverter<Long> longConverter = new StringConverter<Long>() {

            @Override
            public Long convert(String value) throws ConvertException {
                try {
                    return Long.valueOf(value);
                } catch (NumberFormatException ex) {
                    throw new ConvertException(value, Long.class.getName());
                }
            }
        };
        converters.put(Long.class, longConverter);
        converters.put(long.class, longConverter);

        StringConverter<Boolean> booleanConverter = new StringConverter<Boolean>() {

            @Override
            public Boolean convert(String value) throws ConvertException {
                return Boolean.valueOf(value);
            }
        };
        converters.put(Boolean.class, booleanConverter);
        converters.put(boolean.class, booleanConverter);

        converters.put(File.class, new StringConverter<File>() {

            @Override
            public File convert(String value) throws ConvertException {
                return new File(value);
            }
        });
    }

    /**
     * Gets the converter for the specified class.
     *
     * @param <TDest>
     *            the target type
     * @param clazz
     *            the target class
     * @return the converter
     */
    @SuppressWarnings("unchecked")
    public static <TDest> StringConverter<TDest> getConverter(Class<TDest> clazz) {
        return (StringConverter<TDest>) converters.get(clazz);
    }

}
