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
 * A package-private implementation of {@link ITimer} based around native
 * <code>getTime</code> method. It will work on any platform for which a JNI
 * implementation of "hrtlib" library is available.<P> 
 * 
 * {@link TimerFactory} acts as the Factory for this class.<P>
 * 
 * MT-safety: an instance of this class is safe to be used within the same
 * thread only.
 * 
 * @author <a href="mailto:vroubtsov@illinoisalumni.org">Vlad Roubtsov</a>
 * @author Originally published in <a href="http://www.javaworld.com/javaworld/javaqa/2003-01/01-qa-0110-timing.html">JavaWorld</a>
 * @version $Revision$
 */
final class HRTimer implements ITimer, ITimerConstants 
{
    public void start ()
    {
        if (DO_STATE_CHECKS)
        {
            if (m_state != STATE_READY)
            {
                throw new IllegalStateException(
                    this
                        + ": start() must be called from READY state, "
                        + "current state is "
                        + STATE_NAMES[m_state]);
            }
        }
        
        if (DO_STATE_CHECKS)
        {
            m_state = STATE_STARTED;
        } 
        m_data = getTime ();
    }
    
    public void stop ()
    {
        // Latch stop time in a local var before doing anything else.
        final double data = getTime ();
        
        if (DO_STATE_CHECKS)
        {
            if (m_state != STATE_STARTED)
            {
                throw new IllegalStateException(
                    this
                        + ": stop() must be called from STARTED state, "
                        + "current state is "
                        + STATE_NAMES[m_state]);
            }
        }
        
        m_data = data - m_data;
        if (DO_STATE_CHECKS)
        {
            m_state = STATE_STOPPED;
        } 
    }
    
    public double getDuration ()
    {
        if (DO_STATE_CHECKS)
        {
            if (m_state != STATE_STOPPED)
            {
                throw new IllegalStateException(
                    this
                        + ": getDuration() must be called from STOPPED state, "
                        + "current state is "
                        + STATE_NAMES[m_state]);
            }
        }
        
        return m_data;
    }
    
    public void reset ()
    {
        if (DO_STATE_CHECKS)
        {
            m_state = STATE_READY;
        } 
    }


    /*
     * This is supposed to return a fractional count of milliseconds elapsed
     * since some indeterminate moment in the past. The exact starting point
     * is not relevant because this timer class reports time differences only.
     * 
     * JNI code in HRTIMER_LIB library is supposed to implement this.
     */
    private static native double getTime ();
    

    /** Used to keep track of timer state. */
    private int m_state;
    
    /** Timing data. */
    private double m_data;
        
    private static final String HRTIMER_LIB = "hrtlib";
    
    static
    {
        try
        {
            System.loadLibrary (HRTIMER_LIB);
        }
        catch (UnsatisfiedLinkError e)
        {
            System.out.println ("native lib '" + HRTIMER_LIB
                + "' not found in 'java.library.path': "
                + System.getProperty ("java.library.path"));
            
            throw e; // re-throw
        }
    }
}
