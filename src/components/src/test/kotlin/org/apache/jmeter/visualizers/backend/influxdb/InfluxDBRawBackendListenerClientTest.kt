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

package org.apache.jmeter.visualizers.backend.influxdb

import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import org.apache.jmeter.samplers.SampleResult
import org.apache.jmeter.visualizers.backend.BackendListenerContext
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail

class InfluxDBRawBackendListenerClientTest {

    val sut = InfluxDBRawBackendListenerClient()
    private val defaultContext = BackendListenerContext(sut.getDefaultParameters())

    private fun createOkSample(): SampleResult {
        val t = 1600123456789
        return SampleResult.createTestSample(t - 100, t).apply {
            latency = 42
            connectTime = 7
            sampleLabel = "myLabel"
            threadName = "myThread"
            setResponseOK()
        }
    }

    @Test
    fun `Default parameters contain minimum required options`() {
        val actualKeys = sut.getDefaultParameters()
            .getArgumentsAsMap()
            .keys
        val expectedKeys = setOf(
            "influxdbMetricsSender", "influxdbUrl",
            "influxdbToken", "measurement"
        )
        if (!actualKeys.containsAll(expectedKeys)) {
            fail("Default arguments $actualKeys should include all keys of $expectedKeys")
        }
    }

    @Test
    fun `Provided args are used during setup`() {
        sut.setupTest(defaultContext)
        assertEquals("jmeter", sut.measurement, ".measurement")
        assertEquals(
            HttpMetricsSender::class.java,
            sut.influxDBMetricsManager::class.java,
            ".influxDBMetricsManager.class"
        )
    }

    @Test
    fun `OK sample data is mapped correctly to InfluxDB tags and fields`() {
        val okSample = createOkSample()
        assertEquals(
            "status=ok,transaction=myLabel,threadName=myThread",
            InfluxDBRawBackendListenerClient.createTags(okSample),
            "createTags($okSample)"
        )
        assertEquals(
            "duration=100,ttfb=42,connectTime=7",
            InfluxDBRawBackendListenerClient.createFields(okSample),
            "createFields($okSample)"
        )
    }

    @Test
    fun `Failed sample data is mapped correctly to InfluxDB tags and fields`() {
        val koSample = SampleResult().apply {
            sampleLabel = "myLabel"
            threadName = "myThread"
        }
        assertEquals(
            "status=ko,transaction=myLabel,threadName=myThread",
            InfluxDBRawBackendListenerClient.createTags(koSample),
            "createTags($koSample)"
        )
    }

    @Test
    fun `Upon handling sample result data is added to influxDBMetricsManager and written`() {
        val mockSender = mockk<InfluxdbMetricsSender>(relaxed = true)
        val sut = InfluxDBRawBackendListenerClient(mockSender)
        sut.handleSampleResults(listOf(createOkSample()), defaultContext)
        verifyOrder {
            mockSender.addMetric(any(), any(), any(), any())
            mockSender.writeAndSendMetrics()
        }
    }

    @Test
    fun `teardownTest calls destroy on influxDBMetricsManager`() {
        val mockSender = mockk<InfluxdbMetricsSender>(relaxed = true)
        val sut = InfluxDBRawBackendListenerClient(mockSender)
        sut.teardownTest(defaultContext)
        verify(exactly = 1) {
            mockSender.destroy()
        }
    }
}
