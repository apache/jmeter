package org.apache.jmeter.modifiers;
import java.io.Serializable;

import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.testelement.ThreadListener;
import org.apache.jmeter.testelement.VariablesCollection;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;
/**
 * @author Administrator
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 */
public class CounterConfig
	extends ConfigTestElement
	implements Serializable, ThreadListener
{
	private static Logger log = LoggingManager.getLoggerFor(JMeterUtils.ELEMENTS);
	private final static String START = "CounterConfig.start";
	private final static String END = "CounterConfig.end";
	private final static String INCREMENT = "CounterConfig.incr";
	private final static String PER_USER = "CounterConfig.per_user";
	private final static String VAR_NAME = "CounterConfig.name";
	
	private boolean perUser = false;
	private int globalCounter = -1;
	private int increment = 1;
	private int start = 0;
	private int end = Integer.MAX_VALUE;
	private VariablesCollection vars = new VariablesCollection();
	private int currentIterationCount = -1;
	/**
	 * @see org.apache.jmeter.testelement.ThreadListener#iterationStarted(int)
	 */
	public synchronized void iterationStarted(int iterationCount)
	{
		JMeterVariables variables = vars.getVariables();
		if(!perUser)
		{
			globalCounter++;
			int value = start + (increment * globalCounter);
			if(value > end)
			{
				globalCounter = 0;
				value = start;
			}
			variables.put(getVarName(),Integer.toString(value));
		}
		else
		{		
			String value = variables.get(getVarName());
			if(value == null)
			{
				variables.put(getVarName(),Integer.toString(start));
			}
			else
			{
				try
				{
					int current = Integer.parseInt(value);
					current += increment;
					if(current > end)
					{
						current = start;
					}
					variables.put(getVarName(),Integer.toString(current));
				}
				catch(NumberFormatException e)
				{
					log.info("Bad number in Counter config",e);
				}		
			}	
		}			
	}
	
	/**
	 * @see org.apache.jmeter.testelement.ThreadListener#setJMeterVariables(JMeterVariables)
	 */
	public void setJMeterVariables(JMeterVariables jmVars)
	{
		vars.addJMeterVariables(jmVars);
		start = getStart();
		end = getEnd();
		increment = getIncrement();
		perUser = isPerUser();
	}
	
	public void setStart(int start)
	{
		setProperty(START,new Integer(start));
	}
	
	public void setStart(String start)
	{
		setProperty(START,start);
	}
	
	public int getStart()
	{
		return getPropertyAsInt(START);
	}
	
	public void setEnd(int end)
	{
		setProperty(END,new Integer(end));
	}
	
	public void setEnd(String end)
	{
		setProperty(END,end);
	}
	
	public int getEnd()
	{
		return getPropertyAsInt(END);
	}
	
	public void setIncrement(int inc)
	{
		setProperty(INCREMENT,new Integer(inc));
	}
	
	public void setIncrement(String incr)
	{
		setProperty(INCREMENT,incr);
	}
	
	public int getIncrement()
	{
		return getPropertyAsInt(INCREMENT);
	}
	
	public void setIsPerUser(boolean isPer)
	{
		setProperty(PER_USER,new Boolean(isPer));
	}
	
	public boolean isPerUser()
	{
		return getPropertyAsBoolean(PER_USER);
	}
	
	public void setVarName(String name)
	{
		setProperty(VAR_NAME,name);
	}
	
	public String getVarName()
	{
		return getPropertyAsString(VAR_NAME);
	}
}
