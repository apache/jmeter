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

package org.apache.jmeter.threads.openmodel

import org.apache.jmeter.control.GenericController
import org.apache.jmeter.control.IteratingController
import org.apache.jmeter.engine.event.LoopIterationEvent
import org.apache.jmeter.samplers.Sampler
import org.apiguardian.api.API

@API(status = API.Status.EXPERIMENTAL, since = "5.5")
public class OpenModelThreadGroupController : GenericController(), IteratingController {
    private companion object {
        private const val serialVersionUID: Long = 1L
    }

    override fun next(): Sampler? =
        super.next().also {
            if (iterCount >= 1) {
                // For now, every thread performs just one iteration
                isDone = true
            }
        }

    override fun iterationStart(iterEvent: LoopIterationEvent?) {
    }

    override fun startNextLoop() {
    }

    override fun breakLoop() {
    }
}
