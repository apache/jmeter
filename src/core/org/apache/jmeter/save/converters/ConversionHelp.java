/*
 * Created on Jul 27, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.apache.jmeter.save.converters;

import java.net.URLDecoder;
import java.net.URLEncoder;

import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * @author mstover
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ConversionHelp
{
   private static final String CHAR_SET = "UTF-8";
   transient private static final Logger log = LoggingManager.getLoggerForClass();

   public static String encode(String p)
   {
      try
      {
         String p1 = URLEncoder.encode(p,CHAR_SET);
         return p1;
      }
      catch (Exception e)
      {
         log.warn("System doesn't support utf-8",e);
         return p;
      }
   }
   
   public static String decode(String p)
   {
      try
      {
         return URLDecoder.decode(p,CHAR_SET);
      }
      catch (Exception e)
      {
         log.warn("System doesn't support utf-8",e);
         return p;
      }
   }
}
