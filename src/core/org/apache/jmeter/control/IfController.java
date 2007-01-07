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
import org.apache.jmeter.testelement.property.StringProperty;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

/*******************************************************************************
 * 
 * @author Cyrus Montakab created 2003/06/30
 * @version $Date$ $Revision$ 
 * This is a Conditional Controller; it will execute the set of statements
 * (samplers/controllers, etc) while the 'condition' is true. 
 * In a programming world - this is equivalant of : 
 * if (condition) {
 *          statements .... 
 *          } 
 * In JMeter you may have : Thread-Group (set to loop a number of times or indefinitely, 
 *    ... Samplers ... (e.g. Counter )
 *    ... Other Controllers .... 
 *    ... IfController ( condition set to something like - ${counter}<10) 
 *       ... statements to perform if condition is true 
 *       ... 
 *    ... Other Controllers /Samplers }
 * 
 ******************************************************************************/

public class IfController extends GenericController implements Serializable {

	private static final Logger logger = LoggingManager.getLoggerForClass();

	private final static String CONDITION = "IfController.condition";

	/**
	 * constructor
	 */
	public IfController() {
		super();
	}

	/**
	 * constructor
	 */
	public IfController(String condition) {
		super();
		this.setCondition(condition);
	}

	/**
	 * Condition Accessor - this is gonna be like ${count}<10
	 */
	public void setCondition(String condition) {
		setProperty(new StringProperty(CONDITION, condition));
	}

	/**
	 * Condition Accessor - this is gonna be like ${count}<10
	 */
	public String getCondition() {
		return getPropertyAsString(CONDITION);
	}

	/**
	 * evaluate the condition clause log error if bad condition
	 */
	static boolean evaluateCondition(String cond) {
		logger.debug("    getCondition() : [" + cond + "]");

		String resultStr = "";
		boolean result = false;

		// now evaluate the condition using JavaScript
		Context cx = Context.enter();
		try {
			Scriptable scope = cx.initStandardObjects(null);
			Object cxResultObject = cx.evaluateString(scope, cond
			/** * conditionString ** */
			, "<cmd>", 1, null);
			resultStr = Context.toString(cxResultObject);

			if (resultStr.equals("false")) {
				result = false;
			} else if (resultStr.equals("true")) {
				result = true;
			} else {
				throw new Exception(" BAD CONDITION :: " + cond);
			}

			logger.debug("    >> evaluate Condition -  [ " + cond + "] results is  [" + result + "]");
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			Context.exit();
		}

		return result;
	}

	/**
	 * This is overriding the parent method. IsDone indicates whether the
	 * termination condition is reached. I.e. if the condition evaluates to
	 * False - then isDone() returns TRUE
	 */
	public boolean isDone() {
		// boolean result = true;
		// try {
		// result = !evaluateCondition();
		// } catch (Exception e) {
		// logger.error(e.getMessage(), e);
		// }
		// setDone(true);
		// return result;
		// setDone(false);
		return false;
	}

	/**
	 * @see org.apache.jmeter.control.Controller#next() 'JMeterThread' iterates
	 *      thru the Controller by calling this method. IF a valid 'Sampler' is
	 *      returned, then it executes the sampler (calls sampler.sampler(xxx)
	 *      method) . So here we make sure that the samplers belonging to this
	 *      Controller do not get called - if isDone is true - if its the first
	 *      time this is run. The first time is special cause it is called prior
	 *      the iteration even starts !
	 */
	public Sampler next() {
		boolean result = evaluateCondition(getCondition());
		if (result) {
			return super.next();
		}
		try {
			return nextIsNull();
		} catch (NextIsNullException e1) {
			return null;
		}
	}
}