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

package org.apache.jmeter.gui.action.template;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.jmeter.junit.JMeterTestCase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

public class TestTemplateManager extends JMeterTestCase {

    private Map<String, Template> templateMap;

    @BeforeEach
    public void setup() {
        templateMap = readTemplateFromFile();
        assertEquals(3, templateMap.size());
    }

    private Map<String, Template> readTemplateFromFile() {
        File xmlTemplate = getFileFromResource("validTemplates.xml");
        try {
            return TemplateManager.getInstance().parseTemplateFile(xmlTemplate);
        } catch (IOException | SAXException | ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    private File getFileFromResource(String resourceName) {
        try {
            return Paths.get(this.getClass().getResource(resourceName).toURI()).toFile();
        } catch (URISyntaxException e) {
            throw new RuntimeException("Can't read resource " + resourceName, e);
        }
    }

    @Test
    public void testValidTemplateFile() {
        Template testTemplate = templateMap.get("testTemplateWithParameters");
        assertTrue(testTemplate.isTestPlan());
        assertEquals("testTemplateWithParameters", testTemplate.getName());
        assertEquals("/bin/templates/testTemplate.jmx.fmkr", testTemplate.getFileName());
        assertEquals("Template with parameters", testTemplate.getDescription());
        Map<String, String> testTemplateParameters = testTemplate.getParameters();
        assertEquals("n 1", testTemplateParameters.get("testKey1"));
        assertEquals("n 2", testTemplateParameters.get("testKey2"));
        assertEquals("n 3", testTemplateParameters.get("testKey3"));

        testTemplate = templateMap.get("testTemplateNotTestPlan");
        assertFalse(testTemplate.isTestPlan());
        assertEquals("testTemplateNotTestPlan", testTemplate.getName());
        assertEquals("/bin/templates/testTemplateNotTestPlan.jmx", testTemplate.getFileName());
        assertEquals("testTemplateNotTestPlan desc", testTemplate.getDescription());
        assertNull(testTemplate.getParameters());

        testTemplate = templateMap.get("testTemplate");
        assertTrue(testTemplate.isTestPlan());
        assertEquals("testTemplate", testTemplate.getName());
        assertEquals("/bin/templates/testTemplate.jmx", testTemplate.getFileName());
        assertEquals("testTemplate desc", testTemplate.getDescription());
        assertNull(testTemplate.getParameters());
    }

    @Test
    public void testNonExistantXmlFileThrowsFileNotFoundException() throws Exception {
        File xmlTemplateFile = new File("missing.xml");
        Assertions.assertThrows(
                FileNotFoundException.class,
                () -> TemplateManager.getInstance().parseTemplateFile(xmlTemplateFile));
    }

    @Test
    public void testInvalidTemplateXml() throws IOException, SAXException, ParserConfigurationException {
        try {
            File templateFile = getFileFromResource("invalidTemplates.xml");
            TemplateManager.getInstance().parseTemplateFile(templateFile);
        } catch (SAXParseException ex) {
            assertTrue("Exception did not contains expected message, got:" + ex.getMessage(),
                    ex.getMessage().contains("Element type \"key\" must be declared."));
        }
    }

    @Test
    public void equalsHashCode() {
        EqualsVerifier.forClass(Template.class)
                .usingGetClass()
                .suppress(Warning.NONFINAL_FIELDS)
                .suppress(Warning.TRANSIENT_FIELDS)
                .verify();
    }

    @Test
    public void testDifferentTemplatesAreNotEqual() {
        Template testTemplate1 = templateMap.get("testTemplateWithParameters");
        Template testTemplate2 = templateMap.get("testTemplateNotTestPlan");
        Template testTemplate3 = templateMap.get("testTemplate");

        assertNotEquals(testTemplate1, testTemplate2);
        assertNotEquals(testTemplate1, testTemplate3);
        assertNotEquals(testTemplate2, testTemplate1);
        assertNotEquals(testTemplate2, testTemplate3);
        assertNotEquals(testTemplate3, testTemplate1);
        assertNotEquals(testTemplate3, testTemplate2);
    }

    @Test
    public void testSameTemplatesAreEqual() {
        Template template = templateMap.get("testTemplateWithParameters");
        assertEquals(template, template);
        assertEquals(template.hashCode(), template.hashCode());
    }

    @Test
    public void testSameButParamsTemplatesAreNotEqual() {
        Template template1 = readTemplateFromFile().get("testTemplateWithParameters");
        Template template2 = readTemplateFromFile().get("testTemplateWithParameters");
        template2.setParameters(Collections.singletonMap("key", "value"));
        assertNotEquals(template1, template2);
    }
}
