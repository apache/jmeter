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

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.jmeter.util.JMeterUtils;

/**
 * The class TimeHelper provides helper functions to ease time displaying.
 *
 * @since 3.0
 */
public class TimeHelper {

    private static final String TIMESTAMP_FORMAT_PROPERTY =
            "jmeter.save.saveservice.timestamp_format";

    public static String time(long t) {
        long h = t / 3600000;
        t %= 3600000;
        long m = t / 60000;
        t %= 60000;
        long s = t / 1000;
        t %= 1000;
        long ms = t;
        if (h > 0) {
            return h + "h " + m + "m " + s + "s " + ms + " ms";
        }
        if (m > 0) {
            return m + "m " + s + "s " + ms + " ms";
        }
        if (s > 0) {
            return s + "s " + ms + " ms";
        }
        if (ms > 0) {
            return ms + "ms";
        }
        return "0 ms";
    }

    /**
     * Format the specified time stamp to string using JMeter properties.
     *
     * @param timeStamp
     *            the time stamp
     * @return the string
     */
    public static String formatTimeStamp(long timeStamp) {
        return formatTimeStamp(timeStamp,
                JMeterUtils.getProperty(TIMESTAMP_FORMAT_PROPERTY));
    }

    /**
     * Format the specified time stamp to string using the format.
     *
     * @param timeStamp
     *            the time stamp
     * @param format
     *            the format
     * @return the string
     */
    public static String formatTimeStamp(long timeStamp, String format) {
        SimpleDateFormat dateFormat = format != null ? new SimpleDateFormat(
                format) : new SimpleDateFormat();
        return dateFormat.format(new Date(timeStamp));
    }
}
