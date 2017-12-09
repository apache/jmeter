/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 */

package org.apache.jmeter.util;

import static org.junit.Assert.assertThat;

import java.io.PrintStream;

import org.hamcrest.CoreMatchers;
import org.junit.Test;

public class XPathUtilTest {

    final String lineSeparator = System.getProperty("line.separator");

    @Test
    public void testFormatXmlSimple() {
        assertThat(XPathUtil.formatXml("<one foo='bar'>Test</one>"),
                CoreMatchers.is("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                        + "<one foo=\"bar\">Test</one>" + lineSeparator));
    }

    @Test
    public void testFormatXmlComplex() {
        assertThat(
                XPathUtil.formatXml(
                        "<one foo='bar'><two/><three><four p=\"1\"/></three>...</one>"),
                CoreMatchers.is(String.join(lineSeparator, "<?xml version=\"1.0\" encoding=\"UTF-8\"?><one foo=\"bar\">",
                        "  <two/>",
                        "  <three>",
                        "    <four p=\"1\"/>",
                        "  </three>...</one>",
                        "")));
    }

    @Test()
    public void testFormatXmlInvalid() {
        PrintStream origErr = System.err;
        // The parser will print an error, so let it go somewhere, where we will
        // not see it
        System.setErr(null);
        assertThat("No well formed xml here", CoreMatchers
                .is(XPathUtil.formatXml("No well formed xml here")));
        System.setErr(origErr);
    }
}
