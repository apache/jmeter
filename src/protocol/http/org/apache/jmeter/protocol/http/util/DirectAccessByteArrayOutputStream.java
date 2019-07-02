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
 */
package org.apache.jmeter.protocol.http.util;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

/**
 * this is a non thread-safe specialization of java {@link ByteArrayOutputStream}
 * it returns the internal buffer if its size matches the byte count
 *
 * @since 3.1
 */
public class DirectAccessByteArrayOutputStream extends ByteArrayOutputStream {

    public DirectAccessByteArrayOutputStream(int initialSize) {
        super(initialSize);
    }

    @SuppressWarnings("sync-override")
    @Override
    public byte[] toByteArray() {
        // no need to copy the buffer if it has the right size
        // avoid an unneeded memory allocation
        if(this.count == this.buf.length) {
            return this.buf;
        }

        return Arrays.copyOf(buf, count);
    }

}
