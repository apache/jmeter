package org.apache.jmeter.samplers;

import java.io.Serializable;

import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.engine.util.NoThreadClone;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.TestListener;
import org.apache.log.Hierarchy;
import org.apache.log.Logger;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2001</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class RemoteTestListenerWrapper extends AbstractTestElement implements
		TestListener,Serializable,NoThreadClone
{
	transient private static Logger log = Hierarchy.getDefaultHierarchy().getLoggerFor(
			"jmeter.elements");
	RemoteSampleListener listener;

	public RemoteTestListenerWrapper()
	{
	}

	public RemoteTestListenerWrapper(RemoteSampleListener l)
	{
		listener = l;
	}
	public void testStarted()
	{
		try
		{
			listener.testStarted();
		}
		catch (Exception ex)
		{
			log.error("",ex);
		}

	}
	public void testEnded()
	{
		try
		{
			listener.testEnded();
		}
		catch (Exception ex)
		{
			log.error("",ex);
		}
	}
	public void testStarted(String host)
	{
		try
		{
			listener.testStarted(host);
		}
		catch (Exception ex)
		{
			log.error("",ex);
		}
	}
	public void testEnded(String host)
	{
		try
		{
			listener.testEnded(host);
		}
		catch (Exception ex)
		{
			log.error("",ex);
		}
	}
    /* (non-Javadoc)
     * @see org.apache.jmeter.testelement.TestListener#testIterationStart(LoopIterationEvent)
     */
    public void testIterationStart(LoopIterationEvent event)
    {}

}