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

package org.apache.jmeter.engine;

import java.util.Map;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.engine.util.ValueReplacer;
import org.apache.jmeter.functions.InvalidVariableException;
import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.HashTreeTraverser;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * Class to replace function and variable references in the test tree.
 *
 */
public class PreCompiler implements HashTreeTraverser {
    private static final Logger log = LoggingManager.getLoggerForClass();

    private final ValueReplacer replacer;

//   Used by both StandardJMeterEngine and ClientJMeterEngine.
//   In the latter case, only ResultCollectors are updated,
//   as only these are relevant to the client, and updating
//   other elements causes all sorts of problems.
    private final boolean isRemote; // skip certain processing for remote tests

    public PreCompiler() {
        replacer = new ValueReplacer();
        isRemote = false;
    }

    public PreCompiler(boolean remote) {
        replacer = new ValueReplacer();
        isRemote = remote;
    }

    /** {@inheritDoc} */
    public void addNode(Object node, HashTree subTree) {
        if(isRemote && node instanceof ResultCollector)
        {
            try {
                replacer.replaceValues((TestElement) node);
            } catch (InvalidVariableException e) {
                log.error("invalid variables", e);
            }
        }
        if (isRemote) {
            return;
        }
        if(node instanceof TestElement)
        {
            try {
                replacer.replaceValues((TestElement) node);
            } catch (InvalidVariableException e) {
                log.error("invalid variables", e);
            }
        }
        if (node instanceof TestPlan) {
            ((TestPlan)node).prepareForPreCompile(); //A hack to make user-defined variables in the testplan element more dynamic
            Map<String, String> args = ((TestPlan) node).getUserDefinedVariables();
            replacer.setUserDefinedVariables(args);
            JMeterVariables vars = new JMeterVariables();
            vars.putAll(args);
            JMeterContextService.getContext().setVariables(vars);
        }

        if (node instanceof Arguments) {
            ((Arguments)node).setRunningVersion(true);
            Map<String, String> args = ((Arguments) node).getArgumentsAsMap();
            replacer.addVariables(args);
            JMeterContextService.getContext().getVariables().putAll(args);
        }
    }

    /** {@inheritDoc} */
    public void subtractNode() {
    }

    /** {@inheritDoc} */
    public void processPath() {
    }
}
