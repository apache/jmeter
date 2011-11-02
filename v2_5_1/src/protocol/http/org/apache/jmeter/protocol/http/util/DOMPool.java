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

package org.apache.jmeter.protocol.http.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.w3c.dom.Document;

/**
 * The purpose of this class is to cache the DOM Documents in memory and by-pass
 * parsing. For old systems or laptops, it's not practical to parse the XML
 * documents every time. Therefore using a memory cache can reduce the CPU
 * usage.
 * <p>
 * For now this is a simple version to test the feasibility of caching. If it
 * works, this class will be replaced with an Apache commons or something
 * equivalent. If I was familiar with Apache Commons Pool, I would probably use
 * it, but since I don't know the API, it is quicker for Proof of Concept to
 * just write a dumb one. If the number documents in the pool exceed several
 * hundred, it will take a long time for the lookup.
 * <p>
 * Created on: Jun 17, 2003<br>
 *
 */
public final class DOMPool {
    /**
     * The cache is created with an initial size of 50. Running a webservice
     * test on an old system will likely run into memory or CPU problems long
     * before the HashMap is an issue.
     */
    private static final Map<Object, Document> MEMCACHE = new ConcurrentHashMap<Object, Document>(50);

    /**
     * Return a document.
     *
     * @param key
     * @return Document
     */
    public static Document getDocument(Object key) {
        return MEMCACHE.get(key);
    }

    /**
     * Add an object to the cache.
     *
     * @param key
     * @param data
     */
    public static void putDocument(Object key, Document data) {
        MEMCACHE.put(key, data);
    }

    /**
     * Private constructor to prevent instantiation.
     */
    private DOMPool() {
    }
}
