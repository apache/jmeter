/*
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002-2003 The Apache Software Foundation.  All rights
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
package org.apache.jmeter.protocol.http.modifier;

import java.io.Serializable;

import org.apache.jmeter.config.Argument;
import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.processor.PreProcessor;
import org.apache.jmeter.protocol.http.sampler.HTTPSampler;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.TestListener;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.apache.jmeter.threads.JMeterContextService;

/**
 * This modifier will replace any single http sampler's url parameter value
 * with a value from a given range - thereby "masking" the value set in the
 * http sampler. The parameter names must match exactly, and the parameter
 * value must be preset to "*" to diferentiate between duplicate parameter
 * names.
 * <P>
 * For example, if you set up the modifier with a lower bound of 1, an upper
 * bound of 10, and an increment of 2, and run the loop 12 times, the parameter
 * will have the following values (one per loop): 1, 3, 5, 7, 9, 1, 3, 5, 7, 9,
 * 1, 3
 * <P>
 * The {@link ParamMask} object contains most of the logic for stepping through
 * this loop. You can make large modifications to this modifier's behaviour by
 * changing one or two method implementations there.
 *
 * @author     David La France
 * @see        ParamMask
 * @version    $Revision$ updated on $Date$
 */
public class ParamModifier
    extends AbstractTestElement
    implements TestListener, PreProcessor, Serializable
{

    /*
     * ------------------------------------------------------------------------
     *  Fields
     * ------------------------------------------------------------------------
     */

    /**
     * The key used to find the ParamMask object in the HashMap.
     */
    private final static String MASK = "ParamModifier.mask";

    /*
     * ------------------------------------------------------------------------
     *  Constructors
     * ------------------------------------------------------------------------
     */

    /**
     * Default constructor.
     */
    public ParamModifier()
    {
        setProperty(new TestElementProperty(MASK, new ParamMask()));
    }

    public ParamMask getMask()
    {
        return (ParamMask) getProperty(MASK).getObjectValue();
    }

    public void testStarted()
    {
        getMask().resetValue();
    }

    public void testStarted(String host)
    {
        getMask().resetValue();
    }

    public void testEnded()
    {
    }

    public void testEnded(String host)
    {
    }

    /*
     * ------------------------------------------------------------------------
     *  Methods implemented from interface org.apache.jmeter.config.Modifier
     * ------------------------------------------------------------------------
     */

    /**
     * Modifies an entry object to replace the value of any url parameter that
     * matches a defined mask.
     *
     */
    public void process()
    {
        Sampler sam = JMeterContextService.getContext().getCurrentSampler();
        HTTPSampler sampler = null;
        if (!(sam instanceof HTTPSampler))
        {
            return;
        }
        else
        {
            sampler = (HTTPSampler) sam;
        }
        boolean modified = false;
        PropertyIterator iter = sampler.getArguments().iterator();
        while (iter.hasNext())
        {
            Argument arg = (Argument) iter.next().getObjectValue();
            modified = modifyArgument(arg);
            if (modified)
            {
                break;
            }
        }
    }

    /*
     * ------------------------------------------------------------------------
     *  Methods
     * ------------------------------------------------------------------------
     */

    /**
     * Helper method for {@link #modifyEntry} Replaces a parameter's value if
     * the parameter name matches the mask name and the value is a '*'.
     *
     * @param  arg  an {@link Argument} representing a http parameter
     * @return      <code>true</code>if the value was replaced
     */
    private boolean modifyArgument(Argument arg)
    {
        // if a mask for this argument exists
        if (arg.getName().equals(getMask().getFieldName()))
        {
            // values to be masked must be set in the WebApp to "*"
            if ("*".equals(arg.getValue()))
            {
                arg.setValue(getMask().getNextValue());
                return true;
            }
        }
        return false;
    }

    /**
     * @see TestListener#testIterationStart(LoopIterationEvent)
     */
    public void testIterationStart(LoopIterationEvent event)
    {
    }
}
