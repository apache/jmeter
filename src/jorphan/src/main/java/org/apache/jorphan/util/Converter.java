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

package org.apache.jorphan.util;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Optional;
import java.util.StringTokenizer;
import java.util.stream.Stream;

/**
 * Converter utilities for TestBeans
 */
public class Converter {

    /**
     * Convert the given value object to an object of the given type
     *
     * @param value  object to convert
     * @param toType type to convert object to
     * @return converted object or original value if no conversion could be applied
     */
    public static Object convert(Object value, Class<?> toType) {
        Object convertedValue = value;
        if (value == null || toType == null) {
            convertedValue = ""; // TODO should we allow null for non-primitive types?
        } else if (toType.isAssignableFrom(value.getClass())) {
            convertedValue = value;
        } else if (toType.equals(float.class) || toType.equals(Float.class)) {
            convertedValue = getFloat(value);
        } else if (toType.equals(double.class) || toType.equals(Double.class)) {
            convertedValue = getDouble(value);
        } else if (toType.equals(String.class)) {
            convertedValue = getString(value);
        } else if (toType.equals(int.class) || toType.equals(Integer.class)) {
            convertedValue = getInt(value);
        } else if (toType.equals(char.class) || toType.equals(Character.class)) {
            convertedValue = getChar(value);
        } else if (toType.equals(long.class) || toType.equals(Long.class)) {
            convertedValue = getLong(value);
        } else if (toType.equals(boolean.class) || toType.equals(Boolean.class)) {
            convertedValue = getBoolean(value);
        } else if (toType.equals(java.util.Date.class)) {
            convertedValue = getDate(value);
        } else if (toType.equals(Calendar.class)) {
            convertedValue = getCalendar(value);
        } else if (toType.equals(File.class)) {
            convertedValue = getFile(value);
        } else if (toType.equals(Class.class)) {
            try {
                convertedValue = Class.forName(value.toString());
            } catch (Exception ignored) {
                // Intentionally left blank
            }
        }
        return convertedValue;
    }

    /**
     * Converts the given object to a calendar object. Defaults to the
     * <code>defaultValue</code> if the given object can't be converted.
     *
     * @param date
     *            object that should be converted to a {@link Calendar}
     * @param defaultValue
     *            default value that will be returned if <code>date</code> can
     *            not be converted
     * @return {@link Calendar} representing the given <code>date</code> or
     *         <code>defaultValue</code> if conversion failed
     */
    public static Calendar getCalendar(Object date, Calendar defaultValue) {
        Calendar cal = new GregorianCalendar();
        if (date instanceof java.util.Date dateValue) {
            cal.setTime(dateValue);
            return cal;
        } else if (date != null) {
            Optional<Date> d = tryToParseDate(date);
            if (!d.isPresent()) {
                return defaultValue;
            }
            cal.setTime(d.get());
        } else {
            cal = defaultValue;
        }
        return cal;
    }

    /**
     * Converts the given object to a calendar object. Defaults to a calendar
     * using the current time if the given object can't be converted.
     *
     * @param o
     *            object that should be converted to a {@link Calendar}
     * @return {@link Calendar} representing the given <code>o</code> or a new
     *         {@link GregorianCalendar} using the current time if conversion
     *         failed
     */
    public static Calendar getCalendar(Object o) {
        return getCalendar(o, new GregorianCalendar());
    }

    /**
     * Converts the given object to a {@link Date} object. Defaults to the
     * current time if the given object can't be converted.
     *
     * @param date
     *            object that should be converted to a {@link Date}
     * @return {@link Date} representing the given <code>date</code> or
     *         the current time if conversion failed
     */
    public static Date getDate(Object date) {
        return getDate(date, Calendar.getInstance().getTime());
    }

    /**
     * Converts the given object to a {@link Date} object. Defaults to the
     * <code>defaultValue</code> if the given object can't be converted.
     *
     * @param date
     *            object that should be converted to a {@link Date}
     * @param defaultValue
     *            default value that will be returned if <code>date</code> can
     *            not be converted
     * @return {@link Date} representing the given <code>date</code> or
     *         <code>defaultValue</code> if conversion failed
     */
    public static Date getDate(Object date, Date defaultValue) {
        if (date instanceof java.util.Date dateValue) {
            return dateValue;
        } else if (date != null) {
            return tryToParseDate(date).orElse(defaultValue);
        } else {
            return defaultValue;
        }
    }

