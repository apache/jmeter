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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.jmeter.junit.JMeterTestCase;
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

    // Cache of parsers - parsers must be re-usable
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

		try
        {
        	Object clazz = Class.forName(htmlParserClassName).newInstance();
        	if (clazz instanceof HTMLParser){
				pars = (HTMLParser) clazz;
        	} else {
        		throw new Error(new ClassCastException(htmlParserClassName));
        	}
        }
        catch (InstantiationException e)
        {
			throw new Error(e);
        }
        catch (IllegalAccessException e)
        {
			throw new Error(e);
        }
        catch (ClassNotFoundException e)
        {
			throw new Error(e);
        }
		log.info("Created "+htmlParserClassName);
		if (pars.isReusable()){
			parsers.put(htmlParserClassName,pars);// cache the parser
		}
		
    	return pars;
    }

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


    /**
     * Parsers should over-ride this method if the parser class is re-usable,
     * in which case the class will be cached for the next getParser() call.
     * 
     * @return true if the Parser is reusable
     */
    protected boolean isReusable()
    {
    	return false;
    }

//////////////////////////// TEST CODE FOLLOWS /////////////////////////////


    public static class HTMLParserTest extends JMeterTestCase
    {
		private String parserName;
        private int testNumber=0;

        public HTMLParserTest(String name) {
			super(name);
		}

		public HTMLParserTest(String name, int test) {
			super(name);
			testNumber = test;
		}

		public HTMLParserTest(String name, String parser, int test) {
			super(name);
			testNumber = test;
			parserName = parser;
		}


		private class TestClass //Can't instantiate
		{
    	    private TestClass(){};	 
		}

    	private static class TestData
    	{
    		private String fileName;
			private String baseURL;
			private String expectedSet;
			private String expectedList;

			private TestData(String f, String b, String s, String l){
				fileName = f;
				baseURL  = b;
				expectedSet = s;
				expectedList = l;
			}

			private TestData(String f, String b, String s){
				this(f,b,s,null);
			}
    	}

        // List of parsers to test. Should probably be derived automatically
        private static final String []  PARSERS = {
			"org.apache.jmeter.protocol.http.parser.HtmlParserHTMLParser",
			"org.apache.jmeter.protocol.http.parser.JTidyHTMLParser",
			"org.apache.jmeter.protocol.http.parser.RegexpHTMLParser"
        };
        private static final TestData[] TESTS = new TestData[]{
        	new TestData(
        	             "testfiles/HTMLParserTestCase.html",
			             "http://myhost/mydir/myfile.html",
			             "testfiles/HTMLParserTestCase.set",
			              "testfiles/HTMLParserTestCase.all"
        	             ),
			new TestData(
			             "testfiles/HTMLParserTestCaseWithBaseHRef.html",
						 "http://myhost/mydir/myfile.html",
						 "testfiles/HTMLParserTestCase.set",
						  "testfiles/HTMLParserTestCase.all"
						 ),
			new TestData(
						 "testfiles/HTMLParserTestCase2.html",
						 "http:", //Dummy, as the file has no entries
						 "",
						 ""
						 ),
			new TestData(
						 "testfiles/HTMLParserTestCase3.html",
						 "http:", //Dummy, as the file has no entries
						 "",
						 ""
						 ),
        };

        public static Test suite(){
        	TestSuite suite = new TestSuite();
        	suite.addTest(new HTMLParserTest("testDefaultParser"));
			suite.addTest(new HTMLParserTest("testParserDefault"));
			suite.addTest(new HTMLParserTest("testParserMissing"));
			suite.addTest(new HTMLParserTest("testNotParser"));
			suite.addTest(new HTMLParserTest("testNotCreatable"));
			for (int i = 0;i<PARSERS.length;i++){
				TestSuite ps = new TestSuite(PARSERS[i]);// Identify the subtests
				ps.addTest(new HTMLParserTest("testParserProperty",PARSERS[i],0));
				for (int j=0;j<TESTS.length;j++){
					TestSuite ts = new TestSuite(TESTS[j].fileName);
					ts.addTest(new HTMLParserTest("testParserSet",PARSERS[i],j));
					ts.addTest(new HTMLParserTest("testParserList",PARSERS[i],j));
					ps.addTest(ts);
				}
				suite.addTest(ps);
			}
        	return suite;
        }
        
        // Test if can instantiate parser using property name
        public void testParserProperty() throws Exception
        {
			Properties p = JMeterUtils.getJMeterProperties();
			if (p == null){
				p=JMeterUtils.getProperties("jmeter.properties");
			}
			p.setProperty(PARSER_CLASSNAME,parserName);
			getParser();
        }
        
		public void testDefaultParser() throws Exception {
			getParser(); 
		}

		public void testParserDefault() throws Exception {
			getParser(DEFAULT_PARSER); 
		}

		public void testParserMissing() throws Exception {
			try{
			    getParser("no.such.parser");
			}
			catch (Error e)
			{
				if (e.getCause() instanceof ClassNotFoundException)
				{
					 //	This is OK
				}
				else
				{
					throw e;
				}
			}
		}

		public void testNotParser() throws Exception {
			try{
                getParser("java.lang.String");
			}
			catch (Error e)
			{
				if (e.getCause() instanceof ClassCastException) return;
				throw e;
			}
		}

		public void testNotCreatable() throws Exception {
			try
			{
				getParser(TestClass.class.getName());
			}
			catch (Error e)
			{
				if (e.getCause() instanceof InstantiationException) return;
				throw e;
			}
		}

        public void testParserSet() throws Exception
        {
			HTMLParser p = getParser(parserName);
        	filetest(p,TESTS[testNumber].fileName,TESTS[testNumber].baseURL,TESTS[testNumber].expectedSet,null);
        }

		public void testParserList() throws Exception
		{
			HTMLParser p = getParser(parserName);
			filetest(p,TESTS[testNumber].fileName,TESTS[testNumber].baseURL,TESTS[testNumber].expectedList,new Vector());
		}

		private static void filetest(HTMLParser p,
		                               String file,
		                               String url,
		                               String resultFile,
		                               Collection c)
		throws Exception
		{
			log.info("file   "+file);
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
			Iterator expected= getFile(resultFile).iterator();
			while (expected.hasNext()) {
				assertTrue(result.hasNext());
				assertEquals(expected.next(), result.next().toString());
			}
			assertFalse(result.hasNext());
		}

        // Get expected results as a List
		private static List getFile(String file)
		    throws Exception
		{
			ArrayList al = new ArrayList();
			if (file != null && file.length() > 0){
			  BufferedReader br = 
			    new BufferedReader(
			        new FileReader(new File(file)));
			  String line = br.readLine();
			  while (line != null){
				al.add(line);
				line = br.readLine();
			  }
			  br.close();
			}
			return al;
		}
    }
}