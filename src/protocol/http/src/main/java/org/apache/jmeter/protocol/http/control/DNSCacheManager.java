/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.apache.jmeter.protocol.http.control;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.conn.DnsResolver;
import org.apache.http.impl.conn.SystemDefaultDnsResolver;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.testelement.TestIterationListener;
import org.apache.jmeter.testelement.property.BooleanProperty;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.NullProperty;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.apache.jmeter.threads.JMeterContextService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xbill.DNS.ARecord;
import org.xbill.DNS.Cache;
import org.xbill.DNS.ExtendedResolver;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.Resolver;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

/**
 * This config element provides ability to have flexible control over DNS
 * caching function. Depending on option from @see
 * {@link org.apache.jmeter.protocol.http.gui.DNSCachePanel}, either system or
 * custom resolver can be used. Custom resolver uses dnsjava library, and gives
 * ability to bypass both OS and JVM cache. It allows to use paradigm
 * "1 virtual user - 1 DNS cache" in performance tests.
 *
 * @since 2.12
 */
public class DNSCacheManager extends ConfigTestElement implements TestIterationListener, Serializable, DnsResolver {

    private static final long serialVersionUID = 2122L;

    private static final Logger log = LoggerFactory.getLogger(DNSCacheManager.class);

    public static final boolean DEFAULT_CLEAR_CACHE_EACH_ITER = false;

    //++ JMX tag values
    private static final String CLEAR_CACHE_EACH_ITER = "DNSCacheManager.clearEachIteration"; // $NON-NLS-1$

    private static final String SERVERS = "DNSCacheManager.servers"; // $NON-NLS-1$

    private static final String HOSTS = "DNSCacheManager.hosts"; // $NON-NLS-1$

    private static final String IS_CUSTOM_RESOLVER = "DNSCacheManager.isCustomResolver"; // $NON-NLS-1$
    //-- JMX tag values

    private static final boolean DEFAULT_IS_CUSTOM_RESOLVER = false;

    private final transient Cache lookupCache;

    private final transient SystemDefaultDnsResolver systemDefaultDnsResolver;

    final Map<String, InetAddress[]> cache;

    private transient Resolver resolver;

    private transient int timeoutMs;

    transient boolean initFailed;

    // ensure that the initial DNSServers are copied to the per-thread instances

