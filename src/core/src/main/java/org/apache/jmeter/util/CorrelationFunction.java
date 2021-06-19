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

package org.apache.jmeter.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class which contains utility methods to extract variables.
 *
 */
public class CorrelationFunction {

    private CorrelationFunction() {}

    /**
     * Extract argument from alias e.g token(1) to token
     *
     * @param argument alias
     * @return argument name
     */
    public static String extractVariable(String argument) {
        Pattern p = Pattern.compile("(.*?)\\(\\d*\\)");//$NON-NLS-1$
        Matcher m = p.matcher(argument);
        return m.matches() ? m.group(1) : argument;
    }

}
