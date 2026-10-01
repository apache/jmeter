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

package org.apache.jmeter.save

import java.io.BufferedReader
import java.io.FilterWriter
import java.io.InputStream
import java.io.Reader
import java.io.Writer
import javax.xml.stream.XMLStreamReader
import javax.xml.stream.XMLStreamWriter

/**
 * Characters XML 1.0 does not allow are written as character references
 * (`&#x0;`, `&#x1f;`, `&#xfffe;`) and accepted again on read.
 *
 * Woodstox rejects NUL, U+FFFE and U+FFFF even as character references.
 * Those references are rewritten to a private-use pair before the parser
 * sees them, then mapped back to the original character.
 */
internal const val XML_CHAR_ESCAPE: Char = '\uE000'

private const val MARK_BASE: Int = 0xE100
private const val MARK_FFFE: Char = '\uE1FE'
private const val MARK_FFFF: Char = '\uE1FF'
private const val MAX_CHAR_REF_LENGTH: Int = 16

internal fun isIllegalXml10Char(c: Char): Boolean {
    if (c == '\t' || c == '\n' || c == '\r') {
        return false
    }
    if (c.code < 0x20) {
        return true
    }
    return c == '\uFFFE' || c == '\uFFFF'
}

internal fun xmlCharRef(c: Char): String {
    return "&#x" + Integer.toHexString(c.code) + ";"
}

private fun markFor(c: Char): Char {
    return when (c) {
        '\uFFFE' -> MARK_FFFE
        '\uFFFF' -> MARK_FFFF
        else -> (MARK_BASE + c.code).toChar()
    }
}

private fun codePointForMark(mark: Char): Int {
    return when (mark) {
        MARK_FFFE -> 0xFFFE
        MARK_FFFF -> 0xFFFF
        else -> {
            val cp = mark.code - MARK_BASE
            if (cp in 0..0x1F) cp else -1
        }
    }
}

internal fun shieldIllegalXmlChars(text: String?): String? {
    if (text == null || !needsShield(text)) {
        return text
    }
    val out = StringBuilder(text.length + 8)
    for (c in text) {
        when {
            c == XML_CHAR_ESCAPE -> out.append(XML_CHAR_ESCAPE).append(XML_CHAR_ESCAPE)
            isIllegalXml10Char(c) -> out.append(XML_CHAR_ESCAPE).append(markFor(c))
            else -> out.append(c)
        }
    }
    return out.toString()
}

private fun needsShield(text: String): Boolean {
    for (c in text) {
        if (c == XML_CHAR_ESCAPE || isIllegalXml10Char(c)) {
            return true
        }
    }
    return false
}

internal fun decodeXmlCharSentinels(text: String?): String? {
    if (text == null || text.indexOf(XML_CHAR_ESCAPE) < 0) {
        return text
    }
    val out = StringBuilder(text.length)
    var i = 0
    while (i < text.length) {
        val c = text[i]
        if (c == XML_CHAR_ESCAPE && i + 1 < text.length) {
            val mark = text[i + 1]
            when {
                mark == XML_CHAR_ESCAPE -> out.append(XML_CHAR_ESCAPE)
                codePointForMark(mark) >= 0 -> out.append(codePointForMark(mark).toChar())
                else -> {
                    out.append(c)
                    i++
                    continue
                }
            }
            i += 2
        } else {
            out.append(c)
            i++
        }
    }
    return out.toString()
}

/**
 * Replaces illegal characters with a private-use pair so Woodstox will write
 * them. [XmlCharRefFilterWriter] turns that pair into a character reference.
 */
internal class XmlCharRefStreamWriter(
    private val delegate: XMLStreamWriter
) : XMLStreamWriter by delegate {

    override fun writeCharacters(text: String?) {
        delegate.writeCharacters(shieldIllegalXmlChars(text))
    }

    override fun writeCharacters(text: CharArray?, start: Int, len: Int) {
        if (text == null) {
            delegate.writeCharacters(text, start, len)
            return
        }
        delegate.writeCharacters(shieldIllegalXmlChars(String(text, start, len)))
    }

    override fun writeAttribute(localName: String, value: String?) {
        delegate.writeAttribute(localName, shieldIllegalXmlChars(value))
    }

    override fun writeAttribute(namespaceURI: String?, localName: String?, value: String?) {
        delegate.writeAttribute(namespaceURI, localName, shieldIllegalXmlChars(value))
    }

    override fun writeAttribute(
        prefix: String?,
        namespaceURI: String?,
        localName: String?,
        value: String?
    ) {
        delegate.writeAttribute(prefix, namespaceURI, localName, shieldIllegalXmlChars(value))
    }

    override fun writeCData(data: String?) {
        if (data != null && needsShield(data)) {
            writeCharacters(data)
        } else {
            delegate.writeCData(data)
        }
    }
}

