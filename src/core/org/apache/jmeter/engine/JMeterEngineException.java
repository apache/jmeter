package org.apache.jmeter.engine;

import java.io.Serializable;

/**
 * @author mstover
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 */
public class JMeterEngineException extends Exception implements Serializable {
	
	public JMeterEngineException()
	{
		super();
	}
	
	public JMeterEngineException(String msg)
	{
		super(msg);
	}

}
