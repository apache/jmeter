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

import org.apache.http.auth.Credentials;
import org.apache.http.auth.KerberosCredentials;
import org.apache.http.impl.auth.SPNegoScheme;
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSManager;
import org.ietf.jgss.GSSName;
import org.ietf.jgss.Oid;

/**
 * Class to workaround <a
 * href="https://issues.apache.org/jira/browse/HTTPCLIENT-1712">issue
 * HTTPCLIENT-1712 regarding SPNego for kerberos and HTTPS</a>, which was
 * introduced in httpclient 4.5.2 and will be fixed with 4.5.3.
 */
public class FixedSPNegoScheme extends SPNegoScheme {

    public FixedSPNegoScheme(boolean stripPort, boolean useCanonicalHostname) {
        super(stripPort, useCanonicalHostname);
    }

    @Override
    protected byte[] generateGSSToken(final byte[] input, final Oid oid,
            final String authServer, final Credentials credentials)
            throws GSSException {
        byte[] inputBuff = input;
        if (inputBuff == null) {
            inputBuff = new byte[0];
        }
        final GSSManager manager = getManager();
        final GSSName serverName = manager.createName("HTTP@" + authServer,
                GSSName.NT_HOSTBASED_SERVICE);

        final GSSCredential gssCredential;
        if (credentials instanceof KerberosCredentials) {
            gssCredential = ((KerberosCredentials) credentials)
                    .getGSSCredential();
        } else {
            gssCredential = null;
        }

        final GSSContext gssContext = manager.createContext(
                serverName.canonicalize(oid), oid, gssCredential,
                GSSContext.DEFAULT_LIFETIME);
        gssContext.requestMutualAuth(true);
        gssContext.requestCredDeleg(true);
        return gssContext.initSecContext(inputBuff, 0, inputBuff.length);
    }

}
