package org.apache.jmeter.testelement.property;

import java.io.Serializable;

import org.apache.jmeter.testelement.TestElement;

/**
 * @author Administrator
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 */
public interface JMeterProperty extends Serializable, Cloneable, Comparable
{
    /**
     * Returns whether the property is a running version.
     * @return boolean
     */
    public boolean isRunningVersion();
    
    /**
     * The name of the property.  Typically this should match the name that keys
     * the property's location in the test elements Map.
     * @return String
     */
    public String getName();
    
    /**
     * Make the property a running version or turn it off as the running
     * version.  A property that is made a running version will preserve the
     * current state in such a way that it is retrievable by a future call to
     * 'recoverRunningVersion()'.  Additionally, a property that is a running
     * version will resolve all functions prior to returning it's property
     * value.  A non-running version property will return functions as their
     * uncompiled string representation.
     * @param runningVersion
     */
    public void setRunningVersion(boolean runningVersion);
    
    /**
     * Tell the property to revert to the state at the time 
     * setRunningVersion(true) was called.
     */
    public void recoverRunningVersion(TestElement owner);
    
    /**
     * Indicates whether the property is a temporary property. A property newly
     * created and added to a test element that is currently a running version
     * should be made temporary.
     * @return boolean
     */
    public boolean isTemporary(TestElement owner);
    
    /**
     * Take the given property object and merge it's value with the current
     * property object.  For most property types, this will simply be ignored.
     * But for collection properties and test element properties, more complex
     * behavior is required.
     * @param prop
     */
    public void mergeIn(JMeterProperty prop);
    
    /**
     * A property newly created and added to a test element that is currently a
     * running version should be made temporary.  This indicates it is not part
     * of the running version of the test element and will be deleted when the
     * test element recovers state.
     * @param temporary
     */
    public void setTemporary(boolean temporary, TestElement owner);
    
    public int getIntValue();
    
    public long getLongValue();
    
    public double getDoubleValue();
    
    public float getFloatValue();
    
    public boolean getBooleanValue();
    
    public String getStringValue();
    
    public Object getObjectValue();
    
    public Object clone();
}
