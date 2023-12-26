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

package org.apache.jmeter.extractor

import org.apache.jmeter.samplers.SampleResult
import org.apache.jmeter.threads.JMeterContext
import org.apache.jmeter.threads.JMeterContextService
import org.apache.jmeter.threads.JMeterVariables
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.fail
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import java.util.stream.Stream

class BoundaryExtractorTest {
    data class ExtractCase(val occurrences: IntRange, val matchNumber: Int, val expected: List<String>)
    companion object {
        const val LEFT = "LB"
        const val RIGHT = "RB"
        const val DEFAULT_VAL = "defaultVal"
        const val VAR_NAME = "varName"

        /**
         * Creates a string with a "match" for each number in the list.
         *
         * @param occurrences list of numbers to be the "body" of a match
         * @return a string with a start, end and then a left boundary + number + right boundary
         * e.g. "... LB1RB LB2RB ..."
         */
        fun createInputString(occurrences: IntRange) =
            occurrences.joinToString(" ", prefix = "start \t\r\n", postfix = "\n\t end") {
                LEFT + it + RIGHT
            }

        fun createSampleResult(responseData: String) = SampleResult().apply {
            sampleStart()
            setResponseData(responseData, "ISO-8859-1")
            sampleEnd()
        }

        @JvmStatic
        fun extractCases() = listOf(
            ExtractCase(1..1, -1, listOf("1")),
            ExtractCase(1..1, 0, listOf("1")),
            ExtractCase(1..1, 1, listOf("1")),
            ExtractCase(1..1, 2, listOf()),
            ExtractCase(1..2, -1, listOf("1", "2")),
            ExtractCase(1..2, 1, listOf("1")),
            ExtractCase(1..2, 2, listOf("2")),
            ExtractCase(1..3, 3, listOf("3")),
        )

        @JvmStatic
        fun extractCasesStream() = listOf(
            ExtractCase(1..1, -1, List(10) { "1" }),
            ExtractCase(1..1, 0, List(10) { "1" }),
            ExtractCase(1..1, 1, listOf("1")),
            ExtractCase(1..1, 10, listOf("1")),
            ExtractCase(1..1, 11, listOf()),
            ExtractCase(1..2, -1, (1..10).flatMap { listOf("1", "2") }),
            ExtractCase(1..2, 1, listOf("1")),
            ExtractCase(1..2, 2, listOf("2")),
            ExtractCase(1..3, 3, listOf("3")),
        )
    }

    val sut = BoundaryExtractor()

    private lateinit var prevResult: SampleResult
    lateinit var vars: JMeterVariables
    private lateinit var context: JMeterContext

    @BeforeEach
    fun setup() {
        vars = JMeterVariables()
        context = JMeterContextService.getContext()
        context.variables = vars

        sut.threadContext = context
        sut.refName = VAR_NAME
        sut.leftBoundary = LEFT
        sut.rightBoundary = RIGHT
        sut.defaultValue = DEFAULT_VAL
        sut.matchNumber = 1

        prevResult = SampleResult().apply {
            sampleStart()
            setResponseData(createInputString(1..2), null)
            sampleEnd()
        }
        context.previousResult = prevResult
    }

    private fun assertVarValueEquals(expected: String?) {
        assertEquals(expected, vars.get(VAR_NAME), "vars.get($VAR_NAME)")
    }
    private fun assertVarNameMatchNrEquals(expected: String?) {
        assertEquals(expected, vars.get("${VAR_NAME}_matchNr"), "vars.get(${VAR_NAME}_matchNr)")
    }

    private fun assertAllVars(expected: List<String>) {
        assertEquals(expected, getAllVars(), "getAllVars()")
    }

    @ParameterizedTest
    @MethodSource("extractCases")
    fun `Extract, where pattern exists, with matchNumber=#matchNumber from #occurrences returns #expected`(case: ExtractCase) {
        val input = createInputString(case.occurrences)
        val matches = BoundaryExtractor.extract(LEFT, RIGHT, case.matchNumber, input)

        assertEquals(case.expected, matches)
    }

