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
package org.apache.jorphan.timer;

/**
 * A simple interface for measuring time intervals. An instance of this goes
 * through the following lifecycle states:
 * <dl>
 *  <dt><em>ready</em></dt>
 *      <dd>timer is ready to start a new measurement</dd>
 *  <dt><em>started</em></dt>
 *      <dd>timer has recorded the starting time interval point</dd>
 *  <dt><em>stopped</em></dt>
 *      <dd>timer has recorded the ending time interval point</dd>
 * </dl>
 * See individual methods for details.
 * <p>
 * If this library has been compiled with
 * {@link ITimerConstants#DO_STATE_CHECKS} set to 'true' the implementation
 * will enforce this lifecycle model and throw IllegalStateException when it
 * is violated.
 * 
 * @author <a href="mailto:vroubtsov@illinoisalumni.org">Vlad Roubtsov</a>
 * @author Originally published in <a href="http://www.javaworld.com/javaworld/javaqa/2003-01/01-qa-0110-timing.html">JavaWorld</a>
 * @version $Revision$
 */
public interface ITimer
{
    /**
     * Starts a new time interval and advances this timer instance to 'started'
     * state. This method can be called from 'ready' state only.
     */
    void start ();
    
    /**
     * Terminates the current time interval and advances this timer instance to
     * 'stopped' state. Interval duration will be available via
     * {@link #getDuration()} method. This method can be called from 'started'
     * state only. 
     */
    void stop ();
    
    /**
     * Returns the duration of the time interval that elapsed between the last
     * calls to {@link #start()} and {@link #stop()}. This method can be called
     * any number of times from 'stopped' state and will return the same value
     * each time.
     * 
     * @return interval duration in milliseconds 
     */
    double getDuration ();
    
    /**
     * This method can be called from any state and will reset this timer
     * instance back to 'ready' state. 
     */
    void reset ();

}
