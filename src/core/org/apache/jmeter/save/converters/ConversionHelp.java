/*
 * Created on Jul 27, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.apache.jmeter.save.converters;

import java.io.UnsupportedEncodingException;

import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.util.JOrphanUtils;
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
      if(p == null)
      {
         return "";
      }
      try
      {
         String p1 = JOrphanUtils.encode(p,CHAR_SET);
         return p1;
      }
      catch (UnsupportedEncodingException e)
      {
         log.warn("System doesn't support " + CHAR_SET,e);
         return p;
      }
   }
   
   public static String decode(String p)
   {
      if(p == null)
      {
         return null;
      }
      try
      {
         return JOrphanUtils.decode(p,CHAR_SET);
      }
      catch (UnsupportedEncodingException e)
      {
         log.warn("System doesn't support " + CHAR_SET,e);
         return p;
      }
   }
   
   public static String cdata(byte[] chars,String encoding) throws UnsupportedEncodingException
   {
      StringBuffer buf = new StringBuffer("<![CDATA[");
      buf.append(new String(chars,encoding));
      buf.append("]]>");
      return buf.toString();
   }
}
