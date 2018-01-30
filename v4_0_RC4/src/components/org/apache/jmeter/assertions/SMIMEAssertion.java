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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.Security;
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

import org.apache.jmeter.samplers.SampleResult;
import org.apache.jorphan.util.JOrphanUtils;
import org.bouncycastle.asn1.x500.AttributeTypeAndValue;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationStore;
import org.bouncycastle.cms.SignerInformationVerifier;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.mail.smime.SMIMEException;
import org.bouncycastle.mail.smime.SMIMESignedParser;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.bc.BcDigestCalculatorProvider;
import org.bouncycastle.util.Store;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class which isolates the BouncyCastle code.
 */
class SMIMEAssertion {

    // Use the name of the test element, otherwise cannot enable/disable debug from the GUI
    private static final Logger log = LoggerFactory.getLogger(SMIMEAssertionTestElement.class);

    SMIMEAssertion() {
        super();
    }

    public static AssertionResult getResult(SMIMEAssertionTestElement testElement, SampleResult response, String name) {
        checkForBouncycastle();
        AssertionResult res = new AssertionResult(name);
        try {
            MimeMessage msg;
            final int msgPos = testElement.getSpecificMessagePositionAsInt();
            if (msgPos < 0){ // means counting from end
                SampleResult[] subResults = response.getSubResults();
                final int pos = subResults.length + msgPos;
                log.debug("Getting message number: {} of {}", pos, subResults.length);
                msg = getMessageFromResponse(response,pos);
            } else {
                log.debug("Getting message number: {}", msgPos);
                msg = getMessageFromResponse(response, msgPos);
            }
            
            SMIMESignedParser s = null;
            if(log.isDebugEnabled()) {
                log.debug("Content-type: {}", msg.getContentType());
            }
            if (msg.isMimeType("multipart/signed")) { // $NON-NLS-1$
                MimeMultipart multipart = (MimeMultipart) msg.getContent();
                s = new SMIMESignedParser(new BcDigestCalculatorProvider(), multipart);
            } else if (msg.isMimeType("application/pkcs7-mime") // $NON-NLS-1$
                    || msg.isMimeType("application/x-pkcs7-mime")) { // $NON-NLS-1$
                s = new SMIMESignedParser(new BcDigestCalculatorProvider(), msg);
            }

            if (null != s) {
                log.debug("Found signature");

                if (testElement.isNotSigned()) {
                    res.setFailure(true);
                    res.setFailureMessage("Mime message is signed");
                } else if (testElement.isVerifySignature() || !testElement.isSignerNoCheck()) {
                    res = verifySignature(testElement, s, name);
                }

            } else {
                log.debug("Did not find signature");
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
            res.setFailureMessage("Cannot extract signed body part from signature: "
                    + e.getMessage());
        } catch (IOException e) { // should never happen
            log.error("Cannot read mime message content: {}", e.getMessage(), e);
            res.setError(true);
            res.setFailureMessage(e.getMessage());
        }

        return res;
    }

    private static AssertionResult verifySignature(SMIMEAssertionTestElement testElement, SMIMESignedParser s, String name)
            throws CMSException {
        AssertionResult res = new AssertionResult(name);

        try {
            Store certs = s.getCertificates();
            SignerInformationStore signers = s.getSignerInfos();
            Iterator<?> signerIt = signers.getSigners().iterator();

            if (signerIt.hasNext()) {

                SignerInformation signer = (SignerInformation) signerIt.next();
                Iterator<?> certIt = certs.getMatches(signer.getSID()).iterator();

                if (certIt.hasNext()) {
                    // the signer certificate
                    X509CertificateHolder cert = (X509CertificateHolder) certIt.next();

                    if (testElement.isVerifySignature()) {

                        SignerInformationVerifier verifier = null;
                        try {
                            verifier = new JcaSimpleSignerInfoVerifierBuilder().setProvider("BC")
                                    .build(cert);
                        } catch (OperatorCreationException e) {
                            log.error("Can't create a provider.", e);
                        }
                        if (verifier == null || !signer.verify(verifier)) {
                            res.setFailure(true);
                            res.setFailureMessage("Signature is invalid");
                        }
                    }

                    if (testElement.isSignerCheckConstraints()) {
                        StringBuilder failureMessage = new StringBuilder();

                        String serial = testElement.getSignerSerial();
                        if (!JOrphanUtils.isBlank(serial)) {
                            BigInteger serialNbr = readSerialNumber(serial);
                            if (!serialNbr.equals(cert.getSerialNumber())) {
                                res.setFailure(true);
                                failureMessage
                                        .append("Serial number ")
                                        .append(serialNbr)
                                        .append(" does not match serial from signer certificate: ")
                                        .append(cert.getSerialNumber()).append("\n");
                            }
                        }

                        String email = testElement.getSignerEmail();
                        if (!JOrphanUtils.isBlank(email)) {
                            List<String> emailFromCert = getEmailFromCert(cert);
                            if (!emailFromCert.contains(email)) {
                                res.setFailure(true);
                                failureMessage
                                        .append("Email address \"")
                                        .append(email)
                                        .append("\" not present in signer certificate\n");
                            }

                        }

                        String subject = testElement.getSignerDn();
                        if (subject.length() > 0) {
                            final X500Name certPrincipal = cert.getSubject();
                            log.debug("DN from cert: {}", certPrincipal);
                            X500Name principal = new X500Name(subject);
                            log.debug("DN from assertion: {}", principal);
                            if (!principal.equals(certPrincipal)) {
                                res.setFailure(true);
                                failureMessage
                                        .append("Distinguished name of signer certificate does not match \"")
                                        .append(subject).append("\"\n");
                            }
                        }

                        String issuer = testElement.getIssuerDn();
                        if (issuer.length() > 0) {
                            final X500Name issuerX500Name = cert.getIssuer();
                            log.debug("IssuerDN from cert: {}", issuerX500Name);
                            X500Name principal = new X500Name(issuer);
                            log.debug("IssuerDN from assertion: {}", principal);
                            if (!principal.equals(issuerX500Name)) {
                                res.setFailure(true);
                                failureMessage
                                        .append("Issuer distinguished name of signer certificate does not match \"")
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
                        try (InputStream fis = new FileInputStream(testElement.getSignerCertFile());
                                InputStream bis = new BufferedInputStream(fis)){
                            X509CertificateHolder certFromFile = new JcaX509CertificateHolder((X509Certificate) cf.generateCertificate(bis));
                            if (!certFromFile.equals(cert)) {
                                res.setFailure(true);
                                res.setFailureMessage("Signer certificate does not match certificate "
                                                + testElement.getSignerCertFile());
                            }
                        } catch (IOException e) {
                            if (log.isDebugEnabled()) {
                                log.debug("Could not read cert file {}", testElement.getSignerCertFile(), e);
                            }
                            res.setFailure(true);
                            res.setFailureMessage("Could not read certificate file " + testElement.getSignerCertFile());
                        }

                        
                    }

                } else {
                    res.setFailure(true);
                    res.setFailureMessage("No signer certificate found in signature");
                }

            }

            // TODO support multiple signers
            if (signerIt.hasNext()) {
                log.warn("SMIME message contains multiple signers! Checking multiple signers is not supported.");
            }

        } catch (GeneralSecurityException e) {
            log.error(e.getMessage(), e);
            res.setError(true);
            res.setFailureMessage(e.getMessage());
        }

        return res;
    }

    /**
     * extracts a MIME message from the SampleResult
     */
    private static MimeMessage getMessageFromResponse(SampleResult response,
            int messageNumber) throws MessagingException {
        SampleResult[] subResults = response.getSubResults();

        if (messageNumber >= subResults.length || messageNumber < 0) {
            throw new MessagingException("Message number not present in results: "+messageNumber);
        }

        final SampleResult sampleResult = subResults[messageNumber];
        if(log.isDebugEnabled()) {
            log.debug("Bytes: {}, Content Type: {}", sampleResult.getBytesAsLong(), sampleResult.getContentType());
        }
        byte[] data = sampleResult.getResponseData();
        Session session = Session.getDefaultInstance(new Properties());
        MimeMessage msg = new MimeMessage(session, new ByteArrayInputStream(data));

        if(log.isDebugEnabled()) {
            log.debug("msg.getSize() = {}", msg.getSize());
        }
        return msg;
    }

    /**
     * Convert the value of <code>serialString</code> into a BigInteger. Strings
     * starting with 0x or 0X are parsed as hex numbers, otherwise as decimal
     * number.
     * 
     * @param serialString
     *            the String representation of the serial Number
     * @return the BitInteger representation of the serial Number
     */
    private static BigInteger readSerialNumber(String serialString) {
        if (serialString.startsWith("0x") || serialString.startsWith("0X")) { // $NON-NLS-1$  // $NON-NLS-2$
            return new BigInteger(serialString.substring(2), 16);
        } 
        return new BigInteger(serialString);
    }

    /**
     * Extract email addresses from a certificate
     * 
     * @param cert the X509 certificate holder
     * @return a List of all email addresses found
     * @throws CertificateException
     */
    private static List<String> getEmailFromCert(X509CertificateHolder cert)
            throws CertificateException {
        List<String> res = new ArrayList<>();

        X500Name subject = cert.getSubject();
        for (RDN emails : subject.getRDNs(BCStyle.EmailAddress)) {
            for (AttributeTypeAndValue emailAttr: emails.getTypesAndValues()) {
                if (log.isDebugEnabled()) {
                    log.debug("Add email from RDN: {}", IETFUtils.valueToString(emailAttr.getValue()));
                }
                res.add(IETFUtils.valueToString(emailAttr.getValue()));
            }
        }

        Extension subjectAlternativeNames = cert
                .getExtension(Extension.subjectAlternativeName);
        if (subjectAlternativeNames != null) {
            for (GeneralName name : GeneralNames.getInstance(
                    subjectAlternativeNames.getParsedValue()).getNames()) {
                if (name.getTagNo() == GeneralName.rfc822Name) {
                    String email = IETFUtils.valueToString(name.getName());
                    log.debug("Add email from subjectAlternativeName: {}", email);
                    res.add(email);
                }
            }
        }

        return res;
    }

    /**
     * Check if the Bouncycastle jce provider is installed and dynamically load
     * it, if needed;
     */
    private static void checkForBouncycastle() {
        if (null == Security.getProvider("BC")) { // $NON-NLS-1$
            Security.addProvider(new BouncyCastleProvider());
        }
    }
}
