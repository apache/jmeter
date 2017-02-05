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

package org.apache.jmeter.protocol.http.sampler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import org.apache.http.client.methods.HttpRequestBase;
import org.apache.jmeter.protocol.http.util.HTTPConstants;
import org.junit.Test;

public class TestHttpWebdav {

    private static final List<String> VALID_METHODS = Arrays.asList("foo",
            "BAR", "123", "VERSION-CONTROL", HTTPConstants.PROPFIND);

    private static final List<String> INVALID_METHODS = Arrays.asList("", null,
            "bär", "with blank", "\"", "Foo(bar)", ":thing", "<xml>");

    @Test
    public void testIsWebdavMethod() {
        for (String method : VALID_METHODS) {
            assertTrue(method + " is a HttpWebdav method",
                    HttpWebdav.isWebdavMethod(method));
        }
        for (String method : INVALID_METHODS) {
            assertFalse(method + " is not a HttpWebdav method",
                    HttpWebdav.isWebdavMethod(method));
        }
    }

    @Test
    public void testGetMethod() throws URISyntaxException {
        for (String method : VALID_METHODS) {
            HttpRequestBase request = new HttpWebdav(method, new URI(
                    "http://example.com"));
            assertEquals(method, request.getMethod());
        }
    }

}
