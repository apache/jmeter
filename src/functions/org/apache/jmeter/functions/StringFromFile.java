/*
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001,2003 The Apache Software Foundation.  All rights
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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.text.DecimalFormat;
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

/**
 * StringFromFile Function to read a String from a text file.
 * 
 * Parameters:
 *      - file name
 *      - variable name (optional - defaults to StringFromFile_)
 * 
 * Returns:
 *      - the next line from the file - or **ERR** if an error occurs
 *      - value is also saved in the variable for later re-use.
 * 
 * Ensure that different variable names are used for each call to the function
 * 
 * 
 * Notes:
 * - JMeter instantiates a copy of each function for every reference in a
 *   Sampler or elsewhere; each instance will open its own copy of the the file
 * - the file name is resolved at file (re-)open time
 * - the output variable name is resolved every time the function is invoked
 * 
 * @author sebb AT apache DOT org
 *
 * @version $Revision$ Updated on: $Date$
 */
public class StringFromFile extends AbstractFunction implements Serializable
{
	private static Logger log = LoggingManager.getLoggerForClass();

    private static final List desc = new LinkedList();
    private static final String KEY = "_StringFromFile";//$NON-NLS-1$
    // Function name (only 1 _)
    
	private static final String ERR_IND = "**ERR**";//$NON-NLS-1$
    
    static {
        desc.add(JMeterUtils.getResString("string_from_file_file_name"));//$NON-NLS-1$
        desc.add(JMeterUtils.getResString("function_name_param"));//$NON-NLS-1$
		desc.add(JMeterUtils.getResString("string_from_file_seq_start"));//$NON-NLS-1$
		desc.add(JMeterUtils.getResString("string_from_file_seq_final"));//$NON-NLS-1$
    }
	private static final int MIN_PARAM_COUNT = 1;
	private static final int PARAM_NAME = 2;
	private static final int PARAM_START = 3;
	private static final int PARAM_END = 4;
	private static final int MAX_PARAM_COUNT = 4;

    private String myValue = ERR_IND;
    private String myName = "StringFromFile_";//$NON-NLS-1$ - Name to store the value in
    private Object[] values;
	transient private BufferedReader myBread; // Buffered reader
	transient private FileReader fis; // keep this round to close it
    private boolean firstTime = false; // should we try to open the file?
    private boolean reopenFile = true; // Set from parameter list one day ...
    private String fileName; // needed for error messages

    public StringFromFile()
    {
		if (log.isDebugEnabled())
		{
			log.debug("++++++++ Construct "+this);
		}
    }

	protected void finalize() throws Throwable{
		if (log.isDebugEnabled())
		{
		    log.debug("-------- Finalize "+this);
		}
	}

    public Object clone()
    {
        StringFromFile newReader = new StringFromFile();
        if (log.isDebugEnabled())
        { // Skip expensive paramter creation ..
            log.debug(this +"::StringFromFile.clone()", new Throwable("debug"));//$NON-NLS-1$
        }

        return newReader;
    }
    
/*
 * Warning: the file will generally be left open at the end of a test run.
 * This is because functions don't have any way to find out when a test has
 * ended ... 
 */
    private void closeFile(){
    	String tn = Thread.currentThread().getName();
    	log.info(tn + " closing file " + fileName);//$NON-NLS-1$
    	try {
    		myBread.close();
			fis.close();
		} catch (IOException e) {
			log.error("closeFile() error: " + e.toString());//$NON-NLS-1$
		}
    }

    private int myStart = 0;
    private int myCurrent = 0;
	private int myEnd = 0;
	
