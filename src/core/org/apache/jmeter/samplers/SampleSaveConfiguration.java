/*
 * Created on Sep 7, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.apache.jmeter.samplers;

import java.io.Serializable;

/**
 * @author mstover
 * 
 * To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Generation - Code and Comments
 */
public class SampleSaveConfiguration implements Cloneable,Serializable
{
   static final long serialVersionUID = 1;
   boolean time = true, latency = true, timestamp = true, success = true,
         label = true, code = true, message = true, threadName = false,
         dataType = true, encoding = true, assertions = false,
         subresults = false, responseData = false, samplerData = false,xml = true,
         fieldNames = true,responseHeaders = false,requestHeaders = false;
   
   public Object clone()
   {
      SampleSaveConfiguration s = new SampleSaveConfiguration();
      s.time = time;
      s.latency = latency;
      s.timestamp = timestamp;
      s.success = success;
      s.label = label;
      s.code = code;
      s.message = message;
      s.threadName = threadName;
      s.dataType = dataType;
      s.encoding = encoding;
      s.assertions = assertions;
      s.subresults = subresults;
      s.responseData = responseData;
      s.samplerData = samplerData;
      s.xml = xml;
      s.fieldNames = fieldNames;
      s.responseHeaders = responseHeaders;
      s.requestHeaders = requestHeaders;
      return s;
   }
   
   public boolean saveResponseHeaders()
   {
      return responseHeaders;
   }
   
   public void setResponseHeaders(boolean r)
   {
      responseHeaders = r;
   }
   
   public boolean saveRequestHeaders()
   {
      return requestHeaders;
   }
   
   public void setRequestHeaders(boolean r)
   {
      requestHeaders = r;
   }

   /**
    * @return Returns the assertions.
    */
   public boolean saveAssertions()
   {
      return assertions;
   }

   /**
    * @param assertions
    *           The assertions to set.
    */
   public void setAssertions(boolean assertions)
   {
      this.assertions = assertions;
   }

   /**
    * @return Returns the code.
    */
   public boolean saveCode()
   {
      return code;
   }

   /**
    * @param code
    *           The code to set.
    */
   public void setCode(boolean code)
   {
      this.code = code;
   }

   /**
    * @return Returns the dataType.
    */
   public boolean saveDataType()
   {
      return dataType;
   }

   /**
    * @param dataType
    *           The dataType to set.
    */
   public void setDataType(boolean dataType)
   {
      this.dataType = dataType;
   }

   /**
    * @return Returns the encoding.
    */
   public boolean saveEncoding()
   {
      return encoding;
   }

   /**
    * @param encoding
    *           The encoding to set.
    */
   public void setEncoding(boolean encoding)
   {
      this.encoding = encoding;
   }

   /**
    * @return Returns the label.
    */
   public boolean saveLabel()
   {
      return label;
   }

   /**
    * @param label
    *           The label to set.
    */
   public void setLabel(boolean label)
   {
      this.label = label;
   }

   /**
    * @return Returns the latency.
    */
   public boolean saveLatency()
   {
      return latency;
   }

   /**
    * @param latency
    *           The latency to set.
    */
   public void setLatency(boolean latency)
   {
      this.latency = latency;
   }

   /**
    * @return Returns the message.
    */
   public boolean saveMessage()
   {
      return message;
   }

   /**
    * @param message
    *           The message to set.
    */
   public void setMessage(boolean message)
   {
      this.message = message;
   }

   /**
    * @return Returns the responseData.
    */
   public boolean saveResponseData()
   {
      return responseData;
   }

   /**
    * @param responseData
    *           The responseData to set.
    */
   public void setResponseData(boolean responseData)
   {
      this.responseData = responseData;
   }

   /**
    * @return Returns the samplerData.
    */
   public boolean saveSamplerData()
   {
      return samplerData;
   }

   /**
    * @param samplerData
    *           The samplerData to set.
    */
   public void setSamplerData(boolean samplerData)
   {
      this.samplerData = samplerData;
   }

   /**
    * @return Returns the subresults.
    */
   public boolean saveSubresults()
   {
      return subresults;
   }

   /**
    * @param subresults
    *           The subresults to set.
    */
   public void setSubresults(boolean subresults)
   {
      this.subresults = subresults;
   }

   /**
    * @return Returns the success.
    */
   public boolean saveSuccess()
   {
      return success;
   }

   /**
    * @param success
    *           The success to set.
    */
   public void setSuccess(boolean success)
   {
      this.success = success;
   }

   /**
    * @return Returns the threadName.
    */
   public boolean saveThreadName()
   {
      return threadName;
   }

   /**
    * @param threadName
    *           The threadName to set.
    */
   public void setThreadName(boolean threadName)
   {
      this.threadName = threadName;
   }

   /**
    * @return Returns the time.
    */
   public boolean saveTime()
   {
      return time;
   }

   /**
    * @param time
    *           The time to set.
    */
   public void setTime(boolean time)
   {
      this.time = time;
   }

   /**
    * @return Returns the timestamp.
    */
   public boolean saveTimestamp()
   {
      return timestamp;
   }

   /**
    * @param timestamp
    *           The timestamp to set.
    */
   public void setTimestamp(boolean timestamp)
   {
      this.timestamp = timestamp;
   }
   /**
    * @return Returns the xml.
    */
   public boolean saveAsXml()
   {
      return xml;
   }
   /**
    * @param xml The xml to set.
    */
   public void setAsXml(boolean xml)
   {
      this.xml = xml;
   }
   /**
    * @return Returns the printFieldNames.
    */
   public boolean saveFieldNames()
   {
      return fieldNames;
   }
   /**
    * @param printFieldNames The printFieldNames to set.
    */
   public void setFieldNames(boolean printFieldNames)
   {
      this.fieldNames = printFieldNames;
   }
}