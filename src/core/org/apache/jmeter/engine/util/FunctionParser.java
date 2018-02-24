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
 * Created on Jul 25, 2003
 */
package org.apache.jmeter.engine.util;

import java.io.IOException;
import java.io.StringReader;
import java.util.LinkedList;

import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.functions.Function;
import org.apache.jmeter.functions.InvalidVariableException;
import org.apache.jmeter.testelement.TestStateListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parses function / variable references of the form
 * ${functionName[([var[,var...]])]}
 * and
 * ${variableName}
 */
class FunctionParser {
    private static final Logger log = LoggerFactory.getLogger(FunctionParser.class);

    /**
     * Compile a general string into a list of elements for a CompoundVariable.
     *
     * Calls {@link #makeFunction(StringReader)} if it detects an unescaped "${".
     *
     * Removes escapes from '$', ',' and '\'.
     * 
     * @param value string containing the function / variable references (if any)
     *
     * @return list of Strings or Objects representing functions
     * @throws InvalidVariableException when evaluation of variables fail
     */
    LinkedList<Object> compileString(String value) throws InvalidVariableException {
        StringReader reader = new StringReader(value);
        LinkedList<Object> result = new LinkedList<>();
        StringBuilder buffer = new StringBuilder();
        char previous = ' '; // TODO - why use space?
        char[] current = new char[1];
        try {
            while (reader.read(current) == 1) {
                if (current[0] == '\\') { // Handle escapes
                    previous = current[0];
                    if (reader.read(current) == 0) {
                        break;
                    }
                    // Keep '\' unless it is one of the escapable chars '$' ',' or '\'
                    // N.B. This method is used to parse function parameters, so must treat ',' as special
                    if (current[0] != '$' && current[0] != ',' && current[0] != '\\') {
                        buffer.append(previous); // i.e. '\\'
                    }
                    previous = ' ';
                    buffer.append(current[0]);
                } else if (current[0] == '{' && previous == '$') {// found "${"
                    buffer.deleteCharAt(buffer.length() - 1);
                    if (buffer.length() > 0) {// save leading text
                        result.add(buffer.toString());
                        buffer.setLength(0);
                    }
                    result.add(makeFunction(reader));
                    previous = ' ';
                } else {
                    buffer.append(current[0]);
                    previous = current[0];
                }
            }

            if (buffer.length() > 0) {
                result.add(buffer.toString());
            }
        } catch (IOException e) {
            log.error("Error parsing function: {}", value, e);
            result.clear();
            result.add(value);
        }
        if (result.isEmpty()) {
            result.add("");
        }
        return result;
    }

    /**
     * Compile a string into a function or SimpleVariable.
     *
     * Called by {@link #compileString(String)} when that has detected "${".
     *
     * Calls {@link CompoundVariable#getNamedFunction(String)} if it detects:
     * '(' - start of parameter list
     * '}' - end of function call
     *
     * @param reader points to input after the "${"
     * @return the function or variable object (or a String)
     * @throws InvalidVariableException when evaluation of variables fail
     */
    Object makeFunction(StringReader reader) throws InvalidVariableException {
        char[] current = new char[1];
        char previous = ' '; // TODO - why use space?
        StringBuilder buffer = new StringBuilder();
        Object function;
        try {
            while (reader.read(current) == 1) {
                if (current[0] == '\\') {
                    if (reader.read(current) == 0) {
                        break;
                    }
                    previous = ' ';
                    buffer.append(current[0]);
                } else if (current[0] == '(' && previous != ' ') {
                    String funcName = buffer.toString();
                    function = CompoundVariable.getNamedFunction(funcName);
                    if (function instanceof Function) {
                        ((Function) function).setParameters(parseParams(reader));
                        if (reader.read(current) == 0 || current[0] != '}') {
                            reader.reset();// set to start of string
                            char []cb = new char[100];
                            int nbRead = reader.read(cb);
                            throw new InvalidVariableException("Expected } after "
                                    + funcName + " function call in " + new String(cb, 0, nbRead));
                        }
                        if (function instanceof TestStateListener) {
                            StandardJMeterEngine.register((TestStateListener) function);
                        }
                        return function;
                    } else { // Function does not exist, so treat as per missing variable
                        buffer.append(current[0]);
                    }
                } else if (current[0] == '}') {// variable, or function with no parameter list
                    function = CompoundVariable.getNamedFunction(buffer.toString());
                    if (function instanceof Function){// ensure that setParameters() is called.
                        ((Function) function).setParameters(new LinkedList<CompoundVariable>());
                    }
                    buffer.setLength(0);
                    return function;
                } else {
                    buffer.append(current[0]);
                    previous = current[0];
                }
            }
        } catch (IOException e) {
            log.error("Error parsing function: {}", buffer, e);
            return null;
        }
        log.warn("Probably an invalid function string: {}", buffer);
        return buffer.toString();
    }

    /**
     * Compile a String into a list of parameters, each made into a
     * CompoundVariable.
     * 
     * Parses strings of the following form:
     * <ul>
     * <li>text)</li>
     * <li>text,text)</li>
     * <li></li>
     * </ul>
     * @param reader a StringReader pointing to the current input location, just after "("
     * @return a list of CompoundVariable elements
     * @throws InvalidVariableException when evaluation of variables fail
     */
    LinkedList<CompoundVariable> parseParams(StringReader reader) throws InvalidVariableException {
        LinkedList<CompoundVariable> result = new LinkedList<>();
        StringBuilder buffer = new StringBuilder();
        char[] current = new char[1];
        char previous = ' ';
        int functionRecursion = 0;
        int parenRecursion = 0;
        try {
            while (reader.read(current) == 1) {
                if (current[0] == '\\') { // Process escaped characters
                    buffer.append(current[0]); // Store the \
                    if (reader.read(current) == 0) {
                        break; // end of buffer
                    }
                    previous = ' ';
                    buffer.append(current[0]); // store the following character
                } else if (current[0] == ',' && functionRecursion == 0) {
                    CompoundVariable param = new CompoundVariable();
                    param.setParameters(buffer.toString());
                    buffer.setLength(0);
                    result.add(param);
                } else if (current[0] == ')' && functionRecursion == 0 && parenRecursion == 0) {
                    // Detect functionName() so this does not generate empty string as the parameter
                    if (buffer.length() == 0 && result.isEmpty()){
                        return result;
                    }
                    // Normal exit occurs here
                    CompoundVariable param = new CompoundVariable();
                    param.setParameters(buffer.toString());
                    buffer.setLength(0);
                    result.add(param);
                    return result;
                } else if (current[0] == '{' && previous == '$') {
                    buffer.append(current[0]);
                    previous = current[0];
                    functionRecursion++;
                } else if (current[0] == '}' && functionRecursion > 0) {
                    buffer.append(current[0]);
                    previous = current[0];
                    functionRecursion--;
                } else if (current[0] == ')' && functionRecursion == 0 && parenRecursion > 0) {
                    buffer.append(current[0]);
                    previous = current[0];
                    parenRecursion--;
                } else if (current[0] == '(' && functionRecursion == 0) {
                    buffer.append(current[0]);
                    previous = current[0];
                    parenRecursion++;
                } else {
                    buffer.append(current[0]);
                    previous = current[0];
                }
            }
        } catch (IOException e) {// Should not happen with StringReader
            log.error("Error parsing function: {}", buffer, e);
        }
        // Dropped out, i.e. did not find closing ')'
        log.warn("Probably an invalid function string: {}", buffer);
        CompoundVariable var = new CompoundVariable();
        var.setParameters(buffer.toString());
        result.add(var);
        return result;
    }
}
