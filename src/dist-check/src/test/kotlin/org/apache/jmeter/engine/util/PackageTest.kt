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

package org.apache.jmeter.engine.util

import org.apache.jmeter.samplers.SampleResult
import org.apache.jmeter.testelement.property.FunctionProperty
import org.apache.jmeter.testelement.property.StringProperty
import org.apache.jmeter.threads.JMeterContext
import org.apache.jmeter.threads.JMeterContextService
import org.apache.jmeter.threads.JMeterVariables
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

/**
 * To run this test stand-alone, ensure that ApacheJMeter_functions.jar is on the classpath,
 * as it is needed to resolve the functions.
 */
class PackageTest {

    lateinit var transformer: ReplaceStringWithFunctions
    lateinit var jmctx: JMeterContext

    data class TransformCase(
        val propertyValue: String,
        val stringValue: String,
        val type: Class<*> = StringProperty::class.java,
    )

    companion object {
        @JvmStatic
        fun transformCases() = listOf(
            TransformCase("", ""),
            TransformCase("just some text", "just some text"),
            TransformCase("\${__regexFunction(<a>(.*)</a>,$1$)}", "hello world", type = FunctionProperty::class.java),
            TransformCase(
                "It should say:\\\${\${__regexFunction(<a>(.+o)(.*)</a>,$1$$2$)}}",
                "It should say:\${hello world}",
                type = FunctionProperty::class.java,
            ),
            TransformCase("\${non - existing; function}", "\${non - existing; function}", type = FunctionProperty::class.java),
            TransformCase("\${server}", "jakarta.apache.org", type = FunctionProperty::class.java),
            TransformCase("\${__regexFunction(<([a-z]*)>,$1$)}", "a", type = FunctionProperty::class.java),
            TransformCase("\${__regexFunction((\\\\$\\d+\\.\\d+),$1$)}", "$3.47", type = FunctionProperty::class.java),
            TransformCase("\${__regexFunction(([$]\\d+\\.\\d+),$1$)}", "$3.47", type = FunctionProperty::class.java),
            TransformCase("\${__regexFunction((\\\\\\$\\d+\\.\\d+\\,\\\\$\\d+\\.\\d+),$1$)}", "$3.47,$5.67", type = FunctionProperty::class.java),
            // Nested examples
            TransformCase("\${__regexFunction(<a>(\${my_regex})</a>,$1$)}", "hello world", type = FunctionProperty::class.java),
            TransformCase(
                "\${__regexFunction(<a>(\${my_regex})</a>,$1$)}\${__regexFunction(<a>(.),$1$)}",
                "hello worldh",
                type = FunctionProperty::class.java,
            ),
            // N.B. See Bug 46831 which wanted to changed the behaviour of \$
            // It's too late now, as this would invalidate some existing test plans,
            // so document the current behaviour with some more tests.
            // With no variable reference
            TransformCase("\\\$a", "\\\$a"),
            TransformCase("\\,", "\\,"),
            TransformCase("\\\\", "\\\\"),
            TransformCase("\\", "\\"),
            TransformCase("\\x", "\\x"),
            TransformCase(
                "\\\$a \\, \\\\ \\ \\x \${server} \\\$b\\,z",
                "\$a , \\ \\ \\x jakarta.apache.org \$b,z",
                type = FunctionProperty::class.java
            ),
            TransformCase(
                "\\\$a \\, \\\\ \\ \\x \${__missing(a)} \\\$b\\,z",
                "\$a , \\ \\ \\x \${__missing(a)} \$b,z",
                type = FunctionProperty::class.java
            ),
            TransformCase(
                "\\\$a \\, \\\\ \\ \\x \${missing}      \\\$b\\,z",
                "\$a , \\ \\ \\x \${missing}      \$b,z",
                type = FunctionProperty::class.java
            ),
            TransformCase("\\\$a \\, \\\\ \${ z", "\$a , \\  z", type = FunctionProperty::class.java),
        )
    }

    @BeforeEach
    fun setup() {
        val variables = mapOf(
            "my_regex" to ".*",
            "server" to "jakarta.apache.org"
        )
        transformer = ReplaceStringWithFunctions(CompoundVariable(), variables)
        jmctx = JMeterContextService.getContext()
        jmctx.variables = JMeterVariables()
        jmctx.isSamplingStarted = true
        val result = SampleResult()
        result.setResponseData("<a>hello world</a> costs: $3.47,$5.67", null)
        jmctx.previousResult = result
        jmctx.variables.put("server", "jakarta.apache.org")
        jmctx.variables.put("my_regex", ".*")
    }

    @Test
    fun testFunctionParse1() {
        val prop = StringProperty(
            "date",
            "\${__javaScript((new Date().getDate() / 100).toString()" +
                ".substr(\${__javaScript(1+1,d\\,ay)}\\,2),heute)}"
        )
        val newProp = transformer.transformValue(prop)
        newProp.setRunningVersion(true)

        assertEquals(FunctionProperty::class.java, newProp::class.java, "class of property $prop after transformation")
        newProp.recoverRunningVersion(null)
        newProp.getStringValue().let {
            if (it?.toIntOrNull().let { it == null || it < 0 }) {
                fail("Property value should be positive integer, but was: $it")
            }
        }
        assertEquals("2", jmctx.variables.getObject("d,ay"), "Variable 'd,ay' value")
    }

    @ParameterizedTest
    @MethodSource("transformCases")
    fun transformValue(case: TransformCase) {
        val prop = StringProperty("a", case.propertyValue)
        val newProp = transformer.transformValue(prop)
        newProp.setRunningVersion(true)

        assertEquals(
            case.stringValue,
            newProp.getStringValue(),
            "stringValue of property with input ${case.propertyValue} (should parse as ${case.type})"
        )
        assertEquals(
            case.type,
            newProp::class.java,
            "class of property with input ${case.propertyValue} after transformation"
        )
    }
}
