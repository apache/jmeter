/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jorphan.util;

import org.apiguardian.api.API;

@API(status = API.Status.EXPERIMENTAL, since = "6.0.0")
public class BooleanUtils {
    private BooleanUtils() {
    }

    /**
     * Converts a String to a Boolean object based on predefined values.
     * "true", "on", "y", "t", "yes" or "1" (case-insensitive) will return {@code true}.
     * "false", "off", "n", "f", "no" or "0" (case-insensitive) will return {@code false}.
     * Otherwise, {@code null} is returned.
     *
     * @param s the String to convert, may be null
     * @return {@link Boolean#TRUE}, {@link Boolean#FALSE}, or {@code null}
     */
    @API(status = API.Status.EXPERIMENTAL, since = "6.0.0")
    public static Boolean toBooleanObject(String s) {
        if (StringUtilities.isEmpty(s)) {
            return null;
        }

        int len = s.length();
        char firstChar = s.charAt(0);

        @SuppressWarnings("NullTernary")
        Boolean result = switch (firstChar) {
            case 't', 'T' -> len == 1 || "true".equalsIgnoreCase(s) ? Boolean.TRUE : null;
            case 'y', 'Y' -> len == 1 || "yes".equalsIgnoreCase(s) ? Boolean.TRUE : null;
            case 'o', 'O' -> "on".equalsIgnoreCase(s) ? Boolean.TRUE : (
                    "off".equalsIgnoreCase(s) ? Boolean.FALSE : null
            );
            case '1' -> len == 1 ? Boolean.TRUE : null;
            case 'f', 'F' -> len == 1 || "false".equalsIgnoreCase(s) ? Boolean.FALSE : null;
            case 'n', 'N' -> len == 1 || "no".equalsIgnoreCase(s) ? Boolean.FALSE : null;
            case '0' -> len == 1 ? Boolean.FALSE : null;
            default -> null;
        };
        return result;
    }
}
