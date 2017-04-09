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
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Summary data for TOP 5 of errors.
 * Compute a map of Sample / Number of errors
 * @since 3.1
 */
public class Top5ErrorsSummaryData {

    private static final Long ONE = Long.valueOf(1L);
    private Map<String, Long> countPerError;
    private long total;
    private long errors;

    /**
     */
    public Top5ErrorsSummaryData() {
        countPerError = new HashMap<>();
    }

    /**
     * 
     * @param errorMessage String error message to add
     */
    public void registerError(String errorMessage) {
        Long value = countPerError.get(errorMessage);
        if(value == null) {
            countPerError.put(errorMessage, ONE);
        } else {
            countPerError.put(errorMessage, Long.valueOf(value.longValue()+1));
        }
    }
    
    /**
     * Increment errors
     */
    public void incErrors() {
        errors++;
    }
    
    /**
     * Increment total
     */
    public void incTotal() {
        total++;
    }

    /**
     * @return the total
     */
    public long getTotal() {
        return total;
    }

    /**
     * @return the errors
     */
    public long getErrors() {
        return errors;
    }

    /**
     * Return Top 5 errors
     * @return array of [String, Long]
     */
    public Object[][] getTop5ErrorsMetrics() {
        SortedSet<Map.Entry<String, Long>> reverseSortedSet = new TreeSet<>(
                (Map.Entry<String, Long> e1,Map.Entry<String, Long> e2) 
                    -> e2.getValue().compareTo(e1.getValue()));
        
        reverseSortedSet.addAll(countPerError.entrySet());
        Object[][] result = new Object[Top5ErrorsBySamplerConsumer.MAX_NUMBER_OF_ERRORS_IN_TOP][2];
        int size = 0;
        for (Map.Entry<String, Long> entry : reverseSortedSet) {
            if(size == Top5ErrorsBySamplerConsumer.MAX_NUMBER_OF_ERRORS_IN_TOP) {
                break;
            }
            result[size] = new Object[] {entry.getKey(), entry.getValue()};
            size++;
        }
        return result;
    }
}
