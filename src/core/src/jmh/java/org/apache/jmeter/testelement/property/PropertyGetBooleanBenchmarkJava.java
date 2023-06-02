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

package org.apache.jmeter.testelement.property;

import java.util.concurrent.TimeUnit;

import org.apache.jmeter.testelement.TestElementSchema;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.testelement.schema.BooleanPropertyDescriptor;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.profile.GCProfiler;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

@Fork(value = 1, jvmArgsPrepend = {"-Xmx128m"})
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class PropertyGetBooleanBenchmarkJava {
    TestPlan testPlan;
    BooleanPropertyDescriptor<TestElementSchema> enabled;

    @Setup
    public void setup() {
        testPlan = new TestPlan();
        testPlan.setName("test plan name");
        testPlan.setComment("test plan comment");
        testPlan.setSerialized(true);
        enabled = TestElementSchema.INSTANCE.getEnabled();
    }

    @Benchmark
    public TestPlan configure_and_getBoolean() {
        // Does not allocate heap
        testPlan.getProps().invoke(
                (plan, klass) -> {
                    // Note: set.. would allocate BooleanProperty, so we test get here
                    if (!plan.get(klass.getEnabled())) {
                        throw new IllegalStateException("enabled must be true");
                    }
                }
        );
        return testPlan;
    }

    @Benchmark
    public boolean props_klass_prop_getBoolean() {
        return testPlan.getProps().getSchema().getEnabled().get(testPlan);
    }

    @Benchmark
    public boolean booleanDescriptor_getBoolean() {
        return enabled.get(testPlan);
    }

    @Benchmark
    public boolean props_getBoolean() {
        return testPlan.getProps().get(enabled);
    }

    @Benchmark
    public boolean props_selector_getBoolean() {
        return testPlan.getProps().get(TestElementSchema::getEnabled);
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(PropertyGetBooleanBenchmarkJava.class.getSimpleName())
                .addProfiler(GCProfiler.class)
                .detectJvmArgs()
                .build();
        new Runner(opt).run();
    }
}
