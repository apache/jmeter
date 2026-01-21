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

package org.apache.jmeter.engine.util;

/**
 * Marker interface for elements that can share properties across threads.
 * Elements implementing this interface will have their properties shared
 * (not deep cloned) when the test tree is cloned for each thread.
 *
 * <p>The element must:
 * <ul>
 *   <li>Never modify JMeterProperty values during test execution</li>
 *   <li>Store all mutable runtime state in transient fields</li>
 * </ul>
 *
 * <p>This optimization reduces memory usage for multi-threaded tests by
 * avoiding redundant property cloning. Elements that need per-thread
 * property state should not implement this interface.
 *
 * <p>This can be disabled globally by setting the property
 * {@code jmeter.clone.lightweight.enabled=false} in jmeter.properties.
 *
 * @see NoThreadClone
 */
public interface LightweightClone {
}
