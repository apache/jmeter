package org.apache.jmeter.visualizers.gui;

import java.awt.Component;
import java.awt.Container;
import java.util.Arrays;
import java.util.Collection;

import javax.swing.JCheckBox;
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

/**
 * This is the base class for JMeter GUI components which can display test
 * results in some way.
 * Copyright: 2000,2003
 *
 * @author    Michael Stover
 * @version   $Revision$
 */
public abstract class AbstractVisualizer
    extends AbstractJMeterGuiComponent
    implements Visualizer, ChangeListener, UnsharedComponent
{
    /** Logging. */
    protected static transient Logger log =
        Hierarchy.getDefaultHierarchy().getLoggerFor("jmeter.gui");

    /** A panel allowing results to be saved. */
    private FilePanel filePanel;
    
    /** A checkbox choosing whether or not only errors should be logged. */
    private JCheckBox errorLogging;
    
    ResultCollector collector;

    /****************************************
     * !ToDo (Constructor description)
     ***************************************/
    public AbstractVisualizer()
    {
        super();
        
        errorLogging =
            new JCheckBox(JMeterUtils.getResString("log_errors_only"));

        filePanel = new FilePanel(
                this,
                JMeterUtils.getResString("file_visualizer_output_file"));

        filePanel.add(errorLogging);
                
    }

    /**
     * Gets the checkbox which selects whether or not only errors should be
     * logged.  Subclasses don't normally need to worry about this checkbox,
     * because it is automatically added to the GUI in
     * {@link #makeTitlePanel()}, and the behavior is handled in this base
     * class.
     * 
     * @return the error logging checkbox
     */
    protected JCheckBox getErrorLoggingCheckbox()
    {
        return errorLogging;
    }

    protected ResultCollector getModel()
    {
        return collector;
    }

   /**
    * Gets the file panel which allows the user to save results to a file.
    * Subclasses don't normally need to worry about this panel, because it
    * is automatically added to the GUI in {@link #makeTitlePanel()}, and the
    * behavior is handled in this base class.
    * 
    * @return the file panel allowing users to save results
    */
    protected Component getFilePanel()
    {
        return filePanel;
    }

    /**
     * Sets the filename which results will be saved to.  This will set the
     * filename in the FilePanel.  Subclasses don't normally need to call this
     * method, because configuration of the FilePanel is handled in this base
     * class.
     * 
     * @param filename the new filename
     * 
     * @see #getFilePanel()
     */
    public void setFile(String filename)
    {
        // TODO: Does this method need to be public?  It isn't currently
        // called outside of this class.
        filePanel.setFilename(filename);
    }

    /**
     * Gets the filename which has been entered in the FilePanel.  Subclasses
     * don't normally need to call this method, because configuration of the
     * FilePanel is handled in this base class.
     * 
     * @return the current filename
     * 
     * @see #getFilePanel()
     */
    public String getFile()
    {
        // TODO: Does this method need to be public?  It isn't currently
        // called outside of this class.
        return filePanel.getFilename();
    }
    
    

    /**
     * When a user right-clicks on the component in the test tree, or
     * selects the edit menu when the component is selected, the 
     * component will be asked to return a JPopupMenu that provides
     * all the options available to the user from this component.
     * <p>
     * This implementation returns menu items appropriate for most
     * visualizer components.
     *
     * @return   a JPopupMenu appropriate for the component.
     */
    public JPopupMenu createPopupMenu()
    {
        return MenuFactory.getDefaultVisualizerMenu();
    }

    /**
     * Invoked when the target of the listener has changed its state.  This
     * implementation assumes that the target is the FilePanel, and will
     * update the result collector for the new filename.
     * 
     * @param e the event that has occurred
     */
    public void stateChanged(ChangeEvent e)
    {
        log.info("getting new collector");
        collector = (ResultCollector) createTestElement();
        try
        {
            collector.loadExistingFile();
        }
        catch (Exception err)
        {
            log.debug("Error occurred while loading file", err);
        }
    }

    /**
     * This is the list of menu categories this gui component will be available
     * under. This implementation returns
     * {@link org.apache.jmeter.gui.util.MenuFactory#LISTENERS}, which
     * is appropriate for most visualizer components.
     *
     * @return   a Collection of Strings, where each element is one of the
     *           constants defined in MenuFactory
     */
    public Collection getMenuCategories()
    {
        return Arrays.asList(new String[] { MenuFactory.LISTENERS });
    }

    /* Implements JMeterGUIComponent.modifyTestElement(TestElement) */
    public TestElement createTestElement()
    {
        if (collector == null)
        {
            collector = new ResultCollector();
        }
        modifyTestElement(collector);
        return (TestElement) collector.clone();
    }

    /* Implements JMeterGUIComponent.modifyTestElement(TestElement) */
    public void modifyTestElement(TestElement c)
    {
        configureTestElement((AbstractListenerElement) c);
        if (c instanceof ResultCollector)
        {
            ResultCollector rc = (ResultCollector) c;
            rc.setErrorLogging(errorLogging.isSelected());
            rc.setFilename(getFile());
            collector = rc;
        }
    }

    /* Overrides AbstractJMeterGuiComponent.configure(TestElement) */
    public void configure(TestElement el)
    {
        super.configure(el);
        setFile(el.getPropertyAsString(ResultCollector.FILENAME));
        ResultCollector rc = (ResultCollector) el;
        errorLogging.setSelected(rc.isErrorLogging());
    }

    /**
     * This provides a convenience for extenders when they implement the
     * {@link JMeterGUIComponent#createTestElement()} method.  This method
     * will set the name, gui class, and test class for the created Test
     * Element.  It should be called by every extending class when creating
     * Test Elements, as that will best assure consistent behavior.
     * 
     * @param mc  the TestElement being created.
     */
    protected void configureTestElement(AbstractListenerElement mc)
    {
        // TODO: Should the method signature of this method be changed to
        // match the super-implementation (using a TestElement parameter
        // instead of AbstractListenerElement)?  This would require an
        // instanceof check before adding the listener (below), but would
        // also make the behavior a bit more obvious for sub-classes -- the
        // Java rules dealing with this situation aren't always intuitive,
        // and a subclass may think it is calling this version of the method
        // when it is really calling the superclass version instead.
        super.configureTestElement(mc);
        mc.setListener(this);
    }
    
    /**
     * Create a standard title section for JMeter components.  This includes
     * the title for the component and the Name Panel allowing the user to
     * change the name for the component.  The AbstractVisualizer also adds
     * the FilePanel allowing the user to save the results, and the
     * error logging checkbox, allowing the user to choose whether or not only
     * errors should be logged.
     * <p>  
     * This method is typically added to the top of the component at the
     * beginning of the component's init method.
     * 
     * @return a panel containing the component title, name panel, file panel,
     *         and error logging checkbox
     */
    protected Container makeTitlePanel()
    {
        Container panel = super.makeTitlePanel();
        // Note: the file panel already includes the error logging checkbox,
        // so we don't have to add it explicitly.
        panel.add(getFilePanel());
        return panel;
    }

    /**
     * @param collector
     */
    protected void setModel(ResultCollector collector)
    {
        this.collector = collector;
    }
}