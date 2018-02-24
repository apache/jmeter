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
import org.apache.http.impl.auth.KerberosScheme;
import org.apache.http.impl.auth.KerberosSchemeFactory;
import org.apache.http.protocol.HttpContext;

/**
 * Extends {@link KerberosSchemeFactory} to provide ability to customize stripPort
 * setting in {@link KerberosScheme} based on {@link HttpContext}
 * @since 4.1
 */
public class DynamicKerberosSchemeFactory extends KerberosSchemeFactory {
    static final String CONTEXT_ATTRIBUTE_STRIP_PORT = "__jmeter.K_SP__";

    /**
     * @since 4.0
     */
    public DynamicKerberosSchemeFactory(final boolean stripPort, final boolean useCanonicalHostname) {
        super(stripPort, useCanonicalHostname);
    }

    @Override
    public AuthScheme create(final HttpContext context) {
        Boolean localStripPort = (Boolean) context.getAttribute(CONTEXT_ATTRIBUTE_STRIP_PORT);
        Boolean stripPort = localStripPort != null ? localStripPort : isStripPort();
        return new KerberosScheme(stripPort, isUseCanonicalHostname());
    }
}
