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

package org.apache.jmeter.assertions

import java.nio.charset.StandardCharsets

import org.apache.commons.lang3.StringUtils
import org.apache.jmeter.samplers.SampleResult

import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class CompareAssertionSpec extends Specification {

    def sut = new CompareAssertion()

    def "Result is simple assertionResult when only one response is given"() {
        given:
            sut.setName("myName")
            sut.iterationStart(null)
        when:
            def assertionResult = sut.getResult(null)
        then:
            assertionResult.getName() == "myName"
    }

    def "content '#content' with compareContent==#compareContent, skip=#skip, elapsed=#elapsed and compareTime==#compareTime"() {
        given:
            sut.setName("myName")
            def firstResponse = simpleResult("OK", 100)
            sut.iterationStart(null)
            sut.getResult(firstResponse)
            sut.setCompareContent(compareContent)
            sut.setCompareTime(compareTime)
            if (skip != null) {
                def subst = new SubstitutionElement()
                subst.setRegex(skip)
                sut.setStringsToSkip(Arrays.asList(subst))
            }
        when:
            def assertionResult = sut.getResult(simpleResult(content, elapsed))
        then:
            assertionResult.isFailure() == isFailure
        where:
            compareContent | compareTime | content        | elapsed | skip        | isFailure
            true           | -1          | "OK"           | 100     | null        | false
            true           | -1          | "different"    | 100     | null        | true
            false          | -1          | "different"    | 100     | null        | false
            false          | -1          | "different OK" | 100     | "d\\w+\\s"  | false
            true           | 10          | "OK"           | 120     | null        | true
            true           | 10          | "OK"           | 110     | null        | false
    }

    private SampleResult simpleResult(String data, long elapsed) {
        def result = new SampleResult(0L, elapsed)
        result.setResponseData(data, StandardCharsets.UTF_8.name())
        result.sampleEnd()
        return result
    }
}
