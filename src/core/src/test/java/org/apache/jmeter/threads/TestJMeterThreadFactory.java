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

package org.apache.jmeter.threads;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.util.JMeterUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TestJMeterThreadFactory extends JMeterTestCase {

    private String previousPropertyValue;

    @BeforeEach
    void rememberProperty() {
        previousPropertyValue = JMeterUtils.getProperty(JMeterThreadFactory.USE_VIRTUAL_THREADS_PROPERTY);
    }

    @AfterEach
    void restoreProperty() {
        if (previousPropertyValue == null) {
            JMeterUtils.getJMeterProperties().remove(JMeterThreadFactory.USE_VIRTUAL_THREADS_PROPERTY);
        } else {
            JMeterUtils.setProperty(JMeterThreadFactory.USE_VIRTUAL_THREADS_PROPERTY, previousPropertyValue);
        }
    }

    @Test
    void platformThreadIsUsedByDefault() throws Exception {
        JMeterUtils.getJMeterProperties().remove(JMeterThreadFactory.USE_VIRTUAL_THREADS_PROPERTY);
        assertFalse(JMeterThreadFactory.isVirtualThreadsEnabled());

        AtomicReference<Thread> executingThread = new AtomicReference<>();
        Thread thread = JMeterThreadFactory.newThread(
                () -> executingThread.set(Thread.currentThread()), "platform-user-thread");
        assertEquals("platform-user-thread", thread.getName());
        assertFalse(JMeterThreadFactory.isVirtual(thread), "thread should not be virtual by default");

        thread.start();
        thread.join(TimeUnit.SECONDS.toMillis(10));
        assertFalse(thread.isAlive());
        assertEquals(thread, executingThread.get());
        assertFalse(JMeterThreadFactory.isVirtual(executingThread.get()));
    }

    @Test
    void platformThreadIsUsedWhenPropertyIsFalse() {
        JMeterUtils.setProperty(JMeterThreadFactory.USE_VIRTUAL_THREADS_PROPERTY, "false");
        Thread thread = JMeterThreadFactory.newThread(() -> { }, "explicit-platform-user-thread");
        assertFalse(JMeterThreadFactory.isVirtual(thread));
    }

    @Test
    void virtualThreadIsUsedWhenPropertyIsTrueAndSupported() throws Exception {
        assumeTrue(JMeterThreadFactory.isVirtualThreadsSupported(),
                "virtual threads require Java 21+");
        JMeterUtils.setProperty(JMeterThreadFactory.USE_VIRTUAL_THREADS_PROPERTY, "true");
        assertTrue(JMeterThreadFactory.isVirtualThreadsEnabled());

        CountDownLatch ran = new CountDownLatch(1);
        AtomicReference<Thread> executingThread = new AtomicReference<>();
        AtomicReference<Boolean> virtualInsideRunnable = new AtomicReference<>();
        Thread thread = JMeterThreadFactory.newThread(() -> {
            executingThread.set(Thread.currentThread());
            virtualInsideRunnable.set(JMeterThreadFactory.isVirtual(Thread.currentThread()));
            ran.countDown();
        }, "virtual-user-thread");

        assertEquals("virtual-user-thread", thread.getName());
        assertTrue(JMeterThreadFactory.isVirtual(thread),
                "factory should create a virtual thread when the property is enabled");
        assertTrue(thread.isDaemon(), "virtual threads are always daemon threads");

        thread.start();
        assertTrue(ran.await(10, TimeUnit.SECONDS), "runnable should have been executed");
        thread.join(TimeUnit.SECONDS.toMillis(10));
        assertFalse(thread.isAlive());
        assertEquals(thread, executingThread.get());
        assertEquals(Boolean.TRUE, virtualInsideRunnable.get(),
                "the user-thread workload should execute on a virtual thread");
    }

    @Test
    void virtualThreadCanBeInterruptedAndJoined() throws Exception {
        assumeTrue(JMeterThreadFactory.isVirtualThreadsSupported(),
                "virtual threads require Java 21+");
        JMeterUtils.setProperty(JMeterThreadFactory.USE_VIRTUAL_THREADS_PROPERTY, "true");

        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch interrupted = new CountDownLatch(1);
        Thread thread = JMeterThreadFactory.newThread(() -> {
            started.countDown();
            try {
                Thread.sleep(TimeUnit.MINUTES.toMillis(1));
            } catch (InterruptedException e) {
                interrupted.countDown();
            }
        }, "interruptible-virtual-user-thread");

        thread.start();
        assertTrue(started.await(10, TimeUnit.SECONDS));
        thread.interrupt();
        assertTrue(interrupted.await(10, TimeUnit.SECONDS),
                "interrupt should wake a virtual thread blocked in sleep, as stopThread relies on it");
        thread.join(TimeUnit.SECONDS.toMillis(10));
        assertFalse(thread.isAlive());
    }
}
