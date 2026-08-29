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

import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * Caps the number of threads that run at the same time in an [OpenModelThreadGroup].
 *
 * The producer thread calls [acquire] before it clones the test plan, so a capped arrival waits for
 * a free thread instead of allocating a fresh clone. This keeps the memory bounded by the limit and
 * stops the group from piling threads onto a system that is already saturated.
 *
 * The limit is read from [concurrencyLimit] by the time elapsed since the schedule start, so it
 * still increases while [acquire] is blocked. A blocked producer wakes up either when a thread
 * finishes ([release]) or when the schedule raises the limit.
 *
 * @param concurrencyLimit per-schedule limit derived from `concurrency(...)` steps
 * @param hardLimit installation-wide cap that always applies, or [Int.MAX_VALUE] for none
 * @param testStartTime schedule start in [System.currentTimeMillis] units
 * @param warnIntervalMillis minimum gap between saturation warnings
 */
internal class ConcurrencyGate(
    private val concurrencyLimit: ConcurrencyLimit,
    private val hardLimit: Int,
    private val testStartTime: Long,
    private val warnIntervalMillis: Long = TimeUnit.SECONDS.toMillis(10),
) {
    private companion object {
        private val log = LoggerFactory.getLogger(ConcurrencyGate::class.java)
    }

    private val lock = ReentrantLock()
    private val slotFreed = lock.newCondition()
    private var running = 0

    private var everSaturated = false
    private var lastWarnMillis = Long.MIN_VALUE

    /** Number of arrivals that had to wait for a free thread. */
    @Volatile
    var delayedArrivals: Long = 0
        private set

    /** Longest single arrival delay in milliseconds. */
    @Volatile
    var maxDelayMillis: Long = 0
        private set

    /** Sum of all arrival delays in milliseconds. */
    @Volatile
    var totalDelayMillis: Long = 0
        private set

    /** Number of arrivals dropped because the schedule ended before a thread became free. */
    @Volatile
    var droppedArrivals: Long = 0
        private set

    /** True when no `concurrency(...)` step and no installation-wide cap apply. */
    val isUnlimited: Boolean get() = concurrencyLimit.isUnlimited && hardLimit == Int.MAX_VALUE

    private fun effectiveLimit(elapsedSeconds: Double): Int =
        minOf(concurrencyLimit.limitAt(elapsedSeconds), hardLimit)

    /**
     * Reserves a thread slot, blocking until one is free or [deadlineMillis] passes. The delay is
     * recorded in the counters.
     * @param deadlineMillis latest [System.currentTimeMillis] to keep waiting, usually the schedule
     *   end. An arrival that cannot start by then is dropped rather than stretching the test.
     * @return true if a slot was reserved, false if the deadline passed first
     * @throws InterruptedException if the producer is interrupted while waiting
     */
    @Throws(InterruptedException::class)
    fun acquire(deadlineMillis: Long): Boolean {
        if (isUnlimited) {
            return true
        }
        lock.lockInterruptibly()
        try {
            val entryMillis = System.currentTimeMillis()
            var blocked = false
            while (true) {
                val now = System.currentTimeMillis()
                val elapsedMillis = now - testStartTime
                val limit = effectiveLimit(elapsedMillis / 1000.0)
                if (running < limit) {
                    running++
                    if (blocked) {
                        recordDelay(System.currentTimeMillis() - entryMillis)
                    }
                    return true
                }
                if (now >= deadlineMillis) {
                    droppedArrivals++
                    return false
                }
                if (!blocked) {
                    blocked = true
                    maybeWarn(limit)
                }
                awaitSlotOrLimitChange(elapsedMillis, deadlineMillis)
            }
        } finally {
            lock.unlock()
        }
    }

    /** Releases a slot reserved by [acquire] and wakes one blocked producer. */
    fun release() {
        if (isUnlimited) {
            return
        }
        lock.withLock {
            running--
            slotFreed.signal()
        }
    }

    private fun awaitSlotOrLimitChange(elapsedMillis: Long, deadlineMillis: Long) {
        // Wake up no later than the schedule deadline so a dropped arrival is not stuck forever
        var waitMillis = deadlineMillis - (testStartTime + elapsedMillis)
        val nextChangeSeconds = concurrencyLimit.nextChangeAfter(elapsedMillis / 1000.0)
        if (nextChangeSeconds.isFinite()) {
            waitMillis = minOf(waitMillis, (nextChangeSeconds * 1000).toLong() - elapsedMillis)
        }
        if (waitMillis > 0) {
            slotFreed.await(waitMillis, TimeUnit.MILLISECONDS)
        }
    }

    private fun recordDelay(delayMillis: Long) {
        delayedArrivals++
        totalDelayMillis += delayMillis
        if (delayMillis > maxDelayMillis) {
            maxDelayMillis = delayMillis
        }
    }

    private fun maybeWarn(limit: Int) {
        val now = System.currentTimeMillis()
        if (!everSaturated) {
            everSaturated = true
            lastWarnMillis = now
            log.warn(
                "Concurrency limit of {} thread(s) reached, further arrivals are delayed until a thread" +
                    " becomes free. Increase concurrency(...) or reduce the rate to keep an open model.",
                limit
            )
        } else if (now - lastWarnMillis >= warnIntervalMillis) {
            lastWarnMillis = now
            log.warn(
                "Concurrency limit of {} thread(s) is still saturated, {} arrival(s) delayed so far" +
                    " (max delay {} ms).",
                limit, delayedArrivals, maxDelayMillis
            )
        }
    }
}
