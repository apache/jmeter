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

package org.apache.jmeter.samplers;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.util.JMeterError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sends samples in a separate Thread and in Batch mode
 */
public class AsynchSampleSender extends AbstractSampleSender implements Serializable {

    private static final long serialVersionUID = 252L;

    private static final Logger log = LoggerFactory.getLogger(AsynchSampleSender.class);

    // Create unique object as marker for end of queue
    private static transient final SampleEvent FINAL_EVENT = new SampleEvent();

    private static final int DEFAULT_QUEUE_SIZE = 100;

    private static final int SERVER_CONFIGURED_CAPACITY = JMeterUtils.getPropDefault("asynch.batch.queue.size", DEFAULT_QUEUE_SIZE); // $NON-NLS-1$

    private final int clientConfiguredCapacity = JMeterUtils.getPropDefault("asynch.batch.queue.size", DEFAULT_QUEUE_SIZE); // $NON-NLS-1$

    // created by client
    private final RemoteSampleListener listener;

    private transient BlockingQueue<SampleEvent> queue; // created by server in readResolve method

    private transient long queueWaits; // how many times we had to wait to queue a sample

    private transient long queueWaitTime; // how long we had to wait (nanoSeconds)

    /**
     * @deprecated only for use by test code
     */
    @Deprecated
    public AsynchSampleSender(){
        this(null);
        log.warn("Constructor only intended for use in testing"); // $NON-NLS-1$
    }

    // Created by SampleSenderFactory
    protected AsynchSampleSender(RemoteSampleListener listener) {
        this.listener = listener;
        if (log.isInfoEnabled()) {
            log.info("Using Asynch Remote Sampler for this test run, queue size: {}", getCapacity());  // client log file
        }
    }

    /**
     * Processed by the RMI server code.
     *
     * @return this
     * @throws ObjectStreamException never
     */
    protected Object readResolve() throws ObjectStreamException{
        int capacity = getCapacity();
        log.info("Using batch queue size (asynch.batch.queue.size): {}", capacity); // server log file
        queue = new ArrayBlockingQueue<>(capacity);
        Worker worker = new Worker(queue, listener);
        worker.setDaemon(true);
        worker.start();
        return this;
    }

    /**
     * @return capacity
     */
    private int getCapacity() {
        return isClientConfigured() ?
                clientConfiguredCapacity : SERVER_CONFIGURED_CAPACITY;
    }

    @Override
    public void testEnded(String host) {
        log.debug("Test Ended on {}", host);
        try {
            listener.testEnded(host);
            queue.put(FINAL_EVENT);
        } catch (Exception ex) {
            log.warn("testEnded(host)", ex);
        }
        if (queueWaits > 0) {
            log.info("QueueWaits: {}; QueueWaitTime: {} (nanoseconds)", queueWaits, queueWaitTime);
        }
    }

    @Override
    public void sampleOccurred(SampleEvent e) {
        try {
            if (!queue.offer(e)){ // we failed to add the element first time
                queueWaits++;
                long t1 = System.nanoTime();
                queue.put(e);
                long t2 = System.nanoTime();
                queueWaitTime += t2-t1;
            }
        } catch (Exception err) {
            log.error("sampleOccurred; failed to queue the sample", err);
        }
    }

    private static class Worker extends Thread {

        private final BlockingQueue<SampleEvent> queue;

        private final RemoteSampleListener listener;

        private Worker(BlockingQueue<SampleEvent> q, RemoteSampleListener l){
            queue = q;
            listener = l;
        }

        @Override
        public void run() {
            try {
                boolean eof = false;
                while (!eof) {
                    List<SampleEvent> l = new ArrayList<>();
                    SampleEvent e = queue.take();
                    // try to process as many as possible
                    // The == comparison is not an error
                    while (!(eof = e == FINAL_EVENT) && e != null) {
                        l.add(e);
                        e = queue.poll(); // returns null if nothing on queue currently
                    }
                    int size = l.size();
                    if (size > 0) {
                        try {
                            listener.processBatch(l);
                        } catch (RemoteException err) {
                            if (err.getCause() instanceof java.net.ConnectException){
                                throw new JMeterError("Could not return sample",err);
                            }
                            log.error("Failed to return sample", err);
                        }
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            log.debug("Worker ended");
        }
    }
}
