package org.apache.jmeter.samplers;

import java.io.Serializable;

import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.engine.util.NoThreadClone;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.TestListener;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * @author unascribed
 * @version $Revision$
 */
public class RemoteTestListenerWrapper
    extends AbstractTestElement
    implements TestListener, Serializable, NoThreadClone
{
    transient private static Logger log = LoggingManager.getLoggerForClass();
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
            log.error("", ex);
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
            log.error("", ex);
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
            log.error("", ex);
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
            log.error("", ex);
        }
    }
    /* (non-Javadoc)
     * @see TestListener#testIterationStart(LoopIterationEvent)
     */
    public void testIterationStart(LoopIterationEvent event)
    {
    }

}