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
import org.apache.bsf.BSFManager;
import org.apache.commons.io.IOUtils;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.util.JOrphanUtils;
import org.apache.log.Logger;

/**
 * A sampler which understands BSF
 * 
 */
public class BSFSampler extends AbstractSampler {

	private static final Logger log = LoggingManager.getLoggerForClass();

	public static final String FILENAME = "BSFSampler.filename"; //$NON-NLS-1$

	public static final String SCRIPT = "BSFSampler.query"; //$NON-NLS-1$

	public static final String LANGUAGE = "BSFSampler.language"; //$NON-NLS-1$

	public static final String PARAMETERS = "BSFSampler.parameters"; //$NON-NLS-1$

	private transient BSFManager mgr;

	public BSFSampler() {
		mgr = new BSFManager();
	}

	public String getFilename() {
		return getPropertyAsString(FILENAME);
	}

	public void setFilename(String newFilename) {
		this.setProperty(FILENAME, newFilename);
	}

	public String getScript() {
		return this.getPropertyAsString(SCRIPT);
	}

	public void setScript(String newScript) {
		this.setProperty(SCRIPT, newScript);
	}

	public String getParameters() {
		return this.getPropertyAsString(PARAMETERS);
	}

	public void setParameters(String newScript) {
		this.setProperty(PARAMETERS, newScript);
	}

	public String getScriptLanguage() {
		return this.getPropertyAsString(LANGUAGE);
	}

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
		log.info(label + " " + getFilename());
		SampleResult res = new SampleResult();
		res.setSampleLabel(label);
		FileInputStream is = null;
		
		res.sampleStart();
		try {
			final String request = getScript();
			final String fileName = getFilename();
			
			mgr.declareBean("log", log, log.getClass()); // $NON-NLS-1$
			mgr.declareBean("Label",label, String.class); // $NON-NLS-1$
			mgr.declareBean("FileName",fileName, String.class); // $NON-NLS-1$
			mgr.declareBean("Parameters", getParameters(), String.class); // $NON-NLS-1$
			String [] args=JOrphanUtils.split(getParameters(), " ");//$NON-NLS-1$
			mgr.declareBean("args",args,args.getClass());//$NON-NLS-1$
			mgr.declareBean("SampleResult", res, res.getClass()); // $NON-NLS-1$
			
			// TODO: find out how to retrieve these from the script
			// At present the script has to use SampleResult methods to set them.
			res.setResponseCode("200"); // $NON-NLS-1$
			res.setResponseMessage("OK"); // $NON-NLS-1$
			res.setSuccessful(true);

			// These are not useful yet, as have not found how to get updated values back
			//mgr.declareBean("ResponseCode", "200", String.class); // $NON-NLS-1$
			//mgr.declareBean("ResponseMessage", "OK", String.class); // $NON-NLS-1$
			//mgr.declareBean("IsSuccess", Boolean.TRUE, Boolean.class); // $NON-NLS-1$

			res.setDataType(SampleResult.TEXT); // Default (can be overridden by the script)

			// Add variables for access to context and variables
			JMeterContext jmctx = JMeterContextService.getContext();
			JMeterVariables vars = jmctx.getVariables();
			mgr.declareBean("ctx", jmctx, jmctx.getClass()); // $NON-NLS-1$
			mgr.declareBean("vars", vars, vars.getClass()); // $NON-NLS-1$

			BSFEngine bsfEngine = mgr.loadScriptingEngine(getScriptLanguage());

			Object bsfOut = null;
			if (fileName.length()>0) {
				res.setSamplerData("File: "+fileName);
				is = new FileInputStream(fileName);
				bsfOut = bsfEngine.eval(fileName, 0, 0, IOUtils.toString(is));
			} else {
				res.setSamplerData("[script]");
			    bsfOut = bsfEngine.eval("script", 0, 0, request);
			}

			if (bsfOut != null) {
			    res.setResponseData(bsfOut.toString().getBytes());
			}
		} catch (NoClassDefFoundError ex) {
			log.warn("", ex);
			res.setSuccessful(false);
			res.setResponseCode("500"); // $NON-NLS-1$
			res.setResponseMessage(ex.toString());
		} catch (Exception ex) {
			log.warn("", ex);
			res.setSuccessful(false);
			res.setResponseCode("500"); // $NON-NLS-1$
			res.setResponseMessage(ex.toString());
		} finally {
			res.sampleEnd();
			IOUtils.closeQuietly(is);
		}

		return res;
	}
}
