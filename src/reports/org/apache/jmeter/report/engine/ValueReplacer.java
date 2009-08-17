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

package org.apache.jmeter.report.engine;

import java.util.HashMap;
import java.util.Map;

import org.apache.jmeter.functions.InvalidVariableException;
import org.apache.jmeter.testelement.ReportPlan;
import org.apache.jmeter.testelement.TestElement;

public class ValueReplacer {
//  private static final Logger log = LoggingManager.getLoggerForClass();

    private Map<String, String> variables = new HashMap<String, String>();

    public ValueReplacer() {
    }

    public ValueReplacer(ReportPlan tp) {
        setUserDefinedVariables(tp.getUserDefinedVariables());
    }

    public void setUserDefinedVariables(Map<String, String> variables) {
        this.variables = variables;
    }

    /**
     * @throws InvalidVariableException not thrown currently 
     */
    public void replaceValues(TestElement el) throws InvalidVariableException {
        /**
        Collection newProps = replaceValues(el.propertyIterator(), new ReplaceStringWithFunctions(masterFunction,
                variables));
        setProperties(el, newProps);
        **/
    }

//    private void setProperties(TestElement el, Collection newProps) {
//        Iterator iter = newProps.iterator();
//        el.clear();
//        while (iter.hasNext()) {
//            el.setProperty((JMeterProperty) iter.next());
//        }
//    }

    /**
     * @throws InvalidVariableException not thrown currently 
     */
    public void reverseReplace(TestElement el) throws InvalidVariableException {
        /**
        Collection newProps = replaceValues(el.propertyIterator(), new ReplaceFunctionsWithStrings(masterFunction,
                variables));
        setProperties(el, newProps);
        **/
    }

    /**
     * @throws InvalidVariableException not thrown currently 
     */
    public void reverseReplace(TestElement el, boolean regexMatch) throws InvalidVariableException {
        /**
        Collection newProps = replaceValues(el.propertyIterator(), new ReplaceFunctionsWithStrings(masterFunction,
                variables, regexMatch));
        setProperties(el, newProps);
        **/
    }

    /**
     * @throws InvalidVariableException not thrown currently 
     */
    public void undoReverseReplace(TestElement el) throws InvalidVariableException {
        /**
        Collection newProps = replaceValues(el.propertyIterator(), new UndoVariableReplacement(masterFunction,
                variables));
        setProperties(el, newProps);
        **/
    }

    public void addVariable(String name, String value) {
        variables.put(name, value);
    }

    /**
     * Add all the given variables to this replacer's variables map.
     *
     * @param vars
     *            A map of variable name-value pairs (String-to-String).
     */
    public void addVariables(Map<String, String> vars) {
        variables.putAll(vars);
    }

    /**
    private Collection replaceValues(PropertyIterator iter, ValueTransformer transform) throws InvalidVariableException {
        List props = new LinkedList();
        while (iter.hasNext()) {
            JMeterProperty val = iter.next();
            if (log.isDebugEnabled()) {
                log.debug("About to replace in property of type: " + val.getClass() + ": " + val);
            }
            if (val instanceof StringProperty) {
                // Must not convert TestElement.gui_class etc
                if (!val.getName().equals(TestElement.GUI_CLASS) &&
                        !val.getName().equals(TestElement.TEST_CLASS)) {
                    val = transform.transformValue(val);
                    if (log.isDebugEnabled()) {
                        log.debug("Replacement result: " + val);
                    }
                }
            } else if (val instanceof MultiProperty) {
                MultiProperty multiVal = (MultiProperty) val;
                Collection newValues = replaceValues(multiVal.iterator(), transform);
                multiVal.clear();
                Iterator propIter = newValues.iterator();
                while (propIter.hasNext()) {
                    multiVal.addProperty((JMeterProperty) propIter.next());
                }
                if (log.isDebugEnabled()) {
                    log.debug("Replacement result: " + multiVal);
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Won't replace " + val);
                }
            }
            props.add(val);
        }
        return props;
    }
    **/
}
