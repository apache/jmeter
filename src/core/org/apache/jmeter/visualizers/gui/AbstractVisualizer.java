package org.apache.jmeter.visualizers.gui;
import java.awt.Component;
import java.awt.FlowLayout;
import java.util.Arrays;
import java.util.Collection;

import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

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

public abstract class AbstractVisualizer extends AbstractJMeterGuiComponent implements Visualizer, ChangeListener, UnsharedComponent
{

    transient private static Logger log = Hierarchy.getDefaultHierarchy().getLoggerFor("jmeter.gui");

    private FilePanel filePanel;
    private JCheckBox errorLogging;
    ResultCollector collector;

    /****************************************
     * !ToDo (Constructor description)
     ***************************************/
    public AbstractVisualizer()
    {
        super();
        filePanel = new FilePanel(this, JMeterUtils.getResString("file_visualizer_output_file"));
        errorLogging = new JCheckBox(JMeterUtils.getResString("log_errors_only"));
    }

    protected JCheckBox getErrorLoggingCheckbox()
    {
        return errorLogging;
    }

    protected ResultCollector getModel()
    {
        return collector;
    }

    protected Component getFilePanel()
    {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(filePanel);
        panel.add(getErrorLoggingCheckbox());
        return panel;
    }

    public void setFile(String filename)
    {
        filePanel.setFilename(filename);
    }

    public String getFile()
    {
        return filePanel.getFilename();
    }
    
    

    /****************************************
     * !ToDo (Method description)
     *
     *@return   !ToDo (Return description)
     ***************************************/
    public JPopupMenu createPopupMenu()
    {
        return MenuFactory.getDefaultVisualizerMenu();
    }

    public void stateChanged(ChangeEvent e)
    {
        log.info("getting new collector");
        collector = (ResultCollector) createTestElement();
        try
        {
            collector.loadExistingFile();
        }
        catch(Exception err)
        {
            log.debug("Error occurred while loading file",err);
        }
    }

    /****************************************
     * !ToDoo (Method description)
     *
     *@return   !ToDo (Return description)
     ***************************************/
    public Collection getMenuCategories()
    {
        return Arrays.asList(new String[] { MenuFactory.LISTENERS });
    }

    public TestElement createTestElement()
    {
        if (collector == null)
        {
            collector = new ResultCollector();
        }
        modifyTestElement(collector);
        return (TestElement) collector.clone();
    }

    /**
     * Modifies a given TestElement to mirror the data in the gui components.
     * @see org.apache.jmeter.gui.JMeterGUIComponent#modifyTestElement(TestElement)
     */
    public void modifyTestElement(TestElement c)
    {
        configureTestElement((AbstractListenerElement)c);
        if (c instanceof ResultCollector)
        {
            ResultCollector rc = (ResultCollector) c;
            rc.setErrorLogging(errorLogging.isSelected());
            rc.setFilename(getFile());
            collector = rc;
        }
    }

    public void configure(TestElement el)
    {
        super.configure(el);
        setFile(el.getPropertyAsString(ResultCollector.FILENAME));
        ResultCollector rc = (ResultCollector) el;
        errorLogging.setSelected(rc.isErrorLogging());
    }

    /****************************************
     * !ToDo (Method description)
     *
     *@param mc  !ToDo (Parameter description)
     ***************************************/
    protected void configureTestElement(AbstractListenerElement mc)
    {
        super.configureTestElement(mc);
        mc.setListener(this);
    }
    /**
     * override parent method to add the file panel to the title panel.
     */
    protected Box makeTitlePanel()
    {
        Box box = super.makeTitlePanel();
        box.add(getFilePanel());
        return box;
    }

}