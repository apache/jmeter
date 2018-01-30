/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 */

package org.apache.jmeter.assertions;

import java.io.IOException;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.apache.jmeter.gui.GUIMenuSortOrder;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.util.JSR223TestElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@GUIMenuSortOrder(4)
public class JSR223Assertion extends JSR223TestElement implements Cloneable, Assertion, TestBean
{
    private static final Logger log = LoggerFactory.getLogger(JSR223Assertion.class);

    private static final long serialVersionUID = 235L;

    @Override
    public AssertionResult getResult(SampleResult response) {
        AssertionResult result = new AssertionResult(getName());
        try {
            ScriptEngine scriptEngine = getScriptEngine();
            Bindings bindings = scriptEngine.createBindings();
            bindings.put("SampleResult", response);
            bindings.put("AssertionResult", result);
            processFileOrScript(scriptEngine, bindings);
            result.setError(false);
        } catch (IOException | ScriptException e) {
            log.error("Problem in JSR223 script: {}", getName(), e);
            result.setError(true);
            result.setFailureMessage(e.toString());
        }
        return result;
    }

    @Override
    public Object clone() {
        return super.clone();
    }
}
