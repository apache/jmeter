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

import net.miginfocom.swing.MigLayout
import org.apache.jmeter.engine.util.CompoundVariable
import org.apache.jmeter.gui.TestElementMetadata
import org.apache.jmeter.testelement.TestElement
import org.apache.jmeter.threads.gui.AbstractThreadGroupGui
import org.apache.jmeter.threads.openmodel.DefaultThreadSchedule
import org.apache.jmeter.threads.openmodel.OpenModelThreadGroup
import org.apache.jmeter.threads.openmodel.ThreadSchedule
import org.apache.jmeter.threads.openmodel.ThreadScheduleStep
import org.apache.jmeter.threads.openmodel.asSeconds
import org.apache.jmeter.threads.openmodel.rateUnitFor
import org.apache.jmeter.util.JMeterUtils
import org.apache.jmeter.util.JMeterUtils.labelFor
import org.apache.jorphan.gui.JFactory
import org.apiguardian.api.API
import java.text.MessageFormat
import java.time.Duration
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextArea
import javax.swing.JTextField
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import kotlin.math.roundToLong

@API(status = API.Status.EXPERIMENTAL, since = "5.5")
@TestElementMetadata(labelResource = "openmodelthreadgroup")
public class OpenModelThreadGroupGui : AbstractThreadGroupGui() {
    override fun getLabelResource(): String = "openmodelthreadgroup"

    private val randomSeedEditor = JTextField()
    private val scheduleStringEditor = JFactory.tabMovesFocus(JTextArea())
    private val explanation = JLabel()
    private val targetRateChart = TargetRateChart()
    private val scheduleSummaryFormat = MessageFormat(JMeterUtils.getResString("openmodelthreadgroup_schedule_summary"))

    init {
        add(createPanel())
        scheduleStringEditor.document.addDocumentListener(object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent?) {
                updateExplanation()
            }

            override fun removeUpdate(e: DocumentEvent?) {
                updateExplanation()
            }

            override fun changedUpdate(e: DocumentEvent?) {
                updateExplanation()
            }
        })
    }

    private fun createPanel() =
        JPanel(MigLayout("fillx, wrap1", "[fill, grow]")).apply {
            add(labelFor(scheduleStringEditor, "openmodelthreadgroup_schedule_string"), "grow 0, split 6")
            add(templateButton("rate(1/min)"), "grow 0")
            add(templateButton("random_arrivals(10 min)"), "grow 0")
            add(templateButton("pause(1 min)"), "grow 0")
            add(templateButton("/* comment */"), "grow 0")
            add(JPanel())
            add(scheduleStringEditor)

            add(labelFor(randomSeedEditor, "openmodelthreadgroup_random_seed"), "grow 0, split 3")
            add(randomSeedEditor, "width 100pt, grow 0")
            add(JPanel())

            add(explanation)
            add(targetRateChart, "height 200")
        }

    private fun templateButton(title: String) = JButton(title).apply {
        isRequestFocusEnabled = false
        addActionListener {
            val editor = scheduleStringEditor
            val originalText = editor.text
            var replacement = title
            if (editor.selectionStart > 0 && !originalText[editor.selectionStart - 1].isWhitespace()) {
                replacement = " $replacement"
            }
            if (editor.selectionEnd < originalText.length && !originalText[editor.selectionEnd].isWhitespace()) {
                replacement = "$replacement "
            }
            editor.replaceSelection(replacement)
        }
    }

    private fun updateExplanation() {
        val schedule = evaluate(scheduleStringEditor.text)
        explanation.text =
            try {
                val threadSchedule = ThreadSchedule(schedule)
                targetRateChart.updateSchedule(threadSchedule)
                val duration = Duration.ofSeconds(threadSchedule.totalDuration.roundToLong())
                val maxRate = threadSchedule.steps.maxOf { (it as? ThreadScheduleStep.RateStep)?.rate ?: 0.0 }
                val rateUnit = rateUnitFor(maxRate)
                val rate = maxRate * rateUnitFor(maxRate).asSeconds
                val rateUnitLabel = rateUnit.toString().lowercase().removeSuffix("s")
                val readableDuration = duration.toString().removePrefix("PT").lowercase()
                val sheduleSummary = scheduleSummaryFormat.format(
                    arrayOf(
                        readableDuration,
                        "$rate / $rateUnitLabel"
                    )
                )
                "<html><body>$sheduleSummary</body></html>"
            } catch (expected: Exception) {
                expected.message
            }
    }

    private fun evaluate(input: String): String = CompoundVariable(input).execute()

    override fun createTestElement(): TestElement =
        OpenModelThreadGroup().also {
            modifyTestElement(it)
        }

    override fun modifyTestElement(tg: TestElement) {
        configureTestElement(tg)
        tg as OpenModelThreadGroup
        tg.scheduleString = scheduleStringEditor.text
        tg.randomSeedString = randomSeedEditor.text
    }

    override fun configure(tg: TestElement) {
        super.configure(tg)
        tg as OpenModelThreadGroup
        scheduleStringEditor.text = tg.scheduleString
        randomSeedEditor.text = tg.randomSeedString
    }

    override fun clearGui() {
        super.clearGui()
        scheduleStringEditor.text = ""
        randomSeedEditor.text = ""
        targetRateChart.updateSchedule(
            DefaultThreadSchedule(
                listOf(
                    ThreadScheduleStep.RateStep(0.0),
                    ThreadScheduleStep.ArrivalsStep(ThreadScheduleStep.ArrivalType.RANDOM, 1.0)
                )
            )
        )
    }
}
