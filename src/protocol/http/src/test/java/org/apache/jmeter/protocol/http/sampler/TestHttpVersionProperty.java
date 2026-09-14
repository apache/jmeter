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

package org.apache.jmeter.protocol.http.sampler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.test.JMeterSerialTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/**
 * Tests the handling of the {@code httpclient.version} property, which selects HTTP versions such
 * as {@code HTTP/2} for the HttpClient5 sampler, but historically only took the values
 * {@code 1.0} and {@code 1.1}, which the AJP sampler still understands.
 */
class TestHttpVersionProperty extends JMeterTestCase implements JMeterSerialTest {

    @AfterEach
    void removeProperty() {
        JMeterUtils.getJMeterProperties().remove("httpclient.version");
    }

    @Test
    void keepsCurrentHttpVersionValues() {
        assertEquals("HTTP/1.1", HTTPAbstractImpl.normalizeHttpVersion("HTTP/1.1"));
        assertEquals("HTTP/2", HTTPAbstractImpl.normalizeHttpVersion("HTTP/2"));
        assertEquals("HTTP/2 Strict", HTTPAbstractImpl.normalizeHttpVersion("HTTP/2 Strict"));
    }

    @Test
    void mapsLegacyValuesToHttp11() {
        assertEquals("HTTP/1.1", HTTPAbstractImpl.normalizeHttpVersion("1.1"));
        assertEquals("HTTP/1.1", HTTPAbstractImpl.normalizeHttpVersion("1.0"));
        assertEquals("HTTP/1.1", HTTPAbstractImpl.normalizeHttpVersion(" 1.1 "));
    }

    @Test
    void defaultsToHttp11() {
        assertEquals("HTTP/1.1", HTTPAbstractImpl.readDefaultHttpVersion());
        assertEquals("", HTTPAbstractImpl.normalizeHttpVersion(null));
    }

    @Test
    void readsPropertyValue() {
        JMeterUtils.setProperty("httpclient.version", "HTTP/2");
        assertEquals("HTTP/2", HTTPAbstractImpl.readDefaultHttpVersion());
        JMeterUtils.setProperty("httpclient.version", "1.1");
        assertEquals("HTTP/1.1", HTTPAbstractImpl.readDefaultHttpVersion());
    }

    @Test
    void ajpAcceptsBothHttp10Spellings() {
        assertTrue(AjpSampler.isHttp10("1.0"));
        assertTrue(AjpSampler.isHttp10("HTTP/1.0"));
        assertTrue(AjpSampler.isHttp10("http/1.0"));
    }

    @Test
    void ajpUsesHttp11ForEveryOtherValue() {
        assertFalse(AjpSampler.isHttp10("1.1"));
        assertFalse(AjpSampler.isHttp10("HTTP/1.1"));
        assertFalse(AjpSampler.isHttp10("HTTP/2"));
        assertFalse(AjpSampler.isHttp10("HTTP/2 Strict"));
        assertFalse(AjpSampler.isHttp10(""));
        assertFalse(AjpSampler.isHttp10(null));
    }
}
