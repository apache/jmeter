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

package org.apache.jorphan.math;

/**
 * StatCalculator for Integer values
 */
public class StatCalculatorInteger extends StatCalculator<Integer> {

    public StatCalculatorInteger() {
        super(Integer.valueOf(0), Integer.valueOf(Integer.MIN_VALUE), Integer.valueOf(Integer.MAX_VALUE));
    }

    public void addValue(int val){
        super.addValue(Integer.valueOf(val));
    }

    /**
     * Update the calculator with the value for an aggregated sample.
     * 
     * @param val the aggregate value
     * @param sampleCount the number of samples contributing to the aggregate value
     */
    public void addValue(int val, int sampleCount){
        super.addValue(Integer.valueOf(val), sampleCount);
    }

    @Override
    protected Integer divide(Integer val, int n) {
        return Integer.valueOf(val.intValue() / n);
    }

    @Override
    protected Integer divide(Integer val, long n) {
        return Integer.valueOf((int) (val.intValue() / n));
    }
}
