/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

package org.apache.jorphan.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.log.Logger;
import org.apache.jorphan.logging.LoggingManager;

/**
 * This class contains frequently-used static utility methods.
 * 
 * @author <a href="mailto://jsalvata@atg.com">Jordi Salvat i Alabart</a>
 *         Created 27th December 2002
 * @version $Revision$ Last updated: $Date$
 */
public final class JOrphanUtils {
	private static Logger log = LoggingManager.getLoggerForClass();

	/**
	 * Private constructor to prevent instantiation.
	 */
	private JOrphanUtils() {
	}

	/**
	 * This is _almost_ equivalent to the String.split method in JDK 1.4. It is
	 * here to enable us to support earlier JDKs.
	 * 
	 * Note that unlike JDK1.4 split(), it optionally ignores leading split Characters,
     * and the splitChar parameter is not a Regular expression
	 * 
	 * <P>
	 * This piece of code used to be part of JMeterUtils, but was moved here
	 * because some JOrphan classes use it too.
	 * 
	 * @param splittee
	 *            String to be split
	 * @param splitChar
	 *            Character(s) to split the string on, these are treated as a single unit
     * @param truncate
     *            Should adjacent and leading/trailing splitChars be removed?
     *            
	 * @return Array of all the tokens.
     * 
     * @see #split(String, String, String)
     * 
	 */
	public static String[] split(String splittee, String splitChar,boolean truncate) {
		if (splittee == null || splitChar == null) {
			return new String[0];
		}
        final String EMPTY_ELEMENT = "";
		int spot;
        final int splitLength = splitChar.length();
        final String adjacentSplit = splitChar + splitChar;
        final int adjacentSplitLength = adjacentSplit.length();
        if(truncate) {
            while ((spot = splittee.indexOf(adjacentSplit)) != -1) {
    			splittee = splittee.substring(0, spot + splitLength)
    					+ splittee.substring(spot + adjacentSplitLength, splittee.length());
    		}
            if(splittee.startsWith(splitChar)) splittee = splittee.substring(splitLength);
            if(splittee.endsWith(splitChar)) // Remove trailing splitter
                splittee = splittee.substring(0,splittee.length()-splitLength);
        }
		Vector returns = new Vector();
        final int length = splittee.length(); // This is the new length
		int start = 0;
		spot = 0;
        while (start < length && (spot = splittee.indexOf(splitChar, start)) > -1) {
			if (spot > 0) {
				returns.addElement(splittee.substring(start, spot));
			}
            else
            {
                returns.addElement(EMPTY_ELEMENT);
            }
			start = spot + splitLength;
		}
		if (start < length) {
			returns.add(splittee.substring(start));
		} else if (spot == length - splitLength){// Found splitChar at end of line
            returns.addElement(EMPTY_ELEMENT);
        }
		String[] values = new String[returns.size()];
		returns.copyInto(values);
		return values;
	}
    
    public static String[] split(String splittee,String splitChar)
    {
        return split(splittee,splitChar,true);
    }

    /**
     * Takes a String and a tokenizer character string, and returns a new array of
     * strings of the string split by the tokenizer character(s).
     * 
     * Trailing delimiters are significant (unless the default = null)
     *  
     * @param splittee
     *            String to be split.
     * @param delims
     *            Delimiter character(s) to split the string on
     * @param def
     *            Default value to place between two split chars that have
     *            nothing between them. If null, then ignore omitted elements.
     *
     * @return Array of all the tokens.
     * 
     * @throws NullPointerException if splittee or delims are null
     * 
     * @see #split(String, String, boolean)
     * @see #split(String, String)
     * 
     * This is a rewritten version of JMeterUtils.split()
     */
    public static String[] split(String splittee, String delims, String def) {
        StringTokenizer tokens = new StringTokenizer(splittee,delims,def!=null);
        boolean lastWasDelim=false;
        List strList=new ArrayList();
        while (tokens.hasMoreTokens()) {
            String tok=tokens.nextToken();
            if (   tok.length()==1 // we have a single character; could be a token 
                && delims.indexOf(tok)!=-1) // it is a token
            {
                if (lastWasDelim) {// we saw a delimiter last time
                    strList.add(def);// so add the default
                }
                lastWasDelim=true;
            } else {
                lastWasDelim=false;
                strList.add(tok);
            }
        }
        if (lastWasDelim) {
            strList.add(def);
        }
        return (String[])strList.toArray(new String[0]);
    }
    
    
    private static final String SPACES = "                                 ";

