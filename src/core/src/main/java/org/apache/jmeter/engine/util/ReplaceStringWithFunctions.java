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

/*
 * Created on May 4, 2003
 */
package org.apache.jmeter.engine.util;

import java.util.Map;

import org.apache.jmeter.functions.InvalidVariableException;
import org.apache.jmeter.testelement.property.FunctionProperty;
import org.apache.jmeter.testelement.property.JMeterProperty;

/**
 * Replaces a String containing functions with their Function properties equivalent, example:
 * ${__time()}_${__threadNum()}_${__machineName()} will become a FunctionProperty of
 * a CompoundVariable containing  3 functions
 */
public class ReplaceStringWithFunctions extends AbstractTransformer {
    public ReplaceStringWithFunctions(CompoundVariable masterFunction, Map<String, String> variables) {
        super();
        setMasterFunction(masterFunction);
        setVariables(variables);
    }

    @Override
    public JMeterProperty transformValue(JMeterProperty prop) throws InvalidVariableException {
        JMeterProperty newValue = prop;
        getMasterFunction().clear();
        getMasterFunction().setParameters(prop.getStringValue());
        if (getMasterFunction().hasFunction()) {
            newValue = new FunctionProperty(prop.getName(), getMasterFunction().getFunction());
        }
        return newValue;
    }

}
