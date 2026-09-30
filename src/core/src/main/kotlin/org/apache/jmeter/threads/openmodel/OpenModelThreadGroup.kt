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

import org.apache.jmeter.control.Controller
import org.apache.jmeter.engine.StandardJMeterEngine
import org.apache.jmeter.gui.GUIMenuSortOrder
import org.apache.jmeter.testelement.schema.PropertiesAccessor
import org.apache.jmeter.threads.AbstractThreadGroup
import org.apache.jmeter.threads.JMeterContextService
import org.apache.jmeter.threads.JMeterThread
import org.apache.jmeter.threads.JMeterThreadMonitor
import org.apache.jmeter.threads.ListenerNotifier
import org.apache.jmeter.threads.TestCompilerHelper
import org.apache.jmeter.util.JMeterUtils
import org.apache.jorphan.collections.ListedHashTree
import org.apiguardian.api.API
import org.slf4j.LoggerFactory
import java.io.Serializable
import java.lang.Thread.sleep
import java.util.Random
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.roundToLong

/**
 * The thread group that emulates open model.
 * Threads are created on demand, and every thread exits after completion.
 * The number of concurrent threads is unlimited by default, however, it can be capped with a
 * `concurrency(...)` step in the schedule or with the `openmodel.max_concurrency` property.
 */