	private static final int SPACES_LEN = SPACES.length();

	/**
	 * Right aligns some text in a StringBuffer N.B. modifies the input buffer
	 * 
	 * @param in
	 *            StringBuffer containing some text
	 * @param len
	 *            output length desired
	 * @return input StringBuffer, with leading spaces
	 */
	public static StringBuffer rightAlign(StringBuffer in, int len) {
		int pfx = len - in.length();
		if (pfx <= 0)
			return in;
		if (pfx > SPACES_LEN)
			pfx = SPACES_LEN;
		in.insert(0, SPACES.substring(0, pfx));
		return in;
	}

	/**
	 * Left aligns some text in a StringBuffer N.B. modifies the input buffer
	 * 
	 * @param in
	 *            StringBuffer containing some text
	 * @param len
	 *            output length desired
	 * @return input StringBuffer, with trailing spaces
	 */
	public static StringBuffer leftAlign(StringBuffer in, int len) {
		int sfx = len - in.length();
		if (sfx <= 0)
			return in;
		if (sfx > SPACES_LEN)
			sfx = SPACES_LEN;
		in.append(SPACES.substring(0, sfx));
		return in;
	}

	/**
	 * Convert a boolean to its string representation Equivalent to
	 * Boolean.valueOf(boolean).toString() but valid also for JDK 1.3, which
	 * does not have valueOf(boolean)
	 * 
	 * @param value
	 *            boolean to convert
	 * @return "true" or "false"
	 */
	public static String booleanToString(boolean value) {
		return value ? "true" : "false";
	}

	/**
	 * Convert a boolean to its string representation Equivalent to
	 * Boolean.valueOf(boolean).toString().toUpperCase() but valid also for JDK
	 * 1.3, which does not have valueOf(boolean)
	 * 
	 * @param value
	 *            boolean to convert
	 * @return "TRUE" or "FALSE"
	 */
	public static String booleanToSTRING(boolean value) {
		return value ? "TRUE" : "FALSE";
	}

	/**
	 * Version of Boolean.valueOf(boolean) for JDK 1.3
	 * 
	 * @param value
	 *            boolean to convert
	 * @return Boolean.TRUE or Boolean.FALSE
	 */
	public static Boolean valueOf(boolean value) {
		return value ? Boolean.TRUE : Boolean.FALSE;
	}

	private static Method decodeMethod = null;

	private static Method encodeMethod = null;

	static {
		Class URLEncoder = URLEncoder.class;
		Class URLDecoder = URLDecoder.class;
		Class[] argTypes = { String.class, String.class };
		try {
			decodeMethod = URLDecoder.getMethod("decode", argTypes);
			encodeMethod = URLEncoder.getMethod("encode", argTypes);
			// System.out.println("Using JDK1.4 xxcode() calls");
        } catch (NoSuchMethodException e) {
        }
		// System.out.println("java.version="+System.getProperty("java.version"));
	}

	/**
	 * Version of URLEncoder().encode(string,encoding) for JDK1.3 Also supports
	 * JDK1.4 (but will be a bit slower)
	 * 
	 * @param string
	 *            to be encoded
	 * @param encoding
	 *            (ignored for JDK1.3)
	 * @return the encoded string
	 */
	public static String encode(String string, String encoding) throws UnsupportedEncodingException {
		if (encodeMethod != null) {
			// JDK1.4: return URLEncoder.encode(string, encoding);
			Object args[] = { string, encoding };
			try {
				return (String) encodeMethod.invoke(null, args);
			} catch (Exception e) {
				log.warn("Error trying to encode", e);
				return string;
			}
		} else {
			return URLEncoder.encode(string);// JDK1.3
		}

	}

