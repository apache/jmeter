package org.apache.jmeter.gui;
import java.util.Collection;
import javax.swing.JPopupMenu;
import org.apache.jmeter.testelement.TestElement;
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

public interface JMeterGUIComponent
{

	/****************************************
	 * !ToDo (Method description)
	 *
	 *@param name  !ToDo (Parameter description)
	 ***************************************/
	public void setName(String name);

	/****************************************
	 * !ToDoo (Method description)
	 *
	 *@return   !ToDo (Return description)
	 ***************************************/
	public String getName();

	/****************************************
	 * !ToDoo (Method description)
	 *
	 *@return   !ToDo (Return description)
	 ***************************************/
	public String getStaticLabel();

	/****************************************
	 * !ToDo (Method description)
	 *
	 *@return   !ToDo (Return description)
	 ***************************************/
	public TestElement createTestElement();
	
	/**
	 * Test GUI elements can be turned disabled, in which case
	 * they do not become part of the test when run.
	 */
	public boolean isEnabled();
	
	public void setEnabled(boolean enabled);

	/****************************************
	 * !ToDo (Method description)
	 *
	 *@return   !ToDo (Return description)
	 ***************************************/
	public JPopupMenu createPopupMenu();

	/****************************************
	 * !ToDo (Method description)
	 *
	 *@param element  !ToDo (Parameter description)
	 ***************************************/
	public void configure(TestElement element);

	/****************************************
	 * This is the list of menu categories this gui component will be available
	 * under. For instance, if this represents a Controller, then the
	 * MenuFactory.CONTROLLERS category should be in the returned collection.
	 *
	 *@return   !ToDo (Return description)
	 ***************************************/
	public Collection getMenuCategories();
}
