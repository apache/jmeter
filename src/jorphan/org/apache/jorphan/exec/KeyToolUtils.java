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
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilities for working with Java keytool
 */
public class KeyToolUtils {
    private static final Logger log = LoggerFactory.getLogger(KeyToolUtils.class);

    // The DNAME which is used if none is provided
    private static final String DEFAULT_DNAME = "cn=JMeter Proxy (DO NOT TRUST)";  // $NON-NLS-1$

    // N.B. It seems that Opera needs a chain in order to accept server keys signed by the intermediate CA
    // Opera does not seem to like server keys signed by the root (self-signed) cert.

    private static final String DNAME_ROOT_CA_KEY;

    private static final String KEYTOOL = "keytool";

    /** Name of property that can be used to override the default keytool location */
    private static final String KEYTOOL_DIRECTORY = "keytool.directory"; // $NON-NLS-1$
    
    private static final String DNAME_INTERMEDIATE_CA_KEY  = "cn=JMeter Intermediate CA for recording (INSTALL ONLY IF IT S YOURS)"; // $NON-NLS-1$

    public static final String ROOT_CACERT_CRT_PFX = "ApacheJMeterTemporaryRootCA"; // $NON-NLS-1$ (do not change)
    private static final String ROOT_CACERT_CRT = ROOT_CACERT_CRT_PFX + ".crt"; // $NON-NLS-1$ (Firefox and Windows)
    private static final String ROOT_CACERT_USR = ROOT_CACERT_CRT_PFX + ".usr"; // $NON-NLS-1$ (Opera)

    private static final String ROOTCA_ALIAS = ":root_ca:";  // $NON-NLS-1$
    private static final String INTERMEDIATE_CA_ALIAS = ":intermediate_ca:";  // $NON-NLS-1$


    /**
     * Where to find the keytool application.
     * If <code>null</code>, then keytool cannot be found.
     */
    private static final String KEYTOOL_PATH;


    static {
        StringBuilder sb = new StringBuilder();
        
        sb.append("CN=_ JMeter Root CA for recording (INSTALL ONLY IF IT S YOURS)"); // $NON-NLS-1$
        String userName = System.getProperty("user.name"); // $NON-NLS-1$
        userName = userName.replace('\\','/'); // Backslash is special (Bugzilla 56178)
        addElement(sb, "OU=Username: ", userName); // $NON-NLS-1$
        addElement(sb, "C=", System.getProperty("user.country")); // $NON-NLS-1$ $NON-NLS-2$
        DNAME_ROOT_CA_KEY = sb.toString();

        // Try to find keytool application
        // N.B. Cannot use JMeter property from jorphan jar.
        final String keytoolDir = System.getProperty(KEYTOOL_DIRECTORY);

        String keytoolPath; // work field
        if (keytoolDir != null) {
            keytoolPath = new File(new File(keytoolDir), KEYTOOL).getPath();
            if (!checkKeytool(keytoolPath)) {
                log.error("Cannot find keytool using property {}={}", KEYTOOL_DIRECTORY, keytoolDir);
                keytoolPath = null; // don't try anything else if the property is provided
            }
        } else {
            keytoolPath = KEYTOOL;
            if (!checkKeytool(keytoolPath)) { // Not found on PATH, check Java Home
                File javaHome = SystemUtils.getJavaHome();
                if (javaHome != null) {
                    keytoolPath = new File(new File(javaHome, "bin"), KEYTOOL).getPath(); // $NON-NLS-1$
                    if (!checkKeytool(keytoolPath)) {
                        keytoolPath = null;
                    }
                } else {
                    keytoolPath = null;
                }
            }
        }
        if (keytoolPath == null) {
            log.error("Unable to find keytool application. Check PATH or define system property {}", KEYTOOL_DIRECTORY);
        } else {
            log.info("keytool found at '{}'", keytoolPath);
        }
        KEYTOOL_PATH = keytoolPath;
    }


    private KeyToolUtils() {
        // not instantiable
    }


    private static void addElement(StringBuilder sb, String prefix, String value) {
        if (value != null) {
            sb.append(", ");
            sb.append(prefix);
            sb.append(value);
        }
    }
    
