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

package org.apache.jmeter.protocol.http.control

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.condition.DisabledIfSystemProperty
import org.junit.jupiter.api.fail
import org.xbill.DNS.ExtendedResolver
import org.xbill.DNS.ResolverConfig
import java.net.InetAddress
import java.net.UnknownHostException

class DNSCacheManagerTest {

    companion object {
        val VALID_DNS_SERVERS = ResolverConfig.getCurrentConfig().servers()
        val INVALID_DNS_SERVER = "512.1.1.1"
        val VALID_HOSTNAME = "jmeter.apache.org"
    }

    private val localDNSResolverOK =
        try {
            if (VALID_DNS_SERVERS == null) {
                false
            } else {
                DNSCacheManager().resolve("apache.org")
                true
            }
        } catch (uhe: UnknownHostException) {
            false
        }

    val sut = DNSCacheManager()

    private fun assumeLocalDnsResolverOK() {
        assumeTrue(localDNSResolverOK, "Local DNS resolver is needed for the test")
    }

    @Test
    fun `A custom resolver with one host will resolve that host`() {
        sut.isCustomResolver = true
        sut.addHost("jmeter.example.org", "127.0.0.1")

        assertEquals(listOf(InetAddress.getByName("127.0.0.1")), sut.resolve("jmeter.example.org").toList())
    }

    @Test
    fun `A custom resolver with one host and an invalid DNS server will still resolve that host`() {
        sut.isCustomResolver = true
        sut.addServer(INVALID_DNS_SERVER)
        sut.addHost("localhost", "127.0.0.1")

        assertEquals(listOf(InetAddress.getByName("127.0.0.1")), sut.resolve("localhost").toList())
    }

    @Test
    fun `A custom resolver with one host with many addresses will resolve all addresses for that host`() {
        sut.isCustomResolver = true
        sut.addHost("jmeter.example.org", "127.0.0.1, 1.2.3.4")
        assertEquals(
            listOf(InetAddress.getByName("127.0.0.1"), InetAddress.getByName("1.2.3.4")),
            sut.resolve("jmeter.example.org").toList()
        )
    }

    @Test
    fun `Clear removes custom resolver status and any added hosts`() {
        assumeLocalDnsResolverOK()
        sut.isCustomResolver = true
        sut.addHost(VALID_HOSTNAME, "127.0.0.1")
        sut.clear()
        // uses real DNS server
        sut.resolve(VALID_HOSTNAME).contains(InetAddress.getByName(VALID_HOSTNAME))
        !sut.resolve(VALID_HOSTNAME).contains(InetAddress.getByName("127.0.0.1"))
    }

    @Test
    fun `A custom resolver with a host entry will still fall back to system lookup`() {
        assumeLocalDnsResolverOK()
        sut.isCustomResolver = true
        sut.addHost("jmeter.example.org", "127.0.0.1")
        // uses real DNS server
        sut.resolve(VALID_HOSTNAME).contains(InetAddress.getByName(VALID_HOSTNAME))
    }

    @Test
    fun `If using an invalid server resolve throws UnknownHostException`() {
        sut.addServer(INVALID_DNS_SERVER)
        sut.isCustomResolver = true
        assertThrows<UnknownHostException> {
            sut.resolve(VALID_HOSTNAME)
        }
        assertNull(sut.resolver, ".resolver")
        assertTrue(sut.initFailed, ".initFailed")
    }

    @Test
    @DisabledIfSystemProperty(named = "skip.test_TestDNSCacheManager.testWithCustomResolverAnd1Server", matches = "true")
    fun `Valid DNS resolves and caches with custom resolve true`() {
        assumeLocalDnsResolverOK()
        for (dns in VALID_DNS_SERVERS) {
            sut.addServer(dns)
        }
        sut.isCustomResolver = true
        sut.timeoutMs = 5000
        sut.resolve(VALID_HOSTNAME)
        assertNotNull(sut.resolver, ".resolver")
        assertEquals(VALID_DNS_SERVERS.size, (sut.resolver as ExtendedResolver).resolvers.size, ".resolver.resolvers.size")
        assertEquals(1, sut.cache.size, ".cache.size")
    }

    @Test
    fun `Cache should be used where entries exist`() {
        assumeLocalDnsResolverOK()
        for (dns in VALID_DNS_SERVERS) {
            sut.addServer(dns)
        }
        sut.isCustomResolver = true
        sut.timeoutMs = 5000
        val cached = arrayOf(InetAddress.getByAddress(byteArrayOf(10, 20, 30, 40)))
        sut.cache[VALID_HOSTNAME] = cached
        assertEquals(cached, sut.resolve(VALID_HOSTNAME), ".resolve($VALID_HOSTNAME) should return cached results")

        sut.cache[VALID_HOSTNAME] = null
        assertNull(sut.resolve(VALID_HOSTNAME)) {
            ".resolve($VALID_HOSTNAME) after removing entry from the cache"
        }
    }

    @Test
    fun `set custom resolver but without an address should use system resolver`() {
        assumeLocalDnsResolverOK()
        sut.isCustomResolver = true
        sut.timeoutMs = 5000
        sut.resolve(VALID_HOSTNAME)
        assertNotNull(sut.resolver, ".resolver")
        assertEquals(0, (sut.resolver as ExtendedResolver).resolvers.size, ".resolver.resolvers.size")
    }

    @Test
    fun `Clones retain custom resolve and server info`() {
        sut.isCustomResolver = true
        sut.addServer(INVALID_DNS_SERVER)
        val clone = sut.clone() as DNSCacheManager
        clone.timeoutMs = 5000
        assertThrows<UnknownHostException> {
            clone.resolve(VALID_HOSTNAME)
        }
        assertEquals(sut.resolver, clone.resolver, "Cloned DNSCacheManager should share the resolver")
    }

    @Test
    fun `Resolve Existing Host With System Default DNS Server`() {
        assumeLocalDnsResolverOK()
        sut.isCustomResolver = false

        val result = sut.resolve("www.example.org")
        assertNull(sut.resolver, ".resolver")
        if (result == null || result.isEmpty()) {
            fail("Resolution result should be non-empty, got ${result.contentDeepToString()}")
        }
    }

    @Test
    fun `Resolve Non-existing Host With System Default DNS Server`() {
        sut.isCustomResolver = false
        assertThrows<UnknownHostException> {
            sut.resolve("jmeterxxx.apache.org")
        }
        assertNull(sut.resolver, ".resolver")
    }
}