/**
 * Writes the private-use pair from [XmlCharRefStreamWriter] as `&#xN;`.
 */
internal class XmlCharRefFilterWriter(out: Writer) : FilterWriter(out) {
    private var pendingEscape = false

    override fun write(cbuf: CharArray, off: Int, len: Int) {
        var i = off
        val end = off + len
        while (i < end) {
            if (pendingEscape) {
                pendingEscape = false
                writeMark(cbuf[i])
                i++
                continue
            }
            if (cbuf[i] == XML_CHAR_ESCAPE) {
                if (i + 1 < end) {
                    writeMark(cbuf[i + 1])
                    i += 2
                } else {
                    pendingEscape = true
                    i++
                }
                continue
            }
            var j = i + 1
            while (j < end && cbuf[j] != XML_CHAR_ESCAPE) {
                j++
            }
            out.write(cbuf, i, j - i)
            i = j
        }
    }

    override fun write(c: Int) {
        val ch = c.toChar()
        if (pendingEscape) {
            pendingEscape = false
            writeMark(ch)
            return
        }
        if (ch == XML_CHAR_ESCAPE) {
            pendingEscape = true
            return
        }
        out.write(c)
    }

    override fun flush() {
        super.flush()
    }

    override fun close() {
        if (pendingEscape) {
            pendingEscape = false
            out.write(XML_CHAR_ESCAPE.code)
        }
        super.close()
    }

    private fun writeMark(mark: Char) {
        if (mark == XML_CHAR_ESCAPE) {
            out.write(XML_CHAR_ESCAPE.code)
            return
        }
        val cp = codePointForMark(mark)
        if (cp < 0) {
            out.write(XML_CHAR_ESCAPE.code)
            out.write(mark.code)
            return
        }
        out.write(xmlCharRef(cp.toChar()))
    }
}

/**
 * Rewrites character references Woodstox always rejects (`&#x0;`, `&#xfffe;`,
 * `&#xffff;`) into the private-use pair [decodeXmlCharSentinels] understands.
 */
