/*
 * Created on May 15, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.apache.jmeter.protocol.jdbc.config;

import java.beans.PropertyDescriptor;

import org.apache.jmeter.testbeans.BeanInfoSupport;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * @author mstover
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class DataSourceElementBeanInfo extends BeanInfoSupport
{
   Logger log = LoggingManager.getLoggerForClass();
   /**
    * @param beanClass
    */
   public DataSourceElementBeanInfo()
   {
      super(DataSourceElement.class);
      createPropertyGroup("varName",new String[]{"dataSource"});
      
      createPropertyGroup("pool", new String[] { "poolMax",
            "timeout","trimInterval","autocommit"});
      
      createPropertyGroup("keep-alive", new String[]{"keepAlive",
            "connectionAge","checkQuery"});
      
      createPropertyGroup("database",new String[]{"dbUrl",
            "driver","username","password"});     
      
      PropertyDescriptor p= property("dataSource");
      p.setValue(NOT_UNDEFINED, Boolean.TRUE);
      p.setValue(DEFAULT, "");
      p= property("poolMax");
      p.setValue(NOT_UNDEFINED, Boolean.TRUE);
      p.setValue(DEFAULT, "10");
      p= property("timeout");
      p.setValue(NOT_UNDEFINED, Boolean.TRUE);
      p.setValue(DEFAULT, "10000");
      p= property("trimInterval");
      p.setValue(NOT_UNDEFINED, Boolean.TRUE);
      p.setValue(DEFAULT, "60000");
      p= property("autocommit");
      p.setValue(NOT_UNDEFINED, Boolean.TRUE);
      p.setValue(DEFAULT, new Boolean(true));
      p= property("keepAlive");
      p.setValue(NOT_UNDEFINED, Boolean.TRUE);
      p.setValue(DEFAULT, new Boolean(true));
      p= property("connectionAge");
      p.setValue(NOT_UNDEFINED, Boolean.TRUE);
      p.setValue(DEFAULT, "5000");
      p= property("checkQuery");
      p.setValue(NOT_UNDEFINED, Boolean.TRUE);
      p.setValue(DEFAULT, "Select 1");
      p= property("dbUrl");
      p.setValue(NOT_UNDEFINED, Boolean.TRUE);
      p.setValue(DEFAULT, "");
      p= property("driver");
      p.setValue(NOT_UNDEFINED, Boolean.TRUE);
      p.setValue(DEFAULT, "");
      p= property("username");
      p.setValue(NOT_UNDEFINED, Boolean.TRUE);
      p.setValue(DEFAULT, "");
      p= property("password");
      p.setValue(NOT_UNDEFINED, Boolean.TRUE);
      p.setValue(DEFAULT, "");
   }
}
