/*
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002,2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 * if any, must include the following acknowledgment:
 * "This product includes software developed by the
 * Apache Software Foundation (http://www.apache.org/)."
 * Alternately, this acknowledgment may appear in the software itself,
 * if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 * "Apache JMeter" must not be used to endorse or promote products
 * derived from this software without prior written permission. For
 * written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 * "Apache JMeter", nor may "Apache" appear in their name, without
 * prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
package org.apache.jmeter.timers;

import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.testelement.TestListener;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * This class implements a constant throughput timer. A Constant Throughtput
 * Timer paces the samplers under it's influence so that the total number of
 * samples per unit of time approaches a given constant as much as possible.
 *
 * @author <a href="mailto:jsalvata@atg.com">Jordi Salvat i Alabart</a>
 * @author <a href="mailto:seade@backstagetech.com.au">Scott Eade</a>
 * @version $Id$
 */
public class ConstantThroughputTimer
        extends TestBean
        implements Timer, TestListener
{
	protected static final Logger log = LoggingManager.getLoggerForClass();

    /**
     * Target time for the start of the next request. The delay provided by
     * the timer will be calculated so that the next request happens at this
     * time.
     */
    private long targetTime= 0;

	/**
	 * Desired throughput, in samples per minute.
	 */
	private double throughput;

    /**
     * Constructor for a non-configured ConstantThroughputTimer.
     */
    public ConstantThroughputTimer()
    {
    }

    /**
     * Sets the desired throughput.
     *
     * @param throughput Desired sampling rate, in samples per minute.
     */
    public void setThroughput(double throughput)
    {
    	this.throughput= throughput;
    }

    /**
     * Gets the configured desired throughput.
     *
     * @return the rate at which samples should occur, in samples per minute.
     */
    public double getThroughput()
    {
    	return throughput;
    }
    
    /**
     * Retrieve the delay to use during test execution.
     * 
     * @see org.apache.jmeter.timers.Timer#delay()
     */
    public synchronized long delay()
    {
    	prepare();

        long currentTime = System.currentTimeMillis();
        long currentTarget = targetTime == 0 ? currentTime : targetTime;
        targetTime = currentTarget + (long)( 60000.0 / getThroughput() );
        if (currentTime > currentTarget)
        {
            // We're behind schedule -- try to catch up:
            return 0;
        }
        return currentTarget - currentTime;
    }

    /**
     * Provide a description of this timer class.
     * 
     * TODO: Is this ever used? I can't remember where. Remove if it isn't --
     * TODO: or obtain text from bean's displayName or shortDescription.
     *
     * @return the description of this timer class.
     */
    public String toString()
    {
        return JMeterUtils.getResString("constant_throughput_timer_memo");
    }

    /**
     * Get the timer ready to compute delays for a new test.
     * 
     * @see org.apache.jmeter.testelement.TestListener#testStarted()
     */
    public void testStarted()
    {
    	log.debug("Test started - reset throughput calculation.");
    	targetTime= 0;
    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.testelement.TestListener#testEnded()
     */
    public void testEnded()
    {
    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.testelement.TestListener#testStarted(java.lang.String)
     */
    public void testStarted(String host)
    {
    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.testelement.TestListener#testEnded(java.lang.String)
     */
    public void testEnded(String host)
    {
    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.testelement.TestListener#testIterationStart(org.apache.jmeter.engine.event.LoopIterationEvent)
     */
    public void testIterationStart(LoopIterationEvent event)
    {
    }
}