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

/**
 * The class Converters provides converters of string.
 *
 * @since 3.0
 */
public final class Converters {

    private static HashMap<Class<?>, StringConverter<?>> converters = new HashMap<>();

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
        converters.put(Character.class, characterConverter);
        converters.put(char.class, characterConverter);

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
        converters.put(Double.class, doubleConverter);
        converters.put(double.class, doubleConverter);

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
        converters.put(Float.class, floatConverter);
        converters.put(float.class, floatConverter);

        StringConverter<Integer> integerConverter = new StringConverter<Integer>() {

            @Override
            public Integer convert(String value) throws ConvertException {
                try {
                    return Integer.valueOf(value);
                } catch (NumberFormatException ex) {
                    throw new ConvertException(value, Integer.class.getName(),
                            ex);
                }
            }
        };
        converters.put(Integer.class, integerConverter);
        converters.put(int.class, integerConverter);

        StringConverter<Long> longConverter = new StringConverter<Long>() {

            @Override
            public Long convert(String value) throws ConvertException {
                try {
                    return Long.valueOf(value);
                } catch (NumberFormatException ex) {
                    throw new ConvertException(value, Long.class.getName(), ex);
                }
            }
        };
        converters.put(Long.class, longConverter);
        converters.put(long.class, longConverter);

        StringConverter<Boolean> booleanConverter = new StringConverter<Boolean>() {

            @Override
            public Boolean convert(String value) {
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
    public static <TDest> StringConverter<TDest> getConverter(
            Class<TDest> clazz) {
        return (StringConverter<TDest>) converters.get(clazz);
    }

    /**
     * Converts the specified value to the destination type
     * 
     * @param <TDest>
     *            the target type
     * @param clazz
     *            the target class
     * @param value
     *            the value to convert
     * @return the converted value
     * @throws ConvertException
     *             when the conversion failed
     */
    public static <TDest> TDest convert(Class<TDest> clazz, String value)
            throws ConvertException {
        TDest result;
        if (clazz.isAssignableFrom(String.class)) {
            @SuppressWarnings("unchecked") // OK because checked above
            TDest temp = (TDest) value;
            result = temp;
        } else {
            StringConverter<TDest> converter = Converters.getConverter(clazz);
            if (converter == null) {
                throw new ConvertException(value, clazz.getName());
            }
            result = converter.convert(value);
        }
        return result;
    }

}
