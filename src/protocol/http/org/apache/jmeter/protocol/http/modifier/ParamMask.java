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
 *
 * @author  David La France
 * @author  <a href="mailto:seade@backstagetech.com.au">Scott Eade</a>
 * @version $Id$
 */
 
package org.apache.jmeter.protocol.http.modifier;

import java.io.Serializable;

import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.property.LongProperty;

/**
 * This object defines with what a parameter has its value replaced, and the
 * policies for how that value changes. Used in {@link ParamModifier}.
 */
public class ParamMask extends AbstractTestElement implements Serializable
{
    private String PREFIX = "ParamModifier.prefix";
    private String FIELD_NAME = "ParamModifier.field_name";
    private String UPPER_BOUND = "ParamModifier.upper_bound";
    private String LOWER_BOUND = "ParamModifier.lower_bound";
    private String INCREMENT = "ParamModifier.increment";
    private String SUFFIX = "ParamModifier.suffix";

    private long _value = 0;

    /**
     * Default constructor.
     */
    public ParamMask()
    {
        setFieldName("");
        setPrefix("");
        setLowerBound(0);
        setUpperBound(0);
        setIncrement(0);
        setSuffix("");
    }

    /**
     * Sets the prefix for the <code>long</code> value. The prefix, the value
     * and the suffix are concatenated to give the parameter value. This allows
     * a wider range of posibilities for the parameter values.
     *
     * @param  prefix  a string to prefix to the parameter value
     */
    public void setPrefix(String prefix)
    {
        setProperty(PREFIX, prefix);
    }

    /**
     * Set the current value of the <code>long<code> portion of the parameter
     * value to replace. This is usually not used, as the method
     * {@link #resetValue} is used to define a policy for the starting value.
     *
     * @param  val  the new parameter value
     */
    public void setValue(long val)
    {
        _value = val;
    }

    public void setFieldName(String fieldName)
    {
        setProperty(FIELD_NAME, fieldName);
    }

    /**
     * Sets the lowest possible value that the <code>long</code> portion of the
     * parameter value may be.
     *
     * @param  val  the new lowest possible parameter value
     */
    public void setLowerBound(long val)
    {
        setProperty(new LongProperty(LOWER_BOUND, val));
    }

    /**
     * Sets the highest possible value that the <code>long</code> portion of the
     * parameter value may be.
     *
     * @param  val  the new highest possible parameter value
     */
    public void setUpperBound(long val)
    {
        setProperty(new LongProperty(UPPER_BOUND, val));
    }

    /**
     * Sets the number by which the parameter value is incremented between
     * loops.
     *
     * @param  incr  the new increment for the parameter value
     */
    public void setIncrement(long incr)
    {
        setProperty(new LongProperty(INCREMENT, incr));
    }

    /**
     * Sets the suffix for the <code>long</code> value. The prefix, the value
     * and the suffix are concatenated to give the parameter value. This allows
     * a wider range of posibilities for the parameter values.
     *
     * @param  suffix  a string to suffix to the parameter value
     */
    public void setSuffix(String suffix)
    {
        setProperty(SUFFIX, suffix);
    }

    /**
     * Accessor method to return the <code>String</code> that will be prefixed
     * to the <code>long</code> value.
     *
     * @return    the parameter value prefix
     */
    public String getPrefix()
    {
        return getPropertyAsString(PREFIX);
    }

    /**
     * Accessor method, returns the lowest possible value that the
     * <code>long</code> portion of the parameter value may be.
     *
     * @return    the lower bound of the possible values
     */
    public long getLowerBound()
    {
        return getPropertyAsLong(LOWER_BOUND);
    }

    /**
     * Accessor method, returns the highest possible value that the
     * <code>long</code> portion of the parameter value may be.
     *
     * @return    the higher bound of the possible values
     */
    public long getUpperBound()
    {
        return getPropertyAsLong(UPPER_BOUND);
    }

    /**
     * Accessor method, returns the number by which the parameter value is
     * incremented between loops.
     *
     * @return    the increment
     */
    public long getIncrement()
    {
        return getPropertyAsLong(INCREMENT);
    }

    /**
     * Accessor method to return the <code>String</code> that will be suffixed
     * to the <code>long</code> value.
     *
     * @return    the parameter value suffix
     */
    public String getSuffix()
    {
        return getPropertyAsString(SUFFIX);
    }

    /*
     *  -----------------------------------------------------------------------
     *  Methods
     *  -----------------------------------------------------------------------
     */

    /**
     *  Returns the current value, prefixed and suffixed, as a string, then
     *  increments it. If the incremented value is above the upper bound, the
     *  value is reset to the lower bound. <BR>
     *  <P>
     *  This method determines the policy of what happens when an upper bound is
     *  reached/surpassed.
     *
     * @return  a <code>String</code> representing the current <code>long</code>
     *          value
     */
    public String getNextValue()
    {
        // return the current value (don't forget the prefix!)
        String retval = getPrefix() + Long.toString(_value) + getSuffix();

        // increment the value
        _value += getIncrement();
        if (_value > getUpperBound())
        {
            _value = getLowerBound();
        }

        return retval;
    }

    /**
     * This method determines the policy of what value to start (and re-start)
     * at.
     */
    public void resetValue()
    {
        _value = getLowerBound();
    }

    public String getFieldName()
    {
        return getPropertyAsString(FIELD_NAME);
    }

    /**
     * For debugging purposes.
     *
     * @return    a <code>String</code> representing the object
     */
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("-------------------------------\n");
        sb.append("Dumping ParamMask Object\n");
        sb.append("-------------------------------\n");
        sb.append("Name          = " + getFieldName() + "\n");
        sb.append("Prefix        = " + getPrefix() + "\n");
        sb.append("Current Value = " + _value + "\n");
        sb.append("Lower Bound   = " + getLowerBound() + "\n");
        sb.append("Upper Bound   = " + getUpperBound() + "\n");
        sb.append("Increment     = " + getIncrement() + "\n");
        sb.append("Suffix        = " + getSuffix() + "\n");
        sb.append("-------------------------------\n");

        return sb.toString();
    }
}
