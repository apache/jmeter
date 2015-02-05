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

package org.apache.jmeter.visualizers.backend;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.LockSupport;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.engine.util.NoThreadClone;
import org.apache.jmeter.samplers.Remoteable;
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleListener;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * Async Listener that delegates SampleResult handling to implementations of {@link BackendListenerClient}
 * @since 2.13
 */
public class BackendListener extends AbstractTestElement
    implements Serializable, SampleListener, TestStateListener, NoThreadClone, Remoteable  {

    /**
     * 
     */
    private static final class ListenerClientData {
        private BackendListenerClient client;
        private BlockingQueue<SampleResult> queue;
        private AtomicLong queueWaits; // how many times we had to wait to queue a SampleResult        
        private AtomicLong queueWaitTime; // how long we had to wait (nanoSeconds)
        // @GuardedBy("LOCK")
        private int instanceCount; // number of active tests
        private CountDownLatch latch;
    }

    /**
     * 
     */
    private static final long serialVersionUID = 8184103677832024335L;

    private static final Logger LOGGER = LoggingManager.getLoggerForClass();

    /**
     * Property key representing the classname of the BackendListenerClient to user.
     */
    public static final String CLASSNAME = "classname";

    /**
     * Queue size
     */
    public static final String QUEUE_SIZE = "QUEUE_SIZE";

    /**
     * Lock used to protect accumulators update + instanceCount update
     */
    private static final Object LOCK = new Object();

    /**
     * Property key representing the arguments for the BackendListenerClient.
     */
    public static final String ARGUMENTS = "arguments";

    /**
     * The BackendListenerClient class used by this sampler.
     * Created by testStarted; copied to cloned instances.
     */
    private Class<?> clientClass;

    public static final String DEFAULT_QUEUE_SIZE = "5000";

    // Create unique object as marker for end of queue
    private transient static final SampleResult FINAL_SAMPLE_RESULT = new SampleResult();

    // Name of the test element. Set up by testStarted().
    private transient String myName;

    // Holds listenerClientData for this test element
    private transient ListenerClientData listenerClientData;

    /*
     * This is needed for distributed testing where there is 1 instance
     * per server. But we need the total to be shared.
     */
    //@GuardedBy("LOCK") - needed to ensure consistency between this and instanceCount
    private static final Map<String, ListenerClientData> queuesByTestElementName = 
            new ConcurrentHashMap<String, ListenerClientData>();

    /**
     * Create a BackendListener.
     */
    public BackendListener() {
        synchronized (LOCK) {
            queuesByTestElementName.clear();
        }

        setArguments(new Arguments());
    }

    /*
     * Ensure that the required class variables are cloned,
     * as this is not currently done by the super-implementation.
     */
    @Override
    public Object clone() {
        BackendListener clone = (BackendListener) super.clone();
        clone.clientClass = this.clientClass;
        return clone;
    }

    private void initClass() {
        String name = getClassname().trim();
        try {
            clientClass = Class.forName(name, false, Thread.currentThread().getContextClassLoader());
        } catch (Exception e) {
            LOGGER.error(whoAmI() + "\tException initialising: " + name, e);
        }
    }

    /**
     * Generate a String identifier of this instance for debugging purposes.
     *
     * @return a String identifier for this sampler instance
     */
    private String whoAmI() {
        StringBuilder sb = new StringBuilder();
        sb.append(Thread.currentThread().getName());
        sb.append("@");
        sb.append(Integer.toHexString(hashCode()));
        sb.append("-");
        sb.append(getName());
        return sb.toString();
    }


    /* (non-Javadoc)
     * @see org.apache.jmeter.samplers.SampleListener#sampleOccurred(org.apache.jmeter.samplers.SampleEvent)
     */
    @Override
    public void sampleOccurred(SampleEvent event) {
        Arguments args = getArguments();
        BackendListenerContext context = new BackendListenerContext(args);

        SampleResult sr = listenerClientData.client.createSampleResult(context, event.getResult());
        if(sr == null) {
            if(LOGGER.isDebugEnabled()) {
                LOGGER.debug(getName()+"=>Dropping SampleResult:"+event.getResult());
            }
            return;
        }
        try {
            if (!listenerClientData.queue.offer(sr)){ // we failed to add the element first time
                listenerClientData.queueWaits.incrementAndGet();
                long t1 = System.nanoTime();
                listenerClientData.queue.put(sr);
                long t2 = System.nanoTime();
                listenerClientData.queueWaitTime.addAndGet(t2-t1);
            }
        } catch (Exception err) {
            LOGGER.error("sampleOccurred, failed to queue the sample", err);
        }
    }

    /**
     * Thread that dequeus data from queue to send it to {@link BackendListenerClient}
     */
    private static final class Worker extends Thread {

        private final ListenerClientData listenerClientData;
        private final BackendListenerContext context;
        private final BackendListenerClient backendListenerClient;
        private Worker(BackendListenerClient backendListenerClient, Arguments arguments, ListenerClientData listenerClientData){
            this.listenerClientData = listenerClientData;
            // Allow BackendListenerClient implementations to get access to test element name
            arguments.addArgument(TestElement.NAME, getName());
            context = new BackendListenerContext(arguments);
            this.backendListenerClient = backendListenerClient;
        }

        @Override
        public void run() {
            boolean isDebugEnabled = LOGGER.isDebugEnabled();
            List<SampleResult> sampleResults = new ArrayList<SampleResult>(listenerClientData.queue.size());
            try {
                try {

                    boolean endOfLoop = false;
                    while (!endOfLoop) {
                        if(isDebugEnabled) {
                            LOGGER.debug("Thread:"+Thread.currentThread().getName()+" taking SampleResult from queue:"+listenerClientData.queue.size());
                        }
                        SampleResult sampleResult = listenerClientData.queue.take();
                        if(isDebugEnabled) {
                            LOGGER.debug("Thread:"+Thread.currentThread().getName()+" took SampleResult:"+sampleResult+", isFinal:" + (sampleResult==FINAL_SAMPLE_RESULT));
                        }
                        while (!(endOfLoop = (sampleResult == FINAL_SAMPLE_RESULT)) && sampleResult != null ) { // try to process as many as possible
                            sampleResults.add(sampleResult);
                            if(isDebugEnabled) {
                                LOGGER.debug("Thread:"+Thread.currentThread().getName()+" polling from queue:"+listenerClientData.queue.size());
                            }
                            sampleResult = listenerClientData.queue.poll(); // returns null if nothing on queue currently
                            if(isDebugEnabled) {
                                LOGGER.debug("Thread:"+Thread.currentThread().getName()+" took from queue:"+sampleResult+", isFinal:" + (sampleResult==FINAL_SAMPLE_RESULT));
                            }
                        }
                        if(isDebugEnabled) {
                            LOGGER.debug("Thread:"+Thread.currentThread().getName()+
                                    " exiting with FINAL EVENT:"+(sampleResult == FINAL_SAMPLE_RESULT)
                                    +", null:" + (sampleResult==null));
                        }
                        sendToListener(backendListenerClient, context, sampleResults);
                        if(!endOfLoop) {
                            LockSupport.parkNanos(100);
                        }
                    }
                } catch (InterruptedException e) {
                    // NOOP
                }
                // We may have been interrupted
                sendToListener(backendListenerClient, context, sampleResults);
                LOGGER.info("Worker ended");
            } finally {
                listenerClientData.latch.countDown();
            }
        }
    }

    /**
     * Send sampleResults to {@link BackendListenerClient}
     * @param backendListenerClient {@link BackendListenerClient}
     * @param context {@link BackendListenerContext}
     * @param sampleResults List of {@link SampleResult}
     */
    static final void sendToListener(final BackendListenerClient backendListenerClient, 
            final BackendListenerContext context, 
            final List<SampleResult> sampleResults) {
        if (sampleResults.size() > 0) {
            backendListenerClient.handleSampleResults(sampleResults, context);
            sampleResults.clear();
        }
    }

    /**
     * Returns reference to {@link BackendListener}
     * @param clientClass {@link BackendListenerClient} client class
     * @return BackendListenerClient reference.
     */
    static BackendListenerClient createBackendListenerClientImpl(Class<?> clientClass) {
        if (clientClass == null) { // failed to initialise the class
            return new ErrorBackendListenerClient();
        }
        try {
            return (BackendListenerClient) clientClass.newInstance();
        } catch (Exception e) {
            LOGGER.error("Exception creating: " + clientClass, e);
            return new ErrorBackendListenerClient();
        }
    }

    // TestStateListener implementation
    /**
     *  Implements TestStateListener.testStarted() 
     **/
    @Override
    public void testStarted() {
        testStarted("local"); //$NON-NLS-1$
    }

    /** Implements TestStateListener.testStarted(String) 
     **/
    @Override
    public void testStarted(String host) {
        if(LOGGER.isDebugEnabled()){
            LOGGER.debug(whoAmI() + "\ttestStarted(" + host + ")");
        }

        int queueSize;
        final String size = getQueueSize();
        try {
            queueSize = Integer.parseInt(size);
        } catch (NumberFormatException nfe) {
            LOGGER.warn("Invalid queue size '" + size + "' defaulting to " + DEFAULT_QUEUE_SIZE);
            queueSize = Integer.parseInt(DEFAULT_QUEUE_SIZE);
        }

        synchronized (LOCK) {
            myName = getName();
            listenerClientData = queuesByTestElementName.get(myName);
            if (listenerClientData == null){
                // We need to do this to ensure in Distributed testing 
                // that only 1 instance of BackendListenerClient is used
                initClass();
                BackendListenerClient backendListenerClient = createBackendListenerClientImpl(clientClass);
                BackendListenerContext context = new BackendListenerContext((Arguments)getArguments().clone());

                listenerClientData = new ListenerClientData();
                listenerClientData.queue = new ArrayBlockingQueue<SampleResult>(queueSize); 
                listenerClientData.queueWaits = new AtomicLong(0L);
                listenerClientData.queueWaitTime = new AtomicLong(0L);
                listenerClientData.latch = new CountDownLatch(1);
                listenerClientData.client = backendListenerClient;
                LOGGER.info(getName()+":Starting worker with class:"+clientClass +" and queue capacity:"+getQueueSize());
                Worker worker = new Worker(backendListenerClient, (Arguments) getArguments().clone(), listenerClientData);
                worker.setDaemon(true);
                worker.start();
                LOGGER.info(getName()+": Started  worker with class:"+clientClass);
                try {
                    backendListenerClient.setupTest(context);
                } catch (Exception e) {
                    throw new java.lang.IllegalStateException("Failed calling setupTest", e);
                }
                queuesByTestElementName.put(myName, listenerClientData);
            }
            listenerClientData.instanceCount++;
        }
    }

    /**
     * Method called at the end of the test. This is called only on one instance
     * of BackendListener. This method will loop through all of the other
     * BackendListenerClients which have been registered (automatically in the
     * constructor) and notify them that the test has ended, allowing the
     * BackendListenerClients to cleanup.
     * Implements TestStateListener.testEnded(String)
     */
    @Override
    public void testEnded(String host) {
        synchronized (LOCK) {
            ListenerClientData listenerClientData = queuesByTestElementName.get(myName);
            if(LOGGER.isDebugEnabled()) {
                LOGGER.debug("testEnded called on instance "+myName+"#"+listenerClientData.instanceCount);
            }
            listenerClientData.instanceCount--;
            if (listenerClientData.instanceCount > 0){
                // Not the last instance of myName
                return;
            }
        }
        try {
            listenerClientData.queue.put(FINAL_SAMPLE_RESULT);
        } catch (Exception ex) {
            LOGGER.warn("testEnded() with exception:"+ex.getMessage(), ex);
        }
        if (listenerClientData.queueWaits.get() > 0) {
            LOGGER.warn("QueueWaits: "+listenerClientData.queueWaits+"; QueueWaitTime: "+listenerClientData.queueWaitTime+
                    " (nanoseconds), you may need to increase queue capacity, see property 'backend_queue_capacity'");
        }
        try {
            listenerClientData.latch.await();
            BackendListenerContext context = new BackendListenerContext(getArguments());
            listenerClientData.client.teardownTest(context);
        } catch (Exception e) {
            throw new java.lang.IllegalStateException("Failed calling teardownTest", e);
        }
    }

    /** Implements TestStateListener.testEnded(String)
     **/
    @Override
    public void testEnded() {
        testEnded("local"); //$NON-NLS-1$
    }

    /**
     * A {@link BackendListenerClient} implementation used for error handling. If an
     * error occurs while creating the real BackendListenerClient object, it is
     * replaced with an instance of this class. Each time a sample occurs with
     * this class, the result is marked as a failure so the user can see that
     * the test failed.
     */
    static class ErrorBackendListenerClient extends AbstractBackendListenerClient {
        /**
         * Return SampleResult with data on error.
         *
         * @see BackendListenerClient#handleSampleResults(List, BackendListenerContext)
         */
        @Override
        public void handleSampleResults(List<SampleResult> sampleResults, BackendListenerContext context) {
            LOGGER.warn("ErrorBackendListenerClient#handleSampleResult called, noop");
            Thread.yield();
        }
    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.samplers.SampleListener#sampleStarted(org.apache.jmeter.samplers.SampleEvent)
     */
    @Override
    public void sampleStarted(SampleEvent e) {
        // NOOP
    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.samplers.SampleListener#sampleStopped(org.apache.jmeter.samplers.SampleEvent)
     */
    @Override
    public void sampleStopped(SampleEvent e) {
        // NOOP
    }

    /**
     * Set the arguments (parameters) for the BackendListenerClient to be executed
     * with.
     *
     * @param args
     *            the new arguments. These replace any existing arguments.
     */
    public void setArguments(Arguments args) {
        setProperty(new TestElementProperty(ARGUMENTS, args));
    }

    /**
     * Get the arguments (parameters) for the BackendListenerClient to be executed
     * with.
     *
     * @return the arguments
     */
    public Arguments getArguments() {
        return (Arguments) getProperty(ARGUMENTS).getObjectValue();
    }

    /**
     * Sets the Classname of the BackendListenerClient object
     *
     * @param classname
     *            the new Classname value
     */
    public void setClassname(String classname) {
        setProperty(CLASSNAME, classname);
    }

    /**
     * Gets the Classname of the BackendListenerClient object
     *
     * @return the Classname value
     */
    public String getClassname() {
        return getPropertyAsString(CLASSNAME);
    }

    /**
     * Sets the queue size
     *
     * @param queueSize the size of the queue
     *
     */
    public void setQueueSize(String queueSize) {
        setProperty(QUEUE_SIZE, queueSize, DEFAULT_QUEUE_SIZE);
    }

    /**
     * Gets the queue size
     *
     * @return int queueSize
     */
    public String getQueueSize() {
        return getPropertyAsString(QUEUE_SIZE, DEFAULT_QUEUE_SIZE);
    }
}
