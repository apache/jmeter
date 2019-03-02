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

package org.apache.jmeter.modifiers;

import org.apache.jmeter.gui.GUIMenuSortOrder;
import org.apache.jmeter.processor.PreProcessor;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.util.BeanShellInterpreter;
import org.apache.jmeter.util.BeanShellTestElement;
import org.apache.jorphan.util.JMeterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@GUIMenuSortOrder(Integer.MAX_VALUE)
public class BeanShellPreProcessor extends BeanShellTestElement
    implements Cloneable, PreProcessor, TestBean
{
    private static final Logger log = LoggerFactory.getLogger(BeanShellPreProcessor.class);

    private static final long serialVersionUID = 5;

    // can be specified in jmeter.properties
    private static final String INIT_FILE = "beanshell.preprocessor.init"; //$NON-NLS-1$

    @Override
    protected String getInitFileProperty() {
        return INIT_FILE;
    }

    @Override
    public void process(){
        final BeanShellInterpreter bshInterpreter = getBeanShellInterpreter();
        if (bshInterpreter == null) {
            log.error("BeanShell not found");
            return;
        }
        JMeterContext jmctx = JMeterContextService.getContext();
        Sampler sam = jmctx.getCurrentSampler();
        try {
            // Add variables for access to context and variables
            bshInterpreter.set("sampler", sam);//$NON-NLS-1$
            processFileOrScript(bshInterpreter);
        } catch (JMeterException e) {
            if (log.isWarnEnabled()) {
                log.warn("Problem in BeanShell script. {}", e.toString());
            }
        }
    }

    @Override
    public Object clone() {
        return super.clone();
    }
}
