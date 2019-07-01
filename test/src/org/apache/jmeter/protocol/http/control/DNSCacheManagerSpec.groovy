/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License") you may not use this file except in compliance with
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

package org.apache.jmeter.protocol.http.control

import org.apache.jmeter.junit.spock.JMeterSpec
import org.xbill.DNS.ExtendedResolver
import spock.lang.IgnoreIf

class DNSCacheManagerSpec extends JMeterSpec {

    private static final String VALID_DNS_SERVER = "8.8.8.8"
    private static final String INVALID_DNS_SERVER = "512.1.1.1"

    static def localDNSResolverFailed() {
        try {
            new DNSCacheManager().resolve("apache.org")
            return false
        } catch (UnknownHostException uhe) {
            return true
        }
    }

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

    @IgnoreIf({ DNSCacheManagerSpec.localDNSResolverFailed() })
    def "Clear removes custom resolver status and any added hosts"() {
        given:
            sut.setCustomResolver(true)
            sut.addHost("apache.jmeter.org", "127.0.0.1")
            sut.clear()
        expect:
            // uses real DNS server
            sut.resolve("jmeter.apache.org").contains(InetAddress.getByName("jmeter.apache.org"))
            !sut.resolve("jmeter.apache.org").contains(InetAddress.getByName("127.0.0.1"))
    }

    def "If using an invalid server resolve throws UnknownHostException"() {
        given:
            sut.addServer(INVALID_DNS_SERVER)
            sut.setCustomResolver(true)
        when:
            sut.resolve("jmeter.apache.org")
        then:
            thrown(UnknownHostException)
            sut.resolver == null
            sut.initFailed
    }

    @IgnoreIf({
        (Boolean.getBoolean("skip.test_TestDNSCacheManager.testWithCustomResolverAnd1Server")
                || DNSCacheManagerSpec.localDNSResolverFailed())
    })
    def "Valid DNS resolves and caches with custom resolve true"() {
        given:
            sut.addServer(VALID_DNS_SERVER)
            sut.setCustomResolver(true)
            sut.setTimeoutMs(100)
        when:
            sut.resolve("jmeter.apache.org")
        then:
            sut.resolver != null
            ((ExtendedResolver) sut.resolver).getResolvers().length == 1
            sut.cache.size() == 1
    }

    def "Cache should be used where entries exist"() {
        given:
            sut.addServer(VALID_DNS_SERVER)
            sut.setCustomResolver(true)
            sut.setTimeoutMs(100)
        when:
            sut.cache.put("jmeter.apache.org", new InetAddress[0])
        then:
            sut.resolve("jmeter.apache.org") == new InetAddress[0]

        when:
            sut.cache.put("jmeter.apache.org", null)
        then:
            sut.resolve("jmeter.apache.org") == null
    }

    @IgnoreIf({ DNSCacheManagerSpec.localDNSResolverFailed() })
    def "set custom resolver but without an address should use system resolver"() {
        given:
            sut.setCustomResolver(true)
            sut.setTimeoutMs(100)
        when:
            // This will use Default System DNS resolver
            sut.resolve("jmeter.apache.org")
        then:
            sut.resolver != null
            ((ExtendedResolver) sut.resolver).getResolvers().length == 0
    }

    def "Clones retain custom resolve and server info"() {
        given:
            sut.setCustomResolver(true)
            sut.addServer(INVALID_DNS_SERVER)
            DNSCacheManager clone = (DNSCacheManager) sut.clone()
            clone.setTimeoutMs(100)
        when:
            clone.resolve("jmeter.apache.org")
        then:
            thrown(UnknownHostException)
            clone.resolver == sut.resolver
    }

    @IgnoreIf({ DNSCacheManagerSpec.localDNSResolverFailed() })
    def "Resolve Existing Host With System Default Dns Server"() {
        given:
            sut.setCustomResolver(false)
        when:
            InetAddress[] result = sut.resolve("www.example.org")
        then:
            sut.resolver == null
            result != null
            result.length > 0 // IPv4 and/or IPv6
    }

    def "Resolve Non-existing Host With System Default Dns Server"() {
        given:
            sut.setCustomResolver(false)
        when:
            sut.resolve("jmeterxxx.apache.org")
        then:
            thrown(UnknownHostException)
            sut.resolver == null
    }
}
