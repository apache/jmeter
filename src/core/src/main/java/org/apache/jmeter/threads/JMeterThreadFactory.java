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

import java.lang.reflect.Method;

import org.apache.jmeter.util.JMeterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates the {@link Thread} instances that run {@link JMeterThread}s.
 *
 * <p>By default this creates ordinary platform threads, exactly as JMeter has always done.
 * When the property {@value #USE_VIRTUAL_THREADS_PROPERTY} is set to {@code true} and the
 * JVM supports virtual threads (Java 21+), the user threads are created as virtual threads
 * instead. One dedicated thread is still created per {@code JMeterThread}: threads are never
 * pooled or shared between virtual users, so {@code ThreadLocal}-based state (for instance
 * {@link JMeterContextService}) keeps its current semantics.</p>
 *
 * <p>JMeter is compiled for Java 17, so the Java 21 virtual-thread APIs
 * ({@code Thread.ofVirtual()}, {@code Thread.isVirtual()}) are accessed reflectively.
 * On Java 17, or when reflective creation fails (for instance on a preview runtime
 * without {@code --enable-preview}), the factory silently falls back to platform threads,
 * apart from a one-time warning.</p>
 *
 * <p>Note that virtual threads are always daemon threads, and a virtual thread that holds
 * a monitor while performing blocking I/O may pin its carrier thread on Java 21.
 * That is one of the reasons this feature is opt-in.</p>
 */
public final class JMeterThreadFactory {

    private static final Logger log = LoggerFactory.getLogger(JMeterThreadFactory.class);

    /** Property that enables running JMeter user threads on virtual threads. */
    public static final String USE_VIRTUAL_THREADS_PROPERTY = "jmeterthread.use_virtual_threads"; // $NON-NLS-1$

    // Reflective handles for the Java 21 virtual thread API, null when unavailable.
    private static final Method OF_VIRTUAL_METHOD = lookupOfVirtualMethod();
    private static final Method BUILDER_NAME_METHOD = lookupBuilderMethod("name", String.class); // $NON-NLS-1$
    private static final Method BUILDER_UNSTARTED_METHOD = lookupBuilderMethod("unstarted", Runnable.class); // $NON-NLS-1$
    private static final Method IS_VIRTUAL_METHOD = lookupIsVirtualMethod();

    private static volatile boolean warnedVirtualThreadsUnavailable;

    private JMeterThreadFactory() {
        // utility class
    }

    /**
     * @return {@code true} when the opt-in property is set. Read dynamically on purpose:
     *         thread creation is not a hot path, and dynamic reads keep tests and GUI
     *         property changes simple.
     */
    public static boolean isVirtualThreadsEnabled() {
        return JMeterUtils.getPropDefault(USE_VIRTUAL_THREADS_PROPERTY, false);
    }

    /**
     * @return {@code true} when the current JVM offers the Java 21 virtual thread API.
     */
    public static boolean isVirtualThreadsSupported() {
        return OF_VIRTUAL_METHOD != null && BUILDER_NAME_METHOD != null && BUILDER_UNSTARTED_METHOD != null;
    }

    /**
     * Creates an unstarted thread for the given workload.
     *
     * @param runnable the workload, typically a {@link JMeterThread}
     * @param name     the thread name, preserved for both platform and virtual threads
     * @return an unstarted platform thread, or an unstarted virtual thread when virtual
     *         threads are enabled, supported, and creation succeeds
     */
    public static Thread newThread(Runnable runnable, String name) {
        if (isVirtualThreadsEnabled()) {
            Thread virtualThread = newVirtualThread(runnable, name);
            if (virtualThread != null) {
                return virtualThread;
            }
        }
        return new Thread(runnable, name);
    }

    /**
     * Reflective {@code Thread.isVirtual()} that also works when running on Java 17.
     *
     * @param thread the thread to inspect
     * @return {@code true} when the thread is a virtual thread, {@code false} otherwise
     *         (including on JVMs without virtual thread support)
     */
    public static boolean isVirtual(Thread thread) {
        if (thread == null || IS_VIRTUAL_METHOD == null) {
            return false;
        }
        try {
            return Boolean.TRUE.equals(IS_VIRTUAL_METHOD.invoke(thread));
        } catch (ReflectiveOperationException | RuntimeException e) {
            return false;
        }
    }

    private static Thread newVirtualThread(Runnable runnable, String name) {
        if (!isVirtualThreadsSupported()) {
            warnUnavailableOnce();
            return null;
        }
        try {
            Object builder = OF_VIRTUAL_METHOD.invoke(null);
            Object namedBuilder = BUILDER_NAME_METHOD.invoke(builder, name);
            return (Thread) BUILDER_UNSTARTED_METHOD.invoke(namedBuilder, runnable);
        } catch (ReflectiveOperationException | RuntimeException e) {
            // For instance, virtual threads are a preview feature on Java 19/20 and
            // creating one without --enable-preview fails. Fall back to a platform thread.
            if (log.isWarnEnabled()) {
                log.warn("Could not create a virtual thread, falling back to a platform thread", e);
            }
            return null;
        }
    }

    private static void warnUnavailableOnce() {
        if (!warnedVirtualThreadsUnavailable) {
            warnedVirtualThreadsUnavailable = true;
            if (log.isWarnEnabled()) {
                log.warn("{}=true, but virtual threads are not available on this JVM "
                        + "(Java 21+ is required); user threads will run on platform threads",
                        USE_VIRTUAL_THREADS_PROPERTY);
            }
        }
    }

    private static Method lookupOfVirtualMethod() {
        try {
            return Thread.class.getMethod("ofVirtual"); // $NON-NLS-1$
        } catch (NoSuchMethodException | RuntimeException e) {
            return null;
        }
    }

    private static Method lookupBuilderMethod(String methodName, Class<?> argumentType) {
        try {
            Class<?> ofVirtualBuilder = Class.forName("java.lang.Thread$Builder$OfVirtual"); // $NON-NLS-1$
            return ofVirtualBuilder.getMethod(methodName, argumentType);
        } catch (ClassNotFoundException | NoSuchMethodException | RuntimeException e) {
            return null;
        }
    }

    private static Method lookupIsVirtualMethod() {
        try {
            return Thread.class.getMethod("isVirtual"); // $NON-NLS-1$
        } catch (NoSuchMethodException | RuntimeException e) {
            return null;
        }
    }
}
