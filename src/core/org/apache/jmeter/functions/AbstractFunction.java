package org.apache.jmeter.functions;

import java.net.URLDecoder;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;

/**
 * @author mstover
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 */
public abstract class AbstractFunction implements Function {
	private Map threadVariables = new HashMap();

	/**
	 * @see Function#execute(SampleResult, Sampler)
	 */
	abstract public String execute(SampleResult previousResult, Sampler currentSampler) 
			throws InvalidVariableException;
			
	public String execute() throws InvalidVariableException
	{
		JMeterContext context = JMeterContextService.getContext();
		setJMeterVariables( context.getVariables() );
		SampleResult previousResult = context.getPreviousResult();
		Sampler currentSampler = context.getCurrentSampler();
		return execute( previousResult, currentSampler );
	}


	/**
	 * @see Function#setParameters(String)
	 */
//	abstract public void setParameters(String parameters) throws InvalidVariableException;
	
	/**
	 * @see Function#setParameters(Collection)
	 */
	abstract public void setParameters(Collection parameters) throws InvalidVariableException;

	/**
	 * @see Function#getReferenceKey()
	 */
	abstract public String getReferenceKey();	
	
	/**
	 * Provides a convenient way to parse the given argument string into a collection of
	 * individual arguments.  Takes care of splitting the string based on commas, generates
	 * blank strings for values between adjacent commas, and decodes the string using URLDecoder.
	 * 
	 * @deprecated
	 */
	protected Collection parseArguments(String params)
	{
		StringTokenizer tk = new StringTokenizer(params,",",true);
		List arguments = new LinkedList();
		String previous = "";
		while(tk.hasMoreTokens())
		{
			String arg = tk.nextToken();
			
			if(arg.equals(",") && previous.equals(",") )
			{
				arguments.add( "" );
			}
			else if(!arg.equals(","))
			{
				arguments.add( URLDecoder.decode(arg) );
			}
			previous = arg;
		}	
		return arguments;
	}

	/**
	 * Provides a convenient way to parse the given argument string into a collection of
	 * individual arguments.  Takes care of splitting the string based on commas, generates
	 * blank strings for values between adjacent commas, and decodes the string using URLDecoder.
	 */
/*	protected Collection parseArguments2(String params)
	{
		StringTokenizer tk = new StringTokenizer(params,",",true);
		List arguments = new LinkedList();
		String previous = "";
		while(tk.hasMoreTokens())
		{
			String arg = tk.nextToken();
			
			if(arg.equals(",") && ( previous.equals(",") || previous.length() == 0 ))
			{
				arguments.add( new CompoundVariable() );
			}
			else if(!arg.equals(","))
			{
				try 
				{
					CompoundVariable compoundArg = new CompoundVariable();
					compoundArg.setParameters(URLDecoder.decode(arg));
					arguments.add( compoundArg );
				}
				catch ( InvalidVariableException e ) { }
			}
			previous = arg;
		}
		
		if ( previous.equals(",") ) 
		{
			arguments.add(new CompoundVariable());
		}
		
		return arguments;
	}*/
	
	protected JMeterVariables getVariables()
	{
		return (JMeterVariables)threadVariables.get(Thread.currentThread().getName());
	}
	
	/**
	 * @see org.apache.jmeter.functions.Function#setJMeterVariables(JMeterVariables)
	 */
	public void setJMeterVariables(JMeterVariables jmv) {
		threadVariables.put(jmv.getThreadName(),jmv);
	}

}
