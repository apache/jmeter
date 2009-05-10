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

import java.net.URL;

import junit.framework.TestCase;

public class TestHTTPUtils extends TestCase {
        public TestHTTPUtils(String name) {
            super(name);
        }

        public void testgetEncoding() throws Exception {
            assertNull(ConversionUtils.getEncodingFromContentType("xyx"));
            assertEquals("utf8",ConversionUtils.getEncodingFromContentType("charset=utf8"));
            assertEquals("utf8",ConversionUtils.getEncodingFromContentType("charset=\"utf8\""));
            assertEquals("utf8",ConversionUtils.getEncodingFromContentType("text/plain ;charset=utf8"));
            assertEquals("utf8",ConversionUtils.getEncodingFromContentType("text/html ;charset=utf8;charset=def"));
            assertNull(ConversionUtils.getEncodingFromContentType("charset="));
            assertNull(ConversionUtils.getEncodingFromContentType(";charset=;"));
            assertNull(ConversionUtils.getEncodingFromContentType(";charset=no-such-charset;"));
        }
        
        public void testMakeRelativeURL() throws Exception {
            URL base = new URL("http://host/a/b/c");
            assertEquals(new URL("http://host/a/b/d"),ConversionUtils.makeRelativeURL(base,"d"));
            assertEquals(new URL("http://host/a/d"),ConversionUtils.makeRelativeURL(base,"../d"));
            assertEquals(new URL("http://host/d"),ConversionUtils.makeRelativeURL(base,"../../d"));
            assertEquals(new URL("http://host/d"),ConversionUtils.makeRelativeURL(base,"../../../d"));
            assertEquals(new URL("http://host/d"),ConversionUtils.makeRelativeURL(base,"../../../../d"));
            assertEquals(new URL("http://host/../d"),ConversionUtils.makeRelativeURL(base,"/../d"));
        }
}
