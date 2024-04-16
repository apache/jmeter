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

package org.apache.jmeter.util

import org.apache.jmeter.samplers.SampleResult
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.math.pow
import kotlin.math.sqrt

class CalculatorTest {
    val calculator = Calculator()

    @Test
    fun min() {
        assertEquals(Long.MAX_VALUE, calculator.min, "min()")
        calculator.addSample(SampleResult(10, 42))
        assertEquals(42, calculator.min, "min(42)")
        calculator.addSample(SampleResult(10, 40))
        assertEquals(40, calculator.min, "min(42, 40)")
        calculator.addSample(SampleResult(10, 50))
        assertEquals(40, calculator.min, "min(42, 40, 50)")
        calculator.addSample(SampleResult(10, 50).apply { sampleCount = 2 })
        assertEquals(25, calculator.min, "min(42, 40, 50, 50/2)")
    }

    @Test
    fun max() {
        assertEquals(Long.MIN_VALUE, calculator.max, "max()")
        calculator.addSample(SampleResult(10, 40))
        assertEquals(40, calculator.max, "max(40)")
        calculator.addSample(SampleResult(10, 42))
        assertEquals(42, calculator.max, "max(40, 42)")
        calculator.addSample(SampleResult(10, 30))
        assertEquals(42, calculator.max, "max(40, 42, 30)")
        calculator.addSample(SampleResult(10, 90).apply { sampleCount = 2 })
        assertEquals(45, calculator.max, "max(40, 42, 30, 90/2)")
    }

    @Test
    fun countLong() {
        assertEquals(0, calculator.countLong, "countLong()")
        calculator.addSample(SampleResult(10, 40))
        assertEquals(1, calculator.countLong, "countLong(40)")
        calculator.addSample(SampleResult(10, 42))
        assertEquals(2, calculator.countLong, "countLong(40, 42)")
        calculator.addSample(SampleResult(10, 42).apply { sampleCount = 2 })
        assertEquals(4, calculator.countLong, "countLong(40, 42, 42/2)")
    }

    @Test
    fun mean() {
        assertEquals(0.0, calculator.mean, "mean()")
        calculator.addSample(SampleResult(10, 40))
        assertEquals(40.0, calculator.mean, 0.001, "mean(40)")
        calculator.addSample(SampleResult(10, 42))
        assertEquals((40.0 + 42.0) / 2, calculator.mean, 0.001, "mean(40, 42)")
        calculator.addSample(SampleResult(10, 48).apply { sampleCount = 2 })
        assertEquals((40.0 + 42.0 + 48) / 4, calculator.mean, 0.001, "mean(40, 42, 48/2)")
    }

    @Test
    fun standardDeviation() {
        assertEquals(0.0, calculator.standardDeviation, "standardDeviation()")
        calculator.addSample(SampleResult(10, 40))
        assertEquals(0.0, calculator.standardDeviation, "standardDeviation(40)")
        calculator.addSample(SampleResult(10, 42))
        assertEquals(1.0, calculator.standardDeviation, "standardDeviation(40, 42)")
        calculator.addSample(SampleResult(10, 43))
        // Math.sqrt((sumOfSquares / count) - (mean * mean))
        assertEquals(
            sqrt((40 * 40 + 42 * 42 + 43 * 43) / 3.0 - ((40.0 + 42 + 43) / 3).pow(2)),
            calculator.standardDeviation,
            0.001,
            "standardDeviation(40, 42, 43)"
        )
        calculator.addSample(SampleResult(10, 48).apply { sampleCount = 2 })
        assertEquals(
            sqrt((40 * 40 + 42 * 42 + 43 * 43 + 24 * 24 + 24 * 24) / 5.0 - ((40.0 + 42 + 43 + 24 + 24) / 5.0).pow(2)),
            calculator.standardDeviation,
            0.001,
            "standardDeviation(40, 42, 43, 48/2)"
        )
    }

    @Test
    fun errorPercentage() {
        assertEquals(0.0, calculator.errorPercentage, 0.001, "errorPercentage()")
        calculator.addSample(SampleResult(10, 40).apply { isSuccessful = false })
        assertEquals(1.0, calculator.errorPercentage, 0.001, "errorPercentage(KO)")
        calculator.addSample(SampleResult(10, 42).apply { isSuccessful = false })
        assertEquals(1.0, calculator.errorPercentage, 0.001, "errorPercentage(KO, KO)")
        calculator.addSample(SampleResult(10, 42).apply { isSuccessful = true })
        assertEquals(0.666, calculator.errorPercentage, 0.001, "errorPercentage(KO, KO, OK)")
    }

    @Test
    fun bytesPerSecond() {
        assertEquals(0.0, calculator.bytesPerSecond, "bytesPerSecond()")
        calculator.addSample(SampleResult(40, 30).apply { setBodySize(50L) })
        assertEquals(
            50.0 * 1000 / (40 - 10.0),
            calculator.bytesPerSecond,
            0.001,
            "bytesPerSecond({50bytes, 10ms..40ms})"
        )
        calculator.addSample(SampleResult(60, 20).apply { setBodySize(70L) })
        assertEquals(
            (50 + 70.0) * 1000 / (60 - 10.0),
            calculator.bytesPerSecond,
            0.001,
            "bytesPerSecond({50bytes, 10ms..40ms}, {70bytes, 40ms..60ms})"
        )
    }

    @Test
    fun sentBytesPerSecond() {
        assertEquals(0.0, calculator.sentBytesPerSecond, "sentBytesPerSecond()")
        calculator.addSample(SampleResult(40, 30).apply { sentBytes = 50L })
        assertEquals(
            50.0 * 1000 / (40 - 10.0),
            calculator.sentBytesPerSecond,
            0.001,
            "sentBytesPerSecond({sent=50bytes, 10ms..40ms})"
        )
        calculator.addSample(SampleResult(60, 20).apply { sentBytes = 70L })
        assertEquals(
            (50 + 70.0) * 1000 / (60 - 10.0),
            calculator.sentBytesPerSecond,
            0.001,
            "sentBytesPerSecond({sent=50bytes, 10ms..40ms}, {sent=70bytes, 40ms..60ms})"
        )
    }
}
