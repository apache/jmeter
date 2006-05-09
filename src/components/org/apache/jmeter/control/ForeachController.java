// $Header$
/*
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.testelement.property.BooleanProperty;
import org.apache.jmeter.testelement.property.StringProperty;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * @author Dolf Smits
 * @author Michael Stover
 * @author Thad Smith
 * @version $Revision$
 */
public class ForeachController extends GenericController implements Serializable {
	private static final Logger log = LoggingManager.getLoggerForClass();

	private final static String INPUTVAL = "ForeachController.inputVal";

	private final static String RETURNVAL = "ForeachController.returnVal";

	private final static String USE_SEPARATOR = "ForeachController.useSeparator";

	private int loopCount = 0;

	private static final String DEFAULT_SEPARATOR = "_";

	public ForeachController() {
	}

	public void setInputVal(String inputValue) {
		setProperty(new StringProperty(INPUTVAL, inputValue));
	}

	private String getInputVal() {
		getProperty(INPUTVAL).recoverRunningVersion(null);
		return getInputValString();
	}

	public String getInputValString() {
		return getPropertyAsString(INPUTVAL);
	}

	public void setReturnVal(String inputValue) {
		setProperty(new StringProperty(RETURNVAL, inputValue));
	}

	private String getReturnVal() {
		getProperty(RETURNVAL).recoverRunningVersion(null);
		return getReturnValString();
	}

	public String getReturnValString() {
		return getPropertyAsString(RETURNVAL);
	}

	private String getSeparator() {
		return getUseSeparator() ? DEFAULT_SEPARATOR : "";
	}

	public void setUseSeparator(boolean b) {
		setProperty(new BooleanProperty(USE_SEPARATOR, b));
	}

	public boolean getUseSeparator() {
		return getPropertyAsBoolean(USE_SEPARATOR, true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jmeter.control.Controller#isDone()
	 */
	public boolean isDone() {
		JMeterContext context = getThreadContext();
		String inputVariable = getInputVal() + getSeparator() + (loopCount + 1);
		if (context.getVariables().get(inputVariable) != null) {
			context.getVariables().put(getReturnVal(), context.getVariables().get(inputVariable));
			log.debug("ForEach resultstring isDone=" + context.getVariables().get(getReturnVal()));
			return false;
		}
		return super.isDone();
	}

	private boolean endOfArguments() {
		JMeterContext context = getThreadContext();
		String inputVariable = getInputVal() + getSeparator() + (loopCount + 1);
		if (context.getVariables().get(inputVariable) != null) {
			log.debug("ForEach resultstring eofArgs= false");
			return false;
		}
		log.debug("ForEach resultstring eofArgs= true");
		return true;
	}

	// Prevent entry if nothing to do
	public Sampler next() {
		if (emptyList()) {
			reInitialize();
			return null;
		}
		return super.next();
	}

	/**
	 * Check if there are any matching entries
	 * 
	 * @return whethere any entries in the list
	 */
	private boolean emptyList() {
		JMeterContext context = getThreadContext();
		String inputVariable = getInputVal() + getSeparator() + "1";
		if (context.getVariables().get(inputVariable) != null) {
			return false;
		}
		log.debug("No entries found - null first entry: " + inputVariable);
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jmeter.control.GenericController#nextIsNull()
	 */
	protected Sampler nextIsNull() throws NextIsNullException {
		reInitialize();
		if (endOfArguments()) {
			// setDone(true);
			resetLoopCount();
			return null;
		}
		return next();
	}

	protected void incrementLoopCount() {
		loopCount++;
	}

	protected void resetLoopCount() {
		loopCount = 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jmeter.control.GenericController#getIterCount()
	 */
	protected int getIterCount() {
		return loopCount + 1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jmeter.control.GenericController#reInitialize()
	 */
	protected void reInitialize() {
		setFirst(true);
		resetCurrent();
		incrementLoopCount();
		recoverRunningVersion();
	}
}