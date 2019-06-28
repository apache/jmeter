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
import org.apache.jorphan.util.JOrphanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MD5HexAssertion extends AbstractTestElement implements Serializable, Assertion {

    private static final long serialVersionUID = 241L;

    private static final Logger log = LoggerFactory.getLogger(MD5HexAssertion.class);

    /** Key for storing assertion-information in the jmx-file. */
    private static final String MD5HEX_KEY = "MD5HexAssertion.size";

    @Override
    public AssertionResult getResult(SampleResult response) {

        AssertionResult result = new AssertionResult(getName());
        result.setFailure(false);
        byte[] resultData = response.getResponseData();

        if (resultData.length == 0) {
            result.setError(false);
            result.setFailure(true);
            result.setFailureMessage("Response was null");
            return result;
        }

        // no point in checking if we don't have anything to compare against
        if (getAllowedMD5Hex().isEmpty()) {
            result.setError(false);
            result.setFailure(true);
            result.setFailureMessage("MD5Hex to test against is empty");
            return result;
        }

        String md5Result = md5Hex(resultData);

        if (!md5Result.equalsIgnoreCase(getAllowedMD5Hex())) {
            result.setFailure(true);

            Object[] arguments = { md5Result, getAllowedMD5Hex() };
            String message = MessageFormat.format(
                    JMeterUtils.getResString("md5hex_assertion_failure"), // $NON-NLS-1$
                    arguments);
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

    private static String md5Hex(byte[] bytes) {
        byte[] md5Result = {};

        try {
            md5Result = MessageDigest.getInstance("MD5").digest(bytes);
        } catch (NoSuchAlgorithmException e) {
            log.error("Message digestion failed.", e);
        }
        return JOrphanUtils.baToHexString(md5Result);
    }
}
