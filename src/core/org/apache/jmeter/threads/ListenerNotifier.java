package org.apache.jmeter.threads;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleListener;
/**
 * @author Administrator
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 */
public class ListenerNotifier extends LinkedList implements Runnable
{
	/**
	 * @see java.lang.Runnable#run()
	 */
	boolean running;
	boolean isStopped;
	
	public ListenerNotifier()
	{
		super();
		running = true;
		isStopped = true;
	}
	
	public void run()
	{
		Iterator iter;
		while(running || this.size() > 0)
		{
			SampleEvent res = (SampleEvent)this.removeFirst();
			if(res != null)
			{
				List listeners = (List)this.removeFirst();
				iter = listeners.iterator();
				while(iter.hasNext())
				{
					((SampleListener)iter.next()).sampleOccurred(res);
				}
			}
			Thread.yield();
		}
		isStopped = true;
	}
	
	public boolean isStopped()
	{
		return isStopped;
	}
	
	public synchronized void addLast(SampleEvent item,List listeners)
	{
		super.addLast(item);
		super.addLast(listeners);
	}
	
	public synchronized Object removeFirst()
	{
		try
		{
			return super.removeFirst();
		}
		catch (RuntimeException e)
		{
			return null;
		}
	}
	
	public void stop()
	{
		running = false;
	}
	
	public void start()
	{
		Thread noteThread = new Thread(this);
		noteThread.start();
		isStopped = false;
	}
}
