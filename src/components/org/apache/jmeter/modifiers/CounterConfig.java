package org.apache.jmeter.modifiers;
import java.io.Serializable;

import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.engine.event.LoopIterationListener;
import org.apache.jmeter.engine.util.NoThreadClone;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.property.BooleanProperty;
import org.apache.jmeter.testelement.property.IntegerProperty;
import org.apache.jmeter.threads.JMeterContextService;
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
	extends AbstractTestElement
	implements Serializable, LoopIterationListener,NoThreadClone
{
	private static Logger log = LoggingManager.getLoggerFor(JMeterUtils.ELEMENTS);
	private final static String START = "CounterConfig.start";
	private final static String END = "CounterConfig.end";
	private final static String INCREMENT = "CounterConfig.incr";
	private final static String PER_USER = "CounterConfig.per_user";
	private final static String VAR_NAME = "CounterConfig.name";
	
	private boolean perUser = false;
	private int globalCounter = -1;
	private int currentIterationCount = -1;
	/**
	 * @see org.apache.jmeter.engine.event.LoopIterationListener#iterationStart(LoopIterationEvent)
	 */
	public synchronized void iterationStart(LoopIterationEvent event)
	{
		JMeterVariables variables = JMeterContextService.getContext().getVariables();
		int start=getStart(),end=getEnd(),increment=getIncrement();
		if(!isPerUser())
		{
			if (globalCounter==-1 || globalCounter>end)
			{
				globalCounter=start;
			}
			variables.put(getVarName(),Integer.toString(globalCounter));
			globalCounter+=increment;
		}
		else
		{		
			String value = variables.get(getVarName());
			if(value == null || value.equals(""))
			{
				variables.put(getVarName(),Integer.toString(start));
                value = variables.get(getVarName());
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
	
	public void setStart(int start)
	{
		setProperty(new IntegerProperty(START,start));
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
		setProperty(new IntegerProperty(END,end));
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
		setProperty(new IntegerProperty(INCREMENT,inc));
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
		setProperty(new BooleanProperty(PER_USER,isPer));
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
