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

package org.apache.jmeter.save.converters;

import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.protocol.http.control.Header;
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

   /** Returns the converter version; used to check for possible incompatibilities */
	public static String getVersion(){	return "$Revision$";}

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
      writer.addAttribute("name", ConversionHelp.encode(prop.getName()));
      writer.addAttribute("elementType", prop.getObjectValue().getClass()
            .getName());
      PropertyIterator iter = prop.iterator();
      while (iter.hasNext())
      {
         writeItem(iter.next(), context, writer);
      }
   }

/*
 * TODO - convert to woek more like upgrade.properties/NameUpdater.java
 * 
 * Special processing is carried out for the Header Class
 * The String property TestElement.name is converted to Header.name
 * for example:
   <elementProp name="User-Agent" elementType="org.apache.jmeter.protocol.http.control.Header">
      <stringProp name="Header.value">Mozilla%2F4.0+%28compatible%3B+MSIE+5.5%3B+Windows+98%29</stringProp>
       <stringProp name="TestElement.name">User-Agent</stringProp>
    </elementProp>  
 *   becomes
   <elementProp name="User-Agent" elementType="org.apache.jmeter.protocol.http.control.Header">
      <stringProp name="Header.value">Mozilla%2F4.0+%28compatible%3B+MSIE+5.5%3B+Windows+98%29</stringProp>
       <stringProp name="Header.name">User-Agent</stringProp>
    </elementProp>
*/
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
         prop.setName(ConversionHelp.decode(reader.getAttribute("name")));
         String element = reader.getAttribute("elementType");
         boolean isHeader = "org.apache.jmeter.protocol.http.control.Header".equals(element);
         prop.setObjectValue(Class.forName(element).newInstance());
         while (reader.hasMoreChildren())
         {
            reader.moveDown();
            JMeterProperty subProp = (JMeterProperty) readItem(reader, context,
                  prop);
            if (isHeader) {
                String name = subProp.getName();
                if (TestElement.NAME.equals(name)){
                    subProp.setName(Header.HNAME);
                }
            }
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
