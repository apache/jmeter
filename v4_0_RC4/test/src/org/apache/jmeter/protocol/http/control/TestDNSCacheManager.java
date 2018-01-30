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

import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

import org.apache.jmeter.junit.JMeterTestCase;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.xbill.DNS.ExtendedResolver;

public class TestDNSCacheManager extends JMeterTestCase {
    private static final String INVALID_DNS_SERVER = "8.8.8.8.9"; //$NON-NLS-1$
    
    private static final String VALID_DNS_SERVER = "8.8.8.8"; //$NON-NLS-1$

    @Test
    public void testWithOneStaticHost() throws Exception {
        DNSCacheManager manager = new DNSCacheManager();
        manager.setCustomResolver(true);
        manager.addHost("jmeter.example.org", "127.0.0.1");
        assertThat(manager.resolve("jmeter.example.org"),
                CoreMatchers.is(CoreMatchers.equalTo(new InetAddress[] { InetAddress.getByName("127.0.0.1") })));
    }

    @Test
    public void testWithOneAsStaticHostAndInvalidCustomResolver() throws Exception {
        DNSCacheManager manager = new DNSCacheManager();
        manager.setCustomResolver(true);
        manager.addServer(INVALID_DNS_SERVER);
        manager.addHost("localhost", "127.0.0.1");
        assertThat(manager.resolve("localhost"),
                CoreMatchers.is(CoreMatchers.equalTo(new InetAddress[] { InetAddress.getByName("127.0.0.1") })));
    }

    @Test
    public void testWithMultipleStaticHost() throws Exception {
        DNSCacheManager manager = new DNSCacheManager();
        manager.setCustomResolver(true);
        manager.addHost("jmeter.example.org", "127.0.0.1, 1.2.3.4");
        assertThat(manager.resolve("jmeter.example.org"),
                CoreMatchers.is(CoreMatchers.equalTo(new InetAddress[] { InetAddress.getByName("127.0.0.1"), InetAddress.getByName("1.2.3.4") })));
    }

    @Test
    public void testAddAndClearStaticHost() throws Exception {
        DNSCacheManager manager = new DNSCacheManager();
        manager.setCustomResolver(true);
        manager.addHost("apache.jmeter.org", "127.0.0.1");
        manager.resolve("apache.jmeter.org");
        manager.clear();
        assertThat(Arrays.asList(manager.resolve("jmeter.apache.org")),
                CoreMatchers.hasItem(InetAddress.getByName("jmeter.apache.org")));
        assertThat(Arrays.asList(manager.resolve("jmeter.apache.org")),
                CoreMatchers.not(CoreMatchers.hasItem(InetAddress.getByName("127.0.0.1"))));
    }

    
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
        assumeTrue(!Boolean.getBoolean("skip.test_TestDNSCacheManager.testWithCustomResolverAnd1Server"));
        DNSCacheManager original = new DNSCacheManager();
        original.addServer(VALID_DNS_SERVER);
        original.setCustomResolver(true);
        original.setTimeoutMs(100);
        try {
            original.resolve("jmeter.apache.org");
            Assert.assertNotNull(original.resolver);
            Assert.assertEquals(((ExtendedResolver)original.resolver).getResolvers().length, 1);
            Assert.assertEquals(original.cache.size(), 1);
            // OK
        } catch (UnknownHostException e) {
            fail("Should have succeeded resolving jmeter.apache.org, error:"+e.getMessage());
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
            Assert.assertEquals(((ExtendedResolver)original.resolver).getResolvers().length, 0);
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
            // IPv4 and/or IPv6
            Assert.assertTrue(result.length>0);
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