	/**
	 * Version of URLDecoder().decode(string,encoding) for JDK1.3 Also supports
	 * JDK1.4 (but will be a bit slower)
	 * 
	 * @param string
	 *            to be decoded
	 * @param encoding
	 *            (ignored for JDK1.3)
	 * @return the encoded string
	 */
	public static String decode(String string, String encoding) throws UnsupportedEncodingException {
		if (decodeMethod != null) {
			// JDK1.4: return URLDecoder.decode(string, encoding);
			Object args[] = { string, encoding };
			try {
				return (String) decodeMethod.invoke(null, args);
			} catch (Exception e) {
				log.warn("Error trying to decode", e);
				return string;
			}
		} else {
			return URLDecoder.decode(string); // JDK1.3
		}
	}

	/**
	 * Simple-minded String.replace() for JDK1.3 Should probably be recoded...
	 * 
	 * @param source
	 *            input string
	 * @param search
	 *            string to look for (no regular expressions)
	 * @param replace
	 *            string to replace the search string
	 * @return the output string
	 */
	public static String replaceFirst(String source, String search, String replace) {
		int start = source.indexOf(search);
		int len = search.length();
		if (start == -1)
			return source;
		if (start == 0)
			return replace + source.substring(len);
		return source.substring(0, start) + replace + source.substring(start + len);
	}

    /**
     * Version of String.replaceAll() for JDK1.3
     * See below for another version which replaces strings rather than chars
     * 
     * @param source
     *            input string
     * @param search
     *            char to look for (no regular expressions)
     * @param replace
     *            string to replace the search string
     * @return the output string
     */
    public static String replaceAllChars(String source, char search, String replace) {
        char[] chars = source.toCharArray();
        StringBuffer sb = new StringBuffer(source.length()+20);
        for(int i = 0; i < chars.length; i++){
            char c = chars[i];
            if (c == search){
                sb.append(replace);
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * Replace all patterns in a String
     * 
     * @see String.replaceAll(regex,replacement) - JDK1.4 only
     * 
     * @param input - string to be transformed
     * @param pattern - pattern to replace
     * @param sub - replacement
     * @return the updated string
     */
    public static String substitute(final String input, final String pattern, final String sub) {
        StringBuffer ret = new StringBuffer(input.length());
        int start = 0;
        int index = -1;
        final int length = pattern.length();
        while ((index = input.indexOf(pattern, start)) >= start) {
            ret.append(input.substring(start, index));
            ret.append(sub);
            start = index + length;
        }
        ret.append(input.substring(start));
        return ret.toString();
    }

    /**
     * Trim a string by the tokens provided.
     *  
     * @param input string to trim
     * @param delims list of delimiters
     * @return input trimmed at the first delimiter
     */
    public static String trim(final String input, final String delims){
        StringTokenizer tokens = new StringTokenizer(input,delims);
        return tokens.hasMoreTokens() ? tokens.nextToken() : "";
    }
    
    /**
	 * Returns a slice of a byte array.
	 * 
	 * TODO - add bounds checking?
	 * 
	 * @param array -
	 *            input array
	 * @param begin -
	 *            start of slice
	 * @param end -
	 *            end of slice
	 * @return slice from the input array
	 */
	public static byte[] getByteArraySlice(byte[] array, int begin, int end) {
		byte[] slice = new byte[(end - begin + 1)];
		int count = 0;
		for (int i = begin; i <= end; i++) {
			slice[count] = array[i];
			count++;
		}

		return slice;
	}

    /**
     * close a stream with no error thrown
     * @param is - InputStream (may be null)
     */
    public static void closeQuietly(InputStream is){
        try {
            if (is != null) is.close();
        } catch (IOException e) {
        }
    }

    /**
     * close a stream with no error thrown
     * @param os - OutputStream (may be null)
     */
    public static void closeQuietly(OutputStream os){
        try {
            if (os != null) os.close();
        } catch (IOException e) {
        }
    }

    /**
     * close a Writer with no error thrown
     * @param os - Writer (may be null)
     */
    public static void closeQuietly(Writer wr){
        try {
            if (wr != null) wr.close();
        } catch (IOException e) {
        }
    }

    /**
     * close a Reader with no error thrown
     * @param os - Reader (may be null)
     */
    public static void closeQuietly(Reader rd){
        try {
            if (rd != null) rd.close();
        } catch (IOException e) {
        }
    }
}
