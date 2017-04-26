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

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.JMeterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * timeShifting Function permit to shift a date
 *
 * Parameters: - format date @see
 * https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html
 * (optional - defaults to epoch time in millisecond) - date to shift formated
 * as first param (optional - defaults now) - amount of (seconds / minutes /
 * hours / days / months ) to add (optional - default nothing is add ) -
 * variable name ( optional )
 *
 * Returns: - Returns a formated date with the specified number of (seconds /
 * minutes / hours / days / months ) added. - value is also saved in the
 * variable for later re-use.
 *
 * @since 3.3
 */
public class TimeShiftingFunction extends AbstractFunction {
    private static final Logger log = LoggerFactory.getLogger(TimeShiftingFunction.class);

    private static final String KEY = "__timeShifting"; // $NON-NLS-1$

    private static final List<String> desc = new LinkedList<>();

    static {
        desc.add(JMeterUtils.getResString("time_format_shift")); //$NON-NLS-1$
        desc.add(JMeterUtils.getResString("date_to_shift")); //$NON-NLS-1$
        desc.add(JMeterUtils.getResString("value_to_shift")); //$NON-NLS-1$
        desc.add(JMeterUtils.getResString("function_name_paropt")); //$NON-NLS-1$
    }

    // Ensure that these are set, even if no paramters are provided
    private String format = ""; //$NON-NLS-1$
    private String dateToShift = ""; //$NON-NLS-1$
    private String shift = ""; //$NON-NLS-1$
    private String variable = ""; //$NON-NLS-1$

    public TimeShiftingFunction() {
        super();
    }

    /** {@inheritDoc} */
    @Override
    public String execute(SampleResult previousResult, Sampler currentSampler) throws InvalidVariableException {
        String dateString;
        LocalDateTime localDateTimeToShift = LocalDateTime.now(ZoneId.systemDefault());
        DateTimeFormatter formatter = null;
        if (!StringUtils.isEmpty(format)) {
            try {
                formatter = new DateTimeFormatterBuilder().appendPattern(format)
                        .parseDefaulting(ChronoField.NANO_OF_SECOND, 0).parseDefaulting(ChronoField.MILLI_OF_SECOND, 0)
                        .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0).parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
                        .parseDefaulting(ChronoField.HOUR_OF_DAY, 0).parseDefaulting(ChronoField.DAY_OF_MONTH, 1)
                        .parseDefaulting(ChronoField.MONTH_OF_YEAR, 1)
                        .parseDefaulting(ChronoField.YEAR_OF_ERA, Year.now().getValue())
                        .toFormatter(JMeterUtils.getLocale());
            } catch (IllegalArgumentException ex) {
                log.error("Pattern '{}' is invalid", format, ex); // $NON-NLS-1$
            }
        }

        if (!dateToShift.isEmpty()) {
            try {
                if (formatter != null) {
                    localDateTimeToShift = LocalDateTime.parse(dateToShift, formatter);
                } else {
                    localDateTimeToShift = LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(dateToShift)),
                            ZoneId.systemDefault());
                }
            } catch (DateTimeParseException | NumberFormatException ex) {
                log.error("Failed to parse the date '{}' to shift", dateToShift, ex); // $NON-NLS-1$
            }
        }

        // Check amount value to shift
        if (!StringUtils.isEmpty(shift)) {
            int strLength = shift.length();
            Character lastChar = shift.charAt(strLength - 1);
            try {
                long amount = Long.parseLong(shift.substring(0, strLength - 1));
                log.debug("Add '{}' period of time on '{}'", shift, localDateTimeToShift);
                switch (lastChar) {
                case 's':
                    localDateTimeToShift = localDateTimeToShift.plusSeconds(amount);
                    break;
                case 'm':
                    localDateTimeToShift = localDateTimeToShift.plusMinutes(amount);
                    break;
                case 'H':
                    localDateTimeToShift = localDateTimeToShift.plusHours(amount);
                    break;
                case 'd':
                    localDateTimeToShift = localDateTimeToShift.plusDays(amount);
                    break;
                case 'M':
                    localDateTimeToShift = localDateTimeToShift.plusMonths(amount);
                    break;
                }
            } catch (NumberFormatException nfe) {
                log.warn("Failed to parse the amount '{}' of time to add", shift, nfe); // $NON-NLS-1$
            }
        }

        if (formatter != null) {
            dateString = localDateTimeToShift.format(formatter);
        } else {
            ZoneOffset offset = ZoneOffset.systemDefault().getRules().getOffset(localDateTimeToShift);
            dateString = String.valueOf(localDateTimeToShift.toInstant(offset).toEpochMilli());
        }

        if (variable.length() > 0) {
            JMeterVariables vars = getVariables();
            if (vars != null) {// vars will be null on TestPlan
                vars.put(variable, dateString);
            }
        }
        return dateString;
    }

    /** {@inheritDoc} */
    @Override
    public void setParameters(Collection<CompoundVariable> parameters) throws InvalidVariableException {

        checkParameterCount(parameters, 0, 4);
        Object[] values = parameters.toArray();

        format = ((CompoundVariable) values[0]).execute();
        dateToShift = ((CompoundVariable) values[1]).execute();
        shift = ((CompoundVariable) values[2]).execute();
        variable = ((CompoundVariable) values[3]).execute().trim();

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
