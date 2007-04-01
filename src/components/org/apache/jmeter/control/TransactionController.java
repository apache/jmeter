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

package org.apache.jmeter.control;

import java.io.Serializable;

import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleListener;
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
 */
public class TransactionController extends GenericController implements SampleListener, Controller, Serializable {
	protected static final Logger log = LoggingManager.getLoggerForClass();

	transient private String threadName;

	transient private ListenerNotifier lnf;

	transient private SampleResult res;
    
    transient private int calls;

    transient private int noFailingSamples;

	/**
	 * Creates a Transaction Controller
	 */
	public TransactionController() {
		threadName = Thread.currentThread().getName();
		lnf = new ListenerNotifier();
	}

    private Object readResolve(){
        threadName = Thread.currentThread().getName();
        lnf = new ListenerNotifier();
        return this;
    }

    private void log_debug(String s) {
		String n = this.getName();
		log.debug(threadName + " " + n + " " + s);
	}
    
	/**
	 * @see org.apache.jmeter.control.Controller#next()
	 */
	public Sampler next() {
		if (isFirst()) // must be the start of the subtree
		{
			log_debug("+++++++++++++++++++++++++++++");
			calls = 0;
            noFailingSamples = 0;
			res = new SampleResult();
            res.setSampleLabel(getName());
            // Assume success
            res.setSuccessful(true);
			res.sampleStart();
		}

        Sampler returnValue = super.next();
        
		if (returnValue == null) // Must be the end of the controller
		{
			log_debug("-----------------------------" + calls);
			if (res == null) {
				log_debug("already called");
			} else {
				res.sampleEnd();
                res.setResponseMessage("Number of samples in transaction : " + calls + ", number of failing samples : " + noFailingSamples);
                if(res.isSuccessful()) {
                    res.setResponseCodeOK();
                }

				// TODO could these be done earlier (or just once?)
                JMeterContext threadContext = getThreadContext();
                JMeterVariables threadVars = threadContext.getVariables();

				SamplePackage pack = (SamplePackage) threadVars.getObject(JMeterThread.PACKAGE_OBJECT);
				if (pack == null) {
					log.warn("Could not fetch SamplePackage");
				} else {
                    SampleEvent event = new SampleEvent(res, getName());
                    // We must set res to null now, before sending the event for the transaction,
                    // so that we can ignore that event in our sampleOccured method 
                    res = null;
					lnf.notifyListeners(event, pack.getSampleListeners());
				}
			}
		}
        else {
            // We have sampled one of our children
            calls++;            
        }

		return returnValue;
	}
    
    public void sampleOccurred(SampleEvent se) {
        // Check if we have are still sampling our children
        if(res != null) {
            SampleResult sampleResult = se.getResult(); 
            res.setThreadName(sampleResult.getThreadName());
            res.setBytes(res.getBytes() + sampleResult.getBytes());
            if(!sampleResult.isSuccessful()) {
                res.setSuccessful(false);
                noFailingSamples++;
            }
       }
    }

    public void sampleStarted(SampleEvent e) {
    }

    public void sampleStopped(SampleEvent e) {
    }
}
