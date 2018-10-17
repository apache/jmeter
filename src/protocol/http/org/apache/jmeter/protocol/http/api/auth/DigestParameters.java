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

package org.apache.jmeter.protocol.http.api.auth;

/**
 * Allows digest customization as per:
 * https://en.wikipedia.org/wiki/Digest_access_authentication
 * 
 * @since 5.0
 */
public class DigestParameters {
    public static final String VARIABLE_NAME = "__jmeter_DP__";
    private String qop;
    private String nonce;
    private String charset;
    private String algorithm;
    private String opaque;


    public DigestParameters() {
        super();
    }
    /**
     * @return the quality of protection
     */
    public String getQop() {
        return qop;
    }
    /**
     * @param qop the quality of protection to set
     */
    public void setQop(String qop) {
        this.qop = qop;
    }
    /**
     * @return the nonce
     */
    public String getNonce() {
        return nonce;
    }
    /**
     * @param nonce the nonce to set
     */
    public void setNonce(String nonce) {
        this.nonce = nonce;
    }
    /**
     * @return the charset
     */
    public String getCharset() {
        return charset;
    }
    /**
     * @param charset the charset to set
     */
    public void setCharset(String charset) {
        this.charset = charset;
    }
    /**
     * @return the algorithm (MD5 usually)
     */
    public String getAlgorithm() {
        return algorithm;
    }
    /**
     * @param algorithm the algorithm to set
     */
    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }
    /**
     * @return the opaque
     */
    public String getOpaque() {
        return opaque;
    }
    /**
     * @param opaque the opaque to set
     */
    public void setOpaque(String opaque) {
        this.opaque = opaque;
    }
}