    @ParameterizedTest
    @ValueSource(ints = [-1, 0, 1, 2, 100])
    fun `Extract, where pattern does not exist, with matchNumber=#matchNumber returns an empty list`(matchNumber: Int) {
        assertEquals(listOf<Any>(), BoundaryExtractor.extract(LEFT, RIGHT, matchNumber, "start end"))
    }

    @ParameterizedTest
    @MethodSource("extractCasesStream")
    fun `Extract, where pattern exists in the stream, with matchNumber=#matchNumber from #occurrences returns #expected`(case: ExtractCase) {
        val input = createInputString(case.occurrences)
        val stream = (1..10).flatMap { listOf(input, "", null) }.stream()
        val matches = BoundaryExtractor.extract(LEFT, RIGHT, case.matchNumber, stream)

        assertEquals(case.expected, matches)
    }

    @ParameterizedTest
    @ValueSource(ints = [-1, 0, 1, 2, 100])
    fun `Extract, where pattern does not exist in the stream, with matchNumber=#matchNumber returns an empty list`(matchNumber: Int) {
        val stream: Stream<String> = (1..10).flatMap { listOf("start end") }.stream()

        assertEquals(listOf<Any>(), BoundaryExtractor.extract(LEFT, RIGHT, matchNumber, stream))
    }

    @Test
    fun `IllegalArgumentException when name (#name) is null`() {
        sut.leftBoundary = "l"
        sut.rightBoundary = "r"
        sut.refName = null

        assertThrows<IllegalArgumentException> {
            sut.process()
        }
    }

    @Test
    fun `matching only on left boundary returns default`() {
        sut.rightBoundary = "does-not-exist"
        sut.process()
        assertVarValueEquals(DEFAULT_VAL)
    }

    @Test
    fun `matching only on right boundary returns default`() {
        sut.leftBoundary = "does-not-exist"
        sut.process()
        assertVarValueEquals(DEFAULT_VAL)
    }

    @Test
    fun `variables from a previous extraction are removed`() {
        sut.matchNumber = -1
        sut.process()
        assertEquals("1", vars.get("${VAR_NAME}_1"))
        assertEquals("2", vars.get("${VAR_NAME}_matchNr"))

        // Now rerun with match fail
        sut.matchNumber = 10
        sut.process()
        assertVarValueEquals(DEFAULT_VAL)
        assertNull(vars.get("${VAR_NAME}_1"), "vars.get(${VAR_NAME}_1)")
        assertVarNameMatchNrEquals(null)
    }

    @Test
    fun `with no sub-samples parent and all scope return data but children scope does not`() {
        sut.setScopeParent()
        sut.process()
        assertVarValueEquals("1")

        sut.setScopeAll()
        sut.process()
        assertVarValueEquals("1")

        sut.setScopeChildren()
        sut.process()
        assertVarValueEquals(DEFAULT_VAL)
    }

    @Test
    fun `with sub-samples parent, all and children scope return expected item`() {
        prevResult.addSubResult(createSampleResult("${LEFT}sub1$RIGHT"))
        prevResult.addSubResult(createSampleResult("${LEFT}sub2$RIGHT"))
        prevResult.addSubResult(createSampleResult("${LEFT}sub3$RIGHT"))
        sut.setScopeParent()

        sut.process()

        assertVarValueEquals("1")

        sut.setScopeAll()
        sut.matchNumber = 3 // skip 2 in parent sample
        sut.process()
        assertVarValueEquals("sub1")

        sut.setScopeChildren()
        sut.matchNumber = 3
        sut.process()
        assertVarValueEquals("sub3")
    }

