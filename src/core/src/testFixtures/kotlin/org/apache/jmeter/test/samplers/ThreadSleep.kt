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

import org.apache.jmeter.samplers.AbstractSampler
import org.apache.jmeter.samplers.Entry
import org.apache.jmeter.samplers.SampleResult
import org.apache.jmeter.testelement.TestElementSchema
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

/**
 * Sampler that uses explicit [Thread.sleep] in order to verify if thread groups would interrupt the sleep.
 * Default JMeter's timers might use a different strategy. They might compute the time till
 * the end of the test, so we can't use timers to reliably verify interrupt behovoir.
 */
class ThreadSleep : AbstractSampler() {
    abstract class Schema : TestElementSchema() {
        companion object INSTANCE : Schema()

        val duration by long("ThreadSleep.duration")
    }

    var duration: Duration
        get() = durationMillis.toDuration(DurationUnit.MILLISECONDS)
        set(value) {
            durationMillis = value.inWholeMilliseconds
        }

    var durationMillis by Schema.duration
    var durationString by Schema.duration.asString

    override fun sample(e: Entry?): SampleResult {
        val res = SampleResult()
        res.sampleStart()
        try {
            Thread.sleep(durationMillis)
            res.responseCode = "Sleep $durationMillis succeeded"
        } catch (t: InterruptedException) {
            res.responseCode = "InterruptedException"
        } finally {
            res.sampleEnd()
        }
        return res
    }
}
