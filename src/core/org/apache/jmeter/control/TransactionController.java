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
import org.apache.jmeter.testelement.property.BooleanProperty;
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
 * There are two different modes for the controller:
 * - generate additional total sample after nested samples (as in JMeter 2.2)
 * - generate parent sampler containing the nested samples
 * 
 */
public class TransactionController extends GenericController implements SampleListener, Controller, Serializable {
	private static final Logger log = LoggingManager.getLoggerForClass();

    transient private TransactionSampler transactionSampler;

	transient private ListenerNotifier lnf;

	transient private SampleResult res;
    
    transient private int calls;

    transient private int noFailingSamples;
    
	private static final String PARENT = "TransactionController.parent";// $NON-NLS-1$

	/**
	 * Creates a Transaction Controller
	 */
	public TransactionController() {
		lnf = new ListenerNotifier();
	}

    private Object readResolve(){
        lnf = new ListenerNotifier();
        return this;
    }

	public void setParent(boolean _parent){
		setProperty(new BooleanProperty(PARENT, _parent));
	}

	public boolean isParent(){
		return getPropertyAsBoolean(PARENT);
	}

	public Sampler next(){
		if (isParent()){
			return next1();
		} else {
			return next2();
		}
	}
	
///////////////// Transaction Controller - parent ////////////////

	/**
	 * @see org.apache.jmeter.control.Controller#next()
	 */
	public Sampler next1() {
        // Check if transaction is done
        if(transactionSampler != null && transactionSampler.isTransactionDone()) {
        	if (log.isDebugEnabled()) {
                log.debug("End of transaction " + getName());
        	}
            // This transaction is done
            transactionSampler = null;
            return null;
        }
        
        // Check if it is the start of a new transaction
		if (isFirst()) // must be the start of the subtree
		{
        	if (log.isDebugEnabled()) {
		        log.debug("Start of transaction " + getName());
        	}
		    transactionSampler = new TransactionSampler(this, getName());
		}

        // Sample the children of the transaction
		Sampler subSampler = super.next();
        transactionSampler.setSubSampler(subSampler);
        // If we do not get any sub samplers, the transaction is done
        if (subSampler == null) {
            transactionSampler.setTransactionDone();
        }
        return transactionSampler;
	}
	
	protected Sampler nextIsAController(Controller controller) throws NextIsNullException {
		if (!isParent()) {
			return super.nextIsAController(controller);
		}
		Sampler returnValue;
		Sampler sampler = controller.next();
		if (sampler == null) {
			currentReturnedNull(controller);
			// We need to call the super.next, instead of this.next, which is done in GenericController,
			// because if we call this.next(), it will return the TransactionSampler, and we do not want that.
			// We need to get the next real sampler or controller
			returnValue = super.next();
		} else {
			returnValue = sampler;
		}
		return returnValue;
	}

////////////////////// Transaction Controller - additional sample //////////////////////////////

	public Sampler next2() {
		if (isFirst()) // must be the start of the subtree
		{
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
			if (res != null) {
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
    	if (!isParent()) {
	        // Check if we are still sampling our children
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
    }

    public void sampleStarted(SampleEvent e) {
    }

    public void sampleStopped(SampleEvent e) {
    }
}
