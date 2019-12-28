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

package org.apache.jmeter.functions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import org.apache.jorphan.util.JMeterStopThreadException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class StringFromFileFunctionTest {
    private static Path dir;

    @BeforeAll
    public static void init(@TempDir Path tempDir) throws IOException {
        dir = tempDir;
        Files.write(dir.resolve("SFFTest1.txt"), Arrays.asList("uno", "dos", "tres", "cuatro", "cinco"));
    }

    @Test
    public void SFFTest1() throws Exception {
        Files.write(dir.resolve("SFFTest2.txt"), Arrays.asList("one", "two", "three", "four", "five"));
        Files.write(dir.resolve("SFFTest3.txt"), Arrays.asList("eins", "zwei", "drei", "fier", "fuenf"));
        // It is processed by DecimalFormat, and dots in path would result in
        // IllegalArgumentException: Multiple decimal separators in pattern
        StringFromFile sff1 = createSFF("'" + dir.toString().replaceAll("'", "''") + "/SFFTest'#'.'txt", "", "1", "3");
        try {
            assertEquals("uno", sff1.execute());
            assertEquals("dos", sff1.execute());
            assertEquals("tres", sff1.execute());
            assertEquals("cuatro", sff1.execute());
            assertEquals("cinco", sff1.execute());
            assertEquals("one", sff1.execute());
            assertEquals("two", sff1.execute());
            sff1.execute();
            sff1.execute();
            assertEquals("five", sff1.execute());
            assertEquals("eins", sff1.execute());
            sff1.execute();
            sff1.execute();
            sff1.execute();
            assertEquals("fuenf", sff1.execute());
            assertThrows(JMeterStopThreadException.class, sff1::execute);
        } finally {
            closeFile(sff1);
        }
    }

    @Test
    public void SFFTest2() throws Exception {
        StringFromFile sff = createSFF(dir.resolve("SFFTest1.txt"), "");
        try {
            assertEquals("uno", sff.execute());
            assertEquals("dos", sff.execute());
            assertEquals("tres", sff.execute());
            assertEquals("cuatro", sff.execute());
            assertEquals("cinco", sff.execute());
            assertEquals("uno", sff.execute()); // Restarts
            assertEquals("dos", sff.execute());
            assertEquals("tres", sff.execute());
            assertEquals("cuatro", sff.execute());
            assertEquals("cinco", sff.execute());
        } finally {
            closeFile(sff);
        }
    }

    @Test
    public void SFFTest3() throws Exception {
        StringFromFile sff = createSFF(dir.resolve("SFFTest1.txt"), "", "", "");
        try {
            assertEquals("uno", sff.execute());
            assertEquals("dos", sff.execute());
            assertEquals("tres", sff.execute());
            assertEquals("cuatro", sff.execute());
            assertEquals("cinco", sff.execute());
            assertEquals("uno", sff.execute()); // Restarts
            assertEquals("dos", sff.execute());
            assertEquals("tres", sff.execute());
            assertEquals("cuatro", sff.execute());
            assertEquals("cinco", sff.execute());
        } finally {
            closeFile(sff);
        }
    }

    @Test
    public void SFFTest4() throws Exception {
        StringFromFile sff = createSFF(dir.resolve("InvalidFileName.txt"), "", "", "");
        try {
            assertEquals(StringFromFile.ERR_IND, sff.execute());
            assertEquals(StringFromFile.ERR_IND, sff.execute());
        } finally {
            closeFile(sff);
        }
    }

    // Test that only loops twice
    @Test
    public void SFFTest5() throws Exception {
        StringFromFile sff = createSFF(dir.resolve("SFFTest1.txt"), "", "", "2");
        try {
            assertEquals("uno", sff.execute());
            assertEquals("dos", sff.execute());
            assertEquals("tres", sff.execute());
            assertEquals("cuatro", sff.execute());
            assertEquals("cinco", sff.execute());
            assertEquals("uno", sff.execute());
            assertEquals("dos", sff.execute());
            assertEquals("tres", sff.execute());
            assertEquals("cuatro", sff.execute());
            assertEquals("cinco", sff.execute());
            assertThrows(JMeterStopThreadException.class, sff::execute);
        } finally {
            closeFile(sff);
        }
    }

    private void closeFile(StringFromFile sff) {
        sff.testEnded();
    }

    // Create the StringFromFile function and set its parameters.
    private StringFromFile createSFF(Object... params) throws Exception {
        StringFromFile sff = new StringFromFile();
        sff.setParameters(FunctionTestHelper.makeParams(params));
        return sff;
    }
}
