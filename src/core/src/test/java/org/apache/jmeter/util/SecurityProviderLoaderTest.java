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

package org.apache.jmeter.util;

import java.security.Provider;
import java.security.Security;
import java.util.Arrays;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

public class SecurityProviderLoaderTest {

    @AfterEach
    public void removeAllDummyProviders() {
        Security.removeProvider(DummyProvider.PROVIDER_NAME);
        Security.removeProvider(DummyProviderWithConfig.PROVIDER_NAME);
        Assert.assertNull(Security.getProvider(DummyProvider.PROVIDER_NAME));
        Assert.assertNull(Security.getProvider(DummyProviderWithConfig.PROVIDER_NAME));
    }

    @Test
    public void addSecurityProviderTest() {
        removeAllDummyProviders();
        Provider[] providers = Security.getProviders();
        int providersCountBefore = providers.length;

        SecurityProviderLoader.addSecurityProvider(DummyProvider.class.getName());

        Provider[] providersAfter = Security.getProviders();
        Provider provider = Security.getProvider(DummyProvider.PROVIDER_NAME);
        try {
            Assert.assertEquals(providersCountBefore + 1, providersAfter.length);
            Assert.assertNotNull("Provider not installed.", provider);
            Assert.assertEquals(DummyProvider.class, provider.getClass());
            Assert.assertEquals(provider, providersAfter[providersAfter.length - 1]);
        } catch (AssertionError e){
            Arrays.stream(providers).forEach(pro -> System.err.println(pro.getName()));
            throw e;
        }
    }

    @Test
    public void addSecurityProviderTestWithConfigForUnconfigurableProvider() {
        removeAllDummyProviders();
        int providersCountBefore = Security.getProviders().length;

        SecurityProviderLoader.addSecurityProvider(DummyProvider.class.getName()+":0:Configure");

        Provider[] providersAfter = Security.getProviders();
        Provider provider = Security.getProvider(DummyProvider.PROVIDER_NAME);

        Assert.assertEquals(providersCountBefore + 1, providersAfter.length);
        Assert.assertNotNull("Provider not installed.", provider);
        Assert.assertEquals(DummyProvider.class, provider.getClass());
        Assert.assertEquals(provider, providersAfter[providersAfter.length - 1]);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "java.lang.Object", "org.apache.jmeter.util.SecurityProviderLoaderTest.UnknownProvider"})
    public void addInvalidProviderClassTest(String invalidClassname) {
        removeAllDummyProviders();
        int providersCountBefore = Security.getProviders().length;

        SecurityProviderLoader.addSecurityProvider(invalidClassname);

        int providersCountAfter = Security.getProviders().length;

        Assert.assertEquals(providersCountBefore, providersCountAfter);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3})
    public void addSecurityProviderWithPositionTest(int position) {
        removeAllDummyProviders();
        int providersCountBefore = Security.getProviders().length;

        SecurityProviderLoader.addSecurityProvider(DummyProvider.class.getName() + ":" + position);

        Provider[] providersAfter = Security.getProviders();
        Provider provider = Security.getProvider(DummyProvider.PROVIDER_NAME);

        Assert.assertEquals(providersCountBefore + 1, providersAfter.length);
        Assert.assertNotNull(provider);
        Assert.assertEquals(DummyProvider.class, provider.getClass());
        Assert.assertEquals(provider, providersAfter[position == 0 ? providersAfter.length - 1 : position - 1]);
    }

    @ParameterizedTest
    @CsvSource({":0:TestConfig,0", ":2:TEST,2", ":3:TEST,3"})
    public void addSecurityProviderWithPositionAndConfigTest(String config, int position) {
        removeAllDummyProviders();
        int providersCountBefore = Security.getProviders().length;

        SecurityProviderLoader.addSecurityProvider(DummyProviderWithConfig.class.getName() + config);

        Provider[] providersAfter = Security.getProviders();
        Provider provider = Security.getProvider(DummyProviderWithConfig.PROVIDER_NAME);

        Assert.assertNotNull("Provider not installed.", provider);
        Assert.assertEquals(providersCountBefore + 1, providersAfter.length);
        Assert.assertEquals(DummyProviderWithConfig.class, provider.getClass());
        Assert.assertEquals(provider, providersAfter[position == 0 ? providersAfter.length - 1 : position - 1]);
        Assert.assertEquals(config.substring(config.lastIndexOf(":") + 1), ((DummyProviderWithConfig) provider).getConfig());


    }

    @Test
    public void addSecurityProvidersViaProperties() {
        removeAllDummyProviders();
        int providersCountBefore = Security.getProviders().length;

        Properties properties = new Properties();
        properties.put("security.provider.1", DummyProviderWithConfig.class.getName() + ":2:CONFIG");
        properties.put("security.provider", DummyProvider.class.getName() + ":1");

        SecurityProviderLoader.addSecurityProvider(properties);

        Provider[] providersAfter = Security.getProviders();
        Assert.assertEquals(providersCountBefore + 2, providersAfter.length);

        Provider provider = Security.getProvider(DummyProvider.PROVIDER_NAME);
        Provider providerWithConfig = Security.getProvider(DummyProviderWithConfig.PROVIDER_NAME);

        Assert.assertNotNull("Provider not installed.", provider);
        Assert.assertEquals(DummyProvider.class, provider.getClass());
        Assert.assertEquals(provider, providersAfter[0]);

        Assert.assertNotNull("Provider not installed.", providerWithConfig);
        Assert.assertEquals(DummyProviderWithConfig.class, providerWithConfig.getClass());
        Assert.assertEquals(providerWithConfig, providersAfter[1]);
        Assert.assertEquals("CONFIG", ((DummyProviderWithConfig) providerWithConfig).getConfig());
    }

    public static class DummyProvider extends Provider {
        private static final long serialVersionUID = 1L;
        public static final String PROVIDER_NAME = "DUMMY";

        public DummyProvider() {
            super(PROVIDER_NAME, 1.0, PROVIDER_NAME);
        }

    }

    public static class DummyProviderWithConfig extends Provider {
        private static final long serialVersionUID = 1L;
        public static final String PROVIDER_NAME = "DUMMY_CONFIG";

        private String config = null;

        public DummyProviderWithConfig() {
            super(PROVIDER_NAME, 1.0, PROVIDER_NAME);
        }

        public DummyProviderWithConfig(String config) {
            this();
            this.config = config;
        }

        public String getConfig() {
            return config;
        }
    }
}
