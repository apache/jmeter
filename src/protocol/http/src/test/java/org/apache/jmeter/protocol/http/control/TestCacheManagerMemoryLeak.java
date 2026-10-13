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

package org.apache.jmeter.protocol.http.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.lang.reflect.Field;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

public class TestCacheManagerMemoryLeak {

    @Test
    @Timeout(30)
    public void testBenchmarkThreadLocalReuseUnderConcurrentLoad() throws Exception {
        CacheManager cacheManager = new CacheManager();
        cacheManager.setClearEachIteration(true);

        Field threadLocalField = CacheManager.class.getDeclaredField("threadCache");
        threadLocalField.setAccessible(true);

        Object initialThreadLocal = threadLocalField.get(cacheManager);
        assertNotNull(initialThreadLocal, "Initial threadCache ThreadLocal should not be null");

        int threadCount = 50;
        int iterationsPerThread = 200;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(threadCount);
        AtomicInteger mismatchCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    for (int j = 0; j < iterationsPerThread; j++) {
                        cacheManager.clear();
                        Object current = threadLocalField.get(cacheManager);
                        if (current != initialThreadLocal) {
                            mismatchCount.incrementAndGet();
                        }
                    }
                } catch (Exception e) {
                    mismatchCount.incrementAndGet();
                } finally {
                    finishLatch.countDown();
                }
            });
        }

        long startNs = System.nanoTime();
        startLatch.countDown();
        finishLatch.await();
        long durationMs = (System.nanoTime() - startNs) / 1_000_000;

        executor.shutdown();

        assertEquals(0, mismatchCount.get(), "ThreadLocal instance mismatch count must be 0 (no new instances created)");
        System.out.println("Benchmark completed: " + (threadCount * iterationsPerThread)
                + " clearCache iterations across " + threadCount + " threads in " + durationMs + " ms.");
    }
}
