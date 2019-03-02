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

package org.apache.jmeter.functions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.JMeterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides a RandomFromMultipleVars function which returns a random element from a multi valued extracted variable.
 * Those kind of variables are extracted by:
 * - Regular Expression extractor
 * - JSON extractor
 * - CSS Selector extractor
 * - XPath Extractor
 * 
 * @since 3.1
 */
public class RandomFromMultipleVars extends AbstractFunction {
    private static final Logger log = LoggerFactory.getLogger(RandomFromMultipleVars.class);

    private static final List<String> desc = new LinkedList<>();
    private static final String KEY = "__RandomFromMultipleVars"; //$NON-NLS-1$
    private static final String SEPARATOR = "\\|"; //$NON-NLS-1$
    static {
        desc.add(JMeterUtils.getResString("random_multi_result_source_variable")); //$NON-NLS-1$
        desc.add(JMeterUtils.getResString("random_multi_result_target_variable")); //$NON-NLS-1$
    }

    private CompoundVariable variablesNamesSplitBySeparator;
    private CompoundVariable varName;

    /**
     * No-arg constructor.
     */
    public RandomFromMultipleVars() {
        super();
    }

    /** {@inheritDoc} */
    @Override
    public String execute(SampleResult previousResult, Sampler currentSampler)
            throws InvalidVariableException {

        String variablesNamesSplitBySeparatorValue = variablesNamesSplitBySeparator.execute().trim();
        JMeterVariables vars = getVariables();
        String outputValue = "";
        String separator = "";
        if (vars != null) { // vars will be null on TestPlan
            List<String> results = new ArrayList<>();
            String[] variables = variablesNamesSplitBySeparatorValue.split(SEPARATOR);
            for (String currentVarName : variables) {
                if(!StringUtils.isEmpty(currentVarName)) {
                    extractVariableValuesToList(currentVarName, vars, results);
                }
            }

            if(!results.isEmpty()) {
                int randomIndex = ThreadLocalRandom.current().nextInt(0, results.size());
                outputValue = results.get(randomIndex);                
            } else {
                if(log.isDebugEnabled()) {
                    log.debug("RandomFromMultiResult didn't find <var>_matchNr in variables :'{}' using separator:'{}', will return empty value",
                            variablesNamesSplitBySeparatorValue, separator);
                }
            }

            if (varName != null) {
                final String varTrim = varName.execute().trim();
                if (!varTrim.isEmpty()){ 
                    vars.put(varTrim, outputValue);
                }
            }
        }    
        return outputValue;

    }

    /**
     * Get from vars the values of variableName (can be missing, contain 1 value or contain multiple values (_matchNr))
     * and stores them in results
     * @param variableName String
     * @param vars {@link JMeterVariables}
     * @param results {@link List} where results are stored
     */
    private void extractVariableValuesToList(String variableName,
            JMeterVariables vars, List<String> results) {
        String matchNumberAsStr = vars.get(variableName+"_matchNr");
        int matchNumber = 0;
        if(!StringUtils.isEmpty(matchNumberAsStr)) {
            matchNumber = Integer.parseInt(matchNumberAsStr);
        }
        if(matchNumber > 0) {
            for (int i = 1; i <= matchNumber; i++) {
                results.add(vars.get(variableName+"_"+i));
            }
        } else {
            String value = vars.get(variableName);
            if(!StringUtils.isEmpty(value)) {
                results.add(value);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void setParameters(Collection<CompoundVariable> parameters) throws InvalidVariableException {
        checkParameterCount(parameters, 1, 2);
        Object[] values = parameters.toArray();
        variablesNamesSplitBySeparator = (CompoundVariable) values[0];
        if (values.length>1){
            varName = (CompoundVariable) values[1];
        }
    }

    /** {@inheritDoc} */
    @Override
    public String getReferenceKey() {
        return KEY;
    }

    /** {@inheritDoc} */
    @Override
    public List<String> getArgumentDesc() {
        return desc;
    }
}
