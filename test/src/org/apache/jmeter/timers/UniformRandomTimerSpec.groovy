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

package org.apache.jmeter.timers

import spock.lang.Specification
import spock.lang.Unroll

class UniformRandomTimerSpec extends Specification {

    def sut = new UniformRandomTimer()

    def "default delay is 0"() {
        given:
            sut.iterationStart(null)
        when:
            def computedDelay = sut.delay()
        then:
            computedDelay == 0L
    }

    def "default range is 0"() {
        given:
            sut.setDelay("1")
            sut.iterationStart(null)
        when:
            def computedDelay = sut.delay()
        then:
            computedDelay == 1L
    }

    @Unroll
    def "#delay <= computedDelay <= trunc(#delay + abs(#range))"() {
        given:
            sut.setDelay(delay)
            sut.setRange(range)
            sut.iterationStart(null)
        when:
            def computedDelay = sut.delay()
        then:
            min <= computedDelay
            computedDelay <= max
        where:
            delay | range | min | max
            "1"   | 10.5  | 1   | 11
            "1"   | 0.1   | 1   | 1
            "0"   | -50.0 | 0   | 50
    }
}
