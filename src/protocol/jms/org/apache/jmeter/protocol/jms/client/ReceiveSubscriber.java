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
 * 
 */
 
package org.apache.jmeter.protocol.jms.client;

import javax.naming.Context;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;

import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * @author pete
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class ReceiveSubscriber implements Runnable {

	static Logger log = LoggingManager.getLoggerForClass();

	private TopicConnection CONN = null;
	private TopicSession SESSION = null;
	private Topic TOPIC = null;
	private TopicSubscriber SUBSCRIBER = null;
	private byte[] RESULT = null;
	private Object OBJ_RESULT = null;
	private long time = System.currentTimeMillis();
	private int counter;
	private int loop = 1;
	private StringBuffer buffer = new StringBuffer();
	private boolean RUN = true;
	private Thread CLIENTTHREAD = null;
	
    /**
     * 
     */
    public ReceiveSubscriber() {
        super();
    }
    
    public ReceiveSubscriber(String jndi, String url, String connfactory,
    String topic, String useAuth, String user, String pwd){
    	Context ctx = initJNDI(jndi,url,useAuth,user,pwd);
    	if (ctx != null) {
    		initConnection(ctx,connfactory,topic);
		} else {
			log.equals("Could not initialize JNDI Initial Context Factory");
    	}
    }
    
    public Context initJNDI(String jndi, String url, String useAuth, String user, String pwd){
    	return InitialContextFactory.lookupContext(jndi,url,useAuth,user,pwd);
    }
    
    public void initConnection(Context ctx, String connfactory, String topic){
    	try {
			TopicConnectionFactory connfac =
				ConnectionFactory.getTopicConnectionFactory(ctx,connfactory);
			this.CONN = ConnectionFactory.getTopicConnection();
			this.TOPIC = InitialContextFactory.lookupTopic(ctx,topic);
			this.SESSION = this.CONN.createTopicSession(false,TopicSession.AUTO_ACKNOWLEDGE);
			this.SUBSCRIBER = this.SESSION.createSubscriber(this.TOPIC);
			log.info("created the topic connection successfully");
        } catch (JMSException e){
    	    log.error("Connection error: " + e.getMessage());
        }
    }

	public void setLoop(int loop){
		this.loop = loop;
	}
	
	public void pause(){
		try {
			this.CONN.stop();
		} catch (JMSException e){
			log.error("failed to stop recieving");
		}
	}
	
	public void resume(){
		try {
			this.CONN.start();
		} catch (JMSException e){
			log.error("failed to start recieving");
		}
	}
	
	public String getMessage(){
		return this.buffer.toString();
	}
	
    public byte[] getByteResult(){
    	if (this.buffer.length() > 0){
    		this.RESULT = this.buffer.toString().getBytes();
    	}
		return this.RESULT;
    }
    
	public synchronized void close(){
		try {
			this.CONN.stop();
			this.SUBSCRIBER.close();
			this.SESSION.close();
			this.CONN.close();
			this.SUBSCRIBER = null;
			this.SESSION = null;
			this.CONN = null;
			this.RUN = false;
			this.CLIENTTHREAD.interrupt();
			this.CLIENTTHREAD = null;
			this.buffer.setLength(0);
			this.buffer = null;
			this.finalize();
		} catch (JMSException e){
			log.error(e.getMessage());
		} catch (Throwable e){
			log.error(e.getMessage());
		}
	}

	public void clear(){
		this.buffer.setLength(0);
		this.RESULT = null;
		this.OBJ_RESULT = null;
	}

	public synchronized int count(int count) {
		counter += count;
		if (counter >= loop){
			this.pause();
		}
		return counter;
	}

    public synchronized int resetCount() {
        counter = 0;
        return counter;
    }

	public void start(){
		this.CLIENTTHREAD = new Thread(this,"Subscriber2");
		this.CLIENTTHREAD.start();
	}
	
    public void run() {
    	ReceiveSubscriber.this.listen();
    }

    protected void listen() {
		log.info("Subscriber2.listen() called");
        try {
            while (RUN) {
                Message message = this.SUBSCRIBER.receive();
                if (message instanceof TextMessage) {
                	TextMessage msg = (TextMessage)message;
                	if (msg.getText().trim().length() > 0){
						this.buffer.append(msg.getText());
						count(1);
                	}
                }
            }
        } catch (JMSException e) {
            log.info("Communication error: " + e.getMessage());
        }
    }
}
