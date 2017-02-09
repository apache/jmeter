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
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterThread;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.threads.ListenerNotifier;
import org.apache.jmeter.threads.SamplePackage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Transaction Controller to measure transaction times
 *
 * There are two different modes for the controller:
 * - generate additional total sample after nested samples (as in JMeter 2.2)
 * - generate parent sampler containing the nested samples
 *
 */
public class TransactionController extends GenericController implements SampleListener, Controller, Serializable {
    /**
     * Used to identify Transaction Controller Parent Sampler
     */
    static final String NUMBER_OF_SAMPLES_IN_TRANSACTION_PREFIX = "Number of samples in transaction : ";

    private static final long serialVersionUID = 234L;
    
    private static final String TRUE = Boolean.toString(true); // i.e. "true"

    private static final String GENERATE_PARENT_SAMPLE = "TransactionController.parent";// $NON-NLS-1$

    private static final String INCLUDE_TIMERS = "TransactionController.includeTimers";// $NON-NLS-1$
    
    private static final Logger log = LoggerFactory.getLogger(TransactionController.class);

    private static final boolean DEFAULT_VALUE_FOR_INCLUDE_TIMERS = true; // default true for compatibility

    /**
     * Only used in parent Mode
     */
    private transient TransactionSampler transactionSampler;
    
    /**
     * Only used in NON parent Mode
     */
    private transient ListenerNotifier lnf;

    /**
     * Only used in NON parent Mode
     */
    private transient SampleResult res;
    
    /**
     * Only used in NON parent Mode
     */
    private transient int calls;
    
    /**
     * Only used in NON parent Mode
     */
    private transient int noFailingSamples;

    /**
     * Cumulated pause time to excluse timer and post/pre processor times
     * Only used in NON parent Mode
     */
    private transient long pauseTime;

    /**
     * Previous end time
     * Only used in NON parent Mode
     */
    private transient long prevEndTime;

    /**
     * Creates a Transaction Controller
     */
    public TransactionController() {
        lnf = new ListenerNotifier();
    }

    @Override
    protected Object readResolve(){
        super.readResolve();
        lnf = new ListenerNotifier();
        return this;
    }

    /**
     * @param generateParent flag whether a parent sample should be generated.
     */
    public void setGenerateParentSample(boolean generateParent) {
        setProperty(new BooleanProperty(GENERATE_PARENT_SAMPLE, generateParent));
    }

    /**
     * @return {@code true} if a parent sample will be generated
     */
    public boolean isGenerateParentSample() {
        return getPropertyAsBoolean(GENERATE_PARENT_SAMPLE);
    }

    /**
     * @see org.apache.jmeter.control.Controller#next()
     */
    @Override
    public Sampler next(){
        if (isGenerateParentSample()){
            return nextWithTransactionSampler();
        }
        return nextWithoutTransactionSampler();
    }

///////////////// Transaction Controller - parent ////////////////

