/*
 * Copyright 2006 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
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

import java.io.Serializable;

import org.apache.jmeter.processor.PostProcessor;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.BeanShellInterpreter;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.util.JMeterException;
import org.apache.log.Logger;

public class BeanShellPostProcessor extends AbstractTestElement implements PostProcessor, Serializable, TestBean {
    private static final Logger log = LoggingManager.getLoggerForClass();
    
    private static final long serialVersionUID = 3;

    private String script;
    
    transient private BeanShellInterpreter bshInterpreter = null;

    // can be specified in jmeter.properties
    private static final String INIT_FILE = "beanshell.postprocessor.init"; //$NON-NLS-1$

    public BeanShellPostProcessor() {
        super();
        init();
    }

	private void init() {
		try {
			bshInterpreter = new BeanShellInterpreter(JMeterUtils.getProperty(INIT_FILE),log);
		} catch (ClassNotFoundException e) {
			log.error("Cannot find BeanShell: "+e.getLocalizedMessage());
		}
	}

    private Object readResolve() {
    	init();
    	return this;
    }
    
     public void process() {
        JMeterContext jmctx = JMeterContextService.getContext();

        SampleResult prev = jmctx.getPreviousResult();
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
            bshInterpreter.eval(script);
        } catch (JMeterException e) {
            log.warn("Problem in BeanShell script "+e);
        }
	}

	public Object clone() {
        BeanShellPostProcessor o = (BeanShellPostProcessor) super.clone();
        o.script = script;
		return o;
	}
    
    public String getScript(){
        return script;
    }

    public void setScript(String s){
        script=s;
    }
}
