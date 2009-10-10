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

import org.apache.jorphan.io.TextFile;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.services.FileServer;
import org.apache.jmeter.testelement.TestListener;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.engine.event.LoopIterationEvent;

import org.apache.jmeter.protocol.jms.control.gui.JMSPublisherGui;
import org.apache.jmeter.protocol.jms.client.ClientPool;
import org.apache.jmeter.protocol.jms.client.Publisher;

import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * This class implements the JMS Publisher sampler.
 */
public class PublisherSampler extends BaseJMSSampler implements TestListener {

    private static final long serialVersionUID = 233L;

    private static final Logger log = LoggingManager.getLoggerForClass();

    //++ These are JMX file names and must not be changed
    private static final String INPUT_FILE = "jms.input_file"; //$NON-NLS-1$

    private static final String RANDOM_PATH = "jms.random_path"; //$NON-NLS-1$

    private static final String TEXT_MSG = "jms.text_message"; //$NON-NLS-1$

    private static final String CONFIG_CHOICE = "jms.config_choice"; //$NON-NLS-1$

    private static final String MESSAGE_CHOICE = "jms.config_msg_type"; //$NON-NLS-1$
    //--

    // Does not need to be synch. because it is only accessed from the sampler thread
    // The ClientPool does access it in a different thread, but ClientPool is fully synch.
    private transient Publisher PUB = null; // TODO URGENT probably needs to be synch.

    private static final FileServer FSERVER = FileServer.getFileServer();

    // Cache for file. Only used by sample() in a single thread
    private String file_contents = null;

    public PublisherSampler() {
    }

    /**
     * the implementation calls testStarted() without any parameters.
     */
    public void testStarted(String test) {
        testStarted();
    }

    /**
     * the implementation calls testEnded() without any parameters.
     */
    public void testEnded(String test) {
        testEnded();
    }

    /**
     * endTest cleans up the client
     *
     * @see junit.framework.TestListener#endTest(junit.framework.Test)
     */
    public void testEnded() {
        log.debug("PublisherSampler.testEnded called");
        ClientPool.clearClient();
    }

    public void testStarted() {
    }

    /**
     * NO implementation provided for the sampler. It is necessary in this case.
     */
    public void testIterationStart(LoopIterationEvent event) {
    }

    /**
     * initialize the Publisher client.
     *
     */
    private void initClient() {
        this.PUB = new Publisher(this.getUseJNDIPropertiesAsBoolean(), this.getJNDIInitialContextFactory(), this
                .getProviderUrl(), this.getConnectionFactory(), this.getTopic(), this.isUseAuth(), this.getUsername(),
                this.getPassword());
        ClientPool.addClient(this.PUB);
        log.debug("PublisherSampler.initClient called");
    }

    /**
     * The implementation calls sample() without any parameters
     */
    @Override
    public SampleResult sample(Entry e) {
        return this.sample();
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
        if (this.PUB == null) {
            this.initClient();
        }
        StringBuilder buffer = new StringBuilder();
        int loop = this.getIterationCount();
        if (this.PUB != null) {
            result.sampleStart();
            for (int idx = 0; idx < loop; idx++) {
                String tmsg = this.getMessageContent();
                this.PUB.publish(tmsg);
                buffer.append(tmsg);
            }
            result.sampleEnd();
            String content = buffer.toString();
            result.setBytes(content.getBytes().length);
            result.setResponseCode("message published successfully");
            result.setResponseMessage(loop + " messages published");
            result.setSuccessful(true);
            result.setResponseData(content.getBytes());
            result.setSampleCount(loop);
        }
        return result;
    }

    /**
     * Method will check the setting and get the contents for the message.
     *
     * @return the contents for the message
     */
    private String getMessageContent() {
        if (this.getConfigChoice().equals(JMSPublisherGui.USE_FILE_RSC)) {
            // in the case the test uses a file, we set it locally and
            // prevent loading the file repeatedly
            if (this.file_contents == null) {
                this.file_contents = this.getFileContent(this.getInputFile());
            }
            return this.file_contents;
        } else if (this.getConfigChoice().equals(JMSPublisherGui.USE_RANDOM_RSC)) {
            // Maybe we should consider creating a global cache for the
            // random files to make JMeter more efficient.
            String fname = FSERVER.getRandomFile(this.getRandomPath(), new String[] { ".txt", ".obj" })
                    .getAbsolutePath();
            return this.getFileContent(fname);
        } else {
            return this.getTextMessage();
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

    // ------------- get/set properties ----------------------//
    /**
     * set the config choice
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
     * return the config choice
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
     * set the source of the message
     *
     * @param choice
     */
    public void setMessageChoice(String choice) {
        setProperty(MESSAGE_CHOICE, choice);
    }

    /**
     * return the source of the message
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
}
