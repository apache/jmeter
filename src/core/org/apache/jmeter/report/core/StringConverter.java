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

/**
 * The interface StringConverter represents a converter from a string to another
 * type.
 *
 * @param <TDest>
 *            the generic type
 * @since 3.0
 */
public interface StringConverter<TDest> {

    /**
     * Converts the specified value to the type TDest.
     *
     * @param value
     *            the value to convert
     * @return the destination type
     * @throws ConvertException
     *             occurs when the value cannot be converted to the type TDest
     */
    TDest convert(String value) throws ConvertException;
}
