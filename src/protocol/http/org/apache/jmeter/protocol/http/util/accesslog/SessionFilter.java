/*
 * Created on May 21, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.apache.jmeter.protocol.http.util.accesslog;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.jmeter.testelement.TestCloneable;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;

/**
 * @author mstover
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class SessionFilter implements Filter,Serializable,TestCloneable
{
   static Logger log = LoggingManager.getLoggerForClass();
   /**
    * This object is static across multiple threads in a test,
   	* via clone() method.
    */
   protected List excludedIps; 
   String ipAddress;
   
   
   /* (non-Javadoc)
    * @see org.apache.jmeter.protocol.http.util.accesslog.LogFilter#excPattern(java.lang.String)
    */
   protected boolean hasExcPattern(String text)
   {
      synchronized(excludedIps)
      {
	      boolean exclude = false;
	      for(Iterator x = excludedIps.iterator();x.hasNext();)
	      {
	         if(text.indexOf((String)x.next()) > -1)
	         {
	            exclude = true;
	            break;
	         }
	      }
	      if(!exclude)
	      {
	         ipAddress = getIpAddress(text);
	         excludedIps.add(ipAddress);
	      }
	      return exclude;
      }
   }
   
   protected String getIpAddress(String logLine)
   {
      Pattern incIp = JMeterUtils.getPatternCache()
      		.getPattern("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}", 
            Perl5Compiler.READ_ONLY_MASK | Perl5Compiler.SINGLELINE_MASK);
      Perl5Matcher matcher = JMeterUtils.getMatcher();
      matcher.contains(logLine,incIp);
      return matcher.getMatch().group(0);
   }
   /* (non-Javadoc)
    * @see org.apache.jmeter.protocol.http.util.accesslog.Filter#reset()
    */
   public void reset()
   {
      ipAddress = null;
   }
   
   public Object clone()
   {
      SessionFilter f = new SessionFilter();
      f.excludedIps = excludedIps;
      return f;
   }
   
   /**
    * 
    */
   public SessionFilter()
   {
      excludedIps = new LinkedList();
   }
   /* (non-Javadoc)
    * @see org.apache.jmeter.protocol.http.util.accesslog.Filter#excludeFiles(java.lang.String[])
    */
   public void excludeFiles(String[] filenames)
   {
   }
   /* (non-Javadoc)
    * @see org.apache.jmeter.protocol.http.util.accesslog.Filter#excludePattern(java.lang.String[])
    */
   public void excludePattern(String[] regexp)
   {
   }
   /* (non-Javadoc)
    * @see org.apache.jmeter.protocol.http.util.accesslog.Filter#filter(java.lang.String)
    */
   public String filter(String text)
   {
      return text;
   }
   /* (non-Javadoc)
    * @see org.apache.jmeter.protocol.http.util.accesslog.Filter#includeFiles(java.lang.String[])
    */
   public void includeFiles(String[] filenames)
   {
   }
   /* (non-Javadoc)
    * @see org.apache.jmeter.protocol.http.util.accesslog.Filter#includePattern(java.lang.String[])
    */
   public void includePattern(String[] regexp)
   {
   }
   /* (non-Javadoc)
    * @see org.apache.jmeter.protocol.http.util.accesslog.Filter#isFiltered(java.lang.String)
    */
   public boolean isFiltered(String path)
   {
      if(ipAddress != null)
      {
         log.debug("looking for ip address: " + ipAddress + " in line: " + path);
         return !(path.indexOf(ipAddress) > -1);
      }
      else return hasExcPattern(path);
   }
   /* (non-Javadoc)
    * @see org.apache.jmeter.protocol.http.util.accesslog.Filter#setReplaceExtension(java.lang.String, java.lang.String)
    */
   public void setReplaceExtension(String oldextension, String newextension)
   {
   }
}
