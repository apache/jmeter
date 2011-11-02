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

import java.util.Iterator;
import java.util.Map;

import org.apache.jmeter.functions.InvalidVariableException;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.StringProperty;
import org.apache.jmeter.util.StringUtilities;

public class UndoVariableReplacement extends AbstractTransformer {
    public UndoVariableReplacement(CompoundVariable masterFunction, Map<String, String> variables) {
        super();
        setMasterFunction(masterFunction);
        setVariables(variables);
    }

    public JMeterProperty transformValue(JMeterProperty prop) throws InvalidVariableException {
        Iterator<String> iter = getVariables().keySet().iterator();
        String input = prop.getStringValue();
        while (iter.hasNext()) {
            String key = iter.next();
            String value = getVariables().get(key);
            input = StringUtilities.substitute(input, "${" + key + "}", value);
        }
        StringProperty newProp = new StringProperty(prop.getName(), input);
        return newProp;
    }
}
