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

import java.io.FileInputStream;

import org.apache.bsf.BSFEngine;
import org.apache.bsf.BSFException;
import org.apache.bsf.BSFManager;
import org.apache.commons.io.IOUtils;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.util.BSFTestElement;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * A sampler which understands BSF
 *
 */
public class BSFSampler extends BSFTestElement implements Sampler {

    private static final long serialVersionUID = 240L;

    private static final Logger log = LoggingManager.getLoggerForClass();

    //+ JMX file attributes - do not change
    private static final String FILENAME = "BSFSampler.filename"; //$NON-NLS-1$

    private static final String SCRIPT = "BSFSampler.query"; //$NON-NLS-1$

    private static final String LANGUAGE = "BSFSampler.language"; //$NON-NLS-1$

    private static final String PARAMETERS = "BSFSampler.parameters"; //$NON-NLS-1$
    //- JMX file attributes

    public BSFSampler() {
        super();
    }

    @Override
    public String getFilename() {
        return getPropertyAsString(FILENAME);
    }

    @Override
    public void setFilename(String newFilename) {
        this.setProperty(FILENAME, newFilename);
    }

    @Override
    public String getScript() {
        return this.getPropertyAsString(SCRIPT);
    }

    @Override
    public void setScript(String newScript) {
        this.setProperty(SCRIPT, newScript);
    }

    @Override
    public String getParameters() {
        return this.getPropertyAsString(PARAMETERS);
    }

    @Override
    public void setParameters(String newScript) {
        this.setProperty(PARAMETERS, newScript);
    }

    @Override
    public String getScriptLanguage() {
        return this.getPropertyAsString(LANGUAGE);
    }

    @Override
    public void setScriptLanguage(String lang) {
        this.setProperty(LANGUAGE, lang);
    }

    /**
     * Returns a formatted string label describing this sampler
     *
     * @return a formatted string label describing this sampler
     */

    public String getLabel() {
        return getName();
    }

    public SampleResult sample(Entry e)// Entry tends to be ignored ...
    {
        final String label = getLabel();
        final String request = getScript();
        final String fileName = getFilename();
        log.debug(label + " " + fileName);
        SampleResult res = new SampleResult();
        res.setSampleLabel(label);
        FileInputStream is = null;
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

            // These are not useful yet, as have not found how to get updated values back
            //mgr.declareBean("ResponseCode", "200", String.class); // $NON-NLS-1$
            //mgr.declareBean("ResponseMessage", "OK", String.class); // $NON-NLS-1$
            //mgr.declareBean("IsSuccess", Boolean.TRUE, Boolean.class); // $NON-NLS-1$

            // N.B. some engines (e.g. Javascript) cannot handle certain declareBean() calls
            // after the engine has been initialised, so create the engine last
            bsfEngine = mgr.loadScriptingEngine(getScriptLanguage());

            Object bsfOut = null;
            if (fileName.length()>0) {
                res.setSamplerData("File: "+fileName);
                is = new FileInputStream(fileName);
                bsfOut = bsfEngine.eval(fileName, 0, 0, IOUtils.toString(is));
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
            IOUtils.closeQuietly(is);
// Will be done by mgr.terminate() anyway
//          if (bsfEngine != null) {
//              bsfEngine.terminate();
//          }
            mgr.terminate();
        }

        return res;
    }
}
