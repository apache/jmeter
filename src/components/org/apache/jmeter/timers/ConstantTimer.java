/*
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
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

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import org.apache.jmeter.engine.event.IterationEvent;
import org.apache.jmeter.engine.event.IterationListener;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.VariablesCollection;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.log.Hierarchy;
import org.apache.log.Logger;

/**
 * This class implements a constant timer with its own panel and fields for
 * value update and user interaction.
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @author <a href="mailto:seade@backstagetech.com.au">Scott Eade</a>
 * @version $Revision$ $Date$
 */
public class ConstantTimer
    extends AbstractTestElement
    implements Timer, Serializable, IterationListener
{
    private static Logger log =
        Hierarchy.getDefaultHierarchy().getLoggerFor("jmeter.elements");

    public final static String DELAY = "ConstantTimer.delay";
    private VariablesCollection vars = new VariablesCollection();
    private JMeterVariables variables;
    private static List addableList = new LinkedList();
    private long delay = 0;

    /**
     * No-arg constructor.
     */
    public ConstantTimer()
    {
    }

    /**
     * Set the delay for this timer.
     *  
     * @see org.apache.jmeter.timers.Timer#setDelay(String)
     */
    public void setDelay(String delay)
    {
        setProperty(DELAY, delay);
    }

    /**
     * Set the range (not used for this timer).
     * 
     * @see org.apache.jmeter.timers.Timer#setRange(double)
     */
    public void setRange(double range)
    {
    }

    /**
     * Get the delay value for display.
     * 
     * @return the delay value for display.
     * @see org.apache.jmeter.timers.Timer#getDelay()
     */
    public String getDelay()
    {
        return getPropertyAsString(DELAY);
    }

    /**
     * Retrieve the range (not used for this timer).
     * 
     * @return the range (always zero for this timer).
     * @see org.apache.jmeter.timers.Timer#getRange()
     */
    public double getRange()
    {
        return (double) 0;
    }

    /**
     * Retrieve the delay to use during test execution.
     * 
     * @return the delay.
     */
    public long delay()
    {
        return delay;
    }

    /**
     * Provide a description of this timer class.
     * 
     * @return the description of this timer class.
     */
    public String toString()
    {
        return JMeterUtils.getResString("constant_timer_memo");
    }

    /**
     * Gain access to any variables that have been defined.
     * 
     * @see org.apache.jmeter.testelement.ThreadListener#iterationStarted(int)
     */
    public void iterationStart(IterationEvent event)
    {
        delay = getPropertyAsLong(DELAY);
        
    }

    /**
     * Make changes to variables available elsewhere.
     * 
     * @see org.apache.jmeter.testelement.ThreadListener#setJMeterVariables(JMeterVariables)
     */
    public void setJMeterVariables(JMeterVariables jmVars)
    {
        //vars.addJMeterVariables(jmVars);
    }

}
