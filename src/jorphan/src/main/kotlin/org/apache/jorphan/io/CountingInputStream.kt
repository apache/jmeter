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

import java.io.InputStream

/**
 * A specialized InputStream that counts the number of bytes read.
 *
 * This class wraps an existing InputStream and tracks the total number of bytes
 * that have been read from the wrapped stream. The byte count is updated for all
 * read operations including single-byte reads, bulk reads into a byte array, and
 * skipped bytes. The byte count can be accessed through the `bytesRead` property.
 *
 * The behavior and functionality of the original InputStream are preserved, while
 * adding the additional feature of byte counting.
 *
 * @constructor Creates a CountingInputStream wrapping the provided InputStream.
 * @param input The InputStream to be wrapped and monitored for byte counting.
 */
public class CountingInputStream(private val input: InputStream) : InputStream() {
    public var bytesRead: Long = 0
        private set

    override fun read(): Int {
        val result = input.read()
        if (result != -1) {
            bytesRead++
        }
        return result
    }

    override fun read(b: ByteArray): Int {
        val result = input.read(b)
        if (result > 0) {
            bytesRead += result
        }
        return result
    }

    override fun read(b: ByteArray, off: Int, len: Int): Int {
        val result = input.read(b, off, len)
        if (result > 0) {
            bytesRead += result
        }
        return result
    }

    override fun skip(n: Long): Long {
        val result = input.skip(n)
        if (result > 0) {
            bytesRead += result
        }
        return result
    }

    override fun available(): Int = input.available()

    override fun close(): Unit = input.close()

    override fun markSupported(): Boolean = input.markSupported()

    override fun mark(readlimit: Int): Unit = input.mark(readlimit)

    override fun reset() {
        input.reset()
    }
}
