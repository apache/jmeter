/*
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 * if any, must include the following acknowledgment:
 * "This product includes software developed by the
 * Apache Software Foundation (http://www.apache.org/)."
 * Alternately, this acknowledgment may appear in the software itself,
 * if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 * "Apache JMeter" must not be used to endorse or promote products
 * derived from this software without prior written permission. For
 * written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 * "Apache JMeter", nor may "Apache" appear in their name, without
 * prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package org.apache.jmeter.protocol.http.control;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.Vector;

import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.testelement.PerThreadClonable;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.log.Hierarchy;
import org.apache.log.Logger;

/**
 * This class provides an interface to the netscape cookies file to
 * pass cookies along with a request.
 *
 * @author  <a href="mailto:sdowd@arcmail.com">Sean Dowd</a>
 * @version $Revision$ $Date$
 */
public class CookieManager extends ConfigTestElement implements
		  Serializable,PerThreadClonable
{
	transient private static Logger log = Hierarchy.getDefaultHierarchy().getLoggerFor(
			"jmeter.protocol.http");

	 public static final String COOKIES = "CookieManager.cookies";
	  /**
		* A vector of Cookies managed by this class.
		* @associates <{org.apache.jmeter.controllers.Cookie}>
		*/
	  private static SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, dd-MMM-yyyy HH:mm:ss zzz");

	  private static List addableList = new LinkedList();

	 static
	 {
		  // The cookie specification requires that the timezone be GMT.
		  // See http://developer.netscape.com/docs/manuals/communicator/jsguide4/cookies.htm
		  // See http://www.cookiecentral.com/faq/
		  dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
	 }

	  public CookieManager () {
		setProperty(COOKIES,new ArrayList());
	  }

	 public List getCookies()
	 {
		  return (List)getProperty(COOKIES);
	 }

	 public int getCookieCount()
	 {
		  return getCookies().size();
	 }


	 // Incorrect method. Always returns String. I changed CookiePanel code to perform
	 // this lookup.
	 //public Class getColumnClass(int column)
	 //{
	 //	return columnNames[column].getClass();
	 //}

	 public Cookie getCookie(int row)
	 {
		  return (Cookie)getCookies().get(row);
	 }

	  /** save the cookie data to a file */
	  public void save(String authFile) throws IOException {
			File file = new File(authFile);
			 if (!file.isAbsolute()) file = new File(System.getProperty("user.dir") + File.separator + authFile);
			 PrintWriter writer = new PrintWriter(new FileWriter(file));
	 writer.println("# JMeter generated Cookie file");
	 List cookies = getCookies();
	 for (int i = 0; i < cookies.size(); i++) {
		  Cookie cook = (Cookie) cookies.get(i);
		  writer.println(cook.toString());
	 }
	 writer.flush();
	 writer.close();
	  }

	  /** add cookie data from a file */
	  public void addFile (String cookieFile) throws IOException {
	 File file = new File(cookieFile);
			 if (!file.isAbsolute()) file = new File(System.getProperty("user.dir") + File.separator + cookieFile);
	 BufferedReader reader = null;
	 if( file.canRead() ) {
				reader = new BufferedReader(new FileReader(file));
	 } else {
		  throw new IOException("The file you specified cannot be read.");
	 }

			 String line;
			 while ((line = reader.readLine()) != null) {
					 try {
							if (line.startsWith("#") || line.trim().length() == 0) continue;
							String[] st = split(line, "\t"," ");
							int domain = 0;
							int foo = 1;
							int path = 2;
							if(st[path].equals(" "))
								st[path] = "/";
							boolean secure = new Boolean(st[3]).booleanValue();
							long expires = new Long(st[4]).longValue();
							int name = 5;
							int value = 6;
							Cookie cookie = new Cookie(st[name], st[value], st[domain],
										  st[path], secure, expires);
							getCookies().add(cookie);
					 } catch (Exception e) {
							throw new IOException("Error parsing cookie line\n\t'" + line + "'\n\t" + e);
					 }
			 }
			 reader.close();
	  }

	  /** add a cookie */
	  public void add(Cookie c) {
			 getCookies().add(c);
	  }

	  /** add an empty cookie */
	  public void add() {
		  getCookies().add(new Cookie());
	  }

	  /** remove a cookie */
	  public void remove(int index) {
			 getCookies().remove(index);
	  }

	 /** return the number cookies */
	 public int size() {
			return getCookies().size();
	 }

	 /** return the cookie at index i */
	 public Cookie get(int i) {
			return (Cookie) getCookies().get(i);
	 }

	 public String convertLongToDateFormatStr(long dateLong)
	 {
		  return dateFormat.format(new Date(dateLong));
	 }

	 public long convertDateFormatStrToLong(String dateStr)
	 {
		  long time = 0;

		  try
		  {
				Date date = dateFormat.parse(dateStr);
				time = date.getTime();
		  }
		  catch (ParseException e)
		  {
				// ERROR!!!
				// Later, display error dialog?  For now, we have
				// to specify a number that can be converted to
				// a Date. So, I chose 0. The Date will appear as
				// the beginning of the Epoch (Jan 1, 1970 00:00:00 GMT)
				time = 0;
				log.error("DateFormat.ParseException: ",e);
		  }

		  return time;
	 }

	 public String getCookieHeaderForURL(URL url) {
	 if (! url.getProtocol().toUpperCase().trim().equals("HTTP")
			&&
			! url.getProtocol().toUpperCase().trim().equals("HTTPS")) return null;

			 StringBuffer header = new StringBuffer();
			 for (Iterator enum = getCookies().iterator(); enum.hasNext();) {
					 Cookie cookie = (Cookie) enum.next();
					 if (url.getHost().endsWith(cookie.getDomain()) &&
								  url.getFile().startsWith(cookie.getPath()) &&
								  (System.currentTimeMillis() / 1000) <= cookie.getExpires()) {
							if (header.length() > 0) {
								  header.append("; ");
							}
							header.append(cookie.getName()).append("=").append(cookie.getValue());
					 }
			 }

			 if (header.length() != 0) {
					 return header.toString();
			 } else {
					 return null;
			 }
	  }

	  public void addCookieFromHeader(String cookieHeader, URL url) {
			 StringTokenizer st = new StringTokenizer(cookieHeader, ";");
			 String nvp;

			 // first n=v is name=value
			 nvp = st.nextToken();
			 int index = nvp.indexOf("=");
			 String name = nvp.substring(0,index);
			 String value = nvp.substring(index+1);
			 String domain = url.getHost();
			 String path = "/";

			 Cookie newCookie = new Cookie(name, value, domain, path, false,
							System.currentTimeMillis() + 1000 * 60 * 60 * 24);
			 // check the rest of the headers
			 while (st.hasMoreTokens()) {
					 nvp = st.nextToken();
					 nvp = nvp.trim();
					 index = nvp.indexOf("=");
					 if(index == -1)
					 {
						  index = nvp.length();
					 }
					 String key = nvp.substring(0,index);
					 if (key.equalsIgnoreCase("expires")) {
							try {
								  String expires = nvp.substring(index+1);
								  Date date = dateFormat.parse(expires);
								  newCookie.setExpires(date.getTime());
							} catch (ParseException pe) {}
					 } else if (key.equalsIgnoreCase("domain")) {
							newCookie.setDomain(nvp.substring(index+1));
					 } else if (key.equalsIgnoreCase("path")) {
							newCookie.setPath(nvp.substring(index+1));
					 } else if (key.equalsIgnoreCase("secure")) {
							newCookie.setSecure(true);
					 }
			 }

			 Vector removeIndices = new Vector();
			 for (int i = getCookies().size() - 1; i >= 0; i--) {
					 Cookie cookie = (Cookie) getCookies().get(i);
					 if (cookie == null)
							continue;
					 if (cookie.getPath().equals(newCookie.getPath()) &&
								  cookie.getDomain().equals(newCookie.getDomain()) &&
								  cookie.getName().equals(newCookie.getName())) {
							removeIndices.addElement(new Integer(i));
					 }
			 }

			 for (Enumeration e = removeIndices.elements(); e.hasMoreElements();) {
					 index = ((Integer) e.nextElement()).intValue();
					 getCookies().remove(index);
			 }

			 if (newCookie.getExpires() >= System.currentTimeMillis())
			 {
					 getCookies().add(newCookie);
			 }
	  }

	  public void removeCookieNamed(String name) {
			 Vector removeIndices = new Vector();
			 for (int i = getCookies().size() - 1; i > 0; i--) {
					 Cookie cookie = (Cookie) getCookies().get(i);
					 if (cookie == null)
							continue;
					 if (cookie.getName().equals(name))
							removeIndices.addElement(new Integer(i));
			 }

			 for (Enumeration e = removeIndices.elements(); e.hasMoreElements();) {
					 getCookies().remove(((Integer) e.nextElement()).intValue());
			 }

	  }

	  /******************************************************
		* Takes a String and a tokenizer character, and returns
		a new array of strings of the string split by the tokenizer
		character.
		@param splittee String to be split
		@param splitChar Character to split the string on
		@param def Default value to place between two split chars that have
		nothing between them
		@return Array of all the tokens.
	  ******************************************************/
	  public String[] split(String splittee,String splitChar,String def)
	  {
	 if(splittee == null || splitChar == null)
			return new String[0];
	 StringTokenizer tokens;
	 String temp;
	 int spot;
	 while((spot=splittee.indexOf(splitChar+splitChar))!=-1)
			splittee=splittee.substring(0,spot+splitChar.length())+def+
		  splittee.substring(spot+1*splitChar.length(),
						  splittee.length());
	 Vector returns=new Vector();
	 tokens=new StringTokenizer(splittee,splitChar);
	 while(tokens.hasMoreTokens()) {
			temp=(String)tokens.nextToken();
			returns.addElement(temp);
	 }
	 String[] values=new String[returns.size()];
	 returns.copyInto(values);
	 return values;
	  }

	  public String getClassLabel()
	  {
	return JMeterUtils.getResString("cookie_manager_title");
	  }
}
