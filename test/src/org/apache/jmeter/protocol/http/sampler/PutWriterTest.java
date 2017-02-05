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

import java.net.URLConnection;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.http.util.HTTPArgument;
import org.apache.jmeter.protocol.http.util.HTTPFileArg;
import org.junit.Test;
import org.apache.jmeter.protocol.http.util.HTTPConstants;

public class PutWriterTest {

    @Test
    public void testSetHeadersWithNoParams() throws Exception {
        URLConnection uc = new NullURLConnection();
        HTTPSampler sampler = new HTTPSampler();
        sampler.setHTTPFiles(new HTTPFileArg[] { new HTTPFileArg("file1", "",
                "mime1") });
        PutWriter pw = new PutWriter();
        pw.setHeaders(uc, sampler);
        assertEquals("mime1",
                uc.getRequestProperty(HTTPConstants.HEADER_CONTENT_TYPE));
    }

    @Test
    public void testSetHeadersWithParams() throws Exception {
        URLConnection uc = new NullURLConnection();
        HTTPSampler sampler = new HTTPSampler();
        sampler.setHTTPFiles(new HTTPFileArg[] { new HTTPFileArg("file2",
                "param2", "mime2") });
        Arguments arguments = new Arguments();
        arguments.addArgument(new HTTPArgument("", "parameter with no name"));
        sampler.setArguments(arguments);
        PutWriter pw = new PutWriter();
        pw.setHeaders(uc, sampler);
        assertEquals("mime2",
                uc.getRequestProperty(HTTPConstants.HEADER_CONTENT_TYPE));
    }
}
