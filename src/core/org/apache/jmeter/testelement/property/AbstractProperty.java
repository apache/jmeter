package org.apache.jmeter.testelement.property;

import java.util.HashMap;
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
        if(this.getClass().equals(prop.getClass()))
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

    protected void setName(String name)
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
        if(ownerMap == null)
        {
            return false;
        }
        else
        {
            return ((boolean[])ownerMap.get(owner))[0];
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
        if(ownerMap == null)
        {
            ownerMap = new HashMap();
        }
        boolean[] temp = (boolean[])ownerMap.get(owner);
        if(temp != null)
        {
            temp[0] = temporary;
        }
        else
        {
            temp = new boolean[]{temporary};
            ownerMap.put(owner,temp);
        }
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
            log.error("Tried to parse a non-number string to an integer", e);
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
            log.error("Tried to parse a non-number string to an integer", e);
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
    
    /**
         * @see java.lang.Comparable#compareTo(java.lang.Object)
         */
        public int compareTo(Object arg0)
        {
            if(arg0 instanceof JMeterProperty)
            {
                return getStringValue().compareTo(((JMeterProperty)arg0).getStringValue());
            }
            else
            {
                return -1;
            }
        }

}
