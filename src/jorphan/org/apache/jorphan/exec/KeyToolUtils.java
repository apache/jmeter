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

package org.apache.jorphan.exec;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.JavaVersion;
import org.apache.commons.lang3.SystemUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * Utilities for working with Java keytool
 */
public class KeyToolUtils {
    private static final Logger log = LoggingManager.getLoggerForClass();

    // The DNAME which is used if none is provided
    private static final String DEFAULT_DNAME = "cn=JMeter Proxy (DO NOT TRUST)";  // $NON-NLS-1$

    private static final String DNAME_ROOT_KEY = "cn=Apache JMeter Proxy root (TEMPORARY TRUST ONLY)"; // $NON-NLS-1$
    private static final String DNAME_CA_KEY   = "cn=Apache JMeter Proxy server CA (TEMPORARY TRUST ONLY)"; // $NON-NLS-1$
    private static final String CACERT = "ApacheJMeterTemporaryCA.crt"; // $NON-NLS-1$
    private static final String ROOT_ALIAS = "root";  // $NON-NLS-1$
    public static final String CA_ALIAS = "ca";  // $NON-NLS-1$

    /** Does this class support generation of host certificates? */
    public static final boolean SUPPORTS_HOST_CERT = SystemUtils.isJavaVersionAtLeast(JavaVersion.JAVA_1_7);
    // i.e. does keytool support -gencert and -ext ?

    private KeyToolUtils() {
        // not instantiable
    }

    /**
     * Generate a self-signed keypair using the algorithm "RSA".
     * Requires Java 7 or later if the "ext" parameter is not null.
     *
     * @param keystore the keystore; if it already contains the alias the command will fail
     * @param alias the alias to use, not null
     * @param password the password to use for the store and the key
     * @param validity the validity period in days, greater than 0
     * @param dname the dname value, if omitted use "cn=JMeter Proxy (DO NOT TRUST)"
     * @param ext if not null, the extension (-ext) to add (e.g. "bc:c"). This requires Java 7.
     *
     * @throws IOException
     */
    public static void genkeypair(final File keystore, String alias, final String password, int validity, String dname, String ext)
            throws IOException {
        final File workingDir = keystore.getParentFile();
        final SystemCommand nativeCommand = new SystemCommand(workingDir, null);
        final List<String> arguments = new ArrayList<String>();
        arguments.add("keytool"); // $NON-NLS-1$
        arguments.add("-genkeypair"); // $NON-NLS-1$
        arguments.add("-alias"); // $NON-NLS-1$
        arguments.add(alias);
        arguments.add("-dname"); // $NON-NLS-1$
        arguments.add(dname == null ? DEFAULT_DNAME : dname);
        arguments.add("-keyalg"); // $NON-NLS-1$
        arguments.add("RSA"); // $NON-NLS-1$

        arguments.add("-keystore"); // $NON-NLS-1$
        arguments.add(keystore.getName());
        arguments.add("-storepass"); // $NON-NLS-1$
        arguments.add(password);
        arguments.add("-keypass"); // $NON-NLS-1$
        arguments.add(password);
        arguments.add("-validity"); // $NON-NLS-1$
        arguments.add(Integer.toString(validity));
        if (ext != null) { // Requires Java 7
            arguments.add("-ext"); // $NON-NLS-1$
            arguments.add(ext);
        }
        try {
            int exitVal = nativeCommand.run(arguments);
            if (exitVal != 0) {
                throw new IOException("Command failed, code: " + exitVal + "\n" + nativeCommand.getOutResult());
            }
        } catch (InterruptedException e) {
            throw new IOException("Command was interrupted\n" + nativeCommand.getOutResult(), e);
        }
    }

    /**
     * Create a self-signed CA certificate that can be used to sign SSL domain certificates.
     * The certificate file is created in the same directory as the keystore.
     * Requires Java 7 or later.
     *
     * @param keystore the keystore in which to store everything
     * @param password the password for keystore and keys
     * @param validity the validity period in days, must be greater than 0 
     *
     * @throws IOException
     */
    public static void generateProxyCA(File keystore, String password,  int validity) throws IOException {
        keystore.delete(); // any existing entries will be invalidated anyway
        // not strictly needed
        if(new File(CACERT).exists() && !new File(CACERT).delete()) {
            // Noop as we accept not to be able to delete it
            log.warn("Could not delete file:"+new File(CACERT).getAbsolutePath()+", will continue ignoring this");
        }
        // Create the self-signed keypairs (requires Java 7 for -ext flag)
        KeyToolUtils.genkeypair(keystore, ROOT_ALIAS, password, validity, DNAME_ROOT_KEY, "bc:c");
        KeyToolUtils.genkeypair(keystore, CA_ALIAS, password, validity, DNAME_CA_KEY, "bc:c");

        // Create cert for CA using root (requires Java 7 for gencert)
        ByteArrayOutputStream certReqOut = new ByteArrayOutputStream();
        // generate the request
        KeyToolUtils.keytool("-certreq", keystore, password, CA_ALIAS, null, certReqOut);

        // generate the certificate and store in output file
        InputStream certReqIn = new ByteArrayInputStream(certReqOut.toByteArray());
        KeyToolUtils.keytool("-gencert", keystore, password, CA_ALIAS, certReqIn, null, "-ext", "BC:0", "-outfile", CACERT);

        // import the signed CA cert into the store (root already there) - both are needed to sign the domain certificates
        KeyToolUtils.keytool("-importcert", keystore, password, CA_ALIAS, null, null, "-file", CACERT);
    }

