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

package org.apache.jmeter.functions;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.EcmaError;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.WrappedException;

public class JavaScript extends AbstractFunction implements Serializable {

	private static final List desc = new LinkedList();

	private static final String KEY = "__javaScript"; //$NON-NLS-1$

	private static Logger log = LoggingManager.getLoggerForClass();

	static {
		desc.add(JMeterUtils.getResString("javascript_expression"));//$NON-NLS-1$
		desc.add(JMeterUtils.getResString("function_name_param")); //$NON-NLS-1$
	}

	private Object[] values;

	public JavaScript() {
	}

	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jmeter.functions.Function#execute(SampleResult, Sampler)
	 */
	public synchronized String execute(SampleResult previousResult, Sampler currentSampler)
			throws InvalidVariableException {

		JMeterVariables vars = getVariables();

		String script = ((CompoundVariable) values[0]).execute();
		// Allow variable to be omitted
		String varName = values.length < 2 ? null : ((CompoundVariable) values[1]).execute();
		String resultStr = "";

		Context cx = Context.enter();
		try {

			Scriptable scope = cx.initStandardObjects(null);
			Object result = cx.evaluateString(scope, script, "<cmd>", 1, null); //$NON-NLS-1$

			resultStr = Context.toString(result);
			if (varName != null && vars != null) {// vars can be null if run from TestPlan
				vars.put(varName, resultStr);
            }

		} catch (WrappedException e) {
			log.error("Error processing Javascript", e);
			throw new InvalidVariableException();
		} catch (EcmaError e) {
			log.error("Error processing Javascript", e);
			throw new InvalidVariableException();
		} catch (JavaScriptException e) {
			log.error("Error processing Javascript", e);
			throw new InvalidVariableException();
		} finally {
			Context.exit();
		}

		return resultStr;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jmeter.functions.Function#setParameters(Collection)
	 */
	public void setParameters(Collection parameters) throws InvalidVariableException {

		values = parameters.toArray();

		if (values.length < 1 || values.length > 2) {
			throw new InvalidVariableException("Expecting 1 or 2 parameters, but found " + values.length);//$NON-NLS-1$
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jmeter.functions.Function#getReferenceKey()
	 */
	public String getReferenceKey() {
		return KEY;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jmeter.functions.Function#getArgumentDesc()
	 */
	public List getArgumentDesc() {
		return desc;
	}

}
