package org.apache.jmeter.testelement.property;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * @author Administrator
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 */
public abstract class AbstractProperty implements JMeterProperty
{
    protected static Logger log = LoggingManager.getLoggerFor(JMeterUtils.PROPERTIES);
    private String name;
    private boolean runningVersion = false;
    private Map ownerMap;

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

    /**
     * @see org.apache.jmeter.testelement.property.JMeterProperty#isRunningVersion()
     */
    public boolean isRunningVersion()
    {
        return runningVersion;
    }

    /**
     * @see org.apache.jmeter.testelement.property.JMeterProperty#getName()
     */
    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * @see org.apache.jmeter.testelement.property.JMeterProperty#setRunningVersion(boolean)
     */
    public void setRunningVersion(boolean runningVersion)
    {
        this.runningVersion = runningVersion;
    }

    /**
     * @see org.apache.jmeter.testelement.property.JMeterProperty#isTemporary()
     */
    public boolean isTemporary(TestElement owner)
    {
        if (ownerMap == null)
        {
            return false;
        }
        else
        {
            boolean[] temp = (boolean[]) ownerMap.get(owner);
            if (temp == null)
            {
                return false;
            }
            return temp[0];
        }
    }

    /**
     * @see org.apache.jmeter.testelement.property.JMeterProperty#merge(org.apache.jmeter.testelement.property.JMeterProperty)
     */
    public void mergeIn(JMeterProperty prop)
    {}

    /**
     * @see org.apache.jmeter.testelement.property.JMeterProperty#setTemporary(boolean)
     */
    public void setTemporary(boolean temporary, TestElement owner)
    {
        if (ownerMap == null)
        {
            ownerMap = new HashMap();
        }
        boolean[] temp = (boolean[]) ownerMap.get(owner);
        if (temp != null)
        {
            temp[0] = temporary;
        }
        else
        {
            temp = new boolean[] { temporary };
            ownerMap.put(owner, temp);
        }
    }

    public void clearTemporary(TestElement owner)
    {
        if (ownerMap == null)
        {
            return;
        }
        ownerMap.remove(owner);
    }

    /**
     * @see java.lang.Object#clone()
     */
    public Object clone()
    {
        try
        {
            AbstractProperty prop = (AbstractProperty) this.getClass().newInstance();
            prop.name = name;
            prop.runningVersion = runningVersion;
            prop.ownerMap = ownerMap;
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
         * returns 0 if string is invalid or null.
         * @see org.apache.jmeter.testelement.property.JMeterProperty#getIntValue()
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
     * returns 0 if string is invalid or null.
     * @see org.apache.jmeter.testelement.property.JMeterProperty#getLongValue()
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
     * returns 0 if string is invalid or null.
     * @see org.apache.jmeter.testelement.property.JMeterProperty#getDoubleValue()
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
         * @see org.apache.jmeter.testelement.property.JMeterProperty#recoverRunningVersion()
         */
    public void recoverRunningVersion(TestElement owner)
    {}

    /**
     * returns 0 if string is invalid or null.
     * @see org.apache.jmeter.testelement.property.JMeterProperty#getFloatValue()
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
     * @see org.apache.jmeter.testelement.property.JMeterProperty#getBooleanValue()
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
        log.debug("Testing whether " + this + " is equal to " + o);
        return compareTo(o) == 0;
    }

    /**
         * @see java.lang.Comparable#compareTo(java.lang.Object)
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
            if (val == null) {
                log.warn(
                    "Warning: Unexpected null value for property: " + name);
                
                if (((JMeterProperty)arg0).getStringValue() == null) {
                    // Two null values -- return equal
                    return 0;
                } else {
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
     * @return Class
     */
    protected Class getPropertyType()
    {
        return getClass();
    }

    protected JMeterProperty getBlankProperty()
    {
        try
        {
            JMeterProperty prop = (JMeterProperty) getPropertyType().newInstance();
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
     * @param coll
     * @return Map
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
            newColl.put(item, convertObject(item));
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
            return new TestElementProperty(((TestElement) item).getPropertyAsString(TestElement.NAME), (TestElement) item);
        }
        else if (item instanceof Collection)
        {
            return new CollectionProperty("" + item.hashCode(),(Collection) item);
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

}
