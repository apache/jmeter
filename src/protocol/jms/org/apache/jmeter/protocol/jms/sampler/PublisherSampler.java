/*
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestListener;
import org.apache.jmeter.engine.event.LoopIterationEvent;

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
public class PublisherSampler
    extends BaseJMSSampler
    implements TestListener {

	public static final String INPUT_FILE = "jms.input_file";
	public static final String RANDOM_PATH = "jms.random_path";
	public static final String TEXT_MSG = "jms.text_message";
	public static final String CONFIG_CHOICE = "jms.config_choice";
	public static final String MESSAGE_CHOICE = "jms.config_msg_type";
	
	private Publisher PUB = null;
	private StringBuffer BUFFER = new StringBuffer();
	static Logger log = LoggingManager.getLoggerForClass();
	
	public PublisherSampler(){
	}
	
	public void testStarted(String test){
		testStarted();
	}
	
	public void testEnded(String test){
		testEnded();
	}

    /**
     * endTest cleans up the client
     * @see junit.framework.TestListener#endTest(junit.framework.Test)
     */
    public void testEnded() {
		log.info("PublisherSampler.testEnded called");
		Thread.currentThread().interrupt();
		this.PUB = null;
		this.BUFFER.setLength(0);
		this.BUFFER = null;
		ClientPool.clearClient();
		try {
			this.finalize();
		} catch (Throwable e){
			log.error(e.getMessage());
		}
    }

    /**
     * startTest sets up the client and gets it ready for the
     * test. Since async messaging is different than request/
     * response applications, the connection is created at the
     * beginning of the test and closed at the end of the test.
     */
    public void testStarted() {
		this.BUFFER = new StringBuffer();
    }

	public void testIterationStart(LoopIterationEvent event){
		
	}

    public synchronized void initClient() {
        this.PUB =
            new Publisher(
                this.getUseJNDIPropertiesAsBoolean(),
                this.getJNDIInitialContextFactory(),
                this.getProviderUrl(),
                this.getConnectionFactory(),
                this.getTopic(),
                this.getUseAuth(),
                this.getUsername(),
                this.getPassword());
        ClientPool.addClient(this.PUB);
        log.info("PublisherSampler.initClient called");
    }
	
    /* (non-Javadoc)
     * @see org.apache.jmeter.samplers.Sampler#sample(org.apache.jmeter.samplers.Entry)
     */
    public SampleResult sample(Entry e) {
        return this.sample();
    }
    
    public SampleResult sample(){
    	SampleResult result = new SampleResult();
    	result.setSampleLabel("PublisherSampler:" + this.getTopic());
        if (this.PUB == null) {
            this.initClient();
        }
        int loop = this.getIterationCount();
        if (this.PUB != null){
			result.sampleStart();
			for (int idx = 0; idx < loop; idx++) {
				this.PUB.publish(this.getTextMessage());
			}
			result.sampleEnd();
			// since each call to sampler.sample() uses the same message
			// it's better to append the messages after the messages are
			// sent to get a more accurate measurement
			for (int idx = 0; idx < loop; idx++) {
				this.BUFFER.append(this.getTextMessage());
			}
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

	//-------------  get/set properties ----------------------//
	/**
	 * set the config choice
	 * @param choice
	 */	
	public void setConfigChoice(String choice){
		setProperty(CONFIG_CHOICE,choice);
	}
	
	/**
	 * return the config choice
	 * @return
	 */
	public String getConfigChoice(){
		return getPropertyAsString(CONFIG_CHOICE);
	}

	public void setMessageChoice(String choice){
		setProperty(MESSAGE_CHOICE,choice);
	}
	
	public String getMessageChoice(){
		return getPropertyAsString(MESSAGE_CHOICE);
	}
	
	public void setInputFile(String file){
		setProperty(INPUT_FILE,file);	
	}
	
	public String getInputFile(){
		return getPropertyAsString(INPUT_FILE);
	}
	
	public void setRandomPath(String path){
		setProperty(RANDOM_PATH,path);
	}
	
	public String getRandomPath(){
		return getPropertyAsString(RANDOM_PATH);
	}
	
	public void setTextMessage(String message){
		setProperty(TEXT_MSG,message);
	}
	
	public String getTextMessage(){
		return getPropertyAsString(TEXT_MSG);
	}
}
