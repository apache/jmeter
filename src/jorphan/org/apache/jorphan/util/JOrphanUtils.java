/*
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
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
package org.apache.jorphan.util;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Vector;

import junit.framework.TestCase;

/**
 * This class contains frequently-used static utility methods.
 *
 * @author     <a href="mailto://jsalvata@atg.com">Jordi Salvat i Alabart</a>
 * Created    27th December 2002
 * @version    $Revision$ Last updated: $Date$
 */
public final class JOrphanUtils
{
    /**
     * Private constructor to prevent instantiation.
     */
    private JOrphanUtils()
    {
    }
    
    /**
     * This is equivalent to the String.split method in JDK 1.4. It is
     * here to enable us to support earlier JDKs.
     *
     * <P>This piece of code used to be part of JMeterUtils, but was moved
     * here because some JOrphan classes use it too.
     *
     * @param  splittee   String to be split
     * @param  splitChar  Character to split the string on
     * @return            Array of all the tokens.
     */
    public static String[] split(String splittee, String splitChar)
    {
        if (splittee == null || splitChar == null)
        {
            return new String[0];
        }
        int spot;
        while ((spot = splittee.indexOf(splitChar + splitChar)) != -1)
        {
            splittee =
                splittee.substring(0, spot + splitChar.length())
                    + splittee.substring(
                        spot + 2 * splitChar.length(),
                        splittee.length());
        }
        Vector returns = new Vector();
        int start = 0;
        int length = splittee.length();
        spot = 0;
        while (start < length
            && (spot = splittee.indexOf(splitChar, start)) > -1)
        {
            if (spot > 0)
            {
                returns.addElement(splittee.substring(start, spot));
            }
            start = spot + splitChar.length();
        }
        if (start < length)
        {
            returns.add(splittee.substring(start));
        }
        String[] values = new String[returns.size()];
        returns.copyInto(values);
        return values;
    }

	private static final String SPACES = "                                 ";
	private static final int SPACES_LEN = SPACES.length(); 

    /**
     * Right aligns some text in a StringBuffer
     * N.B. modifies the input buffer
     * 
     * @param in StringBuffer containing some text
     * @param len output length desired
     * @return input StringBuffer, with leading spaces
     */
	public static StringBuffer rightAlign(StringBuffer in, int len){
		int pfx = len - in.length(); 
		if (pfx <= 0 ) return in;
		if (pfx > SPACES_LEN) pfx = SPACES_LEN;
		in.insert(0,SPACES.substring(0,pfx));
		return in;
	}
	
	/**
	 * Left aligns some text in a StringBuffer
	 * N.B. modifies the input buffer
	 * 
	 * @param in StringBuffer containing some text
	 * @param len output length desired
	 * @return input StringBuffer, with trailing spaces
	 */
	public static StringBuffer leftAlign(StringBuffer in, int len){
		int sfx = len - in.length(); 
		if (sfx <= 0 ) return in;
		if (sfx > SPACES_LEN) sfx = SPACES_LEN;
		in.append(SPACES.substring(0,sfx));
		return in;
	}
	
	/**
	 * Convert a boolean to its string representation
	 * Equivalent to Boolean.valueOf(boolean).toString()
	 * but valid also for JDK 1.3, which does not have valueOf(boolean)
	 * 
	 * @param value boolean to convert
	 * @return "true" or "false"
	 */
	public static String booleanToString(boolean value){
		return value ? "true" : "false";
	}

	/**
	 * Convert a boolean to its string representation
	 * Equivalent to Boolean.valueOf(boolean).toString().toUpperCase()
	 * but valid also for JDK 1.3, which does not have valueOf(boolean)
	 * 
	 * @param value boolean to convert
	 * @return "TRUE" or "FALSE"
	 */
	public static String booleanToSTRING(boolean value){
		return value ? "TRUE" : "FALSE";
	}
	
