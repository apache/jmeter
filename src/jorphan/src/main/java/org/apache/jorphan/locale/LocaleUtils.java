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

package org.apache.jorphan.locale;

import java.util.Locale;

import org.apache.jorphan.util.StringUtilities;

/**
 * Code was copied from commons-lang3:3.19.0. Deprecated {@code new Locale(...)} have been
 * refactored to {@code new Locale.Builder()...build()} calls
 */
public class LocaleUtils {
    /** The underscore character {@code '}{@value}{@code '}. */
    private static final char UNDERSCORE = '_';

    /** The dash character {@code '}{@value}{@code '}. */
    private static final char DASH = '-';

    private LocaleUtils() {}

    /**
     * Tests whether the given String is a ISO 639 compliant language code.
     *
     * @param str the String to check.
     * @return true, if the given String is a ISO 639 compliant language code.
     */
    private static boolean isISO639LanguageCode(final String str) {
        return StringUtilities.isAllLowerCase(str) && (str.length() == 2 || str.length() == 3);
    }

    /**
     * Tests whether the given String is a ISO 3166 alpha-2 country code.
     *
     * @param str the String to check
     * @return true, is the given String is a ISO 3166 compliant country code.
     */
    private static boolean isISO3166CountryCode(final String str) {
        return str.length() == 2 && StringUtilities.isAllUpperCase(str);
    }

    /**
     * TestsNo whether the given String is a UN M.49 numeric area code.
     *
     * @param str the String to check
     * @return true, is the given String is a UN M.49 numeric area code.
     */
    private static boolean isNumericAreaCode(final String str) {
        return str.length() == 3 && StringUtilities.isNumeric(str);
    }

    /**
     * Tries to parse a Locale from the given String.
     *
     * <p>See {@link Locale} for the format.
     *
     * @param str the String to parse as a Locale.
     * @return a Locale parsed from the given String.
     * @throws IllegalArgumentException if the given String cannot be parsed.
     * @see Locale
     */
    private static Locale parseLocale(final String str) {
        if (isISO639LanguageCode(str)) {
            return new Locale.Builder().setLanguage(str).build();
        }
        final int limit = 3;
        final char separator = str.indexOf(UNDERSCORE) != -1 ? UNDERSCORE : DASH;
        final String[] segments = str.split(String.valueOf(separator), 3);
        final String language = segments[0];
        if (segments.length == 2) {
            final String country = segments[1];
            if ((isISO639LanguageCode(language) && isISO3166CountryCode(country))
                    || isNumericAreaCode(country)) {
                return new Locale.Builder().setLanguage(language).setRegion(country).build();
            }
        } else if (segments.length == limit) {
            final String country = segments[1];
            final String variant = segments[2];
            if (isISO639LanguageCode(language)
                    && (country.isEmpty() || isISO3166CountryCode(country) || isNumericAreaCode(country))
                    && !variant.isEmpty()) {
                return new Locale.Builder()
                        .setLanguage(language)
                        .setRegion(country)
                        .setVariant(variant)
                        .build();
            }
        }
        throw new IllegalArgumentException("Invalid locale format: " + str);
    }

    /**
     * Converts a String to a Locale.
     *
     * <p>This method takes the string format of a locale and creates the locale object from it.
     *
     * <pre>
     *   LocaleUtils.toLocale("")           = new Locale("", "")
     *   LocaleUtils.toLocale("en")         = new Locale("en", "")
     *   LocaleUtils.toLocale("en_GB")      = new Locale("en", "GB")
     *   LocaleUtils.toLocale("en-GB")      = new Locale("en", "GB")
     *   LocaleUtils.toLocale("en_001")     = new Locale("en", "001")
     *   LocaleUtils.toLocale("en_GB_xxx")  = new Locale("en", "GB", "xxx")   (#)
     * </pre>
     *
     * <p>(#) The behavior of the JDK variant constructor changed between JDK1.3 and JDK1.4. In
     * JDK1.3, the constructor upper cases the variant, in JDK1.4, it doesn't. Thus, the result from
     * getVariant() may vary depending on your JDK.
     *
     * <p>This method validates the input strictly. The language code must be lowercase. The country
     * code must be uppercase. The separator must be an underscore or a dash. The length must be
     * correct.
     *
     * @param str the locale String to convert, null returns null
     * @return a Locale, null if null input
     * @throws IllegalArgumentException if the string is an invalid format
     * @see Locale#forLanguageTag(String)
     */
    public static Locale toLocale(final String str) {
        if (str == null) {
            // TODO Should this return the default locale?
            return null;
        }
        if (str.isEmpty()) {
            return Locale.ROOT;
        }
        if (str.contains("#")) { // LANG-879 - Cannot handle Java 7 script & extensions
            throw new IllegalArgumentException("Invalid locale format: " + str);
        }
        final int len = str.length();
        if (len < 2) {
            throw new IllegalArgumentException("Invalid locale format: " + str);
        }
        final char ch0 = str.charAt(0);
        if (ch0 == UNDERSCORE || ch0 == DASH) {
            if (len < 3) {
                throw new IllegalArgumentException("Invalid locale format: " + str);
            }
            final char ch1 = str.charAt(1);
            final char ch2 = str.charAt(2);
            if (!Character.isUpperCase(ch1) || !Character.isUpperCase(ch2)) {
                throw new IllegalArgumentException("Invalid locale format: " + str);
            }
            if (len == 3) {
                return new Locale.Builder().setRegion(str.substring(1, 3)).build();
            }
            if (len < 5) {
                throw new IllegalArgumentException("Invalid locale format: " + str);
            }
            if (str.charAt(3) != ch0) {
                throw new IllegalArgumentException("Invalid locale format: " + str);
            }
            return new Locale.Builder()
                    .setRegion(str.substring(1, 3))
                    .setVariant(str.substring(4))
                    .build();
        }

        return parseLocale(str);
    }
}