internal class XmlIllegalCharRefReader(
    delegate: Reader
) : Reader() {
    private val input = BufferedReader(delegate, 4096)
    private val pending = StringBuilder()
    private var pendingAt = 0
    private val tail = StringBuilder()
    private var mode = Mode.TEXT
    private var eof = false

    private enum class Mode { TEXT, COMMENT, CDATA }

    override fun read(cbuf: CharArray, off: Int, len: Int): Int {
        if (len <= 0) {
            return 0
        }
        var written = 0
        while (written < len) {
            if (pendingAt >= pending.length) {
                pending.setLength(0)
                pendingAt = 0
                if (!produce()) {
                    return if (written == 0) -1 else written
                }
            }
            val n = minOf(len - written, pending.length - pendingAt)
            pending.getChars(pendingAt, pendingAt + n, cbuf, off + written)
            pendingAt += n
            written += n
        }
        return written
    }

    override fun close() {
        input.close()
    }

    private fun produce(): Boolean {
        if (!eof) {
            val chunk = CharArray(2048)
            val n = input.read(chunk)
            if (n < 0) {
                eof = true
            } else {
                tail.append(chunk, 0, n)
            }
        }
        drainTail()
        if (pending.isNotEmpty()) {
            return true
        }
        return if (eof) {
            false
        } else {
            produce()
        }
    }

    private fun drainTail() {
        var i = 0
        while (i < tail.length) {
            if (mode == Mode.COMMENT) {
                val end = tail.indexOf("-->", i)
                if (end < 0) {
                    i = holdSuffix(i, 2)
                    break
                }
                pending.append(tail, i, end + 3)
                i = end + 3
                mode = Mode.TEXT
                continue
            }
            if (mode == Mode.CDATA) {
                val end = tail.indexOf("]]>", i)
                if (end < 0) {
                    i = holdSuffix(i, 2)
                    break
                }
                pending.append(tail, i, end + 3)
                i = end + 3
                mode = Mode.TEXT
                continue
            }
            val special = indexOfSpecial(i)
            if (special < 0) {
                appendPlain(i, tail.length)
                i = tail.length
                break
            }
            appendPlain(i, special)
            val ch = tail[special]
            if (ch == '&') {
                val semi = tail.indexOf(";", special)
                val refLen = if (semi < 0) -1 else semi - special + 1
                if (semi < 0 || refLen > MAX_CHAR_REF_LENGTH) {
                    if (!eof && semi < 0 && tail.length - special <= MAX_CHAR_REF_LENGTH) {
                        i = special
                        break
                    }
                    pending.append('&')
                    i = special + 1
                    continue
                }
                rewriteRef(special, semi)
                i = semi + 1
                continue
            }
            if (tail.startsWith("<!--", special)) {
                mode = Mode.COMMENT
                pending.append("<!--")
                i = special + 4
                continue
            }
            if (tail.startsWith("<![CDATA[", special)) {
                mode = Mode.CDATA
                pending.append("<![CDATA[")
                i = special + 9
                continue
            }
            if (!eof && tail.length - special < 9) {
                i = special
                break
            }
            pending.append('<')
            i = special + 1
        }
        tail.delete(0, i)
    }

    private fun holdSuffix(from: Int, keep: Int): Int {
        val available = tail.length - from
        if (eof || available <= keep) {
            if (eof) {
                pending.append(tail, from, tail.length)
                return tail.length
            }
            return from
        }
        pending.append(tail, from, tail.length - keep)
        return tail.length - keep
    }

    private fun indexOfSpecial(from: Int): Int {
        for (k in from until tail.length) {
            val c = tail[k]
            if (c == '&' || c == '<' || c == XML_CHAR_ESCAPE) {
                return k
            }
        }
        return -1
    }

    private fun appendPlain(from: Int, to: Int) {
        for (k in from until to) {
            val c = tail[k]
            if (c == XML_CHAR_ESCAPE) {
                pending.append(XML_CHAR_ESCAPE).append(XML_CHAR_ESCAPE)
            } else {
                pending.append(c)
            }
        }
    }

    private fun rewriteRef(start: Int, semi: Int) {
        val ref = tail.substring(start, semi + 1)
        val cp = parseCharRef(ref)
        when {
            cp == 0 || cp == 0xFFFE || cp == 0xFFFF -> {
                pending.append(XML_CHAR_ESCAPE)
                pending.append(markFor(cp.toChar()))
            }
            cp == XML_CHAR_ESCAPE.code -> {
                pending.append(XML_CHAR_ESCAPE).append(XML_CHAR_ESCAPE)
            }
            else -> pending.append(ref)
        }
    }
}

