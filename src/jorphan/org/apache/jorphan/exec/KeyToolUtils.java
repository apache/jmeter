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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Utilities for working with Java keytool
 */
public class KeyToolUtils {

    // The DNAME which is used if none is provided
    private static final String DEFAULT_DNAME = "cn=JMeter Proxy (DO NOT TRUST)";  // $NON-NLS-1$

    private KeyToolUtils() {
        // not instantiable
    }

    /**
     * Generate a self-signed keypair using the algorithm "RSA".
     * 
     * @param keystore the keystore; if it already contains the alias the command will fail
     * @param alias the alias to use, not null 
     * @param password the password to use for the store and the key
     * @param validity the validity period in days, greater than 0
     * @param dname the dname value, if omitted use "cn=JMeter Proxy (DO NOT TRUST)"
     * @param ext if not null, the extension (-ext) to add (e.g. "bc:c")
     * 
     * @throws InterruptedException 
     * @throws IOException
     */
    public static void genkeypair(final File keystore, String alias, final String password, int validity, String dname, String ext) 
            throws IOException, InterruptedException {
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
        if (ext != null) {
            arguments.add("-ext"); // $NON-NLS-1$
            arguments.add(ext);            
        }
        int exitVal = nativeCommand.run(arguments);
        if (exitVal != 0) {
            throw new IOException("Command failed, code: " + exitVal + "\n" + nativeCommand.getOutResult());
        }
    }

    /**
     * List the contents of a keystore
     * 
     * @param keystore the keystore file
     * @param storePass the keystore password
     * @return the output from the command "keytool -list -v"
     */
    public static String list(final File keystore, final String storePass) throws IOException, InterruptedException {
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
        int exitVal = nativeCommand.run(arguments);
        if (exitVal != 0) {
            throw new IOException("Command failed, code: " + exitVal + "\n" + nativeCommand.getOutResult());
        }
        return nativeCommand.getOutResult();
    }
}
