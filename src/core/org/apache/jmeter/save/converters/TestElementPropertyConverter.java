/*
 * Created on Jun 19, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.apache.jmeter.save.converters;

import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import com.thoughtworks.xstream.alias.ClassMapper;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.collections.AbstractCollectionConverter;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * @author mstover
 * 
 * To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Generation - Code and Comments
 */
public class TestElementPropertyConverter extends AbstractCollectionConverter
{
   Logger log = LoggingManager.getLoggerForClass();

   /*
    * (non-Javadoc)
    * 
    * @see com.thoughtworks.xstream.converters.Converter#canConvert(java.lang.Class)
    */
   public boolean canConvert(Class arg0)
   {
      return arg0.equals(TestElementProperty.class);
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.thoughtworks.xstream.converters.Converter#marshal(java.lang.Object,
    *      com.thoughtworks.xstream.io.HierarchicalStreamWriter,
    *      com.thoughtworks.xstream.converters.MarshallingContext)
    */
   public void marshal(Object arg0, HierarchicalStreamWriter writer,
         MarshallingContext context)
   {
      TestElementProperty prop = (TestElementProperty) arg0;
      writer.addAttribute("name", prop.getName());
      writer.addAttribute("elementType", prop.getObjectValue().getClass()
            .getName());
      PropertyIterator iter = prop.iterator();
      while (iter.hasNext())
      {
         writeItem(iter.next(), context, writer);
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
      try
      {
         TestElementProperty prop = (TestElementProperty) createCollection(context
               .getRequiredType());
         prop.setName(reader.getAttribute("name"));
         prop.setObjectValue((TestElement) Class.forName(
               reader.getAttribute("elementType")).newInstance());
         while (reader.hasMoreChildren())
         {
            reader.moveDown();
            JMeterProperty subProp = (JMeterProperty) readItem(reader, context,
                  prop);
            prop.addProperty(subProp);
            reader.moveUp();
         }
         return prop;
      }
      catch (Exception e)
      {
         log.error("Couldn't unmarshall TestElementProperty", e);
         return new TestElementProperty("ERROR", new ConfigTestElement());
      }
   }

   /**
    * @param arg0
    * @param arg1
    */
   public TestElementPropertyConverter(ClassMapper arg0, String arg1)
   {
      super(arg0, arg1);
   }
}
