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
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import java.util.Vector;

import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import junit.framework.TestSuite;

public class TestHTMLParser16 extends JMeterTestCase {
    private static final Logger log = LoggingManager.getLoggerForClass();

    public TestHTMLParser16(String arg0) {
        super(arg0);
    }
        private String parserName;

        private int testNumber = 0;

        public TestHTMLParser16(String name, int test) {
            super(name);
            testNumber = test;
        }

        public TestHTMLParser16(String name, String parser, int test) {
            super(name);
            testNumber = test;
            parserName = parser;
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
            "org.apache.jmeter.protocol.http.parser.HtmlParserHTMLParser16",
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
                         };

        public static junit.framework.Test suite() {
            TestSuite suite = new TestSuite("TestHTMLParser");
            for (int i = 0; i < PARSERS.length; i++) {
                TestSuite ps = new TestSuite(PARSERS[i]);// Identify subtests
                for (int j = 0; j < TESTS.length; j++) {
                    TestSuite ts = new TestSuite(TESTS[j].fileName);
                    ts.addTest(new TestHTMLParser16("testParserSet", PARSERS[i], j));
                    ts.addTest(new TestHTMLParser16("testParserList", PARSERS[i], j));
                    ps.addTest(ts);
                }
                suite.addTest(ps);
            }
            return suite;
        }

        public void testParserSet() throws Exception {
            HTMLParser p = HTMLParser.getParser(parserName);
            filetest(p, TESTS[testNumber].fileName, TESTS[testNumber].baseURL, TESTS[testNumber].expectedSet, null,
                    false);
        }

        public void testParserList() throws Exception {
            HTMLParser p = HTMLParser.getParser(parserName);
            filetest(p, TESTS[testNumber].fileName, TESTS[testNumber].baseURL, TESTS[testNumber].expectedList,
                    new Vector(), true);
        }

        private static void filetest(HTMLParser p, String file, String url, String resultFile, Collection c,
                boolean orderMatters) // Does the order matter?
                throws Exception {
            String parserName = p.getClass().getName().substring("org.apache.jmeter.protocol.http.parser.".length());
            String fname = file.substring(file.indexOf("/")+1);
            log.debug("file   " + file);
            File f = findTestFile(file);
            byte[] buffer = new byte[(int) f.length()];
            int len = new FileInputStream(f).read(buffer);
            assertEquals(len, buffer.length);
            Iterator result;
            if (c == null) {
                result = p.getEmbeddedResourceURLs(buffer, new URL(url));
            } else {
                result = p.getEmbeddedResourceURLs(buffer, new URL(url), c);
            }
            /*
             * TODO: Exact ordering is only required for some tests; change the
             * comparison to do a set compare where necessary.
             */
            Iterator expected;
            if (orderMatters) {
                expected = getFile(resultFile).iterator();
            } else {
                // Convert both to Sets
                expected = new TreeSet(getFile(resultFile)).iterator();
                TreeSet temp = new TreeSet(new Comparator() {
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
                    assertEquals(fname+"::"+parserName + "(next)", next, ((URL) result.next()).toString());
                } catch (ClassCastException e) {
                    fail(fname+"::"+parserName + "::Expected URL, but got " + e.toString());
                }
            }
            assertFalse(fname+"::"+parserName + "::Should have reached the end of the results", result.hasNext());
        }

        // Get expected results as a List
        private static List getFile(String file) throws Exception {
            ArrayList al = new ArrayList();
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
