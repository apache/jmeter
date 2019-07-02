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

/**
 *
 * Abstract Function initialized by key and parameters count
 *
 * @since 5.0
 *
 */
abstract class AbstractFunctionByKey extends AbstractFunction {

    private final String key;
    private final int parametersCount;

    private Object[] values;

    public AbstractFunctionByKey(String key, int parametersCount) {
        this.key = key;
        this.parametersCount = parametersCount;
    }

    public void setParameters(Collection<CompoundVariable> parameters, Integer min, Integer max)
            throws InvalidVariableException {
        checkParameterCount(parameters, min, max);
        values = parameters.toArray();
    }

    /** {@inheritDoc} */
    @Override
    public String getReferenceKey() {
        return key;
    }

    protected final Object[] getParameterValues() {
        return values;
    }

    /** {@inheritDoc} */
    @Override
    public void setParameters(Collection<CompoundVariable> parameters) throws InvalidVariableException {
        checkParameterCount(parameters, parametersCount);
    }
}
