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

import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo
import com.github.tomakehurst.wiremock.junit5.WireMockTest
import org.apache.jmeter.junit.JMeterTestCase
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.beans.Introspector
import java.nio.file.Path
import kotlin.io.path.writeText

@WireMockTest
class AccessLogSamplerTest : JMeterTestCase() {
    @TempDir
    lateinit var dir: Path

    @Test
    fun `implementation is exposed as a test bean property`() {
        val property = Introspector.getBeanInfo(AccessLogSampler::class.java)
            .propertyDescriptors
            .singleOrNull { it.name == "implementation" }

        assertNotNull(property?.readMethod)
        assertNotNull(property?.writeMethod)
    }

    @ParameterizedTest
    @CsvSource(
        "Java,true,keep-alive",
        "Java,false,close",
        "HttpClient4,true,keep-alive",
        "HttpClient4,false,close",
        "HttpClient5,true,keep-alive",
        "HttpClient5,false,close",
    )
    fun `selected implementation honors keep alive`(
        implementation: String,
        keepAlive: Boolean,
        expectedConnection: String,
        server: WireMockRuntimeInfo,
    ) {
        val sampler = createSampler(server).apply {
            setImplementation(implementation)
            setUseKeepAlive(keepAlive)
        }

        try {
            assertSamplesAndConnectionHeaders(sampler, server, expectedConnection)
        } finally {
            sampler.testEnded()
        }
    }

    @Test
    fun `missing implementation and keep alive settings keep legacy defaults`(server: WireMockRuntimeInfo) {
        val sampler = createSampler(server)

        try {
            assertSamplesAndConnectionHeaders(sampler, server, "close")
        } finally {
            sampler.testEnded()
        }
    }

    private fun createSampler(server: WireMockRuntimeInfo): AccessLogSampler {
        server.wireMock.register(get("/one").willReturn(aResponse().withStatus(200)))
        server.wireMock.register(get("/two").willReturn(aResponse().withStatus(200)))

        val logFile = dir.resolve("access.log")
        logFile.writeText(
            """
            127.0.0.1 - - [21/Oct/2003:05:37:21 -0500] "GET /one HTTP/1.1" 200 10
            127.0.0.1 - - [21/Oct/2003:05:37:22 -0500] "GET /two HTTP/1.1" 200 10
            """.trimIndent(),
        )
        return AccessLogSampler().apply {
            setLogFile(logFile.toString())
            setParserClassName(AccessLogSampler.DEFAULT_CLASS)
            setDomain("localhost")
            setPortString(server.httpPort.toString())
        }
    }

    private fun assertSamplesAndConnectionHeaders(
        sampler: AccessLogSampler,
        server: WireMockRuntimeInfo,
        expectedConnection: String,
    ) {
        assertTrue(sampler.sampleWithParser().isSuccessful)
        assertTrue(sampler.sampleWithParser().isSuccessful)

        server.wireMock.verifyThat(
            getRequestedFor(urlEqualTo("/one")).withHeader("Connection", equalTo(expectedConnection)),
        )
        server.wireMock.verifyThat(
            getRequestedFor(urlEqualTo("/two")).withHeader("Connection", equalTo(expectedConnection)),
        )
    }
}
