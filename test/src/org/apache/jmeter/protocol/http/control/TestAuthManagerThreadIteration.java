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
package org.apache.jmeter.protocol.http.control;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.lang.reflect.Field;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.security.auth.Subject;

import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.junit.Before;
import org.junit.Test;

public class TestAuthManagerThreadIteration {
    private JMeterContext jmctx;
    private JMeterVariables jmvars;
    private static final String SAME_USER = "__jmv_SAME_USER";
    private final ConcurrentMap<String, Future<Subject>> subjects = new ConcurrentHashMap<>();

    public KerberosManager initKerberosManager() throws IllegalAccessException, NoSuchFieldException {
        KerberosManager kerberosManager = new KerberosManager();
        Future<Subject> future = Executors.newSingleThreadExecutor().submit(new Callable<Subject>() {
            @Override
            public Subject call() throws Exception {
                return new Subject();
            }
        });
        subjects.put("test", future);
        Field privateField = kerberosManager.getClass().getDeclaredField("subjects");
        privateField.setAccessible(true);
        privateField.set(kerberosManager, subjects);
        return kerberosManager;
    }
    @Before
    public void setUp() {
        jmctx = JMeterContextService.getContext();
        jmvars = new JMeterVariables();
    }

    @Test
    public void testJmeterVariableAuthorizationWhenThreadIterationIsADifferentUser()
            throws IllegalAccessException, NoSuchFieldException {
        //Test button clear after each Iteration
        KerberosManager kerberosManager=initKerberosManager();
        AuthManager authManager = new AuthManager();
        Field authPrivateField = authManager.getClass().getDeclaredField("kerberosManager");
        authPrivateField.setAccessible(true);
        authPrivateField.set(authManager, kerberosManager);
        assertNotNull("Before the iteration, the AuthManager shouldn't be cleared",subjects.get("test"));
        authManager.setControlledByThread(false);
        authManager.setClearEachIteration(true);
        authManager.testIterationStart(null);
        assertNull("After the iteration, the AuthManager should be cleared",subjects.get("test"));
        //Test button controlled by Thread
        kerberosManager=initKerberosManager();
        jmvars.putObject(SAME_USER, false);
        jmctx.setVariables(jmvars);
        authManager.setThreadContext(jmctx);
        authPrivateField.set(authManager, kerberosManager);
        assertNotNull("Before the iteration, the AuthManager shouldn't be cleared",subjects.get("test"));
        authManager.setControlledByThread(true);
        authManager.setClearEachIteration(false);
        authManager.testIterationStart(null);
        assertNull("After the iteration, the AuthManager should be cleared",subjects.get("test"));
    }

    @Test
    public void testJmeterVariableAuthorizationWhenThreadIterationIsASameUser()
            throws IllegalAccessException, NoSuchFieldException {
        // Test button clear after each Iteration
        KerberosManager kerberosManager = initKerberosManager();
        AuthManager authManager = new AuthManager();
        Field authPrivateField = authManager.getClass().getDeclaredField("kerberosManager");
        authPrivateField.setAccessible(true);
        authPrivateField.set(authManager, kerberosManager);
        assertNotNull("Before the iteration, the AuthManager shouldn't be cleared", subjects.get("test"));
        authManager.setControlledByThread(false);
        authManager.setClearEachIteration(false);
        authManager.testIterationStart(null);
        assertNotNull("After the iteration, the AuthManager shouldn't be cleared", subjects.get("test"));
        // Test button controlled by Thread
        kerberosManager = initKerberosManager();
        jmvars.putObject(SAME_USER, true);
        jmctx.setVariables(jmvars);
        authManager.setThreadContext(jmctx);
        authPrivateField.set(authManager, kerberosManager);
        assertNotNull("Before the iteration, the AuthManager shouldn't be cleared", subjects.get("test"));
        authManager.setControlledByThread(true);
        authManager.setClearEachIteration(false);
        authManager.testIterationStart(null);
        assertNotNull("After the iteration, the AuthManager shouldn't be cleared", subjects.get("test"));
    }
}
