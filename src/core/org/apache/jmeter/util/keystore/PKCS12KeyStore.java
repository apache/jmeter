// $Header$
/*
 * Copyright 2001-2004 The Apache Software Foundation.
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
public class PKCS12KeyStore extends JmeterKeyStore
{
    /** The Certificate chain */
    private X509Certificate[] certChain;

    /** The private key */
    private PrivateKey key;

    /** The alias */
    private String alias;

    public PKCS12KeyStore(String type) throws Exception
    {
        if (!"PKCS12".equalsIgnoreCase(type))
        {
            throw new Exception("Invalid keystore type");
        }
    }

    public final String getAlias()
    {
        return this.alias;
    }

    /**
     * Process PKCS12 input stream into the private key and certificate chain.
     */
    public void load(InputStream is, String pword)
        throws IOException, PKCSException, CertificateException
    {
        PKCS12 p12 = new PKCS12(is);
        is.close();

        p12.decrypt(pword.toCharArray());

        KeyBag keyBag = p12.getKeyBag();

        if (null == keyBag)
        {
            throw new PKCSException("No private key found");
        }

        byte[] keyBagLocalKeyId = keyBag.getLocalKeyID();

        this.key = keyBag.getPrivateKey();

        CertificateBag[] certBags = p12.getCertificateBags();
        if ((null == certBags) || (certBags.length == 0))
        {
            throw new PKCSException("No certificates found");
        }

        this.alias = new String(keyBagLocalKeyId);
        X509Certificate myCert = null;

        for (int i = 0; i < certBags.length; i++)
        {
            byte[] certBagLocalKeyId = certBags[i].getLocalKeyID();
            if ((null != keyBagLocalKeyId) && (null != certBagLocalKeyId))
            {
                if (Arrays.equals(certBagLocalKeyId, keyBagLocalKeyId))
                {
                    myCert = certBags[i].getCertificate();
                    break;
                }
            }
        }

        if (null == myCert)
        {
            throw new PKCSException("No owner certificate found");
        }

        iaik.x509.X509Certificate[] certChain =
            CertificateBag.getCertificates(certBags);
        this.certChain = Util.arrangeCertificateChain(certChain, false);
    }

    /**
     * Get the ordered certificate chain.
     */
    public final X509Certificate[] getCertificateChain()
    {
        return this.certChain;
    }

    /**
     * Return the private Key
     */
    public final PrivateKey getPrivateKey()
    {
        return this.key;
    }
}