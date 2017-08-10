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
import java.time.LocalDate;
import java.time.Year;
import java.time.ZoneId;
import java.time.ZoneOffset;
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
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.JMeterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

/**
 * RandomDate Function permit to generate a date in a specific range
 *
 * Parameters: - format date @see
 * https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html
 * (optional - defaults to epoch time in millisecond) - date to shift formated
 * as first param (optional - defaults now) - amount of (seconds, minutes,
 * hours, days ) to add (optional - default nothing is add ) - a string of the
 * locale for the format ( optional ) - variable name ( optional )
 *
 * Returns: a formatted date with the specified number of (seconds, minutes,
 * hours, days or months ) added. - value is also saved in the variable for
 * later re-use.
 *
 * @since 3.3
 */

public class RandomDate extends AbstractFunction {

	private static final Logger log = LoggerFactory.getLogger(RandomDate.class);

	private static final String KEY = "__RandomDate"; // $NON-NLS-1$

	private static final List<String> desc = Arrays.asList(JMeterUtils.getResString("time_format_random"),
	        JMeterUtils.getResString("date_start"), JMeterUtils.getResString("date_end"),
	        JMeterUtils.getResString("locale_format"), JMeterUtils.getResString("function_name_paropt"));

	// Ensure that these are set, even if no paramters are provided
	private String format = ""; //$NON-NLS-1$
	private CompoundVariable dateStartCompound; // $NON-NLS-1$
	private CompoundVariable dateEndCompound; // $NON-NLS-1$
	private Locale locale = JMeterUtils.getLocale(); // $NON-NLS-1$
	private String variableName = ""; //$NON-NLS-1$
	private ZoneId systemDefaultZoneID = ZoneId.systemDefault();

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
	private Cache<LocaleFormatObject, DateTimeFormatter> dateFormatterCache = null;

	public RandomDate() {
		super();
	}

	/** {@inheritDoc} */
	@Override
	public String execute(SampleResult previousResult, Sampler currentSampler) throws InvalidVariableException {
		String dateStart = dateStartCompound.execute().trim();
		String dateEnd = dateEndCompound.execute().trim();
		long localStartDate = 0;
		long localEndDate = 0;

		DateTimeFormatter formatter = null;
		if (!StringUtils.isEmpty(format)) {
			try {
				LocaleFormatObject lfo = new LocaleFormatObject(format, locale);
				formatter = dateFormatterCache.get(lfo, key -> createFormatter((LocaleFormatObject) key));
			} catch (IllegalArgumentException ex) {
				log.error(
				        "Format date pattern '{}' is invalid (see https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html)",
				        format, ex); // $NON-NLS-1$
				return "";
			}
		} else {
			try {
				LocaleFormatObject lfo = new LocaleFormatObject("yyyy-MM-dd", locale);
				formatter = dateFormatterCache.get(lfo, key -> createFormatter((LocaleFormatObject) key));
			} catch (IllegalArgumentException ex) {
				log.error(
				        "Format date pattern '{}' is invalid (see https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html)",
				        format, ex); // $NON-NLS-1$
				return "";
			}
		}

		if (!dateStart.isEmpty()) {
			try {
					localStartDate = LocalDate.parse(dateStart, formatter).toEpochDay();
			} catch (DateTimeParseException | NumberFormatException ex) {
				log.error("Failed to parse the date '{}' to shift with formatter '{}'", dateStart, formatter, ex); // $NON-NLS-1$
			}
		} else {
			try {
				localStartDate = LocalDate.now(systemDefaultZoneID).toEpochDay();
			} catch (DateTimeParseException | NumberFormatException ex) {
				log.error("Failed to parse the date '{}' to shift with formatter '{}'", dateStart, formatter, ex); // $NON-NLS-1$
			}
		}

		try {
			localEndDate = LocalDate.parse(dateEnd, formatter).toEpochDay();
		} catch (DateTimeParseException | NumberFormatException ex) {
			log.error("Failed to parse the date '{}' to shift with formatter '{}'", dateEnd, formatter, ex); // $NON-NLS-1$
		}

		// Generate the random date
		String dateString="";
		long randomDay = ThreadLocalRandom.current().nextLong(localStartDate, localEndDate);
		try {
			dateString = LocalDate.ofEpochDay(randomDay).format(formatter);
		} catch (DateTimeParseException | NumberFormatException ex) {
			log.error("Failed to parse the date '{}' to shift with formatter '{}'", randomDay, formatter, ex); // $NON-NLS-1$
		}
		
		if (!StringUtils.isEmpty(variableName)) {
			JMeterVariables vars = getVariables();
			if (vars != null) {// vars will be null on TestPlan
				vars.put(variableName, dateString);
			}
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

		checkParameterCount(parameters, 4, 5);
		Object[] values = parameters.toArray();

		format = ((CompoundVariable) values[0]).execute().trim();
		dateStartCompound = (CompoundVariable) values[1];
		dateEndCompound = (CompoundVariable) values[2];
		if (values.length == 4) {
			variableName = ((CompoundVariable) values[3]).execute().trim();
		} else {
			String localeAsString = ((CompoundVariable) values[3]).execute().trim();
			if (!localeAsString.trim().isEmpty()) {
				locale = LocaleUtils.toLocale(localeAsString);
			}
			variableName = ((CompoundVariable) values[4]).execute().trim();
		}
		// Create the cache
		if (dateFormatterCache == null) {
			dateFormatterCache = Caffeine.newBuilder().maximumSize(100).build();
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

