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

package org.apache.jmeter.extractor;

import java.io.IOException;

import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.apache.jmeter.gui.GUIMenuSortOrder;
import org.apache.jmeter.gui.TestElementMetadata;
import org.apache.jmeter.processor.PostProcessor;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.util.JSR223TestElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@GUIMenuSortOrder(5)
@TestElementMetadata(labelResource = "displayName")
public class JSR223PostProcessor extends JSR223TestElement implements Cloneable, PostProcessor, TestBean
{
    private static final Logger log = LoggerFactory.getLogger(JSR223PostProcessor.class);

    private static final long serialVersionUID = 233L;

    @Override
    public void process() {
        try {
            ScriptEngine scriptEngine = getScriptEngine();
            processFileOrScript(scriptEngine, null);
        } catch (ScriptException | IOException e) {
            log.error("Problem in JSR223 script, {}", getName(), e);
        }
    }

    @Override
    @SuppressWarnings("RedundantOverride")
    public Object clone() {
        return super.clone();
    }
}
