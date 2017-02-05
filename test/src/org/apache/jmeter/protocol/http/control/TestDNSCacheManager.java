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

import static org.junit.Assert.fail;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.jmeter.junit.JMeterTestCase;
import org.junit.Assert;
import org.junit.Test;
import org.xbill.DNS.ExtendedResolver;

public class TestDNSCacheManager extends JMeterTestCase {
    private static final String INVALID_DNS_SERVER = "8.8.8.8.9"; //$NON-NLS-1$
    
    private static final String VALID_DNS_SERVER = "8.8.8.8"; //$NON-NLS-1$
    @Test
    public void testWithCustomResolverAnd1WrongServer() throws UnknownHostException {
        DNSCacheManager original = new DNSCacheManager();
        original.addServer(INVALID_DNS_SERVER);
        original.setCustomResolver(true);
        original.setTimeoutMs(100);
        try {
            original.resolve("jmeter.apache.org");
            fail("Should have failed as DNS server does not exist");
        } catch (UnknownHostException e) {
            Assert.assertNull(original.resolver);
            Assert.assertTrue(original.initFailed);
        }
        
        try {
            original.resolve("www.apache.org");
            fail("Should have failed as DNS server does not exist");
            // OK
        } catch (UnknownHostException e) {
            Assert.assertNull(original.resolver);
            Assert.assertTrue(original.initFailed);
        }
    }
    
    @Test
    public void testWithCustomResolverAnd1Server() throws UnknownHostException {
        DNSCacheManager original = new DNSCacheManager();
        original.addServer(VALID_DNS_SERVER);
        original.setCustomResolver(true);
        original.setTimeoutMs(100);
        try {
            original.resolve("jmeter.apache.org");
            Assert.assertNotNull(original.resolver);
            Assert.assertTrue(((ExtendedResolver)original.resolver).getResolvers().length==1);
            Assert.assertTrue(original.cache.size()==1);
            // OK
        } catch (UnknownHostException e) {
            fail("System DNS server should have been used");
        }
    }
    
    @Test
    public void testUseCache() throws UnknownHostException {
        DNSCacheManager original = new DNSCacheManager();
        original.addServer(VALID_DNS_SERVER);
        original.setCustomResolver(true);
        original.setTimeoutMs(100);
        try {
            InetAddress[] expectedResult = new InetAddress[0];
            original.cache.put("jmeter.apache.org", new InetAddress[0]);
            InetAddress[] actual = original.resolve("jmeter.apache.org");
            Assert.assertArrayEquals(expectedResult, actual);
            // OK
        } catch (UnknownHostException e) {
            fail("Cache should have been used");
        }
        
        try {
            original.cache.put("jmeter.apache.org", null);
            Assert.assertNull(original.resolve("jmeter.apache.org"));
            // OK
        } catch (UnknownHostException e) {
            fail("Cache should have been used");
        }
    }
    
    @Test
    public void testWithCustomResolverAndNoServer() throws UnknownHostException {
        DNSCacheManager original = new DNSCacheManager();
        original.setCustomResolver(true);
        original.setTimeoutMs(100);
        try {
            // This will use Default System DNS resolver
            original.resolve("jmeter.apache.org");
            Assert.assertNotNull(original.resolver);
            Assert.assertTrue(((ExtendedResolver)original.resolver).getResolvers().length==0);
        } catch (UnknownHostException e) {
            fail("Should have failed as no DNS server provided");
        }
    }
    
    @Test
    public void testWithCustomResolverAndInvalidNameserver() throws UnknownHostException {
        DNSCacheManager original = new DNSCacheManager();
        original.setCustomResolver(true);
        original.addServer(INVALID_DNS_SERVER);
        original.setTimeoutMs(100);
        try {
            original.resolve("jmeter.apache.org");
            fail();
        } catch (UnknownHostException e) {
            // OK
        }
    }
    
    @Test
    public void testCloneWithCustomResolverAndInvalidNameserver() throws UnknownHostException {
        DNSCacheManager original = new DNSCacheManager();
        original.setCustomResolver(true);
        original.addServer(INVALID_DNS_SERVER);
        DNSCacheManager clone = (DNSCacheManager) original.clone();
        clone.setTimeoutMs(100);
        try {
            clone.resolve("jmeter.apache.org");
            fail();
        } catch (UnknownHostException e) {
            // OK
        }
    }
    
    @Test
    public void testResolveExistingHostWithSystemDefaultDnsServer() throws UnknownHostException {
        DNSCacheManager original = new DNSCacheManager();
        original.setCustomResolver(false);
        try {
            InetAddress[] result = original.resolve("www.example.org");
            Assert.assertNotNull(result);
            Assert.assertNull(original.resolver);
            // IPv4 and IPv6
            // Disable this test because error if no IPv6 network available
            //Assert.assertTrue(result.length == 2);
        } catch (UnknownHostException e) {
            Assert.fail("Should not have failed");
        }
    }
    
    @Test
    public void testResolveNonExistingHostWithSystemDefaultDnsServer() throws UnknownHostException {
        DNSCacheManager original = new DNSCacheManager();
        original.setCustomResolver(false);
        try {
            original.resolve("jmeterxxx.apache.org");
            fail();
        } catch (UnknownHostException e) {
            Assert.assertNull(original.resolver);
            // OK
        }
    }
}
