package org.apache.jmeter.testelement;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.ConfigElement;
import org.apache.jmeter.testelement.property.BooleanProperty;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.StringProperty;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jmeter.util.JMeterUtils;

/**
 * @author    Michael Stover
 * Created   March 13, 2001
 * @version   $Revision$ Last updated: $Date$
 */
public class TestPlan extends AbstractTestElement implements Serializable
{
    public final static String THREAD_GROUPS = "TestPlan.thread_groups";
    public final static String FUNCTIONAL_MODE = "TestPlan.functional_mode";
    public final static String USER_DEFINED_VARIABLES =
        "TestPlan.user_defined_variables";
    public final static String SERIALIZE_THREADGROUPS =
        "TestPlan.serialize_threadgroups";
    public final static String COMMENTS = "TestPlan.comments";

    private List threadGroups = new LinkedList();
    private List configs = new LinkedList();
    private static List itemsCanAdd = new LinkedList();
    private static TestPlan plan;
    
    static {
        // WARNING! This String value must be identical to the String value
        // returned in org.apache.jmeter.threads.ThreadGroup.getClassLabel()
        // method. If it's not you will not be able to add a Thread Group
        // element to a Test Plan.
        itemsCanAdd.add(JMeterUtils.getResString("threadgroup"));
    }

    public TestPlan()
    {
//        this("Test Plan");
//        setFunctionalMode(false);
//        setSerialized(false);
    }

    public TestPlan(String name)
    {
        setName(name);
//		setFunctionalMode(false);
//		setSerialized(false);
        setProperty(new CollectionProperty(THREAD_GROUPS, threadGroups));
    }

    public boolean isFunctionalMode()
    {
        return getPropertyAsBoolean(FUNCTIONAL_MODE);
    }

    public void setUserDefinedVariables(Arguments vars)
    {
        setProperty(new TestElementProperty(USER_DEFINED_VARIABLES, vars));
    }

    public Map getUserDefinedVariables()
    {
        Arguments args = getVariables();
        return args.getArgumentsAsMap();
    }

    private Arguments getVariables()
    {
        Arguments args =
            (Arguments) getProperty(USER_DEFINED_VARIABLES).getObjectValue();
        if (args == null)
        {
            args = new Arguments();
            setUserDefinedVariables(args);
        }
        return args;
    }

    public void setFunctionalMode(boolean funcMode)
    {
        setProperty(new BooleanProperty(FUNCTIONAL_MODE, funcMode));
    }
    
    public void setSerialized(boolean serializeTGs)
    {
        setProperty(new BooleanProperty(SERIALIZE_THREADGROUPS, serializeTGs));
    }
    
    public boolean isSerialized()
    {
        return getPropertyAsBoolean(SERIALIZE_THREADGROUPS);
    }

    public void addParameter(String name, String value)
    {
        getVariables().addArgument(name, value);
    }

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
            plan.setProperty(
                new StringProperty(
                    TestElement.GUI_CLASS,
                    "org.apache.jmeter.control.gui.TestPlanGui"));
        }
        return plan;
    }

    public void addTestElement(TestElement tg)
    {
        super.addTestElement(tg);
        if (tg instanceof ThreadGroup && !isRunningVersion())
        {
            addThreadGroup((ThreadGroup) tg);
        }
    }

    public void addJMeterComponent(TestElement child)
    {
        if (child instanceof ThreadGroup)
        {
            addThreadGroup((ThreadGroup) child);
        }
    }

    /**
     * Gets the ThreadGroups attribute of the TestPlan object.
     *
     * @return   the ThreadGroups value
     */
    public Collection getThreadGroups()
    {
        return threadGroups;
    }

    /**
     * Adds a feature to the ConfigElement attribute of the TestPlan object.
     *
     * @param c  the feature to be added to the ConfigElement attribute
     */
    public void addConfigElement(ConfigElement c)
    {
        configs.add(c);
    }

    /**
     * Adds a feature to the ThreadGroup attribute of the TestPlan object.
     *
     * @param group  the feature to be added to the ThreadGroup attribute
     */
    public void addThreadGroup(ThreadGroup group)
    {
        threadGroups.add(group);
    }
}
