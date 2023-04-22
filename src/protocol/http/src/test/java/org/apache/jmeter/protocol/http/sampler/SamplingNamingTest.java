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

import static org.junit.Assert.assertEquals;

import java.util.stream.Stream;

import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jorphan.test.JMeterSerialTest;
import org.junit.Ignore;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class SamplingNamingTest extends JMeterTestCase implements JMeterSerialTest {
    private static final String JMETER_HOME_PAGE = "https://jmeter.apache.org";
    private static final String LABEL = "JMeter-HP";

    private static Stream<Arguments> getImplementations() {
        return Stream.of(
                Arguments.of(HTTPSamplerFactory.IMPL_HTTP_CLIENT4),
                Arguments.of(HTTPSamplerFactory.IMPL_JAVA));
    }

    @ParameterizedTest(name="Run {index}: implementation:{0}")
    @Ignore(value = "Test produces: We should have at least one sample result, we had none too often")
    @MethodSource("getImplementations")
    void testBug63364(String implementation) {
        TestPlan plan = new TestPlan();
        SampleResult[] subResults = doSample(implementation);
        Assertions.assertTrue(subResults.length > 0, "We should have at least one sample result, we had none");
        for (int i = 0; i < subResults.length; i++) {
            assertEquals("Expected sample label to be " + LABEL + "-" + i, LABEL + "-" + i,
                    subResults[i].getSampleLabel());
        }
        final boolean prevValue = TestPlan.getFunctionalMode();
        plan.setFunctionalMode(true);
        try {
            subResults = doSample(implementation);
            Assertions.assertTrue(subResults.length > 0, "We should have at least one sample result, we had none");
            for (SampleResult subResult : subResults) {
                Assertions.assertTrue(subResult.getSampleLabel().startsWith(JMETER_HOME_PAGE), "Expected sample label to start with " + JMETER_HOME_PAGE);
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
        httpSamplerProxy.setConnectTimeout("15000");
        httpSamplerProxy.setResponseTimeout("10000");
        httpSamplerProxy.setImageParser(true);
        httpSamplerProxy.setMethod("GET");
        httpSamplerProxy.setPath(JMETER_HOME_PAGE);
        // We intentionally keep only resources which start with JMETER_HOME_PAGE
        httpSamplerProxy.setEmbeddedUrlRE(JMETER_HOME_PAGE + ".*");
        SampleResult result = httpSamplerProxy.sample();
        assertEquals("Expected sample label to be " + LABEL, LABEL, result.getSampleLabel());
        return result.getSubResults();
    }
}
