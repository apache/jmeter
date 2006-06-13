// $Header$
/*
 * Copyright 2004 The Apache Software Foundation.
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

package org.apache.jmeter.control;

import java.io.Serializable;

import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterThread;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.threads.ListenerNotifier;
import org.apache.jmeter.threads.SamplePackage;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * Transaction Controller to measure transaction times
 * 
 * @version $Revision$
 */
public class TransactionController extends GenericController implements Controller, Serializable {
	protected static final Logger log = LoggingManager.getLoggerForClass();

	transient private String threadName;

	transient private ListenerNotifier lnf;

	transient private JMeterContext threadContext;

	transient private JMeterVariables threadVars;

	transient private SampleResult res;

	/**
	 * Creates a Transaction Controller
	 */
	public TransactionController() {
		threadName = Thread.currentThread().getName();
		lnf = new ListenerNotifier();
	}

	private void log_debug(String s) {
		String n = this.getName();
		log.debug(threadName + " " + n + " " + s);
	}

	private int calls;

	/**
	 * @see org.apache.jmeter.control.Controller#next()
	 */
	public Sampler next() {
		Sampler returnValue = null;
		if (isFirst()) // must be the start of the subtree
		{
			log_debug("+++++++++++++++++++++++++++++");
			calls = 0;
			res = new SampleResult();
			res.sampleStart();
		}

		calls++;

		returnValue = super.next();

		if (returnValue == null) // Must be the end of the controller
		{
			log_debug("-----------------------------" + calls);
			if (res == null) {
				log_debug("already called");
			} else {
				res.sampleEnd();
				res.setSuccessful(true);
				res.setSampleLabel(getName());
				res.setResponseCodeOK();
				res.setResponseMessage("Called: " + calls);
				res.setThreadName(threadName);

				// TODO could these be done earlier (or just once?)
				threadContext = getThreadContext();
				threadVars = threadContext.getVariables();

				SamplePackage pack = (SamplePackage) threadVars.getObject(JMeterThread.PACKAGE_OBJECT);
				if (pack == null) {
					log.warn("Could not fetch SamplePackage");
				} else {
					lnf.notifyListeners(new SampleEvent(res, getName()), pack.getSampleListeners());
				}
				res = null;
			}
		}

		return returnValue;
	}
}
