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

 package org.apache.jmeter.extractor

import org.apache.jmeter.samplers.SampleResult
import org.apache.jmeter.threads.JMeterContext
import org.apache.jmeter.threads.JMeterContextService
import org.apache.jmeter.threads.JMeterVariables
import spock.lang.Specification
import spock.lang.Unroll

import java.util.stream.Stream

@Unroll
class BoundaryExtractorSpec extends Specification {

    static LEFT = "LB"
    static RIGHT = "RB"
    static DEFAULT_VAL = "defaultVal"
    static VAR_NAME = "varName"

    def sut = new BoundaryExtractor()

    SampleResult prevResult
    JMeterVariables vars
    JMeterContext context

    def setup() {
        vars = new JMeterVariables()
        context = JMeterContextService.getContext()
        context.setVariables(vars)

        sut.setThreadContext(context)
        sut.setRefName(VAR_NAME)
        sut.setLeftBoundary(LEFT)
        sut.setRightBoundary(RIGHT)
        sut.setDefaultValue(DEFAULT_VAL)
        sut.setMatchNumber(1)

        prevResult = new SampleResult()
        prevResult.sampleStart()
        prevResult.setResponseData(createInputString(1..2), null)
        prevResult.sampleEnd()
        context.setPreviousResult(prevResult)
    }

    def "Extract, where pattern exists, with matchNumber=#matchNumber from #occurences returns #expected"() {
        given:
            def input = createInputString(occurrences)
        when:
            def matches = sut.extract(LEFT, RIGHT, matchNumber, input)
        then:
            matches == expected
        where:
            occurrences | matchNumber || expected
            1..1        | -1          || ['1']
            1..1        | 0           || ['1']
            1..1        | 1           || ['1']
            1..1        | 2           || []
            1..2        | -1          || ['1', '2']
            1..2        | 1           || ['1']
            1..2        | 2           || ['2']
            1..3        | 3           || ['3']
    }

    def "Extract, where pattern does not exist, with matchNumber=#matchNumber returns an empty list"() {
        expect:
            sut.extract(LEFT, RIGHT, matchNumber, 'start end') == []
        where:
            matchNumber << [-1, 0, 1, 2, 100]
    }

    def "Extract, where pattern exists in the stream, with matchNumber=#matchNumber from #occurances returns #expected"() {
        given:
            def input = createInputString(occurrences)
            Stream<String> stream = ([input, "", null] * 10).stream()
        when:
            def matches = sut.extract(LEFT, RIGHT, matchNumber, stream)
        then:
            matches == expected
        where:
            occurrences | matchNumber || expected
            1..1        | -1          || ['1'] * 10
            1..1        | 0           || ['1'] * 10
            1..1        | 1           || ['1']
            1..1        | 10          || ['1']
            1..1        | 11          || []
            1..2        | -1          || ['1', '2'] * 10
            1..2        | 1           || ['1']
            1..2        | 2           || ['2']
            1..3        | 3           || ['3']
    }

    def "Extract, where pattern does not exist in the stream, with matchNumber=#matchNumber returns an empty list"() {
        given:
            Stream<String> stream = (['start end'] * 10).stream()
        expect:
            sut.extract(LEFT, RIGHT, matchNumber, stream) == []
        where:
            matchNumber << [-1, 0, 1, 2, 100]
    }

    def "IllegalArgumentException when one of left (#lb), right (#rb), name (#name) are null"() {
        given:
            sut.setLeftBoundary(lb)
            sut.setRightBoundary(rb)
            sut.setRefName(name)
        when:
            sut.process()
        then:
            thrown(IllegalArgumentException)
        where:
            lb   | rb   | name
            null | "r"  | "name"
            "l"  | null | "name"
            "l"  | "r"  | null
    }

    def "matching only on left boundary returns default"() {
        given:
            sut.setRightBoundary("does-not-exist")
        when:
            sut.process()
        then:
            vars.get(VAR_NAME) == DEFAULT_VAL
    }

    def "matching only on right boundary returns default"() {
        given:
            sut.setLeftBoundary("does-not-exist")
        when:
            sut.process()
        then:
            vars.get(VAR_NAME) == DEFAULT_VAL
    }

    def "variables from a previous extraction are removed"() {
        given:
            sut.setMatchNumber(-1)
            sut.process()
            assert vars.get("${VAR_NAME}_1") == "1"
            assert vars.get("${VAR_NAME}_matchNr") == "2"
        when:
            // Now rerun with match fail
            sut.setMatchNumber(10)
            sut.process()
        then:
            vars.get(VAR_NAME) == DEFAULT_VAL
            vars.get("${VAR_NAME}_1") == null
            vars.get("${VAR_NAME}_matchNr") == null
    }

    def "with no sub-samples parent and all scope return data but children scope does not"() {
        given:
            sut.setScopeParent()
        when:
            sut.process()
        then:
            vars.get(VAR_NAME) == "1"

        and:
            sut.setScopeAll()
        when:
            sut.process()
        then:
            vars.get(VAR_NAME) == "1"

        and:
            sut.setScopeChildren()
        when:
            sut.process()
        then:
            vars.get(VAR_NAME) == DEFAULT_VAL
    }

    def "with sub-samples parent, all and children scope return expected item"() {
        given:
            prevResult.addSubResult(createSampleResult("${LEFT}sub1${RIGHT}"))
            prevResult.addSubResult(createSampleResult("${LEFT}sub2${RIGHT}"))
            prevResult.addSubResult(createSampleResult("${LEFT}sub3${RIGHT}"))
            sut.setScopeParent()
        when:
            sut.process()
        then:
            vars.get(VAR_NAME) == "1"

        and:
            sut.setScopeAll()
            sut.setMatchNumber(3) // skip 2 in parent sample
        when:
            sut.process()
        then:
            vars.get(VAR_NAME) == "sub1"

        and:
            sut.setScopeChildren()
            sut.setMatchNumber(3)
        when:
            sut.process()
        then:
            vars.get(VAR_NAME) == "sub3"
    }

