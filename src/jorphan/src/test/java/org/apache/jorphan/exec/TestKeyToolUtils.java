/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jorphan.exec;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.jorphan.util.JOrphanUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

@Execution(ExecutionMode.SAME_THREAD)
public class TestKeyToolUtils {

    private static File keystore;
    private static String password;
    private static final int validity = 1;

    @BeforeAll
    public static void setup(@TempDir Path keystoreDir) throws IOException {
        keystore = keystoreDir.resolve("dummy-keystore.jks").toFile();
        password = JOrphanUtils.generateRandomAlphanumericPassword(32);
        KeyToolUtils.generateProxyCA(keystore, password, validity);
    }

    /*
     * Check the assumption that a missing executable will generate
     * either an IOException or status which is neither 0 nor 1
     */
    @Test
    public void testCheckKeytool() throws Exception {
        SystemCommand sc = new SystemCommand(null, null);
        List<String> arguments = new ArrayList<>();
        arguments.add("xyzqwas"); // should not exist
        Assertions.assertThrows(IOException.class, () -> {
            int status = sc.run(arguments);
            if (status == 0 || status == 1) {
                fail("Missing executable should produce exit code of 0 or 1. Actual code is " + status);
            }
        });
    }

    @Test
    public void testIPBasedCert() throws Exception {
        KeyToolUtils.generateHostCert(keystore, password, "10.1.2.3", validity);
    }

    @Test
    public void testDNSNameBasedCert() throws Exception {
        KeyToolUtils.generateHostCert(keystore, password, "www.example.invalid", validity);
    }

}
