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
package org.apache.jmeter.report.utils;

import org.apache.commons.lang3.StringUtils;

/**
 * @since 5.0
 */
public class MetricUtils {
    public static final String ASSERTION_FAILED = "Assertion failed"; //$NON-NLS-1$

    /**
     *
     */
    private MetricUtils() {
        super();
    }

    /**
     * Determine if the HTTP status code is successful or not i.e. in range 200
     * to 399 inclusive
     *
     * @param codeAsString
     *            status code to check
     * @return whether in range 200-399 or not
     */
    public static boolean isSuccessCode(String codeAsString) {
        if (StringUtils.isNumeric(codeAsString)) {
            try {
                int code = Integer.parseInt(codeAsString);
                return isSuccessCode(code);
            } catch (NumberFormatException ex) {
                return false;
            }
        }
        return false;
    }

    /**
     * @param code Response code
     * @return true if code is between 200 and 399 included
     */
    public static boolean isSuccessCode(int code) {
        return code >= 200 && code <= 399;
    }
}
