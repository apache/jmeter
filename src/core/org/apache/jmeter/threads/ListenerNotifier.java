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

/////////////////////////////////////////
////////
//////// This code is mostly unused at present
//////// it seems that only notifyListeners()
//////// is used.
////////
//////// However, it does look useful.
//////// And it may one day be used...
////////
/////////////////////////////////////////

package org.apache.jmeter.threads;

import java.util.Iterator;
import java.util.List;

//import org.apache.commons.collections.Buffer;
//import org.apache.commons.collections.BufferUtils;
//import org.apache.commons.collections.buffer.UnboundedFifoBuffer;
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleListener;
import org.apache.jmeter.testbeans.TestBeanHelper;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

///**
// * The <code>ListenerNotifier</code> thread is responsible for performing
// * asynchronous notifications that a sample has occurred. Each time a sample
// * occurs, the <code>addLast</code> method should be called to add the sample
// * and its list of listeners to the notification queue. This thread will then
// * notify those listeners asynchronously at some future time.
// * <p>
// * In the current implementation, the notifications will be made in batches,
// * with 2 seconds between the beginning of successive batches. If the notifier
// * thread starts to get behind, the priority of the thread will be increased in
// * an attempt to help it to keep up.
// *
// * @see org.apache.jmeter.samplers.SampleListener
// *
// */
/**
 * Processes sample events.
 * The current implementation processes events in the calling thread
 * using {@link #notifyListeners(SampleEvent, List)}
 * The other code is not used currently, so is commented out.
 */
