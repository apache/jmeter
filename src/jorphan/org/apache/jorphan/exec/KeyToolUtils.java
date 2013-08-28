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

    private KeyToolUtils() {
        // not instantiable
    }

    /**
     * Generate a self-signed keypair using the alias "jmeter",
     * algorithm "RSA" and dname "cn=JMeter Proxy (DO NOT TRUST)"
     * 
     * @param keystore the keystore to create; must not exist
     * @param password the password to use for the store and the key
     * @param validity the validity period in days, greater than 0
     * @throws InterruptedException 
     * @throws IOException
     */
    public static void genkeypair(final File keystore, final String password, int validity) throws IOException, InterruptedException {
        if (keystore.exists()) {
            throw new IOException("Keystore already exists");
        }
        final File workingDir = keystore.getParentFile();
        final SystemCommand nativeCommand = new SystemCommand(workingDir, null);
        final List<String> arguments = new ArrayList<String>();
        arguments.add("keytool"); // $NON-NLS-1$
        arguments.add("-genkeypair"); // $NON-NLS-1$
        arguments.add("-alias"); // $NON-NLS-1$
        arguments.add("jmeter"); // $NON-NLS-1$
        arguments.add("-dname"); // $NON-NLS-1$
        arguments.add("cn=JMeter Proxy (DO NOT TRUST)"); // $NON-NLS-1$
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
