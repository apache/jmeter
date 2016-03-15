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

package org.apache.jmeter.assertions;

import java.io.Serializable;

import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.ThreadListener;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import com.google.gson.Gson;

/**
 * Checks if the result is a well-formed XML content using jdom
 * 
 */
public class JSONAssertion extends AbstractTestElement implements Serializable, Assertion, ThreadListener {
    private static final Logger log = LoggingManager.getLoggerForClass();

    private static final long serialVersionUID = 240L;

    /**
     * Returns the result of the Assertion. Here it checks wether the Sample
     * took to long to be considered successful. If so an AssertionResult
     * containing a FailureMessage will be returned. Otherwise the returned
     * AssertionResult will reflect the success of the Sample.
     */
    @Override
    public AssertionResult getResult(SampleResult response) {
        // no error as default
        AssertionResult result = new AssertionResult(getName());
        String resultData = response.getResponseDataAsString();
       
        if (resultData.length()==0) {
            return result.setResultForNull();
        }else{
        	Gson gson = new Gson();
        	if(!gson.toJsonTree(resultData).isJsonObject()){
        		log.debug("Cannot parse result content"); // may well happen
        		result.setFailure(true);
                result.setFailureMessage("ResultData is not Json");
        	}
        }
        return result;

    }

    @Override
    public void threadStarted() {
    }

    @Override
    public void threadFinished() {
    }
}
