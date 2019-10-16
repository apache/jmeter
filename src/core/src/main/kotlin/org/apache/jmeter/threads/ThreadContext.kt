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

import java.util.concurrent.ExecutorService
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.RejectedExecutionHandler
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.thread
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.Continuation
import kotlin.coroutines.ContinuationInterceptor
import kotlin.coroutines.CoroutineContext

fun newFixedThreadPoolContext(nThreads: Int, name: String) =
    // Each thread can multi-task.
    // We don't want to use more than 200 threads
    ThreadContext(nThreads.coerceAtMost(200), name)

class ThreadContext(
    nThreads: Int,
    name: String
) : AbstractCoroutineContextElement(ContinuationInterceptor), ContinuationInterceptor {
    private val threadNo = AtomicInteger()

    val executor: ExecutorService =
        ThreadPoolExecutor(nThreads, nThreads, 60, TimeUnit.SECONDS, LinkedBlockingQueue()) { target ->
            thread(start = false, isDaemon = true, name = name + "-" + threadNo.incrementAndGet()) {
                target.run()
            }
        }.apply {
            // Coroutine cancelation is cooperative, so we still need to give it a chance to run
            // Note: all the coroutines are in "cancelling" state before thread pool is shutdown,
            // So the runnable would ensure the coroutine terminates appropriately
            rejectedExecutionHandler = RejectedExecutionHandler { runnable, _ -> runnable.run() }
            allowCoreThreadTimeOut(true)
        }

    override fun <T> interceptContinuation(continuation: Continuation<T>): Continuation<T> =
        ThreadContinuation(continuation)

    private inner class ThreadContinuation<T>(val cont: Continuation<T>) : Continuation<T> {
        override val context: CoroutineContext = cont.context

        override fun resumeWith(result: Result<T>) {
            executor.execute {
                val jmeterContext = context[JMeterContextHolder]!!.jmeterContext
                JMeterContextService.replaceContext(jmeterContext)
                cont.resumeWith(result)
            }
        }
    }
}
