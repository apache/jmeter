/*
 * Created on Jun 18, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.apache.jmeter.save.converters;

import org.apache.jmeter.testelement.property.BooleanProperty;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * @author mstover
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class BooleanPropertyConverter implements Converter
{

	public static String getVersion(){	return "$Revision$";}
	
   /* (non-Javadoc)
    * @see com.thoughtworks.xstream.converters.Converter#canConvert(java.lang.Class)
    */
   public boolean canConvert(Class arg0)
   {
      return arg0.equals(BooleanProperty.class);
   }

   /* (non-Javadoc)
    * @see com.thoughtworks.xstream.converters.Converter#marshal(java.lang.Object, com.thoughtworks.xstream.io.HierarchicalStreamWriter, com.thoughtworks.xstream.converters.MarshallingContext)
    */
   public void marshal(Object obj, HierarchicalStreamWriter writer,
         MarshallingContext arg2)
   {
      BooleanProperty prop = (BooleanProperty)obj;
      writer.addAttribute("name",prop.getName());
      writer.setValue(prop.getStringValue());

   }

   /* (non-Javadoc)
    * @see com.thoughtworks.xstream.converters.Converter#unmarshal(com.thoughtworks.xstream.io.HierarchicalStreamReader, com.thoughtworks.xstream.converters.UnmarshallingContext)
    */
   public Object unmarshal(HierarchicalStreamReader reader,
         UnmarshallingContext arg1)
   {
      BooleanProperty prop = new BooleanProperty(reader.getAttribute("name"),Boolean.valueOf(reader.getValue()).booleanValue());
      return prop;
   }
}