public class ListenerNotifier {
    private static final Logger log = LoggingManager.getLoggerForClass();


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
    @SuppressWarnings("deprecation") // TestBeanHelper.prepare() is OK
    public void notifyListeners(SampleEvent res, List<SampleListener> listeners) {
        Iterator<SampleListener> iter = listeners.iterator();
        while (iter.hasNext()) {
            try {
                SampleListener sampleListener = iter.next();
                TestBeanHelper.prepare((TestElement) sampleListener);
                sampleListener.sampleOccurred(res);
            } catch (RuntimeException e) {
                log.error("Detected problem in Listener: ", e);
                log.info("Continuing to process further listeners");
            }
        }
    }

//    /**
//     * The number of milliseconds between batches of notifications.
//     */
//    private static final int SLEEP_TIME = 2000;
//
//    /**
//     * Indicates whether or not this thread should remain running. The thread
//     * will continue running after this field is set to false until the next
//     * batch of notifications has been completed and the notification queue is
//     * empty.
//     */
//    private boolean running = true;
//
//    /**
//     * Indicates whether or not this thread has stopped. No further
//     * notifications will be performed.
//     */
//    private boolean isStopped = true;
//
//    /**
//     * The queue containing the notifications to be performed. Each notification
//     * consists of a pair of entries in this queue. The first is the
//     * {@link org.apache.jmeter.samplers.SampleEvent SampleEvent} representing
//     * the sample. The second is a List of
//     * {@link org.apache.jmeter.samplers.SampleListener SampleListener}s which
//     * should be notified.
//     */
//    private Buffer listenerEvents = BufferUtils.synchronizedBuffer(new UnboundedFifoBuffer());
//
//    /**
//     * Stops the ListenerNotifier thread. The thread will continue processing
//     * any events remaining in the notification queue before it actually stops,
//     * but this method will return immediately.
//     */
//    public void stop() {
//        running = false;
//    }
//
//    /**
//     * Indicates whether or not the thread has stopped. This will not return
//     * true until the <code>stop</code> method has been called and any
//     * remaining notifications in the queue have been completed.
//     *
//     * @return true if the ListenerNotifier has completely stopped, false
//     *         otherwise
//     */
//    public boolean isStopped() {
//        return isStopped;
//    }
//
//    /**
//     * Process the events in the notification queue until the thread has been
//     * told to stop and the notification queue is empty.
//     * <p>
//     * In the current implementation, this method will iterate continually until
//     * the thread is told to stop. In each iteration it will process any
//     * notifications that are in the queue at the beginning of the iteration,
//     * and then will sleep until it is time to start the next batch. As long as
//     * the thread is keeping up, each batch should start 2 seconds after the
//     * beginning of the last batch. This exact behavior is subject to change.
//     */
//    public void run() {
//        boolean isMaximumPriority = false;
//        int normalCount = 0;
//
//        while (running) {
//            long startTime = System.currentTimeMillis();
//            processNotifications();
//            long sleep = SLEEP_TIME - (System.currentTimeMillis() - startTime);
//
//            // If the thread has been told to stop then we shouldn't sleep
//            if (!running) {
//                break;
//            }
//
//            if (sleep < 0) {
//                isMaximumPriority = true;
//                normalCount = 0;
//                if (log.isInfoEnabled()) {
//                    log.info("ListenerNotifier exceeded maximum " + "notification time by " + (-sleep) + "ms");
//                }
//                boostPriority();
//            } else {
//                normalCount++;
//
//                // If there have been three consecutive iterations since the
//                // last iteration which took too long to execute, return the
//                // thread to normal priority.
//                if (isMaximumPriority && normalCount >= 3) {
//                    isMaximumPriority = false;
//                    unboostPriority();
//                }
//
//                if (log.isDebugEnabled()) {
//                    log.debug("ListenerNotifier sleeping for " + sleep + "ms");
//                }
//
//                try {
//                    Thread.sleep(sleep);
//                } catch (InterruptedException e) {
//                }
//            }
//        }
//
//        // Make sure that all pending notifications are processed before
//        // actually ending the thread.
//        processNotifications();
//        isStopped = true;
//    }
//
//    /**
//     * Process all of the pending notifications. Only the samples which are in
//     * the queue when this method is called will be processed. Any samples added
//     * between the time when this method is called and when it exits are saved
//     * for the next batch.
//     */
//    private void processNotifications() {
//        int listenerEventsSize = listenerEvents.size();
//        if (log.isDebugEnabled()) {
//            log.debug("ListenerNotifier: processing " + listenerEventsSize + " events");
//        }
//
//        while (listenerEventsSize > 0) {
//            // Since this is a FIFO and this is the only place we remove
//            // from it (only from a single thread) we don't have to remove
//            // these two items in one atomic operation. Each individual
//            // remove is atomic (because we use a synchronized buffer),
//            // which is necessary since the buffer can be accessed from
//            // other threads (to add things to the buffer).
//            SampleEvent res = (SampleEvent) listenerEvents.remove();
//            List listeners = (List) listenerEvents.remove();
//
//            notifyListeners(res, listeners);
//
//            listenerEventsSize -= 2;
//        }
//    }
//
//    /**
//     * Boost the priority of the current thread to maximum priority. If the
//     * thread is already at maximum priority then this will have no effect.
//     */
//    private void boostPriority() {
//        if (Thread.currentThread().getPriority() != Thread.MAX_PRIORITY) {
//            log.info("ListenerNotifier: Boosting thread priority to maximum.");
//            Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
//        }
//    }
//
//    /**
//     * Return the priority of the current thread to normal. If the thread is
//     * already at normal priority then this will have no effect.
//     */
//    private void unboostPriority() {
//        if (Thread.currentThread().getPriority() != Thread.NORM_PRIORITY) {
//            log.info("ListenerNotifier: Returning thread priority to normal.");
//            Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
//        }
//    }
//
//    /**
//     * Add a new sample event to the notification queue. The notification will
//     * be performed asynchronously and this method will return immediately.
//     *
//     * @param item
//     *            the sample event that has occurred. Must be non-null.
//     * @param listeners
//     *            a list of the listeners which should be notified. This list
//     *            must not be null and must contain only SampleListener
//     *            elements.
//     */
//    public void addLast(SampleEvent item, List listeners) {
//        // Must use explicit synchronization here so that the item and
//        // listeners are added together atomically
//        synchronized (listenerEvents) {
//            listenerEvents.add(item);
//            listenerEvents.add(listeners);
//        }
//    }
}
