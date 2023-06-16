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
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerFactory
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy
import org.apache.jmeter.test.assertions.executePlanAndCollectEvents
import org.apache.jmeter.threads.openmodel.OpenModelThreadGroup
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.api.fail
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@WireMockTest
class HttpRequestInterruptTest : JMeterTestCase() {
    @ParameterizedTest
    @Timeout(10, unit = TimeUnit.SECONDS)
    @ValueSource(strings = [HTTPSamplerFactory.IMPL_HTTP_CLIENT4, HTTPSamplerFactory.HTTP_SAMPLER_JAVA])
    fun `http request interrupts`(httpImplementation: String, server: WireMockRuntimeInfo) {
        server.wireMock.register(
            get("/delayed")
                .willReturn(
                    aResponse()
                        .withFixedDelay(1.minutes.inWholeMilliseconds.toInt())
                        .withStatus(200)
                )
        )

        val events = executePlanAndCollectEvents(5.seconds) {
            OpenModelThreadGroup::class {
                scheduleString = "rate(50 / sec) random_arrivals(100 ms) pause(1 s)"
                HTTPSamplerProxy::class {
                    implementation = httpImplementation
                    method = "GET"
                    protocol = "http"
                    domain = "localhost"
                    port = server.httpPort
                    path = "/delayed"
                }
            }
        }

        assertEquals(5, events.size) { "5 events expected, got $events" }
        if (events.any { it.result.isSuccessful || it.result.isResponseCodeOK || it.result.time < 500 }) {
            fail(
                "All events should be failing, and they should take more than 500ms since the requests " +
                    "should have been cancelled after 1sec. Results are: $events"
            )
        }
    }
}
