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

package org.apache.jorphan.math;

import java.math.BigInteger;

/**
 * StatCalculator for Long values
 */
public class StatCalculatorLong extends StatCalculator<BigInteger> {

    public StatCalculatorLong() {
        super(BigInteger.ZERO, BigInteger.valueOf(Long.MIN_VALUE), BigInteger.valueOf(Long.MAX_VALUE));
    }

    /**
     * Add a single value (normally elapsed time)
     *
     * @param val the value to add, which should correspond with a single sample
     */
    public void addValue(long val){
        super.addValue(BigInteger.valueOf(val));
    }

    /**
     * Update the calculator with the value for an aggregated sample.
     *
     * @param val the aggregate value, normally the elapsed time
     * @param sampleCount the number of samples contributing to the aggregate value
     */
    public void addValue(long val, int sampleCount){
        super.addValue(BigInteger.valueOf(val), sampleCount);
    }

    @Override
    protected BigInteger divide(BigInteger val, int n) {
        return val.divide(BigInteger.valueOf(n));
    }

    @Override
    protected BigInteger divide(BigInteger val, long n) {
        return val.divide(BigInteger.valueOf(n));
    }
}
