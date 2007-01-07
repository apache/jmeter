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

import org.apache.log.Logger;
import org.apache.jorphan.logging.LoggingManager;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.io.Serializable;

/**
 * Lars-Erik Helander provided the idea (and original implementation) for the
 * caching functionality (sampleStore).
 */

public class HoldSampleSender implements SampleSender, Serializable {
	private static final Logger log = LoggingManager.getLoggerForClass();

	private List sampleStore = new ArrayList();

	private RemoteSampleListener listener;

    public HoldSampleSender(){
        log.warn("Constructor only intended for use in testing"); // $NON-NLS-1$
    }
    
	HoldSampleSender(RemoteSampleListener listener) {
		log.info("Using Sample store for this test run");
		this.listener = listener;
	}

	public void testEnded() {
		log.info("Test ended()");
		try {
			synchronized (sampleStore) {
				Iterator i = sampleStore.iterator();
				while (i.hasNext()) {
					SampleEvent se = (SampleEvent) i.next();
					listener.sampleOccurred(se);
				}
			}
			listener.testEnded();
			sampleStore = null;
		} catch (Throwable ex) {
			log.warn("testEnded()", ex);
		}

	}

	public void testEnded(String host) {
		log.info("Test Ended on " + host); // should this be debug?
		try {
			Iterator i = sampleStore.iterator();
			while (i.hasNext()) {
				SampleEvent se = (SampleEvent) i.next();
				listener.sampleOccurred(se);
			}
			listener.testEnded(host);
			sampleStore = null;
		} catch (Throwable ex) {
			log.error("testEnded(host)", ex);
		}

	}

	public void SampleOccurred(SampleEvent e) {
		log.debug("Sample occurred");
		synchronized (sampleStore) {
			sampleStore.add(e);
		}
	}
}
