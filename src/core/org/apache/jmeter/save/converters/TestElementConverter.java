/*
 * Created on Jun 17, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.apache.jmeter.save.converters;

import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.PropertyIterator;
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
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class TestElementConverter extends AbstractCollectionConverter
{
   private static Logger log = LoggingManager.getLoggerForClass();

   /* (non-Javadoc)
    * @see com.thoughtworks.xstream.converters.Converter#canConvert(java.lang.Class)
    */
   public boolean canConvert(Class arg0)
   {
      return TestElement.class.isAssignableFrom(arg0);
   }

   /* (non-Javadoc)
    * @see com.thoughtworks.xstream.converters.Converter#marshal(java.lang.Object, com.thoughtworks.xstream.io.HierarchicalStreamWriter, com.thoughtworks.xstream.converters.MarshallingContext)
    */
   public void marshal(Object arg0, HierarchicalStreamWriter writer,
         MarshallingContext context)
   {
      TestElement el = (TestElement)arg0;
      PropertyIterator iter = el.propertyIterator();
      while(iter.hasNext())
      {
         writeItem(iter.next(),context,writer);
      }
   }

   /* (non-Javadoc)
    * @see com.thoughtworks.xstream.converters.Converter#unmarshal(com.thoughtworks.xstream.io.HierarchicalStreamReader, com.thoughtworks.xstream.converters.UnmarshallingContext)
    */
   public Object unmarshal(HierarchicalStreamReader reader,
         UnmarshallingContext context)
   {
      String classAttribute = reader.getAttribute(classAttributeIdentifier);
      Class type;
      if (classAttribute == null) {
          type = classMapper.lookupType(reader.getNodeName());
      } else {
          type = classMapper.lookupType(classAttribute);
      }
      try
      {
         TestElement el = (TestElement)type.newInstance();
         while (reader.hasMoreChildren()) {
            reader.moveDown();
            JMeterProperty prop = (JMeterProperty)readItem(reader, context, el);
            el.setProperty(prop);
            reader.moveUp();
        }
        return el;
      }
      catch (Exception e)
      {
         log.error("TestElement not instantiable: " + type,e);
         return null;
      }
   }
   /**
    * @param arg0
    * @param arg1
    */
   public TestElementConverter(ClassMapper arg0, String arg1)
   {
      super(arg0, arg1);
   }
}