    private static Optional<Date> tryToParseDate(Object date) {
        return Stream.of(DateFormat.SHORT, DateFormat.MEDIUM, DateFormat.LONG, DateFormat.FULL)
                .map(DateFormat::getDateInstance)
                .map(formatter -> tryToParseDate(formatter, date.toString()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
    }

    private static Optional<Date> tryToParseDate(DateFormat formatter, String dateString) {
        try {
            return Optional.of(formatter.parse(dateString));
        } catch (ParseException e) {
            return Optional.empty();
        }
    }

    /**
     * Convert object to float, or <code>defaultValue</code> if conversion failed
     *
     * @param o object to convert
     * @param defaultValue default value to use, when conversion failed
     * @return converted float or <code>defaultValue</code> if conversion failed
     */
    public static float getFloat(Object o, float defaultValue) {
        if (o == null) {
            return defaultValue;
        }
        if (o instanceof Number number) {
            return number.floatValue();
        }
        try {
            return Float.parseFloat(o.toString());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Convert object to float, or <code>0</code> if conversion
     * failed
     *
     * @param o
     *            object to convert
     * @return converted float or <code>0</code> if conversion
     *         failed
     */
    public static float getFloat(Object o) {
        return getFloat(o, 0);
    }

    /**
     * Convert object to double, or <code>defaultValue</code> if conversion
     * failed
     *
     * @param o
     *            object to convert
     * @param defaultValue
     *            default value to use, when conversion failed
     * @return converted double or <code>defaultValue</code> if conversion
     *         failed
     */
    public static double getDouble(Object o, double defaultValue) {
        try {
            if (o == null) {
                return defaultValue;
            }
            if (o instanceof Number number) {
                return number.doubleValue();
            }
            return Double.parseDouble(o.toString());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Convert object to double, or <code>0</code> if conversion
     * failed
     *
     * @param o
     *            object to convert
     * @return converted double or <code>0</code> if conversion
     *         failed
     */
    public static double getDouble(Object o) {
        return getDouble(o, 0);
    }

    /**
     * Convert object to boolean, or <code>false</code> if conversion
     * failed
     *
     * @param o
     *            object to convert
     * @return converted boolean or <code>false</code> if conversion
     *         failed
     */
    public static boolean getBoolean(Object o) {
        return getBoolean(o, false);
    }

    /**
     * Convert object to boolean, or <code>defaultValue</code> if conversion
     * failed
     *
     * @param o
     *            object to convert
     * @param defaultValue
     *            default value to use, when conversion failed
     * @return converted boolean or <code>defaultValue</code> if conversion
     *         failed
     */
    public static boolean getBoolean(Object o, boolean defaultValue) {
        if (o == null) {
            return defaultValue;
        } else if (o instanceof Boolean boolValue) {
            return boolValue;
        }
        return Boolean.parseBoolean(o.toString());
    }

    /**
     * Convert object to integer, return <code>defaultValue</code> if object is not
     * convertible or is <code>null</code>.
     *
     * @param o
     *            object to convert
     * @param defaultValue
     *            default value to be used when no conversion can be done
     * @return converted int or default value if conversion failed
     */
    public static int getInt(Object o, int defaultValue) {
        try {
            if (o == null) {
                return defaultValue;
            }
            if (o instanceof Number number) {
                return number.intValue();
            }
            return Integer.parseInt(o.toString());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Convert object to char, or ' ' if no conversion can
     * be applied
     *
     * @param o
     *            object to convert
     * @return converted char or ' ' if conversion failed
     */
    public static char getChar(Object o) {
        return getChar(o, ' ');
    }

    /**
     * Convert object to char, or <code>defaultValue</code> if no conversion can
     * be applied
     *
     * @param o
     *            object to convert
     * @param defaultValue
     *            default value to use, when conversion failed
     * @return converted char or <code>defaultValue</code> if conversion failed
     */
    public static char getChar(Object o, char defaultValue) {
        try {
            if (o == null) {
                return defaultValue;
            }
            if (o instanceof Character charValue) {
                return charValue;
            } else if (o instanceof Byte byteValue) {
                return (char) byteValue.byteValue();
            } else if (o instanceof Integer intValue) {
                return (char) intValue.intValue();
            } else {
                String s = o.toString();
                if (s.length() > 0) {
                    return o.toString().charAt(0);
                }
                return defaultValue;
            }
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * Converts object to an integer, defaults to <code>0</code> if object is
     * not convertible or is <code>null</code>.
     *
     * @param o
     *            object to convert
     * @return converted int, or <code>0</code> if conversion failed
     */
    public static int getInt(Object o) {
        return getInt(o, 0);
    }

    /**
     * Converts object to a long, return <code>defaultValue</code> if object is
     * not convertible or is <code>null</code>.
     *
     * @param o
     *            object to convert
     * @param defaultValue
     *            default value to use, when conversion failed
     * @return converted long or <code>defaultValue</code> when conversion
     *         failed
     */
    public static long getLong(Object o, long defaultValue) {
        try {
            if (o == null) {
                return defaultValue;
            }
            if (o instanceof Number number) {
                return number.longValue();
            }
            return Long.parseLong(o.toString());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Converts object to a long, defaults to <code>0</code> if object is not
     * convertible or is <code>null</code>
     *
     * @param o
     *            object to convert
     * @return converted long or <code>0</code> if conversion failed
     */
    public static long getLong(Object o) {
        return getLong(o, 0);
    }

    /**
     * Format a date using a given pattern
     *
     * @param date
     *            date to format
     * @param pattern
     *            pattern to use for formatting
     * @return formatted date, or empty string if date was <code>null</code>
     * @throws IllegalArgumentException
     *             when <code>pattern</code> is invalid
     */
    public static String formatDate(Date date, String pattern) {
        if (date == null) {
            return "";
        }
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        return format.format(date);
    }

    /**
     * Format a date using a given pattern
     *
     * @param date
     *            date to format
     * @param pattern
     *            pattern to use for formatting
     * @return formatted date, or empty string if date was <code>null</code>
     * @throws IllegalArgumentException
     *             when <code>pattern</code> is invalid
     */
    public static String formatDate(java.sql.Date date, String pattern) {
        if (date == null) {
            return "";
        }
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        return format.format(date);
    }

    /**
     * Format a date using a given pattern
     *
     * @param date
     *            date to format
     * @param pattern
     *            pattern to use for formatting
     * @return formatted date, or empty string if date was <code>null</code>
     * @throws IllegalArgumentException
     *             when <code>pattern</code> is invalid
     */
    public static String formatDate(String date, String pattern) {
        return formatDate(getCalendar(date, null), pattern);
    }

    /**
     * Format a date using a given pattern
     *
     * @param date
     *            date to format
     * @param pattern
     *            pattern to use for formatting
     * @return formatted date, or empty string if date was <code>null</code>
     * @throws IllegalArgumentException
     *             when <code>pattern</code> is invalid
     */
    public static String formatDate(Calendar date, String pattern) {
        return formatCalendar(date, pattern);
    }

    /**
     * Format a calendar using a given pattern
     *
     * @param date
     *            calendar to format
     * @param pattern
     *            pattern to use for formatting
     * @return formatted date, or empty string if date was <code>null</code>
     * @throws IllegalArgumentException
     *             when <code>pattern</code> is invalid
     */
    public static String formatCalendar(Calendar date, String pattern) {
        if (date == null) {
            return "";
        }
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        return format.format(date.getTime());
    }

    /**
     * Converts object to a String, return <code>defaultValue</code> if object
     * is <code>null</code>.
     *
     * @param o
     *            object to convert
     * @param defaultValue
     *            default value to use when conversion failed
     * @return converted String or <code>defaultValue</code> when conversion
     *         failed
     */
    public static String getString(Object o, String defaultValue) {
        if (o == null) {
            return defaultValue;
        }
        return o.toString();
    }

    /**
     * Replace newlines "\n" with <code>insertion</code>
     *
     * @param v
     *            String in which the newlines should be replaced
     * @param insertion
     *            new string which should be used instead of "\n"
     * @return new string with newlines replaced by <code>insertion</code>
     */
    public static String insertLineBreaks(String v, String insertion) {
        if (v == null) {
            return "";
        }
        StringBuilder replacement = new StringBuilder();
        StringTokenizer tokens = new StringTokenizer(v, "\n", true);
        while (tokens.hasMoreTokens()) {
            String token = tokens.nextToken();
            if (token.compareTo("\n") == 0) {
                replacement.append(insertion);
            } else {
                replacement.append(token);
            }
        }
        return replacement.toString();
    }

    /**
     * Converts object to a String, defaults to empty string if object is null.
     *
     * @param o
     *            object to convert
     * @return converted String or empty string when conversion failed
     */
    public static String getString(Object o) {
        return getString(o, "");
    }

    /**
     * Converts an object to a {@link File}
     *
     * @param o
     *            object to convert (must be a {@link String} or a {@link File})
     * @return converted file
     * @throws IllegalArgumentException
     *             when object can not be converted
     */
    public static File getFile(Object o){
        if (o instanceof File file) {
            return file;
        }
        if (o instanceof String str) {
            return new File(str);
        }
        throw new IllegalArgumentException("Expected String or file, actual "+o.getClass().getName());
    }
}
