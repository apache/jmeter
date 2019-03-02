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

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.lang.reflect.Modifier;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.naming.NamingException;

import org.apache.commons.io.IOUtils;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.jms.Utils;
import org.apache.jmeter.protocol.jms.client.ClientPool;
import org.apache.jmeter.protocol.jms.client.InitialContextFactory;
import org.apache.jmeter.protocol.jms.client.Publisher;
import org.apache.jmeter.protocol.jms.control.gui.JMSPublisherGui;
import org.apache.jmeter.protocol.jms.sampler.render.MessageRenderer;
import org.apache.jmeter.protocol.jms.sampler.render.Renderers;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.services.FileServer;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.apache.jmeter.util.JMeterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

/**
 * This class implements the JMS Publisher sampler.
 */
public class PublisherSampler extends BaseJMSSampler implements TestStateListener {

    /** Encoding value to sent data as is (no variabilisation) **/
    public static final String RAW_DATA = "<RAW>";
    /**
     * Encoding value to sent parsed data but read with default system encoding
     **/
    public static final String DEFAULT_ENCODING = "<DEFAULT>";

    /** Constant for system default encodings **/
    public static final Set<String> NO_ENCODING = Collections
            .unmodifiableSet(new LinkedHashSet<>(Arrays.asList(RAW_DATA, DEFAULT_ENCODING)));

    /** 
     * Init available encoding using constants, then JVM standard ones
     * @return Array of String containing supported encodings 
     */
    public static String[] getSupportedEncodings() {
        // Only get JVM standard charsets
        return Stream.concat(
                NO_ENCODING.stream(),
                Arrays.stream(StandardCharsets.class.getDeclaredFields())
                        .filter(f -> Modifier.isStatic(f.getModifiers())
                                && Modifier.isPublic(f.getModifiers())
                                && f.getType() == Charset.class)
                        .map(f -> {
                            try {
                                return (Charset) f.get(null);
                            } catch (IllegalArgumentException | IllegalAccessException e) {
                                throw new RuntimeException(e);
                            }
                        })
                        .map(Charset::displayName)
                        .sorted())
                .toArray(String[]::new);
    }

    private static final long serialVersionUID = 233L;

    private static final Logger log = LoggerFactory.getLogger(PublisherSampler.class);

    // ++ These are JMX file names and must not be changed
    private static final String INPUT_FILE = "jms.input_file"; //$NON-NLS-1$

    private static final String RANDOM_PATH = "jms.random_path"; //$NON-NLS-1$

    private static final String TEXT_MSG = "jms.text_message"; //$NON-NLS-1$

    private static final String CONFIG_CHOICE = "jms.config_choice"; //$NON-NLS-1$

    private static final String MESSAGE_CHOICE = "jms.config_msg_type"; //$NON-NLS-1$

    private static final String NON_PERSISTENT_DELIVERY = "jms.non_persistent"; //$NON-NLS-1$

    private static final String JMS_PROPERTIES = "jms.jmsProperties"; // $NON-NLS-1$

    private static final String JMS_PRIORITY = "jms.priority"; // $NON-NLS-1$

    private static final String JMS_EXPIRATION = "jms.expiration"; // $NON-NLS-1$

    private static final String JMS_FILE_ENCODING = "jms.file_encoding"; // $NON-NLS-1$

    /** File extensions for text files **/
    private static final String[] TEXT_FILE_EXTS = { ".txt", ".obj" };
    /** File extensions for binary files **/
    private static final String[] BIN_FILE_EXTS = { ".dat" };

    // --

    // Does not need to be synch. because it is only accessed from the sampler
    // thread
    // The ClientPool does access it in a different thread, but ClientPool is
    // fully synch.
    private transient Publisher publisher = null;

    private static final FileServer FSERVER = FileServer.getFileServer();

    /** File cache handler **/
    private Cache<Object, Object> fileCache = null;

    /**
     * the implementation calls testStarted() without any parameters.
     */
    @Override
    public void testStarted(String test) {
        testStarted();
    }

    /**
     * the implementation calls testEnded() without any parameters.
     */
    @Override
    public void testEnded(String host) {
        testEnded();
    }

    /**
     * endTest cleans up the client
     */
    @Override
    public void testEnded() {
        log.debug("PublisherSampler.testEnded called");
        ClientPool.clearClient();
        InitialContextFactory.close();
    }

