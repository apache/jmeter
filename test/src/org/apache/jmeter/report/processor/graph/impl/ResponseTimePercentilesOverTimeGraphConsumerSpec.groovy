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