    /**
     * Generate a self-signed keypair using the algorithm "RSA".
     *
     * @param keystore the keystore; if it already contains the alias the command will fail
     * @param alias the alias to use, not null
     * @param password the password to use for the store and the key
     * @param validity the validity period in days, greater than 0
     * @param dname the <em>distinguished name</em> value, if omitted use "cn=JMeter Proxy (DO NOT TRUST)"
     * @param ext if not null, the extension (-ext) to add (e.g. "bc:c").
     *
     * @throws IOException if keytool was not configured or running keytool application fails
     */
    public static void genkeypair(final File keystore, String alias, final String password, int validity, String dname, String ext)
            throws IOException {
        final File workingDir = keystore.getParentFile();
        final SystemCommand nativeCommand = new SystemCommand(workingDir, null);
        final List<String> arguments = new ArrayList<>();
        arguments.add(getKeyToolPath());
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
        if (ext != null) {
            arguments.add("-ext"); // $NON-NLS-1$
            arguments.add(ext);
        }
        try {
            int exitVal = nativeCommand.run(arguments);
            if (exitVal != 0) {
                throw new IOException("  >> " + nativeCommand.getOutResult().trim() + " <<"
                    + "\nCommand failed, code: " + exitVal
                    + "\n'" + formatCommand(arguments)+"'");
            }
        } catch (InterruptedException e) { // NOSONAR
            throw new IOException("Command was interrupted\n" + nativeCommand.getOutResult(), e);
        }
    }

    /**
     * Formats arguments
     * @param arguments
     * @return String command line
     */
    private static String formatCommand(List<String> arguments) {
        StringBuilder builder = new StringBuilder();
        boolean redact = false; // whether to redact next parameter
        for (String string : arguments) {
            final boolean quote = string.contains(" ");
            if (quote) {
                builder.append("\"");
            }
            builder.append(redact ? "{redacted}" : string);
            if (quote) {
                builder.append("\"");
            }
            builder.append(" ");
            redact = "-storepass".equals(string) || "-keypass".equals(string);
        }
        if (!arguments.isEmpty()) {
            builder.setLength(builder.length() - 1); // trim trailing space
        }
        return builder.toString();
    }

    /**
     * Creates a self-signed Root CA certificate and an intermediate CA certificate
     * (signed by the Root CA certificate) that can be used to sign server certificates.
     * The Root CA certificate file is exported to the same directory as the keystore
     * in formats suitable for Firefox/Chrome/IE (.crt) and Opera (.usr).
     *
     * @param keystore the keystore in which to store everything
     * @param password the password for keystore and keys
     * @param validity the validity period in days, must be greater than 0
     *
     * @throws IOException if keytool was not configured, running keytool application failed or copying the keys failed
     */
    public static void generateProxyCA(File keystore, String password, int validity) throws IOException {
        File caCertCrt = new File(ROOT_CACERT_CRT);
        File caCertUsr = new File(ROOT_CACERT_USR);
        boolean fileExists = false;
        if (!keystore.delete() && keystore.exists()) {
            log.warn("Problem deleting the keystore '" + keystore + "'");
            fileExists = true;
        }
        if (!caCertCrt.delete() && caCertCrt.exists()) {
            log.warn("Problem deleting the certificate file '" + caCertCrt + "'");
            fileExists = true;
        }
        if (!caCertUsr.delete() && caCertUsr.exists()) {
            log.warn("Problem deleting the certificate file '" + caCertUsr + "'");
            fileExists = true;
        }
        if (fileExists) {
            log.warn("If problems occur when recording SSL, delete the files manually and retry.");
        }
        // Create the self-signed keypairs
        KeyToolUtils.genkeypair(keystore, ROOTCA_ALIAS, password, validity, DNAME_ROOT_CA_KEY, "bc:c");
        KeyToolUtils.genkeypair(keystore, INTERMEDIATE_CA_ALIAS, password, validity, DNAME_INTERMEDIATE_CA_KEY, "bc:c");

        // Create cert for CA using root
        ByteArrayOutputStream certReqOut = new ByteArrayOutputStream();
        // generate the request
        KeyToolUtils.keytool("-certreq", keystore, password, INTERMEDIATE_CA_ALIAS, null, certReqOut);

        // generate the certificate and store in output file
        InputStream certReqIn = new ByteArrayInputStream(certReqOut.toByteArray());
        ByteArrayOutputStream genCertOut = new ByteArrayOutputStream();
        KeyToolUtils.keytool("-gencert", keystore, password, ROOTCA_ALIAS, certReqIn, genCertOut, "-ext", "BC:0");

        // import the signed CA cert into the store (root already there) - both are needed to sign the domain certificates
        InputStream genCertIn = new ByteArrayInputStream(genCertOut.toByteArray());
        KeyToolUtils.keytool("-importcert", keystore, password, INTERMEDIATE_CA_ALIAS, genCertIn, null);

        // Export the Root CA for Firefox/Chrome/IE
        KeyToolUtils.keytool("-exportcert", keystore, password, ROOTCA_ALIAS, null, null, "-rfc", "-file", ROOT_CACERT_CRT);
        // Copy for Opera
        if(caCertCrt.exists() && caCertCrt.canRead()) {
            FileUtils.copyFile(caCertCrt, caCertUsr);            
        } else {
            log.warn("Failed creating "+caCertCrt.getAbsolutePath()+", check 'keytool' utility in path is available and points to a JDK >= 7");
        }
    }

