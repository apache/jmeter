/*
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
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
 * 
 * @author <a href="mailto:jsalvata@apache.org">Jordi Salvat i Alabart</a>
 * @version $Id$
 */
package org.apache.jmeter.protocol.http.parser;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Vector;

import junit.framework.TestCase;

import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * HtmlParsers can parse HTML content to obtain URLs.
 */
public abstract class HTMLParser
{
    /** Used to store the Logger (used for debug and error messages). */
	transient private static Logger log = LoggingManager.getLoggerForClass();

//    /** Singleton */
//    private static HTMLParser parser;

	private static final String PARSER_METHOD = "getParserInstance";
	private static final String PARSER_REUSABLE = "isParserReusable";

    /*
     * Cache of methods.
     * These need to be invoked each time, in case the parser cannot be re-used 
     */
    private static Hashtable methods = new Hashtable(3);

    // Cache of parsers - used if parsers are re-usable
	private static Hashtable parsers = new Hashtable(3);
    
	private final static String PARSER_CLASSNAME = "htmlParser.className";
	    
	private final static String DEFAULT_PARSER = 
        "org.apache.jmeter.protocol.http.parser.HtmlParserHTMLParser";

    /**
     * Protected constructor to prevent instantiation except
     * from within subclasses. 
     */
    protected HTMLParser() {
    }
    
//    /**
//     * Create the single instance.
//     */
//    private static void initialize()
//    {
//        String htmlParserClassName=
//            JMeterUtils.getPropDefault(PARSER_CLASSNAME,DEFAULT_PARSER);
//
//        try
//        {
//            parser=
//                (HTMLParser)Class.forName(htmlParserClassName).newInstance();
//        }
//        catch (InstantiationException e)
//        {
//            throw new Error(e);
//        }
//        catch (IllegalAccessException e)
//        {
//            throw new Error(e);
//        }
//        catch (ClassNotFoundException e)
//        {
//            throw new Error(e);
//        }
//        log.info("Using "+htmlParserClassName);
//    }

    public static final HTMLParser getParser(){
        return getParser(JMeterUtils.getPropDefault(PARSER_CLASSNAME,DEFAULT_PARSER));
    }

	public static final synchronized HTMLParser getParser(String htmlParserClassName){

        // Is there a cached parser?
		HTMLParser pars=(HTMLParser) parsers.get(htmlParserClassName);
		if (pars != null){
			log.info("Fetched "+htmlParserClassName);
			return pars;
		}

        // Is there a cached method for creating a parser?
		Method meth =(Method) methods.get(htmlParserClassName);
		if (meth != null) {
			try
            {
                pars = (HTMLParser) meth.invoke(null,null);
            }
			catch (NullPointerException e) // method did not return anything
			{
				throw new Error(PARSER_METHOD+"() returned null",e);
			}
            catch (IllegalArgumentException e)
            {
				throw new Error("Should not happen",e);
            }
            catch (IllegalAccessException e)
            {
				throw new Error("Should not happen",e);
            }
            catch (InvocationTargetException e)
            {
				throw new Error("Should not happen",e);
            };
			log.info("Refetched "+htmlParserClassName);
			return pars;
		}
		
		// Create the method cache, and the parser cache if possible
		try
		{
			Class clazz = Class.forName(htmlParserClassName);
			meth = clazz.getMethod(PARSER_METHOD,null);
			methods.put(htmlParserClassName,meth);// Cache the method
			pars= (HTMLParser) meth.invoke(null,null);
			boolean reusable=false;
			try{
				reusable=((Boolean)
				           clazz.getMethod(PARSER_REUSABLE,null)
				          .invoke(null,null))
				          .booleanValue();
				if (reusable){
					parsers.put(htmlParserClassName,pars);// cache the parser
				}
			}
			catch (Exception e){
				reusable=false;
			}
			log.info("Created "+htmlParserClassName+(reusable? " - reusable":""));
		}
		catch (NullPointerException e) // method did not return anything
		{
			throw new Error(PARSER_METHOD+"() returned null",e);
		}
		catch (IllegalAccessException e)
		{
			throw new Error(e);
		}
		catch (ClassNotFoundException e)
		{
			throw new Error(e);
		}
		catch (SecurityException e)
        {
			throw new Error(e);
        }
        catch (NoSuchMethodException e)
        {
			throw new Error(e);
        }
        catch (IllegalArgumentException e)
        {
			throw new Error(e);
        }
        catch (InvocationTargetException e)
        {
			throw new Error(e);
        }
    	return pars;
    }
//    /**
//     * Obtain the (singleton) HtmlParser. 
//     * 
//     * @return The single HtmlParser instance.
//     */
//    public static final synchronized HTMLParser xgetParser()
//    {
//        if (parser == null) {
//            initialize();
//        }
//        return parser;
//    }

	/**
	 * Obtain the (singleton) HtmlParser. 
	 * 
	 * @return The single HtmlParser instance.
	 */
	//public static abstract HTMLParser xgetParserInstance();

