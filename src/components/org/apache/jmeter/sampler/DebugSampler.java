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

package org.apache.jmeter.sampler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;

import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.util.JMeterUtils;

/**
 * The Debug Sampler can be used to "sample" JMeter variables, JMeter properties and System Properties.
 *
 */
public class DebugSampler extends AbstractSampler implements TestBean {

    private static final long serialVersionUID = 232L;

    private boolean displayJMeterVariables;

    private boolean displayJMeterProperties;

    private boolean displaySystemProperties;

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
        res.setSuccessful(true);
        res.sampleEnd();
        return res;
    }

    private void formatSet(StringBuilder sb, @SuppressWarnings("rawtypes") Set s) {
        @SuppressWarnings("unchecked")
        ArrayList<Map.Entry<Object, Object>> al = new ArrayList<Map.Entry<Object, Object>>(s);
        Collections.sort(al, new Comparator<Map.Entry<Object, Object>>(){
            public int compare(Map.Entry<Object, Object> o1, Map.Entry<Object, Object> o2) {
                String m1,m2;
                m1=(String)o1.getKey();
                m2=(String)o2.getKey();
                return m1.compareTo(m2);
            }
        });
        for(Map.Entry<Object, Object> me : al){
            sb.append(me.getKey());
            sb.append("=");
            sb.append(me.getValue());
            sb.append("\n");
        }
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

    public boolean isDisplaySystemProperties() {
        return displaySystemProperties;
    }

    public void setDisplaySystemProperties(boolean displaySystemProperties) {
        this.displaySystemProperties = displaySystemProperties;
    }
}
