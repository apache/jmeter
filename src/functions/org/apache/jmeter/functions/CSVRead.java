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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

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
 * NOTE: A single instance of file is opened and used for all threads.
 * For example, if thread-1 reads the first line and then issues a 'next()',
 * then thread-2 will start from line-2.
 *
 *
 * @author Cyrus M.
 * @version $Revision$ Last Updated: $Date$
 */

/*
 * It appears that JMeter instantiates a new copy of each function for
 * every reference in a Sampler or elsewhere.
 */
public class CSVRead extends AbstractFunction implements Serializable
{
    transient private static final Logger log = LoggingManager.getLoggerForClass();

    private static final String KEY = "__CSVRead"; // Function name

    private static final List desc = new LinkedList();

    private static FileDataContainer fileData;

    private Object[] values;
    private BufferedReader myBread; // Buffered reader

    private static Hashtable threadData = null;

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
    	String columnOrNext="";
    	String myValue = "";
        try
        {

            ArrayList processedLines = null;
            String fileName = null;

            fileName =
                ((org.apache.jmeter.engine.util.CompoundVariable) values[0])
                    .execute();
            columnOrNext =
                ((org.apache.jmeter.engine.util.CompoundVariable) values[1])
                    .execute();

            // instantiates the fileDataContainer if one not already present.
            FileDataContainer myfileData = getFileData(fileName);

            // if argument is 'next' - go to the next line
            if (columnOrNext.equals("next()") || columnOrNext.equals("next"))
            {
                storeCurrentLine(null);
                /*
                 * All done now ,so return the empty string - this allows the caller to
                 * append __CSVRead(file,next) to the last instance of __CSVRead(file,col)
                 * 
                 * N.B. It is important not to read any further lines at this point, otherwise
                 * the wrong line can be retrieved when using multiple threads. 
                 */
                 return "";
            }

            // see if we already have read a line for this thread ...
            processedLines = reloadCurrentLine();

            // if no lines associated with this thread - then read, process and
            // store...
            if (myfileData != null && processedLines == null)
            {
                processedLines = (ArrayList) myfileData.getNextLine();
				myfileData.incrementRowPosition();
                this.storeCurrentLine(processedLines);
            }

            try
            {
                int columnIndex = Integer.parseInt(columnOrNext); // what column is wanted?
                myValue = (String) processedLines.get(columnIndex);
            }
            catch (NumberFormatException e)
            {
                log.warn("Column number error: " + columnOrNext + " "+ e.toString());
            }
			catch (IndexOutOfBoundsException e)
			{
				log.warn("Invalid column number: " + columnOrNext + " "+ e.toString());
			}
			if (log.isDebugEnabled()){
				log.debug(
                  Thread.currentThread().getName() + " " + getId()
                    + ">>>> execute ("
                    + fileName
                    + " , "
                    + columnOrNext
                    + ")   "
                    + this.hashCode());
			}
        }
        catch (IOException e)
        {
            log.error("execute", e);
        }
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
     * get the FileDataContainer
     */
    protected synchronized FileDataContainer getFileData(String fileName)
        throws IOException
    {
        if (CSVRead.fileData == null)
        {
			CSVRead.fileData = load(fileName);
        }

        return CSVRead.fileData;
    }

    protected String getId()
    {
        return "" + Thread.currentThread().hashCode();
    }

    /**
     * @see org.apache.jmeter.functions.Function#getReferenceKey()
     */
    public String getReferenceKey()
    {
        return KEY;
    }
    
    /**
     * Creation date: (24/03/2003 17:11:30)
     * @return java.util.Hashtable
     */
    protected static synchronized Hashtable getThreadData()
    {
        if (threadData == null)
        {
            threadData = new Hashtable();
        }

        return threadData;
    }
    
    /**
     * @see org.apache.jmeter.functions.Function#execute(SampleResult, Sampler)
     */
    private synchronized FileDataContainer load(String fileName)
        throws IOException
    {
        FileDataContainer myfileData = new FileDataContainer();
        openFile(fileName);

        if (null != myBread)
        { // Did we open the file?

            try
            {
                String line = myBread.readLine();
                while (line != null)
                {
                    myfileData.addLine(line);
                    line = myBread.readLine();
                }
                myBread.close();
                setFileData(myfileData);
                return myfileData;
            }
            catch (java.io.IOException e)
            {
                log.error("load(" + fileName + ")", e);
                throw e;
            }
        }
        return myfileData;
    }
    
    private void openFile(String fileName)
    {
        try
        {
            FileReader fis = new FileReader(fileName);
            myBread = new BufferedReader(fis);
        }
        catch (Exception e)
        {
            log.error("openFile", e);
        }
    }
    
    /**
     * this is for version 1.8.1 only -
     * @deprecated
     */
    protected ArrayList reloadCurrentLine() throws InvalidVariableException
    {
    	if (log.isDebugEnabled()){
            log.debug( Thread.currentThread().getName() 
                + " " + getId() + " reloaded " + getThreadData().get(getId()));
    	}

        return (ArrayList) getThreadData().get(getId());
    }

    /**
     * reset - resets the file - so the file is read in the next iteration
     */
    protected synchronized void reset()
    {
        log.debug(Thread.currentThread().getName() + " " +getId() + " reseting .... ");
        this.setFileData(null);
        CSVRead.threadData = new Hashtable();
    }

    /**
     * Set the FileDataContainer.
     */
    protected synchronized void setFileData(FileDataContainer newValue)
    {
		CSVRead.fileData = newValue;
    }
    
    /**
     * @see org.apache.jmeter.functions.Function#setParameters(Collection)
     */
    public void setParameters(Collection parameters)
        throws InvalidVariableException
    {
        log.debug(Thread.currentThread().getName() + " " + getId() + "setParameter - Collection" + parameters);

        reset();
        values = parameters.toArray();

        if (values.length > 2)
        {
            throw new InvalidVariableException();
        }
    }

    /**
     * this is for version 1.8.1 only -
     * @deprecated
     */
    public void storeCurrentLine(ArrayList currentLine)
        throws InvalidVariableException
    {
        String id = getId();
        if (log.isDebugEnabled()){
        	log.debug(Thread.currentThread().getName() + " " + id + " storing " + currentLine);
        }
        if (currentLine == null)
        {
            getThreadData().remove(id);
        }
        else
        {
            getThreadData().put(id, currentLine);
        }
    }
}