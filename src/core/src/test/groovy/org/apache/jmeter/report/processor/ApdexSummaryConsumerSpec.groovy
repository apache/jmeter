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

package org.apache.jmeter.report.processor

import spock.lang.Specification

class ApdexSummaryConsumerSpec extends Specification {

    def sut = new ApdexSummaryConsumer()

    def "createDataResult contains apdex, satisfied, tolerated, key"() {
        given:
            def info = new ApdexThresholdsInfo()
            info.setSatisfiedThreshold(3L)
            info.setToleratedThreshold(12L)
            def data = new ApdexSummaryData(info)
            data.satisfiedCount = 60L
            data.toleratedCount = 30L
            data.totalCount = 100L
            def expectedApdex = 0.75
        when:
            def result = sut.createDataResult("key", data)
        then:
            def resultValues = result.asList().collect {
                ((ValueResultData) it).value
            }
            // [apdex, satisfied, tolerated, key]
            resultValues == [expectedApdex, 3L, 12L, "key"]
    }
}
