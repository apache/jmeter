package org.apache.jmeter.reporters;


import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.visualizers.Visualizer;


/**
 * Title:        JMeter
 * Description:
 * Copyright:    Copyright (c) 2000
 * Company:      Apache
 * @author Michael Stover
 * @version 1.0
 */

public abstract class AbstractListenerElement extends AbstractTestElement
{
    private Visualizer listener;

    public AbstractListenerElement()
    {}

    protected Visualizer getVisualizer()
    {
        return listener;
    }

    public void setListener(Visualizer vis)
    {
        listener = vis;
    }

    public Object clone()
    {
        AbstractListenerElement clone = (AbstractListenerElement) super.clone();

        clone.setListener(getVisualizer());
        return clone;
    }
}
