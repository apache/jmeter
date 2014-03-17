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

import java.net.URI;
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
        URL base = new URL("http://192.168.0.1/a/b/c"); // Trailing file
        assertEquals(new URL("http://192.168.0.1/a/b/d"),ConversionUtils.makeRelativeURL(base,"d"));
        assertEquals(new URL("http://192.168.0.1/a/d"),ConversionUtils.makeRelativeURL(base,"../d"));
        assertEquals(new URL("http://192.168.0.1/d"),ConversionUtils.makeRelativeURL(base,"../../d"));
        assertEquals(new URL("http://192.168.0.1/d"),ConversionUtils.makeRelativeURL(base,"../../../d"));
        assertEquals(new URL("http://192.168.0.1/d"),ConversionUtils.makeRelativeURL(base,"../../../../d"));
        assertEquals(new URL("http://192.168.0.1/../d"),ConversionUtils.makeRelativeURL(base,"/../d"));
        assertEquals(new URL("http://192.168.0.1/a/b/d"),ConversionUtils.makeRelativeURL(base,"./d"));
    }

    public void testMakeRelativeURL2() throws Exception {
        URL base = new URL("http://192.168.0.1/a/b/c/"); // Trailing directory
        assertEquals(new URL("http://192.168.0.1/a/b/c/d"),ConversionUtils.makeRelativeURL(base,"d"));
        assertEquals(new URL("http://192.168.0.1/a/b/d"),ConversionUtils.makeRelativeURL(base,"../d"));
        assertEquals(new URL("http://192.168.0.1/a/d"),ConversionUtils.makeRelativeURL(base,"../../d"));
        assertEquals(new URL("http://192.168.0.1/d"),ConversionUtils.makeRelativeURL(base,"../../../d"));
        assertEquals(new URL("http://192.168.0.1/d"),ConversionUtils.makeRelativeURL(base,"../../../../d"));
        assertEquals(new URL("http://192.168.0.1/../d"),ConversionUtils.makeRelativeURL(base,"/../d"));
        assertEquals(new URL("http://192.168.0.1/a/b/c/d"),ConversionUtils.makeRelativeURL(base,"./d"));
    }

    // Test that location urls with a protocol are passed unchanged
    public void testMakeRelativeURL3() throws Exception {
        URL base = new URL("http://ahost.invalid/a/b/c");
        assertEquals(new URL("http://host.invalid/e"),ConversionUtils.makeRelativeURL(base ,"http://host.invalid/e"));
        assertEquals(new URL("https://host.invalid/e"),ConversionUtils.makeRelativeURL(base ,"https://host.invalid/e"));
        assertEquals(new URL("http://host.invalid:8081/e"),ConversionUtils.makeRelativeURL(base ,"http://host.invalid:8081/e"));
        assertEquals(new URL("https://host.invalid:8081/e"),ConversionUtils.makeRelativeURL(base ,"https://host.invalid:8081/e"));
    }

    public void testRemoveSlashDotDot()
    {
        assertEquals("/path/", ConversionUtils.removeSlashDotDot("/path/"));
        assertEquals("http://host/", ConversionUtils.removeSlashDotDot("http://host/"));
        assertEquals("http://host/one", ConversionUtils.removeSlashDotDot("http://host/one"));
        assertEquals("/two", ConversionUtils.removeSlashDotDot("/one/../two"));
        assertEquals("http://host:8080/two", ConversionUtils.removeSlashDotDot("http://host:8080/one/../two"));
        assertEquals("http://host:8080/two/", ConversionUtils.removeSlashDotDot("http://host:8080/one/../two/"));
        assertEquals("http://usr@host:8080/two/", ConversionUtils.removeSlashDotDot("http://usr@host:8080/one/../two/"));
        assertEquals("http://host:8080/two/?query#anchor", ConversionUtils.removeSlashDotDot("http://host:8080/one/../two/?query#anchor"));
        assertEquals("one", ConversionUtils.removeSlashDotDot("one/two/.."));
        assertEquals("../../path", ConversionUtils.removeSlashDotDot("../../path"));
        assertEquals("/", ConversionUtils.removeSlashDotDot("/one/.."));
        assertEquals("/", ConversionUtils.removeSlashDotDot("/one/../"));
        assertEquals("/?a", ConversionUtils.removeSlashDotDot("/one/..?a"));
        assertEquals("http://host/one", ConversionUtils.removeSlashDotDot("http://host/one/../one"));
        assertEquals("http://host/one/two", ConversionUtils.removeSlashDotDot("http://host/one/two/../../one/two"));
        assertEquals("http://host/..", ConversionUtils.removeSlashDotDot("http://host/.."));
        assertEquals("http://host/../abc", ConversionUtils.removeSlashDotDot("http://host/../abc"));
    }
    
    public void testsanitizeUrl() throws Exception {
        testSanitizeUrl("http://localhost/", "http://localhost/"); // normal, no encoding needed
        testSanitizeUrl("http://localhost/a/b/c%7Cd", "http://localhost/a/b/c|d"); // pipe needs encoding
        testSanitizeUrl("http://localhost:8080/%5B%5D", "http://localhost:8080/%5B%5D"); // already encoded
        testSanitizeUrl("http://localhost:8080/?%5B%5D", "http://localhost:8080/?%5B%5D"); //already encoded
        testSanitizeUrl("http://localhost:8080/?!£$*():@~;'%22%25%5E%7B%7D[]%3C%3E%7C%5C#",
                        "http://localhost:8080/?!£$*():@~;'\"%^{}[]<>|\\#"); // unencoded query
        testSanitizeUrl("http://localhost:8080/?!£$*():@~;'%22%25%5E%7B%7D[]%3C%3E%7C%5C#",
                        "http://localhost:8080/?!£$*():@~;'%22%25%5E%7B%7D[]%3C%3E%7C%5C#"); // encoded
        testSanitizeUrl("http://localhost:8080/!£$*():@~;'%22%25%5E%7B%7D%5B%5D%3C%3E%7C%5C#",
                        "http://localhost:8080/!£$*():@~;'\"%^{}[]<>|\\#"); // unencoded path
        testSanitizeUrl("http://localhost:8080/!£$*():@~;'%22%25%5E%7B%7D%5B%5D%3C%3E%7C%5C#",
                        "http://localhost:8080/!£$*():@~;'%22%25%5E%7B%7D%5B%5D%3C%3E%7C%5C#"); // encoded
    }
    

    private void testSanitizeUrl(String expected, String input) throws Exception {
        final URL url = new URL(input);
        final URI uri = new URI(expected);
        assertEquals(uri, ConversionUtils.sanitizeUrl(url));
    }
}
