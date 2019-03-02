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

package org.apache.jmeter.protocol.http.parser;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.jmeter.config.Argument;
import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.protocol.http.sampler.HTTPNullSampler;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.junit.Before;
import org.junit.Test;

// TODO: need more tests
public final class TestHtmlParsingUtils extends JMeterTestCase {


        @Before
        public void setUp() {
        }

        @Test
        public void testGetParser() throws Exception {
            HtmlParsingUtils.getParser();
        }

        @Test
        public void testGetDom() throws Exception {
            HtmlParsingUtils.getDOM("<HTML></HTML>");
            HtmlParsingUtils.getDOM("");
        }

        @Test
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
        
        @Test
        public void testIsAnchorMatched() throws Exception {
            HTTPSamplerBase target=new HTTPNullSampler();
            HTTPSamplerBase pattern=new HTTPNullSampler();

            assertTrue(HtmlParsingUtils.isAnchorMatched(target, pattern));

            target.setProtocol("http:");
            assertFalse(HtmlParsingUtils.isAnchorMatched(target, pattern));

            pattern.setProtocol(".*");
            assertTrue(HtmlParsingUtils.isAnchorMatched(target, pattern));
            
            target.setDomain("a.b.c");
            assertTrue(HtmlParsingUtils.isAnchorMatched(target, pattern));

            pattern.setDomain(".*");
            assertTrue(HtmlParsingUtils.isAnchorMatched(target, pattern));
            
            target.setPath("/abc");
            assertFalse(HtmlParsingUtils.isAnchorMatched(target, pattern));

            pattern.setPath(".*");
            assertTrue(HtmlParsingUtils.isAnchorMatched(target, pattern));
            
            target.addArgument("param2", "value2", "=");
            assertTrue(HtmlParsingUtils.isAnchorMatched(target, pattern));
            
            pattern.addArgument("param1", ".*", "=");
            assertFalse(HtmlParsingUtils.isAnchorMatched(target, pattern));
            
            target.addArgument("param1", "value1", "=");
            assertTrue(HtmlParsingUtils.isAnchorMatched(target, pattern));
        }
        
        @Test
        public void testisEqualOrMatches() throws Exception {
            assertTrue(HtmlParsingUtils.isEqualOrMatches("http:","http:"));
            assertFalse(HtmlParsingUtils.isEqualOrMatches("http:","htTp:"));
            assertTrue(HtmlParsingUtils.isEqualOrMatches("http:","ht+p:"));
            assertFalse(HtmlParsingUtils.isEqualOrMatches("ht+p:","http:"));
        }

        @Test
        public void testisEqualOrMatchesCaseBlind() throws Exception {
            assertTrue(HtmlParsingUtils.isEqualOrMatchesCaseBlind("http:","http:"));
            assertTrue(HtmlParsingUtils.isEqualOrMatchesCaseBlind("http:","htTp:"));
            assertTrue(HtmlParsingUtils.isEqualOrMatches("http:","ht+p:"));
            assertFalse(HtmlParsingUtils.isEqualOrMatches("ht+p:","http:"));
        }
}
