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

@file:JvmName("BomInputStream")
package org.apache.jorphan.io

import java.io.InputStream
import java.io.Reader
import java.util.Arrays

/**
 * Represents a Byte Order Mark (BOM) used for identifying the encoding of text files.
 * A BOM is a sequence of bytes at the start of a file that indicates the encoding used.
 *
 * This enum defines multiple predefined BOMs for various character encodings,
 * each associated with its respective encoding name and byte sequence.
 *
 * @property charsetName The name of the character set associated with the BOM.
 * @property bytes The byte sequence representing the BOM.
 */
private enum class ByteOrderMark(val charsetName: String, val bytes: ByteArray) {
    // See https://en.wikipedia.org/wiki/Byte_order_mark#Byte-order_marks_by_encoding
    // 4-byte BOMs
    GB18030("GB18030", byteArrayOf(0x84.toByte(), 0x31, 0x95.toByte(), 0x33)),
    UTF_32_BE("UTF-32BE", byteArrayOf(0x00, 0x00, 0xFE.toByte(), 0xFF.toByte())),
    UTF_32_LE("UTF-32LE", byteArrayOf(0xFF.toByte(), 0xFE.toByte(), 0x00, 0x00)),
    // UTF_EBCDIC("UTF-EBCDIC", byteArrayOf(0xDD.toByte(), 0x73, 0x66, 0x73)),

    // 3-byte BOMs
    // BOCU_1("BOCU-1", byteArrayOf(0xFB.toByte(), 0xEE.toByte(), 0x28)),
    // SCSU("SCSU", byteArrayOf(0x0E, 0xFE.toByte(), 0xFF.toByte())),
    // UTF_1("UTF-1", byteArrayOf(0xF7.toByte(), 0x64, 0x4C)),
    // UTF_7("UTF-7", byteArrayOf(0x2B, 0x2F, 0x76)),
    UTF_8("UTF-8", byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte())),

    // 2-byte BOMs
    UTF_16_BE("UTF-16BE", byteArrayOf(0xFE.toByte(), 0xFF.toByte())),
    UTF_16_LE("UTF-16LE", byteArrayOf(0xFF.toByte(), 0xFE.toByte()));

    companion object {
        fun detect(buffer: ByteArray): ByteOrderMark? {
            for (bom in entries) {
                val bomBytes = bom.bytes
                if (buffer.size >= bomBytes.size &&
                    Arrays.equals(buffer, 0, bomBytes.size, bomBytes, 0, bomBytes.size)
                ) {
                    return bom
                }
            }
            return null
        }
    }
}

/**
 * Creates a [Reader] for the given [InputStream], detecting and handling any byte order mark (BOM) that may be present.
 * If the input stream contains a BOM, the corresponding charset is used for the reader, and the BOM bytes are skipped.
 * If no BOM is found, the default character set of the platform is used.
 *
 * @param inputStream the input stream to be wrapped as a reader, potentially with BOM handling
 * @return a reader that reads from the input stream, using the appropriate character set based on any BOM detected
 */
public fun reader(inputStream: InputStream): Reader {
    val input = if (inputStream.markSupported()) inputStream else inputStream.buffered(4)
    val bom: ByteOrderMark?
    input.mark(4)
    try {
        val buffer = input.readNBytes(4)
        bom = ByteOrderMark.detect(buffer)
    } finally {
        input.reset()
    }
    if (bom == null) {
        return input.reader()
    }
    // skip BOM
    input.skip(bom.bytes.size.toLong())
    return input.reader(charset(bom.charsetName))
}
