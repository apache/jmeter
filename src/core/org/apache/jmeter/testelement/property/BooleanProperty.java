package org.apache.jmeter.testelement.property;

/**
 * @version $Revision$
 */
public class BooleanProperty extends AbstractProperty
{
    boolean value;
    
    public BooleanProperty(String name,boolean v)
    {
        super(name);
        value = v;
    }
    
    public BooleanProperty()
    {
        super();
    }
    
    public void setObjectValue(Object v)
    {
        if(v instanceof Boolean)
        {
            value = ((Boolean)v).booleanValue();
        }
        else
        {
            value = Boolean.valueOf(v.toString()).booleanValue();
        }
    }

    /**
     * @see JMeterProperty#getStringValue()
     */
    public String getStringValue()
    {
        return value ? "true" : "false";
    }

    /**
     * @see JMeterProperty#getObjectValue()
     */
    public Object getObjectValue()
    {
        return new Boolean(value);
    }

    /**
     * @see Object#clone()
     */
    public Object clone()
    {
        BooleanProperty prop = (BooleanProperty)super.clone();
        prop.value = value;
        return prop;
    }

    /**
     * @see JMeterProperty#getBooleanValue()
     */
    public boolean getBooleanValue()
    {
        return value;
    }
}
