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

package org.apache.jmeter.test.assertions

import org.apache.jmeter.engine.StandardJMeterEngine
import org.apache.jmeter.samplers.SampleEvent
import org.apache.jmeter.test.samplers.CollectSamplesListener
import org.apache.jmeter.testelement.TestPlan
import org.apache.jmeter.treebuilder.TreeBuilder
import org.apache.jmeter.treebuilder.dsl.testTree
import kotlin.time.Duration
import kotlin.time.toJavaDuration

fun executePlanAndCollectEvents(duration: Duration, testBody: TreeBuilder.() -> Unit): List<SampleEvent> {
    val listener = CollectSamplesListener()

    val tree = testTree {
        TestPlan::class {
            +listener
            testBody()
        }
    }
    StandardJMeterEngine().apply {
        configure(tree)
        runTest()
        awaitTermination(duration.toJavaDuration())
    }

    return listener.events
}
