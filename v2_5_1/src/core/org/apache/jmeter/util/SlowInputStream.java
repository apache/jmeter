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

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * InputStream wrapper to emulate a slow device, e.g. modem
 *
 */
public class SlowInputStream extends FilterInputStream {

    private final CPSPauser pauser;

    /**
     * Wraps the input stream to emulate a slow device
     * @param in input stream
     * @param cps characters per second to emulate
     */
    public SlowInputStream(InputStream in, int cps) {
        super(in);
        pauser = new CPSPauser(cps);
    }

    @Override
    public int read() throws IOException {
        pauser.pause(1);
        return in.read();
    }

    // Also handles read(byte[])
    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        pauser.pause(len);
        return in.read(b, off, len);
    }

}
