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

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.util.JMeterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DateConvert function to change date format
 * Can optionally store it in a variable.
 * 
 * @since 4.0
 *
 */
public class DateTimeConvertFunction extends AbstractFunction {
    private static final Logger log = LoggerFactory.getLogger(DateTimeConvertFunction.class);

    private static final List<String> desc = new LinkedList<>();
    private static final String KEY = "__dateTimeConvert";

    // Number of parameters expected - used to reject invalid calls
    private static final int MIN_PARAMETER_COUNT = 3;
    private static final int MAX_PARAMETER_COUNT = 4;

    static {
        desc.add(JMeterUtils.getResString("date_string"));
        desc.add(JMeterUtils.getResString("date_format_old"));
        desc.add(JMeterUtils.getResString("date_format_new"));
        desc.add(JMeterUtils.getResString("function_name_paropt"));
    }

    private CompoundVariable[] values;

    @Override
    public List<String> getArgumentDesc() {
        return desc;
    }

    @Override
    public String execute(SampleResult previousResult, Sampler currentSampler) throws InvalidVariableException {
        String dateString = values[0].execute();
        String sourceDateFormat = values[1].execute();
        String targetDateFormat = values[2].execute();
        try {
            SimpleDateFormat sourceDateFormatter = new SimpleDateFormat(sourceDateFormat);
            SimpleDateFormat targetDateFormatter = new SimpleDateFormat(targetDateFormat);
            String newDate = targetDateFormatter.format(sourceDateFormatter.parse(dateString));
            addVariableValue(newDate, values, 3);
            return newDate;
        } catch (Exception e) {
            log.error("Error calling {} function with value {}, source format {}, target format {}, ", KEY, dateString,
                    sourceDateFormat, targetDateFormat, e);
        }
        return ""; ////$NON-NLS-1$
    }

    @Override
    public void setParameters(Collection<CompoundVariable> parameters) throws InvalidVariableException {
        checkParameterCount(parameters, MIN_PARAMETER_COUNT, MAX_PARAMETER_COUNT);
        values = parameters.toArray(new CompoundVariable[parameters.size()]);
    }

    @Override
    public String getReferenceKey() {
        return KEY;
    }
}
