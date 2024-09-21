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

package org.apache.jmeter.samplers;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.test.JMeterSerialTest;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class TestDataStrippingSampleSender extends JMeterTestCase implements JMeterSerialTest {

    private static final String TEST_CONTENT = "Something important";

    private static Stream<Arguments> parameters() {
        return Stream.of(
            Arguments.of(TRUE, "", TRUE),
            Arguments.of(TRUE, "", FALSE),
            Arguments.of(FALSE, "", TRUE),
            Arguments.of(FALSE, TEST_CONTENT, FALSE)
        );
    }

    @ParameterizedTest(name = "{index}: is successful sample: {0}, expected content after stripping: {1}, stripOnFailure: {2}")
    @MethodSource("parameters")
    void testSampleOccurred(boolean successfulParent, String content, Boolean stripOnError) throws IOException {
        Path props = Files.createTempFile("mydummy", ".properties");
        JMeterUtils.loadJMeterProperties(props.toString());
        JMeterUtils.getJMeterProperties().setProperty("sample_sender_strip_also_on_error", stripOnError.toString());
        SimpleSender nextSender = new SimpleSender();
        DataStrippingSampleSender sut = new DataStrippingSampleSender(nextSender);
        sut.readResolve();
        SampleResult sample = result(successfulParent, result(result(result())));
        sut.sampleOccurred(event(sample));
        assertResultsHaveContent(content, sample);
        assertThat(sample, CoreMatchers.is(nextSender.getResult()));
    }

    private void assertResultsHaveContent(String content, SampleResult sample) {
        assertThat(sample.getResponseDataAsString(), CoreMatchers.is(content));
        for (SampleResult subResult : sample.getSubResults()) {
            assertResultsHaveContent(content, subResult);
        }
    }

    private static SampleEvent event(SampleResult result) {
        return new SampleEvent(result, "tg-one");
    }

    private static SampleResult result() {
        return result(true);
    }

    private static SampleResult result(SampleResult... subResults) {
        return result(true, subResults);
    }

    private static SampleResult result(boolean isSuccess, SampleResult... subResults) {
        SampleResult result = new SampleResult();
        result.setSuccessful(isSuccess);
        result.setResponseData(TEST_CONTENT, StandardCharsets.UTF_8.name());
        for (SampleResult subResult : subResults) {
            result.addSubResult(subResult);
        }
        return result;
    }

    private static class SimpleSender implements SampleSender {

        private SampleResult result;

        public SampleResult getResult() {
            return result;
        }

        @Override
        public void testEnded() {
            // nothing to implement here
        }

        @Override
        public void testEnded(String host) {
            // nothing to implement here
        }

        @Override
        public void sampleOccurred(SampleEvent e) {
            this.result = e.getResult();

        }

    }
}
