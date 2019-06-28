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

package org.apache.jmeter.protocol.http.sampler;

import static org.junit.Assert.assertEquals;

import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jorphan.test.JMeterSerialTest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class SamplingNamingTest extends JMeterTestCase implements JMeterSerialTest {
    private static final String JMETER_HOME_PAGE = "https://jmeter.apache.org";
    private static final String LABEL = "JMeter-HP";
    private String implementation;

    public SamplingNamingTest(String implementation) {
        this.implementation = implementation;
    }

    @Parameters(name = "Run {index}: implementation:{0}")
    public static final String[] getImplementations() {
        return new String[]{
                HTTPSamplerFactory.IMPL_HTTP_CLIENT4,
                HTTPSamplerFactory.IMPL_JAVA};
    }

    @Test
    @Parameters(name = "getImplementations")
    public void testBug63364() {
        TestPlan plan = new TestPlan();
        SampleResult[] subResults = doSample(implementation);
        Assert.assertTrue("We have at least one sample result",subResults.length>0);
        for (int i = 0; i < subResults.length; i++) {
            assertEquals(LABEL+"-"+i, subResults[i].getSampleLabel());
        }
        final boolean prevValue = TestPlan.getFunctionalMode();
        plan.setFunctionalMode(true);
        try {
            subResults = doSample(implementation);
            Assert.assertTrue("We have at least one sample result",subResults.length>0);
            for (int i = 0; i < subResults.length; i++) {
                Assert.assertTrue(subResults[i].getSampleLabel().startsWith(JMETER_HOME_PAGE));
            }
        } finally {
            plan.setFunctionalMode(prevValue);
        }
    }


    /**
     * @param implementation HTTP implementation
     * @return array of {@link SampleResult}
     */
    private SampleResult[] doSample(String implementation) {
        HTTPSamplerProxy httpSamplerProxy = new HTTPSamplerProxy(implementation);
        httpSamplerProxy.setName(LABEL);
        httpSamplerProxy.setConnectTimeout("500");
        httpSamplerProxy.setResponseTimeout("1000");
        httpSamplerProxy.setImageParser(true);
        httpSamplerProxy.setMethod("GET");
        httpSamplerProxy.setPath(JMETER_HOME_PAGE);
        // We intentionally keep only resources which start with JMETER_HOME_PAGE
        httpSamplerProxy.setEmbeddedUrlRE(JMETER_HOME_PAGE+".*");
        SampleResult result = httpSamplerProxy.sample();
        assertEquals(LABEL, result.getSampleLabel());
        SampleResult[] subResults = result.getSubResults();
        return subResults;
    }
}
