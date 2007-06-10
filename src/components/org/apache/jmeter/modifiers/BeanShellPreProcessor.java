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

import java.io.Serializable;

import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.processor.PreProcessor;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.TestListener;
import org.apache.jmeter.testelement.ThreadListener;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.BeanShellInterpreter;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.util.JMeterException;
import org.apache.log.Logger;

public class BeanShellPreProcessor extends AbstractTestElement
    implements PreProcessor, Serializable, TestBean, ThreadListener, TestListener
{
    private static final Logger log = LoggingManager.getLoggerForClass();
    
    private static final long serialVersionUID = 3;

    private String script;
    
    transient private BeanShellInterpreter bshInterpreter = null;

    // can be specified in jmeter.properties
    private static final String INIT_FILE = "beanshell.preprocessor.init"; //$NON-NLS-1$

    public BeanShellPreProcessor() {
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

    public void process(){
        if (bshInterpreter == null) {
            return;
        }
        JMeterContext jmctx = JMeterContextService.getContext();
        JMeterVariables vars = jmctx.getVariables();
		Sampler sam = jmctx.getCurrentSampler();
		SampleResult prev = jmctx.getPreviousResult();
        try {
            // Add variables for access to context and variables
            bshInterpreter.set("ctx", jmctx);//$NON-NLS-1$
            bshInterpreter.set("vars", vars);//$NON-NLS-1$
            bshInterpreter.set("sampler", sam);//$NON-NLS-1$
            bshInterpreter.set("prev", prev);//$NON-NLS-1$
            
            bshInterpreter.eval(script);
        } catch (JMeterException e) {
            log.warn("Problem in BeanShell script "+e);
        }
	}

	public Object clone() {
        BeanShellPreProcessor o = (BeanShellPreProcessor) super.clone();
        o.script = script;
		return o;
	}
    
    public String getScript(){
        return script;
    }

    public void setScript(String s){
        script=s;
    }

	public void threadStarted() {
		if (bshInterpreter == null) return;
		try {
			bshInterpreter.evalNoLog("threadStarted()"); // $NON-NLS-1$
		} catch (JMeterException ignored) {
			log.debug(ignored.getLocalizedMessage());
		}
	}

	public void threadFinished() {
		if (bshInterpreter == null) return;
		try {
			bshInterpreter.evalNoLog("threadFinished()"); // $NON-NLS-1$
		} catch (JMeterException ignored) {
			log.debug(ignored.getLocalizedMessage());
		}		
	}

	public void testEnded() {
		if (bshInterpreter == null) return;
		try {
			bshInterpreter.evalNoLog("testEnded()"); // $NON-NLS-1$
		} catch (JMeterException ignored) {
			log.debug(ignored.getLocalizedMessage());
		}		
	}

	public void testEnded(String host) {
		if (bshInterpreter == null) return;
		try {
			bshInterpreter.eval((new StringBuffer("testEnded(")) // $NON-NLS-1$
					.append(host)
					.append(")") // $NON-NLS-1$
					.toString()); // $NON-NLS-1$
		} catch (JMeterException ignored) {
			log.debug(ignored.getLocalizedMessage());
		}		
	}

	public void testIterationStart(LoopIterationEvent event) {
		// Not implemented
	}

	public void testStarted() {
		if (bshInterpreter == null) return;
		try {
			bshInterpreter.evalNoLog("testStarted()"); // $NON-NLS-1$
		} catch (JMeterException ignored) {
			log.debug(ignored.getLocalizedMessage());
		}		
	}

	public void testStarted(String host) {
		if (bshInterpreter == null) return;
		try {
			bshInterpreter.eval((new StringBuffer("testStarted(")) // $NON-NLS-1$
					.append(host)
					.append(")") // $NON-NLS-1$
					.toString()); // $NON-NLS-1$
		} catch (JMeterException ignored) {
			log.debug(ignored.getLocalizedMessage());
		}		
	}
}
