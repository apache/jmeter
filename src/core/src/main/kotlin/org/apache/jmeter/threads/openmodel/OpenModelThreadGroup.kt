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
import java.lang.invoke.MethodHandles
import java.util.Random
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.ThreadFactory
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.roundToLong

/**
 * The thread group that emulates open model.
 * Currently, threads are created on demand, every thread exists after completion,
 * and the maximum number of threads is not limited.
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

        /** Whether to use Java 21 Virtual Threads */
        private val VIRTUAL_THREADS_ENABLED =
            JMeterUtils.getPropDefault("jmeter.threads.virtual.enabled", true)

        /** Counter for naming virtual threads */
        private val virtualThreadCounter = AtomicLong(0)

        /** Cached MethodHandle for creating virtual thread executor (Java 21+) */
        private val newVirtualThreadExecutor: java.lang.invoke.MethodHandle? = run {
            if (!VIRTUAL_THREADS_ENABLED) {
                return@run null
            }
            try {
                val lookup = MethodHandles.lookup()
                // Verify Thread.ofVirtual() exists (Java 21+)
                Thread::class.java.getMethod("ofVirtual")
                // Get the newThreadPerTaskExecutor method
                val method = Executors::class.java.getMethod(
                    "newThreadPerTaskExecutor",
                    ThreadFactory::class.java
                )
                val handle = lookup.unreflect(method)
                log.info("Virtual threads support enabled for OpenModelThreadGroup (Java 21+)")
                handle
            } catch (e: NoSuchMethodException) {
                log.warn(
                    "Virtual threads requested but not available for OpenModelThreadGroup " +
                        "(requires Java 21+), falling back to platform threads"
                )
                null
            } catch (e: IllegalAccessException) {
                log.warn(
                    "Virtual threads requested but not available for OpenModelThreadGroup " +
                        "(requires Java 21+), falling back to platform threads"
                )
                null
            }
        }

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

        private const val serialVersionUID: Long = 1L

        /**
         * Creates an ExecutorService that uses virtual threads on Java 21+,
         * or falls back to a cached thread pool on older versions.
         */
        private fun createExecutorService(): ExecutorService {
            val executor = newVirtualThreadExecutor
            if (executor != null) {
                try {
                    val factory = ThreadFactory { r ->
                        createVirtualThread(r, "OpenModel-vt-${virtualThreadCounter.incrementAndGet()}")
                            ?: Thread(r).apply { name = "OpenModel-$name" }
                    }
                    val result = executor.invoke(factory) as ExecutorService
                    log.debug("Created virtual thread executor for OpenModelThreadGroup")
                    return result
                } catch (t: Throwable) {
                    log.warn("Failed to create virtual thread executor, falling back to cached thread pool", t)
                }
            }
            return Executors.newCachedThreadPool()
        }

        /**
         * Creates a virtual thread using reflection (for Java 21+ compatibility).
         */
        private fun createVirtualThread(runnable: Runnable, name: String): Thread? {
            return try {
                val lookup = MethodHandles.lookup()
                val ofVirtualMethod = Thread::class.java.getMethod("ofVirtual")
                var builder = lookup.unreflect(ofVirtualMethod).invoke()

                val builderClass = builder.javaClass
                val nameMethod = findMethodInHierarchy(builderClass, "name", String::class.java)
                if (nameMethod != null) {
                    builder = lookup.unreflect(nameMethod).invoke(builder, name)
                }

                val unstartedMethod = findMethodInHierarchy(builderClass, "unstarted", Runnable::class.java)
                if (unstartedMethod != null) {
                    lookup.unreflect(unstartedMethod).invoke(builder, runnable) as Thread
                } else {
                    null
                }
            } catch (t: Throwable) {
                log.trace("Failed to create virtual thread", t)
                null
            }
        }

        /**
         * Finds a method in the class hierarchy, including interfaces.
         */
        private fun findMethodInHierarchy(
            clazz: Class<*>,
            methodName: String,
            vararg paramTypes: Class<*>
        ): java.lang.reflect.Method? {
            var c: Class<*>? = clazz
            while (c != null) {
                try {
                    return c.getMethod(methodName, *paramTypes)
                } catch (ignored: NoSuchMethodException) {
                    // Try interfaces
                    for (iface in c.interfaces) {
                        try {
                            return iface.getMethod(methodName, *paramTypes)
                        } catch (e: NoSuchMethodException) {
                            // Continue searching
                        }
                    }
                }
                c = c.superclass
            }
            return null
        }
    }

    // A thread pool that executes main workload.
    // It is created on test start and it is shutdown on every test stop.
    // ExecutorService shutdown is the only way to wait for completion of all tasks.
    private var executorService: ExecutorService? = null

    private val threadStarterFuture = AtomicReference<Future<*>?>()
    private val activeThreads = ConcurrentHashMap<JMeterThread, Future<*>>()

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
                val jmeterThread = jmeterThreadFactory(threadNumber++)
                jmeterThread.endTime = endTime
                activeThreads[jmeterThread] = executorService.submit {
                    Thread.currentThread().name = jmeterThread.threadName
                    jmeterThread.run()
                }
            }
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
            val executorService = createExecutorService()
            this.executorService = executorService
            val starter = ThreadsStarter(testStartTime, executorService, activeThreads, gen) { threadNumber ->
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
    }

    override fun addNewThread(delay: Int, engine: StandardJMeterEngine?): JMeterThread {
        TODO("Will not be implemented as the semantics of the API is unclear")
    }

    override fun stopThread(threadName: String?, now: Boolean): Boolean {
        TODO("Will not be implemented as the semantics of the API is unclear")
    }

    override fun numberOfActiveThreads(): Int = activeThreads.size

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
