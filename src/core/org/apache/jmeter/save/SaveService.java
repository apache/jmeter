/*
 * Copyright 2001-2004 The Apache Software Foundation.
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

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.save.converters.BooleanPropertyConverter;
import org.apache.jmeter.save.converters.HashTreeConverter;
import org.apache.jmeter.save.converters.IntegerPropertyConverter;
import org.apache.jmeter.save.converters.LongPropertyConverter;
import org.apache.jmeter.save.converters.MultiPropertyConverter;
import org.apache.jmeter.save.converters.SampleResultConverter;
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
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.ListedHashTree;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import com.thoughtworks.xstream.XStream;

/**
 * @author     Mike Stover
 * @author     <a href="mailto:kcassell&#X0040;apache.org">Keith Cassell</a>
 */
public class SaveService
{
   private static XStream saver = new XStream();
   private static Logger log = LoggingManager.getLoggerForClass();
   
   // Version information for test plan header
   static String version="1.0";
   static String propertiesVersion="";//read from properties file
   private static final String PROPVERSION="1.3";
   
   // Helper method to simplify alias creation from properties
   private static void makeAlias(String alias,String clazz)
   {
   	try {
		saver.alias(alias,Class.forName(clazz));
	} catch (ClassNotFoundException e) {
		log.warn("Could not set up alias "+alias+" "+e.toString());
	}
   }
   
   private static void initProps()
   {
   // Load the alias properties
	  Properties nameMap = new Properties();
	  try
	  {
	     nameMap.load(
	         new FileInputStream(
	             JMeterUtils.getJMeterHome()
	                 + JMeterUtils.getPropDefault(
	                     "saveservice_properties",
	                     "/bin/saveservice.properties")));
	     // now create the aliases
		 Iterator it = nameMap.entrySet().iterator();
		 while (it.hasNext())
		 {
		 	Map.Entry me = (Map.Entry) it.next();
		 	String key = (String)me.getKey();
		 	String val = (String)me.getValue();
		 	if (!key.startsWith("_"))
		 	{
		 		makeAlias(key,val);
		 	}
		 	else
		 	{
		 		//process special keys
		 		if (key.equalsIgnoreCase("_version"))
		 		{
		 			val=extractVersion(val);
		 			log.info("Using SaveService properties file "+val);
		 			propertiesVersion=val;
		 		}
		 	}
		 }
	  }
	  catch (Exception e)
	  {
	     log.error("Bad saveservice properties file",e);
	  }
   }
	  
   static
   {
	  initProps();
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
      
      checkVersions();
   }

   public static void saveTree(HashTree tree,Writer writer) throws Exception
   {
      ScriptWrapper wrapper = new ScriptWrapper();
      wrapper.testPlan = tree;
      saver.toXML(wrapper,writer);
   }
   
   static boolean versionsOK = true;
   
   // Extract version digits from String of the form #Revision: n.mm #
   // (where # is actually $ above)
   private static final String REVPFX = "$Revision: ";
   private static final String REVSFX = " $";
   private static String extractVersion(String rev)
   {
   	  if (rev.length() > REVPFX.length() + REVSFX.length())
   	  {
   	      return rev.substring(REVPFX.length(),rev.length()-REVSFX.length());
   	  }
   	  else
   	  {
   	     return rev;
   	  }
   }
	private static void checkVersion(Class clazz, String expected) {
	
		String actual="*NONE*";
		try {
			actual=(String)clazz.getMethod("getVersion",null).invoke(null,null);
			actual = extractVersion(actual);
		} catch (Exception e) 
		{
			//Not needed
		}
		if (0!=actual.compareTo(expected))
		{
			versionsOK=false;
			log.warn("Version mismatch: expected '"+expected+ "' found '"+actual+"' in "+clazz.getName());
		}
	}

	private static void checkVersions()
	{
		versionsOK=true;
		checkVersion(BooleanPropertyConverter.class,"1.4");
		checkVersion(HashTreeConverter.class,"1.2");
		checkVersion(IntegerPropertyConverter.class,"1.3");
		checkVersion(LongPropertyConverter.class,"1.3");
		checkVersion(MultiPropertyConverter.class,"1.3");
		checkVersion(SampleResultConverter.class,"1.2");
		checkVersion(StringPropertyConverter.class,"1.5");
		checkVersion(TestElementConverter.class,"1.2");
		checkVersion(TestElementPropertyConverter.class,"1.3");
		checkVersion(ScriptWrapperConverter.class,"1.2");
		if (!PROPVERSION.equalsIgnoreCase(propertiesVersion))
		{
			log.warn("Property file - expected "+PROPVERSION+", found "+propertiesVersion);
		}
		if (versionsOK)
		{
			log.info("All converter versions present and correct");
		}
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
   public static class Test extends JMeterTestCase
   {
	public Test() {
		super();
	}

    public Test(String name) {
		super(name);
	}

   	public void testVersions() throws Exception
   	{
   		initProps();
   		checkVersions();
   		assertTrue("Unexpected version found",versionsOK);
   		assertEquals("Property Version mismatch",PROPVERSION,propertiesVersion);
   	}
   }
}
