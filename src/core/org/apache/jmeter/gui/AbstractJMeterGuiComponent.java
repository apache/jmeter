package org.apache.jmeter.gui;

import java.util.Collection;

import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import org.apache.jmeter.testelement.TestElement;

/**
 * @author mstover
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 */
public abstract class AbstractJMeterGuiComponent
	extends JPanel
	implements JMeterGUIComponent {
		
		private boolean enabled = true;

	public AbstractJMeterGuiComponent() {
		namePanel = new NamePanel();
		setName(getStaticLabel());
	}

	/**
	 * @see JMeterGUIComponent#setName(String)
	 */
	public void setName(String name) {
		namePanel.setName(name);
	}
	
	public boolean isEnabled()
	{
		return enabled;
	}
	
	public void setEnabled(boolean e)
	{
		enabled = e;
	}

	/**
	 * @see JMeterGUIComponent#getName()
	 */
	public String getName() {
		return getNamePanel().getName();
	}

	protected NamePanel getNamePanel() {
		return namePanel;
	}

	protected NamePanel namePanel;
	
	/****************************************
	 * !ToDo (Method description)
	 *
	 *@param element  !ToDo (Parameter description)
	 ***************************************/
	public void configure(TestElement element)
	{
		setName((String)element.getProperty(TestElement.NAME));
	}

	  protected void configureTestElement(TestElement mc)
	{
		mc.setProperty(TestElement.NAME, getName());
		mc.setProperty(TestElement.GUI_CLASS, this.getClass().getName());
		mc.setProperty(TestElement.TEST_CLASS, mc.getClass().getName());
	}


}