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

package org.apache.jmeter.protocol.bolt.sampler

import org.apache.jmeter.protocol.bolt.config.BoltConnectionElement
import org.apache.jmeter.samplers.Entry
import org.apache.jmeter.threads.JMeterContextService
import org.apache.jmeter.threads.JMeterVariables
import org.neo4j.driver.Driver
import org.neo4j.driver.Record
import org.neo4j.driver.Result
import org.neo4j.driver.Session
import org.neo4j.driver.exceptions.ClientException
import org.neo4j.driver.summary.ResultSummary
import org.neo4j.driver.summary.SummaryCounters

import spock.lang.Specification

class BoltSamplerSpec extends Specification {

    BoltSampler sampler
    Entry entry
    Session session

    def setup() {
        sampler = new BoltSampler()
        entry = new Entry()
        def driver = Mock(Driver)
        def boltConfig = new BoltConnectionElement()
        def variables = new JMeterVariables()
        // ugly but could not find a better way to pass the driver to the sampler...
        variables.putObject(BoltConnectionElement.BOLT_CONNECTION, driver)
        JMeterContextService.getContext().setVariables(variables)
        entry.addConfigElement(boltConfig)
        session = Mock(Session)
        driver.session(_) >> session
    }

    def "should execute return success on successful query"() {
        given:
            sampler.setCypher("MATCH x")
            session.run("MATCH x", [:], _) >> getEmptyQueryResult()
        when:
            def response = sampler.sample(entry)
        then:
            response.isSuccessful()
            response.isResponseCodeOK()
            def str = response.getResponseDataAsString()
            str.contains("Summary:")
            str.endsWith("Records: Skipped")
            response.getSampleCount() == 1
            response.getErrorCount() == 0
            response.getTime() > 0
    }

    def "should not display results by default"() {
        given:
            sampler.setCypher("MATCH x")
            session.run("MATCH x", [:], _) >> getPopulatedQueryResult()
        when:
            def response = sampler.sample(entry)
        then:
            response.isSuccessful()
            response.isResponseCodeOK()
            def str = response.getResponseDataAsString()
            str.contains("Summary:")
            str.endsWith("Records: Skipped")
            response.getSampleCount() == 1
            response.getErrorCount() == 0
    }

    def "should display results if asked"() {
        given:
            sampler.setCypher("MATCH x")
            sampler.setRecordQueryResults(true)
            session.run("MATCH x", [:], _) >> getPopulatedQueryResult()
        when:
            def response = sampler.sample(entry)
        then:
            response.isSuccessful()
            response.isResponseCodeOK()
            def str = response.getResponseDataAsString()
            str.contains("Summary:")
            str.endsWith("Mock for type 'Record'")
            response.getSampleCount() == 1
            response.getErrorCount() == 0
            response.getTime() > 0
    }

    def "should return error on failed query"() {
        given:
            sampler.setCypher("MATCH x")
            session.run("MATCH x", [:], _) >> { throw new RuntimeException("a message") }
        when:
            def response = sampler.sample(entry)
        then:
            !response.isSuccessful()
            !response.isResponseCodeOK()
            response.getResponseCode() == "500"
            def str = response.getResponseDataAsString()
            str.contains("a message")
            response.getSampleCount() == 1
            response.getErrorCount() == 1
            response.getTime() > 0
    }

    def "should return error on invalid parameters"() {
        given:
            sampler.setCypher("MATCH x")
            sampler.setParams("{invalid}")
        when:
            def response = sampler.sample(entry)
        then:
            !response.isSuccessful()
            !response.isResponseCodeOK()
            response.getResponseCode() == "500"
            def str = response.getResponseDataAsString()
            str.contains("Unexpected character")
            response.getSampleCount() == 1
            response.getErrorCount() == 1
            response.getTime() == 0
    }

    def "should return db error code"() {
        given:
            sampler.setCypher("MATCH x")
            session.run("MATCH x", [:], _) >> { throw new ClientException("a code", "a message") }
        when:
            def response = sampler.sample(entry)
        then:
            response.getResponseCode() == "a code"
    }

    def "should ignore invalid timeout values"() {
        given:
            sampler.setCypher("MATCH x")
            sampler.setTxTimeout(-1)
            session.run("MATCH x", [:], _) >> getEmptyQueryResult()
        when:
            def response = sampler.sample(entry)
        then:
            response.isSuccessful()
            response.isResponseCodeOK()
            def str = response.getResponseDataAsString()
            str.contains("Summary:")
            str.endsWith("Records: Skipped")
            response.getSampleCount() == 1
            response.getErrorCount() == 0
    }

    def getEmptyQueryResult() {
        def queryResult = Mock(Result)
        def summary = Mock(ResultSummary)
        queryResult.consume() >> summary
        SummaryCounters counters = Mock(SummaryCounters)
        summary.counters() >> counters
        return queryResult
    }

    def getPopulatedQueryResult() {
        def queryResult = Mock(Result)
        def summary = Mock(ResultSummary)
        def list = [Mock(Record), Mock(Record), Mock(Record)]
        queryResult.consume() >> summary
        queryResult.list() >> list
        SummaryCounters counters = Mock(SummaryCounters)
        summary.counters() >> counters
        return queryResult
    }
}
