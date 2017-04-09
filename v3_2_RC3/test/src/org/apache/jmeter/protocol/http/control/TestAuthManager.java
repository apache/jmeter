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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;

import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.junit.Test;

public class TestAuthManager extends JMeterTestCase {

        @Test
        public void testHttp() throws Exception {
            assertTrue(AuthManager.isSupportedProtocol(new URL("http:")));
        }

        @Test
        public void testHttps() throws Exception {
            assertTrue(AuthManager.isSupportedProtocol(new URL("https:")));
        }

        @Test
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

        @Test
        public void testAddFileWithoutDomainAndRealmWithMechanism() throws IOException {
            File authFile = File.createTempFile("auth", ".txt");
            Files.write(authFile.toPath(), "http://example.com\tuser\tpassword\t\t\tBASIC_DIGEST".getBytes());
            AuthManager manager = new AuthManager();
            manager.addFile(authFile.getAbsolutePath());
            Authorization authForURL = manager.getAuthForURL(new URL("http://example.com"));
            assertEquals("password", authForURL.getPass());
        }

        @Test
        public void testAddFileWithDomainAndRealmAndDefaultMechanism() throws IOException {
            File authFile = File.createTempFile("auth", ".txt");
            Files.write(authFile.toPath(), "http://example.com\tuser\tpassword\tdomain\tEXAMPLE.COM\tBASIC_DIGEST".getBytes());
            AuthManager manager = new AuthManager();
            manager.addFile(authFile.getAbsolutePath());
            Authorization authForURL = manager.getAuthForURL(new URL("http://example.com"));
            assertEquals("password", authForURL.getPass());
            assertEquals("domain", authForURL.getDomain());
        }

        @Test
        public void testAddFileWithDomainAndRealmAndMechanism() throws IOException {
            File authFile = File.createTempFile("auth", ".txt");
            Files.write(authFile.toPath(), "http://example.com\tuser\tpassword\tdomain\tEXAMPLE.COM\tKERBEROS".getBytes());
            AuthManager manager = new AuthManager();
            manager.addFile(authFile.getAbsolutePath());
            Authorization authForURL = manager.getAuthForURL(new URL("http://example.com"));
            assertEquals("password", authForURL.getPass());
            assertEquals("domain", authForURL.getDomain());
            assertEquals(AuthManager.Mechanism.KERBEROS, authForURL.getMechanism());
        }
}
