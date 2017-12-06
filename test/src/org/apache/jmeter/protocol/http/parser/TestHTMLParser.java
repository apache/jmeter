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

package org.apache.jmeter.protocol.http.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.TreeSet;
import java.util.Vector;

import org.apache.commons.io.IOUtils;
import org.apache.jmeter.junit.JMeterTestCaseJUnit;
import org.apache.jmeter.util.JMeterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import junit.framework.TestSuite;

public class TestHTMLParser extends JMeterTestCaseJUnit {
    private static final Logger log = LoggerFactory.getLogger(TestHTMLParser.class);

    private static final String DEFAULT_UA  = "Apache-HttpClient/4.2.6";
    private static final String UA_FF       = "Mozilla/5.0 (Windows NT 5.1; rv:31.0) Gecko/20100101 Firefox/31.0";
    private static final String UA_IE55     = "Mozilla/4.0 (compatible;MSIE 5.5; Windows 98)";
    private static final String UA_IE6      = "Mozilla/5.0 (Windows; U; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 2.0.50727)";
    private static final String UA_IE7      = "Mozilla/5.0 (Windows; U; MSIE 7.0; Windows NT 6.0; en-US)";
    private static final String UA_IE8      = "Mozilla/5.0 (compatible; MSIE 8.0; Windows NT 6.1; Trident/4.0; GTB7.4; InfoPath.2; SV1; .NET CLR 3.3.69573; WOW64; en-US)";
    private static final String UA_IE9      = "Mozilla/5.0 (Windows; U; MSIE 9.0; WIndows NT 9.0; en-US))";
    private static final String UA_IE10     = "Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.1; Trident/6.0)";

    public TestHTMLParser(String arg0) {
        super(arg0);
    }
        private String parserName;

        private int testNumber = 0;


        public TestHTMLParser(String name, int test) {
            super(name);
            testNumber = test;
        }

        public TestHTMLParser(String name, String parser, int test) {
            super(name);
            testNumber = test;
            parserName = parser;
        }

        private static class StaticTestClass // Can't instantiate
        {
            private StaticTestClass() {
            }
        }

        private class TestClass // Can't instantiate
        {
            private TestClass() {
            }
        }

        private static class TestData {
            private String fileName;

            private String baseURL;

            private String expectedSet;

            private String expectedList;

            public String userAgent;

            /**
             * 
             * @param htmlFileName HTML File with content
             * @param baseUrl Base URL
             * @param expectedSet Set of expected URLs
             * @param expectedList List of expected URLs
             * @param userAgent User Agent
             */
            private TestData(String htmlFileName, String baseUrl, String expectedSet, String expectedList) {
                this(htmlFileName, baseUrl, expectedList, expectedList, DEFAULT_UA);
            }
            /**
             * 
             * @param htmlFileName HTML File with content
             * @param baseUrl Base URL
             * @param expectedSet Set of expected URLs
             * @param expectedList List of expected URLs
             * @param userAgent User Agent
             */
            private TestData(String htmlFileName, String baseUrl, String expectedSet, String expectedList, String userAgent) {
                this.fileName = htmlFileName;
                this.baseURL = baseUrl;
                this.expectedSet = expectedSet;
                this.expectedList = expectedList;
                this.userAgent = userAgent;
            }

        }
        
        private static final String DEFAULT_JMETER_PARSER = 
                "org.apache.jmeter.protocol.http.parser.LagartoBasedHtmlParser";
        
        // List of parsers to test. Should probably be derived automatically
        private static final String[] PARSERS = { 
            "org.apache.jmeter.protocol.http.parser.JTidyHTMLParser",
            "org.apache.jmeter.protocol.http.parser.RegexpHTMLParser",
            DEFAULT_JMETER_PARSER,
            "org.apache.jmeter.protocol.http.parser.JsoupBasedHtmlParser"
            };

