package org.apache.jmeter.samplers;

import java.io.Serializable;
import java.rmi.RemoteException;

import org.apache.jmeter.engine.util.NoThreadClone;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.log.Hierarchy;
import org.apache.log.Logger;

/**
 * @author unascribed
 * @version $Revision$
 */

public class RemoteSampleListenerWrapper
    extends AbstractTestElement
    implements SampleListener, Serializable, NoThreadClone
{
    transient private static Logger log =
        Hierarchy.getDefaultHierarchy().getLoggerFor("jmeter.elements");
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
        catch (RemoteException err)
        {
            log.error("", err);
        }
    }
    public void sampleStarted(SampleEvent e)
    {
        try
        {
            listener.sampleStarted(e);
        }
        catch (RemoteException err)
        {
            log.error("", err);
        }
    }
    public void sampleStopped(SampleEvent e)
    {
        try
        {
            listener.sampleStopped(e);
        }
        catch (RemoteException err)
        {
            log.error("", err);
        }
    }
}