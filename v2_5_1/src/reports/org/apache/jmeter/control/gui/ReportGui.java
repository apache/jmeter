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

package org.apache.jmeter.control.gui;

import java.awt.Color;
import java.awt.BorderLayout;
import java.awt.Container;
import java.util.Collection;

import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.gui.ArgumentsPanel;
import org.apache.jmeter.gui.util.DirectoryPanel;
import org.apache.jmeter.gui.util.ReportMenuFactory;
import org.apache.jmeter.report.gui.AbstractReportGui;
import org.apache.jmeter.report.gui.ReportPageGui;
import org.apache.jmeter.report.writers.gui.HTMLReportWriterGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.ReportPlan;
import org.apache.jmeter.util.JMeterUtils;

/**
 * JMeter GUI component representing the test plan which will be executed when
 * the test is run.
 *
 * @version $Revision$
 */
public class ReportGui extends AbstractReportGui {

    private static final long serialVersionUID = 240L;

    /** A panel to contain comments on the test plan. */
    private JTextField commentPanel;

    private DirectoryPanel baseDir =
        new DirectoryPanel(JMeterUtils.getResString("report_base_directory"), "",
                Color.white);

    /** A panel allowing the user to define variables. */
    private ArgumentsPanel argsPanel;

    /**
     * Create a new TestPlanGui.
     */
    public ReportGui() {
        init();
    }

    /**
     * Need to update this to make the context popupmenu correct
     * @return a JPopupMenu appropriate for the component.
     */
    @Override
    public JPopupMenu createPopupMenu() {
        JPopupMenu pop = new JPopupMenu();
        JMenu addMenu = new JMenu(JMeterUtils.getResString("Add"));
        addMenu.add(ReportMenuFactory.makeMenuItem(new ReportPageGui().getStaticLabel(),
                ReportPageGui.class.getName(),
                "Add"));
        addMenu.add(ReportMenuFactory.makeMenuItem(new HTMLReportWriterGui().getStaticLabel(),
                HTMLReportWriterGui.class.getName(),
                "Add"));
        addMenu.add(ReportMenuFactory.makeMenu(ReportMenuFactory.CONFIG_ELEMENTS, "Add"));
        pop.add(addMenu);
        ReportMenuFactory.addFileMenu(pop);
        ReportMenuFactory.addEditMenu(pop,true);
        return pop;
    }

    /* Implements JMeterGUIComponent.createTestElement() */
    public TestElement createTestElement() {
        ReportPlan tp = new ReportPlan();
        modifyTestElement(tp);
        return tp;
    }

    /* Implements JMeterGUIComponent.modifyTestElement(TestElement) */
    public void modifyTestElement(TestElement plan) {
        super.configureTestElement(plan);
        if (plan instanceof ReportPlan) {
            ReportPlan rp = (ReportPlan) plan;
            rp.setUserDefinedVariables((Arguments) argsPanel.createTestElement());
            rp.setProperty(ReportPlan.REPORT_COMMENTS, commentPanel.getText());
            rp.setBasedir(baseDir.getFilename());
        }
    }

    @Override
    public String getLabelResource() {
        return "report_plan";
    }

    /**
     * This is the list of menu categories this gui component will be available
     * under. This implementation returns null, since the TestPlan appears at
     * the top level of the tree and cannot be added elsewhere.
     *
     * @return a Collection of Strings, where each element is one of the
     *         constants defined in MenuFactory
     */
    @Override
    public Collection<String> getMenuCategories() {
        return null;
    }

    /**
     * A newly created component can be initialized with the contents of a Test
     * Element object by calling this method. The component is responsible for
     * querying the Test Element object for the relevant information to display
     * in its GUI.
     *
     * @param el
     *            the TestElement to configure
     */
    @Override
    public void configure(TestElement el) {
        super.configure(el);

        if (el.getProperty(ReportPlan.USER_DEFINED_VARIABLES) != null) {
            argsPanel.configure((Arguments) el.getProperty(ReportPlan.USER_DEFINED_VARIABLES).getObjectValue());
        }
        commentPanel.setText(el.getPropertyAsString(ReportPlan.REPORT_COMMENTS));
        baseDir.setFilename(el.getPropertyAsString(ReportPlan.BASEDIR));
    }

    /**
     * Create a panel allowing the user to define variables for the test.
     *
     * @return a panel for user-defined variables
     */
    private JPanel createVariablePanel() {
        argsPanel =
            new ArgumentsPanel(JMeterUtils.getResString("user_defined_variables"),
                    Color.white);
        return argsPanel;
    }

    private Container createCommentPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(Color.white);
        panel.setLayout(new BorderLayout(10, 10));
        Container title = makeTitlePanel();
        commentPanel = new JTextField();
        commentPanel.setBackground(Color.white);
        JLabel label = new JLabel(JMeterUtils.getResString("testplan_comments"));
        label.setBackground(Color.white);
        label.setLabelFor(commentPanel);
        title.add(label);
        title.add(commentPanel);
        panel.add(title,BorderLayout.NORTH);
        panel.add(baseDir,BorderLayout.CENTER);
        return panel;
    }

    /**
     * Initialize the components and layout of this component.
     */
    private void init() {// called from ctor, so must not be overridable
        setLayout(new BorderLayout(10, 10));
        setBorder(makeBorder());
        setBackground(Color.white);
        add(createCommentPanel(), BorderLayout.NORTH);
        add(createVariablePanel(), BorderLayout.CENTER);
    }
}
