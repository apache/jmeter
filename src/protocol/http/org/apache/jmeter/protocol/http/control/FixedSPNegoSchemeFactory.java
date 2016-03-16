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
import org.apache.http.impl.auth.SPNegoSchemeFactory;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;

/**
 * Class to workaround <a
 * href="https://issues.apache.org/jira/browse/HTTPCLIENT-1712">issue
 * HTTPCLIENT-1712 regarding SPNego for kerberos and HTTPS</a>, which was
 * introduced in httpclient 4.5.2 and will be fixed with 4.5.3.
 */
public class FixedSPNegoSchemeFactory extends SPNegoSchemeFactory {

    public FixedSPNegoSchemeFactory(boolean stripPort) {
        super(stripPort);
    }

    @Override
    public AuthScheme create(HttpContext context) {
        return new FixedSPNegoScheme(isStripPort(), isUseCanonicalHostname());
    }

    @Override
    public AuthScheme newInstance(HttpParams params) {
        return new FixedSPNegoScheme(isStripPort(), isUseCanonicalHostname());
    }
}
