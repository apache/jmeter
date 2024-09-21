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

package org.apache.jmeter.test.samplers

import org.apache.jmeter.engine.util.NoThreadClone
import org.apache.jmeter.reporters.AbstractListenerElement
import org.apache.jmeter.samplers.SampleEvent
import org.apache.jmeter.samplers.SampleListener
import java.util.Collections

/**
 * Collects [SampleEvent] to a list.
 */
class CollectSamplesListener : AbstractListenerElement(), SampleListener, NoThreadClone {
    private val mutableEvents: MutableList<SampleEvent> = Collections.synchronizedList(mutableListOf<SampleEvent>())

    // Copy the events to prevent late arrivals after the test has finished
    val events: List<SampleEvent> get() = mutableEvents.toList()

    override fun sampleOccurred(e: SampleEvent) {
        mutableEvents += e
    }

    override fun sampleStarted(e: SampleEvent) {
        mutableEvents += e
    }

    override fun sampleStopped(e: SampleEvent) {
        mutableEvents += e
    }
}
