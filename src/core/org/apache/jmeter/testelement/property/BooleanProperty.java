package org.apache.jmeter.testelement.property;

/**
 * @author Administrator
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
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
     * @see org.apache.jmeter.testelement.property.JMeterProperty#getStringValue()
     */
    public String getStringValue()
    {
        return value ? "true" : "false";
    }

    /**
     * @see org.apache.jmeter.testelement.property.JMeterProperty#getObjectValue()
     */
    public Object getObjectValue()
    {
        return new Boolean(value);
    }

    /**
     * @see java.lang.Object#clone()
     */
    public Object clone()
    {
        BooleanProperty prop = (BooleanProperty)super.clone();
        prop.value = value;
        return prop;
    }

    /**
     * @see org.apache.jmeter.testelement.property.JMeterProperty#getBooleanValue()
     */
    public boolean getBooleanValue()
    {
        return super.getBooleanValue();
    }

}
