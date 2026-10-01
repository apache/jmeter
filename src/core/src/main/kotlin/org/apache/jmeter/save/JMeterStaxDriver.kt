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

import com.ctc.wstx.api.WstxInputProperties
import com.ctc.wstx.stax.WstxInputFactory
import com.ctc.wstx.stax.WstxOutputFactory
import com.thoughtworks.xstream.io.xml.StaxDriver
import java.io.InputStream
import java.io.Reader
import javax.xml.stream.XMLInputFactory
import javax.xml.stream.XMLOutputFactory
import javax.xml.stream.XMLStreamReader
import javax.xml.transform.Source
import javax.xml.transform.stream.StreamSource

public class JMeterStaxDriver(
    public val xmlHeader: Boolean = true,
    public val indent: Boolean = true,
) : StaxDriver() {
    override fun createOutputFactory(): XMLOutputFactory {
        // A plugin StAX jar, or a javax.xml.stream.XMLOutputFactory system
        // property, must not replace Woodstox. Character-reference handling
        // below is Woodstox-specific.
        return XMLOutputFactoryDelegate(WstxOutputFactory(), xmlHeader = xmlHeader, indent = indent)
    }

    override fun createInputFactory(): XMLInputFactory {
        val factory = WstxInputFactory()
        factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false)
        // XML 1.0 documents may still contain the character references JMeter
        // 5.6.3 wrote for C0 controls (&#x1;, &#x1f;, ...). NUL, U+FFFE and
        // U+FFFF stay rejected by Woodstox and are rewritten in createParser.
        factory.setProperty(WstxInputProperties.P_ALLOW_XML11_ESCAPED_CHARS_IN_XML10, true)
        return factory
    }

    override fun createParser(reader: Reader): XMLStreamReader {
        return XmlCharRefStreamReader(super.createParser(XmlIllegalCharRefReader(reader)))
    }

    override fun createParser(input: InputStream): XMLStreamReader {
        return XmlCharRefStreamReader(super.createParser(XmlIllegalCharRefInputStream(input)))
    }

    override fun createParser(source: Source): XMLStreamReader {
        if (source is StreamSource) {
            source.reader?.let { return createParser(it) }
            source.inputStream?.let { return createParser(it) }
        }
        return XmlCharRefStreamReader(super.createParser(source))
    }
}
