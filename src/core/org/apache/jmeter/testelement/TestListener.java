package org.apache.jmeter.testelement;

import org.apache.jmeter.engine.event.IterationEvent;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2001</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public interface TestListener
{

	public void testStarted();

	public void testEnded();
	
	public void testStarted(String host);
	
	public void testEnded(String host);
    
    /**
     * Each time through a Thread Group's test script, an iteration event is
     * fired.
     * @param event
     */
    public void testIterationStart(IterationEvent event);
}