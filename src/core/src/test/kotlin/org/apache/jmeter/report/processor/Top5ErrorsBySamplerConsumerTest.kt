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

import io.mockk.every
import io.mockk.mockk
import org.apache.jmeter.report.core.Sample
import org.apache.jmeter.report.utils.MetricUtils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class Top5ErrorsBySamplerConsumerTest {
    val sut = Top5ErrorsBySamplerConsumer()

    @Test
    fun `summary info data updated with non-controller passing sample`() {
        val mockSummaryInfo = mockk<AbstractSummaryConsumer<Top5ErrorsSummaryData>.SummaryInfo> {
            every { getData() } answers { callOriginal() }
            every { setData(any()) } answers { callOriginal() }
        }
        val mockSample = mockk<Sample>(relaxed = true) {
            every { success } returns true
        }
        sut.updateData(mockSummaryInfo, mockSample)

        val data = mockSummaryInfo.getData()
        assertEquals(1, data.total, "data.total")
    }

    @Test
    fun `summary info data updated with non-controller failing sample`() {
        val mockSummaryInfo = mockk<AbstractSummaryConsumer<Top5ErrorsSummaryData>.SummaryInfo> {
            every { getData() } answers { callOriginal() }
            every { setData(any()) } answers { callOriginal() }
        }
        val mockSample = mockk<Sample>(relaxed = true) {
            every { responseCode } returns "200"
        }
        sut.updateData(mockSummaryInfo, mockSample)

        val data = mockSummaryInfo.getData()
        assertEquals(1, data.total, "data.total")
        assertEquals(1, data.errors, "data.errors")
        assertEquals(MetricUtils.ASSERTION_FAILED, data.top5ErrorsMetrics[0][0], "data.top5ErrorsMetrics[0][0]")

        val overallData = sut.overallInfo.getData()
        assertEquals(1, overallData.total, "data.total")
        assertEquals(1, overallData.errors, "data.errors")
        assertEquals(
            MetricUtils.ASSERTION_FAILED,
            overallData.top5ErrorsMetrics[0][0],
            "overallData.top5ErrorsMetrics[0][0]"
        )
    }

    @Test
    fun `key from sample is name`() {
        val sample = mockk<Sample> {
            every { name } returns "name"
        }
        assertEquals("name", sut.getKeyFromSample(sample)) {
            "getKeyFromSample(sample) should return the name of the sample"
        }
    }

    @Test
    fun `there are 3 + 2n expected results title columns`() {
        assertEquals(
            3 + 2 * Top5ErrorsBySamplerConsumer.MAX_NUMBER_OF_ERRORS_IN_TOP,
            sut.createResultTitles().size
        ) {
            ".createResultTitles().size should be 3 + 2 * ${Top5ErrorsBySamplerConsumer.MAX_NUMBER_OF_ERRORS_IN_TOP}"
        }
    }
}
