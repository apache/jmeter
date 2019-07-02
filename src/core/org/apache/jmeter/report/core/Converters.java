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
package org.apache.jmeter.report.core;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * The class Converters provides converters of string.
 *
 * @since 3.0
 */
public final class Converters {

    private static final Map<Class<?>, StringConverter<?>> CONVERTER_MAP = new HashMap<>();

    static {

        StringConverter<Character> characterConverter = new StringConverter<Character>() {

            @Override
            public Character convert(String value) throws ConvertException {
                try {
                    return Character.valueOf(value.charAt(0));
                } catch (NumberFormatException ex) {
                    throw new ConvertException(value, Character.class.getName(),
                            ex);
                }
            }
        };
        CONVERTER_MAP.put(Character.class, characterConverter);
        CONVERTER_MAP.put(char.class, characterConverter);

        StringConverter<Double> doubleConverter = new StringConverter<Double>() {

            @Override
            public Double convert(String value) throws ConvertException {
                try {
                    return Double.valueOf(value);
                } catch (NumberFormatException ex) {
                    throw new ConvertException(value, Double.class.getName(),
                            ex);
                }
            }
        };
        CONVERTER_MAP.put(Double.class, doubleConverter);
        CONVERTER_MAP.put(double.class, doubleConverter);

        StringConverter<Float> floatConverter = new StringConverter<Float>() {

            @Override
            public Float convert(String value) throws ConvertException {
                try {
                    return Float.valueOf(value);
                } catch (NumberFormatException ex) {
                    throw new ConvertException(value, Float.class.getName(),
                            ex);
                }
            }
        };
        CONVERTER_MAP.put(Float.class, floatConverter);
        CONVERTER_MAP.put(float.class, floatConverter);

        StringConverter<Integer> integerConverter = new StringConverter<Integer>() {

            @Override
            public Integer convert(String value) throws ConvertException {
                try {
                    return Integer.valueOf(value.trim());
                } catch (NumberFormatException ex) {
                    throw new ConvertException(value, Integer.class.getName(),
                            ex);
                }
            }
        };
        CONVERTER_MAP.put(Integer.class, integerConverter);
        CONVERTER_MAP.put(int.class, integerConverter);

        StringConverter<Long> longConverter = new StringConverter<Long>() {

            @Override
            public Long convert(String value) throws ConvertException {
                try {
                    return Long.valueOf(value.trim());
                } catch (NumberFormatException ex) {
                    throw new ConvertException(value, Long.class.getName(), ex);
                }
            }
        };
        CONVERTER_MAP.put(Long.class, longConverter);
        CONVERTER_MAP.put(long.class, longConverter);

        StringConverter<Boolean> booleanConverter = Boolean::valueOf;

        CONVERTER_MAP.put(Boolean.class, booleanConverter);
        CONVERTER_MAP.put(boolean.class, booleanConverter);

        CONVERTER_MAP.put(File.class, new StringConverter<File>() {

            @Override
            public File convert(String value) throws ConvertException {
                return new File(value);
            }
        });
    }

    private Converters() {
        // OK, we don't want anyone to instantiate this class.
    }

    /**
     * Gets the converter for the specified class.
     *
     * @param <T>
     *            the target type
     * @param clazz
     *            the target class
     * @return the converter
     */
    @SuppressWarnings("unchecked")
    public static <T> StringConverter<T> getConverter(
            Class<T> clazz) {
        return (StringConverter<T>) CONVERTER_MAP.get(clazz);
    }

    /**
     * Converts the specified value to the destination type
     *
     * @param <T>
     *            the target type
     * @param clazz
     *            the target class
     * @param value
     *            the value to convert
     * @return the converted value
     * @throws ConvertException
     *             when the conversion failed
     */
    public static <T> T convert(Class<T> clazz, String value)
            throws ConvertException {
        T result;
        if (clazz.isAssignableFrom(String.class)) {
            @SuppressWarnings("unchecked") // OK because checked above
            T temp = (T) value;
            result = temp;
        } else {
            StringConverter<T> converter = Converters.getConverter(clazz);
            if (converter == null) {
                throw new ConvertException(value, clazz.getName());
            }
            result = converter.convert(value);
        }
        return result;
    }

}
