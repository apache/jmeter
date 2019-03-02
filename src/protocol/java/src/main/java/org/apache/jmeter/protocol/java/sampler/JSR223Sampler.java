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

package org.apache.jmeter.protocol.java.sampler;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.engine.util.ConfigMergabilityIndicator;
import org.apache.jmeter.gui.GUIMenuSortOrder;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JSR223TestElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@GUIMenuSortOrder(4)
public class JSR223Sampler extends JSR223TestElement implements Cloneable, Sampler, TestBean, ConfigMergabilityIndicator {
    private static final Set<String> APPLIABLE_CONFIG_CLASSES = new HashSet<>(
            Arrays.asList("org.apache.jmeter.config.gui.SimpleConfigGui"));

    private static final long serialVersionUID = 235L;

    private static final Logger log = LoggerFactory.getLogger(JSR223Sampler.class);

    @Override
    public SampleResult sample(Entry entry) {
        SampleResult result = new SampleResult();
        result.setSampleLabel(getName());
        result.setSuccessful(true);
        result.setResponseCodeOK();
        result.setResponseMessageOK();

        final String filename = getFilename();
        if (filename.length() > 0){
            result.setSamplerData("File: "+filename);
        } else {
            result.setSamplerData(getScript());
        }
        result.setDataType(SampleResult.TEXT);
        result.sampleStart();
        try {
            ScriptEngine scriptEngine = getScriptEngine();
            Bindings bindings = scriptEngine.createBindings();
            bindings.put("SampleResult",result);
            Object ret = processFileOrScript(scriptEngine, bindings);
            if (ret != null && (result.getResponseData() == null || result.getResponseData().length==0)){
                result.setResponseData(ret.toString(), null);
            }
        } catch (IOException | ScriptException e) {
            log.error("Problem in JSR223 script {}, message: {}", getName(), e, e);
            result.setSuccessful(false);
            result.setResponseCode("500"); // $NON-NLS-1$
            result.setResponseMessage(e.toString());
        }
        result.sampleEnd();
        return result;
    }

    /**
     * @see org.apache.jmeter.samplers.AbstractSampler#applies(org.apache.jmeter.config.ConfigTestElement)
     */
    @Override
    public boolean applies(ConfigTestElement configElement) {
        String guiClass = configElement.getProperty(TestElement.GUI_CLASS).getStringValue();
        return APPLIABLE_CONFIG_CLASSES.contains(guiClass);
    }
    
    @Override
    public Object clone() {
        return super.clone();
    }

}
