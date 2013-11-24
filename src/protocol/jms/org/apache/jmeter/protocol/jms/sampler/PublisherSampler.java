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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

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
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.services.FileServer;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.io.TextFile;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.util.JOrphanUtils;
import org.apache.log.Logger;

import com.thoughtworks.xstream.XStream;

/**
 * This class implements the JMS Publisher sampler.
 */
public class PublisherSampler extends BaseJMSSampler implements TestStateListener {

    private static final long serialVersionUID = 233L;

    private static final Logger log = LoggingManager.getLoggerForClass();

    //++ These are JMX file names and must not be changed
    private static final String INPUT_FILE = "jms.input_file"; //$NON-NLS-1$

    private static final String RANDOM_PATH = "jms.random_path"; //$NON-NLS-1$

    private static final String TEXT_MSG = "jms.text_message"; //$NON-NLS-1$

    private static final String CONFIG_CHOICE = "jms.config_choice"; //$NON-NLS-1$

    private static final String MESSAGE_CHOICE = "jms.config_msg_type"; //$NON-NLS-1$
    
    private static final String NON_PERSISTENT_DELIVERY = "jms.non_persistent"; //$NON-NLS-1$
    
    private static final String JMS_PROPERTIES = "jms.jmsProperties"; // $NON-NLS-1$

    //--

    // Does not need to be synch. because it is only accessed from the sampler thread
    // The ClientPool does access it in a different thread, but ClientPool is fully synch.
    private transient Publisher publisher = null;

    private static final FileServer FSERVER = FileServer.getFileServer();

    // Cache for file. Only used by sample() in a single thread
    private String file_contents = null;
    // Cache for object-message, only used when parsing from a file because in text-area
    // property replacement might have been used
    private Serializable object_msg_file_contents = null;
    // Cache for bytes-message, only used when parsing from a file 
    private byte[] bytes_msg_file_contents = null;

    public PublisherSampler() {
    }

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
     * @throws JMSException 
     * @throws NamingException 
     *
     */
    private void initClient() throws JMSException, NamingException {
        publisher = new Publisher(getUseJNDIPropertiesAsBoolean(), getJNDIInitialContextFactory(), 
                getProviderUrl(), getConnectionFactory(), getDestination(), isUseAuth(), getUsername(),
                getPassword(), isDestinationStatic(), getUseNonPersistentDelivery());
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
        SampleResult result = new SampleResult();
        result.setSampleLabel(getName());
        result.setSuccessful(false); // Assume it will fail
        result.setResponseCode("000"); // ditto $NON-NLS-1$
        if (publisher == null) {
            try {
                initClient();
            } catch (JMSException e) {
                result.setResponseMessage(e.toString());
                return result;
            } catch (NamingException e) {
                result.setResponseMessage(e.toString());
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
            for (int idx = 0; idx < loop; idx++) {
                if (JMSPublisherGui.TEXT_MSG_RSC.equals(type)){
                    String tmsg = getMessageContent();
                    Message msg = publisher.publish(tmsg, getDestination(), msgProperties);
                    buffer.append(tmsg);
                    Utils.messageProperties(propBuffer, msg);
                } else if (JMSPublisherGui.MAP_MSG_RSC.equals(type)){
                    Map<String, Object> m = getMapContent();
                    Message msg = publisher.publish(m, getDestination(), msgProperties);
                    Utils.messageProperties(propBuffer, msg);
                } else if (JMSPublisherGui.OBJECT_MSG_RSC.equals(type)){
                    Serializable omsg = getObjectContent();
                    Message msg = publisher.publish(omsg, getDestination(), msgProperties);
                    Utils.messageProperties(propBuffer, msg);
                } else if (JMSPublisherGui.BYTES_MSG_RSC.equals(type)){
                    byte[] bmsg = getBytesContent();
                    Message msg = publisher.publish(bmsg, getDestination(), msgProperties);
                    Utils.messageProperties(propBuffer, msg);
                } else {
                    throw new JMSException(type+ " is not recognised");                    
                }
            }
            result.setResponseCodeOK();
            result.setResponseMessage(loop + " messages published");
            result.setSuccessful(true);
            result.setSamplerData(buffer.toString());
            result.setSampleCount(loop);
            result.setRequestHeaders(propBuffer.toString());
        } catch (Exception e) {
            result.setResponseMessage(e.toString());
        } finally {
            result.sampleEnd();            
        }
        return result;
    }

    private Map<String, Object> getMapContent() throws ClassNotFoundException, SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        Map<String,Object> m = new HashMap<String,Object>();
        String text = getMessageContent();
        String[] lines = text.split("\n");
        for (String line : lines){
            String[] parts = line.split(",",3);
            if (parts.length != 3) {
                throw new IllegalArgumentException("line must have 3 parts: "+line);
            }
            String name = parts[0];
            String type = parts[1];
            if (!type.contains(".")){// Allow shorthand names
                type = "java.lang."+type;
            }
            String value = parts[2];
            Object obj;
            if (type.equals("java.lang.String")){
                obj = value;
            } else {
                Class <?> clazz = Class.forName(type);
                Method method = clazz.getMethod("valueOf", new Class<?>[]{String.class});
                obj = method.invoke(clazz, value);                
            }
            m.put(name, obj);
        }
        return m;
    }

