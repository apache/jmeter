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

package org.apache.jmeter.protocol.http.util.accesslog;

import java.io.File;
import java.io.FileWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import org.apache.jmeter.protocol.http.sampler.HTTPSampler;

/**
 * Title:		Apache Jakarta JMeter<br>
 * Copyright:	Copyright (c) Apache<br>
 * Company:		Apache<br>
 * License:<br>
 * <br>
 * The license is at the top!<br>
 * <br>
 * Description:<br>
 * <br>
 * StandardGenerator will be the default generator used
 * to pre-process logs. It uses JMeter classes to
 * generate the .jmx file. The first version of the
 * utility only generated the HTTP requests as XML, but
 * it required users to copy and paste it into a blank
 * jmx file. Doing that way isn't flexible and would
 * require changes to keep the format in sync.<p>
 * This version is a completely new class with a totally
 * different implementation, since generating the XML
 * is no longer handled by the generator. The generator
 * is only responsible for handling the parsed results
 * and passing it to the appropriate JMeter class.<p>
 * Notes:<br>
 * the class needs to first create a thread group and
 * add it to the HashTree. Then the samplers should
 * be added to the thread group. Listeners shouldn't
 * be added and should be left up to the user. One
 * option is to provide parameters, so the user can
 * pass the desired listener to the tool.
 * <p>
 * Author:	Peter Lin<br>
 * Version: 	0.1<br>
 * Created on:	Jul 1, 2003<br>
 * Last Modified:	5:40:53 PM<br>
 */

public class StandardGenerator implements Generator, Serializable {

	protected HTTPSampler SAMPLE = null;
	protected FileWriter WRITER = null;
	protected OutputStream OUTPUT = null;
	protected String FILENAME = null;
	protected File FILE = null;
	protected ThreadGroup THREADGROUP = null;
	
	/**
	 * The constructor is used by GUI and samplers
	 * to generate request objects.
	 */
	public StandardGenerator() {
		super();
		init();
	}

	/**
	 * 
	 * @param file
	 */
	public StandardGenerator(String file){
		FILENAME = file;
		init();
	}

	/**
	 * initialize the generator. It should create
	 * the following objects.<p>
	 * <ol>
	 *   <li> ListedHashTree</li>
	 *   <li> ThreadGroup</li>
	 *   <li> File object</li>
	 *   <li> Writer</li>
	 * </ol>
	 */
	protected void init(){
		generateRequest();
	}

	/**
	 * Create the FileWriter to save the JMX file.
	 */
	protected void initStream(){
		try {
			this.OUTPUT = new FileOutputStream(FILE);
		} catch (IOException exception){
			// do nothing
		}
	}
	
	/* (non-Javadoc)
	 * @see org.apache.jmeter.protocol.http.util.accesslog.Generator#close()
	 */
	public void close() {
		try {
			if (OUTPUT != null){
				OUTPUT.close();
			}
			if (WRITER != null){
				WRITER.close();
			}
		} catch (IOException exception){
		}
	}

	/* (non-Javadoc)
	 * @see org.apache.jmeter.protocol.http.util.accesslog.Generator#setHost(java.lang.String)
	 */
	public void setHost(String host) {
		SAMPLE.setDomain(host);
	}

	/* (non-Javadoc)
	 * @see org.apache.jmeter.protocol.http.util.accesslog.Generator#setLabel(java.lang.String)
	 */
	public void setLabel(String label) {

	}

	/* (non-Javadoc)
	 * @see org.apache.jmeter.protocol.http.util.accesslog.Generator#setMethod(java.lang.String)
	 */
	public void setMethod(String post_get) {
		SAMPLE.setMethod(post_get);
	}

	/* (non-Javadoc)
	 * @see org.apache.jmeter.protocol.http.util.accesslog.Generator#setParams(org.apache.jmeter.protocol.http.util.accesslog.NVPair[])
	 */
	public void setParams(NVPair[] params) {
		for (int idx=0; idx < params.length; idx++){
			SAMPLE.addArgument(params[idx].getName(),params[idx].getValue());
		}
	}

	/* (non-Javadoc)
	 * @see org.apache.jmeter.protocol.http.util.accesslog.Generator#setPath(java.lang.String)
	 */
	public void setPath(String path) {
		SAMPLE.setPath(path);
	}

	/* (non-Javadoc)
	 * @see org.apache.jmeter.protocol.http.util.accesslog.Generator#setPort(int)
	 */
	public void setPort(int port) {
		SAMPLE.setPort(port);
	}

	/* (non-Javadoc)
	 * @see org.apache.jmeter.protocol.http.util.accesslog.Generator#setQueryString(java.lang.String)
	 */
	public void setQueryString(String querystring) {
		SAMPLE.parseArguments(querystring);
	}

	/* (non-Javadoc)
	 * @see org.apache.jmeter.protocol.http.util.accesslog.Generator#setSourceLogs(java.lang.String)
	 */
	public void setSourceLogs(String sourcefile) {
	}

	/* (non-Javadoc)
	 * @see org.apache.jmeter.protocol.http.util.accesslog.Generator#setTarget(java.lang.Object)
	 */
	public void setTarget(Object target) {
	}

	/* (non-Javadoc)
	 * @see org.apache.jmeter.protocol.http.util.accesslog.Generator#generateRequest()
	 */
	public Object generateRequest() {
		try {
			SAMPLE = new HTTPSampler();
		} catch (NullPointerException e){
			e.printStackTrace();
		}
		return SAMPLE;
	}
	
	/**
	 * save must be called to write the jmx file,
	 * otherwise it will not be saved.
	 */
	public void save(){
		try {
			// no implementation at this time, since
			// we bypass the idea of having a console
			// tool to generate test plans. Instead
			// I decided to have a sampler that uses
			// the generator and parser directly
		} catch (Exception exception){
		}
	}

	/**
	 * Reset the HTTPSampler to make sure it is a new instance.
	 */
	public void reset(){
		SAMPLE = null;
		generateRequest();
	}
	
	public static void main(String[] args) {
	}
}
