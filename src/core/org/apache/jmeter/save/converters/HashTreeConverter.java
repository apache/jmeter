/*
 * Created on Jun 18, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.apache.jmeter.save.converters;

import java.util.Iterator;

import org.apache.jorphan.collections.HashTree;
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
public class HashTreeConverter extends AbstractCollectionConverter
{
   Logger log = LoggingManager.getLoggerForClass();

   /* (non-Javadoc)
    * @see com.thoughtworks.xstream.converters.Converter#canConvert(java.lang.Class)
    */
   public boolean canConvert(Class arg0)
   {
      return HashTree.class.isAssignableFrom(arg0);
   }

   /* (non-Javadoc)
    * @see com.thoughtworks.xstream.converters.Converter#marshal(java.lang.Object, com.thoughtworks.xstream.io.HierarchicalStreamWriter, com.thoughtworks.xstream.converters.MarshallingContext)
    */
   public void marshal(Object arg0, HierarchicalStreamWriter writer,
         MarshallingContext context)
   {
      HashTree tree = (HashTree)arg0;
      Iterator iter = tree.list().iterator();
      while(iter.hasNext())
      {
         Object item = iter.next();
         writeItem(item,context,writer);
         writeItem(tree.getTree(item),context,writer);
      }

   }

   /* (non-Javadoc)
    * @see com.thoughtworks.xstream.converters.Converter#unmarshal(com.thoughtworks.xstream.io.HierarchicalStreamReader, com.thoughtworks.xstream.converters.UnmarshallingContext)
    */
   public Object unmarshal(HierarchicalStreamReader reader,
         UnmarshallingContext context)
   {
      boolean isKey = true;
      Object current = null;
      HashTree tree = (HashTree) createCollection(context.getRequiredType());
      while (reader.hasMoreChildren()) {
         reader.moveDown();
         Object item = readItem(reader, context, tree);
         if(isKey)
         {
            tree.add(item);
            current = item;
            isKey = false;
         }
         else
         {
            tree.set(current,(HashTree)item);
            isKey = true;
         }
         reader.moveUp();
     }
      return tree;
   }
   /**
    * @param arg0
    * @param arg1
    */
   public HashTreeConverter(ClassMapper arg0, String arg1)
   {
      super(arg0, arg1);
   }
}
