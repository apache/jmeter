/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 */

package org.apache.jmeter.assertions;

import org.apache.bsf.BSFException;
import org.apache.bsf.BSFManager;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.util.BSFTestElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BSFAssertion extends BSFTestElement implements Cloneable, Assertion, TestBean
{
    private static final Logger log = LoggerFactory.getLogger(BSFAssertion.class);

    private static final long serialVersionUID = 235L;

    @Override
    public AssertionResult getResult(SampleResult response) {
        AssertionResult result = new AssertionResult(getName());
        BSFManager mgr =null;
        try {
            mgr = getManager();
            mgr.declareBean("SampleResult", response, SampleResult.class);
            mgr.declareBean("AssertionResult", result, AssertionResult.class);
            processFileOrScript(mgr);
            result.setError(false);
        } catch (BSFException e) {
            if (log.isWarnEnabled()) {
                log.warn("Problem in BSF script {}", e.toString());
            }
            result.setFailure(true);
            result.setError(true);
            result.setFailureMessage(e.toString());
        } finally {
            if(mgr != null) {
                mgr.terminate();
            }
        }
        return result;
    }
    
    @Override
    public Object clone() {
        return super.clone();
    }
}
