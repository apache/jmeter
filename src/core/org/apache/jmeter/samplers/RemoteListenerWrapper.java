// $Header$
/*
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
*/

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
    private RemoteSampleListener listener = null;
    
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
    	log.info("Test Started()");
    	setUpStore();
        try
        {
           listener.testStarted();
        }
        catch (Throwable ex)
        {
            log.warn("testStarted()", ex);
        }

    }
    public void testEnded()
    {
    	log.info("Test ended()");
        try
        {
        	if (holdSamples){
        	    synchronized(sampleStore){
					Iterator i = sampleStore.iterator();
					while (i.hasNext()) {
					  SampleEvent se = (SampleEvent) i.next();
					  listener.sampleOccurred(se);
					}
				}
        	}
            listener.testEnded();
            sampleStore = null;
        }
        catch (Throwable ex)
        {
            log.warn("testEnded()", ex);
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
        catch (Throwable ex)
        {
            log.error("testStarted(host)", ex);
        }
    }
    public void testEnded(String host)
    {
    	log.info("Test Ended on " + host); // should this be debug?
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
        catch (Throwable ex)
        {
            log.error("testEnded(host)", ex);
        }
    }

    public void sampleOccurred(SampleEvent e)
    {
    	log.debug("Sample occurred");
        try
        {
          if (holdSamples) {
          	synchronized(sampleStore)
            {
            	sampleStore.add(e);
            }
          } else { 
            listener.sampleOccurred(e);
          }
        }
        catch (RemoteException err)
        {
            log.error("sampleOccurred", err);
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
            log.error("sampleStarted", err);
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
            log.error("sampleStopped", err);
        }
    }
    /* (non-Javadoc)
     * @see TestListener#testIterationStart(LoopIterationEvent)
     */
    public void testIterationStart(LoopIterationEvent event)
    {
    }

}