    /**
     * Get the URLs for all the resources that a browser would automatically
     * download following the download of the HTML content, that is: images,
     * stylesheets, javascript files, applets, etc...
     * <p>
     * URLs should not appear twice in the returned iterator.
     * <p>
     * Malformed URLs can be reported to the caller by having the Iterator
     * return the corresponding RL String. Overall problems parsing the html
     * should be reported by throwing an HTMLParseException. 
     * 
     * @param html HTML code
     * @param baseUrl Base URL from which the HTML code was obtained
     * @return an Iterator for the resource URLs 
     */
    public Iterator getEmbeddedResourceURLs(byte[] html, URL baseUrl)
        throws HTMLParseException
        {    
        	// The Set is used to ignore duplicated binary files.
			// Using a LinkedHashSet to avoid unnecessary overhead in iterating
			// the elements in the set later on. As a side-effect, this will keep
			// them roughly in order, which should be a better model of browser
			// behaviour.
			// N.B. LinkedHashSet is Java 1.4
        	return getEmbeddedResourceURLs(html, baseUrl,new LinkedHashSet());
        }

	/**
	 * Get the URLs for all the resources that a browser would automatically
	 * download following the download of the HTML content, that is: images,
	 * stylesheets, javascript files, applets, etc...
	 * <p>
	 * All URLs should be added to the Collection.
	 * <p>
	 * Malformed URLs can be reported to the caller by having the Iterator
	 * return the corresponding RL String. Overall problems parsing the html
	 * should be reported by throwing an HTMLParseException. 
	 * 
	 * @param html HTML code
	 * @param baseUrl Base URL from which the HTML code was obtained
	 * @param coll Collection
	 * @return an Iterator for the resource URLs 
	 */
	public abstract Iterator getEmbeddedResourceURLs(byte[] html, URL baseUrl,
	                                                  Collection coll)
		throws HTMLParseException;

    public static class HTMLParserTest extends TestCase
    {
		private static final String TESTFILE1= "testfiles/HTMLParserTestCase.html";
        private static final String TESTFILE2= "testfiles/HTMLParserTestCaseWithBaseHRef.html";
		private static final String URL1 = "http://myhost/mydir/myfile.html";
		private static final String[] EXPECTED_RESULT1 = new String[] {
			"http://myhost/mydir/images/image-a.gif",
			"http://myhost/mydir/images/image-b.gif",
			"http://myhost/mydir/images/image-c.gif",
			"http://myhost/mydir/images/image-d.gif",
			"http://myhost/mydir/images/image-e.gif",
			"http://myhost/mydir/images/image-f.gif",
			"http://myhost/mydir/images/image-a2.gif",
			"http://myhost/mydir/images/image-b2.gif",
			"http://myhost/mydir/images/image-c2.gif",
			"http://myhost/mydir/images/image-d2.gif",
			"http://myhost/mydir/images/image-e2.gif",
			"http://myhost/mydir/images/image-f2.gif",
		};
		
		private static final String[] EXPECTED_RESULT1A = new String[] {
			"http://myhost/mydir/images/image-a.gif",
			"http://myhost/mydir/images/image-b.gif",
			"http://myhost/mydir/images/image-b.gif",
			"http://myhost/mydir/images/image-c.gif",
			"http://myhost/mydir/images/image-d.gif",
			"http://myhost/mydir/images/image-e.gif",
			"http://myhost/mydir/images/image-f.gif",
			"http://myhost/mydir/images/image-a2.gif",
			"http://myhost/mydir/images/image-b2.gif",
			"http://myhost/mydir/images/image-c2.gif",
			"http://myhost/mydir/images/image-d2.gif",
			"http://myhost/mydir/images/image-d2.gif",
			"http://myhost/mydir/images/image-e2.gif",
			"http://myhost/mydir/images/image-f2.gif",
		};
        public HTMLParserTest() {
            super();
        }
        // Test if can instantiate parser using property name
        public static void testParser(String s) throws Exception
        {
			Properties p = JMeterUtils.getJMeterProperties();
			if (p == null){
				p=JMeterUtils.getProperties("jmeter.properties");
			}
			p.setProperty(PARSER_CLASSNAME,s);
        	testParser(getParser());
			testParser(getParser());// check re-usability
        }
        
        //TODO - this test won't work for non-reusable parsers
        public static void testParser(HTMLParser p) throws Exception
        {
        	filetest(p,TESTFILE1,URL1,EXPECTED_RESULT1,null);
			filetest(p,TESTFILE1,URL1,EXPECTED_RESULT1,null);// See if reusable
			filetest(p,TESTFILE1,URL1,EXPECTED_RESULT1A,new Vector());
			filetest(p,TESTFILE1,URL1,EXPECTED_RESULT1A,new Vector());
            // Test for BASE HREF support:
            filetest(p,TESTFILE2,URL1,EXPECTED_RESULT1,null);
            filetest(p,TESTFILE2,URL1,EXPECTED_RESULT1A,new Vector());
        }

		private static void filetest(HTMLParser p,String file, String url, String[] expected_result,
		              Collection c) throws Exception
		{
			log.info("file   "+file);
			log.info("parser "+p.getClass().getName());
			File f= new File(file);
			byte[] buffer= new byte[(int)f.length()];
			int len= new FileInputStream(f).read(buffer);
			assertEquals(len, buffer.length);
			Iterator result;
			if (c == null) {
				result = p.getEmbeddedResourceURLs(buffer,new URL(url));
			} else {
			    result = p.getEmbeddedResourceURLs(buffer,new URL(url),c);
			}
			Iterator expected= Arrays.asList(expected_result).iterator();
			while (expected.hasNext()) {
				assertTrue(result.hasNext());
				assertEquals(expected.next(), result.next().toString());
			}
			assertFalse(result.hasNext());
		}

        public void testDefaultParser() throws Exception {
            testParser(getParser());
        }
    }
}