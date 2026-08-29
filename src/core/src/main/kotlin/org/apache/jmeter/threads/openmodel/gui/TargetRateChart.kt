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

import org.apache.jmeter.threads.openmodel.ConcurrencyLimit
import org.apache.jmeter.threads.openmodel.ThreadSchedule
import org.apache.jmeter.threads.openmodel.ThreadScheduleStep
import org.apache.jmeter.threads.openmodel.asSeconds
import org.apache.jmeter.threads.openmodel.rateUnitFor
import org.apiguardian.api.API
import org.jetbrains.letsPlot.batik.plot.component.DefaultPlotPanelBatik
import org.jetbrains.letsPlot.commons.registration.Disposable
import org.jetbrains.letsPlot.core.util.MonolithicCommon
import org.jetbrains.letsPlot.geom.geomLine
import org.jetbrains.letsPlot.geom.geomStep
import org.jetbrains.letsPlot.gggrid
import org.jetbrains.letsPlot.intern.Plot
import org.jetbrains.letsPlot.intern.toSpec
import org.jetbrains.letsPlot.label.ggtitle
import org.jetbrains.letsPlot.letsPlot
import org.jetbrains.letsPlot.scale.scaleXTime
import org.jetbrains.letsPlot.themes.theme
import org.slf4j.LoggerFactory
import java.awt.BorderLayout
import java.util.concurrent.TimeUnit
import javax.swing.JComponent
import javax.swing.JPanel

/**
 * Draws a line chart with the expected load rate over time for given [ThreadSchedule].
 */
@API(status = API.Status.EXPERIMENTAL, since = "5.5")
public class TargetRateChart : JPanel() {
    private companion object {
        private val log = LoggerFactory.getLogger(TargetRateChart::class.java)
        private const val MIN_TICKS_FOR_TIME_AXIS = 2.5
    }

    init {
        layout = BorderLayout()
    }

    private var prevSteps: List<ThreadScheduleStep>? = null

    public fun updateSchedule(threadSchedule: ThreadSchedule) {
        if (threadSchedule.steps == prevSteps) {
            return
        }
        prevSteps = threadSchedule.steps
        val spec = buildSpec(threadSchedule)
        components.forEach {
            if (it is Disposable) {
                it.dispose()
            }
        }
        removeAll()
        add(render(spec), BorderLayout.CENTER)
    }

    /**
     * Builds the lets-plot specification for the schedule. Kept separate from [updateSchedule] so it
     * can be tested without instantiating the Swing/Batik rendering.
     */
    internal fun buildSpec(threadSchedule: ThreadSchedule): MutableMap<String, Any> {
        val timeValues = mutableListOf<Double>()
        val rateValues = mutableListOf<Double>()
        var time = 0.0
        var rate = 0.0
        var addPoint = false

        for (step in threadSchedule.steps) {
            when (step) {
                is ThreadScheduleStep.RateStep -> {
                    rate = step.rate
                    if (addPoint) {
                        addPoint = false
                        timeValues += time
                        rateValues += rate
                    }
                }
                is ThreadScheduleStep.ArrivalsStep -> if (step.duration > 0) {
                    timeValues += time
                    rateValues += rate
                    addPoint = true
                    time += step.duration
                }
                // Concurrency is drawn as a separate panel below
                is ThreadScheduleStep.ConcurrencyStep -> Unit
            }
        }
        if (addPoint) {
            timeValues += time
            rateValues += rate
        }

        val timeArray = timeValues.toDoubleArray()
        val rateArray = rateValues.toDoubleArray()
        val timeScale = TimeUnit.SECONDS.toMillis(1).toDouble()
        for (i in timeArray.indices) {
            timeArray[i] *= timeScale
        }
        val maxRate = rateArray.maxOrNull() ?: 0.0
        val rateUnit = rateUnitFor(maxRate)
        if (rateUnit != TimeUnit.SECONDS) {
            val scale = rateUnit.asSeconds
            for (i in rateArray.indices) {
                rateArray[i] *= scale
            }
        }

        val changes = ConcurrencyLimit.of(threadSchedule).changes
        // A single limit that applies from the start is shown as a subtitle rather than a panel.
        val constantLimit = changes.singleOrNull()?.takeIf { it.timeSeconds == 0.0 }?.limit
        val drawPanel = changes.isNotEmpty() && constantLimit == null

        val ratePlot = createRateChart(
            time = timeArray,
            rate = rateArray,
            rateUnit = rateUnit,
            subtitle = constantLimit?.let { "Max threads: $it" },
            showTimeAxis = !drawPanel,
        )
        return if (drawPanel) {
            val concurrencyPlot = createConcurrencyChart(changes, time * timeScale, timeScale)
            // Stack the two panels; sharex aligns the time axes so they line up vertically
            LinkedHashMap(gggrid(listOf(ratePlot, concurrencyPlot), ncol = 1, sharex = "all", align = true).toSpec())
        } else {
            ratePlot.toSpec()
        }
    }

    private fun createRateChart(
        time: DoubleArray,
        rate: DoubleArray,
        rateUnit: TimeUnit,
        subtitle: String?,
        showTimeAxis: Boolean,
    ): Plot {
        val data = mapOf("time" to time, "rate" to rate)
        var plot = letsPlot(data) + geomLine { x = "time"; y = "rate" } +
            scaleXTime("Time since test start", expand = listOf(0, 0)) +
            ggtitle("Target load rate per " + rateUnit.name.lowercase().removeSuffix("s"), subtitle) +
            theme(axisTitleY = "blank")
        if (!showTimeAxis) {
            // The concurrency panel below carries the shared time axis
            plot += theme(axisTitleX = "blank", axisTextX = "blank")
        }
        return plot
    }

    private fun createConcurrencyChart(changes: List<ConcurrencyLimit.Change>, endTimeMillis: Double, timeScale: Double): Plot {
        val time = DoubleArray(changes.size + 1) { if (it < changes.size) changes[it].timeSeconds * timeScale else endTimeMillis }
        val limit = DoubleArray(changes.size + 1) { changes[if (it < changes.size) it else changes.size - 1].limit.toDouble() }
        val data = mapOf("time" to time, "threads" to limit)
        // geomStep, not geomLine: the limit is piecewise-constant and jumps between windows
        return letsPlot(data) + geomStep { x = "time"; y = "threads" } +
            scaleXTime("Time since test start", expand = listOf(0, 0)) +
            ggtitle("Max concurrent threads") +
            theme(axisTitleY = "blank")
    }

    private fun render(spec: MutableMap<String, Any>): JComponent {
        val processedSpec = MonolithicCommon.processRawSpecs(spec, frontendOnly = false)
        return DefaultPlotPanelBatik(
            processedSpec = processedSpec,
            preserveAspectRatio = false,
            preferredSizeFromPlot = false,
            repaintDelay = 10,
        ) { messages ->
            for (message in messages) {
                log.info(message)
            }
        }
    }
}