internal class XmlIllegalCharRefInputStream(
    private val delegate: InputStream
) : InputStream() {
    private val pending = StringBuilder()
    private var pendingAt = 0
    private val tail = StringBuilder()
    private var mode = Mode.TEXT
    private var eof = false

    private enum class Mode { TEXT, COMMENT, CDATA }

    override fun read(): Int {
        val one = ByteArray(1)
        val n = read(one, 0, 1)
        return if (n < 0) -1 else one[0].toInt() and 0xff
    }

    override fun read(b: ByteArray, off: Int, len: Int): Int {
        if (len <= 0) {
            return 0
        }
        var written = 0
        while (written < len) {
            if (pendingAt >= pending.length) {
                pending.setLength(0)
                pendingAt = 0
                if (!produce()) {
                    return if (written == 0) -1 else written
                }
            }
            val ch = pending[pendingAt]
            pendingAt++
            // Replacement text and the bytes we copy through are ASCII, or a
            // single raw byte stored as a char in 0..255.
            b[off + written] = ch.code.toByte()
            written++
        }
        return written
    }

    override fun close() {
        delegate.close()
    }

    private fun produce(): Boolean {
        if (!eof) {
            val chunk = ByteArray(2048)
            val n = delegate.read(chunk)
            if (n < 0) {
                eof = true
            } else {
                for (i in 0 until n) {
                    tail.append((chunk[i].toInt() and 0xff).toChar())
                }
            }
        }
        drainTail()
        if (pending.isNotEmpty()) {
            return true
        }
        return if (eof) false else produce()
    }

    private fun drainTail() {
        var i = 0
        while (i < tail.length) {
            if (mode == Mode.COMMENT) {
                val end = tail.indexOf("-->", i)
                if (end < 0) {
                    i = holdSuffix(i, 2)
                    break
                }
                pending.append(tail, i, end + 3)
                i = end + 3
                mode = Mode.TEXT
                continue
            }
            if (mode == Mode.CDATA) {
                val end = tail.indexOf("]]>", i)
                if (end < 0) {
                    i = holdSuffix(i, 2)
                    break
                }
                pending.append(tail, i, end + 3)
                i = end + 3
                mode = Mode.TEXT
                continue
            }
            val special = indexOfAsciiSpecial(i)
            if (special < 0) {
                pending.append(tail, i, tail.length)
                i = tail.length
                break
            }
            pending.append(tail, i, special)
            val ch = tail[special]
            if (ch == '&') {
                val semi = tail.indexOf(";", special)
                val refLen = if (semi < 0) -1 else semi - special + 1
                if (semi < 0 || refLen > MAX_CHAR_REF_LENGTH) {
                    if (!eof && semi < 0 && tail.length - special <= MAX_CHAR_REF_LENGTH) {
                        i = special
                        break
                    }
                    pending.append('&')
                    i = special + 1
                    continue
                }
                rewriteRef(special, semi)
                i = semi + 1
                continue
            }
            if (tail.startsWith("<!--", special)) {
                mode = Mode.COMMENT
                pending.append("<!--")
                i = special + 4
                continue
            }
            if (tail.startsWith("<![CDATA[", special)) {
                mode = Mode.CDATA
                pending.append("<![CDATA[")
                i = special + 9
                continue
            }
            if (!eof && tail.length - special < 9) {
                i = special
                break
            }
            pending.append('<')
            i = special + 1
        }
        tail.delete(0, i)
    }

    private fun holdSuffix(from: Int, keep: Int): Int {
        val available = tail.length - from
        if (eof || available <= keep) {
            if (eof) {
                pending.append(tail, from, tail.length)
                return tail.length
            }
            return from
        }
        pending.append(tail, from, tail.length - keep)
        return tail.length - keep
    }

    private fun indexOfAsciiSpecial(from: Int): Int {
        for (k in from until tail.length) {
            val c = tail[k]
            if (c == '&' || c == '<') {
                return k
            }
        }
        return -1
    }

    private fun rewriteRef(start: Int, semi: Int) {
        val ref = tail.substring(start, semi + 1)
        val cp = parseCharRef(ref)
        when {
            cp == 0 || cp == 0xFFFE || cp == 0xFFFF -> {
                // ASCII character references, so this stays valid in UTF-8 and Latin-1.
                pending.append("&#x")
                pending.append(Integer.toHexString(XML_CHAR_ESCAPE.code))
                pending.append(";&#x")
                pending.append(Integer.toHexString(markFor(cp.toChar()).code))
                pending.append(';')
            }
            cp == XML_CHAR_ESCAPE.code -> {
                pending.append("&#x")
                pending.append(Integer.toHexString(XML_CHAR_ESCAPE.code))
                pending.append(";&#x")
                pending.append(Integer.toHexString(XML_CHAR_ESCAPE.code))
                pending.append(';')
            }
            else -> pending.append(ref)
        }
    }
}

internal class XmlCharRefStreamReader(
    private val delegate: XMLStreamReader
) : XMLStreamReader by delegate {

    override fun getText(): String? {
        return decodeXmlCharSentinels(delegate.text)
    }

    override fun getAttributeValue(index: Int): String? {
        return decodeXmlCharSentinels(delegate.getAttributeValue(index))
    }

    override fun getAttributeValue(namespaceURI: String?, localName: String?): String? {
        return decodeXmlCharSentinels(delegate.getAttributeValue(namespaceURI, localName))
    }
}

private fun parseCharRef(ref: String): Int? {
    if (ref.length < 4 || ref[0] != '&' || ref[1] != '#' || ref[ref.length - 1] != ';') {
        return null
    }
    val body = ref.substring(2, ref.length - 1)
    return if (body.length > 1 && (body[0] == 'x' || body[0] == 'X')) {
        body.substring(1).toIntOrNull(16)
    } else {
        body.toIntOrNull(10)
    }
}

private fun StringBuilder.startsWith(prefix: String, offset: Int): Boolean {
    if (offset + prefix.length > length) {
        return false
    }
    for (i in prefix.indices) {
        if (this[offset + i] != prefix[i]) {
            return false
        }
    }
    return true
}
