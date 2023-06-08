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

package org.apache.jmeter.protocol.http

import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo
import com.github.tomakehurst.wiremock.junit5.WireMockTest
import org.apache.jmeter.junit.JMeterTestCase
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy
import org.apache.jmeter.samplers.SampleEvent
import org.apache.jmeter.test.assertions.executePlanAndCollectEvents
import org.apache.jmeter.threads.openmodel.OpenModelThreadGroup
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@WireMockTest
class HttpRequestInterruptTest : JMeterTestCase() {
    @Test
    @Timeout(5, unit = TimeUnit.SECONDS)
    fun `httpClient4 interrupts`(server: WireMockRuntimeInfo) {
        server.wireMock.register(
            get("/delayed")
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withFixedDelay(1.minutes.inWholeMilliseconds.toInt())
                )
        )

        val events = executePlanAndCollectEvents(50.seconds) {
            OpenModelThreadGroup::class {
                scheduleString = "rate(50 / sec) random_arrivals(100 ms) pause(1 s)"
                HTTPSamplerProxy::class {
                    method = "GET"
                    protocol = "http"
                    domain = "localhost"
                    port = server.httpPort
                    path = "/delayed"
                }
            }
        }

        assertEquals(
            listOf<SampleEvent>(),
            events,
            "No samples expected as all the threads should be interrupted before the request completes"
        )
    }
}
