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

package org.apache.jmeter.protocol.http.sampler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.util.JMeterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages the parallel http resources download.<br>
 * A shared thread pool is used by all the sample.<br>
 * A sampler will usually do the following
 * <pre> {@code
 *   // list of AsynSamplerResultHolder to download
 *   List<Callable<AsynSamplerResultHolder>> list = ...
 *
 *   // max parallel downloads
 *   int maxConcurrentDownloads = ...
 *
 *   // get the singleton instance
 *   ResourcesDownloader resourcesDownloader = ResourcesDownloader.getInstance();
 *
 *   // schedule the downloads and wait for the completion
 *   List<Future<AsynSamplerResultHolder>> retExec =
 *           resourcesDownloader.invokeAllAndAwaitTermination(maxConcurrentDownloads, list);
 *
 * }</pre>
 *
 * the call to invokeAllAndAwaitTermination will block until the downloads complete or get interrupted<br>
 * the Future list only contains task that have been scheduled in the threadpool.<br>
 * The status of those futures are either done or cancelled<br>
 * <br>
 *
 *  Future enhancements:
 *  <ul>
 *  <li>this implementation should be replaced with a NIO async download
 *   in order to reduce the number of threads needed</li>
 *  </ul>
 * @since 3.0
 */
public class ResourcesDownloader {

    private static final Logger LOG = LoggerFactory.getLogger(ResourcesDownloader.class);

    /** this is the maximum time that excess idle threads will wait for new tasks before terminating */
    private static final long THREAD_KEEP_ALIVE_TIME =
            JMeterUtils.getPropDefault("httpsampler.parallel_download_thread_keepalive_inseconds", 60L);

    private static final int MIN_POOL_SIZE = 1;
    private static final int MAX_POOL_SIZE = Integer.MAX_VALUE;

    /** Whether to use Java 21 Virtual Threads for resource downloads */
    private static final boolean VIRTUAL_THREADS_ENABLED =
            JMeterUtils.getPropDefault("jmeter.threads.virtual.enabled", true); // $NON-NLS-1$

    /** Cached reference to Executors.newThreadPerTaskExecutor() method for Java 21+ virtual thread support */
    private static final java.lang.invoke.MethodHandle NEW_VIRTUAL_THREAD_EXECUTOR;

    /** Counter for naming virtual threads */
    private static final AtomicLong VIRTUAL_THREAD_COUNTER = new AtomicLong(0);

    static {
        java.lang.invoke.MethodHandle newExecutor = null;
        if (VIRTUAL_THREADS_ENABLED) {
            try {
                // Try to get Executors.newThreadPerTaskExecutor(ThreadFactory) method (Java 21+)
                java.lang.invoke.MethodHandles.Lookup lookup = java.lang.invoke.MethodHandles.lookup();

                // Verify Thread.ofVirtual() exists (Java 21+)
                Thread.class.getMethod("ofVirtual");

                // Get the newThreadPerTaskExecutor method
                java.lang.reflect.Method newExecutorMethod = java.util.concurrent.Executors.class
                        .getMethod("newThreadPerTaskExecutor", java.util.concurrent.ThreadFactory.class);
                newExecutor = lookup.unreflect(newExecutorMethod);

                LOG.info("Virtual threads support enabled for ResourcesDownloader (Java 21+)");
            } catch (NoSuchMethodException | IllegalAccessException e) {
                LOG.warn("Virtual threads requested but not available for ResourcesDownloader (requires Java 21+), " +
                        "falling back to platform threads");
            }
        }
        NEW_VIRTUAL_THREAD_EXECUTOR = newExecutor;
    }

    private static final ResourcesDownloader INSTANCE = new ResourcesDownloader();

    public static ResourcesDownloader getInstance() {
        return INSTANCE;
    }

    private ExecutorService concurrentExecutor = null;
    private final boolean usingVirtualThreads;

    private ResourcesDownloader() {
        usingVirtualThreads = initVirtualThreadExecutor();
        if (!usingVirtualThreads) {
            initPlatformThreadExecutor();
        }
    }

