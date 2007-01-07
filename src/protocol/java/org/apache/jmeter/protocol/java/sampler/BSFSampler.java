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

import org.apache.bsf.BSFEngine;
import org.apache.bsf.BSFManager;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * A sampler which understands BSF
 * 
 * @version $Revision$ Updated on: $Date$
 */
public class BSFSampler extends AbstractSampler {

	private static final Logger log = LoggingManager.getLoggerForClass();

	public static final String FILENAME = "BSFSampler.filename"; //$NON-NLS-1$

	public static final String SCRIPT = "BSFSampler.query"; //$NON-NLS-1$

	public static final String LANGUAGE = "BSFSampler.language"; //$NON-NLS-1$

	public static final String PARAMETERS = "BSFSampler.parameters"; //$NON-NLS-1$

	private transient BSFManager mgr;

	private transient BSFEngine bsfEngine;

	public BSFSampler() {
		try {
			// register beanshell with the BSF framework
			mgr = new BSFManager();
			BSFManager.registerScriptingEngine("beanshell", "bsh.util.BeanShellBSFEngine", new String[] { "bsh" });
		} catch (NoClassDefFoundError e) {
		}

		// TODO: register other scripting languages ...

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
		log.info(getLabel() + " " + getFilename());
		SampleResult res = new SampleResult();
		boolean isSuccessful = false;
		res.setSampleLabel(getLabel());
		res.sampleStart();
		try {
			String request = getScript();
			res.setSamplerData(request);

			mgr.registerBean("Label", getLabel());
			mgr.registerBean("Name", getFilename());

			bsfEngine = mgr.loadScriptingEngine(getScriptLanguage());

			Object bsfOut = bsfEngine.eval("Sampler", 0, 0, request);

			res.setResponseData(bsfOut.toString().getBytes());
			res.setDataType(SampleResult.TEXT);
			res.setResponseCode("200");// TODO set from script
			res.setResponseMessage("OK");// TODO set from script
			isSuccessful = true;// TODO set from script
		} catch (NoClassDefFoundError ex) {
			log.warn("", ex);
			res.setResponseCode("500");
			res.setResponseMessage(ex.toString());
		} catch (Exception ex) {
			log.warn("", ex);
			res.setResponseCode("500");
			res.setResponseMessage(ex.toString());
		}

		res.sampleEnd();

		// Set if we were successful or not
		res.setSuccessful(isSuccessful);

		return res;
	}
}
