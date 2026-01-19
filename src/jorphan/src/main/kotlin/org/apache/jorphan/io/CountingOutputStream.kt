/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jorphan.io

import java.io.OutputStream

/**
 * A decorator for an [OutputStream] that counts the number of bytes written to it.
 *
 * This class wraps an existing [OutputStream] and intercepts write operations to
 * count the total number of bytes written. The byte count can be accessed through
 * the [bytesWritten] property. The original [OutputStream]'s functionality
 * (such as writing, flushing, and closing) is preserved.
 *
 * The byte count is incremented only for the `write` operations that accept a byte array.
 */
public class CountingOutputStream(private val output: OutputStream) : OutputStream() {
    public var bytesWritten: Long = 0
        private set

    override fun write(b: Int) {
        output.write(b)
    }

    override fun write(b: ByteArray, off: Int, len: Int) {
        output.write(b, off, len)
        bytesWritten += len
    }

    override fun flush() {
        output.flush()
    }

    override fun close(): Unit = output.close()
}
