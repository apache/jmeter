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

package org.apache.jmeter.functions;

import java.util.concurrent.CountDownLatch

import org.apache.jmeter.engine.util.CompoundVariable
import org.apache.jmeter.threads.JMeterContextService
import org.apache.jmeter.threads.JMeterVariables

import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class IterationCounterSpec extends Specification {

    def "Counter per thread counts for each thread"() {
        given:
            def context = JMeterContextService.getContext()
            context.setVariables(new JMeterVariables())
            def counter = new IterationCounter()
            counter.setParameters(Arrays.asList(new CompoundVariable("true"), new CompoundVariable("var")))
        when:
            Thread.start({ (1..10).each { counter.execute(null, null) } }).join()
            (1..10).each { counter.execute(null, null) }
        then:
            context.getVariables().get("var") == "10"
    }

    def "global Counter counts for all threads"() {
        given:
            def context = JMeterContextService.getContext()
            context.setVariables(new JMeterVariables())
            def counter = new IterationCounter()
            counter.setParameters(Arrays.asList(new CompoundVariable("false"), new CompoundVariable("var")))
            def nrOfThreads = 100
            def latch = new CountDownLatch(nrOfThreads)
        when:
            (1..nrOfThreads).each {
                Thread.start({
                    (1..1000).each { counter.execute(null, null) }
                    latch.countDown()
                })
            }
            latch.await()
            counter.execute(null, null)
        then:
            context.getVariables().get("var") == "100001"
    }
}
