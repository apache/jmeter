package org.apache.jmeter.engine.event;

import org.apache.jmeter.testelement.TestElement;

/**
 * An iteration event provides information about the iteration number and the
 * source of the event.
 */
public class IterationEvent
{
    
    int iteration;
    TestElement source;
    TestElement current;
    
    public IterationEvent(TestElement source,TestElement current,int iter)
    {
        iteration = iter;
        this.current = current;
        this.source = source;
    }

    /**
     * Returns the iteration.
     * @return int
     */
    public int getIteration()
    {
        return iteration;
    }

    /**
     * Returns the source.
     * @return TestElement
     */
    public TestElement getSource()
    {
        return source;
    }
    
    public TestElement getCurrent()
    {
    	return current;
    }

    /**
     * Sets the iteration.
     * @param iteration The iteration to set
     */
    public void setIteration(int iteration)
    {
        this.iteration = iteration;
    }

    /**
     * Sets the source.
     * @param source The source to set
     */
    public void setSource(TestElement source)
    {
        this.source = source;
    }
    
    public void setCurrent(TestElement current)
    {
    	this.current = current;
    }

}