    @Override
    public void testStarted() {
    }

    /**
     * initialize the Publisher client.
     * 
     * @throws JMSException
     * @throws NamingException
     *
     */
    private void initClient() throws JMSException, NamingException {

        configureIsReconnectErrorCode();
        publisher = new Publisher(getUseJNDIPropertiesAsBoolean(), getJNDIInitialContextFactory(), getProviderUrl(),
                getConnectionFactory(), getDestination(), isUseAuth(), getUsername(), getPassword(),
                isDestinationStatic());
        ClientPool.addClient(publisher);
        log.debug("PublisherSampler.initClient called");
    }

    /**
     * The implementation will publish n messages within a for loop. Once n
     * messages are published, it sets the attributes of SampleResult.
     *
     * @return the populated sample result
     */
    @Override
    public SampleResult sample() {
        String configChoice = getConfigChoice();
        if (fileCache == null) {
            fileCache = buildCache(configChoice);
        }

        SampleResult result = new SampleResult();
        result.setSampleLabel(getName());
        result.setSuccessful(false); // Assume it will fail
        result.setResponseCode("000"); // ditto $NON-NLS-1$
        if (publisher == null) {
            try {
                initClient();
            } catch (JMSException | NamingException e) {
                result.sampleStart();
                result.sampleEnd();
                handleError(result, e, false);
                return result;
            }
        }
        StringBuilder buffer = new StringBuilder();
        StringBuilder propBuffer = new StringBuilder();
        int loop = getIterationCount();
        result.sampleStart();
        String type = getMessageChoice();

        try {
            Map<String, Object> msgProperties = getJMSProperties().getJmsPropertysAsMap();
            int deliveryMode = getUseNonPersistentDelivery() ? DeliveryMode.NON_PERSISTENT : DeliveryMode.PERSISTENT;
            int priority = Integer.parseInt(getPriority());
            long expiration = Long.parseLong(getExpiration());

            for (int idx = 0; idx < loop; idx++) {
                Message msg;
                if (JMSPublisherGui.TEXT_MSG_RSC.equals(type)) {
                    String tmsg = getRenderedContent(String.class, TEXT_FILE_EXTS);
                    msg = publisher.publish(tmsg, getDestination(), msgProperties, deliveryMode, priority, expiration);
                    buffer.append(tmsg);
                } else if (JMSPublisherGui.MAP_MSG_RSC.equals(type)) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> map = getRenderedContent(Map.class, TEXT_FILE_EXTS);
                    msg = publisher.publish(map, getDestination(), msgProperties, deliveryMode, priority, expiration);
                } else if (JMSPublisherGui.OBJECT_MSG_RSC.equals(type)) {
                    Serializable omsg = getRenderedContent(Serializable.class, TEXT_FILE_EXTS);
                    msg = publisher.publish(omsg, getDestination(), msgProperties, deliveryMode, priority, expiration);
                } else if (JMSPublisherGui.BYTES_MSG_RSC.equals(type)) {
                    byte[] bmsg = getRenderedContent(byte[].class, BIN_FILE_EXTS);
                    msg = publisher.publish(bmsg, getDestination(), msgProperties, deliveryMode, priority, expiration);
                } else {
                    throw new JMSException(type + " is not recognised");
                }
                Utils.messageProperties(propBuffer, msg);
            }
            result.setResponseCodeOK();
            result.setResponseMessage(loop + " messages published");
            result.setSuccessful(true);
            result.setSamplerData(buffer.toString());
            result.setSampleCount(loop);
            result.setRequestHeaders(propBuffer.toString());
        } catch (JMSException e) {
            handleError(result, e, true);
        } catch (Exception e) {
            handleError(result, e, false);
        } finally {
            result.sampleEnd();
        }
        return result;
    }

    /**
     * Fills in result and decide wether to reconnect or not depending on
     * checkForReconnect and underlying {@link JMSException#getErrorCode()}
     * 
     * @param result
     *            {@link SampleResult}
     * @param e
     *            {@link Exception}
     * @param checkForReconnect
     *            if true and exception is a {@link JMSException}
     */
    private void handleError(SampleResult result, Exception e, boolean checkForReconnect) {
        result.setSuccessful(false);
        result.setResponseMessage(e.toString());

        if (e instanceof JMSException) {
            JMSException jms = (JMSException) e;

            String errorCode = Optional.ofNullable(jms.getErrorCode()).orElse("");
            if (checkForReconnect && publisher != null && getIsReconnectErrorCode().test(errorCode)) {
                ClientPool.removeClient(publisher);
                IOUtils.closeQuietly(publisher);
                publisher = null;
            }

            result.setResponseCode(errorCode);
        }

        StringWriter writer = new StringWriter();
        e.printStackTrace(new PrintWriter(writer)); // NOSONAR We're getting it
                                                    // to put it in ResponseData
        result.setResponseData(writer.toString(), "UTF-8");
    }

    protected static Cache<Object, Object> buildCache(String configChoice) {
        Caffeine<Object, Object> cacheBuilder = Caffeine.newBuilder();
        switch (configChoice) {
        case JMSPublisherGui.USE_FILE_RSC:
            cacheBuilder.maximumSize(1);
            break;
        default:
            cacheBuilder.expireAfterWrite(0, TimeUnit.MILLISECONDS).maximumSize(0);
        }
        return cacheBuilder.build();
    }

    /** Gets file path to use **/
    private String getFilePath(String... ext) {
        switch (getConfigChoice()) {
        case JMSPublisherGui.USE_FILE_RSC:
            return getInputFile();
        case JMSPublisherGui.USE_RANDOM_RSC:
            return FSERVER.getRandomFile(getRandomPath(), ext).getAbsolutePath();
        default:
            throw new IllegalArgumentException("Type of input not handled:" + getConfigChoice());
        }
    }

    /**
     * Look-up renderer and get appropriate value
     *
     * @param type
     *            Message type to render
     * @param fileExts
     *            File extensions for directory mode.
     **/
    private <T> T getRenderedContent(Class<T> type, String[] fileExts) {
        MessageRenderer<T> renderer = Renderers.getInstance(type);
        if (getConfigChoice().equals(JMSPublisherGui.USE_TEXT_RSC)) {
            return renderer.getValueFromText(getTextMessage());
        } else {
            return renderer.getValueFromFile(getFilePath(fileExts), getFileEncoding(), !isRaw(), fileCache);
        }
    }

    /**
     * Specified if value must be parsed or not.
     * 
     * @return <code>true</code> if value must be sent as-is.
     */
    private boolean isRaw() {
        return RAW_DATA.equals(getFileEncoding());
    }

    // ------------- get/set properties ----------------------//
    /**
     * set the source of the message
     *
     * @param choice
     *            source of the messages. One of
     *            {@link JMSPublisherGui#USE_FILE_RSC},
     *            {@link JMSPublisherGui#USE_RANDOM_RSC} or
     *            JMSPublisherGui#USE_TEXT_RSC
     */
    public void setConfigChoice(String choice) {
        setProperty(CONFIG_CHOICE, choice);
    }

    // These static variables are only used to convert existing files
    private static final String USE_FILE_LOCALNAME = JMeterUtils.getResString(JMSPublisherGui.USE_FILE_RSC);
    private static final String USE_RANDOM_LOCALNAME = JMeterUtils.getResString(JMSPublisherGui.USE_RANDOM_RSC);

    /**
     * return the source of the message Converts from old JMX files which used
     * the local language string
     *
     * @return source of the messages
     */
    public String getConfigChoice() {
        // Allow for the old JMX file which used the local language string
        String config = getPropertyAsString(CONFIG_CHOICE);
        if (config.equals(USE_FILE_LOCALNAME) || config.equals(JMSPublisherGui.USE_FILE_RSC)) {
            return JMSPublisherGui.USE_FILE_RSC;
        }
        if (config.equals(USE_RANDOM_LOCALNAME) || config.equals(JMSPublisherGui.USE_RANDOM_RSC)) {
            return JMSPublisherGui.USE_RANDOM_RSC;
        }
        return config; // will be the 3rd option, which is not checked
                       // specifically
    }

    /**
     * set the type of the message
     *
     * @param choice
     *            type of the message (Text, Object, Map)
     */
    public void setMessageChoice(String choice) {
        setProperty(MESSAGE_CHOICE, choice);
    }

    /**
     * @return the type of the message (Text, Object, Map)
     *
     */
    public String getMessageChoice() {
        return getPropertyAsString(MESSAGE_CHOICE);
    }

    /**
     * set the input file for the publisher
     *
     * @param file
     *            input file for the publisher
     */
    public void setInputFile(String file) {
        setProperty(INPUT_FILE, file);
    }

    /**
     * @return the path of the input file
     *
     */
    public String getInputFile() {
        return getPropertyAsString(INPUT_FILE);
    }

    /**
     * set the random path for the messages
     *
     * @param path
     *            random path for the messages
     */
    public void setRandomPath(String path) {
        setProperty(RANDOM_PATH, path);
    }

    /**
     * @return the random path for messages
     *
     */
    public String getRandomPath() {
        return getPropertyAsString(RANDOM_PATH);
    }

    /**
     * set the text for the message
     *
     * @param message
     *            text for the message
     */
    public void setTextMessage(String message) {
        setProperty(TEXT_MSG, message);
    }

    /**
     * @return the text for the message
     *
     */
    public String getTextMessage() {
        return getPropertyAsString(TEXT_MSG);
    }

    public String getExpiration() {
        String expiration = getPropertyAsString(JMS_EXPIRATION);
        if (expiration.length() == 0) {
            return Utils.DEFAULT_NO_EXPIRY;
        } else {
            return expiration;
        }
    }

    public String getPriority() {
        String priority = getPropertyAsString(JMS_PRIORITY);
        if (priority.length() == 0) {
            return Utils.DEFAULT_PRIORITY_4;
        } else {
            return priority;
        }
    }

    public void setPriority(String s) {
        // Bug 59173
        if (Utils.DEFAULT_PRIORITY_4.equals(s)) {
            s = ""; // $NON-NLS-1$ make sure the default is not saved explicitly
        }
        setProperty(JMS_PRIORITY, s); // always need to save the field
    }

    public void setExpiration(String s) {
        // Bug 59173
        if (Utils.DEFAULT_NO_EXPIRY.equals(s)) {
            s = ""; // $NON-NLS-1$ make sure the default is not saved explicitly
        }
        setProperty(JMS_EXPIRATION, s); // always need to save the field
    }

    /**
     * @param value
     *            boolean use NON_PERSISTENT
     */
    public void setUseNonPersistentDelivery(boolean value) {
        setProperty(NON_PERSISTENT_DELIVERY, value, false);
    }

    /**
     * @return true if NON_PERSISTENT delivery must be used
     */
    public boolean getUseNonPersistentDelivery() {
        return getPropertyAsBoolean(NON_PERSISTENT_DELIVERY, false);
    }

    /**
     * @return {@link JMSProperties} JMS Properties
     */
    public JMSProperties getJMSProperties() {
        Object o = getProperty(JMS_PROPERTIES).getObjectValue();
        JMSProperties jmsProperties = null;
        // Backward compatibility with versions <= 2.10
        if (o instanceof Arguments) {
            jmsProperties = Utils.convertArgumentsToJmsProperties((Arguments) o);
        } else {
            jmsProperties = (JMSProperties) o;
        }
        if (jmsProperties == null) {
            jmsProperties = new JMSProperties();
            setJMSProperties(jmsProperties);
        }
        return jmsProperties;
    }

    /**
     * @param jmsProperties
     *            JMS Properties
     */
    public void setJMSProperties(JMSProperties jmsProperties) {
        setProperty(new TestElementProperty(JMS_PROPERTIES, jmsProperties));
    }

    /**
     * Gets file encoding to use. If {@link #RAW_DATA}, content isn't parsed.
     * 
     * @return File encoding.
     * @see #RAW_DATA
     * @see #DEFAULT_ENCODING
     * @see #getSupportedEncodings()
     */
    public String getFileEncoding() {
        return getPropertyAsString(JMS_FILE_ENCODING, RAW_DATA);
    }

    /**
     * Sets file encoding to use. If {@link #RAW_DATA}, content isn't parsed.
     * 
     * @param fileEncoding
     *            File encoding.
     * @see #RAW_DATA
     * @see #DEFAULT_ENCODING
     * @see #getSupportedEncodings()
     */
    public void setFileEncoding(String fileEncoding) {
        setProperty(JMS_FILE_ENCODING, fileEncoding, RAW_DATA);
    }
}
