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

package org.apache.jmeter.protocol.http.proxy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.jmeter.protocol.http.sampler.HTTPSampler;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DefaultSamplerCreatorTest {

    @ParameterizedTest
    @CsvSource({
            "3,#{name} - #{counter} - #{scheme}://#{host}:#{port}#{path},prefix| - 42 - https://jmeter.invalid:443/some/path",
            "3,#{counter} - #{path},42 - /some/path",
            "3,#{url},https://jmeter.invalid/some/path",
            "3,{0},{0}",
            "3,'{0,number,#.##}','{0,number,#.##}'",
            "0,,prefix|/some/path-42",
            "1,,prefix|-42",
            "2,,prefix|-42 /some/path",
            "4,,/some/path"
    })
    void computeSamplerNameWithCounter(int sampleNameMode, String format, String expectedName) {
        DefaultSamplerCreator samplerCreator = new DefaultSamplerCreator();
        HTTPSamplerBase sampler = new HTTPSampler();
        sampler.setPath("/some/path");
        sampler.setDomain("jmeter.invalid");
        sampler.setMethod("GET");
        sampler.setPort(443);
        sampler.setProtocol("https");
        HttpRequestHdr request = new HttpRequestHdr(
                "prefix|",
                "samplerName",
                sampleNameMode,
                format
        );
        samplerCreator.setCounter(41);
        samplerCreator.computeSamplerName(sampler, request);
        assertEquals(expectedName, sampler.getName());
    }
}
