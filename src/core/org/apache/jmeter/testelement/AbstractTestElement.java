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
 package org.apache.jmeter.testelement;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.jmeter.control.NextIsNullException;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.MapProperty;
import org.apache.jmeter.testelement.property.MultiProperty;
import org.apache.jmeter.testelement.property.NullProperty;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jmeter.testelement.property.PropertyIteratorImpl;
import org.apache.jmeter.testelement.property.StringProperty;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * @author    Michael Stover
 * @version   $Revision$
 */
public abstract class AbstractTestElement implements TestElement, Serializable
{
    protected static final Logger log = LoggingManager.getLoggerForClass();

    private Map propMap = Collections.synchronizedMap(new HashMap());
    private Set temporaryProperties;

    private boolean runningVersion = false;

    public Object clone()
    {
        TestElement clonedElement = null;
        try
        {
            clonedElement = (TestElement) this.getClass().newInstance();
        }
        catch (Exception e)
        {}

        PropertyIterator iter = propertyIterator();
        while (iter.hasNext())
        {
            clonedElement.setProperty((JMeterProperty) iter.next().clone());
        }
        clonedElement.setRunningVersion(runningVersion);
        return clonedElement;
    }

    public void clear()
    {
        propMap.clear();
    }

    public void removeProperty(String key)
    {
        propMap.remove(key);
    }

    public boolean equals(Object o) //TODO: probably ought to use hashCode() from Map as well
    {
        if (o instanceof AbstractTestElement)
        {
            return ((AbstractTestElement) o).propMap.equals(propMap);
        }
        else
        {
            return false;
        }
    }

    public void addTestElement(TestElement el)
    {
        mergeIn(el);
    }

    public void setName(String name)
    {
        setProperty(new StringProperty(TestElement.NAME, name));
    }

    public String getName()
    {
        return getProperty(TestElement.NAME).getStringValue();
    }

    /**
     * Get the named property.  If it doesn't exist, a NullProperty object is
     * returned.
     */
    public JMeterProperty getProperty(String key)
    {
        JMeterProperty prop = (JMeterProperty) propMap.get(key);
        if (prop == null)
        {
            prop = new NullProperty(key);
        }
        return prop;
    }

    public void traverse(TestElementTraverser traverser)
    {
        PropertyIterator iter = propertyIterator();
        traverser.startTestElement(this);
        while (iter.hasNext())
        {
            traverseProperty(traverser, iter.next());
        }
        traverser.endTestElement(this);
    }

    protected void traverseProperty(
        TestElementTraverser traverser,
        JMeterProperty value)
    {
        traverser.startProperty(value);
        if (value instanceof TestElementProperty)
        {
            ((TestElement) value.getObjectValue()).traverse(traverser);
        }
        else if (value instanceof CollectionProperty)
        {
            traverseCollection((CollectionProperty) value, traverser);
        }
        else if (value instanceof MapProperty)
        {
            traverseMap((MapProperty) value, traverser);
        }
        traverser.endProperty(value);
    }

    protected void traverseMap(MapProperty map, TestElementTraverser traverser)
    {
        PropertyIterator iter = map.valueIterator();
        while (iter.hasNext())
        {
            traverseProperty(traverser, iter.next());
        }
    }

    protected void traverseCollection(
        CollectionProperty col,
        TestElementTraverser traverser)
    {
        PropertyIterator iter = col.iterator();
        while (iter.hasNext())
        {
            traverseProperty(traverser, iter.next());
        }
    }

    public int getPropertyAsInt(String key)
    {
        return getProperty(key).getIntValue();
    }

    public boolean getPropertyAsBoolean(String key)
    {
        return getProperty(key).getBooleanValue();
    }

	public boolean getPropertyAsBoolean(String key,boolean defaultVal)
	{
		JMeterProperty jmp = getProperty(key); 
		return jmp instanceof NullProperty ? defaultVal: jmp.getBooleanValue();
	}

    public float getPropertyAsFloat(String key)
    {
        return getProperty(key).getFloatValue();
    }

    public long getPropertyAsLong(String key)
    {
        return getProperty(key).getLongValue();
    }

