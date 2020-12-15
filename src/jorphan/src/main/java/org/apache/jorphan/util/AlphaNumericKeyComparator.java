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

import java.math.BigInteger;
import java.util.Comparator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Comparator for {@link Map.Entry} Objects, that compares based on their keys only. The keys
 * will be compared in a human readable fashion by trying to parse numbers that appear in
 * the keys as integers and compare those, too.<p>
 * Heavily influenced by https://codereview.stackexchange.com/questions/37192/number-aware-string-sorting-with-comparator
 */
public class AlphaNumericKeyComparator implements Comparator<Map.Entry<Object, Object>> {
    
    public static final AlphaNumericKeyComparator INSTANCE = new AlphaNumericKeyComparator();
    
    private AlphaNumericKeyComparator() {
        // don't instantiate this class on your own.
    }

    private static final Pattern parts = Pattern.compile("(\\D*)(\\d*)");
    private static final int ALPHA_PART = 1;
    private static final int NUM_PART = 2;

    @Override
    public int compare(Map.Entry<Object, Object> o1, Map.Entry<Object, Object> o2) {
        Matcher m1 = parts.matcher(o1.getKey().toString());
        Matcher m2 = parts.matcher(o2.getKey().toString());

        while (m1.find() && m2.find()) {
            int compareCharGroup = m1.group(ALPHA_PART).compareTo(m2.group(ALPHA_PART));
            if (compareCharGroup != 0) {
                return compareCharGroup;
            }
            String numberPart1 = m1.group(NUM_PART);
            String numberPart2 = m2.group(NUM_PART);
            if (numberPart1.isEmpty()) {
                if (numberPart2.isEmpty()) {
                    return 0;
                }
                return -1;
            } else if (numberPart2.isEmpty()) {
                return 1;
            }
            int lengthNumber1 = numberPart1.length();
            int lengthNumber2 = numberPart2.length();
            if (lengthNumber1 != lengthNumber2) {
                if (lengthNumber1 < lengthNumber2) {
                    return -1;
                }
                return 1;
            }
            BigInteger i1 = new BigInteger(numberPart1);
            BigInteger i2 = new BigInteger(numberPart2);
            int compareNumber = i1.compareTo(i2);
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

}
