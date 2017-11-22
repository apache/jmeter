package org.apache.jmeter.report.processor

import spock.lang.Specification

class Top5ErrorsSummaryDataSpec extends Specification {

    def sut = new Top5ErrorsSummaryData()

    def "when no errors are registered an array with null values is returned"() {
        expect:
            sut.getTop5ErrorsMetrics() == new Object[5][2]
    }

    def "error messages with the same frequency are preserved up until the size limit"() {
        given:
            ["A", "B", "C", "D", "E", "F"].each { sut.registerError(it) }
        expect:
            sut.getTop5ErrorsMetrics() == [["A", 1], ["B", 1], ["C", 1], ["D", 1], ["E", 1]]
    }

    def "error messages are sorted by size, descending"() {
        given:
            ["A", "A", "A", "B", "B", "C"].each {
                sut.registerError(it)
            }
        expect:
            sut.getTop5ErrorsMetrics() == [["A", 3], ["B", 2], ["C", 1], [null, null], [null, null]]
    }

    def "error and total count start at 0"() {
        expect:
            sut.getErrors() == 0
            sut.getTotal() == 0
    }

    def "error and total count increment by one each time"() {
        when:
            sut.incErrors()
            sut.incTotal()
        then:
            sut.getErrors() == 1
            sut.getTotal() == 1
    }

}
