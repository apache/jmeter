/*
 * Copyright 2004-2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *  
 */

/*
 * Created on Jul 27, 2004
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

   /*
    *  These must be set before reading/writing the XML. 
    *  Rather a hack, but saves changing all the method calls to include an extra variable.
    */
   private static String inVersion;
   private static String outVersion = "1.1"; // Default for writing
   
   public static void setInVersion(String v)
   {
       inVersion=v;
   }
   
   public static void setOutVersion(String v)
   {
       outVersion=v;
   }
   
   public static String encode(String p)
   {
       if (!"1.0".equals(outVersion)) return p;
       // Only encode strings if inVersion = 1.0
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
       if (!"1.0".equals(inVersion)) return p;
       // Only decode strings if inVersion = 1.0
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
