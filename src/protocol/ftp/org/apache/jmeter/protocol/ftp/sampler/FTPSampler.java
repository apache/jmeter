// $Header$
/*
 * Copyright 2001-2005 The Apache Software Foundation.
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

package org.apache.jmeter.protocol.ftp.sampler;

import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;

/**
 * A sampler which understands FTP file requests.
 * 
 * @version $Revision$ last updated $Date$
 */
public class FTPSampler extends AbstractSampler {
	public final static String SERVER = "FTPSampler.server";

	public final static String FILENAME = "FTPSampler.filename";

	public FTPSampler() {
	}

	public String getUsername() {
		return getPropertyAsString(ConfigTestElement.USERNAME);
	}

	public String getPassword() {
		return getPropertyAsString(ConfigTestElement.PASSWORD);
	}

	public void setServer(String newServer) {
		this.setProperty(SERVER, newServer);
	}

	public String getServer() {
		return getPropertyAsString(SERVER);
	}

	public void setFilename(String newFilename) {
		this.setProperty(FILENAME, newFilename);
	}

	public String getFilename() {
		return getPropertyAsString(FILENAME);
	}

	/**
	 * Returns a formatted string label describing this sampler Example output:
	 * ftp://ftp.nowhere.com/pub/README.txt
	 * 
	 * @return a formatted string label describing this sampler
	 */
	public String getLabel() {
		return ("ftp://" + this.getServer() + "/" + this.getFilename());
	}

	public SampleResult sample(Entry e) {
		SampleResult res = new SampleResult();
		boolean isSuccessful = false;
		// FtpConfig ftpConfig = (FtpConfig)e.getConfigElement(FtpConfig.class);
		res.setSampleLabel(getName());
		// LoginConfig loginConfig =
		// (LoginConfig)e.getConfigElement(LoginConfig.class);
		res.sampleStart();
		try {
			FtpClient ftp = new FtpClient();
			ftp.connect(getServer(), getUsername(), getPassword());
			ftp.setPassive(true);
			// this should probably come from the setup dialog
			String s = ftp.get(getFilename());
			res.setResponseData(s.getBytes());
			// TODO set the response code here somewhere
			ftp.disconnect();
			isSuccessful = true;
		} catch (java.net.ConnectException cex) {
			// java.net.ConnectException -- 502 error code?
			// in the future, possibly define and place error codes into the
			// result so we know exactly what happened.
			res.setResponseData(cex.toString().getBytes());
		} catch (Exception ex) {
			// general exception
			res.setResponseData(ex.toString().getBytes());
		}

		res.sampleEnd();

		// Set if we were successful or not
		res.setSuccessful(isSuccessful);

		return res;
	}
}
