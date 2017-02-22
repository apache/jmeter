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

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.Properties;

import org.apache.bsf.BSFEngine;
import org.apache.bsf.BSFException;
import org.apache.bsf.BSFManager;
import org.apache.commons.io.FileUtils;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jorphan.util.JOrphanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BSFTestElement extends ScriptingTestElement
    implements Serializable
{
    private static final long serialVersionUID = 235L;

    private static final Logger log = LoggerFactory.getLogger(BSFTestElement.class);

    static {
        log.info("Registering JMeter version of JavaScript engine as work-round for BSF-22");
        BSFManager.registerScriptingEngine("javascript", //$NON-NLS-1$
                "org.apache.jmeter.util.BSFJavaScriptEngine", //$NON-NLS-1$
                new String[]{"js"}); //$NON-NLS-1$
    }

    public BSFTestElement() {
        super();
    }

    protected BSFManager getManager() throws BSFException {
        BSFManager mgr = new BSFManager();
        initManager(mgr);
        return mgr;
    }

    protected void initManager(BSFManager mgr) throws BSFException{
        final String label = getName();
        final String fileName = getFilename();
        final String scriptParameters = getParameters();
        // Use actual class name for log
        final Logger logger = LoggerFactory.getLogger(getClass());
        mgr.declareBean("log", logger, Logger.class); // $NON-NLS-1$
        mgr.declareBean("Label",label, String.class); // $NON-NLS-1$
        mgr.declareBean("FileName",fileName, String.class); // $NON-NLS-1$
        mgr.declareBean("Parameters", scriptParameters, String.class); // $NON-NLS-1$
        String [] args=JOrphanUtils.split(scriptParameters, " ");//$NON-NLS-1$
        mgr.declareBean("args",args,args.getClass());//$NON-NLS-1$
        // Add variables for access to context and variables
        JMeterContext jmctx = JMeterContextService.getContext();
        JMeterVariables vars = jmctx.getVariables();
        Properties props = JMeterUtils.getJMeterProperties();

        mgr.declareBean("ctx", jmctx, jmctx.getClass()); // $NON-NLS-1$
        mgr.declareBean("vars", vars, vars.getClass()); // $NON-NLS-1$
        mgr.declareBean("props", props, props.getClass()); // $NON-NLS-1$
        // For use in debugging:
        mgr.declareBean("OUT", System.out, PrintStream.class); // $NON-NLS-1$

        // Most subclasses will need these:
        Sampler sampler = jmctx.getCurrentSampler();
        mgr.declareBean("sampler", sampler, Sampler.class);
        SampleResult prev = jmctx.getPreviousResult();
        mgr.declareBean("prev", prev, SampleResult.class);
    }

    protected void processFileOrScript(BSFManager mgr) throws BSFException{
        BSFEngine bsfEngine = mgr.loadScriptingEngine(getScriptLanguage());
        final String scriptFile = getFilename();
        if (scriptFile.length() == 0) {
            bsfEngine.exec("[script]",0,0,getScript());
        } else {// we have a file, read and process it
            try {
                String script=FileUtils.readFileToString(new File(scriptFile), Charset.defaultCharset());
                bsfEngine.exec(scriptFile,0,0,script);
            } catch (IOException e) {
                if (log.isWarnEnabled()) {
                    log.warn("Exception executing script. {}", e.getLocalizedMessage());
                }
                throw new BSFException(BSFException.REASON_IO_ERROR, "Problem reading script file", e);
            }
        }
    }

    protected Object evalFileOrScript(BSFManager mgr) throws BSFException{
        BSFEngine bsfEngine = mgr.loadScriptingEngine(getScriptLanguage());
        final String scriptFile = getFilename();
        if (scriptFile.length() == 0) {
            return bsfEngine.eval("[script]",0,0,getScript());
        } else {// we have a file, read and process it
            try {
                String script=FileUtils.readFileToString(new File(scriptFile), Charset.defaultCharset());
                return bsfEngine.eval(scriptFile,0,0,script);
            } catch (IOException e) {
                if (log.isWarnEnabled()) {
                    log.warn("Exception evaluating script. {}", e.getLocalizedMessage());
                }
                throw new BSFException(BSFException.REASON_IO_ERROR,"Problem reading script file",e);
            }
        }
    }

    public String getScriptLanguage() {
        return scriptLanguage;
    }

    public void setScriptLanguage(String s) {
        scriptLanguage = s;
    }
}
