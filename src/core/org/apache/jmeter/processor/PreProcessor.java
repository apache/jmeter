package org.apache.jmeter.processor;

/**
 * PreProcessors are executed just prior to a sample being run.
 */
public interface PreProcessor
{    
    public void process();
}