    /**
     * Method will check the setting and get the contents for the message.
     *
     * @return the contents for the message
     */
    private String getMessageContent() {
        if (getConfigChoice().equals(JMSPublisherGui.USE_FILE_RSC)) {
            // in the case the test uses a file, we set it locally and
            // prevent loading the file repeatedly
            if (file_contents == null) {
                file_contents = getFileContent(getInputFile());
            }
            return file_contents;
        } else if (getConfigChoice().equals(JMSPublisherGui.USE_RANDOM_RSC)) {
            // Maybe we should consider creating a global cache for the
            // random files to make JMeter more efficient.
            String fname = FSERVER.getRandomFile(getRandomPath(), new String[] { ".txt", ".obj" })
                    .getAbsolutePath();
            return getFileContent(fname);
        } else {
            return getTextMessage();
        }
    }

    /**
     * The implementation uses TextFile to load the contents of the file and
     * returns a string.
     *
     * @param path
     * @return the contents of the file
     */
    public String getFileContent(String path) {
        TextFile tf = new TextFile(path);
        return tf.getText();
    }

    /**
     * This method will load the contents for the JMS Object Message.
     * The contents are either loaded from file (might be cached), random file
     * or from the GUI text-area.
     * 
     * @return Serialized object as loaded from the specified input file
     */
    private Serializable getObjectContent() {
        if (getConfigChoice().equals(JMSPublisherGui.USE_FILE_RSC)) {
            // in the case the test uses a file, we set it locally and
            // prevent loading the file repeatedly
            if (object_msg_file_contents == null) {
                object_msg_file_contents = getFileObjectContent(getInputFile());
            }

            return object_msg_file_contents;
        } else if (getConfigChoice().equals(JMSPublisherGui.USE_RANDOM_RSC)) {
            // Maybe we should consider creating a global cache for the
            // random files to make JMeter more efficient.
            final String fname = FSERVER.getRandomFile(getRandomPath(), new String[] {".txt", ".obj"})
                .getAbsolutePath();

            return getFileObjectContent(fname);
        } else {
            final String xmlMessage = getTextMessage();
            return transformXmlToObjectMessage(xmlMessage);
        }
    }
    
    /**
     * This method will load the contents for the JMS BytesMessage.
     * The contents are either loaded from file (might be cached), random file
     * 
     * @return byte[] as loaded from the specified input file
     * @since 2.9
     */
    private  byte[] getBytesContent() {
        if (getConfigChoice().equals(JMSPublisherGui.USE_FILE_RSC)) {
            // in the case the test uses a file, we set it locally and
            // prevent loading the file repeatedly
            if (bytes_msg_file_contents == null) {
                bytes_msg_file_contents = getFileBytesContent(getInputFile());
            }

            return bytes_msg_file_contents;
        } else if (getConfigChoice().equals(JMSPublisherGui.USE_RANDOM_RSC)) {
            final String fname = FSERVER.getRandomFile(getRandomPath(), new String[] {".dat"})
                .getAbsolutePath();

            return getFileBytesContent(fname);
        } else {
            throw new IllegalArgumentException("Type of input not handled:" + getConfigChoice());
        }
    }
    
    /**
     * Try to load an object from a provided file, so that it can be used as body
     * for a JMS message.
     * An {@link IllegalStateException} will be thrown if loading the object fails.
     * 
     * @param path Path to the file that will be serialized
     * @return byte[]  instance
     * @since 2.9
     */
    private static byte[] getFileBytesContent(final String path) {
        InputStream inputStream = null;
        try {
            File file = new File(path);
            inputStream = new BufferedInputStream(new FileInputStream(file));
            return IOUtils.toByteArray(inputStream, (int)file.length());
        } catch (Exception e) {
            log.error(e.getLocalizedMessage(), e);
            throw new IllegalStateException("Unable to load file", e);
        } finally {
            JOrphanUtils.closeQuietly(inputStream);
        }
    }
    
