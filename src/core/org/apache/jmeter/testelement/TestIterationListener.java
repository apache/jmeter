/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.apache.jmeter.testelement;

import org.apache.jmeter.engine.event.LoopIterationEvent;

public interface TestIterationListener {

    /**
     * Each time through a Thread Group's test script, an iteration event is
     * fired for each thread.
     *
     * This will be after the test elements have been cloned, so in general
     * the instance will not be the same as the ones the start/end methods call.
     *
     * @param event the iteration event
     */
    void testIterationStart(LoopIterationEvent event);
}
