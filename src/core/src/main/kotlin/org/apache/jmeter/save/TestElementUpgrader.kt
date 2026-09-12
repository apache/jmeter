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
import org.apache.jorphan.reflect.JMeterService
import org.apiguardian.api.API

/**
 * Upgrades a [TestElement] loaded from an older test-plan format, transforming legacy properties
 * into their current representation.
 *
 * Unlike [org.apache.jmeter.util.NameUpdater], which only renames property keys and element
 * classes, an upgrader can transform property *values* — for example, a boolean flag into an enum
 * resource key.
 *
 * Implementations are discovered with [java.util.ServiceLoader], so register them with
 * `@AutoService(TestElementUpgrader::class)`.
 *
 * Implementations must be **idempotent** and **forward-only**:
 * - running [upgrade] on an already-current element returns `false` and changes nothing;
 * - an upgrade never re-creates the legacy shape it just removed.
 *
 * These rules let [TestElementUpgraders] run every upgrader to a fix point, so chained upgrades
 * (one property migrated, then migrated again later) converge regardless of registration order,
 * without numbering the migrations.
 *
 * @since 6.0.0
 */
@JMeterService
@API(status = API.Status.EXPERIMENTAL, since = "6.0.0")
public fun interface TestElementUpgrader {
    /**
     * Upgrades a single element in place.
     *
     * @param element the element to inspect and, if it carries a legacy shape, upgrade
     * @return `true` if the element was modified, `false` otherwise
     */
    public fun upgrade(element: TestElement): Boolean
}
