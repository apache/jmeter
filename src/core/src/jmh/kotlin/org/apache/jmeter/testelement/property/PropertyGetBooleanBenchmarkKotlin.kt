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

package org.apache.jmeter.testelement.property

import org.apache.jmeter.testelement.TestElementSchema
import org.apache.jmeter.testelement.TestPlan
import org.apache.jmeter.testelement.schema.BooleanPropertyDescriptor
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.BenchmarkMode
import org.openjdk.jmh.annotations.Fork
import org.openjdk.jmh.annotations.Measurement
import org.openjdk.jmh.annotations.Mode
import org.openjdk.jmh.annotations.OutputTimeUnit
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.annotations.Warmup
import org.openjdk.jmh.profile.GCProfiler
import org.openjdk.jmh.runner.Runner
import org.openjdk.jmh.runner.options.Options
import org.openjdk.jmh.runner.options.OptionsBuilder
import java.util.concurrent.TimeUnit

@Fork(value = 1, jvmArgsPrepend = ["-Xmx129m"])
@Measurement(iterations = 500, time = 1, timeUnit = TimeUnit.SECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
open class PropertyGetBooleanBenchmarkKotlin {
    lateinit var testPlan: TestPlan
    lateinit var enabled: BooleanPropertyDescriptor<TestElementSchema>

    @Setup
    fun setup() {
        testPlan = TestPlan().apply {
            name = "test plan name"
            comment = "test plan comment"
            isSerialized = true
        }
        enabled = TestElementSchema.enabled
    }

    @Benchmark
    fun getBoolean(): Boolean =
        testPlan.isSerialized

    @Benchmark
    fun configure_and_getBoolean(): TestPlan {
        // Does not allocate heap
        testPlan.props {
            // Note: set.. would allocate BooleanProperty, so we test get here
            if (!it[enabled]) {
                throw IllegalStateException("enabled must be true")
            }
        }
        return testPlan
    }

    @Benchmark
    open fun props_klass_prop_getBoolean(): Boolean =
        testPlan.props.schema.enabled[testPlan]

    @Benchmark
    fun booleanDescriptor_getBoolean(): Boolean =
        enabled[testPlan]

    @Benchmark
    fun props_getBoolean(): Boolean =
        testPlan.props[enabled]

    @Benchmark
    fun props_selector_getBoolean(): Boolean =
        testPlan.props[ { enabled }]
}

fun main() {
    println(PropertyGetBooleanBenchmarkKotlin::class.java.simpleName)
    val opt: Options = OptionsBuilder()
        .include(PropertyGetBooleanBenchmarkKotlin::class.java.simpleName)
        .addProfiler(GCProfiler::class.java)
        .detectJvmArgs()
        .build()
    Runner(opt).run()
}
