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

package org.apache.jmeter.threads.openmodel;

import static org.apache.jmeter.treebuilder.dsl.TreeBuilders.testTree;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.apache.jmeter.control.TestTransactionController;
import org.apache.jmeter.engine.JMeterEngineException;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.modifiers.CounterConfig;
import org.apache.jmeter.sampler.DebugSampler;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.test.JMeterSerialTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class OpenModelThreadGroupConfigElementJavaTest extends JMeterTestCase implements JMeterSerialTest {
    /**
     * Create Test Plan with Open Model Thread Group and Counter Config.
     */
    @Test
    // Un-comment if you want try running the test multiple times locally:
    // @RepeatedTest(value = 10)
    void ensure_thread_group_initializes_counter_only_once_for_each_thread()
            throws JMeterEngineException, ExecutionException, InterruptedException, TimeoutException {
        TestTransactionController.TestSampleListener listener = new TestTransactionController.TestSampleListener();

        HashTree tree = testTree(b -> {
            b.add(TestPlan.class, tp -> {
                b.add(OpenModelThreadGroup.class, tg -> {
                    tg.setName("Thread Group");
                    // 5 samples within 100ms
                    // Then 2 sec pause to let all the threads to finish, especially the ones that start at 99ms
                    tg.setScheduleString("rate(50 / sec) random_arrivals(100 ms) pause(2 s)");
                    b.add(listener);
                    b.add(CounterConfig.class, c -> {
                        c.setVarName("counter");
                        c.setIncrement(1);
                    });
                    b.add(DebugSampler.class, dbg -> {
                        dbg.setName("${counter}");
                        dbg.setDisplayJMeterProperties(false);
                        dbg.setDisplayJMeterVariables(false);
                        dbg.setDisplaySystemProperties(false);
                    });
                });
            });
        });
        StandardJMeterEngine engine = new StandardJMeterEngine();
        engine.configure(tree);
        engine.runTest();
        engine.awaitTermination(Duration.ofSeconds(10));

        // There's no guarantee that threads execute exactly in order, so we sort
        // the labels to avoid test failure in case the thread execute out of order.
        List<String> actual = listener.events.stream()
                .map(e -> e.getResult().getSampleLabel())
                .sorted()
                .collect(Collectors.toList());

        // Use toString for better error message
        Assertions.assertEquals(
                "0\n1\n2\n3\n4",
                String.join("\n", actual),
                "Test should produce 5 iterations, so ${counter} should yield 0..4"
        );
    }
}
