package org.apache.jmeter.samplers;

import java.rmi.RemoteException;
import org.apache.jmeter.testelement.TestListener;
import org.apache.jmeter.testelement.AbstractTestElement;
import java.io.Serializable;

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
			ex.printStackTrace();
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
			ex.printStackTrace();
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
			ex.printStackTrace();
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
			ex.printStackTrace();
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
			err.printStackTrace();
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
			err.printStackTrace();
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
			err.printStackTrace();
		}
	}
}