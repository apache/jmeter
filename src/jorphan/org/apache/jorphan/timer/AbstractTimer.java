/*
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
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
 * @author <a href="mailto:jeremy_a@bigfoot.com">Jeremy Arnold</a>
 * @version $Revision$
 */
public abstract class AbstractTimer implements ITimer, ITimerConstants
{
    /** Used to keep track of timer state. */    
    private int m_state;
    
    /** Timing data. */
    private double m_data;


    /* (non-Javadoc)
     * @see org.apache.jorphan.timer.ITimer#start()
     */
    public void start()
    {
        if (m_state != STATE_READY)
        {
            throw new IllegalStateException(
                this
                    + ": start() must be called from READY state, "
                    + "current state is "
                    + STATE_NAMES[m_state]);
        }
        
        m_state = STATE_STARTED;
        m_data = getCurrentTime();
    }

    /* (non-Javadoc)
     * @see org.apache.jorphan.timer.ITimer#stop()
     */
    public void stop()
    {
        // Latch stop time in a local var before doing anything else
        final double data = getCurrentTime();
        
        if (m_state != STATE_STARTED)
        {
            throw new IllegalStateException(
                this
                    + ": stop() must be called from STARTED state, "
                    + "current state is "
                    + STATE_NAMES[m_state]);
        } 
        
        m_data = data - m_data;
        m_state = STATE_STOPPED;
    }

    /* (non-Javadoc)
     * @see org.apache.jorphan.timer.ITimer#getDuration()
     */
    public double getDuration()
    {
        if (m_state != STATE_STOPPED)
        {
            throw new IllegalStateException(
                this
                    + ": getDuration() must be called from STOPPED state, "
                    + "current state is "
                    + STATE_NAMES[m_state]);
        }
        return m_data;
    }

    /* (non-Javadoc)
     * @see org.apache.jorphan.timer.ITimer#reset()
     */
    public void reset()
    {
        m_state = STATE_READY;
    }
    
    protected abstract double getCurrentTime();
}
