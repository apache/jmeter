/*
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
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

package org.apache.jmeter.protocol.java.sampler;

import java.util.HashMap;
import java.util.Iterator;

import org.apache.jmeter.config.Argument;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.config.JavaConfig;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestElement;
import org.apache.log4j.Category;

/**
 * 
 */


public class JavaSampler extends AbstractSampler implements JavaSamplerClient {

	/** Handle to Java client. **/
	
	public final static String CLASSNAME = "classname";
	
	public final static String ARGUMENTS = "arguments";

	private JavaSamplerClient javaClient = null;

	/** Logging  */
	private static Category cat = Category.getInstance(JavaSampler.class);

	public JavaSampler() {
		setArguments(new Arguments());
	}
	
	public void setArguments(Arguments args)
	{
		this.setProperty(ARGUMENTS,args);
	}
	
	public Arguments getArguments()
	{
		return (Arguments)getProperty(ARGUMENTS);
	}
	
	public void addCustomTestElement(TestElement el)
	{
		if(el instanceof JavaConfig)
		{
			mergeIn(el);
		}
	}
	
	/**
	 * Releases Java Client.
	 */

	public void releaseJavaClient () {
		if (javaClient != null) {
			javaClient.teardownTest(createArgumentsHashMap(getArguments()));
		}
		javaClient = null;
	}
	
	/**
	 *  Sets the Classname attribute of the JavaConfig object
	 *
	 *@param  classname  The new Classname value
	 */
	public void setClassname(String classname)
	{
		this.setProperty(CLASSNAME, classname);
	}



	/**
	 *  Gets the Classname attribute of the JavaConfig object
	 *
	 *@return    The Classname value
	 */
	public String getClassname()
	{
		return this.getPropertyAsString(CLASSNAME);
	}
	
	/**
	 * Performs a test sample.
	 * 
	 * The <code>sample()</code> method retrieves the reference to the
	 * Java client and calls its <code>runTest()</code> method.
	 * @see Sampler#sample()
	 * @see JavaSamplerClient#runTest()
	 * 
	 * @return test SampleResult
	 */
	

	public SampleResult sample(Entry entry) {
		return createJavaClient().runTest(createArgumentsHashMap(getArguments()));
	}
	
	public HashMap createArgumentsHashMap(Arguments args)
	{
		HashMap newArgs = new HashMap();
		Iterator iter = args.iterator();
		while (iter.hasNext())
		{
			Argument item = (Argument)iter.next();
			newArgs.put(item.getName(),item.getValue());
		}

		return newArgs;
	}
	
		/**
	 * Returns reference to <code>JavaSamplerClient</code>.
	 * 
	 * The <code>createJavaClient()</code> method uses reflection
	 * to create an instance of the specified Java protocol client.
	 * If the class can not be found, the method returns a reference
	 * to <code>this</code> object.
	 * 
	 * @return JavaSamplerClient reference.
	 */

	public JavaSamplerClient createJavaClient() {
		if (javaClient == null) {
			try {
				Class javaClass = Class.forName(getClassname().trim(),
						false,Thread.currentThread().getContextClassLoader());
				java.lang.reflect.Constructor[] constructors = javaClass.getConstructors();

				for (int i = 0; i < constructors.length; i++) {
					Class[] params = constructors[i].getParameterTypes();
					if (params.length == 0) {
						Object[] args = {};
						javaClient = (JavaSamplerClient)constructors[i].newInstance(args);
						javaClient.setupTest(createArgumentsHashMap(getArguments()));
						if (cat.isDebugEnabled()) {
							cat.debug(whoAmI() + "\tCreated:\t"+ getClassname()+ "@" + Integer.toHexString(javaClient.hashCode()));
						}
						break;
					}
				}

			} catch (Exception e) {
				cat.error(whoAmI() + "\tException creating: " + getClassname(),e);
				javaClient = this;
			}
		}
		
		return javaClient;
			
	}
	
	/**
	 * Retrieves reference to JavaSamplerClient.
	 * 
	 * Convience method used to check for null reference without
	 * actually creating a JavaSamplerClient
	 * 
	 * @return reference to JavaSamplerClient
	 */
	
	public JavaSamplerClient retrieveJavaClient() {
		return javaClient;
	}
	
	/**
	 * Provide default setupTest() implementation for error conditions.
	 * @see JavaSamplerClient#setupTest()
	 */
	public void setupTest(HashMap arguments) {
		cat.debug(whoAmI() + "\tsetupTest");
	}

	/**
	 * Provide default teardownTest() implementation for error conditions.
	 * @see JavaSamplerClient#teardownTest()
	 */
	public void teardownTest(HashMap arguments) {
		cat.debug(whoAmI() + "\tteardownTest");
		javaClient = null;
	}

	/**
	 * Return SampleResult with data on error.
	 * @see JavaSamplerClient#runTest()
	 */
	public SampleResult runTest(HashMap arguments) {
		cat.debug(whoAmI() + "\trunTest");
		Thread.yield();
		SampleResult results = new SampleResult();
		results.setTime(0);
		results.setSuccessful(false);
		results.setResponseData(new String("Class not found: " + getClassname()).getBytes());
		results.setSampleLabel("ERROR: " + getClassname());
		return results;
	}
	
	private String whoAmI() {
		StringBuffer sb = new StringBuffer();
		sb.append(Thread.currentThread().getName());
		sb.append("@");
		sb.append(Integer.toHexString(hashCode()));
		return sb.toString();
	}

}