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
import org.apache.jmeter.testelement.TestElementSchema
import org.apache.jorphan.collections.ListedHashTree
import org.apiguardian.api.API
import kotlin.reflect.KClass

/**
 * Provides a programmatic way to build test plans.
 *
 * Sample Kotlin:
 *
 *     testTree {
 *         TestPlan::class {
 *             OpenModelThreadGroup::class {
 *                 name = "Thread Group"
 *                 scheduleString = "rate(50 / sec) random_arrivals(100 ms) pause(2 s)"
 *                 +DebugSampler()
 *             }
 *         }
 *     }
 *
 * Sample Java:
 *
 *     testTree(b -> {
 *         b.add(TestPlan.class, tp -> {
 *             b.add(OpenModelThreadGroup.class, tg ->{
 *                 name = "Thread Group"
 *                 scheduleString = "rate(50 / sec) random_arrivals(100 ms) pause(2 s)"
 *                 b.add(new DebugSampler());
 *             });
 *         });
 *     });
 *
 * @since 5.6
 */
@API(status = API.Status.EXPERIMENTAL, since = "5.6")
public class TreeBuilder {
    /**
     * Resulting tree.
     */
    public val tree: ListedHashTree = ListedHashTree()

    private var currentSubtreePrivate: ListedHashTree = tree

    /**
     * Subtree for the currently built element.
     */
    public val currentSubtree: ListedHashTree get() = currentSubtreePrivate

    private val elementStackPrivate = mutableListOf<TestElement>()

    /**
     * Path to the current element from the root.
     * The first element is the root, and the last element is the recently added one.
     */
    public val elementStack: List<TestElement> get() = elementStackPrivate

    public val currentElement: TestElement get() = elementStack.last()

    public val parent: TestElement? get() = elementStack.getOrNull(elementStack.lastIndex - 1)

    /**
     * Contains the stack of configuration actions. The action will be discarded after the builder scope finishes,
     * and every scope can contain a list of actions which is null when no actions exist in the scope.
     */
    private val actionsStack = mutableListOf<MutableList<Action<TestElement>>?>(null)

    /**
     * Adds a test element to the test tree.
     */
    @JvmName("add")
    public operator fun TestElement.unaryPlus() {
        currentSubtreePrivate.add(this)
        runConfigurations(this)
    }

    /**
     * Adds a test element to the test tree.
     */
    @JvmName("add")
    public operator fun <T : TestElement> KClass<T>.unaryPlus() {
        +this.java
    }

    /**
     * Adds a test element to the test tree.
     */
    @JvmName("add")
    public operator fun <T : TestElement> Class<T>.unaryPlus() {
        +getDeclaredConstructor().newInstance()
    }

    /**
     * Creates a test element and adds it to the test tree.
     * The test element class should have a no-argument constructor.
     * @param configure block that configures the newly created test element, and optionally adds children elements
     */
    @JvmName("add")
    public operator fun <T : TestElement> KClass<T>.invoke(configure: Action<T> = Action {}) {
        java(configure)
    }

    /**
     * Creates a test element and adds it to the test tree.
     * The test element class should have a no-argument constructor.
     * @param configure block that configures the newly created test element, and optionally adds children elements
     */
    @JvmName("add")
    public operator fun <T : TestElement> Class<T>.invoke(configure: Action<T> = Action {}) {
        getDeclaredConstructor().newInstance()(configure)
    }

    /**
     * Adds a test element to the test tree.
     * @param configure block that configures the test element, and optionally adds children elements
     */
    @JvmName("add")
    public operator fun <T : TestElement> T.invoke(configure: Action<T> = Action {}) {
        val prevTree = currentSubtreePrivate
        try {
            currentSubtreePrivate = currentSubtreePrivate.add(this)
            elementStackPrivate.add(this)
            actionsStack.add(null)
            configure.run { execute() }
            // Execute configurations after user-provided "constructor" actions were run
            runConfigurations(this)
        } finally {
            currentSubtreePrivate = prevTree
            elementStackPrivate.removeLast()
            actionsStack.removeLast()
        }
    }

    private fun runConfigurations(testElement: TestElement) {
        if (testElement.getPropertyOrNull(TestElementSchema.testClass) == null) {
            testElement[TestElementSchema.testClass] = testElement::class.java
        }
        for (actions in actionsStack) {
            actions?.forEach { it(testElement) }
        }
    }

    /**
     * Add an [Action] that will be executed after each element is added to the tree.
     */
    public fun configureAll(configure: Action<TestElement>) {
        configureEach<TestElement>(configure)
    }

    /**
     * Add an [Action] that will be executed after elements of given type (or its subtype) are added to the tree.
     */
    public inline fun <reified T : TestElement> configureEach(configure: Action<T>) {
        configureEach(T::class, configure)
    }

    /**
     * Add an [Action] that will be executed after elements of given type (or its subtype) are added to the tree.
     */
    public fun <T : TestElement> configureEach(klass: KClass<T>, configure: Action<T>) {
        configureEach(klass.java, configure)
    }

    /**
     * Add an [Action] that will be executed after elements of given type (or its subtype) are added to the tree.
     */
    public fun <T : TestElement> configureEach(klass: Class<T>, configure: Action<T>) {
        // TODO: should we configure already created elements in the scope?
        var actions = actionsStack.last()
        if (actions == null) {
            actions = mutableListOf()
            actionsStack[actionsStack.lastIndex] = actions
        }
        actions += Action {
            if (klass.isInstance(this)) {
                configure(klass.cast(this))
            }
        }
    }
}
