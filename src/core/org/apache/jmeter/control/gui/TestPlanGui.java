package org.apache.jmeter.control.gui;
import java.awt.BorderLayout;
import java.util.Collection;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextArea;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.gui.ArgumentsPanel;
import org.apache.jmeter.gui.AbstractJMeterGuiComponent;
import org.apache.jmeter.gui.util.MenuFactory;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.threads.gui.ThreadGroupGui;
import org.apache.jmeter.util.JMeterUtils;

/****************************************
 * Title: JMeter Description: Copyright: Copyright (c) 2000 Company: Apache
 *
 *@author    Michael Stover
 *@created   $Date$
 *@version   1.0
 ***************************************/

public class TestPlanGui extends AbstractJMeterGuiComponent
{
    JCheckBox functionalMode;
    ArgumentsPanel argsPanel;

    /****************************************
     * !ToDo (Constructor description)
     ***************************************/
    public TestPlanGui()
    {
        init();
    }

    /****************************************
     * !ToDo (Method description)
     *
     *@return   !ToDo (Return description)
     ***************************************/
    public JPopupMenu createPopupMenu()
    {
        JPopupMenu pop = new JPopupMenu();
        JMenu addMenu = new JMenu(JMeterUtils.getResString("Add"));
        addMenu.add(MenuFactory.makeMenuItem(new ThreadGroupGui().getStaticLabel(), ThreadGroupGui.class.getName(), "Add"));
        addMenu.add(MenuFactory.makeMenu(MenuFactory.LISTENERS, "Add"));
        addMenu.add(MenuFactory.makeMenu(MenuFactory.CONFIG_ELEMENTS, "Add"));
        addMenu.add(MenuFactory.makeMenu(MenuFactory.ASSERTIONS, "Add"));
        addMenu.add(MenuFactory.makeMenu(MenuFactory.MODIFIERS, "Add"));
        addMenu.add(MenuFactory.makeMenu(MenuFactory.RESPONSE_BASED_MODIFIERS, "Add"));
        addMenu.add(MenuFactory.makeMenu(MenuFactory.TIMERS, "Add"));
        pop.add(addMenu);
        MenuFactory.addFileMenu(pop);
        return pop;
    }

    /****************************************
     * !ToDo (Method description)
     *
     *@return   !ToDo (Return description)
     ***************************************/
    public TestElement createTestElement()
    {
        TestPlan tp = new TestPlan();
        modifyTestElement(tp);
        return tp;
    }

    /**
     * Modifies a given TestElement to mirror the data in the gui components.
     * @see org.apache.jmeter.gui.JMeterGUIComponent#modifyTestElement(TestElement)
     */
    public void modifyTestElement(TestElement plan)
    {
        super.configureTestElement(plan);
        if (plan instanceof TestPlan)
        {
            TestPlan tp = (TestPlan) plan;
            tp.setFunctionalMode(functionalMode.isSelected());
            tp.setUserDefinedVariables((Arguments) argsPanel.createTestElement());
        }
    }

    /****************************************
     * !ToDoo (Method description)
     *
     *@return   !ToDo (Return description)
     ***************************************/
    public String getStaticLabel()
    {
        return JMeterUtils.getResString("Test Plan");
    }

    /****************************************
     * !ToDoo (Method description)
     *
     *@return   !ToDo (Return description)
     ***************************************/
    public Collection getMenuCategories()
    {
        return null;
    }

    /****************************************
     * !ToDo (Method description)
     *
     *@param el  !ToDo (Parameter description)
     ***************************************/
    public void configure(TestElement el)
    {
        super.configure(el);
        functionalMode.setSelected(((AbstractTestElement) el).getPropertyAsBoolean(TestPlan.FUNCTIONAL_MODE));
        if (el.getProperty(TestPlan.USER_DEFINED_VARIABLES) != null)
        {
            argsPanel.configure((Arguments) el.getProperty(TestPlan.USER_DEFINED_VARIABLES));
        }
    }

    /****************************************
     * !ToDoo (Method description)
     *
     *@return   !ToDo (Return description)
     ***************************************/
    protected JPanel getVariablePanel()
    {
        argsPanel = new ArgumentsPanel(JMeterUtils.getResString("user_defined_variables"));

        return argsPanel;
    }

    private void init()
    {
        this.setLayout(new BorderLayout(10, 10));
        this.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 5));
        this.add(getNamePanel(), BorderLayout.NORTH);
        JPanel southPanel = new JPanel(new BorderLayout());
        functionalMode = new JCheckBox(JMeterUtils.getResString("functional_mode"));
        southPanel.add(functionalMode, BorderLayout.NORTH);
        JTextArea explain = new JTextArea(JMeterUtils.getResString("functional_mode_explanation"));
        explain.setColumns(30);
        explain.setRows(10);
        explain.setBackground(this.getBackground());
        southPanel.add(explain, BorderLayout.CENTER);
        add(getVariablePanel(), BorderLayout.CENTER);
        add(southPanel, BorderLayout.SOUTH);
    }
}
