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

/**
 * MD5HexAssertion class creates an MD5 checksum from the response <br/>
 * and matches it with the MD5 hex provided.
 * The assertion will fail when the expected hex is different from the <br/>
 * one calculated from the response OR when the expected hex is left empty.
 * 
 * @author	<a href="mailto:jh@domek.be">Jorg Heymans</a>
 * @version $Revision$ last updated $Date$
 */
package org.apache.jmeter.assertions;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;

import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.property.StringProperty;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

public class MD5HexAssertion extends AbstractTestElement implements Serializable, Assertion {

	private static final Logger log = LoggingManager.getLoggerForClass();

	/** Key for storing assertion-informations in the jmx-file. */
	private static final String MD5HEX_KEY = "MD5HexAssertion.size";

	/*
	 * @param response @return
	 */
	public AssertionResult getResult(SampleResult response) {

		AssertionResult result = new AssertionResult();
		result.setFailure(false);
		byte[] resultData = response.getResponseData();

		if (resultData.length == 0) {
			result.setError(false);
			result.setFailure(true);
			result.setFailureMessage("Response was null");
			return result;
		}

		// no point in checking if we don't have anything to compare against
		if (getAllowedMD5Hex().equals("")) {
			result.setError(false);
			result.setFailure(true);
			result.setFailureMessage("MD5Hex to test against is empty");
			return result;
		}

		String md5Result = baMD5Hex(resultData);

		// String md5Result = DigestUtils.md5Hex(resultData);

		if (!md5Result.equalsIgnoreCase(getAllowedMD5Hex())) {
			result.setFailure(true);

			Object[] arguments = { md5Result, getAllowedMD5Hex() };
			String message = MessageFormat.format(JMeterUtils.getResString("md5hex_assertion_failure"), arguments);
			result.setFailureMessage(message);

		}

		return result;
	}

	public void setAllowedMD5Hex(String hex) {
		setProperty(new StringProperty(MD5HexAssertion.MD5HEX_KEY, hex));
	}

	public String getAllowedMD5Hex() {
		return getPropertyAsString(MD5HexAssertion.MD5HEX_KEY);
	}

	// package protected so can be accessed by test class
	static String baToHex(byte ba[]) {
		StringBuffer sb = new StringBuffer(32);
		for (int i = 0; i < ba.length; i++) {
			int j = ba[i] & 0xff;
			if (j < 16)
				sb.append("0");
			sb.append(Integer.toHexString(j));
		}
		return sb.toString();
	}

	// package protected so can be accessed by test class
	static String baMD5Hex(byte ba[]) {
		byte[] md5Result = {};

		try {
			MessageDigest md;
			md = MessageDigest.getInstance("MD5");
			md5Result = md.digest(ba);
		} catch (NoSuchAlgorithmException e) {
			log.error("", e);
		}
		return baToHex(md5Result);
	}
}
