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
import org.apache.jmeter.engine.event.LoopIterationEvent;

import org.apache.jmeter.protocol.jms.control.gui.JMSPublisherGui;
import org.apache.jmeter.protocol.jms.client.ClientPool;
import org.apache.jmeter.protocol.jms.client.Publisher;

import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * @author pete
 * 
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class PublisherSampler extends BaseJMSSampler implements TestListener {

	public static final String INPUT_FILE = "jms.input_file"; //$NON-NLS-1$

	public static final String RANDOM_PATH = "jms.random_path"; //$NON-NLS-1$

	public static final String TEXT_MSG = "jms.text_message"; //$NON-NLS-1$

	public static final String CONFIG_CHOICE = "jms.config_choice"; //$NON-NLS-1$

	public static final String MESSAGE_CHOICE = "jms.config_msg_type"; //$NON-NLS-1$

	private Publisher PUB = null;

	private StringBuffer BUFFER = new StringBuffer();

	private static FileServer FSERVER = FileServer.getFileServer();

	private String file_contents = null;

	static Logger log = LoggingManager.getLoggerForClass();

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
		log.info("PublisherSampler.testEnded called");
		Thread.currentThread().interrupt();
		this.PUB = null;
		this.BUFFER.setLength(0);
		this.BUFFER = null;
		ClientPool.clearClient();
	}

	/**
	 * the implementation creates a new StringBuffer
	 */
	public void testStarted() {
		this.BUFFER = new StringBuffer();
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
	public synchronized void initClient() {
		this.PUB = new Publisher(this.getUseJNDIPropertiesAsBoolean(), this.getJNDIInitialContextFactory(), this
				.getProviderUrl(), this.getConnectionFactory(), this.getTopic(), this.getUseAuth(), this.getUsername(),
				this.getPassword());
		ClientPool.addClient(this.PUB);
		log.info("PublisherSampler.initClient called");
	}

	/**
	 * The implementation calls sample() without any parameters
	 */
	public SampleResult sample(Entry e) {
		return this.sample();
	}

	/**
	 * The implementation will publish n messages within a for loop. Once n
	 * messages are published, it sets the attributes of SampleResult.
	 * 
	 * @return
	 */
	public SampleResult sample() {
		SampleResult result = new SampleResult();
		result.setSampleLabel(getName());
		if (this.PUB == null) {
			this.initClient();
		}
		int loop = this.getIterationCount();
		if (this.PUB != null) {
			result.sampleStart();
			for (int idx = 0; idx < loop; idx++) {
				String tmsg = this.getMessageContent();
				this.PUB.publish(tmsg);
				this.BUFFER.append(tmsg);
			}
			result.sampleEnd();
			String content = this.BUFFER.toString();
			result.setBytes(content.getBytes().length);
			result.setResponseCode("message published successfully");
			result.setResponseMessage(loop + " messages published");
			result.setSuccessful(true);
			result.setResponseData(content.getBytes());
			result.setSampleCount(loop);
			this.BUFFER.setLength(0);
		}
		return result;
	}

	/**
	 * Method will check the setting and get the contents for the message.
	 * 
	 * @return
	 */
	public String getMessageContent() {
		if (this.getConfigChoice().equals(JMSPublisherGui.use_file)) {
			// in the case the test uses a file, we set it locally and
			// prevent loading the file repeatedly
			if (this.file_contents == null) {
				this.file_contents = this.getFileContent(this.getInputFile());
			}
			return this.file_contents;
		} else if (this.getConfigChoice().equals(JMSPublisherGui.use_random)) {
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
	 * @return
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

	/**
	 * return the config choice
	 * 
	 * @return
	 */
	public String getConfigChoice() {
		return getPropertyAsString(CONFIG_CHOICE);
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
	 * @return
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
	 * @return
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
	 * @return
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
	 * @return
	 */
	public String getTextMessage() {
		return getPropertyAsString(TEXT_MSG);
	}
}
