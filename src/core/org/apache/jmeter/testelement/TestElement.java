package org.apache.jmeter.testelement;

import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.PropertyIterator;

/**
 * @author    Michael Stover
 * @version   $Revision$
 */

public interface TestElement extends Cloneable
{
    public final static String NAME = "TestElement.name";
    public final static String GUI_CLASS = "TestElement.gui_class";
    public final static String ENABLED = "TestElement.enabled";
    public final static String TEST_CLASS = "TestElement.test_class";

    public void addTestElement(TestElement child);
    
    public void setProperty(String key,String value);
    
    /**
     * Returns true or false whether the element is the running version.
     */
    public boolean isRunningVersion();
    
    /**
     * Test whether a given property is only a temporary resident of the TestElement
     * @param property
     * @return
     * boolean
     */
    public boolean isTemporary(JMeterProperty property);
    
    /**
     * Indicate that the given property should be only a temporary property in the TestElement
     * @param property
     * void
     */
    public void setTemporary(JMeterProperty property);
    
    /**
     * Return a property as a boolean value.
     */
    public boolean getPropertyAsBoolean(String key);
    
    public long getPropertyAsLong(String key);
    
    public int getPropertyAsInt(String key);
    
    public float getPropertyAsFloat(String key);
    
    /**
     * Make the test element the running version, or make it no longer the
     * running version. This tells the test element that it's current state must
     * be retrievable by a call to recoverRunningVersion().  It is kind of like
     * making the TestElement Read- Only, but not as strict.  Changes can be
     * made and the element can be modified, but the state of the element at the
     * time of the call to setRunningVersion() must be recoverable.
     */
    public void setRunningVersion(boolean run);
    
    /**
     * Tells the test element to return to the state it was in when
     * makeRunningVersion() was called.
     */
    public void recoverRunningVersion();
    
    /**
     * Clear the TestElement of all data.
     */
    public void clear();

    public String getPropertyAsString(String key);
    
    /**
     * Sets and overwrites a property in the TestElement.  This call will be 
     * ignored if the TestElement is currently a "running version".
     */
    public void setProperty(JMeterProperty property);
    
    /**
     * Given the name of the property, returns the appropriate property from
     * JMeter.  If it is null, a NullProperty object will be returned.
     */
    public JMeterProperty getProperty(String propName);
    
    /**
     * Get a Property Iterator for the TestElements properties.
     * @return PropertyIterator
     */
    public PropertyIterator propertyIterator();

    public void removeProperty(String key);

    //lifecycle methods

    public Object clone();
    
    /**
     * Convenient way to traverse a test element.
     */
    public void traverse(TestElementTraverser traverser);
}
