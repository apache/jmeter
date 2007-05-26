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

import org.apache.jmeter.samplers.Sampler;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * Transaction Controller to measure transaction times
 * 
 */
public class TransactionController extends GenericController implements Controller, Serializable {
	private static final Logger log = LoggingManager.getLoggerForClass();

    transient private TransactionSampler transactionSampler;

	/**
	 * @see org.apache.jmeter.control.Controller#next()
	 */
	public Sampler next() {
        // Check if transaction is done
        if(transactionSampler != null && transactionSampler.isTransactionDone()) {
            log.debug("End of transaction");
            // This transaction is done
            transactionSampler = null;
            return null;
        }
        
        // Check if it is the start of a new transaction
		if (isFirst()) // must be the start of the subtree
		{
		    log.debug("Start of transaction");
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
}
