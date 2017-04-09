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

import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleListener;
import org.apache.jmeter.testbeans.TestBeanHelper;
import org.apache.jmeter.testelement.TestElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Processes sample events. <br>
 * The current implementation processes events in the calling thread
 * using {@link #notifyListeners(SampleEvent, List)} <br>
 * Thread safe class 
 */
public class ListenerNotifier {
    private static final Logger log = LoggerFactory.getLogger(ListenerNotifier.class);


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

}
