package org.apache.jmeter.testelement;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.ConfigElement;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jmeter.util.JMeterUtils;

/****************************************
 * Title: JMeter Description: Copyright: Copyright (c) 2000 Company: Apache
 *
 *@author    Michael Stover
 *@created   March 13, 2001
 *@version   1.0
 ***************************************/

public class TestPlan extends AbstractTestElement implements Serializable
{
    /****************************************
     * !ToDo (Field description)
     ***************************************/
    public final static String THREAD_GROUPS = "TestPlan.thread_groups";
    public final static String FUNCTIONAL_MODE = "TestPlan.functional_mode";
    public final static String USER_DEFINED_VARIABLES = "TestPlan.user_defined_variables";

    private List threadGroups = new LinkedList();
    private List configs = new LinkedList();
    private static List itemsCanAdd = new LinkedList();
    private static TestPlan plan;
    private Map userDefinedVariables = new HashMap();

    static {
        // WARNING! This String value must be identical to the String value returned
        // in org.apache.jmeter.threads.ThreadGroup.getClassLabel() method.
        // If it's not you will not be able to add a Thread Group element to a Test Plan.
        itemsCanAdd.add(JMeterUtils.getResString("threadgroup"));
    }

    /****************************************
     * !ToDo (Constructor description)
     ***************************************/
    public TestPlan()
    {
        this("Test Plan");
        setFunctionalMode(false);
    }

    public boolean isFunctionalMode()
    {
        return getPropertyAsBoolean(FUNCTIONAL_MODE);
    }

    public void setUserDefinedVariables(Arguments vars)
    {
        setProperty(USER_DEFINED_VARIABLES, vars);
    }

    public Map getUserDefinedVariables()
    {
        Arguments args = getVariables();
        return args.getArgumentsAsMap();
    }

    private Arguments getVariables()
    {
        Arguments args = (Arguments) getProperty(USER_DEFINED_VARIABLES);
        if (args == null)
        {
            args = new Arguments();
            setUserDefinedVariables(args);
        }
        return args;
    }

    public void setFunctionalMode(boolean funcMode)
    {
        setProperty(FUNCTIONAL_MODE, new Boolean(funcMode));
    }

    /****************************************
     * !ToDo (Constructor description)
     *
     *@param name  !ToDo (Parameter description)
     ***************************************/
    public TestPlan(String name)
    {
        setName(name);
        setProperty(THREAD_GROUPS, threadGroups);
    }

    public void addParameter(String name, String value)
    {
        getVariables().addArgument(name, value);
    }

    /****************************************
     * Description of the Method
     *
     *@param name  Description of Parameter
     *@return      Description of the Returned Value
     ***************************************/
    public static TestPlan createTestPlan(String name)
    {
        if (plan == null)
        {
            if (name == null)
            {
                plan = new TestPlan();
            }
            else
            {
                plan = new TestPlan(name);
            }
            plan.setProperty(TestElement.GUI_CLASS, "org.apache.jmeter.control.gui.TestPlanGui");
        }
        return plan;
    }

    /****************************************
     * !ToDo
     *
     *@param tg  !ToDo
     ***************************************/
    public void addTestElement(TestElement tg)
    {
        super.addTestElement(tg);
        if (tg instanceof ThreadGroup)
        {
            addThreadGroup((ThreadGroup) tg);
        }
    }

    /****************************************
     * !ToDo
     *
     *@param child  !ToDo
     ***************************************/
    public void addJMeterComponent(TestElement child)
    {
        if (child instanceof ThreadGroup)
        {
            addThreadGroup((ThreadGroup) child);
        }
    }

    /****************************************
     * Gets the ThreadGroups attribute of the TestPlan object
     *
     *@return   The ThreadGroups value
     ***************************************/
    public Collection getThreadGroups()
    {
        return threadGroups;
    }

    /****************************************
     * Adds a feature to the ConfigElement attribute of the TestPlan object
     *
     *@param c  The feature to be added to the ConfigElement attribute
     ***************************************/
    public void addConfigElement(ConfigElement c)
    {
        configs.add(c);
    }

    /****************************************
     * Adds a feature to the ThreadGroup attribute of the TestPlan object
     *
     *@param group  The feature to be added to the ThreadGroup attribute
     ***************************************/
    public void addThreadGroup(ThreadGroup group)
    {
        threadGroups.add(group);
    }
}
