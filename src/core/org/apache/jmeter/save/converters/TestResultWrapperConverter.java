/*
 * Created on Sep 7, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.apache.jmeter.save.converters;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.save.TestResultWrapper;

import com.thoughtworks.xstream.alias.ClassMapper;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.collections.AbstractCollectionConverter;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * @author mstover
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class TestResultWrapperConverter extends AbstractCollectionConverter
{

   /**
    * @param arg0
    * @param arg1
    */
   public TestResultWrapperConverter(ClassMapper arg0, String arg1)
   {
      super(arg0, arg1);
   }
   /* (non-Javadoc)
    * @see com.thoughtworks.xstream.converters.Converter#canConvert(java.lang.Class)
    */
   public boolean canConvert(Class arg0)
   {
      return arg0.equals(TestResultWrapper.class);
   }

   /* (non-Javadoc)
    * @see com.thoughtworks.xstream.converters.Converter#marshal(java.lang.Object, com.thoughtworks.xstream.io.HierarchicalStreamWriter, com.thoughtworks.xstream.converters.MarshallingContext)
    */
   public void marshal(Object arg0, HierarchicalStreamWriter arg1,
         MarshallingContext arg2)
   {
      
   }

   /* (non-Javadoc)
    * @see com.thoughtworks.xstream.converters.Converter#unmarshal(com.thoughtworks.xstream.io.HierarchicalStreamReader, com.thoughtworks.xstream.converters.UnmarshallingContext)
    */
   public Object unmarshal(HierarchicalStreamReader reader,
         UnmarshallingContext context)
   {
      TestResultWrapper results = new TestResultWrapper();
      Collection samples = new ArrayList();
      while (reader.hasMoreChildren()) {
         reader.moveDown();
         SampleResult res = (SampleResult)readItem(reader, context, results);
         samples.add(res);
         reader.moveUp();
     }
      results.setSampleResults(samples);
      return results;      
   }

   public static void main(String[] args)
   {
   }
}
