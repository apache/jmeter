package org.apache.jmeter.samplers;

import java.io.Serializable;
import java.rmi.RemoteException;

import org.apache.jmeter.testelement.AbstractTestElement;
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

public class RemoteSampleListenerWrapper extends AbstractTestElement implements
		SampleListener,Serializable
{
	transient private static Logger log = Hierarchy.getDefaultHierarchy().getLoggerFor(
			"jmeter.elements");
	RemoteSampleListener listener;

	public RemoteSampleListenerWrapper(RemoteSampleListener l)
	{
		listener = l;
	}

	public RemoteSampleListenerWrapper()
	{
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