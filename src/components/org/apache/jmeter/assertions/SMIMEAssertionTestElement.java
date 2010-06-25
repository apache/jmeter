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

package org.apache.jmeter.assertions;

import java.io.Serializable;

import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.property.BooleanProperty;
import org.apache.jmeter.testelement.property.StringProperty;
import org.apache.jmeter.util.JMeterUtils;

public class SMIMEAssertionTestElement extends AbstractTestElement implements
        Serializable, Assertion {

    private static final long serialVersionUID = 1L;

    private static final String VERIFY_SIGNATURE_KEY = "SMIMEAssertionTestElement.verifySignature";

    private static final String NOT_SIGNED_KEY = "SMIMEAssertionTestElement.notSigned";

    private static final String SIGNER_NO_CHECK_KEY = "SMIMEAssertionTestElement.signerNoCheck";

    private static final String SIGNER_CHECK_BY_FILE_KEY = "SMIMEAssertionTestElement.signerCheckByFile";

    private static final String SIGNER_CERT_FILE_KEY = "SMIMEAssertionTestElement.signerCertFile";

    private static final String SINGER_CHECK_CONSTRAINTS_KEY = "SMIMEAssertionTestElement.signerCheckConstraints";

    private static final String SIGNER_SERIAL_KEY = "SMIMEAssertionTestElement.signerSerial";

    private static final String SIGNER_EMAIL_KEY = "SMIMEAssertionTestElement.signerEmail";

    private static final String SIGNER_DN_KEY = "SMIMEAssertionTestElement.signerDn";

    private static final String ISSUER_DN_KEY = "SMIMEAssertionTestElement.issuerDn";

    public SMIMEAssertionTestElement() {
        super();
    }

    public AssertionResult getResult(SampleResult response) {
        try {
            return SMIMEAssertion.getResult(this, response, getName());
        } catch (NoClassDefFoundError e) {
            AssertionResult assertionResult = new AssertionResult(getName());
            assertionResult.setError(true);
            assertionResult.setResultForFailure(JMeterUtils
                .getResString("bouncy_castle_unavailable_message"));
            return assertionResult;
        }
    }

    public boolean isVerifySignature() {
        return getPropertyAsBoolean(VERIFY_SIGNATURE_KEY);
    }

    public void setVerifySignature(boolean verifySignature) {
        setProperty(new BooleanProperty(VERIFY_SIGNATURE_KEY, verifySignature));
    }

    public String getIssuerDn() {
        return getPropertyAsString(ISSUER_DN_KEY);
    }

    public void setIssuerDn(String issuertDn) {
        setProperty(new StringProperty(ISSUER_DN_KEY, issuertDn));
    }

    public boolean isSignerCheckByFile() {
        return getPropertyAsBoolean(SIGNER_CHECK_BY_FILE_KEY);
    }

    public void setSignerCheckByFile(boolean signerCheckByFile) {
        setProperty(new BooleanProperty(SIGNER_CHECK_BY_FILE_KEY,
                signerCheckByFile));
    }

    public boolean isSignerCheckConstraints() {
        return getPropertyAsBoolean(SINGER_CHECK_CONSTRAINTS_KEY);
    }

    public void setSignerCheckConstraints(boolean signerCheckConstraints) {
        setProperty(new BooleanProperty(SINGER_CHECK_CONSTRAINTS_KEY,
                signerCheckConstraints));
    }

    public boolean isSignerNoCheck() {
        return getPropertyAsBoolean(SIGNER_NO_CHECK_KEY);
    }

    public void setSignerNoCheck(boolean signerNoCheck) {
        setProperty(new BooleanProperty(SIGNER_NO_CHECK_KEY, signerNoCheck));
    }

    public String getSignerCertFile() {
        return getPropertyAsString(SIGNER_CERT_FILE_KEY);
    }

    public void setSignerCertFile(String signerCertFile) {
        setProperty(new StringProperty(SIGNER_CERT_FILE_KEY, signerCertFile));
    }

    public String getSignerDn() {
        return getPropertyAsString(SIGNER_DN_KEY);
    }

    public void setSignerDn(String signerDn) {
        setProperty(new StringProperty(SIGNER_DN_KEY, signerDn));
    }

    public String getSignerSerial() {
        return getPropertyAsString(SIGNER_SERIAL_KEY);
    }

    public void setSignerSerial(String signerSerial) {
        setProperty(new StringProperty(SIGNER_SERIAL_KEY, signerSerial));
    }

    public String getSignerEmail() {
        return getPropertyAsString(SIGNER_EMAIL_KEY);
    }

    public void setSignerEmail(String signerEmail) {
        setProperty(new StringProperty(SIGNER_EMAIL_KEY, signerEmail));
    }

    public boolean isNotSigned() {
        return getPropertyAsBoolean(NOT_SIGNED_KEY);
    }

    public void setNotSigned(boolean notSigned) {
        setProperty(new BooleanProperty(NOT_SIGNED_KEY, notSigned));
    }

}
