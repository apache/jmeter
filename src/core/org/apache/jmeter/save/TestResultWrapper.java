/*
 * Created on Sep 6, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.apache.jmeter.save;

import java.util.Collection;

/**
 * @author mstover
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class TestResultWrapper
{
   String version = "";
   Collection sampleResults;
   long testStartTime;
   /**
    * @return Returns the sampleResults.
    */
   public Collection getSampleResults()
   {
      return sampleResults;
   }
   /**
    * @param sampleResults The sampleResults to set.
    */
   public void setSampleResults(Collection sampleResults)
   {
      this.sampleResults = sampleResults;
   }
   /**
    * @return Returns the testStartTime.
    */
   public long getTestStartTime()
   {
      return testStartTime;
   }
   /**
    * @param testStartTime The testStartTime to set.
    */
   public void setTestStartTime(long testStartTime)
   {
      this.testStartTime = testStartTime;
   }
   /**
    * @return Returns the version.
    */
   public String getVersion()
   {
      return version;
   }
   /**
    * @param version The version to set.
    */
   public void setVersion(String version)
   {
      this.version = version;
   }
}
