package org.apache.jmeter.functions;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

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
	
	private String varName;
	private String script;
	
	static {
		desc.add("JavaScript expression to evaluate");
		desc.add(JMeterUtils.getResString("function_name_param"));
	}


	public JavaScript() {

	}

	public Object clone() 	{
		JavaScript newJavaScript = new JavaScript();
		return newJavaScript;
	}

	/**
	 * @see org.apache.jmeter.functions.Function#execute(org.apache.jmeter.samplers.SampleResult, org.apache.jmeter.samplers.Sampler)
	 */
	public synchronized String execute(SampleResult previousResult, Sampler currentSampler)
			throws InvalidVariableException {

		JMeterVariables vars = getVariables();
		String resultStr = "";
		
		Context cx = Context.enter();
		try {

			Scriptable scope = cx.initStandardObjects(null);
			Object result = cx.evaluateString(scope, script, "<cmd>", 1, null);

			resultStr = Context.toString( result );
			vars.put( varName, resultStr );

		
		} catch ( JavaScriptException e ) {
			
		} finally {
			Context.exit();
		}
		

		return resultStr;

	}

	/**
	 * @see org.apache.jmeter.functions.Function#setParameters(java.lang.String)
	 */
	public void setParameters(String parameters)
			throws InvalidVariableException {
				
		Collection params = this.parseArguments(parameters);
		String[] values = (String[])params.toArray(new String[0]);
		
		if ( values.length > 1 ) {
			varName = values[values.length - 1];
		}
		
		try {
			script = values[0];
		} catch ( Exception e ) {
			throw new InvalidVariableException();
		}


	}


	/**
	 * @see org.apache.jmeter.functions.Function#getReferenceKey()
	 */
	public String getReferenceKey() {
		return KEY;
	}


	/**
	 * @see org.apache.jmeter.functions.Function#getArgumentDesc()
	 */
	public List getArgumentDesc() {
		return desc;
	}

}



