/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.apache.jmeter.protocol.http.sampler

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.apache.jmeter.threads.JMeterContextHolder
import org.apache.jmeter.threads.JMeterVariables
import org.apache.jmeter.threads.newFixedThreadPoolContext
import org.apache.jmeter.util.JMeterContextExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(value = [JMeterContextExtension::class])
class NonBlockingHttpSamplerTest {
    private fun WireMockServer.configureStubs(responseDelay: Int) {
        stubFor(
            WireMock.get("/index.html")
                .willReturn(
                    WireMock.aResponse()
                        .withBody("Hello, world")
                        .withFixedDelay(responseDelay)
                )
        )
    }

    private fun createServer(): WireMockServer {
        val configuration = WireMockConfiguration
            .wireMockConfig()
            .dynamicPort()
            .asynchronousResponseEnabled(true)
        return WireMockServer(configuration)
    }

    @Test
    fun jetty() {
        `many inflight requests`(HTTPSamplerFactory.IMPL_JETTY_NON_BLOCKING)
    }

    @Test
    fun httpclient5() {
        `many inflight requests`(HTTPSamplerFactory.IMPL_APACHE_HTTPCLIENT5_NON_BLOCKING)
    }

    fun `many inflight requests`(impl: String) {
        val responseDelay = 10
        val server = createServer().apply {
            start()
            configureStubs(responseDelay)
        }

        try {
            val jmeterTheadContext = newFixedThreadPoolContext(3, "async http client")

            val now = System.currentTimeMillis()
            runBlocking {
                val results = Channel<HTTPSampleResult>()
                launch {
                    var i = 0
                    for (r in results) {
                        i++
                        println("$i) ${r.startTime - now}...${r.endTime - now} = ${r.endTime - r.startTime}, code: ${r.responseCode}, bodySize: ${r.bodySizeAsLong}, ${r.responseDataAsString}")
                    }
                }
                withTimeout(12000L + responseDelay) {
                    coroutineScope {
                        repeat(200) {
                            val ctx = JMeterContextHolder().apply {
                                jmeterContext.variables = JMeterVariables()
                            }
                            delay(10)
                            launch(jmeterTheadContext + ctx) {
                                results.send(send(impl, server))
                            }
                        }
                    }
                }
                results.close()
            }
        } finally {
            server.stop()
        }
    }

    private suspend fun send(
        impl: String,
        server: WireMockServer
    ): HTTPSampleResult {
        val http = HTTPSamplerFactory.newInstance(impl)

        http.domain = "localhost"
        http.port = server.port()
        http.path = "/index.html"
        http.followRedirects = true
        http.useKeepAlive = true
        return http.suspendingSample() as HTTPSampleResult
    }
}
