package org.apache.jmeter.processor;

import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.ThreadListener;

/**
 * The PostProcessor is activated after a sample result has been generated.
 */
public interface PostProcessor extends ThreadListener
{
   /**
    * Provides the PostProcessor with a SampleResult object from which to extract
    * values for use in future Queries.
    */
   public void process(SampleResult result);

}
