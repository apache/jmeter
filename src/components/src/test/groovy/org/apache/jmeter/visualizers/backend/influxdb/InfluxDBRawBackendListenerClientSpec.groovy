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

import org.apache.jmeter.samplers.SampleResult
import org.apache.jmeter.visualizers.backend.BackendListenerContext

import spock.lang.Specification

class InfluxDBRawBackendListenerClientSpec extends Specification {

    def sut = new InfluxDBRawBackendListenerClient()
    def defaultContext = new BackendListenerContext(sut.getDefaultParameters())

    def createOkSample() {
        def t = 1600123456789
        def okSample = SampleResult.createTestSample(t - 100, t)
        okSample.setLatency(42)
        okSample.setConnectTime(7)
        okSample.setSampleLabel("myLabel")
        okSample.setResponseOK()
        return okSample
    }

    def "Default parameters contain minimum required options"() {
        expect:
            sut.getDefaultParameters()
                    .getArgumentsAsMap()
                    .keySet()
                    .containsAll([
                            "influxdbMetricsSender", "influxdbUrl",
                            "influxdbToken", "measurement"])
    }

    def "Provided args are used during setup"() {
        when:
            sut.setupTest(defaultContext)
        then:
            sut.measurement == sut.DEFAULT_MEASUREMENT
            sut.influxDBMetricsManager.class.isAssignableFrom(HttpMetricsSender.class)
    }

    def "OK sample data is mapped correctly to InfluxDB tags and fields"() {
        given:
            def okSample = createOkSample()
        when:
            def tags = sut.createTags(okSample)
            def fields = sut.createFields(okSample)
        then:
            tags == "status=ok,transaction=myLabel"
            fields == "duration=100,ttfb=42,connectTime=7"
    }

    def "Failed sample data is mapped correctly to InfluxDB tags and fields"() {
        given:
            def koSample = new SampleResult()
            koSample.setSampleLabel("myLabel")
        expect:
            sut.createTags(koSample) == "status=ko,transaction=myLabel"
    }

    def "Upon handling sample result data is added to influxDBMetricsManager and written"() {
        given:
            def mockSender = Mock(InfluxdbMetricsSender)
            def sut = new InfluxDBRawBackendListenerClient(mockSender)
        when:
            sut.handleSampleResults([createOkSample()], defaultContext)
        then:
            1 * mockSender.addMetric(_, _, _, _)
            1 * mockSender.writeAndSendMetrics()
    }

    def "teardownTest calls destroy on influxDBMetricsManager"() {
        given:
            def mockSender = Mock(InfluxdbMetricsSender)
            def sut = new InfluxDBRawBackendListenerClient(mockSender)
        when:
            sut.teardownTest()
        then:
            1 * mockSender.destroy()
    }
}
