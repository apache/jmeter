/*
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001-2003 The Apache Software Foundation.  All rights
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
 package org.apache.jmeter.testelement.property;

import org.apache.jmeter.testelement.TestElement;

/**
 * @version $Revision$
 */
public class TestElementProperty extends MultiProperty
{
    TestElement value;
    TestElement savedValue = null;

    public TestElementProperty(String name, TestElement value)
    {
        super(name);
        this.value = value;
    }

    public TestElementProperty()
    {
        super();
    }

    public boolean equals(Object o)
    {
        if (o instanceof TestElementProperty)
        {
            if (value != null)
            {
                return value.equals(((JMeterProperty) o).getObjectValue());
            }
        }
        return false;
    }

    /* (non-Javadoc)
     * #getStringValue()
     */
    public String getStringValue()
    {
        return value.toString();
    }

    public void setObjectValue(Object v)
    {
        if (v instanceof TestElement)
        {
            value = (TestElement) v;
        }
    }

    /* (non-Javadoc)
     * #getObjectValue()
     */
    public Object getObjectValue()
    {
        return value;
    }

    public TestElement getElement()
    {
        return value;
    }

    public void setElement(TestElement el)
    {
        value = el;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    public Object clone()
    {
        TestElementProperty prop = (TestElementProperty) super.clone();
        prop.value = (TestElement) value.clone();
        return prop;
    }

    /* (non-Javadoc)
     * #mergeIn(JMeterProperty)
     */
    public void mergeIn(JMeterProperty prop)
    {
        log.debug("merging in " + prop);
        if (isEqualType(prop))
        {
            log.debug("was of equal type");
            value.addTestElement((TestElement) prop.getObjectValue());
        }
    }

    /* (non-Javadoc)
     * #recoverRunningVersion(TestElement)
     */
    public void recoverRunningVersion(TestElement owner)
    {
        if (savedValue != null)
        {
            value = savedValue;
        }
        value.recoverRunningVersion();
        super.recoverRunningVersion(null);
    }

    /* (non-Javadoc)
     * #setRunningVersion(boolean)
     */
    public void setRunningVersion(boolean runningVersion)
    {
        super.setRunningVersion(runningVersion);
        value.setRunningVersion(runningVersion);
        if(runningVersion)
        {
            savedValue = value;
        }
        else
        {
            savedValue = null;
        }
    }

    /* (non-Javadoc)
     * @see MultiProperty#addProperty(JMeterProperty)
     */
    public void addProperty(JMeterProperty prop)
    {
        value.setProperty(prop);
    }

    /* (non-Javadoc)
     * @see MultiProperty#clear()
     */
    public void clear()
    {
        value.clear();

    }

    /* (non-Javadoc)
     * @see MultiProperty#iterator()
     */
    public PropertyIterator iterator()
    {
        return value.propertyIterator();
    }
}
