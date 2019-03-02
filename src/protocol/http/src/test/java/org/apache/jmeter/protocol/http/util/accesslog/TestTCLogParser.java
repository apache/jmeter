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

package org.apache.jmeter.protocol.http.util.accesslog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.protocol.http.sampler.HTTPNullSampler;
import org.junit.Test;

// TODO - more tests needed

public class TestTCLogParser extends JMeterTestCase {
        private static final TCLogParser tclp = new TCLogParser();

        private static final String URL1 = "127.0.0.1 - - [08/Jan/2003:07:03:54 -0500] \"GET /addrbook/ HTTP/1.1\" 200 1981";

        private static final String URL2 = "127.0.0.1 - - [08/Jan/2003:07:03:54 -0500] \"GET /addrbook?x=y HTTP/1.1\" 200 1981";

        private static final String TEST3 = "127.0.0.1 - - [08/Jan/2003:07:03:54 -0500] \"HEAD /addrbook/ HTTP/1.1\" 200 1981";

        @Test
        public void testConstruct() throws Exception {
            TCLogParser tcp;
            tcp = new TCLogParser();
            assertNull("Should not have set the filename", tcp.FILENAME);

            String file = "testfiles/access.log";
            tcp = new TCLogParser(file);
            assertEquals("Filename should have been saved", file, tcp.FILENAME);
        }

        @Test
        public void testcleanURL() throws Exception {
            String res = tclp.cleanURL(URL1);
            assertEquals("/addrbook/", res);
            assertNull(tclp.stripFile(res, new HTTPNullSampler()));
        }

        @Test
        public void testcheckURL() throws Exception {
            assertFalse("URL does not have a query", tclp.checkURL(URL1));
            assertTrue("URL is a query", tclp.checkURL(URL2));
        }

        @Test
        public void testHEAD() throws Exception {
            String res = tclp.cleanURL(TEST3);
            assertEquals("/addrbook/", res);
            assertNull(tclp.stripFile(res, new HTTPNullSampler()));
        }

}
