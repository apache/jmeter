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

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.JMeterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * FileToString Function to read a complete file into a String.
 *
 * Parameters:
 * - file name
 * - file encoding (optional)
 * - variable name (optional)
 *
 * Returns:
 * - the whole text from a file
 * - or **ERR** if an error occurs
 * - value is also optionally saved in the variable for later re-use.
 * @since 2.4
 */
public class FileToString extends AbstractFunction {
    private static final Logger log = LoggerFactory.getLogger(FileToString.class);

    private static final List<String> desc = new LinkedList<>();

    private static final String KEY = "__FileToString";//$NON-NLS-1$

    static final String ERR_IND = "**ERR**";//$NON-NLS-1$

    static {
        desc.add(JMeterUtils.getResString("string_from_file_file_name"));//$NON-NLS-1$
        desc.add(JMeterUtils.getResString("string_from_file_encoding"));//$NON-NLS-1$
        desc.add(JMeterUtils.getResString("function_name_paropt"));//$NON-NLS-1$
    }

    private static final int MIN_PARAM_COUNT = 1;

    private static final int MAX_PARAM_COUNT = 3;

    private static final int ENCODING = 2;

    private static final int PARAM_NAME = 3;

    private Object[] values;

    public FileToString() {
    }

    /** {@inheritDoc} */
    @Override
    public String execute(SampleResult previousResult, Sampler currentSampler)
            throws InvalidVariableException {

        String fileName = ((CompoundVariable) values[0]).execute();

        String encoding = null;//means platform default
        if (values.length >= ENCODING) {
            encoding = ((CompoundVariable) values[ENCODING - 1]).execute().trim();
            if (encoding.length() <= 0) { // empty encoding, return to platform default
                encoding = null;
            }
        }

        String myName = "";//$NON-NLS-1$
        if (values.length >= PARAM_NAME) {
            myName = ((CompoundVariable) values[PARAM_NAME - 1]).execute().trim();
        }

        String myValue = ERR_IND;

        try {
            File file = new File(fileName);
            if(file.exists() && file.canRead()) {
                myValue = FileUtils.readFileToString(new File(fileName), encoding);
            } else {
                log.warn("Could not read open: "+fileName+" ");
            }
        } catch (IOException e) {
            log.warn("Could not read file: "+fileName+" "+e.getMessage(), e);
        }

        if (myName.length() > 0) {
            JMeterVariables vars = getVariables();
            if (vars != null) {// Can be null if called from Config item testEnded() method
                vars.put(myName, myValue);
            }
        }

        if (log.isDebugEnabled()) {
            String tn = Thread.currentThread().getName();
            log.debug(tn + " name:" //$NON-NLS-1$
                    + myName + " value:" + myValue);//$NON-NLS-1$
        }

        return myValue;
    }


    /** {@inheritDoc} */
    @Override
    public void setParameters(Collection<CompoundVariable> parameters) throws InvalidVariableException {
        checkParameterCount(parameters, MIN_PARAM_COUNT, MAX_PARAM_COUNT);
        values = parameters.toArray();
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
