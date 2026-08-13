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
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

public class TestHTTPSamplerFactory {

    @Test
    void httpClient5IsSelectable() {
        assertTrue(Arrays.asList(HTTPSamplerFactory.getImplementations()).contains("HttpClient5"));

        HTTPSamplerBase sampler = HTTPSamplerFactory.newInstance("HttpClient5");

        assertEquals("HttpClient5", sampler.getImplementation());
        assertInstanceOf(HTTPHC5Impl.class, HTTPSamplerFactory.getImplementation(sampler.getImplementation(), sampler));
    }

    @Test
    void unknownImplementationIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> HTTPSamplerFactory.newInstance("HttpClient6"));
    }

    @Test
    void httpVersionsMatchTheCapabilitiesOfTheImplementation() {
        assertEquals(Arrays.asList("", "HTTP/1.1"),
                Arrays.asList(HTTPSamplerFactory.getHttpVersions("HttpClient4")));
        assertEquals(Arrays.asList("", "HTTP/1.1", "HTTP/2"),
                Arrays.asList(HTTPSamplerFactory.getHttpVersions("Java")));
        assertEquals(Arrays.asList("", "HTTP/1.1", "HTTP/2", "HTTP/2 Strict"),
                Arrays.asList(HTTPSamplerFactory.getHttpVersions("HttpClient5")));
    }

    @Test
    void httpVersionsOfBlankImplementationAreTheOnesOfTheDefaultImplementation() {
        assertEquals(Arrays.asList(HTTPSamplerFactory.getHttpVersions(HTTPSamplerFactory.DEFAULT_CLASSNAME)),
                Arrays.asList(HTTPSamplerFactory.getHttpVersions("")));
    }
}
