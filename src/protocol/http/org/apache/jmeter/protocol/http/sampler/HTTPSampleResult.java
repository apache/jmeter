/*
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
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
 * 
 * @author <a href="mailto:jsalvata@apache.org">Jordi Salvat i Alabart</a>
 * @version $Id$
 */
package org.apache.jmeter.protocol.http.sampler;

import java.net.URL;

import org.apache.jmeter.samplers.SampleResult;

/**
 * This is a specialisation of the SampleResult class for the HTTP protocol.
 */
public class HTTPSampleResult extends SampleResult
{
    public HTTPSampleResult()
    {
        super();
    }
    
	public HTTPSampleResult(long elapsed)
	{
		super(elapsed,true);
	}
    
    /**
     * Construct a 'parent' result for an already-existing result, essentially
     * cloning it
     * 
     * The start-time is set from the existing sample.
     *  
     * @param res existing sample result
     */
    public HTTPSampleResult(HTTPSampleResult res)
    {
        setStartTime(res.getStartTime());

        setSampleLabel(res.getSampleLabel());
        setHTTPMethod(res.getHTTPMethod());
        setURL(res.getURL());
        setRequestHeaders(res.getRequestHeaders());
        setResponseData(res.getResponseData());
        setResponseCode(res.getResponseCode());
        setSuccessful(res.isSuccessful());
        setResponseMessage(res.getResponseMessage());
        setDataType(res.getDataType());
        setResponseHeaders(res.getResponseHeaders());
        setCookies(res.getCookies());
        addSubResult(res);
    }

    private URL location;
    
    public void setURL(URL location) {
        this.location= location;
    }
    
    public URL getURL() {
        return location;
    }
    
    private String method;
    
    public void setHTTPMethod(String method) {
        this.method= method;
    }
    public String getHTTPMethod() {
        return method;
    }
    
    private String redirectLocation;

    public void setRedirectLocation(String redirectLocation)
    {
        this.redirectLocation= redirectLocation;
    }
    public String getRedirectLocation()
    {
        return redirectLocation;
    }

    /**
     * Determine whether this result is a redirect.
     * 
     * @return      true iif res is an HTTP redirect response
     */
    public boolean isRedirect()
    {
        final String[] REDIRECT_CODES= { "301", "302", "303", "304" };
        String code= getResponseCode();
        for (int i= 0; i < REDIRECT_CODES.length; i++)
        {
            if (REDIRECT_CODES[i].equals(code))
                return true;
        }
        return false;
    }
    
    
    /* (non-Javadoc)
     * @see org.apache.jmeter.samplers.SampleResult#getSamplerData()
     */
    public String getSamplerData()
    {
        StringBuffer sb= new StringBuffer();
        sb.append(getHTTPMethod());
        URL u= getURL();
        if (u != null)
        {
            sb.append(' ');
            sb.append(u.toString());
        }
        String s= super.getSamplerData();
        if (s != null)
        {
            sb.append('\n');
            sb.append(s);
        }
        return sb.toString();
    }
    
    private String cookies=""; // never null
    /**
     * @return cookies as a string
     */
    public String getCookies()
    {
        return cookies;
    }

    /**
     * @param string representing the cookies
     */
    public void setCookies(String string)
    {
        cookies = string;
    }

    private String queryString = ""; // never null
    /**
     * Fetch the query string
     * 
     * @return the query string
     */
    public String getQueryString()
    {
        return queryString;
    }

    /**
     * Save the query string
     * 
     * @param string the query string
     */
    public void setQueryString(String string)
    {
        queryString = string;
    }

}
