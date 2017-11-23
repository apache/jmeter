package org.apache.jmeter.report.processor

import org.apache.jmeter.junit.spock.JMeterSpec
import org.apache.jmeter.report.core.Sample

class Top5ErrorsBySamplerConsumerSpec extends JMeterSpec {

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
            data.top5ErrorsMetrics[0][0] == ErrorsSummaryConsumer.ASSERTION_FAILED
            def overallData = (Top5ErrorsSummaryData) sut.getOverallInfo().getData()
            overallData.getTotal() == 1
            overallData.getErrors() == 1
            overallData.top5ErrorsMetrics[0][0] == ErrorsSummaryConsumer.ASSERTION_FAILED
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
