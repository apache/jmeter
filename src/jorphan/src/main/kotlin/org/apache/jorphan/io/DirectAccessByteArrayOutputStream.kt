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
import java.nio.charset.Charset

/**
 * Non thread-safe byte array output stream that uses multiple buffers
 * to avoid copying when growing.
 *
 * This implementation maintains a list of buffers and only allocates new
 * buffers when the current one is full, avoiding the copy overhead of
 * the standard ByteArrayOutputStream.
 *
 * @since 3.1
 */
public class DirectAccessByteArrayOutputStream(initialSize: Int = 32) : OutputStream() {
    private val buffers = mutableListOf<ByteArray>()
    private var currentBuffer: ByteArray
    private var currentBufferIndex = 0
    private var currentBufferPos = 0
    private var totalSize = 0

    init {
        currentBuffer = ByteArray(initialSize.coerceAtLeast(1))
        buffers.add(currentBuffer)
    }

    override fun write(b: Int) {
        if (currentBufferPos >= currentBuffer.size) {
            allocateNewBuffer(1)
        }
        currentBuffer[currentBufferPos++] = b.toByte()
        totalSize++
    }

    override fun write(b: ByteArray, off: Int, len: Int) {
        if (off < 0 || len < 0 || off + len > b.size) {
            throw IndexOutOfBoundsException()
        }

        var remaining = len
        var offset = off

        while (remaining > 0) {
            var available = currentBuffer.size - currentBufferPos
            if (available == 0) {
                allocateNewBuffer(remaining)
                available = currentBuffer.size
            }

            val toWrite = minOf(remaining, available)
            System.arraycopy(b, offset, currentBuffer, currentBufferPos, toWrite)
            currentBufferPos += toWrite
            offset += toWrite
            remaining -= toWrite
            totalSize += toWrite
        }
    }

    private fun allocateNewBuffer(minSize: Int) {
        val newBufferIndex = ++currentBufferIndex
        currentBufferPos = 0
        if (newBufferIndex < buffers.size) {
            currentBuffer = buffers[newBufferIndex]
        } else {
            // Double the size of the last buffer, but at least minSize
            val newSize = maxOf(currentBuffer.size * 2, minSize)
            currentBuffer = ByteArray(newSize)
            buffers.add(currentBuffer)
        }
    }

    public fun toByteArray(): ByteArray {
        // Optimization: if we only have one buffer and it's exactly the right size
        if (currentBufferIndex == 0 && totalSize == currentBuffer.size) {
            return currentBuffer
        }

        // Combine all buffers
        val result = ByteArray(totalSize)
        var destPos = 0

        // Copy all complete buffers (all except the last)
        for (i in 0 until currentBufferIndex) {
            val buffer = buffers[i]
            System.arraycopy(buffer, 0, result, destPos, buffer.size)
            destPos += buffer.size
        }

        // Copy the current (last) buffer - only the used portion
        System.arraycopy(currentBuffer, 0, result, destPos, currentBufferPos)

        return result
    }

    public fun size(): Int = totalSize

    public fun reset() {
        currentBufferIndex = 0
        currentBufferPos = 0
        currentBuffer = buffers[0]
        totalSize = 0
    }

    override fun toString(): String {
        return String(toByteArray())
    }

    public fun toString(charset: Charset): String {
        return String(toByteArray(), charset)
    }

    public fun toString(charsetName: String): String {
        return String(toByteArray(), Charset.forName(charsetName))
    }
}
