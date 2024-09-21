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

package org.apache.jmeter.functions;

import java.util.ArrayList;
import java.util.List;

import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.threads.AbstractThreadGroup;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;

import com.google.auto.service.AutoService;

/**
 * Returns Thread Group Name
 *
 * @since 5.0
 */
@AutoService(Function.class)
public class ThreadGroupName extends AbstractFunctionByKey {
    private static final String KEY = "__threadGroupName"; //$NON-NLS-1$

    private static final List<String> DESC = new ArrayList<>();

    public ThreadGroupName() {
        super(KEY, 0); //$NON-NLS-1$
    }

    /**
     * Get current thread group using sampler's context
     */
    @Override
    public String execute(SampleResult previousResult, Sampler currentSampler) throws InvalidVariableException {
        JMeterContext context;
        if (currentSampler != null) {
            context = currentSampler.getThreadContext();
        } else {
            context = JMeterContextService.getContext();
        }
        AbstractThreadGroup threadGroup = context.getThreadGroup();
        if (threadGroup != null) {
            return threadGroup.getName();
        } else {
            // Can happen if called from GUI or from non test threads
            return "";
        }
    }

    /** {@inheritDoc} */
    @Override
    public List<String> getArgumentDesc() {
        return DESC;
    }
}
