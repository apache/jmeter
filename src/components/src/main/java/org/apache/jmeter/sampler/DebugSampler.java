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

package org.apache.jmeter.sampler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.gui.GUIMenuSortOrder;
import org.apache.jmeter.gui.TestElementMetadata;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.schema.PropertiesAccessor;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.util.AlphaNumericKeyComparator;

/**
 * The Debug Sampler can be used to "sample" JMeter variables, JMeter properties and System Properties.
 */
@GUIMenuSortOrder(2)
@TestElementMetadata(labelResource = "displayName")
public class DebugSampler extends AbstractSampler implements TestBean {

    private static final long serialVersionUID = 232L;

    private static final Set<String> APPLIABLE_CONFIG_CLASSES = new HashSet<>(
            Arrays.asList("org.apache.jmeter.config.gui.SimpleConfigGui"));

    private boolean displayJMeterVariables;
    private boolean displayJMeterProperties;
    private boolean displaySystemProperties;

    @Override
    public DebugSamplerSchema getSchema() {
        return DebugSamplerSchema.INSTANCE;
    }

    @Override
    public PropertiesAccessor<? extends DebugSampler, ? extends DebugSamplerSchema> getProps() {
        return new PropertiesAccessor<>(this, getSchema());
    }

    @Override
    public SampleResult sample(Entry e) {
        SampleResult res = new SampleResult();
        res.setSampleLabel(getName());
        res.sampleStart();
        StringBuilder sb = new StringBuilder(100);
        StringBuilder rd = new StringBuilder(20); // for request Data
        if (isDisplayJMeterVariables()){
            rd.append("JMeterVariables\n");
            sb.append("JMeterVariables:\n");
            formatSet(sb, JMeterContextService.getContext().getVariables().entrySet());
            sb.append("\n");
        }

        if (isDisplayJMeterProperties()){
            rd.append("JMeterProperties\n");
            sb.append("JMeterProperties:\n");
            formatSet(sb, JMeterUtils.getJMeterProperties().entrySet());
            sb.append("\n");
        }

        if (isDisplaySystemProperties()){
            rd.append("SystemProperties\n");
            sb.append("SystemProperties:\n");
            formatSet(sb, System.getProperties().entrySet());
            sb.append("\n");
        }

        res.setResponseData(sb.toString(), null);
        res.setDataType(SampleResult.TEXT);
        res.setSamplerData(rd.toString());
        res.setResponseOK();
        res.sampleEnd();
        return res;
    }

    private static void formatSet(StringBuilder sb, @SuppressWarnings("rawtypes") Set s) {
        @SuppressWarnings("unchecked")
        List<Map.Entry<Object, Object>> al = new ArrayList<>(s);
        al.sort(AlphaNumericKeyComparator.INSTANCE);
        al.forEach(me -> sb.append(me.getKey()).append("=").append(me.getValue()).append("\n"));
    }

    public boolean isDisplayJMeterVariables() {
        return displayJMeterVariables;
    }

    public void setDisplayJMeterVariables(boolean displayJMeterVariables) {
        this.displayJMeterVariables = displayJMeterVariables;
    }

    public boolean isDisplayJMeterProperties() {
        return displayJMeterProperties;
    }

    public void setDisplayJMeterProperties(boolean displayJMeterProperties) {
        this.displayJMeterProperties = displayJMeterProperties;
    }

    public boolean isDisplaySystemProperties() {
        return displaySystemProperties;
    }

    public void setDisplaySystemProperties(boolean displaySystemProperties) {
        this.displaySystemProperties = displaySystemProperties;
    }

    /**
     * @see org.apache.jmeter.samplers.AbstractSampler#applies(org.apache.jmeter.config.ConfigTestElement)
     */
    @Override
    public boolean applies(ConfigTestElement configElement) {
        String guiClass = configElement.getProperty(TestElement.GUI_CLASS).getStringValue();
        return APPLIABLE_CONFIG_CLASSES.contains(guiClass);
    }
}
