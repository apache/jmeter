package org.apache.jmeter.samplers;

import java.io.Serializable;
import java.rmi.RemoteException;

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

public class RemoteListenerWrapper extends AbstractTestElement implements
		SampleListener,TestListener,Serializable
{
	transient private static Logger log = Hierarchy.getDefaultHierarchy().getLoggerFor(
			"jmeter.elements");
	RemoteSampleListener listener;

	public RemoteListenerWrapper(RemoteSampleListener l)
	{
		listener = l;
	}

	public RemoteListenerWrapper()
	{
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

	public void sampleOccurred(SampleEvent e)
	{
		try
		{
			listener.sampleOccurred(e);
		}
		catch(RemoteException err)
		{
			log.error("",err);
		}
	}
	public void sampleStarted(SampleEvent e)
	{
		try
		{
			listener.sampleStarted(e);
		}
		catch(RemoteException err)
		{
			log.error("",err);
		}
	}
	public void sampleStopped(SampleEvent e)
	{
		try
		{
			listener.sampleStopped(e);
		}
		catch(RemoteException err)
		{
			log.error("",err);
		}
	}
}