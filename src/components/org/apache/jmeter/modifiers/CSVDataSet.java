/*
 * Created on Sep 29, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.apache.jmeter.modifiers;

import java.io.File;

import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.engine.event.LoopIterationListener;
import org.apache.jmeter.processor.PreProcessor;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.TestListener;

/**
 * @author mstover
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class CSVDataSet extends AbstractTestElement implements PreProcessor, TestBean, LoopIterationListener,TestListener
{
    static final public long serialVersionUID = 1;
    
    private File filename;
    private String variableNames;
    
    /* (non-Javadoc)
     * @see org.apache.jmeter.processor.PreProcessor#process()
     */
    public void process()
    {
    // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.engine.event.LoopIterationListener#iterationStart(org.apache.jmeter.engine.event.LoopIterationEvent)
     */
    public void iterationStart(LoopIterationEvent iterEvent)
    {
    // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    public Object clone()
    {
        // TODO Auto-generated method stub
        return super.clone();
    }
    /* (non-Javadoc)
     * @see org.apache.jmeter.testelement.TestListener#testEnded()
     */
    public void testEnded()
    {
    // TODO Auto-generated method stub

    }
    /* (non-Javadoc)
     * @see org.apache.jmeter.testelement.TestListener#testEnded(java.lang.String)
     */
    public void testEnded(String host)
    {
    // TODO Auto-generated method stub

    }
    /* (non-Javadoc)
     * @see org.apache.jmeter.testelement.TestListener#testIterationStart(org.apache.jmeter.engine.event.LoopIterationEvent)
     */
    public void testIterationStart(LoopIterationEvent event)
    {
    // TODO Auto-generated method stub

    }
    /* (non-Javadoc)
     * @see org.apache.jmeter.testelement.TestListener#testStarted()
     */
    public void testStarted()
    {
    // TODO Auto-generated method stub

    }
    /* (non-Javadoc)
     * @see org.apache.jmeter.testelement.TestListener#testStarted(java.lang.String)
     */
    public void testStarted(String host)
    {
    // TODO Auto-generated method stub

    }
    /**
     * @return Returns the filename.
     */
    public File getFilename()
    {
        return filename;
    }
    /**
     * @param filename The filename to set.
     */
    public void setFilename(File filename)
    {
        this.filename = filename;
    }
    /**
     * @return Returns the variableNames.
     */
    public String getVariableNames()
    {
        return variableNames;
    }
    /**
     * @param variableNames The variableNames to set.
     */
    public void setVariableNames(String variableNames)
    {
        this.variableNames = variableNames;
    }
}
