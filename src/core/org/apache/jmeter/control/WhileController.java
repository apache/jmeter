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
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.StringProperty;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterThread;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

// @see TestWhileController for unit tests

/**
 * @version $Revision$
 */
public class WhileController extends GenericController implements Serializable {
	private static Logger log = LoggingManager.getLoggerForClass();

	private final static String CONDITION = "WhileController.condition"; // $NON-NLS-1$

	static boolean testMode = false; // To make testing easier

	static boolean testModeResult = false; // dummy sample result

	public WhileController() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jmeter.control.Controller#isDone()
	 */
	public boolean isDone() {
		if (getSubControllers().size() == 0) // Nothing left to run
		{
			return true;
		}
		return false;// Never want to remove the controller from the tree
	}

	/*
	 * Evaluate the condition, which can be: blank or LAST = was the last
	 * sampler OK? otherwise, evaluate the condition to see if it is not "false"
	 * If blank, only evaluate at the end of the loop
	 * 
	 * Must only be called at start and end of loop
	 * 
	 * @param loopEnd - are we at loop end? @return true means OK to continue
	 */
	private boolean endOfLoop(boolean loopEnd) {
		String cnd = getCondition();
		log.debug("Condition string:" + cnd);
		boolean res;
		// If blank, only check previous sample when at end of loop
		if ((loopEnd && cnd.length() == 0) || "LAST".equalsIgnoreCase(cnd)) {// $NON-NLS-1$
			if (testMode) {
				res = !testModeResult;
			} else {
				JMeterVariables threadVars = JMeterContextService.getContext().getVariables();
				// Use !false rather than true, so that null is treated as true
				res = "false".equalsIgnoreCase(threadVars.get(JMeterThread.LAST_SAMPLE_OK));// $NON-NLS-1$
			}
		} else {
			// cnd may be blank if next() called us
			res = "false".equalsIgnoreCase(cnd);// $NON-NLS-1$
		}
		log.debug("Condition value: " + res);
		return res;
	}

	/*
	 * (non-Javadoc) Only called at End of Loop
	 * 
	 * @see org.apache.jmeter.control.GenericController#nextIsNull()
	 */
	protected Sampler nextIsNull() throws NextIsNullException {
		reInitialize();
		if (!endOfLoop(true)) {
			return super.next();
		}
		setDone(true);
		return null;
	}

	/*
	 * This skips controller entirely if the condition is false
	 * 
	 * TODO consider checking for previous sampler failure here - would need to
	 * distinguish this from previous failure *inside* loop
	 * 
	 */
	public Sampler next() {
		if (current != 0) { // in the middle of the loop
			return super.next();
		}
		// Must be start of loop
		if (!endOfLoop(false)) {
			return super.next(); // OK to continue
		}
		reInitialize(); // Don't even start the loop
		return null;
	}

	/**
	 * @param string
	 *            the condition to save
	 */
	public void setCondition(String string) {
		log.debug("setCondition(" + string + ")");
		setProperty(new StringProperty(CONDITION, string));
	}

	/**
	 * @return the condition
	 */
	public String getCondition() {
		String cnd;
        JMeterProperty prop=getProperty(CONDITION);
        prop.recoverRunningVersion(this);
		cnd = prop.getStringValue();
		log.debug("getCondition() => " + cnd);
		return cnd;
	}
}