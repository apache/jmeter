/*
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 * if any, must include the following acknowledgment:
 * "This product includes software developed by the
 * Apache Software Foundation (http://www.apache.org/)."
 * Alternately, this acknowledgment may appear in the software itself,
 * if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 * "Apache JMeter" must not be used to endorse or promote products
 * derived from this software without prior written permission. For
 * written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 * "Apache JMeter", nor may "Apache" appear in their name, without
 * prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
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
import org.mozilla.javascript.Context;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.Scriptable;



public class JavaScript extends AbstractFunction implements Serializable {

	private static final List desc = new LinkedList();
	private static final String KEY = "__javaScript";
			
	static {
		desc.add("JavaScript expression to evaluate");
		desc.add(JMeterUtils.getResString("function_name_param"));
	}

	private Object[] values;


	public JavaScript() {}

	public Object clone() 	{
		JavaScript newJavaScript = new JavaScript();
		return newJavaScript;
	}

    /* (non-Javadoc)
	 * @see org.apache.jmeter.functions.Function#execute(SampleResult, Sampler)
	 */
	public synchronized String execute(SampleResult previousResult, Sampler currentSampler)
			throws InvalidVariableException {

		JMeterVariables vars = getVariables();
		
		String script = ((CompoundVariable)values[0]).execute();
		String varName = ((CompoundVariable)values[values.length - 1]).execute();
		String resultStr = "";
		
		Context cx = Context.enter();
		try {

			Scriptable scope = cx.initStandardObjects(null);
			Object result = cx.evaluateString(scope, script, "<cmd>", 1, null);

			resultStr = Context.toString( result );
			vars.put( varName, resultStr );

		} catch ( JavaScriptException e ) {
			throw new InvalidVariableException();			
		} finally {
			Context.exit();
		}

		return resultStr;

	}


    /* (non-Javadoc)
	 * @see org.apache.jmeter.functions.Function#setParameters(Collection)
	 */
	public void setParameters(Collection parameters)
			throws InvalidVariableException {
		
		values = parameters.toArray();
		
		if ( values.length < 2 ) {
			throw new InvalidVariableException();
		}

	}


    /* (non-Javadoc)
	 * @see org.apache.jmeter.functions.Function#getReferenceKey()
	 */
	public String getReferenceKey() {
		return KEY;
	}


    /* (non-Javadoc)
	 * @see org.apache.jmeter.functions.Function#getArgumentDesc()
	 */
	public List getArgumentDesc() {
		return desc;
	}

}
