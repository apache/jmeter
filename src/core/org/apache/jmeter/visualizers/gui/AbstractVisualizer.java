package org.apache.jmeter.visualizers.gui;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import javax.swing.JPopupMenu;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.apache.jmeter.exceptions.IllegalUserActionException;
import org.apache.jmeter.gui.AbstractJMeterGuiComponent;
import org.apache.jmeter.gui.NamePanel;
import org.apache.jmeter.gui.util.FilePanel;
import org.apache.jmeter.gui.util.MenuFactory;
import org.apache.jmeter.reporters.AbstractListenerElement;
import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.Visualizer;

/****************************************
 * Title: JMeter Description: Copyright: Copyright (c) 2000 Company: Apache
 *
 *@author    Michael Stover
 *@created   $Date$
 *@version   1.0
 ***************************************/

public abstract class AbstractVisualizer extends AbstractJMeterGuiComponent
	implements Visualizer, ChangeListener {

		private FilePanel filePanel;
	ResultCollector collector;

	/****************************************
	 * !ToDo (Constructor description)
	 ***************************************/
	public AbstractVisualizer() {
		super();
		filePanel = new FilePanel(this);
	}


	protected ResultCollector getModel() {
		return collector;
	}

	protected FilePanel getFilePanel() {
		return filePanel;
	}

	public void setFile(String filename) {
		filePanel.setFilename(filename);
	}

	public String getFile() {
		return filePanel.getFilename();
	}

	/****************************************
	 * !ToDo (Method description)
	 *
	 *@return   !ToDo (Return description)
	 ***************************************/
	public JPopupMenu createPopupMenu() {
		return MenuFactory.getDefaultVisualizerMenu();
	}

	public void stateChanged(ChangeEvent e) {
		System.out.println("getting new collector");
		collector = (ResultCollector) createTestElement();
	}

	/****************************************
	 * !ToDoo (Method description)
	 *
	 *@return   !ToDo (Return description)
	 ***************************************/
	public Collection getMenuCategories() {
		return Arrays.asList(new String[] { MenuFactory.LISTENERS });
	}

	public TestElement createTestElement() {
		if (collector == null) {
			collector = new ResultCollector();
		}
		configureTestElement(collector);
		try {
			if (!getFile().equals("")) {
				try {
					collector.setFilename(getFile());
				}
				catch (IllegalUserActionException e) {
					JMeterUtils.reportErrorToUser(e.getMessage());
					setFile("");
				}
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return (TestElement)collector.clone();
	}
	
	public void configure(TestElement el)
	{
		super.configure(el);
		setFile(el.getPropertyAsString(ResultCollector.FILENAME));
	}

	/****************************************
	 * !ToDo (Method description)
	 *
	 *@param mc  !ToDo (Parameter description)
	 ***************************************/
	protected void configureTestElement(AbstractListenerElement mc) {
		super.configureTestElement(mc);
		mc.setListener(this);
	}
}