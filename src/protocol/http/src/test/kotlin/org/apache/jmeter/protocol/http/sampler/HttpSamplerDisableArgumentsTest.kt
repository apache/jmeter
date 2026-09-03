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

package org.apache.jmeter.protocol.http.sampler

import com.github.tomakehurst.wiremock.client.WireMock.aMultipart
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.put
import com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo
import com.github.tomakehurst.wiremock.junit5.WireMockTest
import org.apache.jmeter.junit.JMeterTestCase
import org.apache.jmeter.protocol.http.control.arguments
import org.apache.jmeter.protocol.http.control.httpRequestDefaults
import org.apache.jmeter.protocol.http.util.HTTPArgument
import org.apache.jmeter.test.assertions.executePlanAndCollectEvents
import org.apache.jmeter.treebuilder.TreeBuilder
import org.apache.jmeter.treebuilder.oneRequest
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import kotlin.time.Duration.Companion.seconds

@WireMockTest
class HttpSamplerDisableArgumentsTest : JMeterTestCase() {

    fun TreeBuilder.httpRequest(body: HTTPSamplerProxy.() -> Unit) {
        HTTPSamplerProxy::class {
            name = "Test disabled params"
            method = "GET"
            domain = "localhost"
            path = "/test"
            body()
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["Java", "HttpClient4"])
    fun `GET disable param1 should send enabled param2`(httpImplementation: String, server: WireMockRuntimeInfo) {
        server.wireMock.register(
            get("/test").willReturn(aResponse().withStatus(200))
        )

        executePlanAndCollectEvents(10.seconds) {
            oneRequest {
                httpRequest {
                    method = "GET"
                    implementation = httpImplementation
                    port = server.httpPort
                    addArgument("param1", "value1")
                    arguments.getArgument(0).isEnabled = false
                    addArgument("param2", "value2")
                }
            }
        }

        server.wireMock.verifyThat(
            1,
            getRequestedFor(urlEqualTo("/test?param2=value2"))
        )
    }

    @ParameterizedTest
    @ValueSource(strings = ["Java", "HttpClient4"])
    fun `PUT disable param2 should send enabled param1 and param3`(httpImplementation: String, server: WireMockRuntimeInfo) {
        server.wireMock.register(
            put("/test").willReturn(aResponse().withStatus(200))
        )

        executePlanAndCollectEvents(1000.seconds) {
            oneRequest {
                httpRequest {
                    method = "PUT"
                    implementation = httpImplementation
                    port = server.httpPort
                    postBodyRaw = true
                    addArgument("param1", "value1")
                    addArgument("param2", "value2")
                    arguments.getArgument(1).isEnabled = false
                    addArgument("param3", "value3")
                }
            }
        }

        server.wireMock.verifyThat(
            1,
            putRequestedFor(urlEqualTo("/test"))
                .withRequestBody(equalTo("value1value3"))
        )
    }

    @ParameterizedTest
    @ValueSource(strings = ["Java", "HttpClient4"])
    fun `POST disable default and non-default param should send the only enabled non-default param`(httpImplementation: String, server: WireMockRuntimeInfo) {
        server.wireMock.register(
            post("/test").willReturn(aResponse().withStatus(200))
        )

        executePlanAndCollectEvents(10.seconds) {
            oneRequest {
                httpRequestDefaults {
                    arguments {
                        addArgument(
                            HTTPArgument("param0", "value0").apply {
                                isEnabled = false
                            }
                        )
                        addArgument(
                            HTTPArgument("param4", "value4")
                        )
                    }
                }
                httpRequest {
                    method = "POST"
                    doMultipart = true
                    implementation = httpImplementation
                    port = server.httpPort
                    addArgument("param1", "value1")
                    arguments.getArgument(0).isEnabled = false
                    addArgument("param2", "value2")
                }
            }
        }

        server.wireMock.verifyThat(
            1,
            postRequestedFor(urlEqualTo("/test"))
                .withRequestBodyPart(
                    aMultipart("param2").withBody(equalTo("value2")).build()
                )
                .withRequestBodyPart(
                    aMultipart("param4").withBody(equalTo("value4")).build()
                )
                .withRequestBody(
                    httpImplementation,
                    """
                    -----------------------------7d159c1302d0y0
                    Content-Disposition: form-data; name="param2"
                    Content-Type: text/plain; charset=UTF-8
                    Content-Transfer-Encoding: 8bit

                    value2
                    -----------------------------7d159c1302d0y0
                    Content-Disposition: form-data; name="param4"
                    Content-Type: text/plain; charset=UTF-8
                    Content-Transfer-Encoding: 8bit

                    value4
                    -----------------------------7d159c1302d0y0--

                    """.trimIndent()
                )
        )
    }
}
