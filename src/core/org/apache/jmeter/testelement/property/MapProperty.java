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

import java.util.Map;

import org.apache.jmeter.testelement.TestElement;

/**
 * @version $Revision$
 */
public class MapProperty extends MultiProperty
{
    Map value;
    Map savedValue = null;

    public MapProperty(String name, Map value)
    {
        super(name);
        log.info("map = " + value);
        this.value = normalizeMap(value);
        log.info("normalized map = " + this.value);
    }

    public MapProperty()
    {
        super();
    }

    public boolean equals(Object o)
    {
        if (o instanceof MapProperty)
        {
            if (value != null)
            {
                return value.equals(((JMeterProperty) o).getObjectValue());
            }
        }
        return false;
    }

    public void setObjectValue(Object v)
    {
        if (v instanceof Map)
        {
            setMap((Map) v);
        }
    }

    public void addProperty(JMeterProperty prop)
    {
        addProperty(prop.getName(), prop);
    }

    public JMeterProperty get(String key)
    {
        return (JMeterProperty) value.get(key);
    }

    /**
     * Figures out what kind of properties this collection is holding and
     * returns the class type.
     * @see AbstractProperty#getPropertyType()
     */
    protected Class getPropertyType()
    {
        if (value.size() > 0)
        {
            return valueIterator().next().getClass();
        }
        else
        {
            return NullProperty.class;
        }
    }

    /**
     * @see JMeterProperty#getStringValue()
     */
    public String getStringValue()
    {
        return value.toString();
    }

    /**
     * @see JMeterProperty#getObjectValue()
     */
    public Object getObjectValue()
    {
        return value;
    }

    /**
     * @see java.lang.Object#clone()
     */
    public Object clone()
    {
        MapProperty prop = (MapProperty) super.clone();
        prop.value = cloneMap();
        return prop;
    }

    private Map cloneMap()
    {
        try
        {
            Map newCol = (Map) value.getClass().newInstance();
            PropertyIterator iter = valueIterator();
            while (iter.hasNext())
            {
                JMeterProperty item = iter.next();
                newCol.put(item.getName(), item.clone());
            }
            return newCol;
        }
        catch (Exception e)
        {
            log.error("Couldn't clone map", e);
            return value;
        }
    }

    public PropertyIterator valueIterator()
    {
        return new PropertyIteratorImpl(value.values());
    }

    public void addProperty(String name, JMeterProperty prop)
    {
        if (!value.containsKey(name))
        {
            value.put(name, prop);
        }
    }

    public void setMap(Map newMap)
    {
        value = normalizeMap(newMap);
    }

    /**
     * @see JMeterProperty#recoverRunningVersion(TestElement)
     */
    public void recoverRunningVersion(TestElement owner)
    {
        if (savedValue != null)
        {
            value = savedValue;
        }
        recoverRunningVersionOfSubElements(owner);
    }

    public void clear()
    {
        value.clear();
    }

    /* (non-Javadoc)
     * @see MultiProperty#iterator()
     */
    public PropertyIterator iterator()
    {
        return valueIterator();
    }

    /* (non-Javadoc)
     * @see JMeterProperty#setRunningVersion(boolean)
     */
    public void setRunningVersion(boolean running)
    {
        super.setRunningVersion(running);
        if (running)
        {
            savedValue = value;
        }
        else
        {
            savedValue = null;
        }
    }
}
