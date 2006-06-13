/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

/**
 * This class provides a way to provide Authorization in jmeter requests. The
 * format of the authorization file is: URL user pass where URL is an HTTP URL,
 * user a username to use and pass the appropriate password.
 * 
 * @author <a href="mailto:luta.raphael@networks.vivendi.com">Raphael Luta</a>
 * @version $Revision$
 */
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
			am.addFile("testfiles/TestAuth.txt");
			assertEquals(5, ao.size());
			Authorization at;
			at = am.getAuthForURL(new URL("http://a.b.c/"));
			assertEquals("login", at.getUser());
			assertEquals("password", at.getPass());
			at = am.getAuthForURL(new URL("http://a.b.c/1"));
			assertEquals("login1", at.getUser());
			assertEquals("password1", at.getPass());
		}
}
