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

import java.util.Comparator;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Comparator for Objects, that compares based on their <em>converted</em> values. The objects will be
 * converted to a String value using the given {@link Function}. That value
 * will be compared in a human readable fashion by trying to parse numbers that appear in
 * the keys as integers and compare those, too.<p>
 * Heavily influenced by https://codereview.stackexchange.com/questions/37192/number-aware-string-sorting-with-comparator
 */
public class AlphaNumericComparator<T> implements Comparator<T> {

    private Function<T, String> converter;

    /**
     * Constructs a comparator with a converter function
     * @param converter that generates a String value from the arguments given to {@link Comparator#compare(Object, Object)}
     */
    public AlphaNumericComparator(Function<T, String> converter) {
        this.converter = converter;
    }

    private static final Pattern parts = Pattern.compile("(\\D*)(\\d*)");
    private static final int ALPHA_PART = 1;
    private static final int NUM_PART = 2;

    @Override
    public int compare(T o1, T o2) {
        Matcher m1 = parts.matcher(converter.apply(o1));
        Matcher m2 = parts.matcher(converter.apply(o2));

        while (m1.find() && m2.find()) {
            int compareCharGroup = m1.group(ALPHA_PART).compareTo(m2.group(ALPHA_PART));
            if (compareCharGroup != 0) {
                return compareCharGroup;
            }
            String numberPart1 = m1.group(NUM_PART);
            String numberPart2 = m2.group(NUM_PART);
            if (numberPart1.isEmpty() || numberPart2.isEmpty()) {
                return compareOneEmptyPart(numberPart1, numberPart2);
            }
            String nonZeroNumberPart1 = trimLeadingZeroes(numberPart1);
            String nonZeroNumberPart2 = trimLeadingZeroes(numberPart2);
            int lengthNumber1 = nonZeroNumberPart1.length();
            int lengthNumber2 = nonZeroNumberPart2.length();
            if (lengthNumber1 != lengthNumber2) {
                if (lengthNumber1 < lengthNumber2) {
                    return -1;
                }
                return 1;
            }
            int compareNumber = nonZeroNumberPart1.compareTo(nonZeroNumberPart2);
            if (compareNumber != 0) {
                return compareNumber;
            }
        }
        if (m1.hitEnd() && m2.hitEnd()) {
            return 0;
        }
        if (m1.hitEnd()) {
            return -1;
        }
        return 1;
    }

    private int compareOneEmptyPart(String numberPart1, String numberPart2) {
        if (numberPart1.isEmpty()) {
            if (numberPart2.isEmpty()) {
                return 0;
            }
            return -1;
        } else if (numberPart2.isEmpty()) {
            return 1;
        }
        throw new IllegalArgumentException("At least one of the parameters have to be empty");
    }

    private String trimLeadingZeroes(String numberPart) {
        int length = numberPart.length();
        for (int i = 0; i < length; i++) {
            if (numberPart.charAt(i) != '0') {
                return numberPart.substring(i);
            }
        }
        return "";
    }

}
