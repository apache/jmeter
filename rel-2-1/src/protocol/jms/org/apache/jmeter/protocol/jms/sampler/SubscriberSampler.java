/*
 * Copyright 2004-2005 The Apache Software Foundation.
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

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestListener;
import org.apache.jmeter.engine.event.LoopIterationEvent;

import org.apache.jmeter.protocol.jms.control.gui.JMSSubscriberGui;
import org.apache.jmeter.protocol.jms.client.ClientPool;
import org.apache.jmeter.protocol.jms.client.OnMessageSubscriber;
import org.apache.jmeter.protocol.jms.client.ReceiveSubscriber;

import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * @author pete
 * 
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class SubscriberSampler extends BaseJMSSampler implements TestListener, MessageListener {

	// private Subscriber SUBSCRIBER = null;
	static Logger log = LoggingManager.getLoggerForClass();

	private transient ReceiveSubscriber SUBSCRIBER = null;

	private StringBuffer BUFFER = new StringBuffer();

	private transient int counter = 0;

	private transient int loop = 0;

	private transient boolean RUN = true;

	public static final String CLIENT_CHOICE = "jms.client_choice"; // $NON-NLS-1$

	public SubscriberSampler() {
	}

	public void testEnded(String test) {
		testEnded();
	}

	public void testStarted(String test) {
		testStarted();
	}

	/**
	 * testEnded is called by Jmeter's engine. the implementation will reset the
	 * count, set RUN to false and clear the StringBuffer.
	 */
	public synchronized void testEnded() {
		log.info("SubscriberSampler.testEnded called");
		this.RUN = false;
		this.resetCount();
		ClientPool.clearClient();
		this.BUFFER = null;
		if (this.SUBSCRIBER != null) {
			this.SUBSCRIBER = null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestListener#startTest(junit.framework.Test)
	 */
	public void testStarted() {
	}

	public void testIterationStart(LoopIterationEvent event) {
	}

	/**
	 * Create the OnMessageSubscriber client and set the sampler as the message
	 * listener.
	 * 
	 * @return
	 */
	public synchronized OnMessageSubscriber initListenerClient() {
		OnMessageSubscriber sub = (OnMessageSubscriber) ClientPool.get(this);
		if (sub == null) {
			sub = new OnMessageSubscriber(this.getUseJNDIPropertiesAsBoolean(), this.getJNDIInitialContextFactory(),
					this.getProviderUrl(), this.getConnectionFactory(), this.getTopic(), this.getUseAuth(), this
							.getUsername(), this.getPassword());
			sub.setMessageListener(this);
			sub.resume();
			ClientPool.addClient(sub);
			ClientPool.put(this, sub);
			log.info("SubscriberSampler.initListenerClient called");
			log.info("loop count " + this.getIterations());
		}
		this.RUN = true;
		return sub;
	}

	/**
	 * Create the ReceiveSubscriber client for the sampler.
	 */
	public void initReceiveClient() {
		this.SUBSCRIBER = new ReceiveSubscriber(this.getUseJNDIPropertiesAsBoolean(), this
				.getJNDIInitialContextFactory(), this.getProviderUrl(), this.getConnectionFactory(), this.getTopic(),
				this.getUseAuth(), this.getUsername(), this.getPassword());
		this.SUBSCRIBER.resume();
		ClientPool.addClient(this.SUBSCRIBER);
		log.info("SubscriberSampler.initReceiveClient called");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jmeter.samplers.Sampler#sample(org.apache.jmeter.samplers.Entry)
	 */
	public SampleResult sample(Entry e) {
		return this.sample();
	}

	/**
	 * sample method will check which client it should use and call the
	 * appropriate client specific sample method.
	 * 
	 * @return
	 */
	public SampleResult sample() {
		if (this.getClientChoice().equals(JMSSubscriberGui.receive_str)) {
			return sampleWithReceive();
		} else {
			return sampleWithListener();
		}
	}

	/**
	 * sample will block until messages are received
	 * 
	 * @return
	 */
	public SampleResult sampleWithListener() {
		SampleResult result = new SampleResult();
		result.setSampleLabel(getName());
		OnMessageSubscriber sub = initListenerClient();

		this.loop = this.getIterationCount();

		result.sampleStart();
		while (this.RUN && this.count(0) < this.loop) {
			try {
				Thread.sleep(0, 50);
			} catch (Exception e) {
				log.info(e.getMessage());
			}
		}
		result.sampleEnd();
		result.setResponseMessage(loop + " samples messages recieved");
		if (this.getReadResponseAsBoolean()) {
			result.setResponseData(this.BUFFER.toString().getBytes());
		} else {
			result.setBytes(this.BUFFER.toString().getBytes().length);
		}
		result.setSuccessful(true);
		result.setResponseCode(loop + " message(s) recieved successfully");
		result.setSamplerData("Not applicable");
		result.setSampleCount(loop);

		this.resetCount();
		return result;
	}

	/**
	 * Sample method uses the ReceiveSubscriber client instead of onMessage
	 * approach.
	 * 
	 * @return
	 */
	public SampleResult sampleWithReceive() {
		SampleResult result = new SampleResult();
		result.setSampleLabel(getName());
		if (this.SUBSCRIBER == null) {
			this.initReceiveClient();
			this.SUBSCRIBER.start();
		}
		this.loop = this.getIterationCount();
		this.SUBSCRIBER.setLoop(this.loop);

		result.sampleStart();
		while (this.SUBSCRIBER.count(0) < this.loop) {
			try {
				Thread.sleep(0, 50);
			} catch (Exception e) {
				log.info(e.getMessage());
			}
		}
		result.sampleEnd();
		result.setResponseMessage(loop + " samples messages recieved");
		if (this.getReadResponseAsBoolean()) {
			result.setResponseData(this.SUBSCRIBER.getMessage().getBytes());
		} else {
			result.setBytes(this.SUBSCRIBER.getMessage().getBytes().length);
		}
		result.setSuccessful(true);
		result.setResponseCode(loop + " message(s) recieved successfully");
		result.setSamplerData("Not applicable");
		result.setSampleCount(this.loop);

		this.SUBSCRIBER.clear();
		this.SUBSCRIBER.resetCount();
		return result;
	}

	/**
	 * The sampler implements MessageListener directly and sets itself as the
	 * listener with the TopicSubscriber.
	 */
	public synchronized void onMessage(Message message) {
		try {
			if (message instanceof TextMessage) {
				TextMessage msg = (TextMessage) message;
				String content = msg.getText();
				if (content != null) {
					this.BUFFER.append(content);
					count(1);
				}
			}
		} catch (JMSException e) {
			log.error(e.getMessage());
		}
	}

	/**
	 * increment the count and return the new value.
	 * 
	 * @param count
	 * @return
	 */
	public synchronized int count(int count) {
		this.counter += count;
		return this.counter;
	}

	/**
	 * resetCount will set the counter to zero and set the length of the
	 * StringBuffer to zero.
	 */
	public synchronized void resetCount() {
		this.counter = 0;
		this.BUFFER.setLength(0);
	}

	// ----------- get/set methods ------------------- //
	/**
	 * Set the client choice. There are two options: ReceiveSusbscriber and
	 * OnMessageSubscriber.
	 */
	public void setClientChoice(String choice) {
		setProperty(CLIENT_CHOICE, choice);
	}

	/**
	 * Return the client choice.
	 * 
	 * @return
	 */
	public String getClientChoice() {
		return getPropertyAsString(CLIENT_CHOICE);
	}
}
