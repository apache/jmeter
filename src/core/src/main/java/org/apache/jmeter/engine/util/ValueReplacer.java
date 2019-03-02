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

package org.apache.jmeter.engine.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.jmeter.functions.InvalidVariableException;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.MultiProperty;
import org.apache.jmeter.testelement.property.NumberProperty;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jmeter.testelement.property.StringProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Perform replacement of ${variable} references.
 */
public class ValueReplacer {
    private static final Logger log = LoggerFactory.getLogger(ValueReplacer.class);

    private final CompoundVariable masterFunction = new CompoundVariable();

    private Map<String, String> variables = new HashMap<>();

    public ValueReplacer() {
    }

    /**
     * Constructor which couples the given {@link TestPlan} to this by means of the user defined variables
     * @param tp {@link TestPlan} from which we will take the user defined variables as variables map
     */
    public ValueReplacer(TestPlan tp) {
        setUserDefinedVariables(tp.getUserDefinedVariables());
    }

    boolean containsKey(String k){
        return variables.containsKey(k);
    }

    /**
     * Set this {@link ValueReplacer}'s variable map
     * @param variables Map which stores the variables
     */
    public void setUserDefinedVariables(Map<String, String> variables) {
        this.variables = variables;
    }

    /**
     * Replaces TestElement StringProperties containing functions with their Function properties equivalent, example:
     * ${__time()}_${__threadNum()}_${__machineName()} will become a FunctionProperty of
     * a CompoundVariable containing  3 functions
     * @param el {@link TestElement} in which the values should be replaced
     * @throws InvalidVariableException when transforming of the variables goes awry and
     * the used transformer throws an {@link InvalidVariableException}
     */
    public void replaceValues(TestElement el) throws InvalidVariableException {
        Collection<JMeterProperty> newProps = replaceValues(el.propertyIterator(), new ReplaceStringWithFunctions(masterFunction,
                variables));
        setProperties(el, newProps);
    }

    private void setProperties(TestElement el, Collection<JMeterProperty> newProps) {
        el.clear();
        for (JMeterProperty jmp : newProps) {
            el.setProperty(jmp);
        }
    }

    /**
     * Transforms strings into variable references
     * @param el {@link TestElement} in which the we will look for strings, that can be replaced by variable references
     * @throws InvalidVariableException  when transforming of the strings goes awry and
     * the used transformer throws an {@link InvalidVariableException}
     */
    public void reverseReplace(TestElement el) throws InvalidVariableException {
        Collection<JMeterProperty> newProps = replaceValues(el.propertyIterator(), new ReplaceFunctionsWithStrings(masterFunction,
                variables));
        setProperties(el, newProps);
    }

    /**
     * Transforms strings into variable references using regexp matching if regexMatch is <code>true</code>
     * @param el {@link TestElement} in which the we will look for strings, that can be replaced by variable references
     * @param regexMatch when <code>true</code> variable substitution will be done in regexp matching mode
     * @throws InvalidVariableException  when transforming of the strings goes awry and
     * the used transformer throws an {@link InvalidVariableException}
     */
    public void reverseReplace(TestElement el, boolean regexMatch) throws InvalidVariableException {
        Collection<JMeterProperty> newProps = replaceValues(el.propertyIterator(), new ReplaceFunctionsWithStrings(masterFunction,
                variables, regexMatch));
        setProperties(el, newProps);
    }

    /**
     * Replaces ${key} by value extracted from variables if any
     * @param el {@link TestElement} in which values should be replaced
     * @throws InvalidVariableException when transforming of the variables goes awry and
     * the used transformer throws an {@link InvalidVariableException}
     */
    public void undoReverseReplace(TestElement el) throws InvalidVariableException {
        Collection<JMeterProperty> newProps = replaceValues(el.propertyIterator(), new UndoVariableReplacement(masterFunction,
                variables));
        setProperties(el, newProps);
    }

    /**
     * Add a variable to this replacer's variables map
     * @param name Name of the variable
     * @param value Value of the variable
     */
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
     * Replaces a {@link StringProperty} containing functions with their Function properties equivalent.
     * <p>For example:
     * <code>${__time()}_${__threadNum()}_${__machineName()}</code> will become a
     * {@link org.apache.jmeter.testelement.property.FunctionProperty} of
     * a {@link CompoundVariable} containing three functions
     * @param iter the {@link PropertyIterator} over all properties, in which the values should be replaced
     * @param transform the {@link ValueTransformer}, that should do transformation
     * @return a new {@link Collection} with all the transformed {@link JMeterProperty}s
     * @throws InvalidVariableException when <code>transform</code> throws an {@link InvalidVariableException} while transforming a value
     */
    private Collection<JMeterProperty> replaceValues(PropertyIterator iter, ValueTransformer transform) throws InvalidVariableException {
        List<JMeterProperty> props = new LinkedList<>();
        while (iter.hasNext()) {
            JMeterProperty val = iter.next();
            if (log.isDebugEnabled()) {
                log.debug("About to replace in property of type: {}: {}", val.getClass(), val);
            }
            if (val instanceof StringProperty) {
                // Must not convert TestElement.gui_class etc
                if (!val.getName().equals(TestElement.GUI_CLASS) &&
                        !val.getName().equals(TestElement.TEST_CLASS)) {
                    val = transform.transformValue(val);
                    log.debug("Replacement result: {}", val);
                }
            } else if (val instanceof NumberProperty) {
                val = transform.transformValue(val);
                log.debug("Replacement result: {}", val);
            } else if (val instanceof MultiProperty) {
                MultiProperty multiVal = (MultiProperty) val;
                Collection<JMeterProperty> newValues = replaceValues(multiVal.iterator(), transform);
                multiVal.clear();
                for (JMeterProperty jmp : newValues) {
                    multiVal.addProperty(jmp);
                }
                log.debug("Replacement result: {}", multiVal);
            } else {
                log.debug("Won't replace {}", val);
            }
            props.add(val);
        }
        return props;
    }
}
