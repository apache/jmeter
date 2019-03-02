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

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.jmeter.protocol.http.control.CookieManager;
import org.apache.jmeter.protocol.http.sampler.HTTPSampler;
import org.junit.Test;

public class TestSessionFilter {

    @Test
    public void testGetCookieManager() {
        SessionFilter orig = new SessionFilter();
        SessionFilter clone = (SessionFilter) orig.clone();

        final String ipAddr = "1.2.3.4";
        CookieManager cmOrig = orig.getCookieManager(ipAddr);
        orig.threadFinished(); // clear CookieManager in use in orig
        CookieManager cmClone = clone.getCookieManager(ipAddr);
        clone.threadFinished(); // clear CookieManager in clone

        assertSame(cmOrig, cmClone);
    }

    @Test
    public void testGetCookieManagerLastUse() {
        SessionFilter orig = new SessionFilter();
        SessionFilter clone = (SessionFilter) orig.clone();

        final String ipAddr = "1.2.3.4";
        CookieManager cmOrig = orig.getCookieManager(ipAddr);
        @SuppressWarnings("unused")
        CookieManager secondCm = orig.getCookieManager("2.2.2.2"); // should set cmOrig free
        CookieManager cmClone = clone.getCookieManager(ipAddr);
        orig.threadFinished(); // clear CookieManager in clone
        clone.threadFinished(); // clear CookieManager in clone

        assertSame(cmOrig, cmClone);
    }

    @Test
    public void testIsFiltered() throws Exception {
        Map<String, CookieManager> cm = new ConcurrentHashMap<>();
        Set<CookieManager> inUse = Collections
                .synchronizedSet(new HashSet<CookieManager>());
        SessionFilter filter = new SessionFilter(cm, inUse);
        HTTPSampler sampler = new HTTPSampler();
        filter.isFiltered("1.2.3.4 ...", sampler);
        assertSame(cm.get("1.2.3.4"), sampler.getCookieManager());
        assertTrue(inUse.contains(sampler.getCookieManager()));
    }

}
