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
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import javax.script.Bindings;
import javax.script.ScriptEngine;

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.util.JSR223TestElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * __groovy function
 * Provides a Groovy interpreter
 * @since 3.1
 */
public class Groovy extends AbstractFunction {
    private static final Logger log = LoggerFactory.getLogger(Groovy.class);

    private static final String GROOVY_ENGINE_NAME = "groovy";

    private static final List<String> DESCRIPTION = new LinkedList<>();

    private static final String KEY = "__groovy"; //$NON-NLS-1$

    public static final String INIT_FILE = "groovy.utilities"; //$NON-NLS-1$

    static {
        DESCRIPTION.add(JMeterUtils.getResString("groovy_function_expression"));// $NON-NLS1$
        DESCRIPTION.add(JMeterUtils.getResString("function_name_paropt"));// $NON-NLS1$
    }

    private Object[] values;
    private ScriptEngine scriptEngine;


    public Groovy() {
        super();
    }

    /**
     * Populate variables to be passed to scripts
     * @param bindings Bindings
     */
    protected void populateBindings(Bindings bindings) {
        // NOOP
    }

    /** {@inheritDoc} */
    @Override
    public synchronized String execute(SampleResult previousResult, Sampler currentSampler)
            throws InvalidVariableException {
        Bindings bindings = scriptEngine.createBindings();
        populateBindings(bindings);


        String script = ((CompoundVariable) values[0]).execute();
        String varName = ""; //$NON-NLS-1$
        if (values.length > 1) {
            varName = ((CompoundVariable) values[1]).execute().trim();
        }

        String resultStr = ""; //$NON-NLS-1$
        try {

            // Pass in some variables
            if (currentSampler != null) {
                bindings.put("sampler", currentSampler); // $NON-NLS-1$
            }

            if (previousResult != null) {
                bindings.put("prev", previousResult); //$NON-NLS-1$
            }
            bindings.put("log", log); // $NON-NLS-1$ (this name is fixed)
            // Add variables for access to context and variables
            bindings.put("threadName", Thread.currentThread().getName());
            JMeterContext jmctx = JMeterContextService.getContext();
            bindings.put("ctx", jmctx); // $NON-NLS-1$ (this name is fixed)
            JMeterVariables vars = jmctx.getVariables();
            bindings.put("vars", vars); // $NON-NLS-1$ (this name is fixed)
            Properties props = JMeterUtils.getJMeterProperties();
            bindings.put("props", props); // $NON-NLS-1$ (this name is fixed)
            // For use in debugging:
            bindings.put("OUT", System.out); // $NON-NLS-1$ (this name is fixed)


            // Execute the script
            Object out = scriptEngine.eval(script, bindings);
            if (out != null) {
                resultStr = out.toString();
            }

            if (varName.length() > 0 && vars != null) {// vars will be null on TestPlan
                vars.put(varName, resultStr);
            }
        } catch (Exception ex) // Mainly for bsh.EvalError
        {
            log.warn("Error running groovy script", ex);
        }
        log.debug("__groovy({},{})={}",script, varName, resultStr);
        return resultStr;
    }

    /** {@inheritDoc} */
    @Override
    public void setParameters(Collection<CompoundVariable> parameters) throws InvalidVariableException {
        checkParameterCount(parameters, 1, 2);
        values = parameters.toArray();
        scriptEngine = JSR223TestElement.getInstance().getEngineByName(GROOVY_ENGINE_NAME); //$NON-NLS-N$

        String fileName = JMeterUtils.getProperty(INIT_FILE);
        if(!StringUtils.isEmpty(fileName)) {
            File file = new File(fileName);
            if(!(file.exists() && file.canRead())) {
                // File maybe relative to JMeter home
                File oldFile = file;
                file = new File(JMeterUtils.getJMeterHome(), fileName);
                if(!(file.exists() && file.canRead())) {
                    throw new InvalidVariableException("Cannot read file, neither from:"+oldFile.getAbsolutePath()+
                            ", nor from:"+file.getAbsolutePath()+", check property '"+INIT_FILE+"'");
                }
            }
            try (Reader reader = Files.newBufferedReader(file.toPath(), Charset.defaultCharset())) {
                Bindings bindings = scriptEngine.createBindings();
                bindings.put("log", log);
                scriptEngine.eval(reader, bindings);
            } catch(Exception ex) {
                throw new InvalidVariableException("Failed loading script:"+file.getAbsolutePath(), ex);
            }
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
        return DESCRIPTION;
    }
}
