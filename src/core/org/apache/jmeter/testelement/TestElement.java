package org.apache.jmeter.testelement;
import java.util.Collection;

import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.PropertyIterator;

/****************************************
 * <p>
 *
 * Title: Jakarta JMeter</p> <p>
 *
 * Description: Load testing software</p> <p>
 *
 * Copyright: Copyright (c) 2002</p> <p>
 *
 * Company: Apache Foundation</p>
 *
 *@author    Michael Stover
 *@created   $Date$
 *@version   1.0
 ***************************************/

public interface TestElement extends Cloneable
{
	/****************************************
	 * !ToDo (Field description)
	 ***************************************/
	public final static String NAME = "TestElement.name";
	/****************************************
	 * !ToDo (Field description)
	 ***************************************/
	public final static String GUI_CLASS = "TestElement.gui_class";
	/****************************************
       
        /****************************************
         * This Filed used as identification for each object state
         ***************************************/
        public final static String ENABLED = "TestElement.enabled";

	public final static String TEST_CLASS = "TestElement.test_class";


	/****************************************
	 * !ToDo
	 *
	 *@param child  !ToDo
	 ***************************************/
	public void addTestElement(TestElement child);
    
    /**
     * Returns true or false whether the element is the running version.
     * @return boolean
     */
    public boolean isRunningVersion();
    
    /**
     * Return a property as a boolean value.
     * @param key
     * @return boolean
     */
    public boolean getPropertyAsBoolean(String key);
    
    /**
     * Make the test element the running version, or make it no longer the
     * running version. This tells the test element that it's current state must
     * be retrievable by a call to recoverRunningVersion().  It is kind of like
     * making the TestElement Read- Only, but not as strict.  Changes can be
     * made and the element can be modified, but the state of the element at the
     * time of the call to setRunningVersion() must be recoverable.
     * @param perm
     */
    public void setRunningVersion(boolean run);
    
    /**
     * Tells the test element to return to the state it was in when
     * makeRunningVersion() was called.
     */
    public void recoverRunningVersion();

	/****************************************
	 * !ToDoo (Method description)
	 *
	 *@return   !ToDo (Return description)
	 ***************************************/
	public Collection getPropertyNames();
    
    /**
     * Clear the TestElement of all data.
     */
    public void clear();

	/****************************************
	 * !ToDoo (Method description)
	 *
	 *@param key  !ToDo (Parameter description)
	 *@return     !ToDo (Return description)
	 ***************************************/
	public Object getProperty(String key);

	public String getPropertyAsString(String key);
    
    /**
     * Adds a property to the TestElement.
     * @param property
     */
    public void addProperty(JMeterProperty property);
    
    /**
     * Get a Property Iterator for the TestElements properties.
     * @return PropertyIterator
     */
    public PropertyIterator propertyIterator();

	/****************************************
	 * !ToDo (Method description)
	 *
	 *@param key       !ToDo (Parameter description)
	 *@param property  !ToDo (Parameter description)
	 ***************************************/
	public void setProperty(String key, Object property);

	public void removeProperty(String key);

	//lifecycle methods

	/****************************************
	 * !ToDo (Method description)
	 *
	 *@return   !ToDo (Return description)
	 ***************************************/
	public Object clone();
    
    /**
     * Convenient way to traverse a test element
     * @param traverser
     */
    public void traverse(TestElementTraverser traverser);
}
