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

package org.apache.jmeter.testelement;

import static org.junit.Assert.*;

import org.apache.jmeter.testelement.property.DoubleProperty;
import org.apache.jmeter.testelement.property.IntegerProperty;
import org.apache.jmeter.testelement.property.NumberProperty;
import org.junit.Test;

public class TestNumberProperty {

    @Test
    public void testDZeroCompareToDZero() {
        NumberProperty n1 = new DoubleProperty("n1", 0.0);
        NumberProperty n2 = new DoubleProperty("n2", 0.0);
        assertTrue(n1.compareTo(n2) == 0);
    }

    @Test
    public void testIZeroCompareToDZero() {
        NumberProperty n1 = new IntegerProperty("n1", 0);
        NumberProperty n2 = new DoubleProperty("n2", 0.0);
        assertTrue(n1.compareTo(n2) == 0);
    }

    @Test
    public void testCompareToPositive() {
        NumberProperty n1 = new DoubleProperty("n1", 1.0);
        NumberProperty n2 = new DoubleProperty("n2", 0.0);
        assertTrue(n1.compareTo(n2) > 0);
    }

    @Test
    public void testCompareToNegative() {
        NumberProperty n1 = new DoubleProperty("n1", -1.0);
        NumberProperty n2 = new DoubleProperty("n2", 0.0);
        assertTrue(n1.compareTo(n2) < 0);
    }

    @Test
    public void testCompareToMinMax() {
        NumberProperty n1 = new DoubleProperty("n1", Double.MIN_VALUE);
        NumberProperty n2 = new DoubleProperty("n2", Double.MAX_VALUE);
        assertTrue(n1.compareTo(n2) < 0);
    }

}
