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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class TestStatCalculator {

    private StatCalculatorLong calc;


    @Before
    public void setUp() {
        calc = new StatCalculatorLong();
    }

    @Test
    public void testPercentagePoint() throws Exception {
        long values[] = new long[] {
            10L,9L,5L,6L,1L,3L,8L,2L,7L,4L
        };
        for (long l : values) {
            calc.addValue(l);
        }
        assertEquals(10, calc.getCount());
        assertEquals(9, calc.getPercentPoint(0.8999999).intValue());
    }
    
    @Test
    @Ignore
    // Disabled due to in progress Bug 61071
    public void testPercentagePointBug() throws Exception {
        long values[] = new long[] {
            10L,9L,5L,6L,1L,3L,8L,2L,7L,4L
        };
        DescriptiveStatistics statistics = new DescriptiveStatistics();
        for (long l : values) {
            calc.addValue(l);
            statistics.addValue(l);
        }
        assertEquals(9, calc.getPercentPoint(0.8999999).intValue());
        // 
        assertEquals(Math.round(statistics.getPercentile(90)), 
                calc.getPercentPoint(0.9).intValue());
    }
    
    @Test
    public void testCalculation() {
        assertEquals(Long.MIN_VALUE, calc.getMax().longValue());
        assertEquals(Long.MAX_VALUE, calc.getMin().longValue());
        calc.addValue(18);
        calc.addValue(10);
        calc.addValue(9);
        calc.addValue(11);
        calc.addValue(28);
        calc.addValue(3);
        calc.addValue(30);
        calc.addValue(15);
        calc.addValue(15);
        calc.addValue(21);
        assertEquals(16, (int) calc.getMean());
        assertEquals(8.0622577F, (float) calc.getStandardDeviation(), 0F);
        assertEquals(30, calc.getMax().intValue());
        assertEquals(3, calc.getMin().intValue());
        assertEquals(15, calc.getMedian().intValue());
    }

    @Test
    @Ignore
    // Disabled due to in progress Bug 61071 
    public void testMedianBug61071() {
        long[] values = new long[] {
            10L, 20L, 30L, 40L, 50L, 60L, 80L, 90L
        };
        DescriptiveStatistics statistics = new DescriptiveStatistics();

        for (long l : values) {
            calc.addValue(l);
            statistics.addValue(l);
        }
        assertEquals((int) statistics.getPercentile(50), calc.getMedian().intValue());
    }
    
    @Test
    public void testLong(){
        calc.addValue(0L);
        calc.addValue(2L);
        calc.addValue(2L);
        final Long long0 = Long.valueOf(0);
        final Long long2 = Long.valueOf(2);
        assertEquals(long2,calc.getMax());
        assertEquals(long0,calc.getMin());
        Map<Number, Number[]> map = calc.getDistribution();
        assertTrue(map.containsKey(long0));
        assertTrue(map.containsKey(long2));
    }
    
    @Test
    public void testInteger(){
        StatCalculatorInteger calci = new StatCalculatorInteger();
        assertEquals(Integer.MIN_VALUE, calci.getMax().intValue());
        assertEquals(Integer.MAX_VALUE, calci.getMin().intValue());
        calci.addValue(0);
        calci.addValue(2);
        calci.addValue(2);
        assertEquals(Integer.valueOf(2),calci.getMax());
        assertEquals(Integer.valueOf(0),calci.getMin());
        Map<Number, Number[]> map = calci.getDistribution();
        assertTrue(map.containsKey(Integer.valueOf(0)));
        assertTrue(map.containsKey(Integer.valueOf(2)));
    }
    
    @Test
    public void testBug52125_1(){ // No duplicates when adding
        calc.addValue(1L);
        calc.addValue(2L);
        calc.addValue(3L);
        calc.addValue(2L);
        calc.addValue(2L);
        calc.addValue(2L);
        assertEquals(6, calc.getCount());
        assertEquals(12.0, calc.getSum(), 0.000000000001);
        assertEquals(0.5773502691896255, calc.getStandardDeviation(), 0.000000000000001);
    }

    @Test
    @SuppressWarnings("boxing")
    public void testBug52125_2(){ // add duplicates
        calc.addValue(1L);
        calc.addValue(2L);
        calc.addValue(3L);
        calc.addEachValue(2L, 3);
        assertEquals(6, calc.getCount());
        assertEquals(12.0, calc.getSum(), 0.000000000001);
        assertEquals(0.5773502691896255, calc.getStandardDeviation(), 0.000000000000001);
    }

    @Test
    public void testBug52125_2A(){ // as above, but with aggregate sample instead
        calc.addValue(1L);
        calc.addValue(2L);
        calc.addValue(3L);
        calc.addValue(6L, 3);
        assertEquals(6, calc.getCount());
        assertEquals(12.0, calc.getSum(), 0.00000001);
        assertEquals(0.5773502691896255, calc.getStandardDeviation(), 0.000000000000001);
    }

    @Test
    public void testBug52125_3(){ // add duplicates as per bug
        calc.addValue(1L);
        calc.addValue(2L);
        calc.addValue(3L);
        StatCalculatorLong calc2 = new StatCalculatorLong();
        calc2.addValue(2L);
        calc2.addValue(2L);
        calc2.addValue(2L);
        calc.addAll(calc2);
        assertEquals(6, calc.getCount());
        assertEquals(12.0, calc.getSum(), 0.000000000001);
        assertEquals(0.5773502691896255, calc.getStandardDeviation(), 0.000000000000001);
    }
}
