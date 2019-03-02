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


import org.apache.jmeter.report.core.Sample
import org.apache.jmeter.report.utils.MetricUtils
import spock.lang.Specification

class Top5ErrorsBySamplerConsumerSpec extends Specification {

    def sut = new Top5ErrorsBySamplerConsumer()

    def "summary info data updated with non-controller passing sample"() {
        given:
            def mockSummaryInfo = Mock(AbstractSummaryConsumer.SummaryInfo)
            def mockSample = Mock(Sample) {
                getSuccess() >> true
            }
        when:
            sut.updateData(mockSummaryInfo, mockSample)
        then:
            def data = (Top5ErrorsSummaryData) mockSummaryInfo.getData()
            data.getTotal() == 1
    }

    def "summary info data updated with non-controller failing sample"() {
        given:
            def mockSummaryInfo = Mock(AbstractSummaryConsumer.SummaryInfo)
            def mockSample = Mock(Sample) {
                getResponseCode() >> "200"
            }
        when:
            sut.updateData(mockSummaryInfo, mockSample)
        then:
            def data = (Top5ErrorsSummaryData) mockSummaryInfo.getData()
            data.getTotal() == 1
            data.getErrors() == 1
            data.top5ErrorsMetrics[0][0] == MetricUtils.ASSERTION_FAILED
            def overallData = (Top5ErrorsSummaryData) sut.getOverallInfo().getData()
            overallData.getTotal() == 1
            overallData.getErrors() == 1
            overallData.top5ErrorsMetrics[0][0] == MetricUtils.ASSERTION_FAILED
    }

    def "key from sample is name"() {
        given:
            def mockSample = Mock(Sample)
        when:
            def key = sut.getKeyFromSample(mockSample)
        then:
            1 * mockSample.getName() >> "name"
            key == "name"
    }

    def "there are 3 + 2n expected results title columns"() {
        expect:
            sut.createResultTitles().size ==
                    3 + 2 * sut.MAX_NUMBER_OF_ERRORS_IN_TOP
    }

}
