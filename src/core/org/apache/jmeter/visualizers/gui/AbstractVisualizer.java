package org.apache.jmeter.visualizers.gui;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import javax.swing.JCheckBox;
import javax.swing.JPopupMenu;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.jmeter.exceptions.IllegalUserActionException;
import org.apache.jmeter.gui.AbstractJMeterGuiComponent;
import org.apache.jmeter.gui.UnsharedComponent;
import org.apache.jmeter.gui.util.FilePanel;
import org.apache.jmeter.gui.util.MenuFactory;
import org.apache.jmeter.reporters.AbstractListenerElement;
import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.Visualizer;
import org.apache.log.Hierarchy;
import org.apache.log.Logger;

/****************************************
 * Title: JMeter Description: Copyright: Copyright (c) 2000 Company: Apache
 *
 *@author    Michael Stover
 *@created   $Date$
 *@version   1.0
 ***************************************/

public abstract class AbstractVisualizer extends AbstractJMeterGuiComponent
	implements Visualizer, ChangeListener,UnsharedComponent {
		
	transient private static Logger log = Hierarchy.getDefaultHierarchy().getLoggerFor(
			"jmeter.gui");

		private FilePanel filePanel;
		private JCheckBox errorLogging;
	ResultCollector collector;

	/****************************************
	 * !ToDo (Constructor description)
	 ***************************************/
	public AbstractVisualizer() {
		super();
		filePanel = new FilePanel(this,
				JMeterUtils.getResString("file_visualizer_output_file"));
		errorLogging = new JCheckBox(JMeterUtils.getResString("log_errors_only"));
	}
	
	protected JCheckBox getErrorLoggingCheckbox()
	{
		return errorLogging;
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
		log.info("getting new collector");
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
		collector.setErrorLogging(errorLogging.isSelected());
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
			log.error("",e);
		}
		return (TestElement)collector.clone();
	}
	
	public void configure(TestElement el)
	{
		super.configure(el);
		setFile(el.getPropertyAsString(ResultCollector.FILENAME));
		ResultCollector rc = (ResultCollector)el;
		errorLogging.setSelected(rc.isErrorLogging());
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