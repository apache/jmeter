package org.apache.jmeter.functions;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.io.*;
import java.util.*;

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
	static
	{
		desc.add(JMeterUtils.getResString("string_from_file_file_name"));
		desc.add(JMeterUtils.getResString("function_name_param"));
	}
	
	private static final String KEY = "_StringFromFile"; // Function name (only 1 _)
	private String myValue = "<please supply a file>"; // Default value
	private String myName  = "StringFromFile_"; // Name to store value in
	private String fileName;
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

	private void openFile(){
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
		myValue="**ERR**";
		if (null != myBread) {// Did we open the file?
		  try {
		    String line = myBread.readLine();
		    if (line == null && reopenFile) { // EOF, re-open file
			myBread.close();
			openFile();
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
	public void setParameters(String parameters)
		throws InvalidVariableException {
			Collection params = this.parseArguments(parameters);
			String[] values = (String[])params.toArray(new String[0]);
			fileName = values[0];
			if(values.length > 1)
			{
				myName = values[1];
			}
			openFile();
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
