/*
 * Created on Jun 19, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.apache.jmeter.save.converters;

import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.MapProperty;
import org.apache.jmeter.testelement.property.MultiProperty;
import org.apache.jmeter.testelement.property.PropertyIterator;

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
public class MultiPropertyConverter extends AbstractCollectionConverter
{

   /* (non-Javadoc)
    * @see com.thoughtworks.xstream.converters.Converter#canConvert(java.lang.Class)
    */
   public boolean canConvert(Class arg0)
   {
      return arg0.equals(CollectionProperty.class) || arg0.equals(MapProperty.class);
   }

   /* (non-Javadoc)
    * @see com.thoughtworks.xstream.converters.Converter#marshal(java.lang.Object, com.thoughtworks.xstream.io.HierarchicalStreamWriter, com.thoughtworks.xstream.converters.MarshallingContext)
    */
   public void marshal(Object arg0, HierarchicalStreamWriter writer,
         MarshallingContext context)
   {
      MultiProperty prop = (MultiProperty)arg0;
      writer.addAttribute("name",prop.getName());
      PropertyIterator iter = prop.iterator();
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
      MultiProperty prop = (MultiProperty)createCollection(context.getRequiredType());
      prop.setName(reader.getAttribute("name"));
      while (reader.hasMoreChildren()) {
         reader.moveDown();
         JMeterProperty subProp = (JMeterProperty)readItem(reader, context, prop);
         prop.addProperty(subProp);
         reader.moveUp();
     }
      return prop;
   }
   /**
    * @param arg0
    * @param arg1
    */
   public MultiPropertyConverter(ClassMapper arg0, String arg1)
   {
      super(arg0, arg1);
   }
}
