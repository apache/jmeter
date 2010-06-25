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

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.Security;
import java.security.cert.CertStore;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.security.auth.x500.X500Principal;

import org.apache.jmeter.samplers.SampleResult;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationStore;
import org.bouncycastle.jce.PrincipalUtil;
import org.bouncycastle.jce.X509Principal;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.mail.smime.SMIMEException;
import org.bouncycastle.mail.smime.SMIMESignedParser;
import org.bouncycastle.x509.extension.X509ExtensionUtil;

public class SMIMEAssertion {

    private static final Logger log = LoggingManager.getLoggerForClass();

    public SMIMEAssertion() {
        super();
    }

    public static AssertionResult getResult(SMIMEAssertionTestElement testElement, SampleResult response, String name) {
        checkForBouncycastle();
        AssertionResult res = new AssertionResult(name);

        try {
            MimeMessage msg = getMessageFromResponse(response, 0);
            SMIMESignedParser s = null;
            if (msg.isMimeType("multipart/signed")) {
                MimeMultipart multipart = (MimeMultipart) msg.getContent();
                s = new SMIMESignedParser(multipart);
            } else if (msg.isMimeType("application/pkcs7-mime")
                    || msg.isMimeType("application/x-pkcs7-mime")) {
                s = new SMIMESignedParser(msg);
            }

            if (null != s) {

                if (testElement.isNotSigned()) {
                    res.setFailure(true);
                    res.setFailureMessage("Mime message is signed");
                } else if (testElement.isVerifySignature() || !testElement.isSignerNoCheck()) {
                    res = verifySignature(testElement, s, name);
                }

            } else {
                if (!testElement.isNotSigned()) {
                    res.setFailure(true);
                    res.setFailureMessage("Mime message is not signed");
                }
            }

        } catch (MessagingException e) {
            String msg = "Cannot parse mime msg: " + e.getMessage();
            log.warn(msg, e);
            res.setFailure(true);
            res.setFailureMessage(msg);
        } catch (CMSException e) {
            res.setFailure(true);
            res.setFailureMessage("Error reading the signature: "
                    + e.getMessage());
        } catch (SMIMEException e) {
            res.setFailure(true);
            res
                    .setFailureMessage("Cannot extract signed body part from signature: "
                            + e.getMessage());
        } catch (IOException e) { // should never happen
            log.error("Cannot read mime message content: " + e.getMessage(), e);
            res.setError(true);
            res.setFailureMessage(e.getMessage());
        }

        return res;
    }

