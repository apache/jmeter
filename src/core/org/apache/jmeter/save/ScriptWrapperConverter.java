/*
 * Copyright 2004 The Apache Software Foundation.
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
    /** Returns the converter version; used to check for possible incompatibilities */
	public static String getVersion(){	return "$Revision$";}
	
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
      writer.addAttribute("version",SaveService.version);
      writer.addAttribute("properties",SaveService.propertiesVersion);
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
