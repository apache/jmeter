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

/**
 * Package to test FileServer methods
 */

package org.apache.jmeter.config;

import java.util.Locale;

import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestRandomVariableConfig extends JMeterTestCase {

    private static final String RANDOM_VAR_NAME = "randomVar";

    private JMeterVariables threadVars;

    private static final String MIN_VALUE = "0";
    private static final String MAX_VALUE = "10";
    RandomVariableConfig config = new RandomVariableConfig();

    @Before
    public void setUp(){
        JMeterContext jmcx = JMeterContextService.getContext();
        jmcx.setVariables(new JMeterVariables());
        threadVars = jmcx.getVariables();
        config.setRandomSeed("abcd");
        config.setVariableName(RANDOM_VAR_NAME);
    }

    @Test
    public void testRandom() throws Exception {
        config.setMinimumValue(MIN_VALUE);
        config.setMaximumValue(MAX_VALUE);
        for (int i = 0; i < 100;i++) {
            config.iterationStart(null);
            String value = threadVars.get(RANDOM_VAR_NAME);
            Assert.assertNotNull(threadVars.get(RANDOM_VAR_NAME));
            int numericValue = Integer.parseInt(value);
            Assert.assertTrue("value:" + numericValue + " is not in range [" + MIN_VALUE + "," + MAX_VALUE + "]",
                    numericValue >= 0 && numericValue <= 10);
        }
    }

    @Test
    public void testRandomWithFormat() throws Exception {
        // 010.00 requires explicit locale
        Locale prevLocale = Locale.getDefault();
        try {
            Locale.setDefault(Locale.US);
            config.setMinimumValue(MAX_VALUE);
            config.setMaximumValue(MAX_VALUE);
            config.setOutputFormat("000.00");
            config.iterationStart(null);
            String value = threadVars.get(RANDOM_VAR_NAME);
            Assert.assertNotNull(threadVars.get(RANDOM_VAR_NAME));
            Assert.assertEquals("010.00", value);
        } finally {
            Locale.setDefault(prevLocale);
        }
    }

    @Test
    public void testInvalidRange() throws Exception {
        config.setMinimumValue(MAX_VALUE);
        config.setMaximumValue(MIN_VALUE);
        config.iterationStart(null);
        Assert.assertNull(threadVars.get(RANDOM_VAR_NAME));
    }

}
