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

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * @version $Revision$
 */
public abstract class AbstractProperty implements JMeterProperty
{
    protected static Logger log = LoggingManager.getLoggerForClass();
    private String name;
    private boolean runningVersion = false;

    public AbstractProperty(String name)
    {
        this.name = name;
    }

    public AbstractProperty()
    {
        this("");
    }

    protected boolean isEqualType(JMeterProperty prop)
    {
        if (this.getClass().equals(prop.getClass()))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    /* (non-Javadoc)
     * @see JMeterProperty#isRunningVersion()
     */
    public boolean isRunningVersion()
    {
        return runningVersion;
    }

    /* (non-Javadoc)
     * @see JMeterProperty#getName()
     */
    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    /* (non-Javadoc)
     * @see JMeterProperty#setRunningVersion(boolean)
     */
    public void setRunningVersion(boolean runningVersion)
    {
        this.runningVersion = runningVersion;
    }
    
    protected PropertyIterator getIterator(Collection values)
    {
        return new PropertyIteratorImpl(values);
    }

    /* (non-Javadoc)
     * @see Object#clone()
     */
    public Object clone()
    {
        try
        {
            AbstractProperty prop =
                (AbstractProperty) this.getClass().newInstance();
            prop.name = name;
            prop.runningVersion = runningVersion;
            return prop;
        }
        catch (InstantiationException e)
        {
            return null;
        }
        catch (IllegalAccessException e)
        {
            return null;
        }
    }

    /**
     * Returns 0 if string is invalid or null.
     * @see JMeterProperty#getIntValue()
     */
    public int getIntValue()
    {
        String val = getStringValue();
        if (val == null)
        {
            return 0;
        }
        try
        {
            return Integer.parseInt(val);
        }
        catch (NumberFormatException e)
        {
            return 0;
        }
    }

    /**
     * Returns 0 if string is invalid or null.
     * @see JMeterProperty#getLongValue()
     */
    public long getLongValue()
    {
        String val = getStringValue();
        if (val == null)
        {
            return 0;
        }
        try
        {
            return Long.parseLong(val);
        }
        catch (NumberFormatException e)
        {
            return 0;
        }
    }

    /**
     * Returns 0 if string is invalid or null.
     * @see JMeterProperty#getDoubleValue()
     */
    public double getDoubleValue()
    {
        String val = getStringValue();
        if (val == null)
        {
            return 0;
        }
        try
        {
            return Double.parseDouble(val);
        }
        catch (NumberFormatException e)
        {
            log.error("Tried to parse a non-number string to an integer", e);
            return 0;
        }
    }

    /**
     * Returns 0 if string is invalid or null.
     * @see JMeterProperty#getFloatValue()
     */
    public float getFloatValue()
    {
        String val = getStringValue();
        if (val == null)
        {
            return 0;
        }
        try
        {
            return Float.parseFloat(val);
        }
        catch (NumberFormatException e)
        {
            log.error("Tried to parse a non-number string to an integer", e);
            return 0;
        }
    }

    /**
     * Returns false if string is invalid or null.
     * @see JMeterProperty#getBooleanValue()
     */
    public boolean getBooleanValue()
    {
        String val = getStringValue();
        if (val == null)
        {
            return false;
        }
        return Boolean.valueOf(val).booleanValue();
    }

    public boolean equals(Object o)
    {
        return compareTo(o) == 0;
    }

    /* (non-Javadoc)
     * @seeComparable#compareTo(Object)
     */
    public int compareTo(Object arg0)
    {
        if (arg0 instanceof JMeterProperty)
        {
            // We don't expect the string values to ever be null.  But (as in
            // bug 19499) sometimes they are.  So have null compare less than
            // any other value.  Log a warning so we can try to find the root
            // cause of the null value.
            String val = getStringValue();
            if (val == null)
            {
                log.warn(
                    "Warning: Unexpected null value for property: " + name);
                
                if (((JMeterProperty)arg0).getStringValue() == null)
                {
                    // Two null values -- return equal
                    return 0;
                }
                else
                {
                    return -1;
                }
            }
            
            return getStringValue().compareTo(
                ((JMeterProperty) arg0).getStringValue());
        }
        else
        {
            return -1;
        }
    }

    /**
     * Get the property type for this property.  Used to convert raw values into
     * JMeterProperties.
     */
    protected Class getPropertyType()
    {
        return getClass();
    }

    protected JMeterProperty getBlankProperty()
    {
        try
        {
            JMeterProperty prop =
                (JMeterProperty) getPropertyType().newInstance();
            if (prop instanceof NullProperty)
            {
                return new StringProperty();
            }
            return prop;
        }
        catch (Exception e)
        {
            return new StringProperty();
        }
    }

    protected Collection normalizeList(Collection coll)
    {
        Iterator iter = coll.iterator();
        Collection newColl = null;
        while (iter.hasNext())
        {
            Object item = iter.next();
            if (newColl == null)
            {
                try
                {
                    newColl = (Collection) coll.getClass().newInstance();
                }
                catch (Exception e)
                {
                    log.error("Bad collection", e);
                }
            }
            newColl.add(convertObject(item));
        }
        if (newColl != null)
        {
            return newColl;
        }
        else
        {
            return coll;
        }
    }

    /**
     * Given a Map, it converts the Map into a collection of JMeterProperty
     * objects, appropriate for a MapProperty object.
     */
    protected Map normalizeMap(Map coll)
    {
        Iterator iter = coll.keySet().iterator();
        Map newColl = null;
        while (iter.hasNext())
        {
            Object item = iter.next();
            Object prop = coll.get(item);
            if (newColl == null)
            {
                try
                {
                    newColl = (Map) coll.getClass().newInstance();
                }
                catch (Exception e)
                {
                    log.error("Bad collection", e);
                }
            }
            newColl.put(item, convertObject(prop));
        }
        if (newColl != null)
        {
            return newColl;
        }
        else
        {
            return coll;
        }
    }

    protected JMeterProperty convertObject(Object item)
    {
        if (item instanceof JMeterProperty)
        {
            return (JMeterProperty) item;
        }
        else if (item instanceof TestElement)
        {
            return new TestElementProperty(
                ((TestElement) item).getPropertyAsString(TestElement.NAME),
                (TestElement) item);
        }
        else if (item instanceof Collection)
        {
            return new CollectionProperty(
                "" + item.hashCode(),
                (Collection) item);
        }
        else if (item instanceof Map)
        {
            return new MapProperty("" + item.hashCode(), (Map) item);
        }
        else
        {
            JMeterProperty prop = getBlankProperty();
            prop.setName(item.toString());
            prop.setObjectValue(item);
            return prop;
        }
    }

    public String toString()
    {
        return getStringValue();
    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.testelement.property.JMeterProperty#mergeIn(org.apache.jmeter.testelement.property.JMeterProperty)
     */
    public void mergeIn(JMeterProperty prop)
    {
    }

}
