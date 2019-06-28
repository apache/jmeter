/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.apache.jmeter.visualizers.gui;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.SavePropertyDialog;
import org.apache.jmeter.gui.UnsharedComponent;
import org.apache.jmeter.gui.util.FilePanel;
import org.apache.jmeter.reporters.AbstractListenerElement;
import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.samplers.Clearable;
import org.apache.jmeter.samplers.SampleSaveConfiguration;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.Visualizer;
import org.apache.jorphan.gui.ComponentUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the base class for JMeter GUI components which can display test
 * results in some way. It provides the following conveniences to developers:
 * <ul>
 * <li>Implements the
 * {@link org.apache.jmeter.gui.JMeterGUIComponent JMeterGUIComponent} interface
 * that allows your Gui visualizer to "plug-in" to the JMeter GUI environment.
 * Provides implementations for the following methods:
 * <ul>
 * <li>{@link org.apache.jmeter.gui.JMeterGUIComponent#configure(TestElement) configure(TestElement)}.
 * Any additional parameters of your Visualizer need to be handled by you.</li>
 * <li>{@link org.apache.jmeter.gui.JMeterGUIComponent#createTestElement() createTestElement()}.
 * For most purposes, the default
 * {@link org.apache.jmeter.reporters.ResultCollector ResultCollector} created
 * by this method is sufficient.</li>
 * <li>{@link org.apache.jmeter.gui.JMeterGUIComponent#getMenuCategories getMenuCategories()}.
 * To control where in the GUI your visualizer can be added.</li>
 * <li>{@link org.apache.jmeter.gui.JMeterGUIComponent#modifyTestElement(TestElement) modifyTestElement(TestElement)}.
 * Again, additional parameters you require have to be handled by you.</li>
 * <li>{@link org.apache.jmeter.gui.JMeterGUIComponent#createPopupMenu() createPopupMenu()}.</li>
 * </ul>
 * </li>
 * <li>Provides convenience methods to help you make a JMeter-compatible GUI:
 * <ul>
 * <li>{@link #makeTitlePanel()}. Returns a panel that includes the name of
 * the component, and a FilePanel that allows users to control what file samples
 * are logged to.</li>
 * <li>{@link #getModel()} and {@link #setModel(ResultCollector)} methods for
 * setting and getting the model class that handles the receiving and logging of
 * sample results.</li>
 * </ul>
 * </li>
 * </ul>
 * For most developers, making a new visualizer is primarily for the purpose of
 * either calculating new statistics on the sample results that other
 * visualizers don't calculate, or displaying the results visually in a new and
 * interesting way. Making a new visualizer for either of these purposes is easy -
 * just extend this class and implement the
 * {@link org.apache.jmeter.visualizers.Visualizer#add add(SampleResult)}
 * method and display the results as you see fit. This AbstractVisualizer and
 * the default
 * {@link org.apache.jmeter.reporters.ResultCollector ResultCollector} handle
 * logging and registering to receive SampleEvents for you - all you need to do
 * is include the JPanel created by makeTitlePanel somewhere in your gui to
 * allow users set the log file.
 * <p>
 * If you are doing more than that, you may need to extend
 * {@link org.apache.jmeter.reporters.ResultCollector ResultCollector} as well
 * and modify the {@link #configure(TestElement)},
 * {@link #modifyTestElement(TestElement)}, and {@link #createTestElement()}
 * methods to create and modify your alternate ResultCollector. For an example
 * of this, see the
 * {@link org.apache.jmeter.visualizers.MailerVisualizer MailerVisualizer}.
 */
public abstract class AbstractVisualizer
    extends AbstractListenerGui
    implements Visualizer, ChangeListener, UnsharedComponent, Clearable
    {
    private static final long serialVersionUID = 241L;

    /** Logging. */
    private static final Logger log = LoggerFactory.getLogger(AbstractVisualizer.class);

    /** File Extensions */
    private static final String[] EXTS = { ".xml", ".jtl", ".csv" }; // $NON-NLS-1$ $NON-NLS-2$ $NON-NLS-3$

    /** A panel allowing results to be saved. */
    private final FilePanel filePanel;

    /** A checkbox choosing whether or not only errors should be logged. */
    private final JCheckBox errorLogging;

    /* A checkbox choosing whether or not only successes should be logged. */
    private final JCheckBox successOnlyLogging;

    protected ResultCollector collector = new ResultCollector();

    protected boolean isStats = false;

    public AbstractVisualizer() {
        super();

        // errorLogging and successOnlyLogging are mutually exclusive
        errorLogging = new JCheckBox(JMeterUtils.getResString("log_errors_only")); // $NON-NLS-1$
        errorLogging.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                if (errorLogging.isSelected()) {
                    successOnlyLogging.setSelected(false);
                }
            }
        });
        successOnlyLogging = new JCheckBox(JMeterUtils.getResString("log_success_only")); // $NON-NLS-1$
        successOnlyLogging.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                if (successOnlyLogging.isSelected()) {
                    errorLogging.setSelected(false);
                }
            }
        });
        JButton saveConfigButton = new JButton(JMeterUtils.getResString("config_save_settings")); // $NON-NLS-1$
        saveConfigButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SavePropertyDialog d = new SavePropertyDialog(
                        GuiPackage.getInstance().getMainFrame(),
                        JMeterUtils.getResString("sample_result_save_configuration"), // $NON-NLS-1$
                        true, collector.getSaveConfig());
                d.pack();
                ComponentUtil.centerComponentInComponent(GuiPackage.getInstance().getMainFrame(), d);
                d.setVisible(true);
            }
        });

        filePanel = new FilePanel(JMeterUtils.getResString("file_visualizer_output_file"), EXTS); // $NON-NLS-1$
        filePanel.addChangeListener(this);
        filePanel.add(new JLabel(JMeterUtils.getResString("log_only"))); // $NON-NLS-1$
        filePanel.add(errorLogging);
        filePanel.add(successOnlyLogging);
        filePanel.add(saveConfigButton);

    }

    @Override
    public boolean isStats() {
        return isStats;
    }

    /**
     * Gets the checkbox which selects whether or not only errors should be
     * logged. Subclasses don't normally need to worry about this checkbox,
     * because it is automatically added to the GUI in {@link #makeTitlePanel()},
     * and the behavior is handled in this base class.
     *
     * @return the error logging checkbox
     */
    protected JCheckBox getErrorLoggingCheckbox() {
        return errorLogging;
    }

    /**
     * Provides access to the ResultCollector model class for extending
     * implementations. Using this method and setModel(ResultCollector) is only
     * necessary if your visualizer requires a differently behaving
     * ResultCollector. Using these methods will allow maximum reuse of the
     * methods provided by AbstractVisualizer in this event.
     *
     * @return the associated collector
     */
    protected ResultCollector getModel() {
        return collector;
    }

    /**
     * Gets the file panel which allows the user to save results to a file.
     * Subclasses don't normally need to worry about this panel, because it is
     * automatically added to the GUI in {@link #makeTitlePanel()}, and the
     * behavior is handled in this base class.
     *
     * @return the file panel allowing users to save results
     */
    protected Component getFilePanel() {
        return filePanel;
    }

    /**
     * Sets the filename which results will be saved to. This will set the
     * filename in the FilePanel. Subclasses don't normally need to call this
     * method, because configuration of the FilePanel is handled in this base
     * class.
     *
     * @param filename
     *            the new filename
     *
     * @see #getFilePanel()
     */
    public void setFile(String filename) {
        // TODO: Does this method need to be public? It isn't currently
        // called outside of this class.
        filePanel.setFilename(filename);
    }

    /**
     * Gets the filename which has been entered in the FilePanel. Subclasses
     * don't normally need to call this method, because configuration of the
     * FilePanel is handled in this base class.
     *
     * @return the current filename
     *
     * @see #getFilePanel()
     */
    public String getFile() {
        // TODO: Does this method need to be public? It isn't currently
        // called outside of this class.
        return filePanel.getFilename();
    }

    /**
     * Invoked when the target of the listener has changed its state. This
     * implementation assumes that the target is the FilePanel, and will update
     * the result collector for the new filename.
     *
     * @param e
     *            the event that has occurred
     */
    @Override
    public void stateChanged(ChangeEvent e) {
        log.debug("getting new collector");
        collector = (ResultCollector) createTestElement();
        collector.loadExistingFile();
    }

    /* Implements JMeterGUIComponent.createTestElement() */
    @Override
    public TestElement createTestElement() {
        if (collector == null) {
            collector = new ResultCollector();
        }
        modifyTestElement(collector);
        return (TestElement) collector.clone();
    }

    /* Implements JMeterGUIComponent.modifyTestElement(TestElement) */
    @Override
    public void modifyTestElement(TestElement c) {
        configureTestElement((AbstractListenerElement) c);
        if (c instanceof ResultCollector) {
            ResultCollector rc = (ResultCollector) c;
            rc.setErrorLogging(errorLogging.isSelected());
            rc.setSuccessOnlyLogging(successOnlyLogging.isSelected());
            rc.setFilename(getFile());
            collector = rc;
        }
    }

    /* Overrides AbstractJMeterGuiComponent.configure(TestElement) */
    @Override
    public void configure(TestElement el) {
        super.configure(el);
        setFile(el.getPropertyAsString(ResultCollector.FILENAME));
        ResultCollector rc = (ResultCollector) el;
        errorLogging.setSelected(rc.isErrorLogging());
        successOnlyLogging.setSelected(rc.isSuccessOnlyLogging());
        if (collector == null) {
            collector = new ResultCollector();
        }
        collector.setSaveConfig((SampleSaveConfiguration) rc.getSaveConfig().clone());
    }

    /**
     * This provides a convenience for extenders when they implement the
     * {@link org.apache.jmeter.gui.JMeterGUIComponent#createTestElement()}
     * method. This method will set the name, gui class, and test class for the
     * created Test Element. It should be called by every extending class when
     * creating Test Elements, as that will best assure consistent behavior.
     *
     * @param mc
     *            the TestElement being created.
     */
    protected void configureTestElement(AbstractListenerElement mc) {
        // TODO: Should the method signature of this method be changed to
        // match the super-implementation (using a TestElement parameter
        // instead of AbstractListenerElement)? This would require an
        // instanceof check before adding the listener (below), but would
        // also make the behavior a bit more obvious for sub-classes -- the
        // Java rules dealing with this situation aren't always intuitive,
        // and a subclass may think it is calling this version of the method
        // when it is really calling the superclass version instead.
        super.configureTestElement(mc);
        mc.setListener(this);
    }

    /**
     * Create a standard title section for JMeter components. This includes the
     * title for the component and the Name Panel allowing the user to change
     * the name for the component. The AbstractVisualizer also adds the
     * FilePanel allowing the user to save the results, and the error logging
     * checkbox, allowing the user to choose whether or not only errors should
     * be logged.
     * <p>
     * This method is typically added to the top of the component at the
     * beginning of the component's init method.
     *
     * @return a panel containing the component title, name panel, file panel,
     *         and error logging checkbox
     */
    @Override
    protected Container makeTitlePanel() {
        Container panel = super.makeTitlePanel();
        // Note: the file panel already includes the error logging checkbox,
        // so we don't have to add it explicitly.
        panel.add(getFilePanel());
        return panel;
    }

    /**
     * Provides extending classes the opportunity to set the ResultCollector
     * model for the Visualizer. This is useful to allow maximum reuse of the
     * methods from AbstractVisualizer.
     *
     * @param collector {@link ResultCollector} for the visualizer
     */
    protected void setModel(ResultCollector collector) {
        this.collector = collector;
    }

    @Override
    public void clearGui(){
        super.clearGui();
        filePanel.clearGui();
    }
}
