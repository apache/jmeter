/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jmeter.protocol.http.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class TestHTTPFileArg {

    @Test
    public void testConstructors() throws Exception {
        HTTPFileArg file = new HTTPFileArg();
        assertEquals("", file.getPath(), "no parameter failure");
        assertEquals("", file.getParamName(), "no parameter failure");
        assertEquals("", file.getMimeType(), "no parameter failure");
        file = new HTTPFileArg("path");
        assertEquals("path", file.getPath(), "single parameter failure");
        assertEquals("", file.getParamName(), "single parameter failure");
        assertEquals("application/octet-stream", file.getMimeType(), "single parameter failure");
        file = new HTTPFileArg("path", "param", "mimetype");
        assertEquals("path", file.getPath(), "three parameter failure");
        assertEquals("param", file.getParamName(), "three parameter failure");
        assertEquals("mimetype", file.getMimeType(), "three parameter failure");
        HTTPFileArg file2 = new HTTPFileArg(file);
        assertEquals("path", file2.getPath(), "copy constructor failure");
        assertEquals("param", file2.getParamName(), "copy constructor failure");
        assertEquals("mimetype", file2.getMimeType(), "copy constructor failure");
    }

    @Test
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

    @Test
    public void testToString() throws Exception {
        HTTPFileArg file = new HTTPFileArg("path1", "param1", "mimetype1");
        assertEquals("path:'path1'|param:'param1'|mimetype:'mimetype1'", file.toString());
    }
}
