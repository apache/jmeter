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
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * The function represented by this class allows data to be read from CSV
 * files.  Syntax is similar to StringFromFile function.  The function allows
 * the test to line-thru the data in the CSV file - one line per each test.
 * E.g. inserting the following in the test scripts :
 * 
 *   ${_CSVRead(c:/BOF/abcd.csv,0)}       // read (first) line of
'c:/BOF/abcd.csv' , return the 1st column ( represented by the '0'),
 *   ${_CSVRead(c:/BOF/abcd.csv,1)}       // read (first) line of
'c:/BOF/abcd.csv' , return the 2nd column ( represented by the '1'),
 *   ${_CSVRead(c:/BOF/abcd.csv,next())}  // Go to next line of
'c:/BOF/abcd.csv'
 *
 * NOTE: A single instance of each different file is opened and used for all threads.
 * 
 * To open the same file twice, use the alias function:
 *  __CSVRead(abc.csv,*ONE);
 *  __CSVRead(abc.csv,*TWO);
 * 
 *  __CSVRead(*ONE,1); etc
 * 
 *
 * @author Cyrus M.
 * @author sebb AT apache DOT org (multi-file version)
 * 
 * @version $Revision$ Last Updated: $Date$
 */
public class CSVRead extends AbstractFunction implements Serializable
{
    transient private static final Logger log = LoggingManager.getLoggerForClass();

    private static final String KEY = "__CSVRead"; // Function name

    private static final List desc = new LinkedList();

    
    private Object[] values; // Parameter list
    
    static {
        desc.add(JMeterUtils.getResString("csvread_file_file_name"));
        desc.add(JMeterUtils.getResString("column_number"));
    }

    public CSVRead()
    {
    }

    public Object clone()
    {
        CSVRead newReader = new CSVRead();
        return newReader;
    }

    /**
     * @see org.apache.jmeter.functions.Function#execute(SampleResult, Sampler)
     */
    public synchronized String execute(
        SampleResult previousResult,
        Sampler currentSampler)
        throws InvalidVariableException
    {
    	String myValue = "";

        String fileName =
                ((org.apache.jmeter.engine.util.CompoundVariable) values[0])
                    .execute();
        String columnOrNext =
                ((org.apache.jmeter.engine.util.CompoundVariable) values[1])
                    .execute();

		log.debug("execute (" + fileName + " , " + columnOrNext + ")   ");

        // Process __CSVRead(filename,*ALIAS)
        if (columnOrNext.startsWith("*"))
        {
            FileWrapper.open(fileName,columnOrNext);
            /*
             * All done, so return
             */
            return "";
        }  
            
        // if argument is 'next' - go to the next line
        if (columnOrNext.equals("next()") || columnOrNext.equals("next"))
        {
        	FileWrapper.endRow(fileName);
        
            /*
             * All done now ,so return the empty string - this allows the caller to
             * append __CSVRead(file,next) to the last instance of __CSVRead(file,col)
             * 
             * N.B. It is important not to read any further lines at this point, otherwise
             * the wrong line can be retrieved when using multiple threads. 
             */
             return "";
        }

        try
        {
            int columnIndex = Integer.parseInt(columnOrNext); // what column is wanted?
            myValue = FileWrapper.getColumn(fileName,columnIndex);
        }
        catch (NumberFormatException e)
        {
            log.warn("Column number error: " + columnOrNext + " "+ e.toString());
        }
		catch (IndexOutOfBoundsException e)
		{
			log.warn("Invalid column number: " + columnOrNext + " "+ e.toString());
		}

        log.debug("execute value: "+myValue);
        
        return myValue;
    }

    /**
     * @see org.apache.jmeter.functions.Function#getArgumentDesc()
     */
    public List getArgumentDesc()
    {
        return desc;
    }

    /**
     * @see org.apache.jmeter.functions.Function#getReferenceKey()
     */
    public String getReferenceKey()
    {
        return KEY;
    }
    
    /**
     * @see org.apache.jmeter.functions.Function#setParameters(Collection)
     */
    public void setParameters(Collection parameters)
        throws InvalidVariableException
    {
   		log.debug("setParameter - Collection.size=" + parameters.size());

        values = parameters.toArray();

		if (log.isDebugEnabled()){
			for (int i=0;i <parameters.size();i++){
				log.debug("i:"+((CompoundVariable)values[i]).execute());
			}
		}

        if (values.length != 2)
        {
            throw new InvalidVariableException("Wrong number of parameters; 2 != "+values.length);
        }
        
        /*
         * Need to reset the containers for repeated runs; about the only way for 
         * functions to detect that a run is starting seems to be the setParameters()
         * call.
        */
		FileWrapper.clearAll();//TODO only clear the relevant entry - if possible...

    }
}