@GUIMenuSortOrder(1)
@API(status = API.Status.EXPERIMENTAL, since = "5.5")
public class OpenModelThreadGroup :
    AbstractThreadGroup(),
    Serializable,
    Controller,
    JMeterThreadMonitor,
    TestCompilerHelper {
    public companion object {
        private val log = LoggerFactory.getLogger(OpenModelThreadGroup::class.java)

        /** Thread group schedule. See [ThreadSchedule]. */
        @Deprecated(
            message = "Use OpenModelThreadGroupSchema instead",
            replaceWith = ReplaceWith(
                "OpenModelThreadGroupSchema.getSchedule",
                imports = ["org.apache.jmeter.threads.openmodel.OpenModelThreadGroupSchema"]
            ),
            DeprecationLevel.WARNING
        )
        public val SCHEDULE: String = OpenModelThreadGroupSchema.schedule.name

        /** The seed for reproducible workloads. 0 means no seed, so the schedule would be new on every execution */
        @Deprecated(
            message = "Use OpenModelThreadGroupSchema instead",
            replaceWith = ReplaceWith(
                "OpenModelThreadGroupSchema.randomSeed",
                imports = ["org.apache.jmeter.threads.openmodel.OpenModelThreadGroupSchema"]
            ),
            DeprecationLevel.WARNING
        )
        public val RANDOM_SEED: String = OpenModelThreadGroupSchema.randomSeed.name

        /**
         * A thread pool for "thread starter thread".
         * It is not re-created across JMeter test restart
         */
        private val houseKeepingThreadPool = Executors.newCachedThreadPool()

        /**
         * Installation-wide hard cap on the number of concurrent threads, applied even when the
         * schedule has no `concurrency(...)` step. Values below one mean unlimited (the default).
         */
        public const val MAX_CONCURRENCY_PROPERTY: String = "openmodel.max_concurrency"

        private const val serialVersionUID: Long = 1L
    }

    // A thread pool that executes main workload.
    // It is created on test start and it is shutdown on every test stop.
    // ExecutorService shutdown is the only way to wait for completion of all tasks.
    private var executorService: ExecutorService? = null

    private val threadStarterFuture = AtomicReference<Future<*>?>()
    private val activeThreads = ConcurrentHashMap<JMeterThread, Future<*>>()

    // Caps the number of concurrent threads. Created on test start.
    private val concurrencyGate = AtomicReference<ConcurrencyGate?>()

    override val schema: OpenModelThreadGroupSchema
        get() = OpenModelThreadGroupSchema

    override val props: PropertiesAccessor<@JvmWildcard OpenModelThreadGroup, @JvmWildcard OpenModelThreadGroupSchema>
        get() = PropertiesAccessor(this, schema)

    /**
     * Schedule expression (see [ThreadSchedule]).
     */
    public var scheduleString: String by OpenModelThreadGroupSchema.schedule

    public val randomSeed: Long by OpenModelThreadGroupSchema.randomSeed

    /**
     * Random seed for building reproducible schedules. 0 means random seed.
     */
    public var randomSeedString: String by OpenModelThreadGroupSchema.randomSeed.asString

    init {
        this[OpenModelThreadGroupSchema.mainController] = OpenModelThreadGroupController()
    }

    private class ThreadsStarter(
        private val testStartTime: Long,
        private val executorService: ExecutorService,
        private val activeThreads: MutableMap<JMeterThread, Future<*>>,
        private val gen: ThreadScheduleProcessGenerator,
        private val concurrencyGate: ConcurrencyGate,
        private val jmeterThreadFactory: (threadNumber: Int) -> JMeterThread,
    ) : Runnable {
        override fun run() {
            log.info("Thread starting init")
            val endTime = (testStartTime + gen.totalDuration * 1000).roundToLong()
            var threadNumber = 0
            var prevTime = 0L
            while (gen.hasNext()) {
                val scheduledTime = testStartTime + (gen.nextDouble() * 1000).roundToLong()
                // If multiple events are scheduled for the same millisecond, we don't want to call currentTimeMillis
                if (scheduledTime >= prevTime) {
                    prevTime = System.currentTimeMillis()
                    val nextDelay = scheduledTime - prevTime
                    if (nextDelay > 0) {
                        sleep(nextDelay)
                    }
                }
                // Reserve a thread slot before cloning the test plan, so a capped arrival waits
                // for a free thread instead of allocating a clone that would run out of memory.
                if (!concurrencyGate.acquire(endTime)) {
                    // The schedule window ended while waiting for a free thread, so stop launching.
                    break
                }
                val jmeterThread = jmeterThreadFactory(threadNumber++)
                jmeterThread.endTime = endTime
                activeThreads[jmeterThread] = executorService.submit {
                    Thread.currentThread().name = jmeterThread.threadName
                    jmeterThread.run()
                }
            }
            logConcurrencySummary()
            // If test schedule ends with a pause, then we need to wait for it
            val timeLeft = endTime - System.currentTimeMillis()
            if (timeLeft > 0) {
                log.info(
                    "There will be no more events, so will wait for {} sec till the end of the schedule",
                    TimeUnit.MILLISECONDS.toSeconds(timeLeft)
                )
                sleep(timeLeft)
            } else {
                log.info("Thread schedule finished {} ms ago", -timeLeft)
            }
            // No more actions will be scheduled, let awaitTermination to see the completion
            val threadsStillRunning = activeThreads.size
            if (threadsStillRunning == 0) {
                log.info("There will be no more events, will shutdown the thread pool")
            } else {
                log.info(
                    "Test schedule finished, however, there are {} thread(s) still running." +
                        " Will interrupt the threads." +
                        " If you want to keep some time for the threads to complete, consider" +
                        " adding pause(10 min) at the end of the schedule.",
                    threadsStillRunning
                )
                @Suppress("JavaMapForEach")
                activeThreads.forEach { thread, future ->
                    log.info("Terminating thread {}", thread)
                    // Safe stop the thread
                    thread.stop()
                    thread.interrupt()
                    // Interrupt it
                    future.cancel(true)
                }
            }
            executorService.shutdownNow()
            log.info("Thread starting done")
        }

        private fun logConcurrencySummary() {
            val delayed = concurrencyGate.delayedArrivals
            val dropped = concurrencyGate.droppedArrivals
            if (delayed == 0L && dropped == 0L) {
                return
            }
            log.warn(
                "Concurrency limit affected the load: {} arrival(s) delayed (total {} ms, max {} ms)," +
                    " {} arrival(s) dropped after the schedule ended." +
                    " The achieved rate was below the target rate; raise concurrency(...) to reach it.",
                delayed, concurrencyGate.totalDelayMillis, concurrencyGate.maxDelayMillis, dropped
            )
        }
    }

    override fun recoverRunningVersion() {
        // There's no state in OpenModelThreadGroup, so we can skip recoverRunningVersion
    }

    override fun start(
        threadGroupIndex: Int,
        notifier: ListenerNotifier,
        threadGroupTree: ListedHashTree,
        engine: StandardJMeterEngine
    ) {
        try {
            val jMeterContext = JMeterContextService.getContext()
            val variables = jMeterContext.variables
            val schedule = scheduleString
            log.info("Starting OpenModelThreadGroup#{} with schedule {}", threadGroupIndex, schedule)
            val parsedSchedule = ThreadSchedule(schedule)
            val seed = randomSeed
            val rnd = if (seed == 0L) Random() else Random(seed)
            val gen = ThreadScheduleProcessGenerator(rnd, parsedSchedule)
            val testStartTime = this.startTime
            val executorService = Executors.newCachedThreadPool()
            this.executorService = executorService
            val hardLimit = JMeterUtils.getPropDefault(MAX_CONCURRENCY_PROPERTY, 0)
                .let { if (it <= 0) Int.MAX_VALUE else it }
            val gate = ConcurrencyGate(ConcurrencyLimit.of(parsedSchedule), hardLimit, testStartTime)
            concurrencyGate.set(gate)
            val starter = ThreadsStarter(testStartTime, executorService, activeThreads, gen, gate) { threadNumber ->
                val clonedTree = cloneTree(threadGroupTree)
                makeThread(engine, this, notifier, threadGroupIndex, threadNumber, clonedTree, variables)
            }
            threadStarterFuture.set(
                houseKeepingThreadPool.submit {
                    Thread.currentThread().name = "open-model-thread-starter-$name-$threadGroupIndex"
                    starter.run()
                }
            )
        } catch (t: Throwable) {
            throw t.apply {
                addSuppressed(IllegalArgumentException("Failed to start OpenModelThreadGroup $name-$threadGroupIndex"))
            }
        }
    }

    override fun threadFinished(thread: JMeterThread?) {
        activeThreads.remove(thread)
        // Every launched thread reserved a slot in the gate before it started and calls
        // threadFinished exactly once, so release here regardless of the activeThreads state:
        // a fast thread may finish before the starter records it in activeThreads.
        concurrencyGate.get()?.release()
    }

    override fun addNewThread(delay: Int, engine: StandardJMeterEngine?): JMeterThread {
        TODO("Will not be implemented as the semantics of the API is unclear")
    }

    override fun stopThread(threadName: String?, now: Boolean): Boolean {
        TODO("Will not be implemented as the semantics of the API is unclear")
    }

    override fun numberOfActiveThreads(): Int = activeThreads.size

    /** Number of arrivals that had to wait for a free thread because of the concurrency limit. */
    @API(status = API.Status.EXPERIMENTAL, since = "6.0")
    public fun getDelayedArrivals(): Long = concurrencyGate.get()?.delayedArrivals ?: 0

    /** Number of arrivals dropped because the schedule ended before a thread became free. */
    @API(status = API.Status.EXPERIMENTAL, since = "6.0")
    public fun getDroppedArrivals(): Long = concurrencyGate.get()?.droppedArrivals ?: 0

    override fun verifyThreadsStopped(): Boolean =
        executorService?.awaitTermination(0, TimeUnit.SECONDS) != false

    override fun waitThreadsStopped() {
        executorService?.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS)
    }

    override fun stop() {
        log.info("Gracefully stopping the threads")
        threadStarterFuture.getAndSet(null)?.cancel(true)
        // We use Java's forEach since ConcurrentHashMap has a slightly better implementation
        // than Kotlin's generic forEach
        @Suppress("JavaMapForEach")
        activeThreads.forEach { thread, _ ->
            log.info("Gracefully stopping thread {}", thread)
            // Safe stop the thread
            thread.stop()
        }
    }

    override fun tellThreadsToStop() {
        // Graceful stop
        stop()
        log.info("Interrupting the threads")
        activeThreads.forEach { (thread, future) ->
            log.info("Interrupting thread {}", thread)
            // Interrupting the thread
            thread.interrupt()
            future.cancel(true)
        }
        // Interrupting all the threads
        // shutdownNow will interrupt the threads
        executorService?.shutdownNow()?.forEach {
            // In theory, there should be no "queued" runnables left,
            // However, if there's something, we remove it from activeThreads
            activeThreads.remove(it)
        }
    }
}
