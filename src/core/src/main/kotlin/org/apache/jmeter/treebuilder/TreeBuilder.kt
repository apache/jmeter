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

package org.apache.jmeter.treebuilder

import org.apache.jmeter.testelement.TestElement
import org.apache.jorphan.collections.HashTree
import org.apache.jorphan.collections.ListedHashTree
import kotlin.reflect.KClass

/**
 * Provides a programmatic way to build test plans.
 *
 * Sample:
 *
 *     treeBuilder {
 *         TestPlan::class {
 *             OpenModelThreadGroup::class {
 *                 name = "Thread Group"
 *                 scheduleString = "rate(50 / sec) random_arrivals(100 ms) pause(2 s)"
 *             }
 *         }
 *     }
 */
public class TreeBuilder {
    internal val tree = ListedHashTree()
    private var currentSubtree: HashTree = tree

    public operator fun <T : TestElement> KClass<T>.invoke(configure: T.() -> Unit = {}) {
        java.getDeclaredConstructor().newInstance()(configure)
    }

    public operator fun TestElement.unaryPlus(): HashTree {
        return currentSubtree.add(this)
    }

    public operator fun  <T : TestElement> T.invoke(configure: T.() -> Unit = {}) {
        val prevTree = currentSubtree
        try {
            currentSubtree = +this
            configure()
        } finally {
            currentSubtree = prevTree
        }
    }
}
