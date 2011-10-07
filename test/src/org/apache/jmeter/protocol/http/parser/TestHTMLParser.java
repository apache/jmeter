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
import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import junit.framework.TestSuite;
import junit.textui.TestRunner;

public class TestHTMLParser extends JMeterTestCase {
    private static final Logger log = LoggingManager.getLoggerForClass();

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

            private TestData(String f, String b, String s, String l) {
                fileName = f;
                baseURL = b;
                expectedSet = s;
                expectedList = l;
            }

//            private TestData(String f, String b, String s) {
//                this(f, b, s, null);
//            }
        }

        // List of parsers to test. Should probably be derived automatically
        private static final String[] PARSERS = { 
            "org.apache.jmeter.protocol.http.parser.HtmlParserHTMLParser",
            "org.apache.jmeter.protocol.http.parser.JTidyHTMLParser",
            "org.apache.jmeter.protocol.http.parser.RegexpHTMLParser" 
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

        public static junit.framework.Test suite() {
            TestSuite suite = new TestSuite("TestHTMLParser");
            suite.addTest(new TestHTMLParser("testDefaultParser"));
            suite.addTest(new TestHTMLParser("testParserDefault"));
            suite.addTest(new TestHTMLParser("testParserMissing"));
            suite.addTest(new TestHTMLParser("testNotParser"));
            suite.addTest(new TestHTMLParser("testNotCreatable"));
            suite.addTest(new TestHTMLParser("testNotCreatableStatic"));
            for (int i = 0; i < PARSERS.length; i++) {
                TestSuite ps = new TestSuite(PARSERS[i]);// Identify subtests
                ps.addTest(new TestHTMLParser("testParserProperty", PARSERS[i], 0));
                for (int j = 0; j < TESTS.length; j++) {
                    TestSuite ts = new TestSuite(TESTS[j].fileName);
                    ts.addTest(new TestHTMLParser("testParserSet", PARSERS[i], j));
                    ts.addTest(new TestHTMLParser("testParserList", PARSERS[i], j));
                    ps.addTest(ts);
                }
                suite.addTest(ps);
            }
            return suite;
        }

        // Test if can instantiate parser using property name
        public void testParserProperty() throws Exception {
            Properties p = JMeterUtils.getJMeterProperties();
            if (p == null) {
                p = JMeterUtils.getProperties("jmeter.properties");
            }
            p.setProperty(HTMLParser.PARSER_CLASSNAME, parserName);
            HTMLParser.getParser();
        }

        public void testDefaultParser() throws Exception {
            HTMLParser.getParser();
        }

        public void testParserDefault() throws Exception {
            HTMLParser.getParser(HTMLParser.DEFAULT_PARSER);
        }

        public void testParserMissing() throws Exception {
            try {
                HTMLParser.getParser("no.such.parser");
                fail("Should not have been able to create the parser");
            } catch (HTMLParseError e) {
                if (e.getCause() instanceof ClassNotFoundException) {
                    // This is OK
                } else {
                    throw e;
                }
            }
        }

        public void testNotParser() throws Exception {
            try {
                HTMLParser.getParser("java.lang.String");
                fail("Should not have been able to create the parser");
            } catch (HTMLParseError e) {
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
            } catch (HTMLParseError e) {
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
            } catch (HTMLParseError e) {
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
            HTMLParser p = HTMLParser.getParser(parserName);
            filetest(p, TESTS[testNumber].fileName, TESTS[testNumber].baseURL, TESTS[testNumber].expectedSet, null,
                    false);
        }

        public void testParserList() throws Exception {
            HTMLParser p = HTMLParser.getParser(parserName);
            filetest(p, TESTS[testNumber].fileName, TESTS[testNumber].baseURL, TESTS[testNumber].expectedList,
                    new Vector<URLString>(), true);
        }

        private static void filetest(HTMLParser p, String file, String url, String resultFile, Collection<URLString> c,
                boolean orderMatters) // Does the order matter?
                throws Exception {
            String parserName = p.getClass().getName().substring("org.apache.jmeter.protocol.http.parser.".length());
            String fname = file.substring(file.indexOf("/")+1);
            log.debug("file   " + file);
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
                result = p.getEmbeddedResourceURLs(buffer, new URL(url));
            } else {
                result = p.getEmbeddedResourceURLs(buffer, new URL(url), c);
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
                expected = new TreeSet<String>(getFile(resultFile)).iterator();
                TreeSet<URL> temp = new TreeSet<URL>(new Comparator<Object>() {
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
                assertTrue(fname+"::"+parserName + "::Expecting another result " + next, result.hasNext());
                try {
                    assertEquals(fname+"::"+parserName + "(next)", next, result.next().toString());
                } catch (ClassCastException e) {
                    fail(fname+"::"+parserName + "::Expected URL, but got " + e.toString());
                }
            }
            assertFalse(fname+"::"+parserName + "::Should have reached the end of the results", result.hasNext());
        }

        // Get expected results as a List
        private static List<String> getFile(String file) throws Exception {
            ArrayList<String> al = new ArrayList<String>();
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
