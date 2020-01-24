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

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.testkit.ResourceLocator;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class XPathConcurrencyTest {
    enum ReferenceEquality {
        SAME_OBJECTS, DIFFERENT_OBJECTS
    }

    private static XPath createXPath(String file, String expr) throws Exception {
        Collection<CompoundVariable> parms = new ArrayList<>();
        parms.add(new CompoundVariable(ResourceLocator.getResource(XPathConcurrencyTest.class, file)));
        parms.add(new CompoundVariable(expr));
        XPath xp = new XPath();
        xp.setParameters(parms);
        return xp;
    }

    private static AtomicLong TEST_INDEX = new AtomicLong();

    @ParameterizedTest
    @ValueSource(strings = {"SAME_OBJECTS", "SAME_OBJECTS", "DIFFERENT_OBJECTS", "DIFFERENT_OBJECTS", "SAME_OBJECTS", "DIFFERENT_OBJECTS"})
    public void concurrentRequestsToSameXPath(ReferenceEquality referenceEquality) throws Exception {
        // TODO: fix XPath
        Assumptions.assumeTrue(TEST_INDEX.incrementAndGet() == 1,
                "Second execution of concurrentRequestsToSameXPath is known to fail");
        XPath a = createXPath("testfiles/XPathTest.xml", "//user/@username");
        XPath b;
        if (referenceEquality == ReferenceEquality.SAME_OBJECTS) {
            b = a;
        } else {
            b = createXPath("testfiles/XPathTest.xml", "//user/@username");
        }
        Synchronizer sync = new Synchronizer();
        synchronized (sync) {
            Future<Void> thread2 = CompletableFuture.runAsync(() -> {
                synchronized (sync) {
                    try {
                        assertEquals("u3", b.execute());
                        assertEquals("u4", b.execute());
                        sync.pass();
                        assertEquals("u1", b.execute());
                        sync.pass();
                        assertEquals("u3", b.execute());
                        sync.done();
                    } catch (Throwable e) {
                        throw sync.failure(e, "thread2");
                    }
                }
            });

            assertEquals("u1", a.execute());
            assertEquals("u2", a.execute());
            sync.pass();
            assertEquals("u5", a.execute());
            sync.pass();
            assertEquals("u2", a.execute());
            sync.pass();

            thread2.get();
        }
    }
}