    /**
     * Try to load an object from a provided file, so that it can be used as body
     * for a JMS message.
     * An {@link IllegalStateException} will be thrown if loading the object fails.
     * 
     * @param path Path to the file that will be serialized
     * @return Serialized object instance
     */
    private static Serializable getFileObjectContent(final String path) {
      Serializable readObject = null;
      InputStream inputStream = null;
      try {
          inputStream = new BufferedInputStream(new FileInputStream(path));
          XStream xstream = new XStream();
        readObject = (Serializable) xstream.fromXML(inputStream, readObject);
      } catch (Exception e) {
          log.error(e.getLocalizedMessage(), e);
          throw new IllegalStateException("Unable to load object instance from file", e);
      } finally {
          JOrphanUtils.closeQuietly(inputStream);
      }
      return readObject;
    }
    
    /**
     * Try to load an object via XStream from XML text, so that it can be used as body
     * for a JMS message.
     * An {@link IllegalStateException} will be thrown if transforming the XML to an object fails.
     *
     * @param xmlMessage String containing XML text as input for the transformation
     * @return Serialized object instance
     */
    private static Serializable transformXmlToObjectMessage(final String xmlMessage) {
      Serializable readObject = null;
      try {
          XStream xstream = new XStream();
          readObject = (Serializable) xstream.fromXML(xmlMessage, readObject);
      } catch (Exception e) {
          log.error(e.getLocalizedMessage(), e);
          throw new IllegalStateException("Unable to load object instance from text", e);
      }
      return readObject;
    }
    
    // ------------- get/set properties ----------------------//
    /**
     * set the source of the message
     *
     * @param choice
     */
    public void setConfigChoice(String choice) {
        setProperty(CONFIG_CHOICE, choice);
    }

    // These static variables are only used to convert existing files
    private static final String USE_FILE_LOCALNAME = JMeterUtils.getResString(JMSPublisherGui.USE_FILE_RSC);
    private static final String USE_RANDOM_LOCALNAME = JMeterUtils.getResString(JMSPublisherGui.USE_RANDOM_RSC);

    /**
     * return the source of the message
     * Converts from old JMX files which used the local language string
     */
    public String getConfigChoice() {
        // Allow for the old JMX file which used the local language string
        String config = getPropertyAsString(CONFIG_CHOICE);
        if (config.equals(USE_FILE_LOCALNAME) 
         || config.equals(JMSPublisherGui.USE_FILE_RSC)){
            return JMSPublisherGui.USE_FILE_RSC;
        }
        if (config.equals(USE_RANDOM_LOCALNAME)
         || config.equals(JMSPublisherGui.USE_RANDOM_RSC)){
            return JMSPublisherGui.USE_RANDOM_RSC;
        }
        return config; // will be the 3rd option, which is not checked specifically
    }

    /**
     * set the type of the message
     *
     * @param choice
     */
    public void setMessageChoice(String choice) {
        setProperty(MESSAGE_CHOICE, choice);
    }

    /**
     * return the type of the message (Text, Object, Map)
     *
     */
    public String getMessageChoice() {
        return getPropertyAsString(MESSAGE_CHOICE);
    }

    /**
     * set the input file for the publisher
     *
     * @param file
     */
    public void setInputFile(String file) {
        setProperty(INPUT_FILE, file);
    }

    /**
     * return the path of the input file
     *
     */
    public String getInputFile() {
        return getPropertyAsString(INPUT_FILE);
    }

    /**
     * set the random path for the messages
     *
     * @param path
     */
    public void setRandomPath(String path) {
        setProperty(RANDOM_PATH, path);
    }

    /**
     * return the random path for messages
     *
     */
    public String getRandomPath() {
        return getPropertyAsString(RANDOM_PATH);
    }

    /**
     * set the text for the message
     *
     * @param message
     */
    public void setTextMessage(String message) {
        setProperty(TEXT_MSG, message);
    }

    /**
     * return the text for the message
     *
     */
    public String getTextMessage() {
        return getPropertyAsString(TEXT_MSG);
    }

    /**
     * @param value boolean use NON_PERSISTENT
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
        if(o instanceof Arguments) {
            jmsProperties = Utils.convertArgumentsToJmsProperties((Arguments)o);
        } else {
            jmsProperties = (JMSProperties) o;
        }
        if(jmsProperties == null) {
            jmsProperties = new JMSProperties();
            setJMSProperties(jmsProperties);
        }
        return jmsProperties;
    }
    
    /**
     * @param jmsProperties JMS Properties
     */
    public void setJMSProperties(JMSProperties jmsProperties) {
        setProperty(new TestElementProperty(JMS_PROPERTIES, jmsProperties));
    }
}
