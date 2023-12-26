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

package org.apache.jmeter.visualizers.backend.influxdb

import org.apache.jmeter.visualizers.backend.BackendListenerContext

import spock.lang.Specification

class InfluxdbBackendListenerClientSpec extends Specification {

    def sut = new InfluxdbBackendListenerClient()
    def defaultContext = new BackendListenerContext(sut.getDefaultParameters())

    def "setupTest with default config does not raise an exception"() {
        when:
            sut.setupTest(defaultContext)
        then:
            noExceptionThrown()
    }

    def "Sending metrics when empty does not raise an exception"() {
        given:
            sut.setupTest(defaultContext)
        when:
            sut.run()
        then:
            noExceptionThrown()
    }

    def "Default parameters are equal to default args"() {
        expect:
            sut.getDefaultParameters().getArgumentsAsMap() == InfluxdbBackendListenerClient.DEFAULT_ARGS
    }
}
