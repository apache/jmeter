package org.apache.jmeter.engine;

import java.io.Serializable;

/**
 * @author mstover
 */
public class JMeterEngineException extends Exception implements Serializable
{
    public JMeterEngineException()
    {
        super();
    }

    public JMeterEngineException(String msg)
    {
        super(msg);
    }
}
