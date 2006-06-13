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

import java.io.Serializable;

import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.engine.util.NoThreadClone;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.TestListener;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * @author unascribed
 * @version $Revision$
 */
public class RemoteTestListenerWrapper extends AbstractTestElement implements TestListener, Serializable, NoThreadClone {
	private static final Logger log = LoggingManager.getLoggerForClass();

	private RemoteSampleListener listener;

	public RemoteTestListenerWrapper() {
	}

	public RemoteTestListenerWrapper(RemoteSampleListener l) {
		listener = l;
	}

	public void testStarted() {
		try {
			listener.testStarted();
		} catch (Exception ex) {
			log.error("", ex); // $NON-NLS-1$
		}

	}

	public void testEnded() {
		try {
			listener.testEnded();
		} catch (Exception ex) {
			log.error("", ex); // $NON-NLS-1$
		}
	}

	public void testStarted(String host) {
		try {
			listener.testStarted(host);
		} catch (Exception ex) {
			log.error("", ex); // $NON-NLS-1$
		}
	}

	public void testEnded(String host) {
		try {
			listener.testEnded(host);
		} catch (Exception ex) {
			log.error("", ex); // $NON-NLS-1$
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