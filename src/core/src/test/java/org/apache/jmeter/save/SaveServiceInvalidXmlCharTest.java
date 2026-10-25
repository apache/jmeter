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

package org.apache.jmeter.save;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.SampleSaveConfiguration;
import org.apache.jmeter.testelement.property.StringProperty;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * A character that XML 1.0 does not allow is saved as a character reference and read back.
 * Saving such a value used to fail with WstxIOException (issue #6761).
 */
public class SaveServiceInvalidXmlCharTest extends JMeterTestCase {

    static Stream<Arguments> illegalCharacters() {
        return Stream.of(
                Arguments.of('\u0000', "&#x0;"),
                Arguments.of('\u0001', "&#x1;"),
                Arguments.of('\u001f', "&#x1f;"),
                Arguments.of('\ufffe', "&#xfffe;"),
                Arguments.of('\uffff', "&#xffff;"));
    }

    @ParameterizedTest
    @MethodSource("illegalCharacters")
    void saveElementRoundTripsIllegalChar(char illegal, String reference) throws Exception {
        String value = "pre" + illegal + "mid";
        StringProperty property = new StringProperty("bin", value);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        SaveService.saveElement(property, out);

        String xml = out.toString(StandardCharsets.UTF_8);
        assertFalse(xml.indexOf(illegal) >= 0, "the raw character must not appear in the file");
        assertTrue(xml.contains(">pre" + reference + "mid<"), xml);

        StringProperty loaded = (StringProperty) SaveService.loadElement(new ByteArrayInputStream(out.toByteArray()));
        assertEquals(value, loaded.getStringValue());
    }

    @ParameterizedTest
    @MethodSource("illegalCharacters")
    void saveElementRoundTripsIllegalCharInAttribute(char illegal, String reference) throws Exception {
        String name = "lab" + illegal + "el";
        StringProperty property = new StringProperty(name, "ok");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        SaveService.saveElement(property, out);

        String xml = out.toString(StandardCharsets.UTF_8);
        assertTrue(xml.contains("name=\"lab" + reference + "el\""), xml);

        StringProperty loaded = (StringProperty) SaveService.loadElement(new ByteArrayInputStream(out.toByteArray()));
        assertEquals(name, loaded.getName());
    }

    @ParameterizedTest
    @MethodSource("illegalCharacters")
    void sampleLabelIsWrittenAsCharacterReference(char illegal, String reference) throws Exception {
        SampleSaveConfiguration saveConfig = new SampleSaveConfiguration(true);
        SampleResult result = new SampleResult();
        result.setSaveConfig(saveConfig);
        result.setSampleLabel("lab" + illegal + "el");
        result.setSamplerData("pre" + illegal + "suf");

        StringWriter writer = new StringWriter();
        SaveService.saveSampleResult(new SampleEvent(result, "tg"), writer);
        String xml = writer.toString();
        assertTrue(xml.contains("lb=\"lab" + reference + "el\""), xml);
        assertTrue(xml.contains(">pre" + reference + "suf<"), xml);
        assertFalse(xml.indexOf(illegal) >= 0);
    }

    @Test
    void loadsLegacyCharacterReferencesFromJMeter563() throws Exception {
        StringProperty template = new StringProperty("bin", "preXmidYsufZend");
        ByteArrayOutputStream saved = new ByteArrayOutputStream();
        SaveService.saveElement(template, saved);
        String legacy = saved.toString(StandardCharsets.UTF_8)
                .replace("preXmidYsufZend", "pre&#x0;mid&#x1f;suf&#xfffe;end");

        StringProperty loaded = (StringProperty) SaveService.loadElement(
                new ByteArrayInputStream(legacy.getBytes(StandardCharsets.UTF_8)));
        assertEquals("pre\u0000mid\u001fsuf\ufffeend", loaded.getStringValue());
    }

    @Test
    void loadsDecimalNulAndPaddedHexReferences() throws Exception {
        StringProperty template = new StringProperty("bin", "A");
        ByteArrayOutputStream saved = new ByteArrayOutputStream();
        SaveService.saveElement(template, saved);
        String legacy = saved.toString(StandardCharsets.UTF_8).replace(">A<", ">&#0;&#x00;&#00031;<");

        StringProperty loaded = (StringProperty) SaveService.loadElement(
                new ByteArrayInputStream(legacy.getBytes(StandardCharsets.UTF_8)));
        assertEquals("\u0000\u0000\u001f", loaded.getStringValue());
    }
}
