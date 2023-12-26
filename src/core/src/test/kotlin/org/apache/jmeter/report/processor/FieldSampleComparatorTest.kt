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

package org.apache.jmeter.report.processor

import org.apache.jmeter.report.core.Sample
import org.apache.jmeter.report.core.SampleMetadata
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import kotlin.math.sign

class FieldSampleComparatorTest {
    companion object {
        const val separator = ','
    }
    private val multiColSampleMeta = SampleMetadata(separator, "col1", "col2")

    fun assertCompare(comparator: FieldSampleComparator, a: Sample, b: Sample, expectedSign: Int) {
        Assertions.assertEquals(expectedSign.sign, comparator.compare(a, b).sign) {
            "$comparator.compare($a, $b)"
        }
    }

    @Test
    fun testCompare() {
        val sampleMetadata = SampleMetadata(separator, "col1")
        val firstRow = Sample(0, sampleMetadata, "1")
        val secondRow = Sample(1, sampleMetadata, "2")
        val sut = FieldSampleComparator("col1")
        sut.initialize(sampleMetadata)

        assertCompare(sut, firstRow, secondRow, -1)
        assertCompare(sut, secondRow, firstRow, 1)
        assertCompare(sut, firstRow, firstRow, 0)
        assertCompare(sut, secondRow, secondRow, 0)
    }

    @Test
    fun `initialize ensures correct column is compared`() {
        val sut = FieldSampleComparator("col2")
        val firstRow = Sample(0, multiColSampleMeta, "1", "3")
        val secondRow = Sample(1, multiColSampleMeta, "2", "3")
        sut.initialize(multiColSampleMeta)

        assertCompare(sut, firstRow, secondRow, 0)
    }

    @Test
    fun `Incorrectly uses first column if initialize isn't called`() {
        val sut = FieldSampleComparator("col2")
        val firstRow = Sample(0, multiColSampleMeta, "1", "3")
        val secondRow = Sample(1, multiColSampleMeta, "2", "3")

        assertCompare(sut, firstRow, secondRow, -1)
    }
}