    def "with sub-samples parent, all and children scope return expected data"() {
        given:
            prevResult.addSubResult(createSampleResult("${LEFT}sub1${RIGHT}"))
            prevResult.addSubResult(createSampleResult("${LEFT}sub2${RIGHT}"))
            prevResult.addSubResult(createSampleResult("${LEFT}sub3${RIGHT}"))
            sut.setMatchNumber(-1)

            sut.setScopeParent()
        when:
            sut.process()
        then:
            vars.get("${VAR_NAME}_matchNr") == "2"
            getAllVars() == ["1", "2"]

        and:
            sut.setScopeAll()
        when:
            sut.process()
        then:
            vars.get("${VAR_NAME}_matchNr") == "5"
            getAllVars() == ["1", "2", "sub1", "sub2", "sub3"]

        and:
            sut.setScopeChildren()
        when:
            sut.process()
        then:
            vars.get("${VAR_NAME}_matchNr") == "3"
            getAllVars() == ["sub1", "sub2", "sub3"]
    }

    def "when 'default empty value' is true the default value is allowed to be empty"() {
        given:
            sut.setMatchNumber(10) // no matches
            sut.setDefaultValue("")
            sut.setDefaultEmptyValue(true)
        when:
            sut.process()
        then:
            vars.get(VAR_NAME) == ""
    }

    def "when default value is empty but not allowed null is returned"() {
        given:
            sut.setMatchNumber(10) // no matches
            sut.setDefaultValue("")
            sut.setDefaultEmptyValue(false)
        when:
            sut.process()
        then:
            vars.get(VAR_NAME) == null
    }

    def "with no previous results result is null"() {
        given:
            context.setPreviousResult(null)
            sut.setDefaultEmptyValue(true)
        when:
            sut.process()
        then:
            vars.get(VAR_NAME) == null
    }

    def "with non-existent variable result is null"() {
        given:
            sut.setDefaultValue(null)
            sut.setScopeVariable("empty-var-name")
        when:
            sut.process()
        then:
            vars.get(VAR_NAME) == null
    }

    def "not allowing blank default value returns null upon no matches"() {
        given:
            sut.setMatchNumber(10) // no matches
            sut.setDefaultValue("")
            sut.setDefaultEmptyValue(false)
        when:
            sut.process()
        then:
            vars.get(VAR_NAME) == null
    }

    def "extract all matches from variable input"() {
        given:
            sut.setMatchNumber(-1)
            sut.setScopeVariable("contentvar")
            vars.put("contentvar", createInputString(1..2))
        when:
            sut.process()
        then:
            getAllVars() == ["1", "2"]
            vars.get("${VAR_NAME}_matchNr") == "2"
    }

    def "extract random from variable returns one of the matches"() {
        given:
            sut.setMatchNumber(0)
            sut.setScopeVariable("contentvar")
            vars.put("contentvar", createInputString(1..42))
        when:
            sut.process()
        then:
            (1..42).collect({ it.toString() }).contains(vars.get(VAR_NAME))
            vars.get("${VAR_NAME}_matchNr") == null
    }

    def "extract all from an empty variable returns no results"() {
        given:
            sut.setMatchNumber(-1)
            sut.setScopeVariable("contentvar")
            vars.put("contentvar", "")
        when:
            sut.process()
        then:
            vars.get("${VAR_NAME}_matchNr") == "0"
            vars.get("${VAR_NAME}_1") == null
    }

    def "previous extractions are cleared"() {
        given:
            sut.setMatchNumber(-1)
            sut.setScopeVariable("contentvar")
            vars.put("contentvar", createInputString(1..10))
            sut.process()
            assert getAllVars() == (1..10).collect({ it.toString() })
            assert vars.get("${VAR_NAME}_matchNr") == "10"
            vars.put("contentvar", createInputString(11..15))
            sut.setMatchNumber("-1")
            def expectedMatches = (11..15).collect({ it.toString() })
        when:
            sut.process()
        then:
            getAllVars() == expectedMatches
            vars.get("${VAR_NAME}_matchNr") == "5"
            (6..10).collect { vars.get("${VAR_NAME}_${it}") } == [null] * 5

        and:
            sut.setMatchNumber(0)
        when:
            sut.process()
        then:
            expectedMatches.contains(vars.get(VAR_NAME))
    }

    /**
     * Creates a string with a "match" for each number in the list.
     *
     * @param occurrences list of numbers to be the "body" of a match
     * @return a string with a start, end and then a left boundary + number + right boundary
     * e.g. "... LB1RB LB2RB ..."
     */
    static createInputString(List<Integer> occurrences) {
        def middle = occurrences.collect({ LEFT + it + RIGHT }).join(" ")
        return 'start \t\r\n' + middle + '\n\t end'
    }

    static createSampleResult(String responseData) {
        SampleResult child = new SampleResult()
        child.sampleStart()
        child.setResponseData(responseData, "ISO-8859-1")
        child.sampleEnd()
        return child
    }

    /**
     * @return a list of all the variables in the format ${VAR_NAME}_${i}
     * starting at i = 1 until null is returned
     */
    def getAllVars() {
        def allVars = []
        def i = 1
        def var = vars.get("${VAR_NAME}_${i}")
        while (var != null) {
            allVars.add(var)
            i++
            var = vars.get("${VAR_NAME}_${i}")
        }
        return allVars
    }

}