    /**
     * Create a domain certificate (*.domain) and sign it with the CA certificate.
     * Requires Java 7 or later.
     *
     * @param keystore the keystore to use
     * @param password the password to use for the keystore and keys
     * @param domain the domain, e.g. apache.org
     * @param validity the validity period for the key
     *
     * @throws IOException
     *
     */
    public static void generateDomainCert(File keystore, String password, String domain, int validity) throws IOException {
        // generate the keypair for the domain
        generateSignedCert(keystore, password, validity, 
                domain,         // alias 
                "*." + domain); // subject
    }

    /**
     * Create a host certificate and sign it with the CA certificate.
     * Requires Java 7 or later.
     *
     * @param keystore the keystore to use
     * @param password the password to use for the keystore and keys
     * @param host the host, e.g. jmeter.apache.org
     * @param validity the validity period for the key
     *
     * @throws IOException
     *
     */
    public static void generateHostCert(File keystore, String password, String host, int validity) throws IOException {
        // generate the keypair for the host
        generateSignedCert(keystore, password, validity, 
                host,  // alias 
                host); // subject
    }

    private static void generateSignedCert(File keystore, String password,
            int validity, String alias, String subject) throws IOException {
        String dname = "cn=" + subject + ", o=JMeter Proxy (TEMPORARY TRUST ONLY";
        KeyToolUtils.genkeypair(keystore, alias, password, validity, dname, null);
        //rem generate cert for DOMAIN using CA (requires Java7 for gencert) and import it

        // get the certificate request
        ByteArrayOutputStream certReqOut = new ByteArrayOutputStream();
        KeyToolUtils.keytool("-certreq", keystore, password, alias, null, certReqOut);

        // create the certificate
        //rem ku:c=dig,keyE means KeyUsage:criticial=digitalSignature,keyEncipherment
        InputStream certReqIn = new ByteArrayInputStream(certReqOut.toByteArray());
        ByteArrayOutputStream certOut = new ByteArrayOutputStream();
        KeyToolUtils.keytool("-gencert", keystore, password, CA_ALIAS, certReqIn, certOut, "-ext", "ku:c=dig,keyE");

        // inport the certificate
        InputStream certIn = new ByteArrayInputStream(certOut.toByteArray());
        KeyToolUtils.keytool("-importcert", keystore, password, alias, certIn, null, "-noprompt");
    }

    /**
     * List the contents of a keystore
     *
     * @param keystore the keystore file
     * @param storePass the keystore password
     * @return the output from the command "keytool -list -v"
     */
    public static String list(final File keystore, final String storePass) throws IOException {
        final File workingDir = keystore.getParentFile();
        final SystemCommand nativeCommand = new SystemCommand(workingDir, null);
        final List<String> arguments = new ArrayList<String>();
        arguments.add("keytool"); // $NON-NLS-1$
        arguments.add("-list"); // $NON-NLS-1$
        arguments.add("-v"); // $NON-NLS-1$

        arguments.add("-keystore"); // $NON-NLS-1$
        arguments.add(keystore.getName());
        arguments.add("-storepass"); // $NON-NLS-1$
        arguments.add(storePass);
        try {
            int exitVal = nativeCommand.run(arguments);
            if (exitVal != 0) {
                throw new IOException("Command failed, code: " + exitVal + "\n" + nativeCommand.getOutResult());
            }
        } catch (InterruptedException e) {
            throw new IOException("Command was interrupted\n" + nativeCommand.getOutResult(), e);
        }
        return nativeCommand.getOutResult();
    }

    /**
     * Helper method to simplify chaining keytool commands.
     * 
     * @param command the command, not null
     * @param keystore the keystore, not nill
     * @param password the password used for keystore and key, not null
     * @param alias the alias, not null
     * @param input where to source input, may be null
     * @param output where to send output, may be null
     * @param parameters additional parameters to the command, may be null
     * @throws IOException
     */
    static void keytool(String command, File keystore, String password, String alias,
            InputStream input, OutputStream output, String ... parameters)
            throws IOException {
        final File workingDir = keystore.getParentFile();
        final SystemCommand nativeCommand = new SystemCommand(workingDir, 0L, 0, null, input, output, null);
        final List<String> arguments = new ArrayList<String>();
        arguments.add("keytool"); // $NON-NLS-1$
        arguments.add(command);
        arguments.add("-keystore"); // $NON-NLS-1$
        arguments.add(keystore.getName());
        arguments.add("-storepass"); // $NON-NLS-1$
        arguments.add(password);
        arguments.add("-keypass"); // $NON-NLS-1$
        arguments.add(password);
        arguments.add("-alias"); // $NON-NLS-1$
        arguments.add(alias);
        for (String parameter : parameters) {
            arguments.add(parameter);
        }

        try {
            int exitVal = nativeCommand.run(arguments);
            if (exitVal != 0) {
                throw new IOException("Command failed, code: " + exitVal + "\n" + nativeCommand.getOutResult());
            }
        } catch (InterruptedException e) {
            throw new IOException("Command was interrupted\n" + nativeCommand.getOutResult(), e);
        }
    }
}
