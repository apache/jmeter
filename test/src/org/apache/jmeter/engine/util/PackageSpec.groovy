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
 */

package org.apache.jmeter.engine.util

import org.apache.jmeter.junit.spock.JMeterSpec
import org.apache.jmeter.samplers.SampleResult
import org.apache.jmeter.testelement.property.JMeterProperty
import org.apache.jmeter.testelement.property.StringProperty
import org.apache.jmeter.threads.JMeterContextService
import org.apache.jmeter.threads.JMeterVariables
import spock.lang.Unroll

/**
 * To run this test stand-alone, ensure that ApacheJMeter_functions.jar is on the classpath,
 * as it is needed to resolve the functions.
 */
class PackageSpec extends JMeterSpec {

    def transformer
    def jmctx

    def setup() {
        def variables = ["my_regex": ".*",
                         "server"  : "jakarta.apache.org"]
        transformer = new ReplaceStringWithFunctions(new CompoundVariable(), variables)
        jmctx = JMeterContextService.getContext()
        jmctx.setVariables(new JMeterVariables())
        jmctx.setSamplingStarted(true)
        def result = new SampleResult()
        result.setResponseData('<a>hello world</a> costs: $3.47,$5.67', null)
        jmctx.setPreviousResult(result)
        jmctx.getVariables().put("server", "jakarta.apache.org")
        jmctx.getVariables().put("my_regex", ".*")
    }

    def testFunctionParse1() {
        given:
            StringProperty prop = new StringProperty("date",
                    '${__javaScript((new Date().getDate() / 100).toString()' +
                            '.substr(${__javaScript(1+1,d\\,ay)}\\,2),heute)}')
        when:
            JMeterProperty newProp = transformer.transformValue(prop)
            newProp.setRunningVersion(true)
        then:
            newProp.getClass().getName() == "org.apache.jmeter.testelement.property.FunctionProperty"
            newProp.recoverRunningVersion(null)
            Integer.parseInt(newProp.getStringValue()) >= 0
            jmctx.getVariables().getObject("d,ay") == "2"
    }

    @Unroll
    def "test parsing StringProperty '#propertyValue' == '#stringValue'"() {
        given:
            StringProperty prop = new StringProperty("a", propertyValue)
        when:
            JMeterProperty newProp = transformer.transformValue(prop)
            newProp.setRunningVersion(true)
        then:
            newProp.getClass().getName() == 'org.apache.jmeter.testelement.property.StringProperty'
            newProp.getStringValue() == stringValue
        where:
            propertyValue    | stringValue
            ""               | ""
            "just some text" | "just some text"
    }

    @Unroll
    def "test parsing FunctionProperty '#propertyValue' == '#stringValue'"() {
        given:
            StringProperty prop = new StringProperty("a", propertyValue)
        when:
            JMeterProperty newProp = transformer.transformValue(prop)
            newProp.setRunningVersion(true)
        then:
            newProp.getClass().getName() == 'org.apache.jmeter.testelement.property.FunctionProperty'
            newProp.getStringValue() == stringValue
        where:
            propertyValue                                                                | stringValue
            '${__regexFunction(<a>(.*)</a>,$1$)}'                                        | "hello world"
            'It should say:\\${${__regexFunction(<a>(.+o)(.*)</a>,$1$$2$)}}'             | 'It should say:${hello world}'
            '${non - existing; function}'                                                | '${non - existing; function}'
            '${server}'                                                                  | "jakarta.apache.org"
            '${__regexFunction(<([a-z]*)>,$1$)}'                                         | "a"
            '${__regexFunction((\\\\$\\d+\\.\\d+),$1$)}'                                 | '$3.47'
            '${__regexFunction(([$]\\d+\\.\\d+),$1$)}'                                   | '$3.47'
            '${__regexFunction((\\\\\\$\\d+\\.\\d+\\,\\\\$\\d+\\.\\d+),$1$)}'            | '$3.47,$5.67'

            // Nested examples
            '${__regexFunction(<a>(${my_regex})</a>,$1$)}'                               | "hello world"
            '${__regexFunction(<a>(${my_regex})</a>,$1$)}${__regexFunction(<a>(.),$1$)}' | "hello worldh"
    }

    @Unroll
    def "Backslashes are removed before escaped dollar, comma and backslash with FunctionProperty"() {
        // N.B. See Bug 46831 which wanted to changed the behaviour of \$
        // It's too late now, as this would invalidate some existing test plans,
        // so document the current behaviour with some more tests.
        given:
            StringProperty prop = new StringProperty("a", propertyValue)
        when:
            JMeterProperty newProp = transformer.transformValue(prop)
            newProp.setRunningVersion(true)
        then:
            newProp.getClass().getName() == 'org.apache.jmeter.testelement.property.' + className
            newProp.getStringValue() == stringValue

        where:
            propertyValue | className        | stringValue
            // With no variable reference
            '\\$a'        | "StringProperty" | '\\$a'
            '\\,'         | "StringProperty" | '\\,'
            '\\\\'        | "StringProperty" | '\\\\'
            '\\'          | "StringProperty" | '\\'
            '\\x'         | "StringProperty" | '\\x'
        and: // With variable reference
            '\\$a \\, \\\\ \\ \\x ${server} \\$b\\,z'       | "FunctionProperty" | '$a , \\ \\ \\x jakarta.apache.org $b,z'
            '\\$a \\, \\\\ \\ \\x ${__missing(a)} \\$b\\,z' | "FunctionProperty" | '$a , \\ \\ \\x ${__missing(a)} $b,z'
            '\\$a \\, \\\\ \\ \\x ${missing}      \\$b\\,z' | "FunctionProperty" | '$a , \\ \\ \\x ${missing}      $b,z'
            '\\$a \\, \\\\ ${ z'                            | "FunctionProperty" | '$a , \\  z'
    }

}
