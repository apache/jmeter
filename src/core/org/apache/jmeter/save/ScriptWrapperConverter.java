/*
 * Created on Jun 19, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.apache.jmeter.save;

import org.apache.jorphan.collections.HashTree;

import com.thoughtworks.xstream.alias.ClassMapper;
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
class ScriptWrapperConverter implements Converter
{
   ClassMapper classMapper;
   
   ScriptWrapperConverter(ClassMapper classMapper)
   {
      this.classMapper = classMapper;
   }

   /* (non-Javadoc)
    * @see com.thoughtworks.xstream.converters.Converter#canConvert(java.lang.Class)
    */
   public boolean canConvert(Class arg0)
   {
      return arg0.equals(ScriptWrapper.class);
   }

   /* (non-Javadoc)
    * @see com.thoughtworks.xstream.converters.Converter#marshal(java.lang.Object, com.thoughtworks.xstream.io.HierarchicalStreamWriter, com.thoughtworks.xstream.converters.MarshallingContext)
    */
   public void marshal(Object arg0, HierarchicalStreamWriter writer,
         MarshallingContext context)
   {
      ScriptWrapper wrap = (ScriptWrapper)arg0;
      writer.addAttribute("version",wrap.version);
      writer.startNode(classMapper.lookupName(wrap.testPlan.getClass()));
      context.convertAnother(wrap.testPlan);
      writer.endNode();
   }

   /* (non-Javadoc)
    * @see com.thoughtworks.xstream.converters.Converter#unmarshal(com.thoughtworks.xstream.io.HierarchicalStreamReader, com.thoughtworks.xstream.converters.UnmarshallingContext)
    */
   public Object unmarshal(HierarchicalStreamReader reader,
         UnmarshallingContext context)
   {
      ScriptWrapper wrap = new ScriptWrapper();
      wrap.version = reader.getAttribute("version");
      reader.moveDown();
      wrap.testPlan = (HashTree)context.convertAnother(wrap,getNextType(reader));
      return wrap;
   }

   protected Class getNextType(HierarchicalStreamReader reader)
   {
      String classAttribute = reader.getAttribute("class");
      Class type;
      if (classAttribute == null) {
          type = classMapper.lookupType(reader.getNodeName());
      } else {
          type = classMapper.lookupType(classAttribute);
      }
      return type;
   }
}
