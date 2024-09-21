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

package org.apache.jmeter.engine;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Execution(ExecutionMode.SAME_THREAD) // System.setOut must not be run concurrently with other tests
public class DistributedRunnerTest {

    @SuppressWarnings("CatchAndPrintStackTrace")
    public static void createJmeterEnv() {
        File propsFile;
        try {
            propsFile = File.createTempFile("jmeter", ".properties");
            propsFile.deleteOnExit();
            JMeterUtils.loadJMeterProperties(propsFile.getAbsolutePath());
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        }
        JMeterUtils.setLocale(new Locale("ignoreResources"));
    }

    @Test
    public void testSuccess() {
        createJmeterEnv();
        JMeterUtils.setProperty(DistributedRunner.RETRIES_NUMBER, "1");
        JMeterUtils.setProperty(DistributedRunner.CONTINUE_ON_FAIL, "false");
        DistributedRunnerEmul obj = new DistributedRunnerEmul();
        obj.engines.add(new EmulatorEngine());
        obj.engines.add(new EmulatorEngine());
        List<String> hosts = Arrays.asList("test1", "test2");
        obj.init(hosts, new HashTree());
        obj.start();
        obj.shutdown(hosts);
        obj.stop(hosts);
        obj.exit(hosts);
    }

    @Test
    public void testFailure1() {
        createJmeterEnv();
        JMeterUtils.setProperty(DistributedRunner.RETRIES_NUMBER, "2");
        JMeterUtils.setProperty(DistributedRunner.RETRIES_DELAY, "1");
        JMeterUtils.setProperty(DistributedRunner.CONTINUE_ON_FAIL, "true");
        DistributedRunnerEmul obj = new DistributedRunnerEmul();
        List<String> hosts = Arrays.asList("test1", "test2");
        initRunner(obj, hosts);
        obj.start();
        obj.shutdown(hosts);
        obj.stop(hosts);
        obj.exit(hosts);
    }

    private void initRunner(DistributedRunnerEmul runner, List<String> hosts) {
        PrintStream origSystemOut = System.out;
        ByteArrayOutputStream catchingOut = new ByteArrayOutputStream();
        System.setOut(new PrintStream(catchingOut));
        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> runner.init(hosts, new HashTree()),
                "Expecting IllegalArgumentException since the testplan is invalid"
        );
        if (!ex.getMessage().startsWith("Following remote engines could not be configured:")) {
            throw new AssertionError(
                    "Message should start with 'Following remote engines could not be configured:', actual message was " +
                            ex.getMessage(), ex);
        }
        System.setOut(origSystemOut);
    }

    @Test
    public void testFailure2() {
        createJmeterEnv();
        JMeterUtils.setProperty(DistributedRunner.RETRIES_NUMBER, "1");
        JMeterUtils.setProperty(DistributedRunner.RETRIES_DELAY, "1");
        JMeterUtils.setProperty(DistributedRunner.CONTINUE_ON_FAIL, "false");
        DistributedRunnerEmul obj = new DistributedRunnerEmul();
        List<String> hosts = Arrays.asList("test1", "test2");
        initRunner(obj, hosts);
    }

    @Test
    public void testFailure3() {
        createJmeterEnv();
        JMeterUtils.setProperty(DistributedRunner.RETRIES_NUMBER, "1");
        JMeterUtils.setProperty(DistributedRunner.RETRIES_DELAY, "1");
        JMeterUtils.setProperty(DistributedRunner.CONTINUE_ON_FAIL, "true");
        DistributedRunnerEmul obj = new DistributedRunnerEmul();
        List<String> hosts = Arrays.asList("test1", "test2");
        initRunner(obj, hosts);
        obj.start(hosts);
        obj.shutdown(hosts);
        obj.stop(hosts);
        obj.exit(hosts);
    }

    private static class DistributedRunnerEmul extends DistributedRunner {
        public List<EmulatorEngine> engines = new ArrayList<>();

        @Override
        protected JMeterEngine createEngine(String address) {
            if (engines.isEmpty()) {
                throw new IllegalArgumentException("Throwing on Engine creation to simulate failure");
            }
            EmulatorEngine engine = engines.remove(0);
            engine.setHost(address);
            return engine;
        }
    }

    private static class EmulatorEngine implements JMeterEngine {
        private static final Logger log = LoggerFactory.getLogger(EmulatorEngine.class);
        private String host;

        public EmulatorEngine() {
            log.debug("Creating emulator");
        }

        @Override
        public void configure(HashTree testPlan) {
            log.debug("Configuring {}", host);
        }

        @Override
        public void runTest() {
            log.debug("Running {}", host);
        }

        @Override
        public void stopTest(boolean now) {
            log.debug("Stopping {}", host);
        }

        @Override
        public void reset() {
            log.debug("Resetting {}", host);
        }

        @Override
        public void setProperties(Properties p) {
            log.debug("Set properties {}", host);
        }

        @Override
        public void exit() {
            log.debug("Exiting {}", host);
        }

        @Override
        public boolean isActive() {
            log.debug("Check if active {}", host);
            return false;
        }

        public void setHost(String host) {
            this.host = host;
        }
    }
}