    private static AssertionResult verifySignature(SMIMEAssertionTestElement testElement, SMIMESignedParser s, String name)
            throws CMSException {
        AssertionResult res = new AssertionResult(name);

        try {
            CertStore certs = s.getCertificatesAndCRLs("Collection", "BC");
            SignerInformationStore signers = s.getSignerInfos();
            Iterator<?> signerIt = signers.getSigners().iterator();

            if (signerIt.hasNext()) {

                SignerInformation signer = (SignerInformation) signerIt.next();
                Iterator<?> certIt = certs.getCertificates(signer.getSID())
                        .iterator();

                if (certIt.hasNext()) {
                    // the signer certificate
                    X509Certificate cert = (X509Certificate) certIt.next();

                    if (testElement.isVerifySignature()) {

                        if (!signer.verify(cert.getPublicKey(), "BC")) {
                            res.setFailure(true);
                            res.setFailureMessage("Signature is invalid");
                        }
                    }

                    if (testElement.isSignerCheckConstraints()) {
                        StringBuffer failureMessage = new StringBuffer();

                        String serial = testElement.getSignerSerial();
                        if (serial.trim().length() > 0) {
                            BigInteger serialNbr = readSerialNumber(serial);
                            if (!serialNbr.equals(cert.getSerialNumber())) {
                                res.setFailure(true);
                                failureMessage
                                        .append("Serial number ")
                                        .append(serialNbr)
                                        .append(
                                                " does not match serial from signer certificate: ")
                                        .append(cert.getSerialNumber()).append(
                                                "\n");
                            }
                        }

                        String email = testElement.getSignerEmail();
                        if (email.trim().length() > 0) {
                            List<String> emailfromCert = getEmailFromCert(cert);
                            if (!emailfromCert.contains(email)) {
                                res.setFailure(true);
                                failureMessage
                                        .append("Email address \"")
                                        .append(email)
                                        .append(
                                                "\" not present in signer certificate\n");
                            }

                        }

                        String subject = testElement.getSignerDn();
                        if (subject.length() > 0) {
                            X500Principal principal = new X500Principal(subject);
                            if (!principal.equals(cert
                                    .getSubjectX500Principal())) {
                                res.setFailure(true);
                                failureMessage
                                        .append(
                                                "Distinguished name of signer certificate does not match \"")
                                        .append(subject).append("\"\n");
                            }
                        }

                        String issuer = testElement.getIssuerDn();
                        if (issuer.length() > 0) {
                            X500Principal principal = new X500Principal(issuer);
                            if (!principal
                                    .equals(cert.getIssuerX500Principal())) {
                                res.setFailure(true);
                                failureMessage
                                        .append(
                                                "Issuer distinguished name of signer certificate does not match \"")
                                        .append(subject).append("\"\n");
                            }
                        }

                        if (failureMessage.length() > 0) {
                            res.setFailureMessage(failureMessage.toString());
                        }
                    }

                    if (testElement.isSignerCheckByFile()) {
                        CertificateFactory cf = CertificateFactory
                                .getInstance("X.509");
                        X509Certificate certFromFile = (X509Certificate) cf
                                .generateCertificate(new FileInputStream(
                                        testElement.getSignerCertFile()));

                        if (!certFromFile.equals(cert)) {
                            res.setFailure(true);
                            res
                                    .setFailureMessage("Signer certificate does not match certificate "
                                            + testElement.getSignerCertFile());
                        }
                    }

                } else {
                    res.setFailure(true);
                    res
                            .setFailureMessage("No signer certificate found in signature");
                }

            }

            // TODO support multiple signers
            if (signerIt.hasNext()) {
                log
                        .warn("SMIME message contains multiple signers! Checking multiple signers is not supported.");
            }

        } catch (GeneralSecurityException e) {
            log.error(e.getMessage(), e);
            res.setError(true);
            res.setFailureMessage(e.getMessage());
        } catch (FileNotFoundException e) {
            res.setFailure(true);
            res.setFailureMessage("certificate file not found: "
                    + e.getMessage());
        }

        return res;
    }

    /**
     * extracts a MIME message from the SampleResult
     */
    private static MimeMessage getMessageFromResponse(SampleResult response,
            int messageNumber) throws MessagingException {
        SampleResult subResults[] = response.getSubResults();

        if (messageNumber >= subResults.length) throw new MessagingException("Message number not present in results: "+messageNumber);

        byte[] data = subResults[messageNumber].getResponseData();
        Session session = Session.getDefaultInstance(new Properties());
        MimeMessage msg = new MimeMessage(session, new ByteArrayInputStream(
                data));

        log.debug("msg.getSize() = " + msg.getSize());
        return msg;
    }

    /**
     * Convert the value of <code>serialString</code> into a BigInteger. Strings
     * starting with 0x or 0X are parsed as hex numbers, otherwise as decimal
     * number.
     * 
     * @param serialString
     *            the String representation of the serial Number
     * @return
     */
    private static BigInteger readSerialNumber(String serialString) {
        if (serialString.startsWith("0x") || serialString.startsWith("0X")) {
            return new BigInteger(serialString.substring(2), 16);
        } 
        return new BigInteger(serialString);
    }

    /**
     * Extract email addresses from a certificate
     * 
     * @param cert
     * @return a List of all email addresses found
     * @throws CertificateException
     */
    private static List<String> getEmailFromCert(X509Certificate cert)
            throws CertificateException {
        List<String> res = new ArrayList<String>();

        X509Principal subject = PrincipalUtil.getSubjectX509Principal(cert);
        Iterator<?> addressIt = subject.getValues(X509Principal.EmailAddress)
                .iterator();
        while (addressIt.hasNext()) {
            String address = (String) addressIt.next();
            res.add(address);
        }

        Iterator<?> subjectAltNamesIt = X509ExtensionUtil
                .getSubjectAlternativeNames(cert).iterator();
        while (subjectAltNamesIt.hasNext()) {
            List<?> altName = (List<?>) subjectAltNamesIt.next();
            int type = ((Integer) altName.get(0)).intValue();
            if (type == GeneralName.rfc822Name) {
                String address = (String) altName.get(1);
                res.add(address);
            }
        }

        return res;
    }

    /**
     * Check if the Bouncycastle jce provider is installed and dynamically load
     * it, if needed;
     * 
     */
    private static void checkForBouncycastle() {
        if (null == Security.getProvider("BC")) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }
}
