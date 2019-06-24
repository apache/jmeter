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
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;

import org.apache.jmeter.junit.JMeterTestCase;
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
