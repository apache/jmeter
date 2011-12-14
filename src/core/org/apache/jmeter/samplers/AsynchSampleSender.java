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
import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.util.JMeterError;
import org.apache.log.Logger;

public class AsynchSampleSender extends AbstractSampleSender implements Serializable {

    private static final long serialVersionUID = 251L;

    private static final Logger log = LoggingManager.getLoggerForClass();

    // Create unique object as marker for end of queue
    private transient static final SampleEvent FINAL_EVENT = new SampleEvent();

    private static final int serverConfiguredCapacity = JMeterUtils.getPropDefault("asynch.batch.queue.size", 100); // $NON-NLS-1$
    
    private final int clientConfiguredCapacity = JMeterUtils.getPropDefault("asynch.batch.queue.size", 100); // $NON-NLS-1$

    // created by client 
    private final RemoteSampleListener listener;

    private transient BlockingQueue<SampleEvent> queue; // created by server in readResolve method
    
    private transient long queueWaits; // how many times we had to wait to queue a sample
    
    private transient long queueWaitTime; // how long we had to wait (nanoSeconds)

    /**
     * Processed by the RMI server code.
     * @throws ObjectStreamException  
     */
    private Object readResolve() throws ObjectStreamException{
    	int capacity = getCapacity();
        log.info("Using batch queue size (asynch.batch.queue.size): " + capacity); // server log file
        queue = new ArrayBlockingQueue<SampleEvent>(capacity);        
        Worker worker = new Worker(queue, listener);
        worker.setDaemon(true);
        worker.start();
        return this;
    }

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
        log.info("Using Asynch Remote Sampler for this test run, queue size "+getCapacity());  // client log file
    }

    /**
     * @return capacity
     */
    private int getCapacity() {
    	return isClientConfigured() ? 
    			clientConfiguredCapacity : serverConfiguredCapacity;
    }
    
    public void testEnded() { // probably not used in server mode
        log.debug("Test ended()");
        try {
            listener.testEnded();
        } catch (RemoteException ex) {
            log.warn("testEnded()"+ex);
        }
    }

    public void testEnded(String host) {
        log.debug("Test Ended on " + host);
        try {
            listener.testEnded(host);
            queue.put(FINAL_EVENT);
        } catch (Exception ex) {
            log.warn("testEnded(host)"+ex);
        }
        if (queueWaits > 0) {
            log.info("QueueWaits: "+queueWaits+"; QueueWaitTime: "+queueWaitTime+" (nanoseconds)");            
        }
    }

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
                    List<SampleEvent> l = new ArrayList<SampleEvent>();
                    SampleEvent e = queue.take();
                    while (!(eof = (e == FINAL_EVENT)) && e != null) { // try to process as many as possible
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
            }
            log.debug("Worker ended");
        }
    }
}