    /**
     * Create a host certificate signed with the CA certificate.
     *
     * @param keystore the keystore to use
     * @param password the password to use for the keystore and keys
     * @param host the host, e.g. jmeter.apache.org or *.apache.org; also used as the alias
     * @param validity the validity period for the generated keypair
     *
     * @throws IOException if keytool was not configured or running keytool application failed
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
        String dname = "cn=" + subject + ", o=JMeter Proxy (TEMPORARY TRUST ONLY)";
        String ext = "san=dns:" + subject;
        KeyToolUtils.genkeypair(keystore, alias, password, validity, dname, ext);
        //rem generate cert for DOMAIN using CA and import it

        // get the certificate request
        ByteArrayOutputStream certReqOut = new ByteArrayOutputStream();
        KeyToolUtils.keytool("-certreq", keystore, password, alias, null, certReqOut, "-ext", ext);

        // create the certificate
        //rem ku:c=dig,keyE means KeyUsage:critical=digitalSignature,keyEncipherment
        InputStream certReqIn = new ByteArrayInputStream(certReqOut.toByteArray());
        ByteArrayOutputStream certOut = new ByteArrayOutputStream();
        KeyToolUtils.keytool("-gencert", keystore, password, INTERMEDIATE_CA_ALIAS, certReqIn, certOut, "-ext", "ku:c=dig,keyE", "-ext ", ext);

        // import the certificate
        InputStream certIn = new ByteArrayInputStream(certOut.toByteArray());
        KeyToolUtils.keytool("-importcert", keystore, password, alias, certIn, null, "-noprompt");
    }

    /**
     * List the contents of a keystore
     *
     * @param keystore
     *            the keystore file
     * @param storePass
     *            the keystore password
     * @return the output from the command "keytool -list -v"
     * @throws IOException
     *             if keytool was not configured or running keytool application
     *             failed
     */
    public static String list(final File keystore, final String storePass) throws IOException {
        final File workingDir = keystore.getParentFile();
        final SystemCommand nativeCommand = new SystemCommand(workingDir, null);
        final List<String> arguments = new ArrayList<>();
        arguments.add(getKeyToolPath());
        arguments.add("-list"); // $NON-NLS-1$
        arguments.add("-v"); // $NON-NLS-1$

        arguments.add("-keystore"); // $NON-NLS-1$
        arguments.add(keystore.getName());
        arguments.add("-storepass"); // $NON-NLS-1$
        arguments.add(storePass);
        runNativeCommand(nativeCommand, arguments);
        return nativeCommand.getOutResult();
    }