    private Sampler nextWithTransactionSampler() {
        // Check if transaction is done
        if(transactionSampler != null && transactionSampler.isTransactionDone()) {
            if (log.isDebugEnabled()) {
                log.debug("End of transaction {}", getName());
            }
            // This transaction is done
            transactionSampler = null;
            return null;
        }

        // Check if it is the start of a new transaction
        if (isFirst()) // must be the start of the subtree
        {
            if (log.isDebugEnabled()) {
                log.debug("Start of transaction {}", getName());
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

    @Override
    protected Sampler nextIsAController(Controller controller) throws NextIsNullException {
        if (!isGenerateParentSample()) {
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

    private Sampler nextWithoutTransactionSampler() {
        if (isFirst()) // must be the start of the subtree
        {
            calls = 0;
            noFailingSamples = 0;
            res = new SampleResult();
            res.setSampleLabel(getName());
            // Assume success
            res.setSuccessful(true);
            res.sampleStart();
            prevEndTime = res.getStartTime();//???
            pauseTime = 0;
        }
        boolean isLast = current==super.subControllersAndSamplers.size();
        Sampler returnValue = super.next();
        if (returnValue == null && isLast) // Must be the end of the controller
        {
            if (res != null) {
                // See BUG 55816
                if (!isIncludeTimers()) {
                    long processingTimeOfLastChild = res.currentTimeInMillis() - prevEndTime;
                    pauseTime += processingTimeOfLastChild;
                }
                res.setIdleTime(pauseTime+res.getIdleTime());
                res.sampleEnd();
                res.setResponseMessage(TransactionController.NUMBER_OF_SAMPLES_IN_TRANSACTION_PREFIX + calls + ", number of failing samples : " + noFailingSamples);
                if(res.isSuccessful()) {
                    res.setResponseCodeOK();
                }
                notifyListeners();
            }
        }
        else {
            // We have sampled one of our children
            calls++;
        }

        return returnValue;
    }
    
    /**
     * @param res {@link SampleResult}
     * @return true if res is the ParentSampler transactions
     */
    public static boolean isFromTransactionController(SampleResult res) {
        return res.getResponseMessage() != null && 
                res.getResponseMessage().startsWith(
                        TransactionController.NUMBER_OF_SAMPLES_IN_TRANSACTION_PREFIX);
    }

    /**
     * @see org.apache.jmeter.control.GenericController#triggerEndOfLoop()
     */
    @Override
    public void triggerEndOfLoop() {
        if(!isGenerateParentSample()) {
            if (res != null) {
                res.setIdleTime(pauseTime + res.getIdleTime());
                res.sampleEnd();
                res.setSuccessful(TRUE.equals(JMeterContextService.getContext().getVariables().get(JMeterThread.LAST_SAMPLE_OK)));
                res.setResponseMessage(TransactionController.NUMBER_OF_SAMPLES_IN_TRANSACTION_PREFIX + calls + ", number of failing samples : " + noFailingSamples);
                notifyListeners();
            }
        } else {
            Sampler subSampler = transactionSampler.getSubSampler();
            // See Bug 56811
            // triggerEndOfLoop is called when error occurs to end Main Loop
            // in this case normal workflow doesn't happen, so we need 
            // to notify the childs of TransactionController and 
            // update them with SubSamplerResult
            if(subSampler instanceof TransactionSampler) {
                TransactionSampler tc = (TransactionSampler) subSampler;
                transactionSampler.addSubSamplerResult(tc.getTransactionResult());
            }
            transactionSampler.setTransactionDone();
            // This transaction is done
            transactionSampler = null;
        }
        super.triggerEndOfLoop();
    }

    /**
     * Create additional SampleEvent in NON Parent Mode
     */
    protected void notifyListeners() {
        // TODO could these be done earlier (or just once?)
        JMeterContext threadContext = getThreadContext();
        JMeterVariables threadVars = threadContext.getVariables();
        SamplePackage pack = (SamplePackage) threadVars.getObject(JMeterThread.PACKAGE_OBJECT);
        if (pack == null) {
            // If child of TransactionController is a ThroughputController and TPC does
            // not sample its children, then we will have this
            // TODO Should this be at warn level ?
            log.warn("Could not fetch SamplePackage");
        } else {
            SampleEvent event = new SampleEvent(res, threadContext.getThreadGroup().getName(),threadVars, true);
            // We must set res to null now, before sending the event for the transaction,
            // so that we can ignore that event in our sampleOccured method
            res = null;
            lnf.notifyListeners(event, pack.getSampleListeners());
        }
    }

    @Override
    public void sampleOccurred(SampleEvent se) {
        if (!isGenerateParentSample()) {
            // Check if we are still sampling our children
            if(res != null && !se.isTransactionSampleEvent()) {
                SampleResult sampleResult = se.getResult();
                res.setThreadName(sampleResult.getThreadName());
                res.setBytes(res.getBytesAsLong() + sampleResult.getBytesAsLong());
                res.setSentBytes(res.getSentBytes() + sampleResult.getSentBytes());
                if (!isIncludeTimers()) {// Accumulate waiting time for later
                    pauseTime += sampleResult.getEndTime() - sampleResult.getTime() - prevEndTime;
                    prevEndTime = sampleResult.getEndTime();
                }
                if(!sampleResult.isSuccessful()) {
                    res.setSuccessful(false);
                    noFailingSamples++;
                }
                res.setAllThreads(sampleResult.getAllThreads());
                res.setGroupThreads(sampleResult.getGroupThreads());
                res.setLatency(res.getLatency() + sampleResult.getLatency());
                res.setConnectTime(res.getConnectTime() + sampleResult.getConnectTime());
            }
        }
    }

    @Override
    public void sampleStarted(SampleEvent e) {
    }

    @Override
    public void sampleStopped(SampleEvent e) {
    }

    /**
     * Whether to include timers and pre/post processor time in overall sample.
     * @param includeTimers Flag whether timers and pre/post processor should be included in overall sample
     */
    public void setIncludeTimers(boolean includeTimers) {
        setProperty(INCLUDE_TIMERS, includeTimers, DEFAULT_VALUE_FOR_INCLUDE_TIMERS);
    }

    /**
     * Whether to include timer and pre/post processor time in overall sample.
     *
     * @return boolean (defaults to true for backwards compatibility)
     */
    public boolean isIncludeTimers() {
        return getPropertyAsBoolean(INCLUDE_TIMERS, DEFAULT_VALUE_FOR_INCLUDE_TIMERS);
    }
}
