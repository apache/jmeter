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

package org.apache.jmeter.save

import org.apache.jmeter.testelement.TestElement
import org.apache.jmeter.util.JMeterUtils
import org.apache.jorphan.collections.HashTree
import org.apache.jorphan.reflect.LogAndIgnoreServiceLoadExceptionHandler
import org.apiguardian.api.API
import org.slf4j.LoggerFactory
import java.util.ServiceLoader

/**
 * Discovers [TestElementUpgrader] services and applies them to a loaded test plan.
 *
 * Each element is upgraded to a fix point: every upgrader is applied to the element repeatedly
 * until a pass changes nothing. Because each upgrader is idempotent, forward-only, and touches only
 * the element it is given, a chain of upgrades on the same property converges without any ordering
 * metadata, and elements can be upgraded independently of each other.
 *
 * @since 6.0.0
 */
@API(status = API.Status.EXPERIMENTAL, since = "6.0.0")
public object TestElementUpgraders {
    private val log = LoggerFactory.getLogger(TestElementUpgraders::class.java)

    private val upgraders: List<TestElementUpgrader> = load()

    private fun load(): List<TestElementUpgrader> =
        try {
            JMeterUtils.loadServicesAndScanJars(
                TestElementUpgrader::class.java,
                ServiceLoader.load(TestElementUpgrader::class.java),
                Thread.currentThread().contextClassLoader,
                LogAndIgnoreServiceLoadExceptionHandler(log)
            ).toList()
        } catch (e: Exception) {
            log.error("Unable to load TestElementUpgrader services", e)
            emptyList()
        }

    /**
     * Applies every registered upgrader to every [TestElement] in the tree.
     *
     * @param tree the loaded test plan to upgrade in place
     */
    @JvmStatic
    public fun upgrade(tree: HashTree) {
        if (upgraders.isEmpty()) {
            return
        }
        // Walk the tree iteratively (no recursion) and upgrade each element as it is visited. Property
        // upgrades change only an element's own properties, not the tree structure, and HashTree keys
        // by identity (IdentityHashMap), so editing an element in place during the walk is safe and
        // needs no separate "collect to a list" pass. A future structural phase would run before this
        // one, so the tree shape is already stable here.
        val pending = ArrayDeque<HashTree>()
        pending.addLast(tree)
        while (pending.isNotEmpty()) {
            val subTree = pending.removeLast()
            for (node in subTree.list()) {
                if (node is TestElement) {
                    upgrade(node)
                }
                pending.addLast(subTree.getTree(node))
            }
        }
    }

    private fun upgrade(element: TestElement) {
        // A forward-only chain on one property settles within `upgraders.size` passes; the extra pass
        // confirms convergence, and the bound stops a misbehaving (cyclic) upgrader from looping forever.
        val maxPasses = upgraders.size + 1
        var pass = 0
        var changed = true
        while (changed && pass < maxPasses) {
            changed = false
            for (upgrader in upgraders) {
                changed = applyUpgrader(upgrader, element) || changed
            }
            pass++
        }
        if (changed) {
            log.warn(
                "TestElement upgraders did not converge for {} after {} passes; some legacy properties may remain",
                describe(element), maxPasses
            )
        }
    }

    private fun applyUpgrader(upgrader: TestElementUpgrader, element: TestElement): Boolean =
        try {
            val changed = upgrader.upgrade(element)
            if (changed && log.isDebugEnabled) {
                log.debug("{} upgraded {}", upgrader.javaClass.name, describe(element))
            }
            changed
        } catch (e: Exception) { // NOSONAR one bad upgrader must not make the whole plan unloadable
            // Surfaced at ERROR so it is visible without enabling debug logging: name the element the
            // user can recognise, say what happened, and that the element was left as-is.
            log.error(
                "Failed to upgrade {} from an older JMeter format; leaving it unchanged. Failing upgrader: {}",
                describe(element), upgrader.javaClass.name, e
            )
            false
        }

    private fun describe(element: TestElement): String =
        "element '${element.name}' (${element.javaClass.name})"
}