    /**
     * @param nativeCommand {@link SystemCommand}
     * @param arguments {@link List}
     */
    private static void runNativeCommand(SystemCommand nativeCommand, List<String> arguments) throws IOException {
        try {
            int exitVal = nativeCommand.run(arguments);
            if (exitVal != 0) {
                throw new IOException("Command failed, code: " + exitVal + "\n" + nativeCommand.getOutResult());
            }
        } catch (InterruptedException e) { // NOSONAR 
            throw new IOException("Command was interrupted\n" + nativeCommand.getOutResult(), e);
        }
    }

    /**
     * Returns a list of the CA aliases that should be in the keystore.
     *
     * @return the aliases that are used for the keystore
     */
    public static String[] getCAaliases() {
        return new String[]{ROOTCA_ALIAS, INTERMEDIATE_CA_ALIAS};
    }

    /**
     * Get the root CA alias; needed to check the serial number and fingerprint
     *
     * @return the alias
     */
    public static String getRootCAalias() {
        return ROOTCA_ALIAS;
    }

    /**
     * Helper method to simplify chaining keytool commands.
     *
     * @param command
     *            the command, not null
     * @param keystore
     *            the keystore, not null
     * @param password
     *            the password used for keystore and key, not null
     * @param alias
     *            the alias, not null
     * @param input
     *            where to source input, may be null
     * @param output
     *            where to send output, may be null
     * @param parameters
     *            additional parameters to the command, may be null
     * @throws IOException
     *             if keytool is not configured or running it failed
     */
    private static void keytool(String command, File keystore, String password, String alias,
            InputStream input, OutputStream output, String ... parameters)
            throws IOException {
        final File workingDir = keystore.getParentFile();
        final SystemCommand nativeCommand =
                new SystemCommand(workingDir, 0L, 0, null, input, output, null);
        final List<String> arguments = new ArrayList<>();
        arguments.add(getKeyToolPath());
        arguments.add(command);
        arguments.add("-keystore"); // $NON-NLS-1$
        arguments.add(keystore.getName());
        arguments.add("-storepass"); // $NON-NLS-1$
        arguments.add(password);
        arguments.add("-keypass"); // $NON-NLS-1$
        arguments.add(password);
        arguments.add("-alias"); // $NON-NLS-1$
        arguments.add(alias);
        Collections.addAll(arguments, parameters);

        runNativeCommand(nativeCommand, arguments);
    }

    /**
     * @return flag whether KeyToolUtils#KEYTOOL_PATH is
     *         configured (is not <code>null</code>)
     */
    public static boolean haveKeytool() {
        return KEYTOOL_PATH != null;
    }

    /**
     * @return path to keytool binary
     * @throws IOException
     *             when {@link KeyToolUtils#KEYTOOL_PATH KEYTOOL_PATH} is
     *             <code>null</code>
     */
    private static String getKeyToolPath() throws IOException {
        if (KEYTOOL_PATH == null) {
            throw new IOException("keytool application cannot be found");
        }
        return KEYTOOL_PATH;
    }

    /**
     * Check if keytool can be found
     * @param keytoolPath the path to check
     */
    private static boolean checkKeytool(String keytoolPath) {
        final SystemCommand nativeCommand = new SystemCommand(null, null);
        final List<String> arguments = new ArrayList<>();
        arguments.add(keytoolPath);
        arguments.add("-help"); // $NON-NLS-1$
        try {
            int status = nativeCommand.run(arguments);
            if (log.isDebugEnabled()) {
                log.debug("checkKeyTool:status=" + status);
                log.debug(nativeCommand.getOutResult());
            }
            /*
             * Some implementations of keytool return status 1 for -help
             * MacOS/Java 7 returns 2 if it cannot find keytool
             */
            return status == 0 || status == 1; // TODO this is rather fragile
        } catch (IOException ioe) {
            log.info("Exception checking for keytool existence, will return false, try another way.");
            log.debug("Exception is: ", ioe);
            return false;
        } catch (InterruptedException e) { // NOSONAR
            log.error("Command was interrupted\n" + nativeCommand.getOutResult(), e);
            return false;
        }
    }
}
