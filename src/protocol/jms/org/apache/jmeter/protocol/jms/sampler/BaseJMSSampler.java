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
import java.util.function.Predicate;
import java.util.regex.Pattern;

import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.util.JMeterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * BaseJMSSampler is an abstract class which provides implementation for common
 * properties. Rather than duplicate the code, it's contained in the base class.
 */
public abstract class BaseJMSSampler extends AbstractSampler {

    private static final long serialVersionUID = 241L;

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseJMSSampler.class);

    //++ These are JMX file attribute names and must not be changed
    private static final String JNDI_INITIAL_CONTEXT_FAC = "jms.initial_context_factory"; // $NON-NLS-1$

    private static final String PROVIDER_URL = "jms.provider_url"; // $NON-NLS-1$

    private static final String CONN_FACTORY = "jms.connection_factory"; // $NON-NLS-1$

    // N.B. Cannot change value, as that is used in JMX files
    private static final String DEST = "jms.topic"; // $NON-NLS-1$

    private static final String PRINCIPAL = "jms.security_principle"; // $NON-NLS-1$

    private static final String CREDENTIALS = "jms.security_credentials"; // $NON-NLS-1$

    /*
     * The number of samples to aggregate
     */
    private static final String ITERATIONS = "jms.iterations"; // $NON-NLS-1$

    private static final String USE_AUTH = "jms.authenticate"; // $NON-NLS-1$

    private static final String USE_PROPERTIES_FILE = "jms.jndi_properties"; // $NON-NLS-1$

    /*
     * If true, store the response in the sampleResponse
     * (N.B. do not change the value, as it is used in JMX files)
     */
    private static final String STORE_RESPONSE = "jms.read_response"; // $NON-NLS-1$

    // Is Destination setup static? else dynamic
    private static final String DESTINATION_STATIC = "jms.destination_static"; // $NON-NLS-1$
    private static final boolean DESTINATION_STATIC_DEFAULT = true; // default to maintain compatibility

    /** Property name for regex of error codes which force reconnection **/
    private static final String ERROR_RECONNECT_ON_CODES = "jms_error_reconnect_on_codes"; // $NON-NLS-1$
    private transient Predicate<String> isReconnectErrorCode = e -> false;

    //-- End of JMX file attribute names

    // See BUG 45460. We need to keep the resource in order to interpret existing files
    private static final String REQUIRED = JMeterUtils.getResString("jms_auth_required"); // $NON-NLS-1$

    public BaseJMSSampler() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SampleResult sample(Entry e) {
        return this.sample();
    }

    public abstract SampleResult sample();

    // ------------- get/set properties ----------------------//
    /**
     * set the initial context factory
     *
     * @param icf the initial context factory
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
     * @param factory the connection factory
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
     * @param user the name of the user
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
     * @param pwd the password to use for login on the jms server
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
     * @param count the number of iterations
     */
    public void setIterations(String count) {
        setProperty(ITERATIONS, count);
    }

    /**
     * get the number of samples to aggregate
     *
     * @return String containing the number of samples to aggregate
     */
    public String getIterations() {
        return getPropertyAsString(ITERATIONS);
    }

    /**
     * get the number of samples to aggregate
     *
     * @return int containing the number of samples to aggregate
     */
    public int getIterationCount() {
        return getPropertyAsInt(ITERATIONS);
    }

    /**
     * Set whether authentication is required for JNDI
     *
     * @param useAuth flag whether to use authentication
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
     * set whether the sampler should store the response or not
     *
     * @param read whether the sampler should store the response or not
     */
    public void setReadResponse(String read) {
        setProperty(STORE_RESPONSE, read);
    }

    /**
     * return whether the sampler should store the response
     *
     * @return whether the sampler should store the response
     */
    public String getReadResponse() {
        return getPropertyAsString(STORE_RESPONSE);
    }

    /**
     * return whether the sampler should store the response
     *
     * @return boolean: whether the sampler should read the response
     */
    public boolean getReadResponseAsBoolean() {
        return getPropertyAsBoolean(STORE_RESPONSE);
    }

    /**
     * if the sampler should use jndi.properties file, call the method with the string "true"
     *
     * @param properties flag whether to use <em>jndi.properties</em> file
     */
    public void setUseJNDIProperties(String properties) {
        setProperty(USE_PROPERTIES_FILE, properties);
    }

    /**
     * return whether the sampler should use properties file instead of UI
     * parameters.
     *
     * @return the string "true" when the sampler should use properties file
     *         instead of UI parameters, the string "false" otherwise.
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
     * @param isStatic flag whether the destination is a static destination
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
            response.append(destination == null ? null : destination
                .toString());

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
            response.append(replyTo == null ? null : replyTo.toString());

            response.append("\n   Timestamp: ");
            response.append(new Date(message.getJMSTimestamp()));

            response.append("\n   Type: ");
            response.append(message.getJMSType());

            response.append("\n\n");

        } catch (JMSException e) {
            LOGGER.warn(
                    "Can't extract message headers", e);
        }

        return response.toString();
    }

    public String getReconnectionErrorCodes() {
        return getPropertyAsString(ERROR_RECONNECT_ON_CODES);
    }

    public void setReconnectionErrorCodes(String reconnectionErrorCodes) {
        setProperty(ERROR_RECONNECT_ON_CODES, reconnectionErrorCodes);
    }

    public Predicate<String> getIsReconnectErrorCode() {
        return isReconnectErrorCode;
    }

    /**
     *
     */
    protected void configureIsReconnectErrorCode() {
        String regex = StringUtils.trimToEmpty(getReconnectionErrorCodes());
        if (regex.isEmpty()) {
            isReconnectErrorCode = e -> false;
        } else {
            isReconnectErrorCode = Pattern.compile(regex).asPredicate();
        }
    }
}
