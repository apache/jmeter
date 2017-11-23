package org.apache.jmeter.report.processor

import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class ListResultDataSpec extends Specification {

    def sut = new ListResultData()

    def "addResult adds #object to list and returns true"() {
        given:
            assert sut.getSize() == 0
        when:
            def result = sut.addResult(object)
        then:
            result
            sut.getSize() == 1
            sut.get(0) == object
        where:
            object << [null, Mock(ResultData), new ListResultData()]
    }
}
