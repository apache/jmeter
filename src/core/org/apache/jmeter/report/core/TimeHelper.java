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
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.apache.jmeter.util.JMeterUtils;

/**
 * The class TimeHelper provides helper functions to ease time displaying.
 * 
 * @since 2.14
 */
public class TimeHelper {

    private static final String MILLISECONDS_FORMAT = "ms";
    private static final String TIMESTAMP_FORMAT_PROPERTY = "jmeter.save.saveservice.timestamp_format";

    public static final String time(long t) {
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
     * <p>
     * Format the specified duration in millisecond to a string like this one :
     * "<i>d</i> days <i>h</i> hours <i>m</i> min <i>s</i> sec <i>ms</i> ms".
     * </p>
     * <p>
     * If d, h, m, s or ms are equals to zero, they're disappeared from the
     * string.
     *
     * @param msDuration
     *            the millisecond duration
     * @param spaced
     *            define if units are spaced
     * @return the formatted string
     */
    public static final String formatDuration(long msDuration, boolean spaced) {
	if (msDuration < 0)
	    throw new IllegalArgumentException(
		    "Duration must be greater than zero.");

	// Define each component of the duration
	long days = TimeUnit.MILLISECONDS.toDays(msDuration);
	msDuration -= TimeUnit.DAYS.toMillis(days);
	long hours = TimeUnit.MILLISECONDS.toHours(msDuration);
	msDuration -= TimeUnit.HOURS.toMillis(hours);
	long minutes = TimeUnit.MILLISECONDS.toMinutes(msDuration);
	msDuration -= TimeUnit.MINUTES.toMillis(minutes);
	long seconds = TimeUnit.MILLISECONDS.toSeconds(msDuration);
	msDuration -= TimeUnit.SECONDS.toMillis(seconds);

	// Append components if not equals to zero
	ArrayList<String> items = new ArrayList<String>(5);

	if (days > 0)
	    items.add(String.format(spaced ? "%d day(s)" : "%dday(s)", days));

	if (hours > 0)
	    items.add(String.format(spaced ? "%d hour(s)" : "%dhour(s)", hours));

	if (minutes > 0)
	    items.add(String.format(spaced ? "%d min" : "%dmin", minutes));

	if (seconds > 0)
	    items.add(String.format(spaced ? "%d sec" : "%dsec", seconds));

	if (msDuration > 0)
	    items.add(String.format(spaced ? "%d ms" : "%dms", msDuration));

	// Build the string with a space character between components
	StringBuilder builder = new StringBuilder();
	int count = items.size() - 1;
	for (int index = 0; index < count; index++) {
	    builder.append(items.get(index) + " ");
	}
	builder.append(items.get(count));

	return builder.toString();
    }

    /**
     * <p>
     * Format the specified duration in millisecond to a string like this :
     * "<i>d</i> days <i>h</i> hours <i>m</i> min <i>s</i> sec <i>ms</i> ms".
     * </p>
     * <p>
     * If d, h, m, s or ms are equals to zero, they're disappeared from the
     * string.
     *
     * @param msDuration
     *            the duration in millisecond
     * @return the formated string
     */
    public static final String formatDuration(long msDuration) {
	return formatDuration(msDuration, true);
    }

    /**
     * Format the specified time stamp to string using JMeter properties.
     *
     * @param timeStamp
     *            the time stamp
     * @return the string
     */
    public static final String formatTimeStamp(long timeStamp) {
	String format = JMeterUtils.getPropDefault(TIMESTAMP_FORMAT_PROPERTY,
	        MILLISECONDS_FORMAT);
	return (MILLISECONDS_FORMAT.equalsIgnoreCase(format)) ? String
	        .valueOf(timeStamp) : formatTimeStamp(timeStamp, format);
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
    public static final String formatTimeStamp(long timeStamp, String format) {
	SimpleDateFormat dateFormat = new SimpleDateFormat(format);
	return dateFormat.format(new Date(timeStamp));
    }
}
