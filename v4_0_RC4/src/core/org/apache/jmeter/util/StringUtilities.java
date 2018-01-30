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

package org.apache.jmeter.util;

public final class StringUtilities {

    /**
     * Private constructor to prevent instantiation.
     */
    private StringUtilities() {
    }

    /**
     * Replace all patterns in a String
     *
     * @see String#replaceAll(String,String)
     *  - JDK1.4 only
     *
     * @param input - string to be transformed
     * @param pattern - pattern to replace
     * @param sub - replacement
     * @return the updated string
     */
    public static String substitute(final String input, final String pattern, final String sub) {
        StringBuilder ret = new StringBuilder(input.length());
        int start = 0;
        int index = -1;
        final int length = pattern.length();
        while ((index = input.indexOf(pattern, start)) >= start) {
            ret.append(input.substring(start, index));
            ret.append(sub);
            start = index + length;
        }
        ret.append(input.substring(start));
        return ret.toString();
    }
}
