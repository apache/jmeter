package org.apache.jmeter.testelement;

/**
 * @author Administrator
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 */
public class TestElementProperty
{
    Object key;
    Object value;
    
    public TestElementProperty()
    {
    }
    
    public TestElementProperty(Object key, Object value)
    {
        setKey(key);
        setValue(value);
    }
    
    public TestElementProperty(Object key)
    {
        setKey(key);
    }
    /**
     * Returns the key.
     * @return Object
     */
    public Object getKey()
    {
        return key;
    }

    /**
     * Returns the value.
     * @return Object
     */
    public Object getValue()
    {
        return value;
    }

    /**
     * Sets the key.
     * @param key The key to set
     */
    public void setKey(Object key)
    {
        this.key = key;
    }

    /**
     * Sets the value.
     * @param value The value to set
     */
    public void setValue(Object value)
    {
        this.value = value;
    }

}
