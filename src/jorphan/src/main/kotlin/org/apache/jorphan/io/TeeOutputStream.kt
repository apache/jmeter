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

import java.io.IOException
import java.io.OutputStream

/**
 * `TeeOutputStream` is an implementation of `OutputStream` that writes data to two underlying output streams.
 * It is useful when data needs to be written to multiple destinations simultaneously.
 *
 * This stream ensures that data written to it is forwarded to both the `first` and `second` output streams.
 * If an exception occurs while writing to one stream, it will still attempt to write to the other stream,
 * collecting suppressed exceptions as necessary. The same behavior applies to `flush` and `close` operations.
 *
 * @constructor Creates a `TeeOutputStream` that writes to the specified `first` and `second` output streams.
 * @param first The primary output stream to which data will be written.
 * @param second The secondary output stream to which data will be written.
 */
public class TeeOutputStream(
    private val first: OutputStream,
    private val second: OutputStream
) : OutputStream() {
    @Volatile
    private var closed: Boolean = false

    @Synchronized
    override fun write(b: Int) {
        var exception: IOException? = null
        try {
            first.write(b)
        } catch (e: IOException) {
            exception = e
        }
        try {
            second.write(b)
        } catch (e: IOException) {
            exception?.addSuppressed(e) ?: run { exception = e }
        }
        exception?.let { throw it }
    }

    override fun write(b: ByteArray) {
        write(b, 0, b.size)
    }

    @Synchronized
    override fun write(b: ByteArray, off: Int, len: Int) {
        var exception: IOException? = null
        try {
            first.write(b, off, len)
        } catch (e: IOException) {
            exception = e
        }
        try {
            second.write(b, off, len)
        } catch (e: IOException) {
            exception?.addSuppressed(e) ?: run { exception = e }
        }
        exception?.let { throw it }
    }

    @Synchronized
    override fun flush() {
        var exception: IOException? = null
        try {
            first.flush()
        } catch (e: IOException) {
            exception = e
        }
        try {
            second.flush()
        } catch (e: IOException) {
            exception?.addSuppressed(e) ?: run { exception = e }
        }
        exception?.let { throw it }
    }

    override fun close() {
        if (closed) {
            return
        }
        synchronized(this) {
            if (closed) {
                return
            }
            closed = true
        }
        var exception: IOException? = null
        try {
            first.close()
        } catch (e: IOException) {
            exception = e
        }
        try {
            second.close()
        } catch (e: IOException) {
            exception?.addSuppressed(e) ?: run { exception = e }
        }
        exception?.let { throw it }
    }
}
