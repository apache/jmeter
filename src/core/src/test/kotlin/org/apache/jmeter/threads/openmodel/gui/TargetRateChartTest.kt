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

package org.apache.jmeter.threads.openmodel.gui

import org.apache.jmeter.threads.openmodel.ThreadSchedule
import org.jetbrains.letsPlot.core.util.MonolithicCommon
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class TargetRateChartTest {
    private val chart = TargetRateChart()

    private fun specFor(schedule: String) = chart.buildSpec(ThreadSchedule(schedule))

    private fun assertLetsPlotAccepts(spec: MutableMap<String, Any>) {
        val processed = MonolithicCommon.processRawSpecs(spec, frontendOnly = false)
        assertFalse(
            processed["kind"] == "error" || processed.containsKey("errorMessage"),
            "lets-plot should process the spec without an error, but got: $processed"
        )
    }

    @Test
    fun `rate only schedule renders a single plot`() {
        val spec = specFor("rate(10/sec) random_arrivals(1 min)")
        assertEquals("plot", spec["kind"], "rate-only schedule should not add a concurrency panel")
        assertLetsPlotAccepts(spec)
    }

    @Test
    fun `constant concurrency renders a single plot with a subtitle`() {
        val spec = specFor("concurrency(10) rate(10/sec) random_arrivals(1 min)")
        assertEquals("plot", spec["kind"], "a constant limit should be a subtitle, not a panel")
        assertTrue(
            spec.toString().contains("Max threads: 10"),
            "the constant limit should appear as a subtitle, but got: $spec"
        )
        assertLetsPlotAccepts(spec)
    }

    @Test
    fun `variable concurrency renders a stacked second panel`() {
        val spec = specFor(
            "concurrency(10) rate(10/sec) random_arrivals(1 min) concurrency(20) random_arrivals(1 min)"
        )
        assertEquals("subplots", spec["kind"], "a changing limit should be drawn as a stacked panel")
        assertLetsPlotAccepts(spec)
    }
}
