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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.testelement.TestElement;

/**
 * @version $Revision$
 */
public class CollectionProperty extends MultiProperty
{
    protected Collection value;
    private Collection savedValue;

    public CollectionProperty(String name, Collection value)
    {
        super(name);
        this.value = normalizeList(value);
    }

    public CollectionProperty()
    {
        super();
        value = new ArrayList();
    }

    public boolean equals(Object o)
    {
        if (o instanceof CollectionProperty)
        {
            if (value != null)
            {
                return value.equals(((JMeterProperty) o).getObjectValue());
            }
        }
        return false;
    }

    public void remove(String prop)
    {
        PropertyIterator iter = iterator();
        while (iter.hasNext())
        {
            if (iter.next().getName().equals(prop))
            {
                iter.remove();
            }
        }
    }

    public void set(int index, String prop)
    {
        if (value instanceof List)
        {
            ((List) value).set(index, new StringProperty(prop, prop));
        }
    }

    public void set(int index, JMeterProperty prop)
    {
        if (value instanceof List)
        {
            ((List) value).set(index, prop);
        }
    }

    public JMeterProperty get(int row)
    {
        if (value instanceof List)
        {
            return (JMeterProperty) ((List) value).get(row);
        }
        else
        {
            return null;
        }
    }

    public void remove(int index)
    {
        if (value instanceof List)
        {
            ((List) value).remove(index);
        }
    }

    public void setObjectValue(Object v)
    {
        if (v instanceof Collection)
        {
            setCollection((Collection) v);
        }

    }

    public PropertyIterator iterator()
    {
        return getIterator(value);
    }

    /* (non-Javadoc)
     * @see JMeterProperty#getStringValue()
     */
    public String getStringValue()
    {
        return value.toString();
    }

    /* (non-Javadoc)
     * @see JMeterProperty#getObjectValue()
     */
    public Object getObjectValue()
    {
        return value;
    }

    public int size()
    {
        return value.size();
    }

    /* (non-Javadoc)
     * @see Object#clone()
     */
    public Object clone()
    {
        CollectionProperty prop = (CollectionProperty) super.clone();
        prop.value = cloneCollection();
        return prop;
    }

    private Collection cloneCollection()
    {
        try
        {
            Collection newCol = (Collection) value.getClass().newInstance();
            PropertyIterator iter = iterator();
            while (iter.hasNext())
            {
                newCol.add(iter.next().clone());
            }
            return newCol;
        }
        catch (Exception e)
        {
            log.error("Couldn't clone collection", e);
            return value;
        }
    }

    public void setCollection(Collection coll)
    {
        value = normalizeList(coll);
    }

    public void clear()
    {
        value.clear();
    }

    /**
     * Easy way to add properties to the list.
     * @param prop
     */
    public void addProperty(JMeterProperty prop)
    {
        value.add(prop);
    }

    public void addItem(Object item)
    {
        addProperty(convertObject(item));
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
            return value.iterator().next().getClass();
        }
        else
        {
            return NullProperty.class;
        }
    }

    /* (non-Javadoc)
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
    
    public static class Test extends JMeterTestCase
    {
        public Test(String name)
        {
            super(name);
        }
        
        public void testAddingProperties() throws Exception
        {
            CollectionProperty coll = new CollectionProperty();
            coll.addItem("joe");
            coll.addProperty(new FunctionProperty());
            assertEquals("joe",coll.get(0).getName());
            assertEquals(
                "org.apache.jmeter.testelement.property.FunctionProperty",
                coll.get(1).getClass().getName());
        }
    }

    /* (non-Javadoc)
     * @see JMeterProperty#setRunningVersion(boolean)
     */
    public void setRunningVersion(boolean running)
    {
        super.setRunningVersion(running);
        if(running)
        {
            savedValue = value;
        }
        else
        {
            savedValue = null;
        }
    }
}
