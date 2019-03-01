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

package org.apache.jmeter.assertions.gui

import org.apache.jmeter.assertions.ResponseAssertion
import org.apache.jmeter.junit.spock.JMeterSpec

/**
 * Extending JMeterSpec is required to initialize resource bundle org.apache.jmeter.resource.messages
 */
class AssertionGuiSpec extends JMeterSpec {

    def sut = new AssertionGui()

    def "init of new component does not throw an exception"() {
        when:
            sut.init()
        then:
            noExceptionThrown()
    }

    def "clearing GUI component fields does not throw an exception"() {
        when:
            sut.clearGui()
        then:
            noExceptionThrown()
    }

    def "Creation of ResponseAssertion sets name of element and enables it"() {
        when:
            def result = sut.createTestElement()
        then:
            result.getName() == "Response Assertion"
            result.isEnabled()
    }

    def "Modification of ResponseAssertion by GUI has no unexpected behaviour"() {
        given:
            def element = new ResponseAssertion()
            sut.clearGui()
        when:
            sut.modifyTestElement(element)
        then:
            element.getName() == "Response Assertion"
            element.isTestFieldResponseData()
            element.getTestStrings().isEmpty()
            !element.getAssumeSuccess()
            !element.isNotType()
            element.isSubstringType()
    }

    def "Modification of GUI by ResponseAssertion does not throw an exception"() {
        given:
            def element = new ResponseAssertion()
        when:
            sut.configure(element)
        then:
            noExceptionThrown()
    }
}
