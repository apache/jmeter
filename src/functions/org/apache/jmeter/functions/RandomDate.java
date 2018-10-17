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

package org.apache.jmeter.functions;

import java.time.LocalDate;
import java.time.Year;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.util.JMeterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

/**
 * RandomDate Function generates a date in a specific range
 *
 * Parameters: 
 * <ul>
 *  <li>Time format @see <a href="https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html">DateTimeFormatter</a>
 *      (optional - defaults to yyyy-MM-dd)</li>
 *  <li>Start date formated as first param (optional - defaults to now)</li>
 *  <li>End date</li>
 *  <li>Locale for the format (optional)</li> 
 *  <li>variable name (optional)</li>
 * </ul>
 * Returns a formatted date with the specified number of (days, month, year)
 * Value is also saved in the variable for later re-use.
 * 
 * @since 3.3
 */
public class RandomDate extends AbstractFunction {

    private static final Logger log = LoggerFactory.getLogger(RandomDate.class);

    private static final String KEY = "__RandomDate"; // $NON-NLS-1$

    private static final int MIN_PARAMETER_COUNT = 3;

    private static final int MAX_PARAMETER_COUNT = 5;

    private static final List<String> desc = Arrays.asList(JMeterUtils.getResString("time_format_random"),
            JMeterUtils.getResString("date_start"), JMeterUtils.getResString("date_end"),
            JMeterUtils.getResString("locale_format"), JMeterUtils.getResString("function_name_paropt"));

    // Ensure that these are set, even if no parameters are provided
    private Locale locale = JMeterUtils.getLocale(); // $NON-NLS-1$
    private ZoneId systemDefaultZoneID = ZoneId.systemDefault(); // $NON-NLS-1$
    private CompoundVariable[] values;

    private static final class LocaleFormatObject {

        private String format;
        private Locale locale;

        public LocaleFormatObject(String format, Locale locale) {
            this.format = format;
            this.locale = locale;
        }

        public String getFormat() {
            return format;
        }

        public Locale getLocale() {
            return locale;
        }

        @Override
        public int hashCode() {
            return format.hashCode() + locale.hashCode();
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof LocaleFormatObject)) {
                return false;
            }

            LocaleFormatObject otherError = (LocaleFormatObject) other;
            return format.equals(otherError.getFormat())
                    && locale.getDisplayName().equals(otherError.getLocale().getDisplayName());
        }

        /**
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return "LocaleFormatObject [format=" + format + ", locale=" + locale + "]";
        }

    }

    /** Date time format cache handler **/
    private Cache<LocaleFormatObject, DateTimeFormatter> dateRandomFormatterCache;

    public RandomDate() {
        super();
    }

    /** {@inheritDoc} */
    @Override
    public String execute(SampleResult previousResult, Sampler currentSampler) throws InvalidVariableException {
        DateTimeFormatter formatter;
        if(values.length>3) {
            String localeAsString = values[3].execute().trim();
            if (!localeAsString.trim().isEmpty()) {
                locale = LocaleUtils.toLocale(localeAsString);
            }
        }

        String format = values[0].execute().trim();
        if (!StringUtils.isEmpty(format)) {
            try {
                LocaleFormatObject lfo = new LocaleFormatObject(format, locale);
                formatter = dateRandomFormatterCache.get(lfo, key -> createFormatter((LocaleFormatObject) key));
            } catch (IllegalArgumentException ex) {
                log.error(
                        "Format date pattern '{}' is invalid (see https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html)",
                        format, ex); // $NON-NLS-1$
                return "";
            }
        } else {
            try {
                LocaleFormatObject lfo = new LocaleFormatObject("yyyy-MM-dd", locale);
                formatter = dateRandomFormatterCache.get(lfo, key -> createFormatter((LocaleFormatObject) key));
            } catch (IllegalArgumentException ex) {
                log.error(
                        "Format date pattern '{}' is invalid (see https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html)",
                        format, ex); // $NON-NLS-1$
                return "";
            }
        }

        String dateStart = values[1].execute().trim();
        long localStartDate = 0;
        if (!dateStart.isEmpty()) {
            try {
                localStartDate = LocalDate.parse(dateStart, formatter).toEpochDay();
            } catch (DateTimeParseException | NumberFormatException ex) {
                log.error("Failed to parse Start Date '{}'", dateStart, ex); // $NON-NLS-1$
            }
        } else {
            try {
                localStartDate = LocalDate.now(systemDefaultZoneID).toEpochDay();
            } catch (DateTimeParseException | NumberFormatException ex) {
                log.error("Failed to create current date '{}'", dateStart, ex); // $NON-NLS-1$
            }
        }
        long localEndDate = 0;
        String dateEnd = values[2].execute().trim();
        try {
            localEndDate = LocalDate.parse(dateEnd, formatter).toEpochDay();
        } catch (DateTimeParseException | NumberFormatException ex) {
            log.error("Failed to parse End date '{}'", dateEnd, ex); // $NON-NLS-1$
        }
        
        // Generate the random date
        String dateString = "";
        if (localEndDate < localStartDate) {
            log.error("End Date '{}' must be greater than Start Date '{}'", dateEnd, dateStart); // $NON-NLS-1$
        } else {
            long randomDay = ThreadLocalRandom.current().nextLong(localStartDate, localEndDate);
            try {
                dateString = LocalDate.ofEpochDay(randomDay).format(formatter);
            } catch (DateTimeParseException | NumberFormatException ex) {
                log.error("Failed to generate random date '{}'", randomDay, ex); // $NON-NLS-1$
            }
            addVariableValue(dateString, values, 4);
        }
        return dateString;
    }

    private DateTimeFormatter createFormatter(LocaleFormatObject format) {
        log.debug("Create a new instance of DateTimeFormatter for format '{}' in the cache", format);
        return new DateTimeFormatterBuilder().appendPattern(format.getFormat())
                .parseDefaulting(ChronoField.DAY_OF_MONTH, 1).parseDefaulting(ChronoField.MONTH_OF_YEAR, 1)
                .parseDefaulting(ChronoField.YEAR_OF_ERA, Year.now().getValue()).toFormatter(format.getLocale());

    }

    /** {@inheritDoc} */
    @Override
    public void setParameters(Collection<CompoundVariable> parameters) throws InvalidVariableException {

        checkParameterCount(parameters, MIN_PARAMETER_COUNT, MAX_PARAMETER_COUNT);
        values = parameters.toArray(new CompoundVariable[parameters.size()]);
        // Create the cache
        if (dateRandomFormatterCache == null) {
            dateRandomFormatterCache = Caffeine.newBuilder().maximumSize(100).build();
        }
    }

    /** {@inheritDoc} */
    @Override
    public String getReferenceKey() {
        return KEY;
    }

    /** {@inheritDoc} */
    @Override
    public List<String> getArgumentDesc() {
        return desc;
    }

}
