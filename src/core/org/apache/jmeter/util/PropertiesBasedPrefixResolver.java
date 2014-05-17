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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.util.JOrphanUtils;
import org.apache.log.Logger;
import org.apache.xml.utils.PrefixResolver;
import org.apache.xml.utils.PrefixResolverDefault;
import org.w3c.dom.Node;

/**
 * {@link PrefixResolver} implementation that loads prefix configuration from jmeter property xpath.namespace.config
 */
public class PropertiesBasedPrefixResolver extends PrefixResolverDefault {
    private static final Logger logger = LoggingManager.getLoggerForClass();
    private static final String XPATH_NAMESPACE_CONFIG = "xpath.namespace.config";
    private static final Map<String, String> NAMESPACE_MAP = new HashMap<String, String>();
    static {
        String pathToNamespaceConfig = JMeterUtils.getPropDefault(XPATH_NAMESPACE_CONFIG, "");
        if(!StringUtils.isEmpty(pathToNamespaceConfig)) {
            Properties properties = new Properties();
            InputStream inputStream = null;
            try {
                File pathToNamespaceConfigFile = JMeterUtils.findFile(pathToNamespaceConfig);
                if(!pathToNamespaceConfigFile.exists()) {
                    logger.error("Cannot find configured file:'"+
                            pathToNamespaceConfig+"' in property:'"+XPATH_NAMESPACE_CONFIG+"', file does not exist");
                } else { 
                    if(!pathToNamespaceConfigFile.canRead()) {
                        logger.error("Cannot read configured file:'"+
                                pathToNamespaceConfig+"' in property:'"+XPATH_NAMESPACE_CONFIG+"'");
                    } else {
                        inputStream = new BufferedInputStream(new FileInputStream(pathToNamespaceConfigFile));
                        properties.load(inputStream);
                        properties.entrySet();
                        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                            NAMESPACE_MAP.put((String) entry.getKey(), (String) entry.getValue());
                        }
                        logger.info("Read following XPath namespace configuration "+ 
                                NAMESPACE_MAP);
                    }
                }
            } catch(IOException e) {
                logger.error("Error loading namespaces from file:'"+
                        pathToNamespaceConfig+"', message:"+e.getMessage(),e);
            } finally {
                JOrphanUtils.closeQuietly(inputStream);
            }
        }
    }
    /**
     * @param xpathExpressionContext Node
     */
    public PropertiesBasedPrefixResolver(Node xpathExpressionContext) {
        super(xpathExpressionContext);
    }

    /**
     * Searches prefix in NAMESPACE_MAP, if it fails to find it defaults to parent implementation
     * @param prefix Prefix
     * @param namespaceContext Node
     */
    @Override
    public String getNamespaceForPrefix(String prefix, Node namespaceContext) {
        String namespace = NAMESPACE_MAP.get(prefix);
        if(namespace==null) {
            return super.getNamespaceForPrefix(prefix, namespaceContext);
        } else {
            return namespace;
        }
    }
}
