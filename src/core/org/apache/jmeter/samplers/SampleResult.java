/*
 *  ====================================================================
 *  The Apache Software License, Version 1.1
 *
 *  Copyright (c) 2001,2003 The Apache Software Foundation.  All rights
 *  reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions
 *  are met:
 *
 *  1. Redistributions of source code must retain the above copyright
 *  notice, this list of conditions and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright
 *  notice, this list of conditions and the following disclaimer in
 *  the documentation and/or other materials provided with the
 *  distribution.
 *
 *  3. The end-user documentation included with the redistribution,
 *  if any, must include the following acknowledgment:
 *  "This product includes software developed by the
 *  Apache Software Foundation (http://www.apache.org/)."
 *  Alternately, this acknowledgment may appear in the software itself,
 *  if and wherever such third-party acknowledgments normally appear.
 *
 *  4. The names "Apache" and "Apache Software Foundation" and
 *  "Apache JMeter" must not be used to endorse or promote products
 *  derived from this software without prior written permission. For
 *  written permission, please contact apache@apache.org.
 *
 *  5. Products derived from this software may not be called "Apache",
 *  "Apache JMeter", nor may "Apache" appear in their name, without
 *  prior written permission of the Apache Software Foundation.
 *
 *  THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 *  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 *  OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 *  ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 *  USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 *  OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 *  OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 *  SUCH DAMAGE.
 *  ====================================================================
 *
 *  This software consists of voluntary contributions made by many
 *  individuals on behalf of the Apache Software Foundation.  For more
 *  information on the Apache Software Foundation, please see
 *  <http://www.apache.org/>.
 */
package org.apache.jmeter.samplers;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.jmeter.assertions.AssertionResult;
import org.apache.log.Hierarchy;
import org.apache.log.Logger;

/**
 *  This is a nice packaging for the various information returned from taking a
 *  sample of an entry.
 *
 *@author     $Author$
 *@created    $Date$
 *@version    $Revision$
 */
public class SampleResult implements Serializable
{
    /**
     * Data type value indicating that the response data is text.
     *
     * @see #getDataType
     * @see #setDataType(java.lang.String)
     */
	public final static String TEXT = "text";

    /**
     * Data type value indicating that the response data is binary.
     *
     * @see #getDataType
     * @see #setDataType(java.lang.String)
     */
	public final static String BINARY = "bin";

	private byte[] responseData;
	private String responseCode;
	private String label;
	private String samplerData;
	private String threadName;
	private String responseMessage;
	private long timeStamp = 0;
	private List assertionResults;
	private List subResults;
	private String dataType;
	private boolean success;
	private Set files;
    private String dataEncoding;
    private long time;

	private final static String TOTAL_TIME = "totalTime";

	transient private static Logger log = Hierarchy.getDefaultHierarchy().getLoggerFor("jmeter.engine");


	public void setMarked(String filename)
	{
        if (files == null) {
            files = new HashSet();
        }
		files.add(filename);
	}
	
	public boolean isMarked(String filename)
	{
        return files != null && files.contains(filename);
	}	
	
	public String getResponseCode()
	{
		return responseCode;
	}
	
	public void setResponseCode(String code)
	{
		responseCode = code;
	}
	
	public String getResponseMessage()
	{
		return responseMessage;
	}
	
	public void setResponseMessage(String msg)
	{
		responseMessage = msg;
	}
	
	public String getThreadName()
	{
		return threadName;
	}
	
	public void setThreadName(String threadName)
	{
		this.threadName = threadName;
	}
	
	public long getTimeStamp()
	{
		return timeStamp;
	}
	
	public void setTimeStamp(long timeStamp)
	{
		this.timeStamp = timeStamp;
	}

	public String getSampleLabel()
	{
		return label;
	}

	public void setSampleLabel(String label)
	{
		this.label = label;
	}

	public void addAssertionResult(AssertionResult assertResult)
	{
        if (assertionResults == null) {
            assertionResults = new ArrayList();
        }
		assertionResults.add(assertResult);
	}

    /**
     * Gets the assertion results associated with this sample.
     *
     * @return an array containing the assertion results for this sample.
     *         Returns null if there are no assertion results.
     */
	public AssertionResult[] getAssertionResults()
	{
        if (assertionResults == null) {
            return null;
        }
		return (AssertionResult[])assertionResults.toArray(new AssertionResult[0]);
	}

	public void addSubResult(SampleResult subResult)
	{
        if (subResults == null) {
            subResults = new ArrayList();
        }
		subResults.add(subResult);
	}

    /**
     * Gets the subresults associated with this sample.
     *
     * @return an array containing the subresults for this sample. Returns
     *         null if there are no subresults.
     */
	public SampleResult[] getSubResults()
	{
        if (subResults == null) {
            return null;
        }
		return (SampleResult[])subResults.toArray(new SampleResult[0]);
	}

	public void configure(Configuration info)
	{
		setTime(info.getAttributeAsLong(TOTAL_TIME,0L));
	}

	/**
	 *  Set the time this sample took to occur.
	 *
	 *@param  t  !ToDo (Parameter description)
	 */
	public void setTime(long t)
	{
		time = t;
	}

	/**
	 *  Sets the responseData attribute of the SampleResult object
	 *
	 *@param  response  The new responseData value
	 */
	public void setResponseData(byte[] response)
	{
		responseData = response;
	}

	


	/**
	 *  Gets the responseData attribute of the SampleResult object
	 *
	 *@return    The responseData value
	 */
	public byte[] getResponseData()
	{
		return responseData;
	}

	public void setSamplerData(String s)
	{
		samplerData = s;
	}

	public String getSamplerData()
	{
		return samplerData;
	}

	/**
	 *  Get the time it took this sample to occur.
	 *
	 *@return    !ToDo (Return description)
	 */
	public long getTime()
	{
		return time;
	}

	public boolean isSuccessful()
	{
		return success;
	}
	
	public void setDataType(String dataType)
	{
		this.dataType = dataType;
	}
	
	public String getDataType()
	{
		return dataType;
	}

	/**
	 *  Sets the successful attribute of the SampleResult object
	 *
	 *@param  success  The new successful value
	 */
	public void setSuccessful(boolean success)
	{
		this.success = success;
	}

	/**
	 *  Returns the display name
	 *
	 *@return    display name of this sample result
	 */
	public String toString()
	{
		return getSampleLabel();
	}
    /**
     * Returns the dataEncoding.
     * @return String
     */
    public String getDataEncoding()
    {
        if(dataEncoding != null)
        {
            return dataEncoding;
        }
        else
        {
            return "8859-1";
        }
    }

    /**
     * Sets the dataEncoding.
     * @param dataEncoding The dataEncoding to set
     */
    public void setDataEncoding(String dataEncoding)
    {
        this.dataEncoding = dataEncoding;
    }

}
