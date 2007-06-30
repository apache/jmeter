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


import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;

/**
 * Transaction Controller to measure transaction times
 * 
 */
public class TransactionSampler extends AbstractSampler {
  private boolean transactionDone = false;

    private TransactionController transactionController; 
    
	private Sampler subSampler;

	private SampleResult transactionSampleResult;

	private int calls = 0;

	private int noFailingSamples = 0;

	public TransactionSampler(){
		//log.warn("Constructor only intended for use in testing");
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
     * One cannot sample the TransactionSample directly.
     */
	public SampleResult sample(Entry e) {
        // It is the JMeterThread which knows how to sample a
        // real sampler
        return null;
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
        // The transaction fails if any sub sample fails
        if (!res.isSuccessful()) {
            transactionSampleResult.setSuccessful(false);
            noFailingSamples++;
        }
        // Add the sub result to the transaction result
        transactionSampleResult.addSubResult(res);
    }

	protected void setTransactionDone() {
		this.transactionDone = true;
		// Set the overall status for the transaction sample
		// TODO: improve, e.g. by adding counts to the SampleResult class
		transactionSampleResult.setResponseMessage("Number of samples in transaction : "
						+ calls + ", number of failing samples : "
						+ noFailingSamples);
		if (transactionSampleResult.isSuccessful()) {
			transactionSampleResult.setResponseCodeOK();
		}
	}

	protected void setSubSampler(Sampler subSampler) {
		this.subSampler = subSampler;
	}
}
