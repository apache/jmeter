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
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jmeter.functions;

import org.apache.commons.lang3.StringUtils;

/**
 * utility class which contains utility methods.
 *
 */
public class CorrelationFunction {

    // Initialize constants variables
    private static final String UNDERSCORE = "_";

    private CorrelationFunction() {}

    /**
     * extract argument
     * @param argument
     * @return
     */
    public static String extractVariable(String argument) {

        String[] arguments = argument.split(UNDERSCORE);
        int numericStringIndex = -1;
        StringBuilder result = new StringBuilder();

        for (int i = arguments.length - 1; i >= 0; i--) {
            if (StringUtils.isNumeric(arguments[i])) {
                numericStringIndex = i;
                break;
            }
        }
        for (int i = numericStringIndex + 1; i < arguments.length; i++) {
            result.append(arguments[i]);
            if (i != arguments.length - 1) {
                result.append(UNDERSCORE);
            }
        }
        return result.toString();
    }

}
