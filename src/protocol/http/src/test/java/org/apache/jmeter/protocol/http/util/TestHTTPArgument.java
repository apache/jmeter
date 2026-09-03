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

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.junit.jupiter.api.Test;

public class TestHTTPArgument {

    @Test
    public void testCloning() throws Exception {
        HTTPArgument arg = new HTTPArgument("name.?", "value_ here");
        assertEquals("name.?", arg.getName());
        assertEquals("value_ here", arg.getValue());
        assertEquals("name.%3F", arg.getEncodedName());
        assertEquals("value_+here", arg.getEncodedValue());
        HTTPArgument clone = (HTTPArgument) arg.clone();
        assertEquals("name.%3F", clone.getEncodedName());
        assertEquals("value_+here", clone.getEncodedValue());
        assertEquals("name.?", clone.getName());
        assertEquals("value_ here", clone.getValue());
    }

    @Test
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

    @Test
    public void testEncoding() throws Exception {
        HTTPArgument arg;
        arg = new HTTPArgument("name.?", "value_ here", false);
        assertEquals("name.?", arg.getName());
        assertEquals("value_ here", arg.getValue());
        assertEquals("name.%3F", arg.getEncodedName());
        assertEquals("value_+here", arg.getEncodedValue());
        // Show that can bypass encoding:
        arg.setAlwaysEncoded(false);
        assertEquals("name.?", arg.getEncodedName());
        assertEquals("value_ here", arg.getEncodedValue());

        // The sample does not use a valid encoding
        arg = new HTTPArgument("name.?", "value_ here", true);
        assertEquals("name.?", arg.getName());
        assertEquals("value_ here", arg.getValue());
        assertEquals("name.%3F", arg.getEncodedName());
        assertEquals("value_+here", arg.getEncodedValue());
        arg.setAlwaysEncoded(false); // by default, name/value are encoded on fetch
        assertEquals("name.?", arg.getEncodedName());
        assertEquals("value_ here", arg.getEncodedValue());

        // Try a real encoded argument
        arg = new HTTPArgument("name.%3F", "value_+here", true);
        assertEquals("name.?", arg.getName());
        assertEquals("value_ here", arg.getValue());
        assertEquals("name.%3F", arg.getEncodedName());
        assertEquals("value_+here", arg.getEncodedValue());
        // Show that can bypass encoding:
        arg.setAlwaysEncoded(false);
        assertEquals("name.?", arg.getEncodedName());
        assertEquals("value_ here", arg.getEncodedValue());

        arg = new HTTPArgument("", "\00\01\07", "", false);
        arg.setAlwaysEncoded(false);
        assertEquals("", arg.getEncodedName());
        assertEquals("\00\01\07", arg.getEncodedValue());
    }

    @Test
    public void testMalformedPercentEncoding() throws Exception {
        // Test case for Bug 6456: Handle malformed percent-encoded strings gracefully
        // These are real-world cases that can occur when recording web application traffic

        // Case 1: Incomplete hex sequence "%u2" - reported in the issue
        HTTPArgument arg1 = new HTTPArgument("param", "value%u2", true);
        // Should preserve the original malformed value instead of throwing IllegalArgumentException
        assertEquals("param", arg1.getName());
        assertEquals("value%u2", arg1.getValue());

        // Case 2: Invalid hex character in encoding
        HTTPArgument arg2 = new HTTPArgument("name", "test%ZZ", true);
        assertEquals("name", arg2.getName());
        assertEquals("test%ZZ", arg2.getValue());

        // Case 3: Truncated percent at end of string
        HTTPArgument arg3 = new HTTPArgument("data", "some%", true);
        assertEquals("data", arg3.getName());
        assertEquals("some%", arg3.getValue());

        // Case 4: Percent followed by single hex digit
        HTTPArgument arg4 = new HTTPArgument("field", "text%2", true);
        assertEquals("field", arg4.getName());
        assertEquals("text%2", arg4.getValue());

        // Case 5: Mix of valid and invalid encoding
        HTTPArgument arg5 = new HTTPArgument("mixed", "hello%20world%u2", true);
        assertEquals("mixed", arg5.getName());
        // Valid %20 should decode to space, but %u2 is malformed
        assertEquals("hello%20world%u2", arg5.getValue());
    }
}
