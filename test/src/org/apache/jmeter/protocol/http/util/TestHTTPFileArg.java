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

package org.apache.jmeter.protocol.http.util;

import junit.framework.TestCase;

public class TestHTTPFileArg extends TestCase {
    public TestHTTPFileArg(String name) {
        super(name);
    }

    public void testConstructors() throws Exception {
        HTTPFileArg file = new HTTPFileArg();
        assertEquals("no parameter failure", "", file.getPath());
        assertEquals("no parameter failure", "", file.getParamName());
        assertEquals("no parameter failure", "", file.getMimeType());
        file = new HTTPFileArg("path");
        assertEquals("single parameter failure", "path", file.getPath());
        assertEquals("single parameter failure", "", file.getParamName());
        assertEquals("single parameter failure", "", file.getMimeType());
        file = new HTTPFileArg("path", "param", "mimetype");
        assertEquals("three parameter failure", "path", file.getPath());
        assertEquals("three parameter failure", "param", file.getParamName());
        assertEquals("three parameter failure", "mimetype", file.getMimeType());
        HTTPFileArg file2 = new HTTPFileArg(file);
        assertEquals("copy constructor failure", "path", file2.getPath());
        assertEquals("copy constructor failure", "param", file2.getParamName());
        assertEquals("copy constructor failure", "mimetype", file2.getMimeType());
    }

    public void testGettersSetters() throws Exception {
        HTTPFileArg file = new HTTPFileArg();
        assertEquals("", file.getPath());
        assertEquals("", file.getParamName());
        assertEquals("", file.getMimeType());
        file.setPath("path");
        file.setParamName("param");
        file.setMimeType("mimetype");
        file.setHeader("header");
        assertEquals("path", file.getPath());
        assertEquals("param", file.getParamName());
        assertEquals("mimetype", file.getMimeType());
        assertEquals("header", file.getHeader());
    }

    public void testToString() throws Exception {
        HTTPFileArg file = new HTTPFileArg("path1", "param1", "mimetype1");
        assertEquals("path:'path1'|param:'param1'|mimetype:'mimetype1'", file.toString());
    }
}