    public DNSCacheManager() {
        setProperty(new CollectionProperty(SERVERS, new ArrayList<String>()));
        this.systemDefaultDnsResolver = new SystemDefaultDnsResolver();
        this.cache = new LinkedHashMap<>();
        //disabling cache
        lookupCache = new Cache();
        lookupCache.setMaxCache(0);
        lookupCache.setMaxEntries(0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object clone() {
        DNSCacheManager clone = (DNSCacheManager) super.clone();
        clone.resolver = createResolver();
        return clone;
    }

    private Resolver createResolver() {
        CollectionProperty dnsServers = getServers();
        try {
            String[] serverNames = new String[dnsServers.size()];
            int index = 0;
            for (JMeterProperty jMeterProperty : dnsServers) {
                serverNames[index] = jMeterProperty.getStringValue();
                index++;
            }
            ExtendedResolver result = new ExtendedResolver(serverNames);
            if (log.isDebugEnabled()) {
                log.debug("Using DNS Resolvers: {}", Arrays.asList(result.getResolvers()));
            }
            // resolvers will be chosen via round-robin
            result.setLoadBalance(true);
            return result;
        } catch (UnknownHostException uhe) {
            this.initFailed = true;
            log.warn("Failed to create Extended resolver: {}", uhe.getMessage(), uhe);
            return null;
        }
    }

    /**
     * Resolves address using system or custom DNS resolver
     */
    @Override
    public InetAddress[] resolve(String host) throws UnknownHostException {
        InetAddress[] result = cache.get(host);
        // cache may contain null.
        // A return value of null does not necessarily 
        // indicate that the map contains no mapping 
        // for the key; it's also possible that the map 
        // explicitly maps the key to null
        // https://docs.oracle.com/javase/8/docs/api/java/util/LinkedHashMap.html
        if (result != null || cache.containsKey(host)) {
            if (log.isDebugEnabled()) {
                logCache("hit", host, result);
            }
            return result;
        } else if (isStaticHost(host)) {
            InetAddress[] staticAddresses = fromStaticHost(host);
            if (log.isDebugEnabled()) {
                logCache("miss", host, staticAddresses);
            }
            cache.put(host, staticAddresses);
            return staticAddresses;
        } else {
            InetAddress[] addresses = requestLookup(host);
            if (log.isDebugEnabled()) {
                logCache("miss", host, addresses);
            }
            cache.put(host, addresses);
            return addresses;
        }
    }

    private void logCache(String hitOrMiss, String host, InetAddress[] addresses) {
        log.debug("Cache " + hitOrMiss + " thread#{}: {} => {}",
                JMeterContextService.getContext().getThreadNum(),
                host,
                Arrays.toString(addresses));
    }

    private boolean isStaticHost(String host) {
        JMeterProperty p = getProperty(HOSTS);
        if (p instanceof NullProperty) {
            removeProperty(HOSTS);
            return false;
        }
        CollectionProperty property = (CollectionProperty) p;
        PropertyIterator iterator = property.iterator();
        while (iterator.hasNext()) {
            TestElementProperty possibleEntry = (TestElementProperty) iterator.next();
            if (log.isDebugEnabled()) {
                log.debug("Look for {} at {}: {}", host, possibleEntry.getObjectValue(), possibleEntry.getObjectValue().getClass());
            }
            StaticHost entry = (StaticHost) possibleEntry.getObjectValue();
            if (entry.getName().equalsIgnoreCase(host)) {
                if (log.isDebugEnabled()) {
                    log.debug("Found static host: {} => {}", host, entry.getAddress());
                }
                return true;
            }
        }
        log.debug("No static host found for {}", host);
        return false;
    }

    private InetAddress[] fromStaticHost(String host) {
        JMeterProperty p = getProperty(HOSTS);
        if (p instanceof NullProperty) {
            removeProperty(HOSTS);
            return new InetAddress[0];
        }
        CollectionProperty property = (CollectionProperty) p;
        for (JMeterProperty jMeterProperty : property) {
            StaticHost entry = (StaticHost) ((TestElementProperty) jMeterProperty).getObjectValue();
            if (!entry.getName().equals(host)) {
                continue; // try the next property
            }

            List<InetAddress> addresses = new ArrayList<>();
            for (String address : entry.getAddress().split("\\s*,\\s*")) {
                try {
                    final InetAddress[] requestLookup = requestLookup(address);
                    if (requestLookup == null) {
                        addAsLiteralAddress(addresses, address);
                    } else {
                        addresses.addAll(Arrays.asList(requestLookup));
                    }
                } catch (UnknownHostException e) {
                    addAsLiteralAddress(addresses, address);
                    log.warn("Couldn't resolve static address {} for host {}", address, host, e);
                }
            }
            return addresses.toArray(new InetAddress[addresses.size()]);
        }
        return new InetAddress[0];
    }

    private void addAsLiteralAddress(List<InetAddress> addresses, String address) {
        try {
            addresses.add(InetAddress.getByName(address));
        } catch (UnknownHostException e) {
            log.info("Couldn't convert {} as literal address to InetAddress", address, e);
        }
    }

    /**
     * Sends DNS request via system or custom DNS resolver
     *
     * @param host Host to lookup
     * @return array of {@link InetAddress} or null if lookup did not return result
     */
    private InetAddress[] requestLookup(String host) throws UnknownHostException {
        InetAddress[] addresses = null;

        if (isCustomResolver()) {
            ExtendedResolver extendedResolver = getOrCreateResolver();
            if (extendedResolver == null) {
                throw new UnknownHostException("Could not resolve host:" + host
                        + ", failed to initialize resolver or no resolver found");
            } else if (extendedResolver.getResolvers().length > 0) {
                addresses = customRequestLookup(host);
            }
        } else {
            addresses = systemDefaultDnsResolver.resolve(host);
            if (log.isDebugEnabled()) {
                logCache("miss (resolved with system resolver)", host, addresses);
            }
        }

        return addresses;
    }

    private InetAddress[] customRequestLookup(String host) throws UnknownHostException {
        InetAddress[] addresses = null;
        try {
            Lookup lookup = new Lookup(host, Type.A);
            lookup.setCache(lookupCache);
            if (timeoutMs > 0) {
                resolver.setTimeout(timeoutMs / 1000, timeoutMs % 1000);
            }
            lookup.setResolver(resolver);
            Record[] records = lookup.run();
            if (records == null || records.length == 0) {
                throw new UnknownHostException("Failed to resolve host name: " + host);
            }
            addresses = new InetAddress[records.length];
            for (int i = 0; i < records.length; i++) {
                addresses[i] = ((ARecord) records[i]).getAddress();
            }
        } catch (TextParseException tpe) {
            log.debug("Failed to create Lookup object: {}", tpe.toString());
        }
        return addresses;
    }

    /**
     * Tries to initialize resolver, otherwise sets initFailed to true
     *
     * @return ExtendedResolver if init succeeded or null otherwise
     */
    private ExtendedResolver getOrCreateResolver() {
        if (resolver == null && !initFailed) {
            resolver = createResolver();
        }
        return (ExtendedResolver) resolver;
    }

    /**
     * {@inheritDoc} Clean DNS cache if appropriate check-box was selected
     */
    @Override
    public void testIterationStart(LoopIterationEvent event) {
        if (isClearEachIteration()) {
            this.cache.clear();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear() {
        super.clear();
        clearServers(); // ensure data is set up OK initially
        clearHosts();
        this.cache.clear();
        this.initFailed = false;
        this.resolver = null;
    }

    /**
     * Remove all the servers.
     */
    private void clearServers() {
        log.debug("Clear all servers from store");
        setProperty(new CollectionProperty(SERVERS, new ArrayList<String>()));
    }

    /**
     * Add DNS Server
     *
     * @param dnsServer DNS Server
     */
    public void addServer(String dnsServer) {
        getServers().addItem(dnsServer);
    }

    /**
     * @return DNS Servers
     */
    public CollectionProperty getServers() {
        return (CollectionProperty) getProperty(SERVERS);
    }

    /**
     * Clear static hosts
     */
    private void clearHosts() {
        log.debug("Clear all hosts from store");
        removeProperty(HOSTS);
        cache.clear();
    }

    /**
     * Add static host
     *
     * @param dnsHost   DNS host
     * @param addresses Comma separated list of addresses
     */
    public void addHost(String dnsHost, String addresses) {
        getHosts().addItem(new StaticHost(dnsHost, addresses));
        cache.clear();
    }

    public CollectionProperty getHosts() {
        if (getProperty(HOSTS) instanceof NullProperty) {
            setProperty(new CollectionProperty(HOSTS, new ArrayList<StaticHost>()));
        }
        return (CollectionProperty) getProperty(HOSTS);
    }

    /**
     * Clean DNS cache each iteration
     *
     * @return boolean
     */
    public boolean isClearEachIteration() {
        return this.getPropertyAsBoolean(CLEAR_CACHE_EACH_ITER, DEFAULT_CLEAR_CACHE_EACH_ITER);
    }

    /**
     * Clean DNS cache each iteration
     *
     * @param clear flag whether DNS cache should be cleared on each iteration
     */
    public void setClearEachIteration(boolean clear) {
        setProperty(new BooleanProperty(CLEAR_CACHE_EACH_ITER, clear));
    }

    public boolean isCustomResolver() {
        return this.getPropertyAsBoolean(IS_CUSTOM_RESOLVER, DEFAULT_IS_CUSTOM_RESOLVER);
    }

    public void setCustomResolver(boolean isCustomResolver) {
        this.setProperty(IS_CUSTOM_RESOLVER, isCustomResolver);
    }

    /**
     * Sets DNS resolution timeout.
     *
     * @param timeoutMs timeout in milliseconds
     */
    void setTimeoutMs(int timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    /**
     * Returns DNS resolution timeout in milliseconds.
     *
     * @return DNS resolution timeout in milliseconds
     */
    int getTimeoutMs() {
        return timeoutMs;
    }

}
