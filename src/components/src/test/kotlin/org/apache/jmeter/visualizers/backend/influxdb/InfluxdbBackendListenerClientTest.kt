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

import org.apache.jmeter.visualizers.backend.BackendListenerContext
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class InfluxdbBackendListenerClientTest {

    val sut = InfluxdbBackendListenerClient()
    private val defaultContext = BackendListenerContext(sut.getDefaultParameters())

    @Test
    fun `setupTest with default config does not raise an exception`() {
        sut.setupTest(defaultContext)
    }

    @Test
    fun `Sending metrics when empty does not raise an exception`() {
        sut.setupTest(defaultContext)
        sut.run()
    }

    @Test
    fun `Default parameters are equal to default args`() {
        assertEquals(
            mapOf(
                "influxdbMetricsSender" to "org.apache.jmeter.visualizers.backend.influxdb.HttpMetricsSender",
                "influxdbUrl" to "http://host_to_change:8086/write?db=jmeter",
                "application" to "application name",
                "measurement" to "jmeter",
                "summaryOnly" to "false",
                "samplersRegex" to ".*",
                "percentiles" to "99;95;90",
                "testTitle" to "Test name",
                "eventTags" to ""
            ),
            sut.getDefaultParameters().getArgumentsAsMap()
        )
    }
}
