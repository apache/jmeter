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

package org.apache.jmeter.protocol.http.sampler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.apache.jmeter.config.KeystoreConfig;
import org.apache.jmeter.protocol.http.control.AuthManager;
import org.apache.jmeter.protocol.http.control.CacheManager;
import org.apache.jmeter.protocol.http.control.CookieManager;
import org.apache.jmeter.protocol.http.control.DNSCacheManager;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.protocol.http.control.gui.HttpTestSampleGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Regression tests for spurious "Existing XxxManager ... superseded by ..." warnings
 * fired on every thread-group iteration after the first one.
 *
 * <p>Root cause: {@link HTTPSamplerBase#addTestElement(TestElement)} routed CookieManager,
 * CacheManager, AuthManager and DNSCacheManager through their public {@code setXxxManager}
 * methods, which log a warning whenever the existing manager is non-null. Since
 * {@code clearTestElementChildren()} only clears the HeaderManager property, the manager
 * reference survives across iterations and the warning fires N-1 times for N iterations.</p>
 *
 * <p>Fix:</p>
 * <ul>
 *   <li>{@code addTestElement} now routes replace-mode managers through their private
 *       {@code setXxxManagerProperty} methods, mirroring the existing KeystoreConfig path.</li>
 *   <li>The public {@code setXxxManager} methods now suppress the warning when the new
 *       value is the same instance as the existing one (defensive guard for direct calls).</li>
 * </ul>
 *
 * <p>These tests verify both the {@code addTestElement} path (used by TestCompiler on each
 * iteration) and the public setter path. Warnings are captured by redirecting the log4j2
 * "jmeter-log" file appender to a per-class temporary file via the {@code jmeter.logfile}
 * system property (see {@code src/core/src/testFixtures/resources/log4j2.xml"}), so the
 * tests do not depend on the working directory and can be re-run in isolation.</p>
 */
public class HttpSamplerManagerWarningsTest {

    /** log4j2 system property used in src/core/src/testFixtures/resources/log4j2.xml. */
    private static final String JMeter_LOGFILE_PROPERTY = "jmeter.logfile";

    private static Path logFile;
    private static String originalLogfileProperty;

    private HTTPSamplerBase sampler;

    @BeforeAll
    public static void redirectLogFile(@TempDir Path tempDir) {
        logFile = tempDir.resolve("HttpSamplerManagerWarningsTest.log");
        originalLogfileProperty = System.getProperty(JMeter_LOGFILE_PROPERTY);
        System.setProperty(JMeter_LOGFILE_PROPERTY, logFile.toAbsolutePath().toString());
        // Force log4j2 to re-read the configuration so the ${sys:jmeter.logfile:-jmeter.log}
        // lookup in src/core/src/testFixtures/resources/log4j2.xml picks up the new path.
        ((LoggerContext) LogManager.getContext(false)).reconfigure();
    }

    @AfterAll
    public static void restoreLogFile() {
        if (originalLogfileProperty == null) {
            System.clearProperty(JMeter_LOGFILE_PROPERTY);
        } else {
            System.setProperty(JMeter_LOGFILE_PROPERTY, originalLogfileProperty);
        }
        ((LoggerContext) LogManager.getContext(false)).reconfigure();
    }

    @BeforeEach
    public void setUp() {
        sampler = (HTTPSamplerBase) new HttpTestSampleGui().createTestElement();
    }

    /**
     * Provides the 5 replace-mode managers covered by the fix.
     * Each argument is: (manager factory, display name).
     */
    static Stream<Arguments> replaceModeManagers() {
        return Stream.of(
                Arguments.of((ManagerFactory<CookieManager>) CookieManager::new, "CookieManager"),
                Arguments.of((ManagerFactory<CacheManager>) CacheManager::new, "CacheManager"),
                Arguments.of((ManagerFactory<AuthManager>) AuthManager::new, "AuthManager"),
                Arguments.of((ManagerFactory<DNSCacheManager>) DNSCacheManager::new, "DNSCacheManager"),
                Arguments.of((ManagerFactory<KeystoreConfig>) KeystoreConfig::new, "KeystoreConfig")
        );
    }

    /**
     * Scenario 1: simulate N thread-group iterations via {@code addTestElement}.
     * Expected: zero "superseded by" warnings across all iterations.
     */
    @ParameterizedTest(name = "{1}: addTestElement x5 should produce no warning")
    @MethodSource("replaceModeManagers")
    public <T extends TestElement> void addTestElementAcrossIterationsShouldNotWarn(
            ManagerFactory<T> factory, String displayName) throws IOException {
        T manager = factory.create();
        manager.setName(displayName + "-UnderTest");

        long baseline = countSupersededWarningsInLog();
        for (int i = 1; i <= 5; i++) {
            sampler.addTestElement(manager);
        }
        long delta = countSupersededWarningsInLog() - baseline;
        assertEquals(0L, delta,
                "[" + displayName + "] 5 addTestElement iterations produced " + delta
                        + " spurious 'superseded by' warnings (expected 0)");
    }

    /**
     * Scenario 2: public setter called with the SAME instance should not warn (defensive guard).
     * This protects against any caller that re-assigns the same manager instance.
     */
    @ParameterizedTest(name = "{1}: setXxx(sameInstance) should not warn")
    @MethodSource("replaceModeManagers")
    public <T extends TestElement> void setXxxManagerWithSameInstanceShouldNotWarn(
            ManagerFactory<T> factory, String displayName) throws IOException {
        T manager = factory.create();
        manager.setName(displayName + "-Same");

        // First call: sets the manager (no prior value, no warning expected)
        long baseline = countSupersededWarningsInLog();
        setManagerOnSampler(displayName, manager);
        long deltaAfterFirst = countSupersededWarningsInLog() - baseline;
        assertEquals(0L, deltaAfterFirst,
                "[" + displayName + "] initial setXxxManager should not warn");

        // Second call with the same instance: must not warn
        setManagerOnSampler(displayName, manager);
        long deltaAfterSecond = countSupersededWarningsInLog() - baseline;
        assertEquals(0L, deltaAfterSecond,
                "[" + displayName + "] setXxxManager with same instance should not warn");
    }

    /**
     * Scenario 3: public setter called with a DIFFERENT instance must still warn, so that
     * genuine misuses (user attaching multiple managers of the same type) are still detected.
     */
    @ParameterizedTest(name = "{1}: setXxx(differentInstance) should still warn")
    @MethodSource("replaceModeManagers")
    public <T extends TestElement> void setXxxManagerWithDifferentInstanceShouldStillWarn(
            ManagerFactory<T> factory, String displayName) throws IOException {
        T first = factory.create();
        first.setName(displayName + "-First");
        T second = factory.create();
        second.setName(displayName + "-Second");
        assertNotSame(first, second, "test fixture: two distinct instances expected");

        setManagerOnSampler(displayName, first);
        long baseline = countSupersededWarningsInLog();
        setManagerOnSampler(displayName, second);
        long delta = countSupersededWarningsInLog() - baseline;
        assertTrue(delta >= 1,
                "[" + displayName + "] setXxxManager with a different instance must still warn"
                        + " (got " + delta + " warnings, expected >= 1)");
    }

    /**
     * Scenario 4: HeaderManager uses merge (not replace) and is the original target of
     * {@code clearTestElementChildren}. It must not produce "superseded by" warnings either,
     * and the merge accumulation behaviour must keep working across iterations.
     */
    @Test
    public void headerManagerAddTestElementShouldNotWarnAndShouldMerge() throws IOException {
        HeaderManager hm = new HeaderManager();
        hm.setName("HeaderManager-UnderTest");

        long baseline = countSupersededWarningsInLog();
        for (int i = 1; i <= 5; i++) {
            sampler.addTestElement(hm);
        }
        long delta = countSupersededWarningsInLog() - baseline;
        assertEquals(0L, delta,
                "HeaderManager 5 addTestElement iterations produced " + delta
                        + " 'superseded by' warnings (expected 0)");
        // HeaderManager is cleared each iteration and re-merged, so it must remain attached.
        assertNotNull(sampler.getHeaderManager(),
                "HeaderManager should remain attached after iterations");
    }

    /**
     * Scenario 5: end-to-end smoke test that combines all 6 managers in a single sampler
     * across 10 iterations. This mirrors the real-world pattern where a Thread Group has
     * multiple config elements as children.
     */
    @Test
    public void allManagersTogetherAcrossIterationsShouldNotWarn() throws IOException {
        CookieManager cookieManager = new CookieManager();
        cookieManager.setName("HTTP Cookie Manager");
        CacheManager cacheManager = new CacheManager();
        cacheManager.setName("HTTP Cache Manager");
        AuthManager authManager = new AuthManager();
        authManager.setName("HTTP Authorization Manager");
        DNSCacheManager dnsCacheManager = new DNSCacheManager();
        dnsCacheManager.setName("DNS Cache Manager");
        KeystoreConfig keystoreConfig = new KeystoreConfig();
        keystoreConfig.setName("Keystore Config");
        HeaderManager headerManager = new HeaderManager();
        headerManager.setName("HTTP Header Manager");

        long baseline = countSupersededWarningsInLog();
        for (int i = 1; i <= 10; i++) {
            sampler.addTestElement(cookieManager);
            sampler.addTestElement(cacheManager);
            sampler.addTestElement(authManager);
            sampler.addTestElement(dnsCacheManager);
            sampler.addTestElement(keystoreConfig);
            sampler.addTestElement(headerManager);
        }
        long delta = countSupersededWarningsInLog() - baseline;
        assertEquals(0L, delta,
                "10 iterations with 6 managers produced " + delta
                        + " 'superseded by' warnings (expected 0)");

        // Sanity: managers are still attached
        assertNotNull(sampler.getCookieManager());
        assertNotNull(sampler.getCacheManager());
        assertNotNull(sampler.getAuthManager());
        assertNotNull(sampler.getDNSResolver());
        assertNotNull(sampler.getKeystoreConfig());
        assertNotNull(sampler.getHeaderManager());
    }

    // ---------------- helpers ----------------

    /**
     * Routes the given manager to the appropriate public setter on the sampler.
     * Used by the parameterized tests so each manager type is exercised uniformly.
     */
    private void setManagerOnSampler(String displayName, TestElement manager) {
        switch (displayName) {
            case "CookieManager" -> sampler.setCookieManager((CookieManager) manager);
            case "CacheManager" -> sampler.setCacheManager((CacheManager) manager);
            case "AuthManager" -> sampler.setAuthManager((AuthManager) manager);
            case "DNSCacheManager" -> sampler.setDNSResolver((DNSCacheManager) manager);
            case "KeystoreConfig" -> sampler.setKeystoreConfig((KeystoreConfig) manager);
            default -> throw new IllegalArgumentException("Unknown manager type: " + displayName);
        }
    }

    private long countSupersededWarningsInLog() throws IOException {
        return readSupersededLinesFromLog().size();
    }

    private List<String> readSupersededLinesFromLog() throws IOException {
        if (!Files.exists(logFile)) {
            return new ArrayList<>();
        }
        List<String> hits = new ArrayList<>();
        for (String line : Files.readAllLines(logFile, StandardCharsets.UTF_8)) {
            if (line.contains("superseded by")) {
                hits.add(line);
            }
        }
        return hits;
    }

    @FunctionalInterface
    interface ManagerFactory<T extends TestElement> {
        T create();
    }
}
