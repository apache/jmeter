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

package org.apache.jmeter.extractor;

import org.apache.jmeter.processor.PostProcessor;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.BeanShellInterpreter;
import org.apache.jmeter.util.BeanShellTestElement;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.util.JMeterException;
import org.apache.log.Logger;

public class BeanShellPostProcessor extends BeanShellTestElement 
    implements Cloneable, PostProcessor, TestBean
{
    private static final Logger log = LoggingManager.getLoggerForClass();
    
    private static final long serialVersionUID = 3;
    
    // can be specified in jmeter.properties
    private static final String INIT_FILE = "beanshell.postprocessor.init"; //$NON-NLS-1$

    protected String getInitFileProperty() {
        return INIT_FILE;
    }
    
     public void process() {
        JMeterContext jmctx = JMeterContextService.getContext();

        SampleResult prev = jmctx.getPreviousResult();
		final BeanShellInterpreter bshInterpreter = getBeanShellInterpreter();
		if (prev == null || bshInterpreter == null) {
			return;
		}

        JMeterVariables vars = jmctx.getVariables();
        try {
            // Add variables for access to context and variables
            bshInterpreter.set("ctx", jmctx);//$NON-NLS-1$
            bshInterpreter.set("vars", vars);//$NON-NLS-1$
            bshInterpreter.set("prev", prev);//$NON-NLS-1$
            bshInterpreter.set("data", prev.getResponseData());//$NON-NLS-1$
            bshInterpreter.eval(getScript());
        } catch (JMeterException e) {
            log.warn("Problem in BeanShell script "+e);
        }
	}
}
