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
 */

package org.apache.jmeter.protocol.jms.sampler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.util.Locale;

import org.apache.jmeter.protocol.jms.control.gui.JMSPublisherGui;
import org.apache.jmeter.util.JMeterUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.github.benmanes.caffeine.cache.Cache;

/**
 *
 */
public class PublisherSamplerTest {

    @Before
    public void initJMeter() {
        JMeterUtils.setLocale(new Locale("ignoreResources"));
    }

    @After
    public void resetJMeter() {
        JMeterUtils.setLocale(Locale.ENGLISH);
    }

    @Test
    public void noopCache() {
        Cache<Object,Object> noopCache = PublisherSampler.buildCache(JMSPublisherGui.USE_RANDOM_RSC);
        assertEquals(0, noopCache.estimatedSize());

        String key  = "key";
        String val1 = "1st time";
        assertSame(val1, noopCache.get(key, k -> val1));

        String val2 = "2nd call";
        assertSame(val2, noopCache.get(key, k -> val2));
    }
}
