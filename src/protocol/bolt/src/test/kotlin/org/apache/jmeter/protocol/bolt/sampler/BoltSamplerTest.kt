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

package org.apache.jmeter.protocol.bolt.sampler

import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import org.apache.jmeter.protocol.bolt.config.BoltConnectionElement
import org.apache.jmeter.samplers.Entry
import org.apache.jmeter.samplers.SampleResult
import org.apache.jmeter.threads.JMeterContextService
import org.apache.jmeter.threads.JMeterVariables
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.neo4j.driver.Driver
import org.neo4j.driver.Record
import org.neo4j.driver.Result
import org.neo4j.driver.Session
import org.neo4j.driver.exceptions.ClientException
import org.neo4j.driver.summary.ResultSummary
import org.neo4j.driver.summary.SummaryCounters

class BoltSamplerTest {
    lateinit var sampler: BoltSampler
    lateinit var entry: Entry
    lateinit var session: Session

    @BeforeEach
    fun setup() {
        sampler = BoltSampler()
        entry = Entry()
        session = mockk<Session> {
            justRun { close() }
        }
        val driver = mockk<Driver> {
            every { session(any()) } returns session
        }
        val boltConfig = BoltConnectionElement()
        val variables = JMeterVariables()
        // ugly but could not find a better way to pass the driver to the sampler...
        variables.putObject(BoltConnectionElement.BOLT_CONNECTION, driver)
        JMeterContextService.getContext().variables = variables
        entry.addConfigElement(boltConfig)
    }

    @Test
    fun `should execute return success on successful query`() {
        sampler.cypher = "MATCH x"
        every {
            session.run("MATCH x", mapOf(), any())
        } returns getEmptyQueryResult()
        val response = sampler.sample(entry)

        assertSuccessResult(response, "Records: Skipped")
    }

    private fun assertSuccessResult(response: SampleResult, responseDataTail: String) {
        val str = response.responseDataAsString
        assertTrue(str.contains("Summary:"), "response contains 'Summary:', got '$str'")
        assertTrue(str.endsWith(responseDataTail), "response ends with '$responseDataTail', got '$str'")
        assertTrue(response.isSuccessful, ".isSuccessful()")
        assertTrue(response.isResponseCodeOK, ".isResponseCodeOK()")
        assertEquals(1, response.sampleCount, ".sampleCount")
        assertEquals(0, response.errorCount, ".errorCount")
        assertSamplerStarted(response)
    }

    private fun assertSamplerStarted(response: SampleResult) {
        assertTrue(response.startTime > 0, "The sampler was executed, so start and end times should be set")
        assertTrue(response.endTime > 0, "The sampler was executed, so start and end times should be set")
    }

    private fun assertFailureResult(
        response: SampleResult,
        responseCode: String,
        message: String,
        samplerStarted: Boolean
    ) {
        val str = response.responseDataAsString
        assertFalse(str.contains("Summary:"), "response contains 'Summary:', got $str")
        assertTrue(str.contains(message), "response contains '$message', got $str")
        assertFalse(response.isSuccessful, ".isSuccessful()")
        assertFalse(response.isResponseCodeOK, ".isResponseCodeOK()")
        assertEquals(responseCode, response.responseCode, ".responseCode")
        assertEquals(1, response.sampleCount, ".sampleCount")
        assertEquals(1, response.errorCount, ".errorCount")
        if (samplerStarted) {
            assertSamplerStarted(response)
        } else {
            assertSamplerNotStarted(response)
        }
    }

    private fun assertSamplerNotStarted(response: SampleResult) {
        assertEquals(0, response.startTime, "The sampler fails at parameter preparation, so no time is recorded")
        assertEquals(0, response.endTime, "The sampler fails at parameter preparation, so no time is recorded")
    }

    @Test
    fun `should not display results by default`() {
        sampler.cypher = "MATCH x"
        every {
            session.run("MATCH x", mapOf(), any())
        } returns getPopulatedQueryResult()
        val response = sampler.sample(entry)

        assertSuccessResult(response, "Records: Skipped")
    }

    @Test
    fun `should display results if asked`() {
        sampler.cypher = "MATCH x"
        sampler.isRecordQueryResults = true
        every {
            session.run("MATCH x", mapOf(), any())
        } returns getPopulatedQueryResult()
        val response = sampler.sample(entry)
        assertSuccessResult(response, "Mock for type 'Record'")
    }

    @Test
    fun `should return error on failed query`() {
        sampler.cypher = "MATCH x"
        every { session.run("MATCH x", mapOf(), any()) } throws RuntimeException("a message")
        val response = sampler.sample(entry)

        assertFailureResult(response, "500", "a message", samplerStarted = true)
    }

    @Test
    fun `should return error on invalid parameters`() {
        sampler.cypher = "MATCH x"
        sampler.params = "{invalid}"
        val response = sampler.sample(entry)

        assertFailureResult(response, "500", "Unexpected character", samplerStarted = false)
    }

    @Test
    fun `should return db error code`() {
        sampler.cypher = "MATCH x"
        every { session.run("MATCH x", mapOf(), any()) } throws ClientException("a code", "a message")
        val response = sampler.sample(entry)
        assertEquals("a code", response.responseCode)
    }

    @Test
    fun `should ignore invalid timeout values`() {
        sampler.cypher = "MATCH x"
        sampler.txTimeout = -1
        every { session.run("MATCH x", mapOf(), any()) } returns getEmptyQueryResult()
        val response = sampler.sample(entry)
        assertSuccessResult(response, "Records: Skipped")
    }

    private fun getEmptyQueryResult() =
        mockk<Result> {
            every { consume() } returns mockk<ResultSummary> {
                every { counters() } returns mockk<SummaryCounters>(relaxed = true)
            }
        }

    @Suppress("LABEL_NAME_CLASH")
    private fun getPopulatedQueryResult() =
        mockk<Result> {
            every { consume() } returns mockk<ResultSummary> {
                every { counters() } returns mockk<SummaryCounters>(relaxed = true)
            }
            every { list() } returns listOf(
                mockk<Record> { every { this@mockk.toString() } returns "Mock for type 'Record'" },
                mockk<Record> { every { this@mockk.toString() } returns "Mock for type 'Record'" },
                mockk<Record> { every { this@mockk.toString() } returns "Mock for type 'Record'" },
            )
        }
}
