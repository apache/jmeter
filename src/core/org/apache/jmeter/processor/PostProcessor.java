package org.apache.jmeter.processor;


/**
 * The PostProcessor is activated after a sample result has been generated.
 * @version $Revivion:$
 */
public interface PostProcessor
{
   /**
    * Provides the PostProcessor with a SampleResult object from which to
    * extract values for use in future Queries.
    */
   public void process();
}
