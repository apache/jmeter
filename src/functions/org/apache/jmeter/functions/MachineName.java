package org.apache.jmeter.functions;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.JMeterUtils;



public class MachineName extends AbstractFunction implements Serializable {

	private static final List desc = new LinkedList();
	private static final String KEY = "__machineName";
	private boolean fullHostName = false;
	
	private String varName;
	
	static {
		desc.add("Use fully qualified host name: TRUE/FALSE (Default FALSE)");
		desc.add(JMeterUtils.getResString("function_name_param"));
	}


	public MachineName() {}

	public Object clone() 	{
		return new MachineName();
	}

	public synchronized String execute(SampleResult previousResult, Sampler currentSampler)
			throws InvalidVariableException {

		JMeterVariables vars = getVariables();
		String machineName = "";
		
		try {
			
			InetAddress Address = InetAddress.getLocalHost();
			
			if ( fullHostName ) {
				machineName = Address.getCanonicalHostName();
			} else {
				machineName = Address.getHostName();
			}
			
		} catch ( UnknownHostException e ) {
		}
		
		vars.put( varName, machineName );
		return machineName;

	}

	public void setParameters(String parameters)
			throws InvalidVariableException {
				
		Collection params = this.parseArguments(parameters);
		String[] values = (String[])params.toArray(new String[0]);
		
		if ( values.length > 0 ) {

			if ( values[0].toLowerCase().equals("true") )
				fullHostName = true;

			varName = values[values.length - 1];
	
		}

	}

	public String getReferenceKey() {
		return KEY;
	}

	public List getArgumentDesc() {
		return desc;
	}

}



