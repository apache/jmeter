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

package org.apache.jmeter.protocol.jms.config;

import java.io.Serializable;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.protocol.jms.sampler.JMSSampler;
import org.apache.jmeter.testelement.property.BooleanProperty;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.TestElementProperty;

/**
 * Configuration for Java Messaging Point-to-Point requests.
 * <br>
 * Created on:  October 28, 2004
 *
 * @author Martijn Blankestijn
 * @version $Id$ 
 */
public class JMSConfig extends ConfigTestElement implements Serializable
{
    public static final String IS_ONE_WAY = "true";
    public static final String ARGUMENTS = "arguments";
    public static final String RECEIVE_QUEUE = "JMSSampler.ReceiveQueue";
    public static final String XML_DATA = "HTTPSamper.xml_data";
    public final static String SEND_QUEUE = "JMSSampler.SendQueue";
    public final static String QUEUE_CONNECTION_FACTORY_JNDI =
        "JMSSampler.queueconnectionfactory";

    /**
     *  Constructor for the JavaConfig object
     */
    public JMSConfig()
    {
        setArguments(new Arguments());
    }

    /**
     * Adds an argument to the list of arguments for this JavaConfig object.
     * The {@link org.apache.jmeter.protocol.java.sampler.JavaSamplerClient}
     * implementation can access these arguments through the
     * {@link org.apache.jmeter.protocol.java.sampler.JavaSamplerContext}.
     * 
     * @param name       the name of the argument to be added
     * @param value      the value of the argument to be added
     */
    public void addArgument(String name, String value)
    {
        Arguments args = this.getArguments();
        args.addArgument(name, value);
    }

    /**
     * Removes all of the arguments associated with this JavaConfig object.
     */
    public void removeArguments()
    {
        setProperty(
            new TestElementProperty(
                JMSSampler.JMS_PROPERTIES,
                new Arguments()));
    }

	/**
	 * Returns the name of the queue connection factory.
	 * @return the queue connection factory
	 */
    public String getQueueConnectionFactory()
    {
        return getPropertyAsString(QUEUE_CONNECTION_FACTORY_JNDI);
    }

    public void setQueueConnectionFactory(String qcf)
    {
        setProperty(QUEUE_CONNECTION_FACTORY_JNDI, qcf);
    }

    public String getSendQueue()
    {
        return getPropertyAsString(SEND_QUEUE);
    }

    public void setSendQueue(String name)
    {
        setProperty(SEND_QUEUE, name);
    }

    public String getReceiveQueue()
    {
        return getPropertyAsString(RECEIVE_QUEUE);
    }

    public void setReceiveQueue(String name)
    {
        setProperty(RECEIVE_QUEUE, name);
    }

    public String getContent()
    {
        return getPropertyAsString(XML_DATA);
    }

    public void setContent(String content)
    {
        setProperty(XML_DATA, content);
    }

    public boolean getIsOneway()
    {
        return getPropertyAsBoolean(IS_ONE_WAY);
    }

    public void setIsOneway(boolean isOneway)
    {
        JMeterProperty property = new BooleanProperty(IS_ONE_WAY, isOneway);
        setProperty(property);
    }
    public void setArguments(Arguments args)
    {
        setProperty(new TestElementProperty(JMSSampler.JMS_PROPERTIES, args));
    }

    public Arguments getArguments()
    {
        return (Arguments) getProperty(JMSSampler.JMS_PROPERTIES)
            .getObjectValue();
    }

}
