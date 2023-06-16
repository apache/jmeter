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
import com.github.tomakehurst.wiremock.client.WireMock.containing
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo
import com.github.tomakehurst.wiremock.junit5.WireMockTest
import org.apache.jmeter.control.LoopController
import org.apache.jmeter.junit.JMeterTestCase
import org.apache.jmeter.protocol.http.util.HTTPFileArg
import org.apache.jmeter.test.assertions.executePlanAndCollectEvents
import org.apache.jmeter.threads.ThreadGroup
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.nio.file.InvalidPathException
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.writeText
import kotlin.time.Duration.Companion.seconds

@WireMockTest
class HttpSamplerTest : JMeterTestCase() {
    @TempDir
    lateinit var dir: Path

    @ParameterizedTest
    @ValueSource(strings = ["Java", "HttpClient4"])
    fun `upload file uses percent encoding for filename`(httpImplementation: String, server: WireMockRuntimeInfo) {
        server.wireMock.register(
            post("/upload")
                .willReturn(
                    aResponse()
                        .withStatus(200)
                )
        )

        // Quote is invalid character for a filename in Windows, so we do not test it here
        // See ConversionUtilsTest for escaping quotes.
        val testFile = try {
            dir.resolve("testfile привет %.txt")
        } catch (e: InvalidPathException) {
            assumeTrue(false) {
                "Skipping the test as the filesystem does not suppport unicode filenames"
            }
            TODO("This is never reached as the assumption above throws error")
        }
        testFile.writeText("hello, привет")

        executePlanAndCollectEvents(10.seconds) {
            ThreadGroup::class {
                numThreads = 1
                rampUp = 0
                setSamplerController(
                    LoopController().apply {
                        loops = 1
                    }
                )
                org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy::class {
                    name = "Upload file"
                    implementation = httpImplementation
                    method = "POST"
                    domain = "localhost"
                    port = server.httpPort
                    path = "/upload"
                    httpFiles = arrayOf(
                        HTTPFileArg(testFile.absolutePathString(), "file_parameter", "application/octet-stream")
                    )
                }
            }
        }

        server.wireMock.verifyThat(
            1,
            postRequestedFor(urlEqualTo("/upload"))
                .withAnyRequestBodyPart(
                    aMultipart("file_parameter")
                        .withHeader(
                            "Content-Disposition",
                            containing("filename=\"testfile%20%D0%BF%D1%80%D0%B8%D0%B2%D0%B5%D1%82%20%25.txt\"")
                        )
                        .withBody(equalTo("hello, привет"))
                )
        )
    }
}
