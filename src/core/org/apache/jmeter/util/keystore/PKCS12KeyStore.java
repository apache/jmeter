/*
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 * if any, must include the following acknowledgment:
 * "This product includes software developed by the
 * Apache Software Foundation (http://www.apache.org/)."
 * Alternately, this acknowledgment may appear in the software itself,
 * if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 * "Apache JMeter" must not be used to endorse or promote products
 * derived from this software without prior written permission. For
 * written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 * "Apache JMeter", nor may "Apache" appear in their name, without
 * prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
package org.apache.jmeter.util.keystore;

import iaik.pkcs.pkcs12.PKCS12;
import iaik.pkcs.pkcs12.KeyBag;
import iaik.pkcs.pkcs12.CertificateBag;
import iaik.pkcs.PKCSParsingException;
import iaik.pkcs.PKCSException;
import iaik.x509.ChainVerifier;
import iaik.utils.Util;

import java.security.cert.X509Certificate;
import java.security.cert.CertificateException;
import java.security.PrivateKey;
import java.io.InputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 * Use this Keystore for iSaSiLk SSL Managers.
 *
 * @author <a href="bloritsch@apache.org">Berin Loritsch</a>
 * @version CVS $Revision$ $Date$
 */
public class PKCS12KeyStore extends JmeterKeyStore {
    /** The Certificate chain */
    private X509Certificate[] certChain;

    /** The private key */
    private PrivateKey key;

    /** The alias */
    private String alias;

    public PKCS12KeyStore(String type)
    throws Exception {
        if ( !"PKCS12".equalsIgnoreCase(type) ) {
            throw new Exception("Invalid keystore type");
        }
    }

    public final String getAlias() {
        return this.alias;
    }

    /**
     * Process PKCS12 input stream into the private key and certificate chain.
     */
    public void load(InputStream is, String pword)
    throws IOException, PKCSException, CertificateException {
        PKCS12 p12 = new PKCS12(is);
        is.close();

        p12.decrypt(pword.toCharArray());

        KeyBag keyBag = p12.getKeyBag();

        if (null == keyBag) {
            throw new PKCSException("No private key found");
        }

        byte[] keyBagLocalKeyId = keyBag.getLocalKeyID();

        this.key = keyBag.getPrivateKey();

        CertificateBag[] certBags = p12.getCertificateBags();
        if ((null == certBags) || (certBags.length == 0)) {
            throw new PKCSException("No certificates found");
        }

        this.alias = new String(keyBagLocalKeyId);
        X509Certificate myCert = null;

        for( int i=0; i < certBags.length; i++) {
            byte[] certBagLocalKeyId = certBags[i].getLocalKeyID();
            if ((null != keyBagLocalKeyId) && (null != certBagLocalKeyId)) {
                if (Arrays.equals(certBagLocalKeyId, keyBagLocalKeyId)) {
                    myCert = certBags[i].getCertificate();
                    break;
                }
            }
        }

        if (null == myCert) {
            throw new PKCSException("No owner certificate found");
        }

        iaik.x509.X509Certificate[] certChain = CertificateBag.getCertificates(certBags);
        this.certChain = Util.arrangeCertificateChain(certChain, false);
    }

    /**
     * Get the ordered certificate chain.
     */
    public final X509Certificate[] getCertificateChain() {
        return this.certChain;
    }

    /**
     * Return the private Key
     */
    public final PrivateKey getPrivateKey() {
        return this.key;
    }
}