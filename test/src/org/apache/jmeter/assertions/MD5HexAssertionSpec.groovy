/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License") you may not use this file except in compliance with
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

package org.apache.jmeter.assertions

import org.apache.commons.lang3.StringUtils
import org.apache.jmeter.samplers.SampleResult
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.charset.StandardCharsets

@Unroll
class MD5HexAssertionSpec extends Specification {

    def sut = new MD5HexAssertion()

    def "unset allowable hash with empty response fails"() {
        when:
            def result = sut.getResult(sampleResult(""))
        then:
            result.isFailure()
            StringUtils.isNotBlank(result.getFailureMessage())
    }

    def "incorrect hash #allowedHex causes result failure"() {
        given:
            sut.setAllowedMD5Hex(allowedHex)
        when:
            def result = sut.getResult(sampleResult("anything"))
        then:
            result.isFailure()
            StringUtils.isNotBlank(result.getFailureMessage())
        where:
            allowedHex << ["", "anything", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"]
    }

    def "example MD5s - '#sampleData' == '#hash'"() {
        given:
            sut.setAllowedMD5Hex(hash)
        when:
            def result = sut.getResult(sampleResult(sampleData))
        then:
            !result.isFailure()
            !result.isError()
            result.getFailureMessage() == null
        where:
            sampleData | hash
            "anything" | "f0e166dc34d14d6c228ffac576c9a43c"
            "anything" | "F0e166Dc34D14d6c228ffac576c9a43c"
    }

    def "empty array has MD5 hash of D41D8CD98F00B204E9800998ECF8427E"() {
        given:
            def emptyByteArray = [] as byte[]
        expect:
            MD5HexAssertion.md5Hex(emptyByteArray)
                    .toUpperCase(Locale.ENGLISH) == "D41D8CD98F00B204E9800998ECF8427E"
    }

    def sampleResult(String data) {
        SampleResult response = new SampleResult()
        response.setResponseData(data.getBytes(StandardCharsets.UTF_8))
        return response
    }
}
