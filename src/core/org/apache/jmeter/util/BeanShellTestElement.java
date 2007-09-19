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

package org.apache.jmeter.util;

import java.io.Serializable;

import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.TestListener;
import org.apache.jmeter.testelement.ThreadListener;
import org.apache.jmeter.util.BeanShellInterpreter;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.util.JMeterException;
import org.apache.jorphan.util.JOrphanUtils;
import org.apache.log.Logger;

public abstract class BeanShellTestElement extends AbstractTestElement
    implements Serializable, Cloneable, ThreadListener, TestListener
{
    private static final Logger log = LoggingManager.getLoggerForClass();
    
    private static final long serialVersionUID = 4;

    //++ For TestBean implementations only
	private String parameters; // passed to file or script
	
	private String filename; // file to source (overrides script)

	private String script; // script (if file not provided)
	//-- For TestBean implementations only

    
    transient private BeanShellInterpreter bshInterpreter = null;

    transient private boolean hasInitFile = false;

    public BeanShellTestElement() {
        super();
        init();
    }

    protected abstract String getInitFileProperty();

    protected BeanShellInterpreter getBeanShellInterpreter() {
        return bshInterpreter;
    }

	private void init() {
		parameters=""; // ensure variables are not null
		filename="";
		script="";
		try {
            String initFileName = JMeterUtils.getProperty(getInitFileProperty());
            hasInitFile = initFileName != null;
            bshInterpreter = new BeanShellInterpreter(initFileName, log);
		} catch (ClassNotFoundException e) {
			log.error("Cannot find BeanShell: "+e.toString());
		}
	}

    private Object readResolve() {
    	init();
    	return this;
    }

    public Object clone() {
        BeanShellTestElement o = (BeanShellTestElement) super.clone();
        o.init();
       return o;
    }

    protected Object processFileOrScript(BeanShellInterpreter bsh) throws JMeterException{
		String fileName = getFilename();

		bsh.set("FileName", getFilename());//$NON-NLS-1$
		// Set params as a single line
		bsh.set("Parameters", getParameters()); // $NON-NLS-1$
		// and set as an array
		bsh.set("bsh.args",//$NON-NLS-1$
				JOrphanUtils.split(getParameters(), " "));//$NON-NLS-1$

		if (fileName.length() == 0) {
			return bsh.eval(getScript());
		} else {
			return bsh.source(fileName);
		}
    }

    /**
     * Return the script (TestBean version).
     * Must be overridden for subclasses that don't implement TestBean
     * otherwise the clone() method won't work.
     * 
     * @return the script to execute
     */
    public String getScript(){
        return script;
    }

    /**
     * Set the script (TestBean version).
     * Must be overridden for subclasses that don't implement TestBean
     * otherwise the clone() method won't work.
     * 
     * @param s the script to execute (may be blank)
     */
    public void setScript(String s){
        script=s;
    }

	public void threadStarted() {
        if (bshInterpreter == null || !hasInitFile) return;
		try {
			bshInterpreter.evalNoLog("threadStarted()"); // $NON-NLS-1$
		} catch (JMeterException ignored) {
            log.debug(getClass().getName() + " : " + ignored.getLocalizedMessage()); // $NON-NLS-1$
		}
	}

	public void threadFinished() {
        if (bshInterpreter == null || !hasInitFile) return;
		try {
			bshInterpreter.evalNoLog("threadFinished()"); // $NON-NLS-1$
		} catch (JMeterException ignored) {
            log.debug(getClass().getName() + " : " + ignored.getLocalizedMessage()); // $NON-NLS-1$
		}		
	}

	public void testEnded() {
        if (bshInterpreter == null || !hasInitFile) return;
		try {
			bshInterpreter.evalNoLog("testEnded()"); // $NON-NLS-1$
		} catch (JMeterException ignored) {
            log.debug(getClass().getName() + " : " + ignored.getLocalizedMessage()); // $NON-NLS-1$
		}		
	}

	public void testEnded(String host) {
        if (bshInterpreter == null || !hasInitFile) return;
		try {
			bshInterpreter.eval((new StringBuffer("testEnded(")) // $NON-NLS-1$
					.append(host)
					.append(")") // $NON-NLS-1$
					.toString()); // $NON-NLS-1$
		} catch (JMeterException ignored) {
            log.debug(getClass().getName() + " : " + ignored.getLocalizedMessage()); // $NON-NLS-1$
		}		
	}

	public void testIterationStart(LoopIterationEvent event) {
		// Not implemented
	}

	public void testStarted() {
        if (bshInterpreter == null || !hasInitFile) return;
		try {
			bshInterpreter.evalNoLog("testStarted()"); // $NON-NLS-1$
		} catch (JMeterException ignored) {
			log.debug(getClass().getName() + " : " + ignored.getLocalizedMessage()); // $NON-NLS-1$
		}		
	}

	public void testStarted(String host) {
        if (bshInterpreter == null || !hasInitFile) return;
		try {
			bshInterpreter.eval((new StringBuffer("testStarted(")) // $NON-NLS-1$
					.append(host)
					.append(")") // $NON-NLS-1$
					.toString()); // $NON-NLS-1$
		} catch (JMeterException ignored) {
            log.debug(getClass().getName() + " : " + ignored.getLocalizedMessage()); // $NON-NLS-1$
		}		
	}

	public String getParameters() {
		return parameters;
	}

	public void setParameters(String s) {
		parameters = s;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String s) {
		filename = s;
	}
}
