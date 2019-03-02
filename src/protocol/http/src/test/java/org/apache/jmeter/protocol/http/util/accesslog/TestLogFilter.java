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

package org.apache.jmeter.protocol.http.util.accesslog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.jmeter.junit.JMeterTestCase;
import org.junit.Before;
import org.junit.Test;

public class TestLogFilter extends JMeterTestCase {

        private static final String TESTSTR = "/test/helloworld.html";

        private static final String TESTSTROUT = "/test/helloworld.jsp";

        private static class TestData {
            private final String file;

            private final boolean exclfile;

            private final boolean inclfile;

            private final boolean exclpatt;

            private final boolean inclpatt;

            TestData(String f, boolean exf, boolean inf, boolean exp, boolean inp) {
                file = f;
                exclfile = exf;
                inclfile = inf;
                exclpatt = exp;
                inclpatt = inp;
            }
        }

        private static final String[] INCL = { "hello.html", "index.html", "/index.jsp" };

        private static final String[] PATTERNS = { "index", ".jtml" };

        private static final TestData[] TESTDATA = {
        // file exclf inclf exclp inclp
                new TestData("/test/hello.jsp", true, false, true, false),
                new TestData("/test/one/hello.html", false, true, true, false),
                new TestData("hello.jsp", true, false, true, false),
                new TestData("hello.htm", true, false, true, false),
                new TestData("/test/open.jsp", true, false, true, false),
                new TestData("/test/open.html", true, false, true, false),
                new TestData("/index.jsp", false, true, false, true),
                new TestData("/index.jhtml", true, false, false, true),
                new TestData("newindex.jsp", true, false, false, true),
                new TestData("oldindex.jsp", true, false, false, true),
                new TestData("oldindex1.jsp", true, false, false, true),
                new TestData("oldindex2.jsp", true, false, false, true),
                new TestData("oldindex3.jsp", true, false, false, true),
                new TestData("oldindex4.jsp", true, false, false, true),
                new TestData("oldindex5.jsp", true, false, false, true),
                new TestData("oldindex6.jsp", true, false, false, true),
                new TestData("/test/index.htm", true, false, false, true) };

        @Test
        public void testConstruct() {
            new LogFilter();
        }

        private LogFilter testf;

        @Before
        public void setUp() {
            testf = new LogFilter();
        }

        @Test
        public void testReplaceExtension() {
            testf.setReplaceExtension("html", "jsp");
            testf.isFiltered(TESTSTR,null);// set the required variables
            assertEquals(TESTSTROUT, testf.filter(TESTSTR));
        }

        @Test
        public void testExcludeFiles() {
            testf.excludeFiles(INCL);
            for (TestData td : TESTDATA) {
                String theFile = td.file;
                boolean expect = td.exclfile;

                testf.isFiltered(theFile, null);
                String line = testf.filter(theFile);
                if (line != null) {
                    assertTrue("Expect to accept " + theFile, expect);
                } else {
                    assertFalse("Expect to reject " + theFile, expect);
                }
            }
        }

        @Test
        public void testIncludeFiles() {
            testf.includeFiles(INCL);
            for (TestData td : TESTDATA) {
                String theFile = td.file;
                boolean expect = td.inclfile;

                testf.isFiltered(theFile, null);
                String line = testf.filter(theFile);
                if (line != null) {
                    assertTrue("Expect to accept " + theFile, expect);
                } else {
                    assertFalse("Expect to reject " + theFile, expect);
                }
            }

        }

        @Test
        public void testExcludePattern() {
            testf.excludePattern(PATTERNS);
            for (TestData td : TESTDATA) {
                String theFile = td.file;
                boolean expect = td.exclpatt;

                assertPrimitiveEquals(!expect, testf.isFiltered(theFile, null));
                String line = testf.filter(theFile);
                if (line != null) {
                    assertTrue("Expect to accept " + theFile, expect);
                } else {
                    assertFalse("Expect to reject " + theFile, expect);
                }
            }
        }

        @Test
        public void testIncludePattern() {
            testf.includePattern(PATTERNS);
            for (TestData td : TESTDATA) {
                String theFile = td.file;
                boolean expect = td.inclpatt;

                assertPrimitiveEquals(!expect, testf.isFiltered(theFile, null));
                String line = testf.filter(theFile);
                if (line != null) {
                    assertTrue("Expect to accept " + theFile, expect);
                } else {
                    assertFalse("Expect to reject " + theFile, expect);
                }
            }
        }
}
