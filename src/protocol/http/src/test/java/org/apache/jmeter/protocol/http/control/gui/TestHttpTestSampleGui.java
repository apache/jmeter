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

package org.apache.jmeter.protocol.http.control.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBaseSchema;
import org.apache.jmeter.protocol.http.util.HTTPConstants;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.NullProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestHttpTestSampleGui {
    private HttpTestSampleGui gui;

    @BeforeEach
    public void setUp() {
        gui = new HttpTestSampleGui();
    }

    @Test
    public void testCloneSampler() throws Exception {
        HTTPSamplerBase sampler = (HTTPSamplerBase) gui.createTestElement();
        sampler.addArgument("param", "value");
        HTTPSamplerBase clonedSampler = (HTTPSamplerBase) sampler.clone();
        clonedSampler.setRunningVersion(true);
        sampler.getArguments().getArgument(0).setValue("new value");
        assertEquals("new value", sampler.getArguments().getArgument(0).getValue(), "Sampler didn't clone correctly");
    }

    @Test
    public void testCreateTestElementDoesNotSetEmptyHttpVersion() {
        HTTPSamplerBase sampler = (HTTPSamplerBase) gui.createTestElement();
        JMeterProperty prop = sampler.getProperty(HTTPSamplerBaseSchema.INSTANCE.getHttpVersion().getName());
        assertInstanceOf(NullProperty.class, prop, "httpVersion property should not be present on default HTTPSamplerBase");
    }

    @Test
    public void testClearGuiResetsHttpVersionAndImplementation() {
        HTTPSamplerBase sampler = (HTTPSamplerBase) gui.createTestElement();
        sampler.set(HTTPSamplerBaseSchema.INSTANCE.getImplementation(), "HttpClient5");
        sampler.set(HTTPSamplerBaseSchema.INSTANCE.getHttpVersion(), HTTPConstants.HTTP_VERSION_2);
        gui.configure(sampler);

        gui.clearGui();

        HTTPSamplerBase clearedSampler = (HTTPSamplerBase) gui.createTestElement();
        gui.modifyTestElement(clearedSampler);
        JMeterProperty versionProp = clearedSampler.getProperty(HTTPSamplerBaseSchema.INSTANCE.getHttpVersion().getName());
        assertInstanceOf(NullProperty.class, versionProp, "httpVersion property should be null/removed after clearGui");
        JMeterProperty implProp = clearedSampler.getProperty(HTTPSamplerBaseSchema.INSTANCE.getImplementation().getName());
        assertInstanceOf(NullProperty.class, implProp, "implementation property should be null/removed after clearGui");
    }
}
