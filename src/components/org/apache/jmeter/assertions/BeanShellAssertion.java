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

import java.io.IOException;
import java.io.Serializable;

import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.BeanShellInterpreter;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.util.JMeterException;
import org.apache.jorphan.util.JOrphanUtils;
import org.apache.log.Logger;

/**
 * A sampler which understands BeanShell
 * 
 * @version $Revision$ Updated on: $Date$
 */
public class BeanShellAssertion extends AbstractTestElement implements Serializable, Assertion {
	private static final Logger log = LoggingManager.getLoggerForClass();

	public static final String FILENAME = "BeanShellAssertion.filename"; //$NON-NLS-1$

	public static final String SCRIPT = "BeanShellAssertion.query"; //$NON-NLS-1$

	public static final String PARAMETERS = "BeanShellAssertion.parameters"; //$NON-NLS-1$

	// Not serialised - recreated as needed
	transient private BeanShellInterpreter bshInterpreter = null;

	// can be specified in jmeter.properties
	public static final String INIT_FILE = "beanshell.assertion.init"; //$NON-NLS-1$

	public BeanShellAssertion() {
		init();
	}

	// Ensure deserialisation works in server
	private Object readResolve(){
		init();
		return this;
	}

	private void init(){
		try {
			bshInterpreter = new BeanShellInterpreter();
			String init = JMeterUtils.getProperty(INIT_FILE);
			try {
				bshInterpreter.init(init, log);
			} catch (IOException e) {
				log.warn("Could not initialise interpreter", e);
			} catch (JMeterException e) {
				log.warn("Could not initialise interpreter", e);
			}
		} catch (ClassNotFoundException e) {
			log.error("Could not establish BeanShellInterpreter: " + e);
		}		
	}
	public String getScript() {
		return getPropertyAsString(SCRIPT);
	}

	public String getFilename() {
		return getPropertyAsString(FILENAME);
	}

	public String getParameters() {
		return getPropertyAsString(PARAMETERS);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jmeter.assertions.Assertion#getResult(org.apache.jmeter.samplers.SampleResult)
	 */
	public AssertionResult getResult(SampleResult response) {
		AssertionResult result = new AssertionResult(getName());

		if (bshInterpreter == null) {
			result.setFailure(true);
			result.setError(true);
			result.setFailureMessage("BeanShell Interpreter not found");
			return result;
		}
		try {
			String request = getScript();
			String fileName = getFilename();

			bshInterpreter.set("FileName", getFilename());//$NON-NLS-1$
			// Set params as a single line
			bshInterpreter.set("Parameters", getParameters()); // $NON-NLS-1$
			bshInterpreter.set("bsh.args",//$NON-NLS-1$
					JOrphanUtils.split(getParameters(), " "));//$NON-NLS-1$

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

			// Add variables for access to context and variables
			JMeterContext jmctx = JMeterContextService.getContext();
			JMeterVariables vars = jmctx.getVariables();
			bshInterpreter.set("ctx", jmctx);//$NON-NLS-1$
			bshInterpreter.set("vars", vars);//$NON-NLS-1$

			// Object bshOut;

			if (fileName.length() == 0) {
				// bshOut =
				bshInterpreter.eval(request);
			} else {
				// bshOut =
				bshInterpreter.source(fileName);
			}

			result.setFailureMessage(bshInterpreter.get("FailureMessage").toString());//$NON-NLS-1$
			result.setFailure(Boolean.valueOf(bshInterpreter.get("Failure") //$NON-NLS-1$
					.toString()).booleanValue());
			result.setError(false);
		}
		/*
		 * To avoid class loading problems when the BSH jar is missing, we don't
		 * try to catch this error separately catch (bsh.EvalError ex) {
		 * log.debug("",ex); result.setError(true);
		 * result.setFailureMessage(ex.toString()); }
		 */
		// but we do trap this error to make tests work better
		catch (NoClassDefFoundError ex) {
			log.error("BeanShell Jar missing? " + ex.toString());
			result.setError(true);
			result.setFailureMessage("BeanShell Jar missing? " + ex.toString());
			response.setStopThread(true); // No point continuing
		} catch (Exception ex) // Mainly for bsh.EvalError
		{
			result.setError(true);
			result.setFailureMessage(ex.toString());
			log.warn(ex.toString());
		}

		return result;
	}
}