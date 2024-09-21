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
import com.github.tomakehurst.wiremock.client.WireMock.matching
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo
import com.github.tomakehurst.wiremock.junit5.WireMockTest
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder
import org.apache.jmeter.control.LoopController
import org.apache.jmeter.junit.JMeterTestCase
import org.apache.jmeter.protocol.http.control.Header
import org.apache.jmeter.protocol.http.util.HTTPFileArg
import org.apache.jmeter.test.assertions.executePlanAndCollectEvents
import org.apache.jmeter.threads.ThreadGroup
import org.apache.jmeter.treebuilder.TreeBuilder
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.nio.charset.Charset
import java.nio.file.InvalidPathException
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.writeText
import kotlin.time.Duration.Companion.seconds

@WireMockTest
class HttpSamplerTest : JMeterTestCase() {
    @TempDir
    lateinit var dir: Path

    fun TreeBuilder.oneRequest(body: ThreadGroup.() -> Unit) {
        ThreadGroup::class {
            numThreads = 1
            rampUp = 0
            setSamplerController(
                LoopController().apply {
                    loops = 1
                }
            )
            body()
        }
    }

    fun TreeBuilder.httpPost(body: HTTPSamplerProxy.() -> Unit) {
        org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy::class {
            name = "Upload file"
            method = "POST"
            domain = "localhost"
            path = "/upload"
            doMultipart = true
            body()
        }
    }

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
                "Skipping the test as the filesystem does not support unicode filenames"
            }
            TODO("This is never reached as the assumption above throws error")
        }
        testFile.writeText("hello, привет")

        executePlanAndCollectEvents(10.seconds) {
            oneRequest {
                httpPost {
                    implementation = httpImplementation
                    port = server.httpPort
                    httpFiles = arrayOf(
                        HTTPFileArg(testFile.absolutePathString(), "file_parameter", "application/octet-stream")
                    )
                    if (!Charset.defaultCharset().name().equals("UTF-8")) {
                        // Content-Encoding: UTF-8 header is not really required, however Wiremock uses commons-fileupload
                        // to parse Content-Disposition, and we need Content-Encoding to workaround
                        // https://issues.apache.org/jira/browse/FILEUPLOAD-206
                        org.apache.jmeter.protocol.http.control.HeaderManager::class {
                            add(Header("Content-Encoding", "UTF-8"))
                            props {
                                // guiClass is needed for org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase.applies
                                it[guiClass] = "org.apache.jmeter.protocol.http.gui.HeaderPanel"
                            }
                        }
                    }
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
                            // Only CR, LF, and % should be percent-encoded
                            containing("filename=\"testfile привет %.txt\"")
                        )
                        .withBody(equalTo("hello, привет"))
                )
        )
    }

    fun RequestPatternBuilder.withRequestBody(
        httpImplementation: String,
        body: String
    ) = apply {
        // normalize line endings to CRLF
        val normalizedBody = body.replace("\r\n", "\n").replace("\n", "\r\n")
        withRequestBody(
            if (httpImplementation == "Java") {
                equalTo(normalizedBody)
            } else {
                matching(
                    normalizedBody
                        .replace(PostWriter.BOUNDARY, "[^ \\n\\r]{1,69}?")
                )
            }
        )
    }

    @ParameterizedTest
    @ValueSource(strings = ["Java", "HttpClient4"])
    fun `one parameter`(httpImplementation: String, server: WireMockRuntimeInfo) {
        server.wireMock.register(
            post("/upload").willReturn(aResponse().withStatus(200))
        )

        executePlanAndCollectEvents(10.seconds) {
            oneRequest {
                httpPost {
                    implementation = httpImplementation
                    port = server.httpPort
                    addArgument("hello", "world")
                }
            }
        }

        server.wireMock.verifyThat(
            1,
            postRequestedFor(urlEqualTo("/upload"))
                .withRequestBodyPart(
                    aMultipart("hello")
                        .withBody(equalTo("world"))
                        .build()
                )
                .withRequestBody(
                    httpImplementation,
                    """
                    -----------------------------7d159c1302d0y0
                    Content-Disposition: form-data; name="hello"
                    Content-Type: text/plain; charset=UTF-8
                    Content-Transfer-Encoding: 8bit

                    world
                    -----------------------------7d159c1302d0y0--

                    """.trimIndent()
                )
        )
    }

    @ParameterizedTest
    @ValueSource(strings = ["Java", "HttpClient4"])
    fun `two parameters`(httpImplementation: String, server: WireMockRuntimeInfo) {
        server.wireMock.register(
            post("/upload").willReturn(aResponse().withStatus(200))
        )

        executePlanAndCollectEvents(10.seconds) {
            oneRequest {
                httpPost {
                    implementation = httpImplementation
                    port = server.httpPort
                    addArgument("hello", "world")
                    addArgument("name", "Tim")
                }
            }
        }

        server.wireMock.verifyThat(
            1,
            postRequestedFor(urlEqualTo("/upload"))
                .withRequestBodyPart(
                    aMultipart("hello").withBody(equalTo("world")).build()
                )
                .withRequestBodyPart(
                    aMultipart("name").withBody(equalTo("Tim")).build()
                )
                .withRequestBody(
                    httpImplementation,
                    """
                    -----------------------------7d159c1302d0y0
                    Content-Disposition: form-data; name="hello"
                    Content-Type: text/plain; charset=UTF-8
                    Content-Transfer-Encoding: 8bit

                    world
                    -----------------------------7d159c1302d0y0
                    Content-Disposition: form-data; name="name"
                    Content-Type: text/plain; charset=UTF-8
                    Content-Transfer-Encoding: 8bit

                    Tim
                    -----------------------------7d159c1302d0y0--

                    """.trimIndent()
                )
        )
    }

    @ParameterizedTest
    @ValueSource(strings = ["Java", "HttpClient4"])
    fun `two parameters and file`(httpImplementation: String, server: WireMockRuntimeInfo) {
        server.wireMock.register(
            post("/upload").willReturn(aResponse().withStatus(200))
        )

        val testFile = dir.resolve("testfile.txt").apply {
            writeText("file contents")
        }

        executePlanAndCollectEvents(10.seconds) {
            oneRequest {
                httpPost {
                    implementation = httpImplementation
                    port = server.httpPort
                    addArgument("hello", "world")
                    addArgument("name", "Tim")
                    httpFiles = arrayOf(
                        HTTPFileArg(testFile.absolutePathString(), "file_parameter", "application/octet-stream")
                    )
                }
            }
        }

        server.wireMock.verifyThat(
            1,
            postRequestedFor(urlEqualTo("/upload"))
                .withRequestBodyPart(
                    aMultipart("hello").withBody(equalTo("world")).build()
                )
                .withRequestBodyPart(
                    aMultipart("name").withBody(equalTo("Tim")).build()
                )
                .withRequestBody(
                    httpImplementation,
                    """
                    -----------------------------7d159c1302d0y0
                    Content-Disposition: form-data; name="hello"
                    Content-Type: text/plain; charset=UTF-8
                    Content-Transfer-Encoding: 8bit

                    world
                    -----------------------------7d159c1302d0y0
                    Content-Disposition: form-data; name="name"
                    Content-Type: text/plain; charset=UTF-8
                    Content-Transfer-Encoding: 8bit

                    Tim
                    -----------------------------7d159c1302d0y0
                    Content-Disposition: form-data; name="file_parameter"; filename="testfile.txt"
                    Content-Type: application/octet-stream
                    Content-Transfer-Encoding: binary

                    file contents
                    -----------------------------7d159c1302d0y0--

                    """.trimIndent()
                )
        )
    }

    @ParameterizedTest
    @ValueSource(strings = ["Java", "HttpClient4"])
    fun `two parameters and two files`(httpImplementation: String, server: WireMockRuntimeInfo) {
        server.wireMock.register(
            post("/upload").willReturn(aResponse().withStatus(200))
        )

        val testFile1 = dir.resolve("testfile1.txt").apply {
            writeText("file contents1")
        }
        val testFile2 = dir.resolve("testfile2.txt").apply {
            writeText("file contents2")
        }

        executePlanAndCollectEvents(10.seconds) {
            oneRequest {
                httpPost {
                    implementation = httpImplementation
                    port = server.httpPort
                    addArgument("hello", "world")
                    addArgument("name", "Tim")
                    httpFiles = arrayOf(
                        HTTPFileArg(testFile1.absolutePathString(), "file_parameter", "application/octet-stream"),
                        HTTPFileArg(testFile2.absolutePathString(), "file_parameter", "application/octet-stream"),
                    )
                }
            }
        }

        server.wireMock.verifyThat(
            1,
            postRequestedFor(urlEqualTo("/upload"))
                .withRequestBodyPart(
                    aMultipart("hello").withBody(equalTo("world")).build()
                )
                .withRequestBodyPart(
                    aMultipart("name").withBody(equalTo("Tim")).build()
                )
                .withRequestBody(
                    httpImplementation,
                    """
                    -----------------------------7d159c1302d0y0
                    Content-Disposition: form-data; name="hello"
                    Content-Type: text/plain; charset=UTF-8
                    Content-Transfer-Encoding: 8bit

                    world
                    -----------------------------7d159c1302d0y0
                    Content-Disposition: form-data; name="name"
                    Content-Type: text/plain; charset=UTF-8
                    Content-Transfer-Encoding: 8bit

                    Tim
                    -----------------------------7d159c1302d0y0
                    Content-Disposition: form-data; name="file_parameter"; filename="testfile1.txt"
                    Content-Type: application/octet-stream
                    Content-Transfer-Encoding: binary

                    file contents1
                    -----------------------------7d159c1302d0y0
                    Content-Disposition: form-data; name="file_parameter"; filename="testfile2.txt"
                    Content-Type: application/octet-stream
                    Content-Transfer-Encoding: binary

                    file contents2
                    -----------------------------7d159c1302d0y0--

                    """.trimIndent()
                )
        )
    }
}
