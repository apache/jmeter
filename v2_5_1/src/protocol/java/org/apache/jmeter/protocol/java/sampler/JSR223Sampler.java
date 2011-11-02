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

import java.io.IOException;

import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.util.JSR223TestElement;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

public class JSR223Sampler extends JSR223TestElement implements Cloneable, Sampler, TestBean {

    private static final long serialVersionUID = 234L;

    private static final Logger log = LoggingManager.getLoggerForClass();

    public SampleResult sample(Entry entry) {
        SampleResult result = new SampleResult();
        result.setSampleLabel(getName());
        final String filename = getFilename();
        if (filename.length() > 0){
            result.setSamplerData("File: "+filename);
        } else {
            result.setSamplerData(getScript());
        }
        result.setDataType(SampleResult.TEXT);
        result.sampleStart();
        try {
            ScriptEngineManager mgr = getManager();
            if (mgr == null) {
                result.setSuccessful(false);
                result.setResponseCode("500"); // $NON-NLS-1$
                result.setResponseMessage("Could not instantiate ScriptManager");
                return result;
            }
            mgr.put("SampleResult",result);
            Object ret = processFileOrScript(mgr);
            result.setSuccessful(true);
            result.setResponseCodeOK();
            result.setResponseMessageOK();
            if (ret != null){
                result.setResponseData(ret.toString(), null);
            }
        } catch (IOException e) {
            log.warn("Problem in JSR223 script "+e);
            result.setSuccessful(false);
            result.setResponseCode("500"); // $NON-NLS-1$
            result.setResponseMessage(e.toString());
        } catch (ScriptException e) {
            log.warn("Problem in JSR223 script "+e);
            result.setSuccessful(false);
            result.setResponseCode("500"); // $NON-NLS-1$
            result.setResponseMessage(e.toString());
        }
        result.sampleEnd();
        return result;
    }
}
