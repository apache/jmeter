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
package org.apache.jmeter.report.processor;

import java.util.HashMap;
import java.util.Map;

/**
 * Summary data for TOP 5 of errors.
 * Compute a map of Sample / Number of errors
 *
 * @since 3.1
 */
public class Top5ErrorsSummaryData {

    private static final Long ONE = Long.valueOf(1L);
    private Map<String, Long> countPerError;
    private long total;
    private long errors;

    public Top5ErrorsSummaryData() {
        countPerError = new HashMap<>();
    }

    /**
     * Stores the provided error message and counts the number of times it is
     * registered.
     *
     * @param errorMessage String error message to register
     */
    public void registerError(String errorMessage) {
        Long value = countPerError.get(errorMessage);
        if (value == null) {
            countPerError.put(errorMessage, ONE);
        } else {
            countPerError.put(errorMessage, Long.valueOf(value.longValue() + 1));
        }
    }

    public void incErrors() {
        errors++;
    }

    public void incTotal() {
        total++;
    }

    public long getTotal() {
        return total;
    }

    public long getErrors() {
        return errors;
    }

    /**
     * Return Top 5 errors and associated frequency.
     *
     * @return array of [String, Long]
     */
    public Object[][] getTop5ErrorsMetrics() {
        int maxSize = Top5ErrorsBySamplerConsumer.MAX_NUMBER_OF_ERRORS_IN_TOP;
        return countPerError.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(maxSize)
                .map(e -> new Object[]{e.getKey(), e.getValue()})
                .toArray(e -> new Object[maxSize][2]);
    }
}
