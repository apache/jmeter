package org.apache.jmeter.testelement;
import java.util.*;

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
	 * !ToDo (Field description)
	 ***************************************/
	public final static String TEST_CLASS = "TestElement.test_class";


	/****************************************
	 * !ToDo
	 *
	 *@param child  !ToDo
	 ***************************************/
	public void addTestElement(TestElement child);

	/****************************************
	 * !ToDoo (Method description)
	 *
	 *@return   !ToDo (Return description)
	 ***************************************/
	public Collection getPropertyNames();

	/****************************************
	 * !ToDoo (Method description)
	 *
	 *@param key  !ToDo (Parameter description)
	 *@return     !ToDo (Return description)
	 ***************************************/
	public Object getProperty(String key);

	public String getPropertyAsString(String key);

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
}