        private static final TestData[] TESTS = new TestData[] {
                new TestData("testfiles/HTMLParserTestCase.html",
                        "http://localhost/mydir/myfile.html",
                        "testfiles/HTMLParserTestCase.set",
                        "testfiles/HTMLParserTestCase.all"),
                new TestData("testfiles/HTMLParserTestCaseWithBaseHRef.html", 
                        "http://localhost/mydir/myfile.html",
                        "testfiles/HTMLParserTestCaseBase.set", 
                        "testfiles/HTMLParserTestCaseBase.all"),
                new TestData("testfiles/HTMLParserTestCaseWithBaseHRef2.html", 
                        "http://localhost/mydir/myfile.html",
                         "testfiles/HTMLParserTestCaseBase.set", 
                         "testfiles/HTMLParserTestCaseBase.all"),
                new TestData("testfiles/HTMLParserTestCaseWithMissingBaseHRef.html",
                        "http://localhost/mydir/images/myfile.html", 
                        "testfiles/HTMLParserTestCaseBase.set",
                        "testfiles/HTMLParserTestCaseBase.all"),
                new TestData("testfiles/HTMLParserTestCase2.html",
                        "http:", "", ""), // Dummy as the file has no entries
                new TestData("testfiles/HTMLParserTestCase3.html",
                        "http:", "", ""), // Dummy as the file has no entries
                new TestData("testfiles/HTMLParserTestCaseWithComments.html",
                        "http://localhost/mydir/myfile.html",
                        "testfiles/HTMLParserTestCaseBase.set",
                        "testfiles/HTMLParserTestCaseBase.all"),
                new TestData("testfiles/HTMLScript.html",
                        "http://localhost/",
                        "testfiles/HTMLScript.set",
                        "testfiles/HTMLScript.all"),
                new TestData("testfiles/HTMLParserTestFrames.html",
                        "http://localhost/",
                        "testfiles/HTMLParserTestFrames.all",
                        "testfiles/HTMLParserTestFrames.all"), 
                // Relative filenames
                new TestData("testfiles/HTMLParserTestFile_2.html",
                        "file:HTMLParserTestFile_2.html",
                        "testfiles/HTMLParserTestFile_2.all",
                        "testfiles/HTMLParserTestFile_2.all"),
                         };


        private static final TestData[] SPECIFIC_PARSER_TESTS = new TestData[] {
            new TestData("testfiles/HTMLParserTestCaseWithConditional1.html",
                    "http://localhost/mydir/myfile.html",
                    null,
                    "testfiles/HTMLParserTestCaseWithConditional1_FF.all",
                    UA_FF),
            new TestData("testfiles/HTMLParserTestCaseWithConditional1.html",
                    "http://localhost/mydir/myfile.html",
                    null,
                    "testfiles/HTMLParserTestCaseWithConditional1_IE6.all",
                    UA_IE6),
            new TestData("testfiles/HTMLParserTestCaseWithConditional1.html",
                    "http://localhost/mydir/myfile.html",
                    null,
                    "testfiles/HTMLParserTestCaseWithConditional1_IE7.all",
                    UA_IE7),
            new TestData("testfiles/HTMLParserTestCaseWithConditional1.html",
                    "http://localhost/mydir/myfile.html",
                    null,
                    "testfiles/HTMLParserTestCaseWithConditional1_IE8.all",
                    UA_IE8),
            new TestData("testfiles/HTMLParserTestCaseWithConditional1.html",
                    "http://localhost/mydir/myfile.html",
                    null,
                    "testfiles/HTMLParserTestCaseWithConditional1_IE8.all",
                    UA_IE8),

            // FF gets mixed up by nested comments
            new TestData("testfiles/HTMLParserTestCaseWithConditional2.html",
                    "http://localhost/mydir/myfile.html",
                    null,
                    "testfiles/HTMLParserTestCaseWithConditional2_FF.all",
                    UA_FF),

            new TestData("testfiles/HTMLParserTestCaseWithConditional2.html",
                    "http://localhost/mydir/myfile.html",
                    null,
                    "testfiles/HTMLParserTestCaseWithConditional2_IE7.all",
                    UA_IE7),
            new TestData("testfiles/HTMLParserTestCaseWithConditional2.html",
                    "http://localhost/mydir/myfile.html",
                    null,
                    "testfiles/HTMLParserTestCaseWithConditional2_IE8.all",
                    UA_IE8),
            new TestData("testfiles/HTMLParserTestCaseWithConditional2.html",
                    "http://localhost/mydir/myfile.html",
                    null,
                    "testfiles/HTMLParserTestCaseWithConditional2_IE9.all",
                    UA_IE9),
            new TestData("testfiles/HTMLParserTestCaseWithConditional3.html",
                    "http://localhost/mydir/myfile.html",
                    null,
                    "testfiles/HTMLParserTestCaseWithConditional3_FF.all",
                    UA_FF),
            new TestData("testfiles/HTMLParserTestCaseWithConditional3.html",
                    "http://localhost/mydir/myfile.html",
                    null,
                    "testfiles/HTMLParserTestCaseWithConditional3_IE10.all",
                    UA_IE10),
            new TestData("testfiles/HTMLParserTestCaseWithConditional3.html",
                    "http://localhost/mydir/myfile.html",
                    null,
                    "testfiles/HTMLParserTestCaseWithConditional3_IE55.all",
                    UA_IE55),
            new TestData("testfiles/HTMLParserTestCaseWithConditional3.html",
                    "http://localhost/mydir/myfile.html",
                    null,
                    "testfiles/HTMLParserTestCaseWithConditional3_IE6.all",
                    UA_IE6)        
        };   

