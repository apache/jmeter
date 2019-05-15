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

package org.apache.jmeter.protocol.http.control;

import org.apache.http.auth.AuthScheme;
import org.apache.http.impl.auth.SPNegoScheme;
import org.apache.http.impl.auth.SPNegoSchemeFactory;
import org.apache.http.protocol.HttpContext;
import org.apache.jmeter.util.JMeterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extends {@link SPNegoSchemeFactory} to provide ability to customize stripPort
 * setting in {@link SPNegoScheme} based on {@link HttpContext}
 * @since 5.0
 */
public class DynamicSPNegoSchemeFactory extends SPNegoSchemeFactory {
    static final String CONTEXT_ATTRIBUTE_STRIP_PORT = "__jmeter.K_SP__";
    static final String CONTEXT_ATTRIBUTE_DELEGATE_CRED = "__jmeter.K_DT__";
    static final boolean DELEGATE_CRED = JMeterUtils.getPropDefault("kerberos.spnego.delegate_cred", false);
    private static final Logger log = LoggerFactory.getLogger(DynamicSPNegoSchemeFactory.class);

    /**
     * Constructor for DynamicSPNegoSchemeFactory
     * @param stripPort flag, whether port should be stripped from SPN
     * @param useCanonicalHostname flag, whether SPN should use the canonical hostname
     * @since 4.0
     */
    public DynamicSPNegoSchemeFactory(final boolean stripPort, final boolean useCanonicalHostname) {
        super(stripPort, useCanonicalHostname);
    }

    @Override
    public AuthScheme create(final HttpContext context) {
        boolean stripPort = isEnabled(context.getAttribute(CONTEXT_ATTRIBUTE_STRIP_PORT), isStripPort());
        if (isEnabled(context.getAttribute(CONTEXT_ATTRIBUTE_DELEGATE_CRED), DELEGATE_CRED)) {
            log.debug("Use DelegatingSPNegoScheme");
            return new DelegatingSPNegoScheme(stripPort, isStripPort());
        }
        log.debug("Use SPNegoScheme");
        return new SPNegoScheme(stripPort, isUseCanonicalHostname());
    }

    private boolean isEnabled(Object contextAttribute, boolean defaultValue) {
        if (contextAttribute instanceof Boolean) {
            return ((Boolean) contextAttribute).booleanValue();
        }
        return defaultValue;
    }
}
