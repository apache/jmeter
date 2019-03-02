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

package org.apache.jmeter.protocol.http.modifier;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.jmeter.config.Argument;
import org.apache.jmeter.processor.PreProcessor;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.threads.JMeterVariables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This component allows you to specify reference name of a regular expression that extracts names and values of HTTP request parameters.
 * Regular expression group numbers must be specified for parameter's name and also for parameter's value.
 * Replacement will only occur for parameters in the Sampler that uses this RegEx User Parameters which name matches
 */
public class RegExUserParameters extends AbstractTestElement implements Serializable, PreProcessor {
    private static final String REGEX_GROUP_SUFFIX = "_g";

    private static final String MATCH_NR = "matchNr";

    /**
     *
     */
    private static final long serialVersionUID = 5486502839185386121L;

    private static final Logger log = LoggerFactory.getLogger(RegExUserParameters.class);

    public static final String REG_EX_REF_NAME = "RegExUserParameters.regex_ref_name";// $NON-NLS-1$

    public static final String REG_EX_PARAM_NAMES_GR_NR = "RegExUserParameters.param_names_gr_nr";// $NON-NLS-1$

    public static final String REG_EX_PARAM_VALUES_GR_NR = "RegExUserParameters.param_values_gr_nr";// $NON-NLS-1$

    @Override
    public void process() {
        if (log.isDebugEnabled()) {
            log.debug(Thread.currentThread().getName() + " Running up named: " + getName());//$NON-NLS-1$
        }
        Sampler entry = getThreadContext().getCurrentSampler();
        if (!(entry instanceof HTTPSamplerBase)) {
            return;
        }

        Map<String, String> paramMap = buildParamsMap();
        if(paramMap == null || paramMap.isEmpty()){
            log.info(
                    "RegExUserParameters element: {} => Referenced RegExp was not found, no parameter will be changed",
                    getName());
            return;
        }

        HTTPSamplerBase sampler = (HTTPSamplerBase) entry;
        for (JMeterProperty jMeterProperty : sampler.getArguments()) {
            Argument arg = (Argument) jMeterProperty.getObjectValue();
            String oldValue = arg.getValue();
            // if parameter name exists in http request
            // then change its value with value obtained with regular expression
            String val = paramMap.get(arg.getName());
            if (val != null) {
                arg.setValue(val);
            }
            if (log.isDebugEnabled()) {
                log.debug(
                        "RegExUserParameters element: {} => changed parameter: {} = {}, was: {}",
                        getName(), arg.getName(), arg.getValue(), oldValue);
            }
        }
    }

    private Map<String, String> buildParamsMap(){
        String regExRefName = getRegExRefName()+"_";
        String grNames = getRegParamNamesGrNr();
        String grValues = getRegExParamValuesGrNr();
        JMeterVariables jmvars = getThreadContext().getVariables();
        // verify if regex groups exists
        if(jmvars.get(regExRefName + MATCH_NR) == null
                || jmvars.get(regExRefName + 1 + REGEX_GROUP_SUFFIX + grNames) == null
                || jmvars.get(regExRefName + 1 + REGEX_GROUP_SUFFIX + grValues) == null){
            return null;
        }
        int n = Integer.parseInt(jmvars.get(regExRefName + MATCH_NR));
        Map<String, String> map = new HashMap<>(n);
        for(int i=1; i<=n; i++){
            map.put(jmvars.get(regExRefName + i + REGEX_GROUP_SUFFIX + grNames),
                    jmvars.get(regExRefName + i + REGEX_GROUP_SUFFIX + grValues));
        }
        return map;
    }

    /**
     * A new instance is created for each thread group, and the
     * clone() method is then called to create copies for each thread in a
     * thread group.
     */
    @Override
    public Object clone() {
        RegExUserParameters up = (RegExUserParameters) super.clone();
        return up;
    }

    public void setRegExRefName(String str) {
        setProperty(REG_EX_REF_NAME, str);
    }

    public String getRegExRefName() {
        return getPropertyAsString(REG_EX_REF_NAME);
    }

    public void setRegExParamNamesGrNr(String str) {
        setProperty(REG_EX_PARAM_NAMES_GR_NR, str);
    }

    public String getRegParamNamesGrNr() {
        return getPropertyAsString(REG_EX_PARAM_NAMES_GR_NR);
    }

    public void setRegExParamValuesGrNr(String str) {
        setProperty(REG_EX_PARAM_VALUES_GR_NR, str);
    }

    public String getRegExParamValuesGrNr() {
        return getPropertyAsString(REG_EX_PARAM_VALUES_GR_NR);
    }
}
