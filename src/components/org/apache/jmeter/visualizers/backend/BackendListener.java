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
import java.util.concurrent.atomic.LongAdder;
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
import org.apache.jmeter.visualizers.backend.graphite.GraphiteBackendListenerClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Async Listener that delegates SampleResult handling to implementations of {@link BackendListenerClient}
 * @since 2.13
 */
public class BackendListener extends AbstractTestElement
    implements Backend, Serializable, SampleListener, 
        TestStateListener, NoThreadClone, Remoteable {

    private static final class ListenerClientData {
        private BackendListenerClient client;
        private BlockingQueue<SampleResult> queue;
        private LongAdder queueWaits; // how many times we had to wait to queue a SampleResult        
        private LongAdder queueWaitTime; // how long we had to wait (nanoSeconds)
        // @GuardedBy("LOCK")
        private int instanceCount; // number of active tests
        private CountDownLatch latch;
    }

    private static final long serialVersionUID = 1L;

    private static final Logger log = LoggerFactory.getLogger(BackendListener.class);

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
    private static final transient SampleResult FINAL_SAMPLE_RESULT = new SampleResult();

    /*
     * This is needed for distributed testing where there is 1 instance
     * per server. But we need the total to be shared.
     */
    private static final Map<String, ListenerClientData> queuesByTestElementName =
            new ConcurrentHashMap<>();

    // Name of the test element. Set up by testStarted().
    private transient String myName;

    // Holds listenerClientData for this test element
    private transient ListenerClientData listenerClientData;

    /**
     * Create a BackendListener.
     */
    public BackendListener() {
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

    private Class<?> initClass() {
        String name = getClassname().trim();
        try {
            return Class.forName(name, false, Thread.currentThread().getContextClassLoader());
        } catch (Exception e) {
            log.error("{}\tException initialising: {}", whoAmI(), name, e);
        }
        return null;
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
            if (log.isDebugEnabled()) {
                log.debug("{} => Dropping SampleResult: {}", getName(), event.getResult());
            }
            return;
        }
        try {
            if (!listenerClientData.queue.offer(sr)){ // we failed to add the element first time
                listenerClientData.queueWaits.add(1L);
                long t1 = System.nanoTime();
                listenerClientData.queue.put(sr);
                long t2 = System.nanoTime();
                listenerClientData.queueWaitTime.add(t2-t1);
            }
        } catch (Exception err) {
            log.error("sampleOccurred, failed to queue the sample", err);
        }
    }

    /**
     * Thread that dequeues data from queue to send it to {@link BackendListenerClient}
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
            final boolean isDebugEnabled = log.isDebugEnabled();
            List<SampleResult> sampleResults = new ArrayList<>(listenerClientData.queue.size());
            try {
                try {

                    boolean endOfLoop = false;
                    while (!endOfLoop) {
                        if (isDebugEnabled) {
                            log.debug("Thread: {} taking SampleResult from queue: {}", Thread.currentThread().getName(),
                                    listenerClientData.queue.size());
                        }
                        SampleResult sampleResult = listenerClientData.queue.take();
                        if (isDebugEnabled) {
                            log.debug("Thread: {} took SampleResult: {}, isFinal: {}",
                                    Thread.currentThread().getName(),
                                    sampleResult,
                                    sampleResult == FINAL_SAMPLE_RESULT);
                        }
                        // try to process as many as possible
                        // The == comparison is not a mistake
                        while (!(endOfLoop = sampleResult == FINAL_SAMPLE_RESULT) && sampleResult != null ) { 
                            sampleResults.add(sampleResult);
                            if (isDebugEnabled) {
                                log.debug("Thread: {} polling from queue: {}", Thread.currentThread().getName(),
                                        listenerClientData.queue.size());
                            }
                            sampleResult = listenerClientData.queue.poll(); // returns null if nothing on queue currently
                            if (isDebugEnabled) {
                                log.debug("Thread: {} took from queue: {}, isFinal: {}", Thread.currentThread().getName(),
                                        sampleResult, sampleResult == FINAL_SAMPLE_RESULT);
                            }
                        }
                        if (isDebugEnabled) {
                            log.debug("Thread: {} exiting with FINAL EVENT: {}, null: {}",
                                    Thread.currentThread().getName(), sampleResult == FINAL_SAMPLE_RESULT,
                                    sampleResult == null);
                        }
                        sendToListener(backendListenerClient, context, sampleResults);
                        if(!endOfLoop) {
                            LockSupport.parkNanos(100);
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                // We may have been interrupted
                sendToListener(backendListenerClient, context, sampleResults);
                log.info("Worker ended");
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
    static void sendToListener(
            final BackendListenerClient backendListenerClient,
            final BackendListenerContext context,
            final List<SampleResult> sampleResults) {
        if (!sampleResults.isEmpty()) {
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
            log.error("Exception creating: {}", clientClass, e);
            return new ErrorBackendListenerClient();
        }
    }

    // TestStateListener implementation
    @Override
    public void testStarted() {
        testStarted("local"); //$NON-NLS-1$
    }

    @Override
    public void testStarted(String host) {
        if (log.isDebugEnabled()) {
            log.debug("{}\ttestStarted({})", whoAmI(), host);
        }

        int queueSize;
        final String size = getQueueSize();
        try {
            queueSize = Integer.parseInt(size);
        } catch (NumberFormatException nfe) {
            log.warn("Invalid queue size '{}' defaulting to {}", size, DEFAULT_QUEUE_SIZE);
            queueSize = Integer.parseInt(DEFAULT_QUEUE_SIZE);
        }

        synchronized (LOCK) {
            myName = getName();
            listenerClientData = queuesByTestElementName.get(myName);
            if (listenerClientData == null){
                // We need to do this to ensure in Distributed testing 
                // that only 1 instance of BackendListenerClient is used
                clientClass = initClass(); // may be null
                BackendListenerClient backendListenerClient = createBackendListenerClientImpl(clientClass);
                BackendListenerContext context = new BackendListenerContext((Arguments)getArguments().clone());

                listenerClientData = new ListenerClientData();
                listenerClientData.queue = new ArrayBlockingQueue<>(queueSize);
                listenerClientData.queueWaits = new LongAdder();
                listenerClientData.queueWaitTime = new LongAdder();
                listenerClientData.latch = new CountDownLatch(1);
                listenerClientData.client = backendListenerClient;
                if (log.isInfoEnabled()) {
                    log.info("{}: Starting worker with class: {} and queue capacity: {}", getName(), clientClass,
                            getQueueSize());
                }
                Worker worker = new Worker(backendListenerClient, (Arguments) getArguments().clone(), listenerClientData);
                worker.setDaemon(true);
                worker.start();
                if (log.isInfoEnabled()) {
                    log.info("{}: Started  worker with class: {}", getName(), clientClass);
                }
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
            ListenerClientData listenerClientDataForName = queuesByTestElementName.get(myName);
            if (log.isDebugEnabled()) {
                log.debug("testEnded called on instance {}#{}", myName, listenerClientDataForName.instanceCount);
            }
            if(listenerClientDataForName != null) {
                listenerClientDataForName.instanceCount--;
                if (listenerClientDataForName.instanceCount > 0){
                    // Not the last instance of myName
                    return;
                } else {
                    queuesByTestElementName.remove(myName);
                }
            } else {
                log.error("No listener client data found for BackendListener {}", myName);
            }
        }
        try {
            listenerClientData.queue.put(FINAL_SAMPLE_RESULT);
        } catch (Exception ex) {
            log.warn("testEnded() with exception: {}", ex, ex);
        }
        if (listenerClientData.queueWaits.longValue() > 0) {
            log.warn(
                    "QueueWaits: {}; QueueWaitTime: {} (nanoseconds), you may need to increase queue capacity, see property 'backend_queue_capacity'",
                    listenerClientData.queueWaits, listenerClientData.queueWaitTime);
        }
        try {
            listenerClientData.latch.await();
            BackendListenerContext context = new BackendListenerContext(getArguments());
            listenerClientData.client.teardownTest(context);
        } catch (Exception e) {
            throw new java.lang.IllegalStateException("Failed calling teardownTest", e);
        }
    }

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
            log.warn("ErrorBackendListenerClient#handleSampleResult called, noop");
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
        // Bug 59173 - don't save new default argument
        args.removeArgument(GraphiteBackendListenerClient.USE_REGEXP_FOR_SAMPLERS_LIST, 
                GraphiteBackendListenerClient.USE_REGEXP_FOR_SAMPLERS_LIST_DEFAULT);
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