    @Test
    fun `with sub-samples parent, all and children scope return expected data`() {
        prevResult.addSubResult(createSampleResult("${LEFT}sub1$RIGHT"))
        prevResult.addSubResult(createSampleResult("${LEFT}sub2$RIGHT"))
        prevResult.addSubResult(createSampleResult("${LEFT}sub3$RIGHT"))
        sut.matchNumber = -1

        sut.setScopeParent()
        sut.process()

        assertVarNameMatchNrEquals("2")
        assertAllVars(listOf("1", "2"))

        sut.setScopeAll()
        sut.process()
        assertVarNameMatchNrEquals("5")
        assertAllVars(listOf("1", "2", "sub1", "sub2", "sub3"))

        sut.setScopeChildren()
        sut.process()
        assertVarNameMatchNrEquals("3")
        assertAllVars(listOf("sub1", "sub2", "sub3"))
    }

    @Test
    fun `when 'default empty value' is true the default value is allowed to be empty`() {
        sut.matchNumber = 10 // no matches
        sut.defaultValue = ""
        sut.setDefaultEmptyValue(true)
        sut.process()
        assertVarValueEquals("")
    }

    @Test
    fun `when default value is empty but not allowed null is returned`() {
        sut.matchNumber = 10 // no matches
        sut.defaultValue = ""
        sut.setDefaultEmptyValue(false)
        sut.process()
        assertVarValueEquals(null)
    }

    @Test
    fun `with no previous results result is null`() {
        context.previousResult = null
        sut.setDefaultEmptyValue(true)
        sut.process()
        assertVarValueEquals(null)
    }

    @Test
    fun `with non-existent variable result is null`() {
        sut.defaultValue = null
        sut.setScopeVariable("empty-var-name")
        sut.process()
        assertVarValueEquals(null)
    }

    @Test
    fun `not allowing blank default value returns null upon no matches`() {
        sut.matchNumber = 10 // no matches
        sut.defaultValue = ""
        sut.setDefaultEmptyValue(false)
        sut.process()
        assertVarValueEquals(null)
    }

    @Test
    fun `extract all matches from variable input`() {
        sut.matchNumber = -1
        sut.setScopeVariable("contentvar")
        vars.put("contentvar", createInputString(1..2))
        sut.process()
        assertAllVars(listOf("1", "2"))
        assertVarNameMatchNrEquals("2")
    }

    @Test
    fun `extract random from variable returns one of the matches`() {
        sut.matchNumber = 0
        sut.setScopeVariable("contentvar")
        vars.put("contentvar", createInputString(1..42))

        sut.process()

        (1..42).map { it.toString() }.contains(vars.get(VAR_NAME))
        assertVarNameMatchNrEquals(null)
    }

    @Test
    fun `extract all from an empty variable returns no results`() {
        sut.matchNumber = -1
        sut.setScopeVariable("contentvar")
        vars.put("contentvar", "")

        sut.process()
        assertVarNameMatchNrEquals("0")
        assertNull(vars.get("${VAR_NAME}_1"), "vars.get(${VAR_NAME}_1)")
    }

    @Test
    fun `previous extractions are cleared`() {
        sut.matchNumber = -1
        sut.setScopeVariable("contentvar")
        vars.put("contentvar", createInputString(1..10))
        sut.process()
        assertAllVars((1..10).map { it.toString() })
        assertVarNameMatchNrEquals("10")
        vars.put("contentvar", createInputString(11..15))
        sut.setMatchNumber("-1")
        val expectedMatches = (11..15).map { it.toString() }

        sut.process()

        assertAllVars(expectedMatches)
        assertVarNameMatchNrEquals("5")
        assertEquals(
            List(5) { null },
            (6..10).map { vars.get("${VAR_NAME}_$it") },
            "vars.get(${VAR_NAME}_6..10)"
        )

        sut.matchNumber = 0
        sut.process()

        val varValue = vars.get(VAR_NAME)
        if (varValue !in expectedMatches) {
            fail("expectedMatches ($expectedMatches) should contain value of $VAR_NAME variable $varValue")
        }
    }

    /**
     * @return a list of all the variables in the format ${VAR_NAME}_${i}
     * starting at i = 1 until null is returned
     */
    private fun getAllVars(): List<String> = buildList {
        var i = 1
        while (true) {
            val value = vars.get("${VAR_NAME}_$i") ?: break
            add(value)
            i++
        }
    }
}
