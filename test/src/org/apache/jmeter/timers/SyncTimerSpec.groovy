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
 *
 */

package org.apache.jmeter.timers

import org.apache.jmeter.threads.JMeterContext
import org.apache.jmeter.threads.JMeterContextService
import org.apache.jmeter.threads.JMeterThread

import spock.lang.Specification

class SyncTimerSpec extends Specification {

    def "timer with scheduled end point"() {
        setup:
            def timer = new SyncTimer()
            def context = Stub(JMeterContext)
            def thread = Stub(JMeterThread)

            thread.getEndTime() >> System.currentTimeMillis() + 100L
            context.getThread() >> thread

            JMeterContextService.replaceContext(context)
            timer.groupSize = 2
            timer.testStarted()
         when:
            def start = System.currentTimeMillis();
            def delay = timer.delay()
            def elapsed = System.currentTimeMillis() - start
         then:
            delay == 0
            elapsed < 10 * 100L
    }

    def "timer with scheduled end point and shorter max timeout in ms"() {
        setup:
            def timer = new SyncTimer()
            timer.setTimeoutInMs(100L)
            def context = Stub(JMeterContext)
            def thread = Stub(JMeterThread)

            thread.getEndTime() >> System.currentTimeMillis() + 2000L
            context.getThread() >> thread

            JMeterContextService.replaceContext(context)
            timer.groupSize = 2
            timer.testStarted()
         when:
            def start = System.currentTimeMillis();
            def delay = timer.delay()
            def elapsed = System.currentTimeMillis() - start
         then:
            delay == 0
            elapsed < 10 * 100L
    }

    def "timer with scheduled end point and longer max timeout in ms"() {
        setup:
            def timer = new SyncTimer()
            timer.setTimeoutInMs(2000L)
            def context = Stub(JMeterContext)
            def thread = Stub(JMeterThread)

            thread.getEndTime() >> System.currentTimeMillis() + 100L
            context.getThread() >> thread

            JMeterContextService.replaceContext(context)
            timer.groupSize = 2
            timer.testStarted()
         when:
            def start = System.currentTimeMillis();
            def delay = timer.delay()
            def elapsed = System.currentTimeMillis() - start
         then:
            delay == 0
            elapsed < 10 * 100L
    }

}