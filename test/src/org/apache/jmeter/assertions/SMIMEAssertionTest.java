package org.apache.jmeter.assertions;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import junit.framework.TestCase;

import org.apache.jmeter.samplers.SampleResult;

/**
 * Tests for the SMIMEAssertion.
 * <p>
 * 
 * A signed email named {@code signed_email.eml} is needed to complete these
 * tests. The one included was generated with the following script:
 * 
 * <pre>
 * {@code
 * # Create a key without a password
 * openssl genrsa -out email.key 2048 -nodes
 * 
 * # Create a config for the certificate request
 * cat > email.cnf <<EOF
 * [req]
 * prompt = no
 * distinguished_name = dn
 * req_extensions = ext
 * 
 * [dn]
 * CN = alice example
 * emailAddress = alice@a.example.com
 * O = Example Ltd
 * L = Wherever
 * C = AU
 * 
 * [ext]
 * subjectAltName = email:alice@a.example.com,email:bob@b.example.com,email:charlie@example.com,DNS:notused.example.com
 * EOF
 * 
 * # Create a certificate request
 * openssl req -new -config email.cnf -key email.key -out email.csr
 * 
 * cat > email.ext <<EOF
 * subjectAltName = email:alice@a.example.com,email:bob@b.example.com,email:charlie@example.com
 * EOF
 * 
 * # Self-Sign the request and add the subjectAltName
 * openssl x509 -req -days 365 -in email.csr -signkey email.key -out email.pem -extfile email.ext
 * 
 * # Create a simple email text
 * cat >email.txt <<EOF
 * Content-type: text/plain
 * 
 * This was really written by me.
 * EOF
 * 
 * # Create the smime with the generated key and the simple mail
 * openssl smime -sign -from bob@b.example.com -to someone@example.com \
 *               -subject "Signed Message" -signer email.pem \
 *               -in email.txt -inkey email.key -out signed_email.eml
 * }
 * </pre>
 * 
 * If a new signed email is generated, the signer key and certificate will
 * change, and thus the tests will have to be changed as well (serial number!).
 */
public class SMIMEAssertionTest extends TestCase {

    private MimeMessage msg;
    private SampleResult parent;

    public void setUp() throws MessagingException, IOException {
        Session mailSession = Session.getDefaultInstance(new Properties());
        msg = new MimeMessage(mailSession, this.getClass().getResourceAsStream(
                "signed_email.eml"));
        parent = new SampleResult();
        parent.sampleStart();
        parent.addSubResult(createChildSample());
    }

    public void testSignature() {
        SMIMEAssertionTestElement testElement = new SMIMEAssertionTestElement();
        testElement.setVerifySignature(true);
        AssertionResult result = SMIMEAssertion.getResult(testElement, parent,
                "Test");
        assertFalse("Result should not be an error", result.isError());
        assertFalse("Result should not fail: " + result.getFailureMessage(),
                result.isFailure());
    }

    public void testSignerEmail() {
        SMIMEAssertionTestElement testElement = new SMIMEAssertionTestElement();
        testElement.setSignerCheckConstraints(true);
        testElement.setSignerEmail("bob@b.example.com");
        AssertionResult result = SMIMEAssertion.getResult(testElement, parent,
                "Test");
        assertFalse("Result should not be an error", result.isError());
        assertFalse("Result should not fail: " + result.getFailureMessage(),
                result.isFailure());
    }

    public void testSignerSerial() {
        SMIMEAssertionTestElement testElement = new SMIMEAssertionTestElement();
        testElement.setSignerCheckConstraints(true);
        testElement.setSignerSerial("0xc8c46f8fbf9ebea4");
        AssertionResult result = SMIMEAssertion.getResult(testElement, parent,
                "Test");
        assertFalse("Result should not be an error", result.isError());
        assertFalse("Result should not fail: " + result.getFailureMessage(),
                result.isFailure());
    }

    public void testSignerSignerDN() {
        SMIMEAssertionTestElement testElement = new SMIMEAssertionTestElement();
        testElement.setSignerCheckConstraints(true);
//        String signerDn = "CN=alice example, E=alice@a.example.com, O=Example Ltd, L=Wherever, C=AU";
        String signerDn = "C=AU, L=Wherever, O=Example Ltd, E=alice@a.example.com, CN=alice example";
        testElement
                .setSignerDn(signerDn);
        AssertionResult result = SMIMEAssertion.getResult(testElement, parent,
                "Test");
        assertFalse("Result should not be an error", result.isError());
        assertFalse("Result should not fail: " + result.getFailureMessage(),
                result.isFailure());
    }

    public void testSignerIssuerDN() {
        SMIMEAssertionTestElement testElement = new SMIMEAssertionTestElement();
        testElement.setSignerCheckConstraints(true);
        String issuerDn = "C=AU, L=Wherever, O=Example Ltd, E=alice@a.example.com, CN=alice example";
        testElement
                .setIssuerDn(issuerDn);
        AssertionResult result = SMIMEAssertion.getResult(testElement, parent,
                "Test");
        assertFalse("Result should not be an error", result.isError());
        assertFalse("Result should not fail: " + result.getFailureMessage(),
                result.isFailure());
    }

    public void testSignerCert() {
        SMIMEAssertionTestElement testElement = new SMIMEAssertionTestElement();
        testElement.setSignerCheckConstraints(true);
        testElement.setSignerCheckByFile(true);
        testElement.setSignerCertFile(getClass().getResource("email.pem")
                .getPath());
        AssertionResult result = SMIMEAssertion.getResult(testElement, parent,
                "Test");
        assertFalse("Result should not be an error", result.isError());
        assertFalse("Result should not fail: " + result.getFailureMessage(),
                result.isFailure());
    }

    private SampleResult createChildSample() throws MessagingException,
            IOException {
        SampleResult child = new SampleResult();
        child.setSampleLabel("Message " + msg.getMessageNumber());
        child.setContentType(msg.getContentType());
        child.setEncodingAndType(msg.getContentType());
        ByteArrayOutputStream outbuf = new ByteArrayOutputStream();
        msg.writeTo(outbuf);
        child.setResponseData(outbuf.toByteArray());
        child.setDataType(SampleResult.TEXT);
        child.setResponseOK();
        return child;
    }

}