        public static junit.framework.Test suite() {
            TestSuite suite = new TestSuite("TestHTMLParser");
            suite.addTest(new TestHTMLParser("testDefaultParser"));
            suite.addTest(new TestHTMLParser("testParserDefault"));
            suite.addTest(new TestHTMLParser("testParserMissing"));
            suite.addTest(new TestHTMLParser("testNotParser"));
            suite.addTest(new TestHTMLParser("testNotCreatable"));
            suite.addTest(new TestHTMLParser("testNotCreatableStatic"));
            for (String parser : PARSERS) {
                TestSuite ps = new TestSuite(parser);// Identify subtests
                ps.addTest(new TestHTMLParser("testParserProperty", parser, 0));
                for (int j = 0; j < TESTS.length; j++) {
                    TestSuite ts = new TestSuite(TESTS[j].fileName);
                    ts.addTest(new TestHTMLParser("testParserSet", parser, j));
                    ts.addTest(new TestHTMLParser("testParserList", parser, j));
                    ps.addTest(ts);
                }
                suite.addTest(ps);
            }
            
            TestSuite ps = new TestSuite(DEFAULT_JMETER_PARSER+"_conditional_comments");// Identify subtests
            for (int j = 0; j < SPECIFIC_PARSER_TESTS.length; j++) {
                TestSuite ts = new TestSuite(SPECIFIC_PARSER_TESTS[j].fileName);
                ts.addTest(new TestHTMLParser("testSpecificParserList", DEFAULT_JMETER_PARSER, j));
                ps.addTest(ts);
            }
            suite.addTest(ps);
            return suite;
        }

        // Test if can instantiate parser using property name
        public void testParserProperty() throws Exception {
            Properties p = JMeterUtils.getJMeterProperties();
            if (p == null) {
                p = JMeterUtils.getProperties("jmeter.properties");
            }
            p.setProperty(HTMLParser.PARSER_CLASSNAME, parserName);
            BaseParser.getParser(p.getProperty(HTMLParser.PARSER_CLASSNAME));
        }

        public void testDefaultParser() throws Exception {
            BaseParser.getParser(JMeterUtils.getPropDefault(HTMLParser.PARSER_CLASSNAME, HTMLParser.DEFAULT_PARSER));
        }

        public void testParserDefault() throws Exception {
            BaseParser.getParser(HTMLParser.DEFAULT_PARSER);
        }

        public void testParserMissing() throws Exception {
            try {
                BaseParser.getParser("no.such.parser");
                fail("Should not have been able to create the parser");
            } catch (LinkExtractorParseException e) {
                if (!(e.getCause() instanceof ClassNotFoundException)) {
                    throw e;
                }
            }
        }

        public void testNotParser() throws Exception {
            try {
                HTMLParser.getParser("java.lang.String");
                fail("Should not have been able to create the parser");
            } catch (LinkExtractorParseException e) {
                if (e.getCause() instanceof ClassCastException) {
                    return;
                }
                throw e;
            }
        }

        public void testNotCreatable() throws Exception {
            try {
                HTMLParser.getParser(TestClass.class.getName());
                fail("Should not have been able to create the parser");
            } catch (LinkExtractorParseException e) {
                if (e.getCause() instanceof InstantiationException) {
                    return;
                }
                throw e;
            }
        }

