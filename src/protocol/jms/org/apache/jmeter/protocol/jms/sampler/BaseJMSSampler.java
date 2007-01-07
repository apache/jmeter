/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jmeter.protocol.jms.sampler;

import org.apache.jmeter.testelement.TestListener;
import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.util.JMeterUtils;

/**
 * @author pete
 * 
 * BaseJMSSampler is an abstract class which provides implementation for common
 * properties. Rather than duplicate the code, it's contained in the base class.
 */
public abstract class BaseJMSSampler extends AbstractSampler implements TestListener {

	public static final String JNDI_INITIAL_CONTEXT_FAC = "jms.initial_context_factory"; // $NON-NLS-1$

	public static final String PROVIDER_URL = "jms.provider_url"; // $NON-NLS-1$

	public static final String CONN_FACTORY = "jms.connection_factory"; // $NON-NLS-1$

	public static final String TOPIC = "jms.topic"; // $NON-NLS-1$

	public static final String PRINCIPAL = "jms.security_principle"; // $NON-NLS-1$

	public static final String CREDENTIALS = "jms.security_credentials"; // $NON-NLS-1$

	public static final String ITERATIONS = "jms.iterations"; // $NON-NLS-1$

	public static final String USE_AUTH = "jms.authenticate"; // $NON-NLS-1$

	public static final String USE_PROPERTIES_FILE = "jms.jndi_properties"; // $NON-NLS-1$

	public static final String READ_RESPONSE = "jms.read_response"; // $NON-NLS-1$

	public static final String required = JMeterUtils.getResString("jms_auth_required"); // $NON-NLS-1$

	public static final String not_req = JMeterUtils.getResString("jms_auth_not_required"); // $NON-NLS-1$

	public BaseJMSSampler() {
	}

	public abstract void testEnded(String host);

	public abstract void testStarted(String host);

	public abstract void testEnded();

	public abstract void testStarted();

	public abstract void testIterationStart(LoopIterationEvent event);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jmeter.samplers.Sampler#sample(org.apache.jmeter.samplers.Entry)
	 */
	public SampleResult sample(Entry e) {
		return new SampleResult();
	}

	// ------------- get/set properties ----------------------//
	/**
	 * set the initial context factory
	 * 
	 * @param icf
	 */
	public void setJNDIIntialContextFactory(String icf) {
		setProperty(JNDI_INITIAL_CONTEXT_FAC, icf);
	}

	/**
	 * method returns the initial context factory for jndi initial context
	 * lookup.
	 * 
	 * @return
	 */
	public String getJNDIInitialContextFactory() {
		return getPropertyAsString(JNDI_INITIAL_CONTEXT_FAC);
	}

	/**
	 * set the provider user for jndi
	 * 
	 * @param factory
	 */
	public void setProviderUrl(String url) {
		setProperty(PROVIDER_URL, url);
	}

	/**
	 * method returns the provider url for jndi to connect to
	 * 
	 * @return
	 */
	public String getProviderUrl() {
		return getPropertyAsString(PROVIDER_URL);
	}

	/**
	 * set the connection factory for
	 * 
	 * @param factory
	 */
	public void setConnectionFactory(String factory) {
		setProperty(CONN_FACTORY, factory);
	}

	/**
	 * return the connection factory parameter used to lookup the connection
	 * factory from the JMS server
	 * 
	 * @return
	 */
	public String getConnectionFactory() {
		return getPropertyAsString(CONN_FACTORY);
	}

	/**
	 * set the topic
	 * 
	 * @param topic
	 */
	public void setTopic(String topic) {
		setProperty(TOPIC, topic);
	}

	/**
	 * return the topic used for the benchmark
	 * 
	 * @return
	 */
	public String getTopic() {
		return getPropertyAsString(TOPIC);
	}

	/**
	 * set the username to login into the jms server if needed
	 * 
	 * @param user
	 */
	public void setUsername(String user) {
		setProperty(PRINCIPAL, user);
	}

	/**
	 * return the username used to login to the jms server
	 * 
	 * @return
	 */
	public String getUsername() {
		return getPropertyAsString(PRINCIPAL);
	}

	/**
	 * Set the password to login to the jms server
	 * 
	 * @param pwd
	 */
	public void setPassword(String pwd) {
		setProperty(CREDENTIALS, pwd);
	}

	/**
	 * return the password used to login to the jms server
	 * 
	 * @return
	 */
	public String getPassword() {
		return getPropertyAsString(CREDENTIALS);
	}

	/**
	 * set the number of iterations the sampler should aggregate
	 * 
	 * @param count
	 */
	public void setIterations(String count) {
		setProperty(ITERATIONS, count);
	}

	/**
	 * get the iterations as string
	 * 
	 * @return
	 */
	public String getIterations() {
		return getPropertyAsString(ITERATIONS);
	}

	/**
	 * return the number of iterations as int instead of string
	 * 
	 * @return
	 */
	public int getIterationCount() {
		return getPropertyAsInt(ITERATIONS);
	}

	/**
	 * Set whether authentication is required for JNDI
	 * 
	 * @param auth
	 */
	public void setUseAuth(String auth) {
		setProperty(USE_AUTH, auth);
	}

	/**
	 * return whether jndi requires authentication
	 * 
	 * @return
	 */
	public String getUseAuth() {
		return getPropertyAsString(USE_AUTH);
	}

	/**
	 * set whether the sampler should read the response or not
	 * 
	 * @param read
	 */
	public void setReadResponse(String read) {
		setProperty(READ_RESPONSE, read);
	}

	/**
	 * return whether the sampler should read the response
	 * 
	 * @return
	 */
	public String getReadResponse() {
		return getPropertyAsString(READ_RESPONSE);
	}

	/**
	 * return whether the sampler should read the response as a boolean value
	 * 
	 * @return
	 */
	public boolean getReadResponseAsBoolean() {
		return getPropertyAsBoolean(READ_RESPONSE);
	}

	/**
	 * if the sampler should use jndi.properties file, call the method with true
	 * 
	 * @param properties
	 */
	public void setUseJNDIProperties(String properties) {
		setProperty(USE_PROPERTIES_FILE, properties);
	}

	/**
	 * return whether the sampler should use properties file instead of UI
	 * parameters.
	 * 
	 * @return
	 */
	public String getUseJNDIProperties() {
		return getPropertyAsString(USE_PROPERTIES_FILE);
	}

	/**
	 * return the properties as boolean true/false.
	 * 
	 * @return
	 */
	public boolean getUseJNDIPropertiesAsBoolean() {
		return getPropertyAsBoolean(USE_PROPERTIES_FILE);
	}
}
