/*
 * Copyright 2003-2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package org.apache.jmeter.protocol.http.sampler;

import java.net.URL;

import org.apache.jmeter.samplers.SampleResult;
//import org.apache.jorphan.logging.LoggingManager;
//import org.apache.log.Logger;

/**
 * This is a specialisation of the SampleResult class for the HTTP protocol.
 * 
 * @author <a href="mailto:jsalvata@apache.org">Jordi Salvat i Alabart</a>
 * @version $Revision$ updated on $Date$
 */
public class HTTPSampleResult extends SampleResult {
	//private static final Logger log = LoggingManager.getLoggerForClass();

	public HTTPSampleResult() {
		super();
	}

	public HTTPSampleResult(long elapsed) {
		super(elapsed, true);
	}

	/**
	 * Construct a 'parent' result for an already-existing result, essentially
	 * cloning it
	 * 
	 * @param res
	 *            existing sample result
	 */
	public HTTPSampleResult(HTTPSampleResult res) {
		super(res);
		method=res.method;
		setURL(res.getURL());
		cookies=res.cookies;
        queryString=res.queryString;
        redirectLocation=res.redirectLocation;
	}

	private String method;

	public void setHTTPMethod(String method) {
		this.method = method;
	}

	public String getHTTPMethod() {
		return method;
	}

	private String redirectLocation;

	public void setRedirectLocation(String redirectLocation) {
		this.redirectLocation = redirectLocation;
	}

	public String getRedirectLocation() {
		return redirectLocation;
	}

	/**
	 * Determine whether this result is a redirect.
	 * 
	 * @return true iif res is an HTTP redirect response
	 */
	public boolean isRedirect() {
		final String[] REDIRECT_CODES = { "301", "302", "303" }; // NOT 304!
		String code = getResponseCode();
		for (int i = 0; i < REDIRECT_CODES.length; i++) {
			if (REDIRECT_CODES[i].equals(code))
				return true;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
     * Overrides version in Sampler data to provide more details
	 * 
	 * @see org.apache.jmeter.samplers.SampleResult#getSamplerData()
	 */
	public String getSamplerData() {
		StringBuffer sb = new StringBuffer();
		sb.append(method);
		URL u = super.getURL();
		if (u != null) {
			sb.append(' ');
			sb.append(u.toString());
            sb.append("\n");
			if (HTTPSamplerBase.POST.equals(method)) {
                sb.append("\nPOST data:\n");
				sb.append(queryString);
                sb.append("\n");
            }
            if (cookies.length()>0){
                sb.append("\nCookie Data:\n");
    			sb.append(cookies);
            } else {
                sb.append("\n[no cookies]");
            }
            sb.append("\n");
		}
		return sb.toString();
	}

	private String cookies = ""; // never null

	/**
	 * @return cookies as a string
	 */
	public String getCookies() {
		return cookies;
	}

	/**
	 * @param string
	 *            representing the cookies
	 */
	public void setCookies(String string) {
        if (string == null) {
            cookies="";// $NON-NLS-1$
        } else {
    		cookies = string;
        }
	}

	private String queryString = ""; // never null

	/**
	 * Fetch the query string
	 * 
	 * @return the query string
	 */
	public String getQueryString() {
		return queryString;
	}

	/**
	 * Save the query string
	 * 
	 * @param string
	 *            the query string
	 */
	public void setQueryString(String string) {
        if (string == null ) {
            queryString="";// $NON-NLS-1$
        } else {
    		queryString = string;
        }
	}
    
}
