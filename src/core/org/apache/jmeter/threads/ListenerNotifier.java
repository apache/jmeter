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
        for (SampleListener sampleListener : listeners) {
            try {
                TestBeanHelper.prepare((TestElement) sampleListener);
                sampleListener.sampleOccurred(res);
            } catch (RuntimeException e) {
                log.error("Detected problem in Listener: ", e);
                log.info("Continuing to process further listeners");
            }
        }
    }

}
