package org.apache.jmeter.extractor;

import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.ThreadListener;

/**
 * @author Administrator
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 */
public interface Extractor extends ThreadListener
{
   /**
    * Provides the Extractor with a SampleResult object from which to extract
    * values for use in future Queries.
    */
   public void processResult(SampleResult result);

}
