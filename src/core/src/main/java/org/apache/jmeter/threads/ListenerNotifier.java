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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.jmeter.samplers.JMeterThreadUnboundSampleListener;
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleListener;
import org.apache.jmeter.testbeans.TestBeanHelper;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Processes sample events. <br>
 * The current implementation processes events in the calling thread
 * using {@link #notifyListeners(SampleEvent, List)} <br>
 * Thread safe class
 */
public final class ListenerNotifier implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = -4861457279068497918L;
    private static final Logger log = LoggerFactory.getLogger(ListenerNotifier.class);
    private static final int QUEUE_SIZE = JMeterUtils.getPropDefault("jmeter.save.queue.size", 5000);
    // Create unique object as marker for end of queue
    private static transient final Pair<SampleEvent, List<SampleListener>> FINAL_EVENT = Pair.of(new SampleEvent(), null);
    /**
     * Initialization On Demand Holder pattern
     */
    private static class ListenerNotifierHolder {
        public static final ListenerNotifier INSTANCE = new ListenerNotifier();
    }

    /**
     * @return ListenerNotifier singleton
     */
    public static ListenerNotifier getInstance() {
        return ListenerNotifierHolder.INSTANCE;
    }

    private Thread queueConsumer;

    private BlockingQueue<Pair<SampleEvent, List<SampleListener>>> queue;

    /**
     * Private constructor
     */
    private ListenerNotifier() {
        super();
    }

    /**
     * Notify a list of listeners that a sample has occurred.
     *
     * @param res
     *            the sample event that has occurred. Must be non-null.
     * @param listeners
     *            a list of the listeners which should be notified. This list
     *            must not be null and must contain only SampleListener
     *            elements.
     */
    public void notifyListeners(SampleEvent res, List<SampleListener> listeners) {
        if (QUEUE_SIZE > 0) {
            try {
                List<SampleListener> threadBoundSampleListeners = new ArrayList<>(listeners.size());
                for (SampleListener sampleListener : listeners) {
                	if (sampleListener.getClass().getAnnotation(JMeterThreadUnboundSampleListener.class) == null) {
                        threadBoundSampleListeners.add(sampleListener);
                    }
                }
                if (!threadBoundSampleListeners.isEmpty()) {
                    // Notify JMeterThreadBoundSampleListener listeners within JMeterThread
                    pNotifyListeners(res, threadBoundSampleListeners);
                    // We must copy the listener to avoid changing underlying SamplePackage listeners
                    List<SampleListener> threadUnboundSampleListeners = new ArrayList<>(listeners);
                    threadUnboundSampleListeners.removeAll(threadBoundSampleListeners);
                    listeners = threadUnboundSampleListeners;
                }
                // Notify JMeterThreadUnboundSampleListener listeners asynchronously
                queue.put(Pair.of(res, listeners));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        } else {
            pNotifyListeners(res, listeners);
        }
    }

    /**
     * Notify a list of listeners that a sample has occurred.
     *
     * @param res
     *            the sample event that has occurred. Must be non-null.
     * @param listeners
     *            a list of the listeners which should be notified. This list
     *            must not be null and must contain only SampleListener
     *            elements.
     */
    private void pNotifyListeners(SampleEvent res, List<SampleListener> listeners) {
        for (SampleListener sampleListener : listeners) {
            try {
                TestBeanHelper.prepare((TestElement) sampleListener);
                sampleListener.sampleOccurred(res);
            } catch (RuntimeException e) {
                log.error("Detected problem in Listener.", e);
                log.info("Continuing to process further listeners");
            }
        }
    }

    private class SampleEventConsumer implements Runnable {
        @Override
        public void run() {
            Pair<SampleEvent, List<SampleListener>> event;
            log.info("Thread {} starting", Thread.currentThread().getName());
            try {
                while ((event = queue.take()) != FINAL_EVENT) {
                    pNotifyListeners(event.getLeft(), event.getRight());
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            log.info("Thread {} exiting", Thread.currentThread().getName());
        }
    }

    /**
     * Setup on test start
     */
    public void testStarted() {
        if (QUEUE_SIZE > 0) {
            log.info("Configuring queue of size:{}", QUEUE_SIZE);
            queue = new ArrayBlockingQueue<>(QUEUE_SIZE);
            queueConsumer = new Thread(new SampleEventConsumer(), "ListenerNotifier-QueueConsumer");
            queueConsumer.setDaemon(true);
            queueConsumer.start();
        } else {
            log.info("No queue configured for SampleResult notification");
        }
    }

    /**
     * Teardown on test end
     */
    public void testEnded() {
        if (QUEUE_SIZE > 0) {
            log.info("Test ended, Interrupting queueConsumer of ListenerNotifier");
            try {
                queue.put(FINAL_EVENT);
                queueConsumer.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        } else {
            log.info("Test ended called");
        }
    }
}
