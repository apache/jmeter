package org.apache.jmeter.modifiers;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;

import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.engine.event.LoopIterationListener;
import org.apache.jmeter.processor.PreProcessor;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.BooleanProperty;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;

/**
 * @author Administrator
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 */
public class UserParameters extends AbstractTestElement implements Serializable, PreProcessor, LoopIterationListener
{

    public static final String NAMES = "UserParameters.names";
    public static final String THREAD_VALUES = "UserParameters.thread_values";
    public static final String PER_ITERATION = "UserParameters.per_iteration";
    private int counter = 0;
    transient private Object lock = new Object();

    public CollectionProperty getNames()
    {
        return (CollectionProperty) getProperty(NAMES);
    }

    public CollectionProperty getThreadLists()
    {
        return (CollectionProperty) getProperty(THREAD_VALUES);
    }

    /**
     * The list of names of the variables to hold values.  This list must come in
     * the same order as the sub lists that are given to setThreadLists(List).
     */
    public void setNames(Collection list)
    {
        setProperty(new CollectionProperty(NAMES, list));
    }

    /**
         * The list of names of the variables to hold values.  This list must come in
         * the same order as the sub lists that are given to setThreadLists(List).
         */
    public void setNames(CollectionProperty list)
    {
        setProperty(list);
    }

    /**
     * The thread list is a list of lists.  Each list within the parent list is a
     * collection of values for a simulated user.  As many different sets of 
     * values can be supplied in this fashion to cause JMeter to set different 
     * values to variables for different test threads.
     */
    public void setThreadLists(Collection threadLists)
    {
        setProperty(new CollectionProperty(THREAD_VALUES, threadLists));
    }

    /**
         * The thread list is a list of lists.  Each list within the parent list is a
         * collection of values for a simulated user.  As many different sets of 
         * values can be supplied in this fashion to cause JMeter to set different 
         * values to variables for different test threads.
         */
    public void setThreadLists(CollectionProperty threadLists)
    {
        setProperty(threadLists);
    }

    private CollectionProperty getValues()
    {
        CollectionProperty threadValues = (CollectionProperty) getProperty(THREAD_VALUES);
        if (threadValues.size() > 0)
        {
            return (CollectionProperty) threadValues.get(JMeterContextService.getContext().getThreadNum() % threadValues.size());
        }
        else
        {
            return new CollectionProperty("noname", new LinkedList());
        }
    }

    public boolean isPerIteration()
    {
        return getPropertyAsBoolean(PER_ITERATION);
    }

    public void setPerIteration(boolean perIter)
    {
        setProperty(new BooleanProperty(PER_ITERATION, perIter));
    }

    public void process()
    {
        if (!isPerIteration())
        {
            setValues();
        }
    }

    private void setValues()
    {
        synchronized (lock)
        {
            log.debug("Running up named: " + getName());
            PropertyIterator namesIter = getNames().iterator();
            PropertyIterator valueIter = getValues().iterator();
            JMeterVariables jmvars = JMeterContextService.getContext().getVariables();
            while (namesIter.hasNext() && valueIter.hasNext())
            {
                String name = namesIter.next().getStringValue();
                String value = valueIter.next().getStringValue();
                log.debug("saving variable: " + name + "=" + value);
                jmvars.put(name, value);
            }
        }
    }

    /**
     * @see org.apache.jmeter.engine.event.LoopIterationListener#iterationStart(LoopIterationEvent)
     */
    public void iterationStart(LoopIterationEvent event)
    {
        if (isPerIteration())
        {
            setValues();
        }
    }

    /* This method doesn't appear to be used anymore.
     * jeremy_a@bigfoot.com  03 May 2003
     * 
     * @see org.apache.jmeter.testelement.ThreadListener#setJMeterVariables(org.apache.jmeter.threads.JMeterVariables)
    public void setJMeterVariables(JMeterVariables jmVars)
    {}
     */

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    public Object clone()
    {
        UserParameters up = (UserParameters) super.clone();
        up.lock = lock;
        return up;
    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.testelement.AbstractTestElement#mergeIn(org.apache.jmeter.testelement.TestElement)
     */
    protected void mergeIn(TestElement element)
    {
        // super.mergeIn(element);
    }

}
