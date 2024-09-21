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

package org.apache.jmeter.dsl.action

import org.apache.jmeter.control.IfController
import org.apache.jmeter.control.WhileController
import org.apache.jmeter.dsl.DslPrinterTraverser
import org.apache.jmeter.reporters.Summariser
import org.apache.jmeter.test.samplers.ThreadSleep
import org.apache.jmeter.threads.openmodel.OpenModelThreadGroup
import org.apache.jmeter.treebuilder.dsl.testTree
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DslPrinterTraverserTest {
    @Test
    fun threadGroup() {
        val d = testTree {
            OpenModelThreadGroup::class {
                name = "thread\n group"
                props {
                    it[enabled] = "\${var_name}"
                }
                scheduleString = "rate(50/sec)"

                +Summariser::class

                WhileController::class {
                    condition = "while \"condition\" \$ "
                    IfController::class {
                        condition = "\${__P(abc, def)}"
                        ThreadSleep::class {
                            isEnabled = false
                        }
                    }
                }
            }
        }

        val str = DslPrinterTraverser()
            .also { d.traverse(it) }
            .toString()

        assertEquals(
            """
            org.apache.jmeter.threads.openmodel.OpenModelThreadGroup::class {
                props {
                    it[name] = "thread\n group"
                    it[enabled] = "\${'$'}{var_name}"
                    it[schedule] = "rate(50/sec)"
                }

                +org.apache.jmeter.reporters.Summariser::class

                org.apache.jmeter.control.WhileController::class {
                    props {
                        it[condition] = "while \"condition\" \$ "
                    }

                    org.apache.jmeter.control.IfController::class {
                        props {
                            it[condition] = "\${'$'}{__P(abc, def)}"
                        }

                        org.apache.jmeter.test.samplers.ThreadSleep::class {
                            props {
                                it[enabled] = false
                            }
                        }
                    }
                }
            }

            """.trimIndent().replace("\r\n", "\n"),
            str.replace("\r\n", "\n")
        )
    }
}
