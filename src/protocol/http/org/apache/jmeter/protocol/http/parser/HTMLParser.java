// $Header$
/*
 * Copyright 2003-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

package org.apache.jmeter.protocol.http.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.TreeSet;
import java.util.Vector;

import junit.framework.TestSuite;

import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * HtmlParsers can parse HTML content to obtain URLs.
 * 
 * @author <a href="mailto:jsalvata@apache.org">Jordi Salvat i Alabart</a>
 * @version $Revision$ updated on $Date$
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
			log.debug("Fetched "+htmlParserClassName);
			return pars;
		}

		try
        {
        	Object clazz = Class.forName(htmlParserClassName).newInstance();
        	if (clazz instanceof HTMLParser){
				pars = (HTMLParser) clazz;
        	} else {
        		throw new HTMLParseError(new ClassCastException(htmlParserClassName));
        	}
        }
        catch (InstantiationException e)
        {
			throw new HTMLParseError(e);
        }
        catch (IllegalAccessException e)
        {
			throw new HTMLParseError(e);
        }
        catch (ClassNotFoundException e)
        {
			throw new HTMLParseError(e);
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
			
			Collection col;
			
			// N.B. LinkedHashSet is Java 1.4
			if (hasLinkedHashSet){
				try {
					col = (Collection) Class.forName("java.util.LinkedHashSet").newInstance();
				} catch (Exception e) {
					throw new Error("Should not happen:"+e.toString());
				}
			} else {
				col = new java.util.HashSet(); //TODO: improve JDK1.3 solution 
			}
        	
			return getEmbeddedResourceURLs(html, baseUrl,new URLCollection(col));
            
            // An additional note on using HashSets to store URLs: I just
            // discovered that obtaining the hashCode of a java.net.URL implies
            // a domain-name resolution process. This means significant delays
            // can occur, even more so if the domain name is not resolvable.
            // Whether this can be a problem in practical situations I can't tell, but
            // thought I'd keep a note just in case...
            // BTW, note that using a Vector and removing duplicates via scan
            // would not help, since URL.equals requires name resolution too.
            // The above problem has now been addressed with the URLString and
            // URLCollection classes.

        }
        
        // See whether we can use LinkedHashSet or not:
        private static final boolean hasLinkedHashSet;
        static {
        	boolean b;
			try
            {
                Class.forName("java.util.LinkedHashSet");
                b = true;
            }
            catch (ClassNotFoundException e)
            {
            	b = false;
            }
            hasLinkedHashSet = b;
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
	 * N.B.
	 * The Iterator returns URLs, but the Collection will contain
	 * objects of class URLString.
	 * 
	 * @param html HTML code
	 * @param baseUrl Base URL from which the HTML code was obtained
	 * @param coll URLCollection
	 * @return an Iterator for the resource URLs 
	 */
	public abstract Iterator getEmbeddedResourceURLs(byte[] html, URL baseUrl,
	                                                  URLCollection coll)
		throws HTMLParseException;


	/**
	 * Get the URLs for all the resources that a browser would automatically
	 * download following the download of the HTML content, that is: images,
	 * stylesheets, javascript files, applets, etc...
	 * 
	 * N.B.
	 * The Iterator returns URLs, but the Collection will contain
	 * objects of class URLString.
	 * 
	 * @param html HTML code
	 * @param baseUrl Base URL from which the HTML code was obtained
	 * @param coll Collection - will contain URLString objects, not URLs
	 * @return an Iterator for the resource URLs 
	 */
	public Iterator getEmbeddedResourceURLs(byte[] html, URL baseUrl,
													  Collection coll)
		throws HTMLParseException
		{
			return getEmbeddedResourceURLs(html,baseUrl, new URLCollection(coll));
		}


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


    public static class Test extends JMeterTestCase
    {
		private String parserName;
        private int testNumber=0;

		public Test() {
			super();
		}

        public Test(String name) {
			super(name);
		}

		public Test(String name, int test) {
			super(name);
			testNumber = test;
		}

		public Test(String name, String parser, int test) {
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
			             "http://localhost/mydir/myfile.html",
			             "testfiles/HTMLParserTestCase.set",
			              "testfiles/HTMLParserTestCase.all"
        	             ),
			new TestData(
			             "testfiles/HTMLParserTestCaseWithBaseHRef.html",
						 "http://localhost/mydir/myfile.html",
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
            new TestData(
                         "testfiles/HTMLParserTestCaseWithComments.html",
                         "http://localhost/mydir/myfile.html",
                         "testfiles/HTMLParserTestCase.set",
                         "testfiles/HTMLParserTestCase.all"
                         ),
            new TestData(
                     "testfiles/HTMLScript.html",
                     "http://localhost/",
                     "testfiles/HTMLScript.set",
                     "testfiles/HTMLScript.all"
                     ),
             new TestData(
		                 "testfiles/HTMLParserTestFrames.html",
		                 "http://localhost/",
		                 "testfiles/HTMLParserTestFrames.all",
		                 "testfiles/HTMLParserTestFrames.all"
		                 ),
        };

        public static junit.framework.Test suite(){
        	TestSuite suite = new TestSuite();
        	suite.addTest(new Test("testDefaultParser"));
			suite.addTest(new Test("testParserDefault"));
			suite.addTest(new Test("testParserMissing"));
			suite.addTest(new Test("testNotParser"));
			suite.addTest(new Test("testNotCreatable"));
			for (int i = 0;i<PARSERS.length;i++){
				TestSuite ps = new TestSuite(PARSERS[i]);// Identify the subtests
				ps.addTest(new Test("testParserProperty",PARSERS[i],0));
				for (int j=0;j<TESTS.length;j++){
					TestSuite ts = new TestSuite(TESTS[j].fileName);
					ts.addTest(new Test("testParserSet",PARSERS[i],j));
					ts.addTest(new Test("testParserList",PARSERS[i],j));
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
			catch (HTMLParseError e)
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
			catch (HTMLParseError e)
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
			catch (HTMLParseError e)
			{
				if (e.getCause() instanceof InstantiationException) return;
				throw e;
			}
		}

        public void testParserSet() throws Exception
        {
			HTMLParser p = getParser(parserName);
        	filetest(p,TESTS[testNumber].fileName,TESTS[testNumber].baseURL,TESTS[testNumber].expectedSet
        	        ,null,false);
        }

		public void testParserList() throws Exception
		{
			HTMLParser p = getParser(parserName);
			filetest(p,TESTS[testNumber].fileName,TESTS[testNumber].baseURL,TESTS[testNumber].expectedList
			        ,new Vector(),true);
		}

		private static void filetest(HTMLParser p,
		                               String file,
		                               String url,
		                               String resultFile,
		                               Collection c,
		                               boolean orderMatters) //Does the order matter?
		throws Exception
		{
			log.debug("file   "+file);
			File f= findTestFile(file);
			byte[] buffer= new byte[(int)f.length()];
			int len= new FileInputStream(f).read(buffer);
			assertEquals(len, buffer.length);
			Iterator result;
			if (c == null) {
				result = p.getEmbeddedResourceURLs(buffer,new URL(url));
			} else {
			    result = p.getEmbeddedResourceURLs(buffer,new URL(url),c);
			}
			/* 
			 * TODO:
			 * Exact ordering is only required for some tests;
			 * change the comparison to do a set compare where
			 * necessary.
			 */
			Iterator expected;
			if (orderMatters) {
			 	expected= getFile(resultFile).iterator();
			} else {
				// Convert both to Sets
				expected = new TreeSet(getFile(resultFile)).iterator();
				TreeSet temp = new TreeSet(new Comparator(){
                    public int compare(Object o1, Object o2)
                    {
                    	return (o1.toString().compareTo(o2.toString()));
                    }});
				while (result.hasNext()){
					temp.add(result.next());
				}
				result=temp.iterator();
			}
			
			while (expected.hasNext()) {
				assertTrue("Expecting another result",result.hasNext());
                try
                {
                    assertEquals(expected.next(),((URL) result.next()).toString());
                }
                catch (ClassCastException e)
                {
                	fail("Expected URL, but got "+e.toString());
                }
			}
			assertFalse("Should have reached the end of the results",result.hasNext());
		}

        // Get expected results as a List
		private static List getFile(String file)
		    throws Exception
		{
			ArrayList al = new ArrayList();
			if (file != null && file.length() > 0){
			  BufferedReader br = 
			    new BufferedReader(
			        new FileReader(findTestFile(file)));
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