	/**
	 * Version of Boolean.valueOf() for JDK 1.3
	 * 
	 * @param value boolean to convert
	 * @return Boolean.TRUE or Boolean.FALSE
	 */
	public static Boolean valueOf(boolean value)
	{
	    return value ? Boolean.TRUE : Boolean.FALSE;	
	}
	
	private static Method decodeMethod = null;
	private static Method encodeMethod = null;
	
	static {
		Class URLEncoder = URLEncoder.class;
		Class URLDecoder = URLDecoder.class;
		Class [] argTypes = { String.class, String.class };
		try
        {
            decodeMethod = URLDecoder.getMethod("decode",argTypes);
			encodeMethod = URLEncoder.getMethod("encode",argTypes);
			//System.out.println("Using JDK1.4 xxcode() calls");
		}
		catch (Exception e)
		{
			//e.printStackTrace();
		}
		//System.out.println("java.version="+System.getProperty("java.version"));
	}
	
	/**
	 * Version of URLEncoder().encode(string,encoding) for JDK1.3
	 * Also supports JDK1.4 (but will be a bit slower)
	 * 
	 * @param string to be encoded
	 * @param encoding (ignored for JDK1.3)
	 * @return the encoded string
	 */
	public static String encode(String string, String encoding)
	throws UnsupportedEncodingException
	{
		if (encodeMethod != null) {
			//JDK1.4: return URLEncoder.encode(string, encoding);
			Object args [] = {string,encoding};
			try
            {
                return (String) encodeMethod.invoke(null, args );
            }
            catch (Exception e)
            {
				e.printStackTrace();
            	return string;
            }
		} else {
			return URLEncoder.encode(string);
		}
		
	}  

	/**
	 * Version of URLDecoder().decode(string,encoding) for JDK1.3
	 * Also supports JDK1.4 (but will be a bit slower)
	 * 
	 * @param string to be decoded
	 * @param encoding (ignored for JDK1.3)
	 * @return the encoded string
	 */
	public static String decode(String string, String encoding)
	throws UnsupportedEncodingException
	{
		if (decodeMethod != null) {
			//JDK1.4: return URLDecoder.decode(string, encoding);
			Object args [] = {string,encoding};
			try {
				return (String) decodeMethod.invoke(null, args );
			}
			catch (Exception e)
			{
				e.printStackTrace();
				return string;
			}
		} else {
			return URLDecoder.decode(string);
		}
	}  

    /**
     * Simple-minded String.replace() for JDK1.3
     * Should probably be recoded...
     * 
     * @param source input string
     * @param search string to look for (no regular expressions)
     * @param replace string to replace the search string
     * @return the output string
     */
    public static String replaceFirst(String source, String search,String replace)
    {
    	int start=source.indexOf(search);
    	int len=search.length();
    	if (start == -1) return source;
    	if (start == 0) return replace+source.substring(len);
    	return source.substring(0,start)+replace+source.substring(start+len);
    }
    
    public static class Test extends TestCase
    {
    	public void testReplace1()
    	{
    		assertEquals("xyzdef",replaceFirst("abcdef","abc","xyz"));
    	}
    	public void testReplace2()
    	{
			assertEquals("axyzdef",replaceFirst("abcdef","bc","xyz"));
		}
		public void testReplace3()
		{
			assertEquals("abcxyz",replaceFirst("abcdef","def","xyz"));
		}
		public void testReplace4()
		{
			assertEquals("abcdef",replaceFirst("abcdef","bce","xyz"));
		}
		public void testReplace5()
		{
			assertEquals("abcdef",replaceFirst("abcdef","alt=\"\" ",""));
		}
		public void testReplace6()
		{
			assertEquals("abcdef",replaceFirst("abcdef","alt=\"\" ",""));
		}
		public void testReplace7()
		{
			assertEquals("alt=\"\"",replaceFirst("alt=\"\"","alt=\"\" ",""));
		}
		public void testReplace8()
		{
			assertEquals("img src=xyz ",replaceFirst("img src=xyz alt=\"\" ","alt=\"\" ",""));
		}
    }
}
