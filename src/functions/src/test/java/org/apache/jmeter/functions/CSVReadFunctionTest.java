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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jorphan.test.JMeterSerialTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CSVReadFunctionTest extends JMeterTestCase implements JMeterSerialTest {
    private static final Logger log = LoggerFactory.getLogger(CSVReadFunctionTest.class);

    private static String csvFilePath;

    @BeforeAll
    public static void prepareCsv(@TempDir Path dir) throws IOException {
        Path path = dir.resolve("test.csv");
        Files.write(path, Arrays.asList(
                "a1,b1,c1,d1",
                "a2,b2,c2,d2",
                "a3,b3,c3,d3",
                "a4,b4,c4,d4"
        ));
        csvFilePath = path.toString();
    }

    private CSVRead createCsvRead(String fileName, String column) throws Exception {
        CSVRead cr = new CSVRead();
        cr.setParameters(FunctionTestHelper.makeParams(fileName, column));
        return cr;
    }

    private void eq(CSVRead a, String expected) throws InvalidVariableException {
        assertEquals(expected, a.execute(null, null));
    }

    @Test
    void concurrentRequestsToSameCsv() throws Exception {
        CSVRead a = createCsvRead(csvFilePath, "1");
        CSVRead b = createCsvRead(csvFilePath, "next");

        final Synchronizer sync = new Synchronizer();

        synchronized (sync) {
            // The future is created under synchronized block => it ensures it does not start executing too fast
            Future<Void> thread2 = CompletableFuture.runAsync(() -> {
                synchronized (sync) {
                    try {
                        eq(a, "b3");
                        eq(b, "");

                        sync.pass();

                        eq(a, "b1");
                        eq(b, "");
                        eq(a, "b2");

                        sync.pass();

                        eq(b, "");
                        eq(a, "b4");
                        sync.done();
                    } catch (Throwable e) {
                        throw sync.failure(e, "thread2");
                    }
                }
            });

            eq(a, "b1");
            eq(b, "");
            eq(a, "b2");

            sync.pass();

            eq(b, "");
            eq(a, "b4");
            eq(b, "");

            sync.pass();

            eq(a, "b3");
            eq(b, "");

            sync.pass();

            // propagate exception if any
            thread2.get();
        }
    }

    @Test
    public void testCSVParamsFail() throws Exception {
        assertThrows(InvalidVariableException.class, () -> createCsvRead(null, null));
        assertThrows(InvalidVariableException.class, () -> createCsvRead("", null));
    }

    @Test
    public void testCSVNoFile() throws Exception {

        CSVRead cr1 = createCsvRead("does/not-exist.csv", "1");
        log.info("Expecting file not found");
        eq(cr1, "");

        CSVRead cr2 = createCsvRead("does/not-exist.csv", "next");
        log.info("Expecting no entry for file");
        eq(cr2, "");

        CSVRead cr3 = createCsvRead("does/not-exist.csv", "*ABC");
        log.info("Expecting file not found");
        eq(cr3, "");

        CSVRead cr4 = createCsvRead("*ABC", "1");
        log.info("Expecting cannot open file");
        eq(cr4, "");
    }

    @Test
    public void testCSValias() throws Exception {
        CSVRead cr1 = createCsvRead(csvFilePath, "*A");
        CSVRead cr2 = createCsvRead("*A", "1");
        CSVRead cr3 = createCsvRead("*A", "next");

        CSVRead cr4 = createCsvRead(csvFilePath, "*B");
        CSVRead cr5 = createCsvRead("*B", "2");
        CSVRead cr6 = createCsvRead("*B", "next");

        eq(cr1, ""); // open as *A
        eq(cr2, "b1"); // col 1, line 1, *A
        eq(cr4, ""); // open as *B
        eq(cr5, "c1"); // col2 line 1
        eq(cr3, ""); // *A next
        eq(cr2, "b2"); // col 1, line 2, *A
        eq(cr5, "c1"); // col2, line 1, *B
        eq(cr6, ""); // *B next
        eq(cr5, "c2"); // col2, line 2, *B
    }

    // Check blank lines are treated as EOF
    @Test
    public void CSVBlankLine(@TempDir Path tmp) throws Exception {
        String fileName =
                Files.write(
                        tmp.resolve("blank.csv"),
                        Arrays.asList("a1,b1,c1,d1",
                                "a2,b2,c2,d2,",
                                "",
                                "The previous line is blank, and should be treated as EOF"))
                        .toString();
        CSVRead csv1 = createCsvRead(fileName, "1");
        CSVRead csv2 = createCsvRead(fileName, "next");

        for (int i = 1; i <= 2; i++) {
            eq(csv1, "b1");
            eq(csv2, "");
            eq(csv1, "b2");
            eq(csv2, "");
        }
    }

    @Test
    public void CSVRun() throws Exception {
        CSVRead cr1 = createCsvRead(csvFilePath, "1");
        CSVRead cr2 = createCsvRead(csvFilePath, "2");
        CSVRead cr3 = createCsvRead(csvFilePath, "3");
        CSVRead cr4 = createCsvRead(csvFilePath, "next");
        CSVRead cr5 = createCsvRead("", "0");
        CSVRead cr6 = createCsvRead("", "next");

        eq(cr1, "b1");
        eq(cr2, "c1");
        eq(cr3, "d1");

        eq(cr4, "");
        eq(cr1, "b2");
        eq(cr2, "c2");
        eq(cr3, "d2");

        eq(cr4, "");
        eq(cr1, "b3");
        eq(cr2, "c3");
        eq(cr3, "d3");

        eq(cr4, "");
        eq(cr1, "b4");
        eq(cr2, "c4");
        eq(cr3, "d4");

        eq(cr4, "");
        eq(cr1, "b1");
        eq(cr2, "c1");
        eq(cr3, "d1");

        eq(cr5, "a1");
        eq(cr6, "");
        eq(cr5, "a2");
    }
}
