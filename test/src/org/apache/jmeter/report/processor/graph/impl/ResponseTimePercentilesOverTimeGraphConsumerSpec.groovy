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
 
 package org.apache.jmeter.report.processor.graph.impl

import org.apache.jmeter.junit.spock.JMeterSpec

import java.util.stream.Collectors

class ResponseTimePercentilesOverTimeGraphConsumerSpec extends JMeterSpec {

    static def EXPECTED_KEYS =
            ['aggregate_report_min',
             'aggregate_report_max',
             'aggregate_rpt_pct1',
             'aggregate_rpt_pct2',
             'aggregate_rpt_pct3'] as Set

    def sut = new ResponseTimePercentilesOverTimeGraphConsumer()

    def "GroupInfos have only the required keys"() {
        when:
            def groupInfosMap = sut.createGroupInfos()
        then:
            groupInfosMap.keySet() == EXPECTED_KEYS
    }

    def "GroupInfos have the expected settings"() {
        when:
            def groupInfos = sut.createGroupInfos()
            def groupInfoValues = groupInfos
                    .entrySet().stream()
                    .map { it.value }
                    .collect(Collectors.toList())
        then:
            groupInfoValues.every { !it.enablesAggregatedKeysSeries() }
            groupInfoValues.every { !it.enablesOverallSeries() }
    }

}