    private void openFile()
    {
		String tn = Thread.currentThread().getName();
        fileName = ((CompoundVariable) values[0]).execute();

		if (values.length >= PARAM_START)
		{
			String tmp = ((CompoundVariable) values[PARAM_START-1]).execute();
			myStart = Integer.valueOf(tmp).intValue();
			// Have we use myCurrent yet?
			if (myCurrent == 0) myCurrent=myStart;
		}

		if (values.length >= PARAM_END)
		{
			String tmp = ((CompoundVariable) values[PARAM_END-1]).execute();
			myEnd = Integer.valueOf(tmp).intValue();
		}

		if (values.length >= PARAM_START)
		{
			log.info("Start ="+myStart+" Current = "+myCurrent+" End ="+myEnd);//$NON-NLS-1$
			if (values.length >= PARAM_END){
				if (myCurrent > myEnd){
					log.info("No more files to process, "+myCurrent+" > "+myEnd);//$NON-NLS-1$
					myBread=null;
					return;
				}
			}
			log.info("Using format "+fileName);
			DecimalFormat myFormatter = new DecimalFormat(fileName);
			fileName = myFormatter.format(myCurrent);
			myCurrent++;// for next time
        }

		log.info(tn + " opening file " + fileName);//$NON-NLS-1$
        try
        {
            fis = new FileReader(fileName);
            myBread = new BufferedReader(fis);
        }
        catch (Exception e)
        {
            log.error("openFile() error: " + e.toString());//$NON-NLS-1$
            myBread=null;
        }
    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.functions.Function#execute(SampleResult, Sampler)
     */
    public synchronized String execute(
        SampleResult previousResult,
        Sampler currentSampler)
        throws InvalidVariableException
    {

        JMeterVariables vars = getVariables();

        if (values.length >= PARAM_NAME)
        {
            myName = ((CompoundVariable) values[PARAM_NAME-1]).execute();
        }

        myValue = ERR_IND;
        
        /*
         * To avoid re-opening the file repeatedly after an error,
         * only try to open it in the first execute() call
         * (It may be re=opened at EOF, but that will cause at most
         * one failure.)
         */
        if (firstTime) {
        	openFile();
        	firstTime=false;
        }
        
        if (null != myBread)
        { // Did we open the file?
            try
            {
                String line = myBread.readLine();
                if (line == null && reopenFile)
                { // EOF, re-open file
                    log.info("Reached EOF on " + fileName);//$NON-NLS-1$
                    closeFile();
                    openFile();
                    if (myBread != null) {
						line = myBread.readLine();
                    } else {
                    	line = ERR_IND;
                    }
                }
                myValue = line;
            }
            catch (Exception e)
            {
				String tn = Thread.currentThread().getName();
                log.error(tn + " error reading file " + e.toString());//$NON-NLS-1$
            }
        } else { // File was not opened successfully
        	if (values.length >= PARAM_END){// Are we processing a file sequence?
        		log.info("Detected end of sequence.");
        		throw new RuntimeException("Stop Thread");//TODO there has to be a better way...
        	}
        }

        if (myName.length() > 0){
			vars.put(myName, myValue);
        }
        
        if (log.isDebugEnabled()){
            log.debug(this +"::StringFromFile.execute() name:" //$NON-NLS-1$ 
                 + myName + " value:" + myValue);//$NON-NLS-1$
        }
        
        return myValue;

    }

    /* (non-Javadoc)
     * Parameters:
     * - file name
     * - variable name (optional)
     * 
     * @see org.apache.jmeter.functions.Function#setParameters(Collection)
     */
    public void setParameters(Collection parameters)
        throws InvalidVariableException
    {

        log.debug(this +"::StringFromFile.setParameters()");//$NON-NLS-1$

        values = parameters.toArray();

        if ((values.length > MAX_PARAM_COUNT) || (values.length < MIN_PARAM_COUNT))
        {
            throw new InvalidVariableException("Wrong number of parameters");//$NON-NLS-1$
        }

		StringBuffer sb = new StringBuffer(40);
		sb.append("setParameters(");//$NON-NLS-1$
		for (int i = 0; i< values.length;i++){
			if (i > 0) sb.append(",");
			sb.append(((CompoundVariable) values[i]).getRawParameters());
		}
		sb.append(")");//$NON-NLS-1$
		log.info(sb.toString());
		
		
		//N.B. seteParameters is called before the test proper is started,
		//     and thus variables are not interpreted at this point
		// So defer the file open until later to allow variable file names to be used.
		firstTime = true;
    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.functions.Function#getReferenceKey()
     */
    public String getReferenceKey()
    {
        return KEY;
    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.functions.Function#getArgumentDesc()
     */
    public List getArgumentDesc()
    {
        return desc;
    }

}
