package org.apache.jmeter.functions;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.log.Hierarchy;
import org.apache.log.Logger;


/**
 * @author default
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
/*
 * It appears that JMeter instantiates a new copy of each function for every reference in a Sampler
 * or elsewhere.
 */

public class StringFromFile extends AbstractFunction implements Serializable
{
	transient private static Logger log = Hierarchy.getDefaultHierarchy().getLoggerFor(
			"jmeter.elements");

	private static final List desc = new LinkedList();
	private static final String KEY = "_StringFromFile"; // Function name (only 1 _)

	static
	{
		desc.add(JMeterUtils.getResString("string_from_file_file_name"));
		desc.add(JMeterUtils.getResString("function_name_param"));
	}
	
	private String myValue = "<please supply a file>"; // Default value
	private String myName  = "StringFromFile_"; // Name to store value in
	private Object[] values;
	private BufferedReader myBread; // Buffered reader
	private boolean reopenFile=true; // Set from parameter list one day ...
	
	public StringFromFile()
	{
	}
	
	public Object clone()
	{
		StringFromFile newReader = new StringFromFile();
		return newReader;
	}

	private void openFile( String fileName ){
	    try {
			FileReader fis = new FileReader(fileName);
			myBread = new BufferedReader(fis);
	    } catch (Exception e) {
			log.error("openFile",e);
	    }
	}

	/**
	 * @see org.apache.jmeter.functions.Function#execute(SampleResult, Sampler)
	 */
	public synchronized String execute(SampleResult previousResult, Sampler currentSampler)
		throws InvalidVariableException {
		
		JMeterVariables vars = getVariables();

		String fileName = ((CompoundVariable)values[0]).execute();
		myName = ((CompoundVariable)values[1]).execute();

		openFile(fileName);

		myValue="**ERR**";
		if (null != myBread) {// Did we open the file?
		  try {
		    String line = myBread.readLine();
		    if (line == null && reopenFile) { // EOF, re-open file
				myBread.close();
				openFile(fileName);
				line = myBread.readLine();
		    }
		    myValue = line;
		  } catch (Exception e) {
		    log.error("Token",e);
		  }
		}
		
		vars.put(myName,myValue);
		return myValue;

	}

	/**
	 * @see org.apache.jmeter.functions.Function#setParameters(String)
	 */
	public void setParameters(Collection parameters)
		throws InvalidVariableException {
			
		values = parameters.toArray();
		
		if ( values.length > 2 )
			throw new InvalidVariableException();
			
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
