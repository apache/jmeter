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

package org.apache.jmeter.protocol.jms.sampler.render;

import static java.lang.String.format;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.jmeter.protocol.jms.sampler.PublisherSampler;
import org.apache.jmeter.util.JMeterUtils;

import com.github.benmanes.caffeine.cache.Cache;
import com.thoughtworks.xstream.XStream;

class ObjectMessageRenderer implements MessageRenderer<Serializable> {

    TextMessageRenderer delegate;

    public ObjectMessageRenderer(TextMessageRenderer delegate) {
        this.delegate = delegate;
    }

    @Override
    public Serializable getValueFromFile(String filename, String encoding, boolean hasVariable, Cache<Object,Object> cache) {
        Serializable value;
        if (hasVariable) {
            value = getInterpretedContent(filename, encoding, hasVariable, cache);
        } else {
            value = (Serializable) cache.get(filename, p -> getContent(filename));
        }

        return value;
    }

    /**
     * Try to load an object via XStream from XML text, so that it can be used as body
     * for a JMS message.
     * An {@link IllegalStateException} will be thrown if transforming the XML to an object fails.
     *
     * @param xmlMessage String containing XML text as input for the transformation
     * @return Serialized object instance
     */
    @Override
    public Serializable getValueFromText(final String xmlMessage) {
      Serializable readObject = null;
      try {
          XStream xstream = JMeterUtils.createXStream();
          readObject = (Serializable) xstream.fromXML(xmlMessage, readObject);
      } catch (Exception e) {
          throw new IllegalStateException("Unable to load object instance from text", e);
      }
      return readObject;
    }

    /**
     * <p>Gets content with variable replaced.</p>
     * <p>If pEncoding {@link PublisherSampler#DEFAULT_ENCODING isn't provided}, try to find it.</p>
     * <p>Only raw text is cached, neither interpreted text, neither parsed object.</p>
     */
    protected Serializable getInterpretedContent(String filename, final String pEncoding, boolean hasVariable, Cache<Object,Object> cache) {
        String encoding = pEncoding;
        if (PublisherSampler.DEFAULT_ENCODING.equals(encoding)) {
            encoding = findEncoding(filename);
        }
        String stringValue = delegate.getValueFromFile(filename, encoding, hasVariable, cache);
        return (Serializable) JMeterUtils.createXStream().fromXML(stringValue);
    }

    /** Try to determine encoding based on XML prolog, if none <code>null</code> is returned. **/
    protected String findEncoding(String filename) {
        XMLInputFactory factory = XMLInputFactory.newInstance();
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        try (FileInputStream input = new FileInputStream(filename)) {
            XMLStreamReader reader = factory.createXMLStreamReader(input);
            return reader.getEncoding();
        } catch (IOException|XMLStreamException e) {
            throw new IllegalArgumentException(format("Unable to read %s", filename), e);
        }
    }

    protected Serializable getContent(String filename) {
        return (Serializable) JMeterUtils.createXStream().fromXML(new File(filename));
    }
}
