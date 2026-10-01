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

import com.sun.xml.txw2.output.IndentingXMLStreamWriter
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.io.Writer
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import javax.xml.stream.XMLEventWriter
import javax.xml.stream.XMLOutputFactory
import javax.xml.stream.XMLStreamWriter
import javax.xml.transform.Result

/**
 * A custom implementation of `XMLOutputFactory` that delegates operations to another `XMLOutputFactory`
 * with additional configurations such as optional XML header skipping and indentation.
 *
 * This class enhances the behavior of the specified `XMLOutputFactory` by allowing the output
 * to be customized based on the provided configuration (e.g., whether to include the XML declaration header
 * or enable indentation for formatted output).
 *
 * @constructor Creates an instance of `XMLOutputFactoryDelegate`.
 * @param delegate The underlying `XMLOutputFactory` to which most operations are delegated.
 * @param xmlHeader A flag indicating whether to include the XML declaration header in the output.
 * @param indent A flag indicating whether to enable formatted (indented) output.
 */
public class XMLOutputFactoryDelegate(
    private val delegate: XMLOutputFactory,
    private val xmlHeader: Boolean,
    private val indent: Boolean
) : XMLOutputFactory() {
    // Methods that wrap with XMLStreamWriterSkipHeader

    private fun XMLStreamWriter.applyXmlStreamWriterConfiguration(): XMLStreamWriter {
        // Shield illegal characters before indentation, so both text and
        // attributes are rewritten. The filter sits under Woodstox and emits
        // the character reference.
        var result: XMLStreamWriter = XmlCharRefStreamWriter(this)
        if (indent) {
            result = IndentingXMLStreamWriter(result)
        }
        if (!xmlHeader) {
            result = XMLStreamWriterSkipHeader(result)
        }
        return result
    }

    override fun createXMLStreamWriter(stream: Writer): XMLStreamWriter {
        return delegate.createXMLStreamWriter(XmlCharRefFilterWriter(stream))
            .applyXmlStreamWriterConfiguration()
    }

    override fun createXMLStreamWriter(stream: OutputStream): XMLStreamWriter {
        return createXMLStreamWriter(OutputStreamWriter(stream, StandardCharsets.UTF_8))
    }

    override fun createXMLStreamWriter(
        stream: OutputStream,
        encoding: String?
    ): XMLStreamWriter {
        val charset = charsetOrUtf8(encoding)
        return createXMLStreamWriter(OutputStreamWriter(stream, charset))
    }

    private fun charsetOrUtf8(encoding: String?): Charset {
        if (encoding.isNullOrEmpty()) {
            return StandardCharsets.UTF_8
        }
        return Charset.forName(encoding)
    }

    override fun createXMLStreamWriter(result: Result): XMLStreamWriter {
        return delegate.createXMLStreamWriter(result).applyXmlStreamWriterConfiguration()
    }

    // Pure delegate methods below

    override fun createXMLEventWriter(result: Result): XMLEventWriter {
        return delegate.createXMLEventWriter(result)
    }

    override fun createXMLEventWriter(stream: OutputStream): XMLEventWriter {
        return delegate.createXMLEventWriter(stream)
    }

    override fun createXMLEventWriter(
        stream: OutputStream,
        encoding: String?
    ): XMLEventWriter {
        return delegate.createXMLEventWriter(stream, encoding)
    }

    override fun createXMLEventWriter(stream: Writer): XMLEventWriter {
        return delegate.createXMLEventWriter(stream)
    }

    override fun setProperty(name: String, value: Any?) {
        return delegate.setProperty(name, value)
    }

    override fun getProperty(name: String): Any {
        return delegate.getProperty(name)
    }

    override fun isPropertySupported(name: String): Boolean {
        return delegate.isPropertySupported(name)
    }
}