    /**
     * Finds a method in the class hierarchy, including interfaces.
     * @param clazz the class to search
     * @param methodName the method name
     * @param paramTypes the parameter types
     * @return the method, or null if not found
     */
    private static java.lang.reflect.Method findMethodInHierarchy(
            Class<?> clazz, String methodName, Class<?>... paramTypes) {
        for (Class<?> c = clazz; c != null; c = c.getSuperclass()) {
            java.lang.reflect.Method method = findMethodInClassOrInterfaces(c, methodName, paramTypes);
            if (method != null) {
                return method;
            }
        }
        return null;
    }

    /**
     * Finds a method in a class or its interfaces.
     */
    private static java.lang.reflect.Method findMethodInClassOrInterfaces(
            Class<?> clazz, String methodName, Class<?>... paramTypes) {
        try {
            return clazz.getMethod(methodName, paramTypes);
        } catch (NoSuchMethodException ignored) {
            // Method not found in this class, try interfaces
            for (Class<?> iface : clazz.getInterfaces()) {
                try {
                    return iface.getMethod(methodName, paramTypes);
                } catch (NoSuchMethodException e) {
                    LOG.trace("Method {} not found in interface {}", methodName, iface.getName());
                }
            }
        }
        return null;
    }

    /**
     * Try to initialize a virtual thread executor (Java 21+).
     * @return true if virtual threads are enabled and available, false otherwise
     */
    private boolean initVirtualThreadExecutor() {
        if (NEW_VIRTUAL_THREAD_EXECUTOR == null) {
            return false;
        }
        try {
            // Create a ThreadFactory that creates virtual threads with custom names
            java.util.concurrent.ThreadFactory virtualFactory = r -> {
                try {
                    java.lang.invoke.MethodHandles.Lookup lookup = java.lang.invoke.MethodHandles.lookup();
                    java.lang.reflect.Method ofVirtualMethod = Thread.class.getMethod("ofVirtual");
                    Object builder = lookup.unreflect(ofVirtualMethod).invoke();

                    Class<?> builderClass = builder.getClass();
                    java.lang.reflect.Method nameMethod = findMethodInHierarchy(builderClass, "name", String.class);
                    if (nameMethod != null) {
                        builder = lookup.unreflect(nameMethod).invoke(builder,
                                "ResDownload-vt-" + VIRTUAL_THREAD_COUNTER.incrementAndGet());
                    }

                    java.lang.reflect.Method unstartedMethod = findMethodInHierarchy(builderClass, "unstarted", Runnable.class);
                    if (unstartedMethod != null) {
                        return (Thread) lookup.unreflect(unstartedMethod).invoke(builder, r);
                    }
                } catch (Throwable t) {
                    LOG.warn("Failed to create virtual thread in factory, creating platform thread", t);
                }
                // Fallback to platform thread
                Thread t = new Thread(r);
                t.setName("ResDownload-" + t.getName());
                t.setDaemon(true);
                return t;
            };

            concurrentExecutor = (ExecutorService) NEW_VIRTUAL_THREAD_EXECUTOR.invoke(virtualFactory);
            LOG.info("Created ResourcesDownloader with virtual threads");
            return true;
        } catch (Throwable t) {
            LOG.warn("Failed to create virtual thread executor, falling back to platform threads", t);
            return false;
        }
    }

    private void initPlatformThreadExecutor() {
        LOG.info("Creating ResourcesDownloader with platform threads, keepalive_inseconds : {}", THREAD_KEEP_ALIVE_TIME);
        concurrentExecutor = new ThreadPoolExecutor(
                MIN_POOL_SIZE, MAX_POOL_SIZE, THREAD_KEEP_ALIVE_TIME, TimeUnit.SECONDS,
                new SynchronousQueue<>(),
                r -> {
                    Thread t = new Thread(r);
                    t.setName("ResDownload-" + t.getName()); //$NON-NLS-1$
                    t.setDaemon(true);
                    return t;
                });
    }

