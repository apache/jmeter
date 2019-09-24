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

package org.apache.jmeter.functions;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.testkit.Resources;
import org.junit.jupiter.api.Test;

public class CsvConcurrencyTest {
    private CSVRead createCsvRead(String fileName, String column) throws Exception {
        Collection<CompoundVariable> parms = new ArrayList<>();
        parms.add(new CompoundVariable(Resources.getResourceFilePath(getClass(), fileName)));
        parms.add(new CompoundVariable(column));
        CSVRead cr = new CSVRead();
        cr.setParameters(parms);
        return cr;
    }

    @Test
    void concurrentRequestsToSameCsv() throws Exception {
        CSVRead a = createCsvRead("testfiles/unit/FunctionsPackageTest.csv", "1");
        CSVRead b = createCsvRead("testfiles/unit/FunctionsPackageTest.csv", "next");

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

    private void eq(CSVRead a, String expected) throws InvalidVariableException {
        assertEquals(expected, a.execute(null, null));
    }
}
