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


/**
 * Title:		JMeter Access Log utilities<br>
 * Copyright:	Apache.org<br>
 * Company:		nobody<br>
 * License:<br>
 * <br>
 * Look at the apache license at the top.<br>
 * <br>
 * Description:<br>
 * <br>
 * Generator is a base interface that defines
 * the minimum methods needed to implement a
 * concrete generator. The reason for creating
 * this interface is eventually JMeter could
 * use the logs directly rather than pre-
 * process the logs into a JMeter .jmx file.
 * In situations where a test plan simulates
 * load from production logs, it is more
 * efficient for JMeter to use the logs
 * directly.<p>
 * From first hand experience, loading a test
 * plan with 10K or more Requests requires a
 * lot of memory. It's important to keep in
 * mind this type of testing is closer to
 * functional and regression testing than the
 * typical stress tests. Typically, this kind
 * of testing is most useful for search sites
 * that get a large number of requests per
 * day, but the request parameters vary
 * dramatically. E-commerce sites typically
 * have limited inventory, therefore it is
 * better to design test plans that use data
 * from the database.
 * <p>
 * Author:	Peter Lin<br>
 * Version: 	0.1<br>
 * Created on:	Jun 23, 2003<br>
 * Last Modified:	5:07:53 PM<br>
 */

public interface Generator {

	/**
	 * close the generator
	 */
	public void close();

	/**
	 * The host is the name of the server.
	 * @param host
	 */	
	public void setHost(String host);

	/**
	 * This is the label for the request,
	 * which is used in the logs and
	 * results.
	 * @param label
	 */
	public void setLabel(String label);

	/**
	 * The method is the HTTP request
	 * method. It's normally POST or GET.
	 * @param post_get
	 */	
	public void setMethod(String post_get);

	/**
	 * Set the request parameters
	 * @param params
	 */	
	public void setParams(NVPair[] params);

	/**
	 * The path is the web page you want
	 * to test.
	 * @param path
	 */
	public void setPath(String path);

	/**
	 * The default port for HTTP is 80,
	 * but not all servers run on that
	 * port.
	 * @param port - port number
	 */	
	public void setPort(int port);

	/**
	 * Set the querystring for the request
	 * if the method is GET.
	 * @param querystring
	 */	
	public void setQueryString(String querystring);

	/**
	 * The source logs is the location
	 * where the access log resides.
	 * @param sourcefile
	 */
	public void setSourceLogs(String sourcefile);

	/**
	 * The target can be either a java.io.File
	 * or a Sampler. We make it generic, so
	 * that later on we can use these classes
	 * directly from a HTTPSampler.
	 * @param target
	 */	
	public void setTarget(Object target);

	/**
	 * The method is responsible for calling
	 * the necessary methods to generate a
	 * valid request. If the generator is
	 * used to pre-process access logs, the
	 * method wouldn't return anything. If
	 * the generator is used by a control
	 * element, it should return the correct
	 * Sampler class with the required
	 * fields set.
	 */	
	public Object generateRequest();
	
	/**
	 * If the generator is converting the logs
	 * to a .jmx file, save should be called.
	 */
	public void save();
	
	/**
	 * The purpose of the reset is so Samplers
	 * can explicitly call reset to create a 
	 * new instance of HTTPSampler.
	 *
	 */
	public void reset();
}
