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
