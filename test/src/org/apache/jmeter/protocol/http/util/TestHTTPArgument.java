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

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.testelement.property.CollectionProperty;

public class TestHTTPArgument extends TestCase {
		public TestHTTPArgument(String name) {
			super(name);
		}

		public void testCloning() throws Exception {
			HTTPArgument arg = new HTTPArgument("name.?", "value_ here");
			assertEquals("name.%3F", arg.getEncodedName());
			assertEquals("value_+here", arg.getEncodedValue());
			HTTPArgument clone = (HTTPArgument) arg.clone();
			assertEquals("name.%3F", clone.getEncodedName());
			assertEquals("value_+here", clone.getEncodedValue());
		}

		public void testConversion() throws Exception {
			Arguments args = new Arguments();
			args.addArgument("name.?", "value_ here");
			args.addArgument("name$of property", "value_.+");
			HTTPArgument.convertArgumentsToHTTP(args);
			CollectionProperty argList = args.getArguments();
			HTTPArgument httpArg = (HTTPArgument) argList.get(0).getObjectValue();
			assertEquals("name.%3F", httpArg.getEncodedName());
			assertEquals("value_+here", httpArg.getEncodedValue());
			httpArg = (HTTPArgument) argList.get(1).getObjectValue();
			assertEquals("name%24of+property", httpArg.getEncodedName());
			assertEquals("value_.%2B", httpArg.getEncodedValue());
		}
}
