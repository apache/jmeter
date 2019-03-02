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

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * OutputStream filter to emulate a slow device, e.g. modem
 *
 */
public class SlowOutputStream extends FilterOutputStream {

    private final CPSPauser pauser;

    /**
     * Create wrapped Output Stream toe emulate the requested CPS.
     * @param out OutputStream
     * @param cps characters per second
     */
    public SlowOutputStream(OutputStream out, int cps) {
        super(out);
        pauser = new CPSPauser(cps);
    }

    // Also handles write(byte[])
    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        pauser.pause(len);
        out.write(b, off, len);
    }

    @Override
    public void write(int b) throws IOException {
        pauser.pause(1);
        out.write(b);
    }
}
