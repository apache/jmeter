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

package org.apache.jmeter.visualizers.backend.graphite

import org.apache.commons.pool2.impl.GenericKeyedObjectPool

import spock.lang.Specification

class TextGraphiteMetricsSenderSpec extends Specification {

    def sut = new TextGraphiteMetricsSender()

    def "new sender has no metrics"() {
        expect:
            sut.metrics.isEmpty()
    }

    def "adding metric to sender creates correct MetricTuple"() {
        given:
            def expectedName = "prefix-contextName.metricName"
            def expectedTS = 1000000
            def expectedVal = "value"

            sut.setup("host", 1024, "prefix-")
        when:
            sut.addMetric(expectedTS, "contextName", "metricName", expectedVal)
        then:
            def actualMetrics = sut.metrics
            actualMetrics.size() == 1
            def actualMetric = actualMetrics.get(0)
            actualMetric.name == expectedName
            actualMetric.timestamp == expectedTS
            actualMetric.value == expectedVal
    }

    def "writeAndSendMetrics does not attempt connection if there's nothing to send"() {
        given:
            sut.setup("non-existant-host", 1024, "prefix-")
        when:
            sut.writeAndSendMetrics()
        then:
            sut.metrics.isEmpty()
            noExceptionThrown()
    }

    def "writeAndSendMetrics connects and sends if there's something to send, dropping metrics on connection failure, without throwing exceptions"() {
        given:
            SocketConnectionInfos socketConnInfoMock = Mock()
            GenericKeyedObjectPool<SocketConnectionInfos, SocketOutputStream> objectPoolStub = Mock()
            sut.setup(socketConnInfoMock, objectPoolStub, "prefix-")
            sut.addMetric(1, "contextName", "metricName", "val")
        when:
            sut.writeAndSendMetrics()
        then:
            1 *  objectPoolStub.borrowObject(socketConnInfoMock)
            sut.metrics.isEmpty()
            noExceptionThrown()
    }

    def "destroy closes outputStreamPool"() {
        given:
            GenericKeyedObjectPool<SocketConnectionInfos, SocketOutputStream> objectPoolStub = Mock()
            sut.setup(Mock(SocketConnectionInfos), objectPoolStub, "prefix-")
            sut.addMetric(1, "contextName", "metricName", "val")
        when:
            sut.destroy()
        then:
            1 *  objectPoolStub.close()
            // TODO: should destroy also set metrics to null or are we relying on the original reference to be removed after destroy is called?
            sut.metrics.size() == 1
            noExceptionThrown()
    }

}
