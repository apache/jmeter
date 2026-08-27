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

package org.apache.jmeter.protocol.http.config.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBaseSchema;
import org.apache.jmeter.protocol.http.util.HTTPConstants;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.NullProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestHttpDefaultsGui {
    private HttpDefaultsGui gui;

    @BeforeEach
    public void setUp() {
        gui = new HttpDefaultsGui();
    }

    @Test
    public void testCreateTestElementDoesNotSetEmptyHttpVersion() {
        ConfigTestElement config = (ConfigTestElement) gui.createTestElement();
        JMeterProperty prop = config.getProperty(HTTPSamplerBaseSchema.INSTANCE.getHttpVersion().getName());
        assertInstanceOf(NullProperty.class, prop, "httpVersion property should not be present on default ConfigTestElement");
    }

    @Test
    public void testModifyTestElementNormalizesBlankHttpVersionToNull() {
        ConfigTestElement config = new ConfigTestElement();
        gui.modifyTestElement(config);
        JMeterProperty prop = config.getProperty(HTTPSamplerBaseSchema.INSTANCE.getHttpVersion().getName());
        assertInstanceOf(NullProperty.class, prop, "httpVersion property should not be present when selection is blank");
    }

    @Test
    public void testClearGuiResetsHttpVersionAndImplementation() {
        ConfigTestElement config = (ConfigTestElement) gui.createTestElement();
        config.set(HTTPSamplerBaseSchema.INSTANCE.getImplementation(), "HttpClient5");
        config.set(HTTPSamplerBaseSchema.INSTANCE.getHttpVersion(), HTTPConstants.HTTP_VERSION_2);
        gui.configure(config);

        gui.clearGui();

        ConfigTestElement clearedConfig = (ConfigTestElement) gui.createTestElement();
        gui.modifyTestElement(clearedConfig);
        JMeterProperty versionProp = clearedConfig.getProperty(HTTPSamplerBaseSchema.INSTANCE.getHttpVersion().getName());
        assertInstanceOf(NullProperty.class, versionProp, "httpVersion property should be null/removed after clearGui");
        JMeterProperty implProp = clearedConfig.getProperty(HTTPSamplerBaseSchema.INSTANCE.getImplementation().getName());
        assertInstanceOf(NullProperty.class, implProp, "implementation property should be null/removed after clearGui");
    }

    @Test
    public void testModifyTestElementHandlesNullSelectionWithoutSettingLiteralNullString() {
        ConfigTestElement config = (ConfigTestElement) gui.createTestElement();
        // Emulate null selection in the comboboxes if any
        gui.clearGui();
        gui.modifyTestElement(config);
        JMeterProperty versionProp = config.getProperty(HTTPSamplerBaseSchema.INSTANCE.getHttpVersion().getName());
        assertInstanceOf(NullProperty.class, versionProp, "httpVersion property should not be present as 'null' string");
        JMeterProperty implProp = config.getProperty(HTTPSamplerBaseSchema.INSTANCE.getImplementation().getName());
        assertInstanceOf(NullProperty.class, implProp, "implementation property should not be present as 'null' string");
    }
}
