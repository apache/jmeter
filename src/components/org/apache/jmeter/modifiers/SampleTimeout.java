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

package org.apache.jmeter.modifiers;

import java.io.Serializable;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.apache.jmeter.samplers.Interruptible;
import org.apache.jmeter.samplers.SampleMonitor;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.ThreadListener;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * 
 * Sample timeout implementation using Executor threads
 * @since 3.0
 */
public class SampleTimeout extends AbstractTestElement implements Serializable, ThreadListener, SampleMonitor {

    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LoggingManager.getLoggerForClass();

    private static final String TIMEOUT = "InterruptTimer.timeout"; //$NON-NLS-1$

    private static class TPOOLHolder {
        static final ScheduledExecutorService EXEC_SERVICE =
                Executors.newScheduledThreadPool(1,
                        new ThreadFactory() {
                            @Override
                            public Thread newThread(Runnable r) {
                                Thread t = Executors.defaultThreadFactory().newThread(r);
                                t.setDaemon(true); // also ensures that Executor thread is daemon
                                return t;
                            }
                        });
    }

    private static ScheduledExecutorService getExecutorService() {
        return TPOOLHolder.EXEC_SERVICE;
    }

    private ScheduledFuture<?> future;
    
    private final transient ScheduledExecutorService execService;
    
    private final boolean debug;

    /**
     * No-arg constructor.
     */
    public SampleTimeout() {
        debug = LOG.isDebugEnabled();
        execService = getExecutorService();
        if (debug) {
            LOG.debug(whoAmI("InterruptTimer()", this));
        }
    }

    /**
     * Set the timeout for this timer.
     * @param timeout The timeout for this timer
     */
    public void setTimeout(String timeout) {
        setProperty(TIMEOUT, timeout);
    }

    /**
     * Get the timeout value for display.
     *
     * @return the timeout value for display.
     */
    public String getTimeout() {
        return getPropertyAsString(TIMEOUT);
    }

    @Override
    public void sampleStarting(Sampler sampler) {
        if (debug) {
            LOG.debug(whoAmI("sampleStarting()", this));
        }
        createTask(sampler);
    }

    @Override
    public void sampleEnded(final Sampler sampler) {
        if (debug) {
            LOG.debug(whoAmI("sampleEnded()", this));
        }
        cancelTask();
    }

    private void createTask(final Sampler samp) {
        long timeout = getPropertyAsLong(TIMEOUT); // refetch each time so it can be a variable
        if (timeout <= 0) {
            return;
        }
        if (!(samp instanceof Interruptible)) { // may be applied to a whole test 
            return; // Cannot time out in this case
        }
        final Interruptible sampler = (Interruptible) samp;

        Callable<Object> call = new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                long start = System.nanoTime();
                boolean interrupted = sampler.interrupt();
                String elapsed = Double.toString((double)(System.nanoTime()-start)/ 1000000000)+" secs";
                if (interrupted) {
                    LOG.warn("Call Done interrupting " + getInfo(samp) + " took " + elapsed);
                } else {
                    if (debug) {
                        LOG.debug("Call Didn't interrupt: " + getInfo(samp) + " took " + elapsed);
                    }
                }
                return null;
            }
        };
        // schedule the interrupt to occur and save for possible cancellation 
        future = execService.schedule(call, timeout, TimeUnit.MILLISECONDS);
        if (debug) {
            LOG.debug("Scheduled timer: @" + System.identityHashCode(future) + " " + getInfo(samp));
        }
    }

    @Override
    public void threadStarted() {
        if (debug) {
            LOG.debug(whoAmI("threadStarted()", this));
        }
     }

    @Override
    public void threadFinished() {
        if (debug) {
            LOG.debug(whoAmI("threadFinished()", this));
        }
        cancelTask(); // cancel future if any
     }

    /**
     * Provide a description of this class.
     *
     * @return the description of this class.
     */
    @Override
    public String toString() {
        return JMeterUtils.getResString("sample_timeout_memo"); //$NON-NLS-1$
    }

    private String whoAmI(String id, TestElement o) {
        return id + " @" + System.identityHashCode(o)+ " '"+ o.getName() + "' " + (debug ?  Thread.currentThread().getName() : "");         
    }

    private String getInfo(TestElement o) {
        return whoAmI(o.getClass().getSimpleName(), o); 
    }

    private void cancelTask() {
        if (future != null) {
            if (!future.isDone()) {
                boolean cancelled = future.cancel(false);
                if (debug) {
                    LOG.debug("Cancelled timer: @" + System.identityHashCode(future) + " with result " + cancelled);
                }
            }
            future = null;
        }        
    }

}
