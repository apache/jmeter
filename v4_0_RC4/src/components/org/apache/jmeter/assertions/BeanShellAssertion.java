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

import org.apache.jmeter.gui.GUIMenuSortOrder;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.util.BeanShellInterpreter;
import org.apache.jmeter.util.BeanShellTestElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An Assertion which understands BeanShell
 *
 */
@GUIMenuSortOrder(Integer.MAX_VALUE)
public class BeanShellAssertion extends BeanShellTestElement implements Assertion {
    private static final Logger log = LoggerFactory.getLogger(BeanShellAssertion.class);

    private static final long serialVersionUID = 4;

    public static final String FILENAME = "BeanShellAssertion.filename"; //$NON-NLS-1$

    public static final String SCRIPT = "BeanShellAssertion.query"; //$NON-NLS-1$

    public static final String PARAMETERS = "BeanShellAssertion.parameters"; //$NON-NLS-1$

    public static final String RESET_INTERPRETER = "BeanShellAssertion.resetInterpreter"; //$NON-NLS-1$

    // can be specified in jmeter.properties
    public static final String INIT_FILE = "beanshell.assertion.init"; //$NON-NLS-1$

    @Override
    protected String getInitFileProperty() {
        return INIT_FILE;
    }

    @Override
    public String getScript() {
        return getPropertyAsString(SCRIPT);
    }

    @Override
    public String getFilename() {
        return getPropertyAsString(FILENAME);
    }

    @Override
    public String getParameters() {
        return getPropertyAsString(PARAMETERS);
    }

    @Override
    public boolean isResetInterpreter() {
        return getPropertyAsBoolean(RESET_INTERPRETER);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AssertionResult getResult(SampleResult response) {
        AssertionResult result = new AssertionResult(getName());

        final BeanShellInterpreter bshInterpreter = getBeanShellInterpreter();
        if (bshInterpreter == null) {
            result.setFailure(true);
            result.setError(true);
            result.setFailureMessage("BeanShell Interpreter not found");
            return result;
        }
        try {

            // Add SamplerData for consistency with BeanShell Sampler
            bshInterpreter.set("SampleResult", response); //$NON-NLS-1$
            bshInterpreter.set("Response", response); //$NON-NLS-1$
            bshInterpreter.set("ResponseData", response.getResponseData());//$NON-NLS-1$
            bshInterpreter.set("ResponseCode", response.getResponseCode());//$NON-NLS-1$
            bshInterpreter.set("ResponseMessage", response.getResponseMessage());//$NON-NLS-1$
            bshInterpreter.set("ResponseHeaders", response.getResponseHeaders());//$NON-NLS-1$
            bshInterpreter.set("RequestHeaders", response.getRequestHeaders());//$NON-NLS-1$
            bshInterpreter.set("SampleLabel", response.getSampleLabel());//$NON-NLS-1$
            bshInterpreter.set("SamplerData", response.getSamplerData());//$NON-NLS-1$
            bshInterpreter.set("Successful", response.isSuccessful());//$NON-NLS-1$

            // The following are used to set the Result details on return from
            // the script:
            bshInterpreter.set("FailureMessage", "");//$NON-NLS-1$ //$NON-NLS-2$
            bshInterpreter.set("Failure", false);//$NON-NLS-1$

            processFileOrScript(bshInterpreter);

            result.setFailureMessage(bshInterpreter.get("FailureMessage").toString());//$NON-NLS-1$
            result.setFailure(Boolean.parseBoolean(bshInterpreter.get("Failure") //$NON-NLS-1$
                    .toString()));
            result.setError(false);
        }
        catch (NoClassDefFoundError ex) { // NOSONAR explicitly trap this error to make tests work better
            log.error("BeanShell Jar missing? " + ex.toString());
            result.setError(true);
            result.setFailureMessage("BeanShell Jar missing? " + ex.toString());
            response.setStopThread(true); // No point continuing
        } catch (Exception ex) // Mainly for bsh.EvalError
        {
            result.setError(true);
            result.setFailureMessage(ex.toString());
            if (log.isWarnEnabled()) {
                log.warn(ex.toString());
            }
        }

        return result;
    }
}
