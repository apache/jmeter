/*
 * Created on Jun 13, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.apache.jmeter.save;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;

import org.apache.jmeter.save.converters.BooleanPropertyConverter;
import org.apache.jmeter.save.converters.HashTreeConverter;
import org.apache.jmeter.save.converters.IntegerPropertyConverter;
import org.apache.jmeter.save.converters.LongPropertyConverter;
import org.apache.jmeter.save.converters.MultiPropertyConverter;
import org.apache.jmeter.save.converters.StringPropertyConverter;
import org.apache.jmeter.save.converters.TestElementConverter;
import org.apache.jmeter.save.converters.TestElementPropertyConverter;
import org.apache.jmeter.testelement.property.BooleanProperty;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.IntegerProperty;
import org.apache.jmeter.testelement.property.LongProperty;
import org.apache.jmeter.testelement.property.MapProperty;
import org.apache.jmeter.testelement.property.StringProperty;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.ListedHashTree;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import com.thoughtworks.xstream.XStream;

/**
 * @author mstover
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class SaveService
{
   private static XStream saver = new XStream();
   private static Logger log = LoggingManager.getLoggerForClass();
   
   static
   {
      saver.alias("stringProp",StringProperty.class);
      saver.alias("intProp",IntegerProperty.class);
      saver.alias("longProp",LongProperty.class);
      saver.alias("collectionProp",CollectionProperty.class);
      saver.alias("mapProp",MapProperty.class);
      saver.alias("elementProp",TestElementProperty.class);
      saver.alias("boolProp",BooleanProperty.class);
      saver.alias("hashTree",ListedHashTree.class);
      saver.alias("jmeterTestPlan",ScriptWrapper.class);
      saver.registerConverter(new StringPropertyConverter());
      saver.registerConverter(new BooleanPropertyConverter());
      saver.registerConverter(new IntegerPropertyConverter());
      saver.registerConverter(new LongPropertyConverter());
      saver.registerConverter(new TestElementConverter(saver.getClassMapper(),"class"));
      saver.registerConverter(new MultiPropertyConverter(saver.getClassMapper(),"class"));
      saver.registerConverter(new TestElementPropertyConverter(saver.getClassMapper(),"class"));
      saver.registerConverter(new HashTreeConverter(saver.getClassMapper(),"class"));
      saver.registerConverter(new ScriptWrapperConverter(saver.getClassMapper()));
   }

   public static void saveTree(HashTree tree,Writer writer) throws Exception
   {
      ScriptWrapper wrapper = new ScriptWrapper();
      wrapper.testPlan = tree;
      saver.toXML(wrapper,writer);
   }
   
   public static HashTree loadTree(InputStream reader) throws Exception
   {
      if(!reader.markSupported())
      {
         reader = new BufferedInputStream(reader);
      }
      reader.mark(Integer.MAX_VALUE);
      ScriptWrapper wrapper = null;
      try
      {
         wrapper = (ScriptWrapper)saver.fromXML(new InputStreamReader(reader));
         return wrapper.testPlan;
      }
      catch (RuntimeException e)
      {
         log.warn("Problem loading new style: ",e);
         reader.reset();
         return OldSaveService.loadSubTree(reader);
      }
   }
}
