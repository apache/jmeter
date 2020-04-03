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

package org.apache.jmeter.threads

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.time.Duration

class MultitaskThreadGroup(
    val name: String,
    val numThreads: Int,
    val rampUpDuration: Duration,
    val startDelay: Duration,
    val endTime: Long,
    val threadFactory: (Int) -> JMeterThread
) {
    private val jmeterTheadContext = newFixedThreadPoolContext(numThreads, name)

    private val scope = CoroutineScope(SupervisorJob())

    private lateinit var allJobs: Job

    fun start() {
        val start = System.currentTimeMillis()
        val rampUpMillis = rampUpDuration.toMillis()
        allJobs = scope.launch(CoroutineName("$name root")) {
            delay(startDelay.toMillis())

            for (i in 1..numThreads) {
                if (rampUpMillis != 0L) {
                    val expectedStart = start + rampUpMillis * i / numThreads
                    delay(expectedStart - System.currentTimeMillis())
                }
                launch(jmeterTheadContext + JMeterContextHolder() + CoroutineName("$name $i")) {
                    threadFactory(i).also {
                        if (endTime != 0L) {
                            it.endTime = endTime
                            it.setScheduled(true)
                        }
                        it.runSuspend()
                    }
                }
            }
        }
    }

    fun waitThreadsStopped() {
        runBlocking {
            allJobs.join()
        }
    }

    fun shutdown() {
        runBlocking {
            allJobs.cancel()
        }
        jmeterTheadContext.executor
            .shutdownNow()
            .forEach { neverStarted ->
                // Coroutine cancellation is cooperative
                // So we need to give a chance for the ones that never started
                // They are already cancelled, so "run" should not take long
                neverStarted.run()
            }
    }
}
