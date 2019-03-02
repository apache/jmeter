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

package org.apache.jmeter.protocol.java.sampler;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.engine.util.ConfigMergabilityIndicator;
import org.apache.jmeter.gui.GUIMenuSortOrder;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.Interruptible;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.BeanShellInterpreter;
import org.apache.jmeter.util.BeanShellTestElement;
import org.apache.jorphan.util.JMeterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A sampler which understands BeanShell
 *
 */
@GUIMenuSortOrder(Integer.MAX_VALUE)
public class BeanShellSampler extends BeanShellTestElement implements Sampler, Interruptible, ConfigMergabilityIndicator
{
    private static final Set<String> APPLIABLE_CONFIG_CLASSES = new HashSet<>(
            Arrays.asList("org.apache.jmeter.config.gui.SimpleConfigGui"));
    
    private static final Logger log = LoggerFactory.getLogger(BeanShellSampler.class);

    private static final long serialVersionUID = 4;

    public static final String FILENAME = "BeanShellSampler.filename"; //$NON-NLS-1$

    public static final String SCRIPT = "BeanShellSampler.query"; //$NON-NLS-1$

    public static final String PARAMETERS = "BeanShellSampler.parameters"; //$NON-NLS-1$

    public static final String INIT_FILE = "beanshell.sampler.init"; //$NON-NLS-1$

    public static final String RESET_INTERPRETER = "BeanShellSampler.resetInterpreter"; //$NON-NLS-1$

    private transient volatile BeanShellInterpreter savedBsh = null;
    
    @Override
    protected String getInitFileProperty() {
        return INIT_FILE;
    }

    @Override
    public String getScript() {
        return this.getPropertyAsString(SCRIPT);
    }

    @Override
    public String getFilename() {
        return getPropertyAsString(FILENAME);
    }

    @Override
    public String getParameters() {
        return getPropertyAsString(PARAMETERS);
    }

    @Override
    public boolean isResetInterpreter() {
        return getPropertyAsBoolean(RESET_INTERPRETER);
    }

    @Override
    public SampleResult sample(Entry e)// Entry tends to be ignored ...
    {
        SampleResult res = new SampleResult();
        boolean isSuccessful = false;
        res.setSampleLabel(getName());
        res.sampleStart();
        final BeanShellInterpreter bshInterpreter = getBeanShellInterpreter();
        if (bshInterpreter == null) {
            res.sampleEnd();
            res.setResponseCode("503");//$NON-NLS-1$
            res.setResponseMessage("BeanShell Interpreter not found");
            res.setSuccessful(false);
            return res;
        }
        try {
            String request = getScript();
            String fileName = getFilename();
            if (fileName.length() == 0) {
                res.setSamplerData(request);
            } else {
                res.setSamplerData(fileName);
            }

            bshInterpreter.set("SampleResult", res); //$NON-NLS-1$

            // Set default values
            bshInterpreter.set("ResponseCode", "200"); //$NON-NLS-1$
            bshInterpreter.set("ResponseMessage", "OK");//$NON-NLS-1$
            bshInterpreter.set("IsSuccess", true);//$NON-NLS-1$

            res.setDataType(SampleResult.TEXT); // assume text output - script can override if necessary

            savedBsh = bshInterpreter;
            Object bshOut = processFileOrScript(bshInterpreter);
            savedBsh = null;

            if (bshOut != null) {// Set response data
                String out = bshOut.toString();
                res.setResponseData(out, null);
            }
            // script can also use setResponseData() so long as it returns null

            res.setResponseCode(bshInterpreter.get("ResponseCode").toString());//$NON-NLS-1$
            res.setResponseMessage(bshInterpreter.get("ResponseMessage").toString());//$NON-NLS-1$
            isSuccessful = Boolean.valueOf(bshInterpreter.get("IsSuccess") //$NON-NLS-1$
                    .toString()).booleanValue();
        }
        /*
         * To avoid class loading problems when bsh,jar is missing, we don't try
         * to catch this error separately catch (bsh.EvalError ex) {
         * log.debug("",ex); res.setResponseCode("500");//$NON-NLS-1$
         * res.setResponseMessage(ex.toString()); }
         */
        // but we do trap this error to make tests work better
        catch (NoClassDefFoundError ex) {
            log.error("BeanShell Jar missing? {}", ex.toString());
            res.setResponseCode("501");//$NON-NLS-1$
            res.setResponseMessage(ex.toString());
            res.setStopThread(true); // No point continuing
        } catch (Exception ex) // Mainly for bsh.EvalError
        {
            if (log.isWarnEnabled()) {
                log.warn("Exception executing script. {}", ex.toString());
            }
            res.setResponseCode("500");//$NON-NLS-1$
            res.setResponseMessage(ex.toString());
        } finally {
            savedBsh = null;
        }

        res.sampleEnd();

        // Set if we were successful or not
        res.setSuccessful(isSuccessful);

        return res;
    }

    @Override
    public boolean interrupt() {
        if (savedBsh != null) {
            try {
                savedBsh.evalNoLog("interrupt()"); // $NON-NLS-1$
            } catch (JMeterException ignored) {
                if (log.isDebugEnabled()) {
                    log.debug("{} : {}", getClass(), ignored.getLocalizedMessage()); // $NON-NLS-1$
                }
            }
            return true;
        }
        return false;
    }

    /**
     * @see org.apache.jmeter.samplers.AbstractSampler#applies(org.apache.jmeter.config.ConfigTestElement)
     */
    @Override
    public boolean applies(ConfigTestElement configElement) {
        String guiClass = configElement.getProperty(TestElement.GUI_CLASS).getStringValue();
        return APPLIABLE_CONFIG_CLASSES.contains(guiClass);
    }
}
