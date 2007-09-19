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

package org.apache.jmeter.visualizers;

import org.apache.jmeter.gui.UnsharedComponent;
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleListener;
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

public class BeanShellListener extends BeanShellTestElement 
    implements Cloneable, SampleListener, Visualizer, TestBean, UnsharedComponent  {
	
    private static final Logger log = LoggingManager.getLoggerForClass();
    
    private static final long serialVersionUID = 4;

    // can be specified in jmeter.properties
    private static final String INIT_FILE = "beanshell.listener.init"; //$NON-NLS-1$

    protected String getInitFileProperty() {
        return INIT_FILE;
    }

	public void sampleOccurred(SampleEvent se) {
        final BeanShellInterpreter bshInterpreter = getBeanShellInterpreter();
		if (bshInterpreter == null) {
            log.error("BeanShell not found");
            return;
        }
        
        JMeterContext jmctx = JMeterContextService.getContext();
        JMeterVariables vars = jmctx.getVariables();
        SampleResult samp=se.getResult();
        try {
            // Add variables for access to context and variables
            bshInterpreter.set("ctx", jmctx);//$NON-NLS-1$
            bshInterpreter.set("vars", vars);//$NON-NLS-1$
            bshInterpreter.set("sampleEvent", se);//$NON-NLS-1$
            bshInterpreter.set("sampleResult", samp);//$NON-NLS-1$
            processFileOrScript(bshInterpreter);
        } catch (JMeterException e) {
            log.warn("Problem in BeanShell script "+e);
        }		
	}

	public void sampleStarted(SampleEvent e) {
	}

	public void sampleStopped(SampleEvent e) {
	}

	public void add(SampleResult sample) {
	}

	public boolean isStats() {// Required by Visualiser
		return false;
	}
}
