// $Header$
/*
 * Copyright 2000-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
*/

package org.apache.jmeter.visualizers;


import java.io.Serializable;


/**
 * @author Michael Stover
 * @version 1.0
 */

public class Sample implements Serializable,Comparable
{
    public long data;
    public long average;
    public long median;
    public long distributionLine;
    public long deviation;
    public double throughput;
    public long errorCount;
    public boolean success = true;
    public String label = null;
    public long count;
    public long endTime;
    public int bytes = 0;

    /**
     *  Constructor for the Sample object
     *
     *@param  data       Description of Parameter
     *@param  average    Description of Parameter
     *@param  deviation  Description of Parameter
     */
    public Sample(
        long data,
        long average,
        long deviation,
        double throughput,
        long median,
        boolean success)
    {
        this(null,data,average,deviation,median,0,throughput,0,success,0,0);
    }
    
    public Sample(long data)
    {
       this(null,data,0,0,0,0,0,0,false,0,0);
    }
    
    public Sample(String name,long data,long average,long deviation,long median,
          long distributionLine,double throughput,long errorCount,
          boolean success,long num,long endTime)
    {
       this.data = data;
       this.average = average;
       this.deviation = deviation;
       this.throughput = throughput;
       this.success = success;
       this.median = median;
       this.distributionLine = distributionLine;
       this.label = name;
       this.errorCount = errorCount;
       this.count = num;
       this.endTime = endTime;
    }

    public Sample()
    {}
    
   public int getBytes()
   {
      return bytes;
   }
   
   public void setBytes(int size)
   {
   	  bytes = size;
   }
   
   /**
    * @return Returns the average.
    */
   public long getAverage()
   {
      return average;
   }
   /**
    * @param average The average to set.
    */
   public void setAverage(long average)
   {
      this.average = average;
   }
   /**
    * @return Returns the count.
    */
   public long getCount()
   {
      return count;
   }
   /**
    * @param count The count to set.
    */
   public void setCount(long count)
   {
      this.count = count;
   }
   /**
    * @return Returns the data.
    */
   public long getData()
   {
      return data;
   }
   /**
    * @param data The data to set.
    */
   public void setData(long data)
   {
      this.data = data;
   }
   /**
    * @return Returns the deviation.
    */
   public long getDeviation()
   {
      return deviation;
   }
   /**
    * @param deviation The deviation to set.
    */
   public void setDeviation(long deviation)
   {
      this.deviation = deviation;
   }
   /**
    * @return Returns the distributionLine.
    */
   public long getDistributionLine()
   {
      return distributionLine;
   }
   /**
    * @param distributionLine The distributionLine to set.
    */
   public void setDistributionLine(long distributionLine)
   {
      this.distributionLine = distributionLine;
   }
   /**
    * @return Returns the error.
    */
   public boolean isSuccess()
   {
      return success;
   }
   /**
    * @param error The error to set.
    */
   public void setSuccess(boolean success)
   {
      this.success = success;
   }
   /**
    * @return Returns the errorRate.
    */
   public long getErrorCount()
   {
      return errorCount;
   }
   /**
    * @param errorRate The errorRate to set.
    */
   public void setErrorCount(long errorCount)
   {
      this.errorCount = errorCount;
   }
   /**
    * @return Returns the label.
    */
   public String getLabel()
   {
      return label;
   }
   /**
    * @param label The label to set.
    */
   public void setLabel(String label)
   {
      this.label = label;
   }
   /**
    * @return Returns the median.
    */
   public long getMedian()
   {
      return median;
   }
   /**
    * @param median The median to set.
    */
   public void setMedian(long median)
   {
      this.median = median;
   }
   /**
    * @return Returns the throughput.
    */
   public double getThroughput()
   {
      return throughput;
   }
   /**
    * @param throughput The throughput to set.
    */
   public void setThroughput(double throughput)
   {
      this.throughput = throughput;
   }
   /* (non-Javadoc)
    * @see java.lang.Comparable#compareTo(java.lang.Object)
    */
   public int compareTo(Object o)
   {
      Sample oo = (Sample)o;
      return ((count - oo.count) < 0 ? -1 : (count == oo.count ? 0 : 1));
   }
   /**
    * @return Returns the endTime.
    */
   public long getEndTime()
   {
      return endTime;
   }
   /**
    * @param endTime The endTime to set.
    */
   public void setEndTime(long endTime)
   {
      this.endTime = endTime;
   }
}
