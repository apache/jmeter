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

import org.xbill.DNS.ExtendedResolver
import org.xbill.DNS.ResolverConfig

import spock.lang.IgnoreIf
import spock.lang.Requires
import spock.lang.Specification

class DNSCacheManagerSpec extends Specification {

    private static final String[] VALID_DNS_SERVERS = ResolverConfig.getCurrentConfig().servers()
    private static final String INVALID_DNS_SERVER = "512.1.1.1"
    private static final String VALID_HOSTNAME = "jmeter.apache.org"

    private static final boolean localDNSResolverOK = {
        try {
            if (VALID_DNS_SERVERS == null) {
                return false
            }
            new DNSCacheManager().resolve("apache.org")
            return true
        } catch (UnknownHostException uhe) {
            return false
        }
    }.call() // <-- this avoids automatic casting of Closure to boolean which yields true in Groovy

    def sut = new DNSCacheManager()

    def "A custom resolver with one host will resolve that host"() {
        given:
            sut.setCustomResolver(true)
            sut.addHost("jmeter.example.org", "127.0.0.1")
        expect:
            sut.resolve("jmeter.example.org") == [InetAddress.getByName("127.0.0.1")].toArray()
    }

    def "A custom resolver with one host and an invalid DNS server will still resolve that host"() {
        given:
            sut.setCustomResolver(true)
            sut.addServer(INVALID_DNS_SERVER)
            sut.addHost("localhost", "127.0.0.1")
        expect:
            sut.resolve("localhost") == [InetAddress.getByName("127.0.0.1")].toArray()
    }

    def "A custom resolver with one host with many addresses will resolve all addresses for that host"() {
        given:
            sut.setCustomResolver(true)
            sut.addHost("jmeter.example.org", "127.0.0.1, 1.2.3.4")
        expect:
            sut.resolve("jmeter.example.org") ==
                    [InetAddress.getByName("127.0.0.1"),
                     InetAddress.getByName("1.2.3.4")].toArray()
    }

    @Requires({ localDNSResolverOK })
    def "Clear removes custom resolver status and any added hosts"() {
        given:
            sut.setCustomResolver(true)
            sut.addHost(VALID_HOSTNAME, "127.0.0.1")
            sut.clear()
        expect:
            // uses real DNS server
            sut.resolve(VALID_HOSTNAME).contains(InetAddress.getByName(VALID_HOSTNAME))
            !sut.resolve(VALID_HOSTNAME).contains(InetAddress.getByName("127.0.0.1"))
    }

    @Requires({ localDNSResolverOK })
    def "A custom resolver with a host entry will still fall back to system lookup"() {
        given:
            sut.setCustomResolver(true)
            sut.addHost("jmeter.example.org", "127.0.0.1")
        expect:
            // uses real DNS server
            sut.resolve(VALID_HOSTNAME).contains(InetAddress.getByName(VALID_HOSTNAME))
    }

    def "If using an invalid server resolve throws UnknownHostException"() {
        given:
            sut.addServer(INVALID_DNS_SERVER)
            sut.setCustomResolver(true)
        when:
            sut.resolve(VALID_HOSTNAME)
        then:
            thrown(UnknownHostException)
            sut.resolver == null
            sut.initFailed
    }

    @IgnoreIf({ Boolean.getBoolean("skip.test_TestDNSCacheManager.testWithCustomResolverAnd1Server") })
    @Requires({ localDNSResolverOK })
    def "Valid DNS resolves and caches with custom resolve true"() {
        given:
            for (dns in VALID_DNS_SERVERS) {
                sut.addServer(dns)
            }
            sut.setCustomResolver(true)
            sut.setTimeoutMs(5000)
        when:
            sut.resolve(VALID_HOSTNAME)
        then:
            sut.resolver != null
            ((ExtendedResolver) sut.resolver).getResolvers().length == VALID_DNS_SERVERS.length
            sut.cache.size() == 1
    }

    def "Cache should be used where entries exist"() {
        given:
            for (dns in VALID_DNS_SERVERS) {
                sut.addServer(dns)
            }
            sut.setCustomResolver(true)
            sut.setTimeoutMs(5000)
        when:
            sut.cache.put(VALID_HOSTNAME, new InetAddress[0])
        then:
            sut.resolve(VALID_HOSTNAME) == new InetAddress[0]

        when:
            sut.cache.put(VALID_HOSTNAME, null)
        then:
            sut.resolve(VALID_HOSTNAME) == null
    }

    @Requires({ localDNSResolverOK })
    def "set custom resolver but without an address should use system resolver"() {
        given:
            sut.setCustomResolver(true)
            sut.setTimeoutMs(5000)
        when:
            sut.resolve(VALID_HOSTNAME)
        then:
            sut.resolver != null
            ((ExtendedResolver) sut.resolver).getResolvers().length == 0
    }

    def "Clones retain custom resolve and server info"() {
        given:
            sut.setCustomResolver(true)
            sut.addServer(INVALID_DNS_SERVER)
            DNSCacheManager clone = (DNSCacheManager) sut.clone()
            clone.setTimeoutMs(5000)
        when:
            clone.resolve(VALID_HOSTNAME)
        then:
            thrown(UnknownHostException)
            clone.resolver == sut.resolver
    }

    @Requires({ localDNSResolverOK })
    def "Resolve Existing Host With System Default DNS Server"() {
        given:
            sut.setCustomResolver(false)
        when:
            InetAddress[] result = sut.resolve("www.example.org")
        then:
            sut.resolver == null
            result != null
            result.length > 0 // IPv4 and/or IPv6
    }

    def "Resolve Non-existing Host With System Default DNS Server"() {
        given:
            sut.setCustomResolver(false)
        when:
            sut.resolve("jmeterxxx.apache.org")
        then:
            thrown(UnknownHostException)
            sut.resolver == null
    }
}
