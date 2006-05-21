/*
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.List;

import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.testelement.TestListener;

/**
 * @version $Revision$
 */
public class RemoteSampleListenerImpl extends java.rmi.server.UnicastRemoteObject implements RemoteSampleListener,
		SampleListener, TestListener {
	TestListener testListener;

	SampleListener sampleListener;

	public RemoteSampleListenerImpl() throws RemoteException {
		super();
	}

	public void setListener(Object listener) {
		if (listener instanceof TestListener) {
			testListener = (TestListener) listener;
		}
		if (listener instanceof SampleListener) {
			sampleListener = (SampleListener) listener;
		}
	}

	public RemoteSampleListenerImpl(Object listener) throws RemoteException {
		super();
		setListener(listener);
	}

	public void testStarted() {
		if (testListener != null) {
			testListener.testStarted();
		}
	}

	public void testStarted(String host) {
		if (testListener != null) {
			testListener.testStarted(host);
		}
	}

	public void testEnded() {
		if (testListener != null) {
			testListener.testEnded();
		}
	}

	public void testEnded(String host) {
		if (testListener != null) {
			testListener.testEnded(host);
		}
	}

	/**
	 * This method is called remotely and fires a list of samples events
	 * recieved locally. The function is to reduce network load when using
	 * remote testing.
	 * 
	 * @param samples
	 *            the list of sample events to be fired locally
	 */
	public void processBatch(List samples) {
        if (samples != null) {
    		Iterator iter = samples.iterator();
			while (iter.hasNext()) {
				SampleEvent e = (SampleEvent) iter.next();
				sampleOccurred(e);
			}
		}
	}

	public void sampleOccurred(SampleEvent e) {
		if (sampleListener != null) {
			sampleListener.sampleOccurred(e);
		}
	}

	/**
	 * A sample has started.
	 */
	public void sampleStarted(SampleEvent e) {
		if (sampleListener != null) {
			sampleListener.sampleStarted(e);
		}
	}

	/**
	 * A sample has stopped.
	 */
	public void sampleStopped(SampleEvent e) {
		if (sampleListener != null) {
			sampleListener.sampleStopped(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see TestListener#testIterationStart(LoopIterationEvent)
	 */
	public void testIterationStart(LoopIterationEvent event) {
	}
}
