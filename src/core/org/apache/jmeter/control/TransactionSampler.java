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

/*
 *  N.B. Although this is a type of sampler, it is only used by the transaction controller,
 *  and so is in the control package
*/
package org.apache.jmeter.control;


import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testelement.TestElement;

/**
 * Transaction Sampler class to measure transaction times
 * (not exposed as a GUI class, as it is only used internally by TransactionController in Generate Parent sample mode)
 */
public class TransactionSampler extends AbstractSampler {
    private static final long serialVersionUID = 240L;

    private static final Set<String> APPLIABLE_CONFIG_CLASSES = new HashSet<>(
            Arrays.asList("org.apache.jmeter.config.gui.SimpleConfigGui"));

    private boolean transactionDone = false;

    private TransactionController transactionController;

    private Sampler subSampler;

    private SampleResult transactionSampleResult;

    private int calls = 0;

    private int noFailingSamples = 0;

    private long totalTime = 0;

    /**
     * @deprecated only for use by test code
     */
    @Deprecated
    public TransactionSampler(){
        super();
    }

    public TransactionSampler(TransactionController controller, String name) {
        transactionController = controller;
        setName(name); // ensure name is available for debugging
        transactionSampleResult = new SampleResult();
        transactionSampleResult.setSampleLabel(name);
        // Assume success
        transactionSampleResult.setSuccessful(true);
        transactionSampleResult.sampleStart();
    }

    /**
     * One cannot sample the TransactionSampler directly.
     */
    @Override
    public SampleResult sample(Entry e) {
        throw new RuntimeException("Cannot sample TransactionSampler directly");
        // It is the JMeterThread which knows how to sample a real sampler
    }

    public Sampler getSubSampler() {
        return subSampler;
    }

    public SampleResult getTransactionResult() {
        return transactionSampleResult;
    }

    public TransactionController getTransactionController() {
        return transactionController;
    }

    public boolean isTransactionDone() {
        return transactionDone;
    }

    public void addSubSamplerResult(SampleResult res) {
        // Another subsample for the transaction
        calls++;
        
        // Set Response code of transaction
        if (noFailingSamples == 0) {
            transactionSampleResult.setResponseCode(res.getResponseCode());
        }

        // The transaction fails if any sub sample fails
        if (!res.isSuccessful()) {
            transactionSampleResult.setSuccessful(false);
            noFailingSamples++;
        }
        // Add the sub result to the transaction result
        transactionSampleResult.addSubResult(res, false);
        // Add current time to total for later use (exclude pause time)
        totalTime += res.getTime();
    }

    protected void setTransactionDone() {
        this.transactionDone = true;
        // Set the overall status for the transaction sample
        // TODO: improve, e.g. by adding counts to the SampleResult class
        transactionSampleResult.setResponseMessage(TransactionController.NUMBER_OF_SAMPLES_IN_TRANSACTION_PREFIX
                        + calls + ", number of failing samples : "
                        + noFailingSamples);
        if (transactionSampleResult.isSuccessful()) {
            transactionSampleResult.setResponseCodeOK();
        }
        // Bug 50080 (not include pause time when generate parent)
        if (!transactionController.isIncludeTimers()) {
            long end = transactionSampleResult.currentTimeInMillis();
            transactionSampleResult.setIdleTime(end
                    - transactionSampleResult.getStartTime() - totalTime);
            transactionSampleResult.setEndTime(end);
        }
    }

    protected void setSubSampler(Sampler subSampler) {
        this.subSampler = subSampler;
    }

    /**
     * @see org.apache.jmeter.samplers.AbstractSampler#applies(org.apache.jmeter.config.ConfigTestElement)
     */
    @Override
    public boolean applies(ConfigTestElement configElement) {
        String guiClass = configElement.getProperty(TestElement.GUI_CLASS).getStringValue();
        return APPLIABLE_CONFIG_CLASSES.contains(guiClass);
    }
}
