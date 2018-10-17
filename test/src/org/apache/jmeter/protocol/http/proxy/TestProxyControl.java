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

package org.apache.jmeter.protocol.http.proxy;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.jmeter.protocol.http.sampler.HTTPNullSampler;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jmeter.samplers.SampleResult;
import org.junit.Before;
import org.junit.Test;

public class TestProxyControl {
        private HTTPSamplerBase sampler;

        private ProxyControl control;


        @Before
        public void setUp() {
            control = new ProxyControl();
            control.addIncludedPattern(".*\\.jsp");
            control.addExcludedPattern(".*apache.org.*");
            sampler = new HTTPNullSampler();
        }

        @Test
        public void testFilter1() throws Exception {
            sampler.setDomain("jakarta.org");
            sampler.setPath("index.jsp");
            assertTrue("Should find jakarta.org/index.jsp", control.filterUrl(sampler));
        }

        @Test
        public void testFilter2() throws Exception {
            sampler.setPath("index.jsp");
            sampler.setDomain("www.apache.org");
            assertFalse("Should not match www.apache.org", control.filterUrl(sampler));
        }

        @Test
        public void testFilter3() throws Exception {
            sampler.setPath("header.gif");
            sampler.setDomain("jakarta.org");
            assertFalse("Should not match header.gif", control.filterUrl(sampler));
        }

        @Test
        public void testContentTypeNoFilters() throws Exception {
            SampleResult result = new SampleResult();
            // No filters
            control.setContentTypeInclude(null);
            control.setContentTypeExclude(null);

            result.setContentType(null);
            assertTrue("Should allow if no content-type present", control.filterContentType(result));           
            result.setContentType("text/html; charset=utf-8");
            assertTrue("Should allow text/html", control.filterContentType(result));            
            result.setContentType("image/png");
            assertTrue("Should allow image/png", control.filterContentType(result));

            // Empty filters
            control.setContentTypeInclude("");
            control.setContentTypeExclude("");
            
            result.setContentType(null);
            assertTrue("Should allow if no content-type present", control.filterContentType(result));           
            result.setContentType("text/html; charset=utf-8");
            assertTrue("Should allow text/html", control.filterContentType(result));            
            result.setContentType("image/png");
            assertTrue("Should allow image/png", control.filterContentType(result));
            
            // Non empty filters
            control.setContentTypeInclude(" ");
            control.setContentTypeExclude(" ");
            
            result.setContentType(null);
            assertTrue("Should allow if no content-type present", control.filterContentType(result));           
            result.setContentType("text/html; charset=utf-8");
            assertFalse("Should not allow text/html", control.filterContentType(result));           
            result.setContentType("image/png");
            assertFalse("Should not allow image/png", control.filterContentType(result));
        }
        
        @Test
        public void testContentTypeInclude() throws Exception {
            SampleResult result = new SampleResult();
            control.setContentTypeInclude("text/html|text/ascii");

            result.setContentType(null);
            assertTrue("Should allow if no content-type present", control.filterContentType(result));           
            result.setContentType("text/html; charset=utf-8");
            assertTrue("Should allow text/html", control.filterContentType(result));            
            result.setContentType("text/css");
            assertFalse("Should not allow text/css", control.filterContentType(result));
        }
        
        @Test
        public void testContentTypeExclude() throws Exception {
            SampleResult result = new SampleResult();
            control.setContentTypeExclude("text/css");

            result.setContentType(null);
            assertTrue("Should allow if no content-type present", control.filterContentType(result));           
            result.setContentType("text/html; charset=utf-8");
            assertTrue("Should allow text/html", control.filterContentType(result));            
            result.setContentType("text/css");
            assertFalse("Should not allow text/css", control.filterContentType(result));
        }
        
        @Test
        public void testContentTypeIncludeAndExclude() throws Exception {
            SampleResult result = new SampleResult();
            // Simple inclusion and exclusion filter
            control.setContentTypeInclude("text/html|text/ascii");
            control.setContentTypeExclude("text/css");

            result.setContentType(null);
            assertTrue("Should allow if no content-type present", control.filterContentType(result));           
            result.setContentType("text/html; charset=utf-8");
            assertTrue("Should allow text/html", control.filterContentType(result));            
            result.setContentType("text/css");
            assertFalse("Should not allow text/css", control.filterContentType(result));            
            result.setContentType("image/png");
            assertFalse("Should not allow image/png", control.filterContentType(result));
            
            // Allow all but images
            control.setContentTypeInclude(null);
            control.setContentTypeExclude("image/.*");
            
            result.setContentType(null);
            assertTrue("Should allow if no content-type present", control.filterContentType(result));           
            result.setContentType("text/html; charset=utf-8");
            assertTrue("Should allow text/html", control.filterContentType(result));            
            result.setContentType("text/css");
            assertTrue("Should allow text/css", control.filterContentType(result));         
            result.setContentType("image/png");
            assertFalse("Should not allow image/png", control.filterContentType(result));
        }
}
