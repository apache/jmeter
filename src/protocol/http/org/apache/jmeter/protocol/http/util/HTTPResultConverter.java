/*
 * Created on Sep 14, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.apache.jmeter.protocol.http.util;

import java.net.URL;

import org.apache.jmeter.protocol.http.sampler.HTTPSampleResult;
import org.apache.jmeter.samplers.SampleSaveConfiguration;
import org.apache.jmeter.save.converters.SampleResultConverter;

import com.thoughtworks.xstream.alias.ClassMapper;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * @author mstover
 * 
 * To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Generation - Code and Comments
 */
public class HTTPResultConverter extends SampleResultConverter
{

   /**
    * @param arg0
    * @param arg1
    */
   public HTTPResultConverter(ClassMapper arg0, String arg1)
   {
      super(arg0, arg1);
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.thoughtworks.xstream.converters.Converter#canConvert(java.lang.Class)
    */
   public boolean canConvert(Class arg0)
   {
      return HTTPSampleResult.class.equals(arg0);
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.thoughtworks.xstream.converters.Converter#marshal(java.lang.Object,
    *      com.thoughtworks.xstream.io.HierarchicalStreamWriter,
    *      com.thoughtworks.xstream.converters.MarshallingContext)
    */
   public void marshal(Object obj, HierarchicalStreamWriter writer,
         MarshallingContext context)
   {
      HTTPSampleResult res = (HTTPSampleResult) obj;
      SampleSaveConfiguration save = res.getSaveConfig();
      setAttributes(writer, res, save);
      saveAssertions(writer, context, res, save);
      saveRequestHeaders(writer, res, save);
      saveSubResults(writer, context, res, save);
      saveResponseHeaders(writer, res, save);
      saveResponseData(writer, res, save);
      saveSamplerData(writer, context, res, save);
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.apache.jmeter.save.converters.SampleResultConverter#saveSamplerData(com.thoughtworks.xstream.io.HierarchicalStreamWriter,
    *      org.apache.jmeter.samplers.SampleResult,
    *      org.apache.jmeter.samplers.SampleSaveConfiguration)
    */
   protected void saveSamplerData(HierarchicalStreamWriter writer,
         MarshallingContext context, HTTPSampleResult res,
         SampleSaveConfiguration save)
   {
      if (save.saveSamplerData())
      {
         writeString(writer, "cookies", res.getCookies());
         writeString(writer, "method", res.getHTTPMethod());
         writeString(writer, "queryString", res.getQueryString());
         writeString(writer, "redirectLocation", res.getRedirectLocation());
         writeItem(res.getURL(), context, writer);
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.thoughtworks.xstream.converters.Converter#unmarshal(com.thoughtworks.xstream.io.HierarchicalStreamReader,
    *      com.thoughtworks.xstream.converters.UnmarshallingContext)
    */
   public Object unmarshal(HierarchicalStreamReader reader,
         UnmarshallingContext context)
   {
      HTTPSampleResult res = (HTTPSampleResult) createCollection(context
            .getRequiredType());
      retrieveAttributes(reader, res);
      while (reader.hasMoreChildren())
      {
         reader.moveDown();
         Object subItem = readItem(reader, context, res);
         if (!retrieveItem(reader, context, res, subItem))
         {
            retrieveHTTPItem(reader, context, res, subItem);
         }
         reader.moveUp();
      }
      return res;
   }

   protected void retrieveHTTPItem(HierarchicalStreamReader reader,
         UnmarshallingContext context, HTTPSampleResult res, Object subItem)
   {
      if (subItem instanceof URL)
      {
         res.setURL((URL) subItem);
      }
      else if (reader.getNodeName().equals("cookies"))
      {
         res.setCookies((String) subItem);
      }
      else if (reader.getNodeName().equals("method"))
      {
         res.setHTTPMethod((String) subItem);
      }
      else if (reader.getNodeName().equals("queryString"))
      {
         res.setQueryString((String) subItem);
      }
      else if (reader.getNodeName().equals("redirectLocation"))
      {
         res.setRedirectLocation((String) subItem);
      }
   }
}