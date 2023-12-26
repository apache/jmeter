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

package org.apache.jmeter.extractor.json.render

import org.apache.jmeter.junit.JMeterTestCase
import org.apache.jmeter.samplers.SampleResult
import org.apache.jmeter.test.gui.DisabledIfHeadless
import org.apache.jmeter.util.JMeterUtils
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.nio.charset.StandardCharsets
import javax.swing.JTabbedPane

class RenderAsJmesPathRendererTest : JMeterTestCase() {
    val sut = RenderAsJmesPathRenderer()

    data class RenderCase(
        @param:Language("json") val input: String,
        @param:Language("json") val output: String
    )

    data class ExecuteCase(
        @param:Language("json") val input: String,
        val expression: String,
        val output: String
    )

    companion object {
        @JvmStatic
        fun renderCases() = listOf(
            RenderCase("This is not json", "This is not json"),
            RenderCase(
                """{name:"Ludwig",age: 23,city: "Bonn"}""",
                """
                {
                    "city": "Bonn",
                    "name": "Ludwig",
                    "age": 23
                }
                """.trimIndent()
            ),
        )

        @JvmStatic
        fun executeCases() = listOf(
            ExecuteCase("{\"name\":\"Ludwig\",\"age\": 23,\"city\": \"Bonn\"}", "name", "Result[0]=Ludwig\n"),
            ExecuteCase("{\"name\":\"Ludwig\",\"age\": 23,\"city\": \"Bonn\"}", "age", "Result[0]=23\n"),
            ExecuteCase("{\"name\":\"Ludwig\",\"age\": 23,\"city\": \"Bonn\"}", "name1", "NO MATCH"),
        )
    }

    @Test
    fun `init of component doesn't fail`() {
        sut.init()
        assertNotNull(sut.jsonWithExtractorPanel, ".jsonWithExtractorPanel")
    }

    @Test
    @DisabledIfHeadless
    fun `render image`() {
        sut.init()
        val sampleResult = SampleResult()
        sut.renderImage(sampleResult)
        sut.assertJsonDataFieldEquals(JMeterUtils.getResString("render_no_text"))
    }

    @Test
    fun `render null Response`() {
        sut.init()
        val sampleResult = SampleResult()
        sut.renderResult(sampleResult)
        sut.assertJsonDataFieldEquals("")
    }

    @DisabledIfHeadless
    @ParameterizedTest
    @MethodSource("renderCases")
    fun `render JSON Response`(case: RenderCase) {
        sut.init()
        val sampleResult = SampleResult()
        sampleResult.setResponseData(case.input, StandardCharsets.UTF_8.name())
        sut.renderResult(sampleResult)

        sut.assertJsonDataFieldEquals(case.output)
    }

    @ParameterizedTest
    @MethodSource("executeCases")
    fun `execute expression`(case: ExecuteCase) {
        sut.init()
        sut.expressionField.text = case.expression
        sut.executeTester(case.input)

        assertEquals(case.output, sut.resultField.getText(), ".resultField.text")
    }

    @Test
    fun `clearData clears expected fields`() {
        sut.init()
        sut.jsonDataField.setText("blabla")
        sut.resultField.setText("blabla")

        sut.clearData()

        assertEquals("", sut.resultField.getText(), ".resultField.text")
        sut.assertJsonDataFieldEquals("")
    }

    @Test
    fun `setupTabPane adds the tab to rightSide`() {
        sut.init()
        val rightSideTabbedPane = JTabbedPane()
        sut.rightSide = rightSideTabbedPane

        sut.setupTabPane()
        assertEquals(1, sut.rightSide.getTabCount(), ".rightSide.getTabCount()")

        // Investigate why it's failing
        // sut.rightSide.getTabComponentAt(0) == sut.jsonWithExtractorPanel
    }

    @Test
    fun `setupTabPane called twice does not add twice the tab`() {
        sut.init()
        val rightSideTabbedPane = JTabbedPane()
        sut.rightSide = rightSideTabbedPane
        sut.setupTabPane()
        sut.setupTabPane()
        assertEquals(1, sut.rightSide.getTabCount(), ".rightSide.getTabCount()")
    }
}