        public void testNotCreatableStatic() throws Exception {
            try {
                HTMLParser.getParser(StaticTestClass.class.getName());
                fail("Should not have been able to create the parser");
            } catch (LinkExtractorParseException e) {
                if (e.getCause() instanceof ClassCastException) {
                    return;
                }
                if (e.getCause() instanceof IllegalAccessException) {
                    return;
                }
                throw e;
            }
        }

        public void testParserSet() throws Exception {
            HTMLParser p = (HTMLParser) BaseParser.getParser(parserName);
            filetest(p, TESTS[testNumber].fileName, TESTS[testNumber].baseURL, TESTS[testNumber].expectedSet, null,
                    false, TESTS[testNumber].userAgent);
        }

        public void testParserList() throws Exception {
            HTMLParser p = (HTMLParser) BaseParser.getParser(parserName);
            filetest(p, TESTS[testNumber].fileName, TESTS[testNumber].baseURL, TESTS[testNumber].expectedList,
                    new Vector<URLString>(), true, TESTS[testNumber].userAgent);
        }
        
        public void testSpecificParserList() throws Exception {
            HTMLParser p = (HTMLParser) BaseParser.getParser(parserName);
            filetest(p, SPECIFIC_PARSER_TESTS[testNumber].fileName, SPECIFIC_PARSER_TESTS[testNumber].baseURL, SPECIFIC_PARSER_TESTS[testNumber].expectedList,
                    new ArrayList<URLString>(), true, SPECIFIC_PARSER_TESTS[testNumber].userAgent);
        }


        private static void filetest(HTMLParser p, String file, String url, String resultFile, Collection<URLString> c,
                boolean orderMatters, // Does the order matter?
                String userAgent)
                throws Exception {
            String parserName = p.getClass().getName().substring("org.apache.jmeter.protocol.http.parser.".length());
            String fname = file.substring(file.indexOf('/')+1);
            log.debug("file   {}", file);
            File f = findTestFile(file);
            byte[] buffer = new byte[(int) f.length()];
            InputStream is = null;
            try {
                is = new FileInputStream(f);
                int len = is.read(buffer);
                assertEquals(len, buffer.length);
            } finally {
                IOUtils.closeQuietly(is);
            }
            Iterator<URL> result;
            if (c == null) {
                result = p.getEmbeddedResourceURLs(userAgent, buffer, new URL(url), System.getProperty("file.encoding"));
            } else {
                result = p.getEmbeddedResourceURLs(userAgent, buffer, new URL(url), c,System.getProperty("file.encoding"));
            }
            /*
             * TODO: Exact ordering is only required for some tests; change the
             * comparison to do a set compare where necessary.
             */
            Iterator<String> expected;
            if (orderMatters) {
                expected = getFile(resultFile).iterator();
            } else {
                // Convert both to Sets
                expected = new TreeSet<>(getFile(resultFile)).iterator();
                TreeSet<URL> temp = new TreeSet<>(new Comparator<Object>() {
                    @Override
                    public int compare(Object o1, Object o2) {
                        return (o1.toString().compareTo(o2.toString()));
                    }
                });
                while (result.hasNext()) {
                    temp.add(result.next());
                }
                result = temp.iterator();
            }

            while (expected.hasNext()) {
                Object next = expected.next();
                assertTrue(userAgent+"::"+fname+"::"+parserName + "::Expecting another result " + next, result.hasNext());
                try {
                    assertEquals(userAgent+"::"+fname+"::"+parserName + "(next)", next, result.next().toString());
                } catch (ClassCastException e) {
                    fail(userAgent+"::"+fname+"::"+parserName + "::Expected URL, but got " + e.toString());
                }
            }
            assertFalse(userAgent+"::"+fname+"::"+parserName + "::Should have reached the end of the results", result.hasNext());
        }

        // Get expected results as a List
        private static List<String> getFile(String file) throws Exception {
            ArrayList<String> al = new ArrayList<>();
            if (file != null && file.length() > 0) {
                BufferedReader br = new BufferedReader(new FileReader(findTestFile(file)));
                String line = br.readLine();
                while (line != null) {
                    al.add(line);
                    line = br.readLine();
                }
                br.close();
            }
            return al;
        }
}
