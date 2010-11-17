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

import java.util.Date;

import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;

import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.util.JMeterUtils;

/**
 *
 * BaseJMSSampler is an abstract class which provides implementation for common
 * properties. Rather than duplicate the code, it's contained in the base class.
 */
public abstract class BaseJMSSampler extends AbstractSampler {

    private static final long serialVersionUID = 240L;

    //++ These are JMX file attribute names and must not be changed
    private static final String JNDI_INITIAL_CONTEXT_FAC = "jms.initial_context_factory"; // $NON-NLS-1$

    private static final String PROVIDER_URL = "jms.provider_url"; // $NON-NLS-1$

    private static final String CONN_FACTORY = "jms.connection_factory"; // $NON-NLS-1$

    // N.B. Cannot change value, as that is used in JMX files
    private static final String DEST = "jms.topic"; // $NON-NLS-1$

    private static final String PRINCIPAL = "jms.security_principle"; // $NON-NLS-1$

    private static final String CREDENTIALS = "jms.security_credentials"; // $NON-NLS-1$

    private static final String ITERATIONS = "jms.iterations"; // $NON-NLS-1$

    private static final String USE_AUTH = "jms.authenticate"; // $NON-NLS-1$

    private static final String USE_PROPERTIES_FILE = "jms.jndi_properties"; // $NON-NLS-1$

    private static final String READ_RESPONSE = "jms.read_response"; // $NON-NLS-1$

    // Is Destination setup static? else dynamic
    private static final String DESTINATION_STATIC = "jms.destination_static"; // $NON-NLS-1$
    private static final boolean DESTINATION_STATIC_DEFAULT = true; // default to maintain compatibility

    //-- End of JMX file attribute names

    // See BUG 45460. We need to keep the resource in order to interpret existing files
    private static final String REQUIRED = JMeterUtils.getResString("jms_auth_required"); // $NON-NLS-1$

    public BaseJMSSampler() {
    }

    /**
     * {@inheritDoc}
     */
    public SampleResult sample(Entry e) {
        return this.sample();
    }

    public abstract SampleResult sample();

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
     * @return the initial context factory
     */
    public String getJNDIInitialContextFactory() {
        return getPropertyAsString(JNDI_INITIAL_CONTEXT_FAC);
    }

    /**
     * set the provider user for jndi
     *
     * @param url the provider URL
     */
    public void setProviderUrl(String url) {
        setProperty(PROVIDER_URL, url);
    }

    /**
     * method returns the provider url for jndi to connect to
     *
     * @return the provider URL
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
     * @return the connection factory
     */
    public String getConnectionFactory() {
        return getPropertyAsString(CONN_FACTORY);
    }

    /**
     * set the destination (topic or queue name)
     *
     * @param dest the destination
     */
    public void setDestination(String dest) {
        setProperty(DEST, dest);
    }

    /**
     * return the destination (topic or queue name)
     *
     * @return the destination
     */
    public String getDestination() {
        return getPropertyAsString(DEST);
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
     * @return the username used to login to the jms server
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
     * @return the password used to login to the jms server
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
     * @return the number of iterations
     */
    public String getIterations() {
        return getPropertyAsString(ITERATIONS);
    }

    /**
     * return the number of iterations as int instead of string
     *
     * @return the number of iterations as int instead of string
     */
    public int getIterationCount() {
        return getPropertyAsInt(ITERATIONS);
    }

    /**
     * Set whether authentication is required for JNDI
     *
     * @param useAuth
     */
    public void setUseAuth(boolean useAuth) {
        setProperty(USE_AUTH, useAuth);
    }

    /**
     * return whether jndi requires authentication
     *
     * @return whether jndi requires authentication
     */
    public boolean isUseAuth() {
        final String useAuth = getPropertyAsString(USE_AUTH);
        return useAuth.equalsIgnoreCase("true") || useAuth.equals(REQUIRED); // $NON-NLS-1$
    }

    /**
     * set whether the sampler should read the response or not
     *
     * @param read whether the sampler should read the response or not
     */
    public void setReadResponse(String read) {
        setProperty(READ_RESPONSE, read);
    }

    /**
     * return whether the sampler should read the response
     *
     * @return whether the sampler should read the response
     */
    public String getReadResponse() {
        return getPropertyAsString(READ_RESPONSE);
    }

    /**
     * return whether the sampler should read the response as a boolean value
     *
     * @return whether the sampler should read the response as a boolean value
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
     * @return  whether the sampler should use properties file instead of UI parameters.
     */
    public String getUseJNDIProperties() {
        return getPropertyAsString(USE_PROPERTIES_FILE);
    }

    /**
     * return the properties as boolean true/false.
     *
     * @return whether the sampler should use properties file instead of UI parameters.
     */
    public boolean getUseJNDIPropertiesAsBoolean() {
        return getPropertyAsBoolean(USE_PROPERTIES_FILE);
    }

    /**
     * if the sampler should use a static destination, call the method with true
     *
     * @param isStatic
     */
    public void setDestinationStatic(boolean isStatic) {
	    setProperty(DESTINATION_STATIC, isStatic, DESTINATION_STATIC_DEFAULT);
    }

    /**
     * return whether the sampler should use a static destination.
     *
     * @return  whether the sampler should use a static destination.
     */
    public boolean isDestinationStatic(){
        return getPropertyAsBoolean(DESTINATION_STATIC, DESTINATION_STATIC_DEFAULT);
    }

    /**
     * Returns a String with the JMS Message Header values.
     *
     * @param message JMS Message
     * @return String with message header values.
     */
    public static String getMessageHeaders(Message message) {
        final StringBuilder response = new StringBuilder(256);
        try {
            response.append("JMS Message Header Attributes:");
            response.append("\n   Correlation ID: ");
            response.append(message.getJMSCorrelationID());

            response.append("\n   Delivery Mode: ");
            if (message.getJMSDeliveryMode() == DeliveryMode.PERSISTENT) {
                response.append("PERSISTANT");
            } else {
                response.append("NON-PERSISTANT");
            }

            final Destination destination = message.getJMSDestination();

            response.append("\n   Destination: ");
            response.append((destination == null ? null : destination
                .toString()));

            response.append("\n   Expiration: ");
            response.append(new Date(message.getJMSExpiration()));

            response.append("\n   Message ID: ");
            response.append(message.getJMSMessageID());

            response.append("\n   Priority: ");
            response.append(message.getJMSPriority());

            response.append("\n   Redelivered: ");
            response.append(message.getJMSRedelivered());

            final Destination replyTo = message.getJMSReplyTo();
            response.append("\n   Reply to: ");
            response.append((replyTo == null ? null : replyTo.toString()));

            response.append("\n   Timestamp: ");
            response.append(new Date(message.getJMSTimestamp()));

            response.append("\n   Type: ");
            response.append(message.getJMSType());

            response.append("\n\n");

        } catch (JMSException e) {
            e.printStackTrace();
        }

        return new String(response);
    }
}
