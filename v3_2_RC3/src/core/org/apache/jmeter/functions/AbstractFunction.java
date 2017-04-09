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

import java.util.Collection;

import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;

/**
 * Provides common methods for all functions
 */
public abstract class AbstractFunction implements Function {

    /**
     * <p><b>
     * N.B. execute() should be synchronized if function is operating with non-thread-safe
     * objects (e.g. operates with files).
     * </b></p>
     * JMeter ensures setParameters() happens-before execute(): setParameters is executed in main thread,
     * and worker threads are started after that.
     * @see Function#execute(SampleResult, Sampler)
     */
    @Override
    abstract public String execute(SampleResult previousResult, Sampler currentSampler) throws InvalidVariableException;

    public String execute() throws InvalidVariableException {
        JMeterContext context = JMeterContextService.getContext();
        SampleResult previousResult = context.getPreviousResult();
        Sampler currentSampler = context.getCurrentSampler();
        return execute(previousResult, currentSampler);
    }

    /** 
     * Note: This is always called even if no parameters are provided
     * (versions of JMeter after 2.3.1)
     * 
     * @see Function#setParameters(Collection)
     */
    @Override
    abstract public void setParameters(Collection<CompoundVariable> parameters) throws InvalidVariableException;

    /**
     * @see Function#getReferenceKey()
     */
    @Override
    abstract public String getReferenceKey();

    /**
     * Gives access to the JMeter variables for the current thread.
     * 
     * @return a pointer to the JMeter variables.
     */
    protected JMeterVariables getVariables() {
        return JMeterContextService.getContext().getVariables();
    }

    /**
     * Utility method to check parameter counts.
     *
     * @param parameters collection of parameters
     * @param min minimum number of parameters allowed
     * @param max maximum number of parameters allowed
     *
     * @throws InvalidVariableException if the number of parameters is incorrect
     */
    protected void checkParameterCount(Collection<CompoundVariable> parameters, int min, int max)
        throws InvalidVariableException
    {
        int num = parameters.size();
        if ((num > max) || (num < min)) {
            throw new InvalidVariableException(
                    getReferenceKey() +
                    " called with wrong number of parameters. Actual: "+num+
                    (
                        min==max ?
                        ". Expected: "+min+"."
                        : ". Expected: >= "+min+" and <= "+max
                    )
                    );
        }
    }

    /**
     * Utility method to check parameter counts.
     *
     * @param parameters collection of parameters
     * @param count number of parameters expected
     *
     * @throws InvalidVariableException if the number of parameters is incorrect
     */
    protected void checkParameterCount(Collection<CompoundVariable> parameters, int count)
        throws InvalidVariableException
    {
        int num = parameters.size();
        if (num != count) {
            throw new InvalidVariableException(
                    getReferenceKey() +
                    " called with wrong number of parameters. Actual: "+num+". Expected: "+count+"."
                   );
        }
    }

    /**
     * Utility method to check parameter counts.
     *
     * @param parameters collection of parameters
     * @param minimum number of parameters expected
     *
     * @throws InvalidVariableException if the number of parameters is incorrect
     */
    protected void checkMinParameterCount(Collection<CompoundVariable> parameters, int minimum)
        throws InvalidVariableException
    {
        int num = parameters.size();
        if (num < minimum) {
            throw new InvalidVariableException(
                    getReferenceKey() +
                    " called with wrong number of parameters. Actual: "+num+". Expected at least: "+minimum+"."
                   );
        }
    }
}
