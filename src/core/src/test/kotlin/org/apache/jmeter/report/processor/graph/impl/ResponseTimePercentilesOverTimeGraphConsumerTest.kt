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

package org.apache.jmeter.report.processor.graph.impl

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test

class ResponseTimePercentilesOverTimeGraphConsumerTest {
    val sut = ResponseTimePercentilesOverTimeGraphConsumer()

    @Test
    fun `GroupInfos have only the required keys`() {
        val groupInfosMap = sut.createGroupInfos()

        assertEquals(
            setOf(
                "aggregate_report_min",
                "aggregate_report_max",
                "aggregate_rpt_pct1",
                "aggregate_rpt_pct2",
                "aggregate_rpt_pct3",
                "aggregate_report_median",
            ),
            groupInfosMap.keys,
            "createGroupInfos().keys"
        )
    }

    @Test
    fun `GroupInfos have the expected settings`() {
        val groupInfos = sut.createGroupInfos()
        groupInfos.values.forEach {
            assertFalse(it.enablesAggregatedKeysSeries(), "enablesAggregatedKeysSeries()")
            assertFalse(it.enablesOverallSeries(), "enablesOverallSeries()")
        }
    }
}