    public double getPropertyAsDouble(String key)
    {
        return getProperty(key).getDoubleValue();
    }

    public String getPropertyAsString(String key)
    {
        return getProperty(key).getStringValue();
    }

    protected void addProperty(JMeterProperty property)
    {
        if (isRunningVersion())
        {
            setTemporary(property);
        }
        else
        {
            clearTemporary(property);
        }
        JMeterProperty prop = getProperty(property.getName());

        if (prop instanceof NullProperty
            || (prop instanceof StringProperty
                && prop.getStringValue().equals("")))
        {
            propMap.put(property.getName(), property);
        }
        else
        {
            prop.mergeIn(property);
        }
    }
    
    protected void clearTemporary(JMeterProperty property)
    {
        if(temporaryProperties != null)
        {
            temporaryProperties.remove(property);
        }
    }

    /**
     * Log the properties of the test element
     * @see TestElement#setProperty(JMeterProperty)
     */
    protected void logProperties()
    {
        if (log.isDebugEnabled())
        {
            PropertyIterator iter = propertyIterator();
            while (iter.hasNext())
            {
                JMeterProperty prop = iter.next();
                log.debug(
                    "Property "
                        + prop.getName()
                        + " is temp? "
                        + isTemporary(prop)
                        + " and is a "
                        + prop.getObjectValue());
            }
        }
    }

    public void setProperty(JMeterProperty property)
    {
        if (isRunningVersion())
        {
            if (getProperty(property.getName()) instanceof NullProperty)
            {
                addProperty(property);
            }
            else
            {
                getProperty(property.getName()).setObjectValue(
                    property.getObjectValue());
            } 
        }
        else
        {
            propMap.put(property.getName(), property);
        }
    }

    public void setProperty(String name, String value)
    {
        setProperty(new StringProperty(name, value));
    }

    public PropertyIterator propertyIterator()
    {
        return new PropertyIteratorImpl(propMap.values());
    }

    protected void mergeIn(TestElement element)
    {
        PropertyIterator iter = element.propertyIterator();
        while (iter.hasNext())
        {
            JMeterProperty prop = iter.next();
            addProperty(prop);
        }
    }

    /**
     * Returns the runningVersion.
     */
    public boolean isRunningVersion()
    {
        return runningVersion;
    }

    /**
     * Sets the runningVersion.
     * @param runningVersion the runningVersion to set
     */
    public void setRunningVersion(boolean runningVersion)
    {
        this.runningVersion = runningVersion;
        PropertyIterator iter = propertyIterator();
        while (iter.hasNext())
        {
            iter.next().setRunningVersion(runningVersion);
        }
    }

    public void recoverRunningVersion()
    {
        Iterator iter = propMap.entrySet().iterator();
        while (iter.hasNext())
        {
            Map.Entry entry = (Map.Entry)iter.next();
            JMeterProperty prop = (JMeterProperty)entry.getValue();
            if (isTemporary(prop))
            {
                iter.remove();
                clearTemporary(prop);
            }
            else
            {
                prop.recoverRunningVersion(this);
            }
        }
        emptyTemporary();
    }
    
    protected void emptyTemporary()
    {
        if(temporaryProperties != null)
        {
            temporaryProperties.clear();
        }
    }

    protected Sampler nextIsNull() throws NextIsNullException
    {
        return null;
    }
    /* (non-Javadoc)
     * @see org.apache.jmeter.testelement.TestElement#isTemporary(org.apache.jmeter.testelement.property.JMeterProperty)
     */
    public boolean isTemporary(JMeterProperty property)
    {
        if(temporaryProperties == null)
        {
            return false;
        }
        else
        {
            return temporaryProperties.contains(property);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.testelement.TestElement#setTemporary(org.apache.jmeter.testelement.property.JMeterProperty)
     */
    public void setTemporary(JMeterProperty property)
    {
        if(temporaryProperties == null)
        {
            temporaryProperties = new HashSet();
        }
        temporaryProperties.add(property);
        if(property instanceof MultiProperty)
        {
            PropertyIterator iter = ((MultiProperty)property).iterator();
            while(iter.hasNext())
            {
                setTemporary(iter.next());
            }
        }
    }

}
