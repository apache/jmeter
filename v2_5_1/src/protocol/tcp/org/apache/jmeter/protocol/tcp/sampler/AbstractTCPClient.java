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

package org.apache.jmeter.protocol.tcp.sampler;

/**
 * Basic implementation of TCPClient interface.
 */
public abstract class AbstractTCPClient implements TCPClient {

    protected byte eolByte;
    protected boolean useEolByte = false;

    /**
     * {@inheritDoc}
     */
    public byte getEolByte() {
        return eolByte;
    }

    /**
     * {@inheritDoc}
     */
    public void setEolByte(int eolInt) {
        if (eolInt >= Byte.MIN_VALUE && eolInt <= Byte.MAX_VALUE) {
            this.eolByte = (byte) eolInt;
            useEolByte = true;
        } else {
            useEolByte = false;
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setupTest() {
    }

    /**
     * {@inheritDoc}
     */
    public void teardownTest() {
    }

}
