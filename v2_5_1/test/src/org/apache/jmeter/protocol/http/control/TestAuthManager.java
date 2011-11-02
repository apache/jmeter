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

package org.apache.jmeter.protocol.http.control;

import java.net.URL;

import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.testelement.property.CollectionProperty;

public class TestAuthManager extends JMeterTestCase {
        public TestAuthManager(String name) {
            super(name);
        }

        public void testHttp() throws Exception {
            assertTrue(AuthManager.isSupportedProtocol(new URL("http:")));
        }

        public void testHttps() throws Exception {
            assertTrue(AuthManager.isSupportedProtocol(new URL("https:")));
        }

        public void testFile() throws Exception {
            AuthManager am = new AuthManager();
            CollectionProperty ao = am.getAuthObjects();
            assertEquals(0, ao.size());
            am.addFile(findTestPath("testfiles/TestAuth.txt"));
            assertEquals(9, ao.size());
            Authorization at;
            at = am.getAuthForURL(new URL("http://a.b.c/"));
            assertEquals("login", at.getUser());
            assertEquals("password", at.getPass());
            at = am.getAuthForURL(new URL("http://a.b.c:80/")); // same as above
            assertEquals("login", at.getUser());
            assertEquals("password", at.getPass());
            at = am.getAuthForURL(new URL("http://a.b.c:443/"));// not same
            assertNull(at);
            at = am.getAuthForURL(new URL("http://a.b.c/1"));
            assertEquals("login1", at.getUser());
            assertEquals("password1", at.getPass());
            assertEquals("", at.getDomain());
            assertEquals("", at.getRealm());
            at = am.getAuthForURL(new URL("http://d.e.f/"));
            assertEquals("user", at.getUser());
            assertEquals("pass", at.getPass());
            assertEquals("domain", at.getDomain());
            assertEquals("realm", at.getRealm());
            at = am.getAuthForURL(new URL("https://j.k.l/"));
            assertEquals("jkl", at.getUser());
            assertEquals("pass", at.getPass());
            at = am.getAuthForURL(new URL("https://j.k.l:443/"));
            assertEquals("jkl", at.getUser());
            assertEquals("pass", at.getPass());
            at = am.getAuthForURL(new URL("https://l.m.n/"));
            assertEquals("lmn443", at.getUser());
            assertEquals("pass", at.getPass());
            at = am.getAuthForURL(new URL("https://l.m.n:443/"));
            assertEquals("lmn443", at.getUser());
            assertEquals("pass", at.getPass());
            at = am.getAuthForURL(new URL("https://l.m.n:8443/"));
            assertEquals("lmn8443", at.getUser());
            assertEquals("pass", at.getPass());
        }
}
