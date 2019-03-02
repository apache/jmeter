/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.apache.jmeter.extractor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jmeter.processor.PostProcessor;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.util.JMeterUtils;

/**
 * Debugging Post-Processor: creates a subSample containing the variables defined in the previous sampler.
 */
public class DebugPostProcessor extends AbstractTestElement implements PostProcessor, TestBean {

    private static final long serialVersionUID = 260L;

    private boolean displaySamplerProperties;

    private boolean displayJMeterVariables;

    private boolean displayJMeterProperties;

    private boolean displaySystemProperties;

    @Override
    public void process(){
        StringBuilder sb = new StringBuilder(100);
        StringBuilder rd = new StringBuilder(20); // for request Data
        SampleResult sr = new SampleResult();
        sr.setSampleLabel(getName());
        sr.sampleStart();
        JMeterContext threadContext = getThreadContext();
        if (isDisplaySamplerProperties()){
            rd.append("SamplerProperties\n");
            sb.append("SamplerProperties:\n");
            formatPropertyIterator(sb, threadContext.getCurrentSampler().propertyIterator());
            sb.append("\n");
        }

        if (isDisplayJMeterVariables()){
            rd.append("JMeterVariables\n");
            sb.append("JMeterVariables:\n");
            formatSet(sb, threadContext.getVariables().entrySet());
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

        sr.setResponseData(sb.toString(), null);
        sr.setDataType(SampleResult.TEXT);
        sr.setSamplerData(rd.toString());
        sr.setResponseOK();
        sr.sampleEnd();
        threadContext.getPreviousResult().addSubResult(sr);
    }

    private void formatPropertyIterator(StringBuilder sb, PropertyIterator iter) {
        Map<String, String> map = new HashMap<>();
        while (iter.hasNext()) {
            JMeterProperty item = iter.next();
            map.put(item.getName(), item.getStringValue());
        }
        formatSet(sb, map.entrySet());
    }

    private void formatSet(StringBuilder sb, @SuppressWarnings("rawtypes") Set s) {
        @SuppressWarnings("unchecked")
        List<Map.Entry<Object, Object>> al = new ArrayList<>(s);
        al.sort(
                (Map.Entry<Object, Object> o1, Map.Entry<Object, Object> o2) -> {
                String m1 = (String)o1.getKey();
                String m2 =(String)o2.getKey();
                return m1.compareTo(m2);
            });
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

    public void setDisplayJMeterProperties(boolean displayJMeterPropterties) {
        this.displayJMeterProperties = displayJMeterPropterties;
    }

    public boolean isDisplaySamplerProperties() {
        return displaySamplerProperties;
    }

    public void setDisplaySamplerProperties(boolean displaySamplerProperties) {
        this.displaySamplerProperties = displaySamplerProperties;
    }

    public boolean isDisplaySystemProperties() {
        return displaySystemProperties;
    }

    public void setDisplaySystemProperties(boolean displaySystemProperties) {
        this.displaySystemProperties = displaySystemProperties;
    }
}
