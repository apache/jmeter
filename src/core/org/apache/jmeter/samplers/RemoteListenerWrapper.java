package org.apache.jmeter.samplers;

import java.io.Serializable;
import java.rmi.RemoteException;

import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.engine.util.NoThreadClone;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.TestListener;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;
import org.apache.jmeter.util.JMeterUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

/**
 * @author unascribed
 * 
 * Lars-Erik Helander provided the idea (and original implementation)
 * for the caching functionality (sampleStore).
 * 
 * @version $Revision$ Updated on: $Date$
 */
public class RemoteListenerWrapper
    extends AbstractTestElement
    implements SampleListener, TestListener, Serializable, NoThreadClone
{
    transient private static Logger log = LoggingManager.getLoggerForClass();
    RemoteSampleListener listener;
    
    private boolean holdSamples; //Hold samples to end of test?
    private List sampleStore; // Samples stored here

    private void setUpStore(){
		holdSamples = JMeterUtils.getPropDefault("hold_samples",false);
		if (holdSamples){
			sampleStore = new ArrayList();
			log.info("Using Sample store for this test run");
		}
    }

    public RemoteListenerWrapper(RemoteSampleListener l)
    {
        listener = l;
    }


    public RemoteListenerWrapper() //TODO: not used - make private?
    {
    }

    public void testStarted()
    {
    	log.info("Test Started()"); // should this be debug?
    	setUpStore();
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
    	log.debug("Test ended");
        try
        {
        	if (holdSamples){
				Iterator i = sampleStore.iterator();
				while (i.hasNext()) {
				  SampleEvent se = (SampleEvent) i.next();
				  listener.sampleOccurred(se);
				}
        	}
            listener.testEnded();
            sampleStore = null;
        }
        catch (Exception ex)
        {
            log.error("", ex);
        }
    }
    public void testStarted(String host)
    {
		log.info("Test Started on "+host); // should this be debug?
		setUpStore();
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
    	log.info("Test Ended"); // should this be debug?
        try
        {
        	if (holdSamples){
				Iterator i = sampleStore.iterator();
				while (i.hasNext()) {
				  SampleEvent se = (SampleEvent) i.next();
				  listener.sampleOccurred(se);
				}
        	}
            listener.testEnded(host);
            sampleStore = null;
        }
        catch (Exception ex)
        {
            log.error("", ex);
        }
    }

    public void sampleOccurred(SampleEvent e)
    {
    	log.debug("Sample occurred");
        try
        {
          if (holdSamples) {
            sampleStore.add(e);
          } else { 
            listener.sampleOccurred(e);
          }
        }
        catch (RemoteException err)
        {
            log.error("", err);
        }
    }

//	Note that sampleStarted() and sampleStopped() is not made to appear
//	in synch with sampleOccured() when replaying held samples.
//	For now this is not critical since sampleStarted() and sampleStopped()
//	is not used, but it may become an issue in the future. Then these
//	events must also be stored so that replay of all events may occur and
//	in the right order. Each stored event must then be tagged with something
//	that lets you distinguish between occured, started and ended.

    public void sampleStarted(SampleEvent e)
    {
		log.debug("Sample started");
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
		log.debug("Sample stopped");
        try
        {
            listener.sampleStopped(e);
        }
        catch (RemoteException err)
        {
            log.error("", err);
        }
    }
    /* (non-Javadoc)
     * @see TestListener#testIterationStart(LoopIterationEvent)
     */
    public void testIterationStart(LoopIterationEvent event)
    {
    }

}