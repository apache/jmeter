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

package org.apache.jmeter.util;

import java.util.List;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XPathExecutable;

import com.github.benmanes.caffeine.cache.CacheLoader;

/**
 * load method is called when the key composed of
 * namespaces + xPathQuery is not in the cache.
 * Return the compiled XPathQuery with the associated
 * namespaces.
 */
public class XPathQueryCacheLoader implements CacheLoader<ImmutablePair<String, String>, XPathExecutable> {
    
    private static final Logger log = LoggerFactory.getLogger(XPathQueryCacheLoader.class);

    @Override
    public XPathExecutable load(ImmutablePair<String, String> key) 
            throws Exception {
        String xPathQuery = key.left;
        String namespacesString = key.right;

        Processor processor = XPathUtil.getProcessor();
        XPathCompiler xPathCompiler = processor.newXPathCompiler();

        List<String[]> namespacesList = XPathUtil.namespacesParse(namespacesString);
        log.debug("Parsed namespaces:{} into list of namespaces:{}", namespacesString, namespacesList);
        for (String[] namespaces : namespacesList) {
            xPathCompiler.declareNamespace(namespaces[0], namespaces[1]);
        }
        log.debug("Declared namespaces:{}, now compiling xPathQuery:{}", namespacesList, xPathQuery);
        return xPathCompiler.compile(xPathQuery);
    }
}
