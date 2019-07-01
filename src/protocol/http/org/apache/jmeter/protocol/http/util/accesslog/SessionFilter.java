/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 */
package org.apache.jmeter.protocol.http.util.accesslog;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.jmeter.protocol.http.control.CookieManager;
import org.apache.jmeter.protocol.http.sampler.HTTPSampler;
import org.apache.jmeter.testelement.TestCloneable;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.ThreadListener;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides Session Filtering for the AccessLog Sampler.
 */
public class SessionFilter implements Filter, Serializable, TestCloneable,ThreadListener {
    private static final long serialVersionUID = 233L;
    private static final Logger log = LoggerFactory.getLogger(SessionFilter.class);

    /**
     * Protects access to managersInUse
     */
    private static final Object LOCK = new Object();
    /**
     * These objects are static across multiple threads in a test, via clone()
     * method.
     */
    private final Map<String, CookieManager> cookieManagers;
    private final Set<CookieManager> managersInUse;

    private CookieManager lastUsed;

    /**
     * Creates a new SessionFilter and initializes its fields to new collections
     */
    public SessionFilter() {
        this(new ConcurrentHashMap<>(), Collections.synchronizedSet(new HashSet<>()));
    }

    /**
     * Creates a new SessionFilter, but re-uses the given collections
     *
     * @param cookieManagers
     *            {@link CookieManager}s to be used for the different IPs
     * @param managersInUse
     *            CookieManagers currently in use by other threads
     */
    public SessionFilter(Map<String, CookieManager> cookieManagers, Set<CookieManager> managersInUse) {
        this.cookieManagers = cookieManagers;
        this.managersInUse = managersInUse;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.jmeter.protocol.http.util.accesslog.LogFilter#excPattern(java.lang.String)
     */
    protected boolean hasExcPattern(String text) {
        return false;
    }

    protected String getIpAddress(String logLine) {
        Pattern incIp = JMeterUtils.getPatternCache().getPattern("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}",
                Perl5Compiler.READ_ONLY_MASK | Perl5Compiler.SINGLELINE_MASK);
        Perl5Matcher matcher = JMeterUtils.getMatcher();
        matcher.contains(logLine, incIp);
        return matcher.getMatch().group(0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reset() {
        cookieManagers.clear();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object clone() {
        return new SessionFilter(cookieManagers, managersInUse);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void excludeFiles(String[] filenames) {
        // NOOP
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void excludePattern(String[] regexp) {
        // NOOP
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String filter(String text) {
        return text;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void includeFiles(String[] filenames) {
        // NOOP
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void includePattern(String[] regexp) {
        // NOOP
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isFiltered(String path,TestElement sampler) {
        String ipAddr = getIpAddress(path);
        CookieManager cm = getCookieManager(ipAddr);
        ((HTTPSampler)sampler).setCookieManager(cm);
        return false;
    }

    protected CookieManager getCookieManager(String ipAddr)
    {
        CookieManager cm;
        // First have to release the cookie we were using so other
        // threads stuck in wait can move on
        synchronized(LOCK) {
            if(lastUsed != null) {
                managersInUse.remove(lastUsed);
                LOCK.notifyAll();
            }
        }
        // let notified threads move on and get lock on managersInUse
        if(lastUsed != null) {
            Thread.yield();
        }
        // here is the core routine to find appropriate cookie manager and
        // check it's not being used.  If used, wait until whoever's using it gives
        // it up
        synchronized(LOCK) {
            cm = cookieManagers.get(ipAddr);
            if(cm == null) {
                cm = new CookieManager();
                cm.testStarted();
                cookieManagers.put(ipAddr,cm);
            }
            while(managersInUse.contains(cm)) {
                try {
                    LOCK.wait();
                } catch (InterruptedException e) {
                    log.info("SessionFilter wait interrupted");
                    Thread.currentThread().interrupt();
                }
            }
            managersInUse.add(cm);
            lastUsed = cm;
        }
        return cm;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setReplaceExtension(String oldextension, String newextension) {
        // NOOP
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void threadFinished() {
        synchronized(LOCK) {
            managersInUse.remove(lastUsed);
            LOCK.notifyAll();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void threadStarted() {
        // NOOP
    }
}
