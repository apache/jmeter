package org.apache.jmeter.engine.event;

import org.apache.jmeter.testelement.TestElement;

/**
 * An iteration event provides information about the iteration number and the
 * source of the event.
 */
public class LoopIterationEvent  
{  
    int iteration;  
    TestElement source;  
      
    public LoopIterationEvent(TestElement source, int iter)
    {
        iteration = iter;
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

    /**  
     * Sets the iteration.  
     * @param iteration The iteration to set  
     */
    public void setIteration(int iteration)
    {
        this.iteration = iteration;
    }
}