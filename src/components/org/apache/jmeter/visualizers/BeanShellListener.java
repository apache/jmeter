/*
 * Copyright 2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import java.io.Serializable;

import org.apache.jmeter.gui.UnsharedComponent;
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleListener;
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

public class BeanShellListener extends AbstractTestElement 
    implements SampleListener, Visualizer, Serializable, TestBean, UnsharedComponent  {
	
    private static final Logger log = LoggingManager.getLoggerForClass();
    
    private static final long serialVersionUID = 2;

    transient private BeanShellInterpreter bshInterpreter = null;

    // can be specified in jmeter.properties
    private static final String INIT_FILE = "beanshell.listener.init"; //$NON-NLS-1$


    private String script = "";
    
    public BeanShellListener()  throws ClassNotFoundException {
        bshInterpreter = new BeanShellInterpreter(JMeterUtils.getProperty(INIT_FILE),log);
    }


	public String getScript() {
		return script;
	}


	public void setScript(String script) {
		this.script = script;
	}


	public void sampleOccurred(SampleEvent se) {
        JMeterContext jmctx = JMeterContextService.getContext();
        JMeterVariables vars = jmctx.getVariables();
        SampleResult samp=se.getResult();
        try {
            // Add variables for access to context and variables
            bshInterpreter.set("ctx", jmctx);//$NON-NLS-1$
            bshInterpreter.set("vars", vars);//$NON-NLS-1$
            bshInterpreter.set("sampleEvent", se);//$NON-NLS-1$
            bshInterpreter.set("sampleResult", samp);//$NON-NLS-1$
            bshInterpreter.eval(script);
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

//	public Object clone() {
//        BeanShellListener o = (BeanShellListener) super.clone();
//        o.script = script;
//		return o;
//	}
    
}
