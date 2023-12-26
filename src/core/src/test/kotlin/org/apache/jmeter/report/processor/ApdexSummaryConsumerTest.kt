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

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ApdexSummaryConsumerTest {
    val sut = ApdexSummaryConsumer()

    @Test
    fun `createDataResult contains apdex, satisfied, tolerated, key`() {
        val info = ApdexThresholdsInfo()
        info.satisfiedThreshold = 3L
        info.toleratedThreshold = 12L
        val data = ApdexSummaryData(info).apply {
            satisfiedCount = 60L
            toleratedCount = 30L
            totalCount = 100L
        }
        val expectedApdex = 0.75

        val result = sut.createDataResult("key", data)

        val resultValues = result.map { (it as ValueResultData).value }
        assertEquals(
            listOf(expectedApdex, 3L, 12L, "key"),
            resultValues
        ) {
            "ApdexSummaryConsumer().createDataResult(\"key\", $data) should yield [apdex, satisfied, tolerated, key]"
        }
    }
}
