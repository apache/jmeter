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

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

/**
 * The class JsonUtil provides helper functions to generate Json.
 *
 * @since 3.0
 */
public final class JsonUtil {

    /**
     * Converts the specified array to a json-like array string.
     *
     * @param array
     *            the array
     * @return the json string
     */
    public static String toJsonArray(final String... array) {
        return '[' + StringUtils.join(array, ", ") + ']';
    }

    /**
     * Converts the specified map to a json-like object string.
     *
     * @param map
     *            the map
     * @return the string
     */
    public static String toJsonObject(Map<String, String> map) {
        String result = "{";
        if (map != null) {
            String[] array = new String[map.size()];
            int index = 0;
            for (Map.Entry<String, String> entry : map.entrySet()) {
                array[index] = '"' + entry.getKey() + "\": " + entry.getValue();
                index++;
            }
            result += StringUtils.join(array, ", ");
        }
        return result + "}";
    }
}
