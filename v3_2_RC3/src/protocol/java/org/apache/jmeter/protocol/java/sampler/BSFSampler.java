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

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.bsf.BSFEngine;
import org.apache.bsf.BSFException;
import org.apache.bsf.BSFManager;
import org.apache.commons.io.IOUtils;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.engine.util.ConfigMergabilityIndicator;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.BSFTestElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A sampler which understands BSF
 *
 */
public class BSFSampler extends BSFTestElement implements Sampler, TestBean, ConfigMergabilityIndicator {

    private static final Set<String> APPLIABLE_CONFIG_CLASSES = new HashSet<>(
            Arrays.asList("org.apache.jmeter.config.gui.SimpleConfigGui"));

    private static final long serialVersionUID = 241L;

    private static final Logger log = LoggerFactory.getLogger(BSFSampler.class);

    public BSFSampler() {
        super();
    }

    @Override
    public SampleResult sample(Entry e)// Entry tends to be ignored ...
    {
        final String label = getName();
        final String request = getScript();
        final String fileName = getFilename();
        log.debug("{} {}", label, fileName);
        SampleResult res = new SampleResult();
        res.setSampleLabel(label);
        
        BSFEngine bsfEngine = null;
        // There's little point saving the manager between invocations
        // as we need to reset most of the beans anyway
        BSFManager mgr = new BSFManager();

        // TODO: find out how to retrieve these from the script
        // At present the script has to use SampleResult methods to set them.
        res.setResponseCode("200"); // $NON-NLS-1$
        res.setResponseMessage("OK"); // $NON-NLS-1$
        res.setSuccessful(true);
        res.setDataType(SampleResult.TEXT); // Default (can be overridden by the script)

        res.sampleStart();
        try {
            initManager(mgr);
            mgr.declareBean("SampleResult", res, res.getClass()); // $NON-NLS-1$

            // N.B. some engines (e.g. Javascript) cannot handle certain declareBean() calls
            // after the engine has been initialised, so create the engine last
            bsfEngine = mgr.loadScriptingEngine(getScriptLanguage());

            Object bsfOut = null;
            if (fileName.length()>0) {
                res.setSamplerData("File: "+fileName);
                try (FileInputStream fis = new FileInputStream(fileName); 
                        BufferedInputStream is = new BufferedInputStream(fis)) {
                    bsfOut = bsfEngine.eval(fileName, 0, 0, IOUtils.toString(is, Charset.defaultCharset()));
                }
            } else {
                res.setSamplerData(request);
                bsfOut = bsfEngine.eval("script", 0, 0, request);
            }

            if (bsfOut != null) {
                res.setResponseData(bsfOut.toString(), null);
            }
        } catch (BSFException ex) {
            log.warn("BSF error", ex);
            res.setSuccessful(false);
            res.setResponseCode("500"); // $NON-NLS-1$
            res.setResponseMessage(ex.toString());
        } catch (Exception ex) {// Catch evaluation errors
            log.warn("Problem evaluating the script", ex);
            res.setSuccessful(false);
            res.setResponseCode("500"); // $NON-NLS-1$
            res.setResponseMessage(ex.toString());
        } finally {
            res.sampleEnd();
            mgr.terminate();
        }

        return res;
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
