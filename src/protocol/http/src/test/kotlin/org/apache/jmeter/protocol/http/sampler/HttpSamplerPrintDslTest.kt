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

import org.apache.jmeter.dsl.DslPrinterTraverser
import org.apache.jmeter.junit.JMeterTestCase
import org.apache.jmeter.protocol.http.control.gui.HttpTestSampleGui
import org.apache.jmeter.testelement.TestElement
import org.apache.jmeter.treebuilder.dsl.testTree
import org.apache.jmeter.util.JMeterUtils
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.ResourceLock
import org.junit.jupiter.api.parallel.Resources
import java.util.Locale

@ResourceLock(value = Resources.LOCALE)
class HttpSamplerPrintDslTest : JMeterTestCase() {
    companion object {
        private var locale = JMeterUtils.getLocale()

        @JvmStatic
        @BeforeAll
        fun setup() {
            // Ensure DSL uses English text, otherwise we can't reliably compare expected values
            JMeterUtils.setLocale(Locale.ENGLISH)
        }

        @JvmStatic
        @AfterAll
        fun tearDown() {
            JMeterUtils.setLocale(locale)
        }
    }

    @Test
    fun `http sampler created with UI`() {
        val ui = HttpTestSampleGui()
        val element = ui.createTestElement()
        val tree = testTree {
            +element
        }

        assertEquals(
            """
            org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy::class {
                props {
                    it[arguments] = org.apache.jmeter.config.Arguments().apply {
                        props {
                            it[name] = "User Defined Variables"
                            it[guiClass] = "org.apache.jmeter.protocol.http.gui.HTTPArgumentsPanel"
                            it[testClass] = "org.apache.jmeter.config.Arguments"
                        }
                    }
                    it[method] = "GET"
                    it[followRedirects] = true
                    it[useKeepalive] = true
                    it[implementation] = "HttpClient4"
                    it[name] = "HTTP Request"
                    it[guiClass] = "org.apache.jmeter.protocol.http.control.gui.HttpTestSampleGui"
                }
            }

            """.trimIndent().replace("\r\n", "\n"),
            DslPrinterTraverser().also { tree.traverse(it) }.toString().replace("\r\n", "\n")
        )
    }

    @Test
    fun `http sampler created with DSL`() {
        val createdWithUi = HttpTestSampleGui().createTestElement()
        val createdWithDsl = testTree {
            org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy::class {
                props {
                    it[arguments] = org.apache.jmeter.config.Arguments().apply {
                        props {
                            it[name] = "User Defined Variables"
                            it[guiClass] = "org.apache.jmeter.protocol.http.gui.HTTPArgumentsPanel"
                            it[testClass] = "org.apache.jmeter.config.Arguments"
                        }
                    }
                    it[method] = "GET"
                    it[followRedirects] = true
                    it[useKeepalive] = true
                    it[implementation] = "HttpClient4"
                    it[name] = "HTTP Request"
                    it[guiClass] = "org.apache.jmeter.protocol.http.control.gui.HttpTestSampleGui"
                }
            }
        }.keys.first() as TestElement

        // We compare elements manually, and call assertEquals(toString, toString) so
        // the test output looks better (diff in IDE) in case of the failure
        // If we use just assertEquals(createdWithUi, createdWithDsl), then there will be no "diff in IDE"
        if (createdWithUi != createdWithDsl) {
            assertEquals(
                DslPrinterTraverser(DslPrinterTraverser.DetailLevel.ALL).append(createdWithUi).toString(),
                DslPrinterTraverser(DslPrinterTraverser.DetailLevel.ALL).append(createdWithDsl).toString()
            ) {
                "Elements created with UI and DSL should match since DSL was generated based on the UI-generated element"
            }
        }
    }

    @Test
    fun `dsl for proxy`() {
        val createdWithDsl = testTree {
            org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy::class {
                props {
                    proxy {
                        it[host] = "proxyhost"
                        it[port] = 8080
                        it[username] = "username"
                        it[password] = "password"
                    }
                }
            }
        }.keys.first() as TestElement

        assertEquals(
            """
            org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy::class {
                props {
                    it[proxy.host] = "proxyhost"
                    it[proxy.port] = 8080
                    it[proxy.username] = "username"
                    it[proxy.password] = "password"
                }
            }

            """.trimIndent(),
            DslPrinterTraverser().append(createdWithDsl).toString()
        ) {
            "Proxy parameters added with a group should be printed with DslPrinterTraverser"
        }
        assertEquals(
            "proxyhost",
            createdWithDsl.getPropertyAsString("HTTPSampler.proxyHost"),
            """getPropertyAsString("HTTPSampler.proxyHost")"""
        )
        assertEquals(
            8080,
            createdWithDsl.getPropertyAsInt("HTTPSampler.proxyPort"),
            """getPropertyAsInt("HTTPSampler.proxyPort")"""
        )
    }
}
