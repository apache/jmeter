/*
 * Copyright 2003-2004 The Apache Software Foundation.
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

package org.apache.jmeter.protocol.http.parser;

import org.apache.jmeter.config.Argument;
import org.apache.jmeter.junit.JMeterTestCase;

// TODO: need more tests
public final class TestHtmlParsingUtils extends JMeterTestCase {

		public TestHtmlParsingUtils(String name) {
			super(name);
		}

		protected void setUp() {
		}

		public void testGetParser() throws Exception {
            HtmlParsingUtils.getParser();
		}

		public void testGetDom() throws Exception {
            HtmlParsingUtils.getDOM("<HTML></HTML>");
            HtmlParsingUtils.getDOM("");
		}

		public void testIsArgumentMatched() throws Exception {
			Argument arg = new Argument();
			Argument argp = new Argument();
			assertTrue(HtmlParsingUtils.isArgumentMatched(arg, argp));

			arg = new Argument("test", "abcd");
			argp = new Argument("test", "a.*d");
			assertTrue(HtmlParsingUtils.isArgumentMatched(arg, argp));

			arg = new Argument("test", "abcd");
			argp = new Argument("test", "a.*e");
			assertFalse(HtmlParsingUtils.isArgumentMatched(arg, argp));
		}
}
