package org.apache.jmeter.testelement;

import org.apache.jmeter.engine.event.LoopIterationEvent;

/**
 * @author unascribed
 * @version $Revision$
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
    public void testIterationStart(LoopIterationEvent event);
}