    /**
     * this method will try to shrink the thread pool size as much as possible
     * it should be called at the end of a test
     */
    public void shrink() {
        // Virtual thread executors don't need shrinking - threads are very lightweight
        if (usingVirtualThreads) {
            return;
        }

        if (!(concurrentExecutor instanceof ThreadPoolExecutor poolExecutor)) {
            return;
        }

        if (poolExecutor.getPoolSize() <= MIN_POOL_SIZE) {
            return;
        }
        // drain the queue
        poolExecutor.purge();
        List<Runnable> drainList = new ArrayList<>();
        poolExecutor.getQueue().drainTo(drainList);
        if (!drainList.isEmpty()) {
            LOG.warn("the pool executor workqueue is not empty size={}", drainList.size());
            for (Runnable runnable : drainList) {
                if (runnable instanceof Future<?> f) {
                    f.cancel(true);
                } else {
                    LOG.warn("Content of workqueue is not an instance of Future");
                }
            }
        }

        // this will force the release of the extra threads that are idle
        // the remaining extra threads will be released with the keepAliveTime of the thread
        poolExecutor.setMaximumPoolSize(MIN_POOL_SIZE);

        // do not immediately restore the MaximumPoolSize as it will block the release of the threads
    }

    /**
     * This method will block until the downloads complete or it get interrupted
     * the Future list returned by this method only contains tasks that have been scheduled in the threadpool.<br>
     * The status of those futures are either done or cancelled
     *
     * @param maxConcurrentDownloads max concurrent downloads
     * @param list                   list of resources to download
     * @return list tasks that have been scheduled
     * @throws InterruptedException when interrupted while waiting
     */
    public List<Future<AsynSamplerResultHolder>> invokeAllAndAwaitTermination(
            int maxConcurrentDownloads, List<Callable<AsynSamplerResultHolder>> list) throws InterruptedException {
        List<Future<AsynSamplerResultHolder>> submittedTasks = new ArrayList<>();

        // paranoid fast path
        if (list.isEmpty()) {
            return submittedTasks;
        }

        // restore MaximumPoolSize original value (only for platform thread pools)
        if (concurrentExecutor instanceof ThreadPoolExecutor poolExecutor) {
            poolExecutor.setMaximumPoolSize(MAX_POOL_SIZE);
            if (LOG.isDebugEnabled()) {
                LOG.debug("PoolSize={} LargestPoolSize={}",
                        poolExecutor.getPoolSize(), poolExecutor.getLargestPoolSize());
            }
        }

        CompletionService<AsynSamplerResultHolder> completionService =
                new ExecutorCompletionService<>(concurrentExecutor);
        int remainingTasksToTake = list.size();

        try {
            // push the task in the threadpool until <maxConcurrentDownloads> is reached
            int i = 0;
            for (i = 0; i < Math.min(maxConcurrentDownloads, list.size()); i++) {
                Callable<AsynSamplerResultHolder> task = list.get(i);
                submittedTasks.add(completionService.submit(task));
            }

            // push the remaining tasks but ensure we use at most <maxConcurrentDownloads> threads
            // wait for a previous download to finish before submitting a new one
            for (; i < list.size(); i++) {
                Callable<AsynSamplerResultHolder> task = list.get(i);
                try {
                    completionService.take().get();
                } catch (ExecutionException e) {
                    throw new RuntimeException("Task execution failed", e.getCause());
                }
                remainingTasksToTake--;
                submittedTasks.add(completionService.submit(task));
            }

            // all the resources downloads are in the thread pool queue
            // wait for the completion of all downloads
            while (remainingTasksToTake > 0) {
                try {
                    completionService.take().get();
                } catch (ExecutionException e) {
                    throw new RuntimeException("Task execution failed", e.getCause());
                }
                remainingTasksToTake--;
            }
        } finally {
            //bug 51925 : Calling Stop on Test leaks executor threads when concurrent download of resources is on
            if (remainingTasksToTake > 0) {
                LOG.debug("Interrupted while waiting for resource downloads : cancelling remaining tasks");
                for (Future<AsynSamplerResultHolder> future : submittedTasks) {
                    if (!future.isDone()) {
                        future.cancel(true);
                    }
                }
            }
        }

        return submittedTasks;
    }

    /**
     * Holder of AsynSampler result
     */
    public static class AsynSamplerResultHolder {
        private final HTTPSampleResult result;
        private final CollectionProperty cookies;

        /**
         * @param result  {@link HTTPSampleResult} to hold
         * @param cookies cookies to hold
         */
        public AsynSamplerResultHolder(HTTPSampleResult result, CollectionProperty cookies) {
            super();
            this.result = result;
            this.cookies = cookies;
        }

        /**
         * @return the result
         */
        public HTTPSampleResult getResult() {
            return result;
        }

        /**
         * @return the cookies
         */
        public CollectionProperty getCookies() {
            return cookies;
        }
    }

}
