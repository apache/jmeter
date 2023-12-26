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

package org.apache.jmeter.visualizers.backend.graphite

import io.mockk.mockk
import io.mockk.verify
import org.apache.commons.pool2.impl.GenericKeyedObjectPool
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class TextGraphiteMetricsSenderTest {

    private val sut = TextGraphiteMetricsSender()

    private fun assertMetricsIsEmpty() {
        Assertions.assertTrue(sut.metrics.isEmpty(), ".metrics.isEmpty()")
    }

    @Test
    fun `new sender has no metrics`() {
        assertMetricsIsEmpty()
    }

    @Test
    fun `adding metric to sender creates correct MetricTuple`() {
        val expectedTS = 1000000L
        val expectedVal = "value"

        sut.setup("host", 1024, "prefix-")
        sut.addMetric(expectedTS, "contextName", "metricName", expectedVal)

        Assertions.assertEquals(
            "MetricTuple(name=prefix-contextName.metricName, timestamp=1000000, value=value)",
            sut.metrics.joinToString { "MetricTuple(name=${it.name}, timestamp=${it.timestamp}, value=${it.value})" },
            ".metrics"
        )
    }

    @Test
    fun `writeAndSendMetrics does not attempt connection if there's nothing to send`() {
        sut.setup("non-existant-host", 1024, "prefix-")
        sut.writeAndSendMetrics()
        assertMetricsIsEmpty()
    }

    @Test
    fun `writeAndSendMetrics connects and sends if there's something to send, dropping metrics on connection failure, without throwing exceptions`() {
        val socketConnInfoMock = mockk<SocketConnectionInfos>()
        val objectPoolStub = mockk<GenericKeyedObjectPool<SocketConnectionInfos, SocketOutputStream>>()
        sut.setup(socketConnInfoMock, objectPoolStub, "prefix-")
        sut.addMetric(1, "contextName", "metricName", "val")
        sut.writeAndSendMetrics()
        verify(exactly = 1) { objectPoolStub.borrowObject(socketConnInfoMock) }
        assertMetricsIsEmpty()
    }

    @Test
    fun `destroy closes outputStreamPool`() {
        val objectPoolStub = mockk<GenericKeyedObjectPool<SocketConnectionInfos, SocketOutputStream>>(relaxed = true)
        sut.setup(mockk<SocketConnectionInfos>(), objectPoolStub, "prefix-")
        sut.addMetric(1, "contextName", "metricName", "val")

        sut.destroy()

        verify(exactly = 1) { objectPoolStub.close() }
    }
}
