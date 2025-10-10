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

import org.apiguardian.api.API;

/**
 * Utility class for performing various String-related operations.
 * This class provides methods to manipulate and evaluate strings, including
 * checking if strings are empty, blank, or contain specific characters,
 * as well as methods for replacing, capitalizing, and trimming strings.
 *
 * This class is marked as {@code @API(status = API.Status.EXPERIMENTAL)}, indicating
 * its API is still evolving and might change in future releases.
 */
@API(status = API.Status.EXPERIMENTAL, since = "5.6")
public class StringUtilities {
    private StringUtilities() {
    }

    /**
     * Counts the number of times a given char is present in the string.
     *
     * @param input input string
     * @param ch    char to search
     * @return number of times the character is present in the string, or 0 if no char found
     */
    public static int count(String input, char ch) {
        if (input.isEmpty()) {
            return 0;
        }
        int count = 0;
        int idx = 0;
        while ((idx = input.indexOf(ch, idx)) != -1) {
            count++;
            idx++;
        }
        return count;
    }

    /**
     * Checks if a CharSequence is empty (""), null, or contains only whitespace characters.
     *
     * @param cs the CharSequence to check, may be null
     * @return true if the CharSequence is null, empty, or only contains whitespace; false otherwise
     */
    @API(since = "6.0.0", status = API.Status.EXPERIMENTAL)
    public static boolean isBlank(CharSequence cs) {
        if (cs == null) {
            return true;
        }
        if (cs instanceof String s) {
            return s.isBlank(); // fast path
        }
        int strLen = cs.length();
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(cs.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if a CharSequence is not empty, not null, and does not contain only whitespace characters.
     *
     * @param cs the CharSequence to check, may be null
     * @return true if the CharSequence is not null, not empty, and contains at least one non-whitespace character; false otherwise
     */
    @API(since = "6.0.0", status = API.Status.EXPERIMENTAL)
    public static boolean isNotBlank(CharSequence cs) {
        return !isBlank(cs);
    }

    /**
     * Checks if a CharSequence is null or empty ("").
     *
     * @param cs the CharSequence to check, may be null
     * @return true if the CharSequence is null or empty; false otherwise
     */
    @API(since = "6.0.0", status = API.Status.EXPERIMENTAL)
    public static boolean isEmpty(CharSequence cs) {
        return cs == null || cs.isEmpty();
    }

    /**
     * Checks if a CharSequence is not null and not empty ("").
     *
     * @param cs the CharSequence to check, may be null
     * @return true if the CharSequence is not null and not empty; false otherwise
     */
    @API(since = "6.0.0", status = API.Status.EXPERIMENTAL)
    public static boolean isNotEmpty(CharSequence cs) {
        return !isEmpty(cs);
    }

    /**
     * Replaces multiple characters in a string.
     *
     * @param str the string to modify, may be null
     * @param searchChars the characters to search for, may be null
     * @param replaceChars the characters to replace with, may be null
     * @return the modified string, or null if input was null
     */
    public static String replaceChars(String str, String searchChars, String replaceChars) {
        if (StringUtilities.isEmpty(str) || StringUtilities.isEmpty(searchChars)) {
            return str;
        }
        StringBuilder sb = new StringBuilder(str.length());
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            int index = searchChars.indexOf(c);
            if (index < 0) {
                sb.append(c);
            } else {
                if (replaceChars != null && index < replaceChars.length()) {
                    sb.append(replaceChars.charAt(index));
                }
                // If replaceChars is null or index is out of bounds, character is deleted
            }
        }
        return sb.toString();
    }

    /**
     * Trims the string and returns null if the result is empty.
     *
     * @param str the String to be trimmed, may be null
     * @return the trimmed String, or null if empty or null input
     */
    @API(since = "6.0.0", status = API.Status.EXPERIMENTAL)
    public static String trimToNull(String str) {
        if (str == null) {
            return null;
        }
        String trimmed = str.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /**
     * Capitalizes a string by converting the first character to title case.
     * Properly handles Unicode surrogate pairs.
     *
     * @param str the string to capitalize, may be null
     * @return the capitalized string, or null if input was null
     */
    @API(since = "6.0.0", status = API.Status.EXPERIMENTAL)
    public static String capitalize(String str) {
        if (StringUtilities.isEmpty(str)) {
            return str;
        }
        int firstCodepoint = str.codePointAt(0);
        int titleCaseCodepoint = Character.toTitleCase(firstCodepoint);

        if (firstCodepoint == titleCaseCodepoint) {
            // No change needed
            return str;
        }

        StringBuilder sb = new StringBuilder(str.length());
        sb.appendCodePoint(titleCaseCodepoint);
        sb.append(str, Character.charCount(firstCodepoint), str.length());
        return sb.toString();
    }

    /**
     * Strips any of the specified characters from the start and end of a string.
     *
     * @param str the string to strip, may be null
     * @param stripChars the characters to remove, null treated as whitespace
     * @return the stripped string, or null if input was null
     */
    @API(since = "6.0.0", status = API.Status.EXPERIMENTAL)
    public static String strip(String str, String stripChars) {
        if (StringUtilities.isEmpty(str)) {
            return str;
        }
        int start = 0;
        int end = str.length();

        // Strip from start
        while (start < end && stripChars.indexOf(str.charAt(start)) >= 0) {
            start++;
        }

        // Strip from end
        while (start < end && stripChars.indexOf(str.charAt(end - 1)) >= 0) {
            end--;
        }

        return str.substring(start, end);
    }

    /**
     * Checks if the string contains only digits (0-9).
     *
     * @param str the string to check
     * @return true if all characters are digits, false otherwise
     */
    @API(status = API.Status.EXPERIMENTAL, since = "6.0.0")
    public static boolean isNumeric(String str) {
        if (isEmpty(str)) {
            return false;
        }
        for (int i = 0; i < str.length(); i++) {
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}
