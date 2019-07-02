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

import java.util.HashMap;
import java.util.Map;

import org.apache.xml.utils.PrefixResolver;
import org.apache.xml.utils.PrefixResolverDefault;
import org.w3c.dom.Node;

/**
 * {@link PrefixResolver} implementation that loads prefix configuration from
 * jmeter property xpath.namespace.config
 */
public class PropertiesBasedPrefixResolverForXpath2 extends PrefixResolverDefault {
    private Map<String, String> namespaceMap = new HashMap<>();

    /**
     * @param xpathExpressionContext Node
     * @param ns Namespaces declaration
     */
    public PropertiesBasedPrefixResolverForXpath2(Node xpathExpressionContext, String ns) {
        super(xpathExpressionContext);
        String namespace = ns.trim();
        if (!namespace.isEmpty()) {
            for (String prefixValue : namespace.split("\\s+")) {
                String[] keyandvalue = prefixValue.trim().split("=");
                namespaceMap.put(keyandvalue[0], keyandvalue[1]);
            }
        }
    }

    /**
     * Searches prefix in NAMESPACE_MAP, if it fails to find it defaults to parent
     * implementation
     *
     * @param prefix           Prefix
     * @param namespaceContext Node
     */
    @Override
    public String getNamespaceForPrefix(String prefix, Node namespaceContext) {
        String namespace = namespaceMap.get(prefix);
        if (namespace == null) {
            return super.getNamespaceForPrefix(prefix, namespaceContext);
        } else {
            return namespace;
        }
    }
}
