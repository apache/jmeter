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

import jetbrains.datalore.base.registration.Disposable
import jetbrains.datalore.plot.MonolithicCommon
import jetbrains.datalore.vis.swing.batik.DefaultPlotPanelBatik
import jetbrains.letsPlot.geom.geomLine
import jetbrains.letsPlot.intern.toSpec
import jetbrains.letsPlot.label.ggtitle
import jetbrains.letsPlot.letsPlot
import jetbrains.letsPlot.scale.scaleXTime
import jetbrains.letsPlot.theme
import org.apache.jmeter.threads.openmodel.ThreadSchedule
import org.apache.jmeter.threads.openmodel.ThreadScheduleStep
import org.apache.jmeter.threads.openmodel.asSeconds
import org.apache.jmeter.threads.openmodel.rateUnitFor
import org.apiguardian.api.API
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
    private var prevTimes: DoubleArray? = null
    private var prevRate: DoubleArray? = null

    public fun updateSchedule(threadSchedule: ThreadSchedule) {
        if (threadSchedule.steps == prevSteps) {
            return
        }
        prevSteps = threadSchedule.steps
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
            }
        }
        if (addPoint) {
            timeValues += time
            rateValues += rate
        }
        setData(timeValues.toDoubleArray(), rateValues.toDoubleArray())
    }

    private fun setData(time: DoubleArray, rate: DoubleArray) {
        if (time.contentEquals(prevTimes) && rate.contentEquals(prevRate)) {
            return
        }
        prevTimes = time.copyOf()
        prevRate = rate.copyOf()
        val timeScale = TimeUnit.SECONDS.toMillis(1).toDouble()
        for (i in time.indices) {
            time[i] *= timeScale
        }
        val maxRate = rate.maxOrNull() ?: 0.0
        val rateUnit = rateUnitFor(maxRate)
        if (rateUnit != TimeUnit.SECONDS) {
            val scale = rateUnit.asSeconds
            for (i in rate.indices) {
                rate[i] *= scale
            }
        }

        components.forEach {
            if (it is Disposable) {
                it.dispose()
            }
        }
        removeAll()
        add(createChart(time = time, rate = rate, rateUnit = rateUnit), BorderLayout.CENTER)
    }

    private fun createChart(time: DoubleArray, rate: DoubleArray, rateUnit: TimeUnit): JComponent {
        val data = mapOf(
            "time" to time,
            "rate" to rate
        )
        val plot = letsPlot(data) + geomLine { x = "time"; y = "rate" } +
            scaleXTime("Time since test start", expand = listOf(0, 0)) +
            ggtitle("Target load rate per " + rateUnit.name.lowercase().removeSuffix("s")) +
            theme(axisTitleY = "blank")

        val rawSpec = plot.toSpec()
        val processedSpec = MonolithicCommon.processRawSpecs(rawSpec, frontendOnly = false)

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
