/*
 * Created on May 24, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.apache.jmeter.protocol.http.sampler;

import java.beans.PropertyDescriptor;

import org.apache.jmeter.protocol.http.util.accesslog.Filter;
import org.apache.jmeter.protocol.http.util.accesslog.LogParser;
import org.apache.jmeter.testbeans.BeanInfoSupport;
import org.apache.jmeter.testbeans.gui.FileEditor;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.reflect.ClassFinder;
import org.apache.log.Logger;

/**
 * @author mstover
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class AccessLogSamplerBeanInfo extends BeanInfoSupport
{
   Logger log = LoggingManager.getLoggerForClass();
   /**
    * @param beanClass
    */
   public AccessLogSamplerBeanInfo()
   {
      super(AccessLogSampler.class);
      log.info("Entered access log sampler bean info");
      try
      {
      createPropertyGroup("defaults",new String[]{"domain","portString","imageParsing"});
      
      createPropertyGroup("plugins",new String[]{"parserClassName","filterClassName"});
      
      createPropertyGroup("accesslogfile", new String[] { "logFile"});   
      
      PropertyDescriptor p;
      
         p = property("parserClassName");
         p.setValue(NOT_UNDEFINED, Boolean.TRUE);
         p.setValue(DEFAULT, "org.apache.jmeter.protocol.http.util.accesslog.TCLogParser");
         p.setValue(NOT_OTHER,Boolean.TRUE);
         p.setValue(NOT_EXPRESSION,Boolean.TRUE);
         log.info("found parsers: " +ClassFinder.findClassesThatExtend(JMeterUtils.getSearchPaths(),new Class[]{LogParser.class}));
         p.setValue(TAGS,
               (String[])ClassFinder.findClassesThatExtend(JMeterUtils.getSearchPaths(),
                     new Class[]{LogParser.class}).toArray(new String[0]));
         
         p = property("filterClassName");
         p.setValue(NOT_UNDEFINED, Boolean.FALSE);
         p.setValue(DEFAULT,"");
         p.setValue(NOT_EXPRESSION,Boolean.TRUE);
         p.setValue(TAGS,
               (String[])ClassFinder.findClassesThatExtend(JMeterUtils.getSearchPaths(),
                     new Class[]{Filter.class},false).toArray(new String[0]));
      
      p = property("logFile");
      p.setValue(NOT_UNDEFINED,Boolean.TRUE);
      p.setValue(DEFAULT,"");
      p.setPropertyEditorClass(FileEditor.class);
      
      p = property("domain");
      p.setValue(NOT_UNDEFINED,Boolean.TRUE);
      p.setValue(DEFAULT,"");
      
      p = property("portString");
      p.setValue(NOT_UNDEFINED,Boolean.TRUE);
      p.setValue(DEFAULT,"");
      
      p = property("imageParsing");
      p.setValue(NOT_UNDEFINED,Boolean.TRUE);
      p.setValue(DEFAULT,Boolean.FALSE);
      p.setValue(NOT_OTHER,Boolean.TRUE);
      }
      catch (Exception e)
      {
         log.warn("couldn't find classes and set up properties",e);
         throw new RuntimeException("Could not find classes with class finder");
      }
      log.info("Got to end of access log samper bean info init");
   }

}
