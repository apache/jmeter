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

package org.apache.jmeter.visualizers.backend;

import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.report.utils.MetricUtils;
import org.apache.jmeter.samplers.SampleResult;

/**
 * Object representing an error by a response code and response message
 * @since 3.3
 */
public class ErrorMetric {

    /**
     * Error code of a sample result, by example : "400"
     */
    private String responseCode = ""; // Never return null

    /**
     * Error response message of a sample result, , by example : "bad request"
     */
    private String responseMessage = ""; // Never return null

    public ErrorMetric() {
    }

    public ErrorMetric(SampleResult result) {
        if (MetricUtils.isSuccessCode(responseCode) ||
                (StringUtils.isEmpty(responseCode) &&
                        !StringUtils.isEmpty(result.getFirstAssertionFailureMessage()))) {
            responseCode = MetricUtils.ASSERTION_FAILED;
            responseMessage = result.getFirstAssertionFailureMessage();
        } else {
            responseCode = result.getResponseCode();
            responseMessage = result.getResponseMessage();
        }
    }

    /**
     * @return the response code , '0' if the code is empty
     */
    public String getResponseCode() {
        if (responseCode.isEmpty()) {
            return "0";
        } else {
            return responseCode.trim();
        }
    }

    /**
     * @return the response message, 'none' if the code is empty
     */
    public String getResponseMessage() {
        if (responseMessage == null || responseMessage.isEmpty()) {
            return "None";
        } else {
            return responseMessage.trim();
        }
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof ErrorMetric)) {
            return false;
        }

        ErrorMetric otherError = (ErrorMetric) other;
        return getResponseCode().equalsIgnoreCase(otherError.getResponseCode())
                && getResponseMessage().equalsIgnoreCase(otherError.getResponseMessage());

    }

    @Override
    public int hashCode() {
        int hash = 1;
        hash = 31 * hash + getResponseCode().toLowerCase(Locale.ROOT).hashCode();
        hash = 31 * hash + getResponseMessage().toLowerCase(Locale.ROOT).hashCode();
        return hash;
    }

}
